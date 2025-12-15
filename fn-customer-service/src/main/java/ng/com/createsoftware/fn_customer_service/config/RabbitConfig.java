package ng.com.createsoftware.fn_customer_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${customer.events.exchange: 'domain.exchange'}")
    private String domainExchange;

    @Value("${customer.events.routing.created: 'customer.created'}")
    public String customerCreatedRouting;

    @Value("${customer.events.routing.bvnVerified: 'customer.bvn.verified'}")
    private String bvnVerifiedRouting;

    public static final String CUSTOMER_CREATED_QUEUE = "customer.created.queue";
    public static final String BVN_QUEUE = "customer.bvn.queue";

    @Bean
    public TopicExchange domainExchange(){
        return new TopicExchange(domainExchange, true, false);
    }

    @Bean
    public Queue customerCreatedQueue(){
        return QueueBuilder.durable(CUSTOMER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue bvnQueue(){
        return QueueBuilder.durable(BVN_QUEUE).build();
    }

    @Bean
    public Binding bindCustomerCreated(Queue customerCreatedQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(customerCreatedQueue).to(domainExchange).with(customerCreatedRouting);
    }

    @Bean
    public Binding bindBvnVerified(Queue bvnQueue, TopicExchange domainExchange){
        return BindingBuilder.bind(bvnQueue).to(domainExchange).with(bvnVerifiedRouting);
    }

}
