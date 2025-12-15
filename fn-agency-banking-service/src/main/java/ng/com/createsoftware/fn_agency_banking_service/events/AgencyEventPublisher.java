package ng.com.createsoftware.fn_agency_banking_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Component
public class AgencyEventPublisher {
    private  final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();


    @Value("${agency.events.exchange}")
    private String exchange;

    @Value("${agency.events.routing.tillTransfer}")
    private String routingTill;

    @Value("${agency.events.routing.posTxn}")
    private String routingPos;

    @Value("${agency.events.routing.ussdTxn}")
    private String routingUssd;

    public AgencyEventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    private void publish(String routingKey, Map<String, Object> payload){
         try {
                 var json = mapper.writeValueAsString(payload);
                 amqpTemplate.convertAndSend(exchange, routingKey, json);
              }catch(Exception ex){
                 System.out.println(ex.getMessage());
              }
    }

    public void publishTillTransfer(Long tillId, String type, BigDecimal amount, String performedBy){
        publish(routingTill, Map.of(
                "event", "till.transfer",
                "tillId", tillId,
                "type", type,
                "amount", amount,
                "performedBy", performedBy,
                "ts", Instant.now().toString()
        ));
    }

    public void publishPosTxn(String posId, BigDecimal amount, String accountNumber){
        publish(routingPos, Map.of(
                "event", "pos.txn",
                "posId", posId,
                "amount", amount,
                "accountNumber", accountNumber,
                "ts", Instant.now().toString()
        ));
    }

    public void publishUssdTxn(String sessionId, String ussdCode, BigDecimal amount, String accountNumber){
        publish(routingUssd, Map.of(
                "event", "ussd.txn",
                "sessionId", sessionId,
                "ussdCode", ussdCode,
                "amount", amount,
                "accountNumber", accountNumber,
                "ts", Instant.now().toString()
        ));
    }
}
