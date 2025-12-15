package ng.com.createsoftware.fn_agency_banking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${agency.events.exchange: 'domain.exchange'}")
    private String domainExchange;

    public static final String AGENCY_TILL_QUEUE = "agency.till.queue";
    public static final String AGENCY_POS_QUEUE = "agency.pos.queue";

    @Bean
    public TopicExchange domainExchange(){
        return new TopicExchange(domainExchange, true, false);
    }


    @Bean
    public Queue tillQueue(){
        return QueueBuilder.durable(AGENCY_TILL_QUEUE).build();
    }

    @Bean
    public Queue posQueue(){
        return QueueBuilder.durable(AGENCY_POS_QUEUE).build();
    }


    @Bean
    public Binding bindTill(Queue tillQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(tillQueue()).to(domainExchange()).with("agency.till.*");
    }

    @Bean
    public Binding bindPos(Queue posQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(posQueue()).to(domainExchange()).with("agency.pos.*");
    }
}
