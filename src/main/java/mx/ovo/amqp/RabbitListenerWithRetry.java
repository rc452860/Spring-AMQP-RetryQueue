package mx.ovo.amqp;

import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * @author kitami
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
public @interface RabbitListenerWithRetry {
    String[] queues() default {};

    String exchange() default "";

    String topic() default "";

    int retry() default 3;
}
