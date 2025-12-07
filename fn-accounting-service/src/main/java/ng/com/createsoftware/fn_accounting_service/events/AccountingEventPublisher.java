package ng.com.createsoftware.fn_accounting_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.config.RabbitConfig;
import ng.com.createsoftware.fn_accounting_service.dto.response.AuditEvent;
import ng.com.createsoftware.fn_accounting_service.dto.response.AuditPayload;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountingEventPublisher {
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    //exchange/route names should exist and be bound to the audit consumer
    @Value("${rabbitmq.exchange.name: audit.exchange}")
    private  String auditExchange;

    @Value("${rabbitmq.routing.ey: audit.accounting}")
    private final String auditRoutingKey;

    public void publishAuditEvent(String username, String action, String details){
        var payload = new AuditEvent(username, action, details);
             try {
                     String json = mapper.writeValueAsString(payload);
                     amqpTemplate.convertAndSend(auditExchange, auditRoutingKey, json);
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }

    public void publishTransactionEvent(GLTransaction transaction){
        AuditPayload payload = AuditPayload.builder()
                .eventType("ACCOUNTING_TRANSACTION")
                .timestamp(LocalDateTime.now().toString())
                .details("GL Txn ID: " + transaction.getId() + ", Amt: " + transaction.getAmount())
                .build();
//        RabbitConfig config = new RabbitConfig();
        amqpTemplate.convertAndSend(
               auditExchange,
               auditRoutingKey,
                payload
        );
    }

    public void publishBatchEvent(int count){
        AuditPayload payload = AuditPayload.builder()
                .eventType("ACCOUNTING_BATCH")
                .timestamp(LocalDateTime.now().toString())
                .details("Processed " + count + " GL transactions")
                .build();

        amqpTemplate.convertAndSend(
                auditExchange,
                auditRoutingKey,
                payload
        );
    }
}
