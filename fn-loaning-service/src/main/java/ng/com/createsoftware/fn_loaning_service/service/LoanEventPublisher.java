package ng.com.createsoftware.fn_loaning_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class LoanEventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${loan.events.exchange}")
    private String exchange;

    @Value("${loan.events.routing.created}")
    private String createdRouting;

    @Value("${loan.events.routing.approved}")
    private String approvedRouting;

    @Value("${loan.events.routing.disbursed}")
    private String disbursedRouting;

    @Value("${loan.events.routing.repayment}")
    private String repaymentRouting;

    public LoanEventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    private void publish(String routingKey, Map<String, Object>payload){
             try {
                     String json = mapper.writeValueAsString(payload);
                     amqpTemplate.convertAndSend(exchange, routingKey, json);
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }

    public void loanCreated(Long loanId, Long customerId){
        publish(createdRouting, Map.of("event", "loan.created", "loanId", loanId, "customerId", customerId, "ts", Instant.now()));
    }

    public void loanApproved(Long loanId){
        publish(approvedRouting, Map.of("event", "loan.approved",
                "loanId", loanId,
                "ts", Instant.now()));
    }

    public void loanDisbursed(Long loanId){
        publish(disbursedRouting, Map.of("event", "loan.disbursed",
                "loanId", loanId,
                "ts", Instant.now()));
    }

    public void repaymentMade(Long loanId, Double amount){
        publish(repaymentRouting, Map.of("event", "loan.repaid",
                "loanId", loanId,
                "amount", amount,
                "ts", Instant.now()));
    }
}
