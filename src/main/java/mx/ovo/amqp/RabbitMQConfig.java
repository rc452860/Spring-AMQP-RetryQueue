package mx.ovo.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange chdeppExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.CHDEPP").durable(true).build();
    }

    @Bean
    public TopicExchange retryExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.RetryExchange").durable(true).build();
    }

    @Bean
    public TopicExchange failedExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.FailedExchange").durable(true).build();
    }

    @Bean
    public Queue testQueue() {
        return new Queue("Citms.Exchange.Test", true);
    }

    @Bean
    public Queue retryQueue() {
        Map args = new ConcurrentHashMap<>(3);
        // 将消息重新投递到emailExchange中
        args.put("x-dead-letter-exchange", "Citms.Exchange.CHDEPP");
        args.put("x-dead-letter-routing-key", "test.retry");
        //消息在队列中延迟30s后超时，消息会重新投递到x-dead-letter-exchage对应的队列中，routingkey为自己指定
        args.put("x-message-ttl", 5 * 1000);
        return QueueBuilder.durable("Citms.Exchange.RetryQueue").withArguments(args).build();
    }

    @Bean
    public Queue failedQueue() {
        return QueueBuilder.durable("Citms.Exchange.FailedQueue").build();
    }

    @Bean
    public Binding topicQueueBinding(@Qualifier("testQueue") Queue queue,
                                     @Qualifier("chdeppExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.#");
    }

    @Bean
    public Binding retryDirectBinding(@Qualifier("retryQueue") Queue queue,
                                      @Qualifier("retryExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.retry.#");
    }

    @Bean
    public Binding failDirectBinding(@Qualifier("failedQueue") Queue queue,
                                     @Qualifier("failedExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.faild.#");
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate amqpTemplate = new RabbitTemplate(connectionFactory);
        // 在消息没有被路由到合适的队列情况下，Broker会将消息返回给生产者，
        // 为true时如果Exchange根据类型和消息Routing Key无法路由到一个合适的Queue存储消息，
        // Broker会调用Basic.Return回调给handleReturn()，再回调给ReturnCallback，将消息返回给生产者。
        // 为false时，丢弃该消息
        amqpTemplate.setMandatory(true);

        // 消息确认，需要配置 spring.rabbitmq.publisher-confirms = true
        amqpTemplate.setConfirmCallback(((correlationData, ack, cause) -> {
            //根据返回的状态，生产者可以处理失败与成功的相应信息，比如发送失败，可重发，转发或者存入日志等
            //if(ack){
            //    correlationData.getId()为message唯一标识，需要生产者发送message时传入自定义的correlationData才能获取到，否则为null
            //    //do something
            //}else{
            //    correlationData.getId()
            //    //do something
            //}

            //此处只做打印，不对生产者发送失败的信息处理
            log.warn("ConfirmCallBackListener：correlationData=" + correlationData + "，ack=" + ack + "，cause=" + cause);
        }));


        // 消息发送失败返回到队列中，需要配置spring.rabbitmq.publisher-returns = true
        amqpTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.warn("ReturnCallBackListener：message=" + new String(message.getBody()) + "，replyCode=" + replyCode + "，replyText=" + replyText + "，exchange=" + exchange + "，routingKey=" + routingKey);
        });

        return amqpTemplate;
    }
}
