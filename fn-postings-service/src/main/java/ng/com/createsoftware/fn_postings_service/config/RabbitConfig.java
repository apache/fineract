package ng.com.createsoftware.fn_postings_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Value("${posting.events.exchange: 'posting.exchange'}")
    private String postingExchange;

    @Value("${posting.events.routing.key: 'posting.event'}")
    public String postingRoutingKey;


    @Value("${posting.events.routing.name: 'posting.event.queue'}")
    public String postingQueue;

    @Bean
    public TopicExchange postingExchange(){
        return new TopicExchange(postingExchange, true, false);
    }


    @Bean
    public Queue postingQueue(){
        return new Queue(postingQueue, true);
    }


    @Bean
    public Binding postingBinding(){
        return BindingBuilder.bind(postingQueue()).to(postingExchange()).with(postingRoutingKey);
    }
}
