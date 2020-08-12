package mx.ovo.amqp;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange chdeppExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.CHDEPP").build();
    }

    @Bean
    public TopicExchange retryExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.RetryExchange").durable(true).build();
    }

    @Bean
    public TopicExchange failedExchange() {
        return (TopicExchange) ExchangeBuilder.topicExchange("Citms.Exchange.FaildExchange").durable(true).build();
    }

    @Bean
    public Queue testQueue() {
        return QueueBuilder.durable("mx.ovo.test").build();
    }

    @Bean
    public Queue retryQueue() {
        Map args = new ConcurrentHashMap<>(3);
        // 将消息重新投递到emailExchange中
        args.put("x-dead-letter-exchange", "emailExchange");
        args.put("x-dead-letter-routing-key", "email.topic.retry");
        //消息在队列中延迟30s后超时，消息会重新投递到x-dead-letter-exchage对应的队列中，routingkey为自己指定
        args.put("x-message-ttl", 30 * 1000);
        return QueueBuilder.durable("retryQueue").withArguments(args).build();
    }

    @Bean
    public Queue failedQueue() {
        return QueueBuilder.durable("faildQueue").build();
    }

    @Bean
    public Binding topicQueueBinding(@Qualifier("testQueue") Queue queue,
                                     @Qualifier("chdeppExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.*");
    }

    @Bean
    public Binding retryDirectBinding(@Qualifier("retryQueue") Queue queue,
                                      @Qualifier("retryExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.retry.*");
    }

    @Bean
    public Binding failDirectBinding(@Qualifier("failedQueue") Queue queue,
                                     @Qualifier("failedExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.faild.*");
    }

}
