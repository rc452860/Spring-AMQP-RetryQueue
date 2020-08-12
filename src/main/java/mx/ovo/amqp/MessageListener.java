package mx.ovo.amqp;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mx.ovo.test", durable = "true"),
            exchange = @Exchange(value = "Citms.Exchange.CHDEPP", ignoreDeclarationExceptions = "true", type = "topic"),
            key = "test#"
    ))
    public void onMessage(Message message, Channel channel) throws InterruptedException {
        log.info(new String(message.getBody()));
        Thread.sleep(1000);
        throw new RuntimeException("aaa");
    }
}
