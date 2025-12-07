package ng.com.createsoftware.fn_accounting_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.exchange.name: 'audit.exchange'}")
    private String exchangeName;

    public static final String AUDIT_EXCHANGE = "audit.exchange";

    @Value("${rabbitmq.queue.name: 'audit.accounting.queue'}")
    public String queueName;

    @Value("${rabbitmq.routing.key: audit.accounting}")
    private String routingKey;

    @Bean
    public TopicExchange auditExchange(){
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue accoutingQueue(){
        return new Queue(queueName, true);
    }

    @Bean
    public Binding accountingBinding(Queue activityQueue, DirectExchange activityExchange){
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(routingKey);
    }

}
