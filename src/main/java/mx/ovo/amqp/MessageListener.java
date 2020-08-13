package mx.ovo.amqp;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    AmqpTemplate amqpTemplate;

    @RabbitListenerWithRetry(
            queues = "Citms.Exchange.Test"
    )
    public void onMessage(Message message, Channel channel) {

    }

//    @RabbitListener(queues = "Citms.Exchange.Test")
//    public void onMessage(Message message, Channel channel) throws IOException {
//        try {
//            log.info("接收到消息:{}", new String(message.getBody(), StandardCharsets.UTF_8));
//            throw new RuntimeException("手动报错");
//        } catch (Throwable e) {
//            long retryCount = getRetryCount(message.getMessageProperties());
//
//            if (retryCount >= 3) {
//
//                /** 重试次数超过3次,则将消息发送到失败队列等待特定消费者处理或者人工处理 */
//                try {
//                    amqpTemplate.convertAndSend("Citms.Exchange.FailedExchange", "test.faild", message);
//                    log.info("消费者消费消息在重试3次后依然失败，将消息发送到faild队列,发送消息:" + new String(message.getBody()));
//                } catch (Exception e1) {
//                    log.error("消息在发送到faild队列的时候报错:" + e1.getMessage() + ",原始消息:" + new String(message.getBody()));
//                }
//
//            } else {
//
//                try {
//                    /** 重试次数不超过3次,则将消息发送到重试队列等待重新被消费（重试队列延迟超时后信息被发送到相应死信队列重新消费，即延迟消费）*/
//                    amqpTemplate.convertAndSend("Citms.Exchange.RetryExchange", "test.retry", message);
//                    log.info("消费者消费失败，消息发送到重试队列;" + "原始消息：" + new String(message.getBody()) + ";第" + (retryCount + 1) + "次重试");
//                } catch (Exception e1) {
//                    log.error("消息发送到重试队列的时候，异常了:" + e1.getMessage() + ",重新发送消息");
//                }
//            }
//        } finally {
//            /**
//             * 无论消费成功还是消费失败,都要手动进行ack,因为即使消费失败了,也已经将消息重新投递到重试队列或者失败队列
//             * 如果不进行ack,生产者在超时后会进行消息重发,如果消费者依然不能处理，则会存在死循环
//             */
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        }
//    }

    private long getRetryCount(MessageProperties messageProperties) {
        Long retryCount = 0L;
        if (null != messageProperties) {
            List<Map<String, ?>> deaths = messageProperties.getXDeathHeader();
            if (deaths != null && deaths.size() > 0) {
                Map death = (Map) deaths.get(0);
                retryCount = (Long) death.get("count");
            }

        }
        return retryCount;
    }
}
