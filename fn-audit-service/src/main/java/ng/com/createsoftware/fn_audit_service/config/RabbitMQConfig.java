package ng.com.createsoftware.fn_audit_service.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AUDIT_EXCHANGE = "audit.exchange";
    public static final String AUDIT_QUEUE = "audit.queue";
    public static final String AUDIT_ROUTING_KEY = "audit.#";

    @Bean
    public TopicExchange auditExchange(){
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Queue auditQueue(){
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Binding auditBinding(){
        return BindingBuilder
                .bind(auditQueue())
                .to(auditExchange())
                .with(AUDIT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
