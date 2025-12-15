package ng.com.createsoftware.fn_customer_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class CustomerEventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${customer.events.exchange}")
    private String exchange;
    @Value("${customer.events.routing.created}")
    private String routingCreated;
    @Value("${customer.events.routing.bvnVerified}")
    private String routingBvn;

    public CustomerEventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void publishCreated(Long customerId, String firstName, String lastName){
             try {
                     var payload = Map.of(
                             "eventType", "customer.created",
                             "customerId", customerId,
                             "firstName", firstName,
                             "lastName", lastName,
                             "timestamp", Instant.now().toString()
                     ) ;
                     String json = mapper.writeValueAsString(payload);
                     amqpTemplate.convertAndSend(exchange, routingCreated, json);
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }

    public void publishBvnVerified(Long customerId, String bvn, boolean verified){
        try{
            var payload = Map.of(
                    "eventType", "customer.bvn.verified",
                    "customerId", customerId,
                    "bvn", bvn,
                    "verified", verified,
                    "timestamp", Instant.now().toString()
            );
            String json = mapper.writeValueAsString(payload);
            amqpTemplate.convertAndSend(exchange, routingBvn, json);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

}
