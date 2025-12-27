package ng.com.createsoftware.fn_account_service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(String type, String entity){
        AuditEvent event = new AuditEvent(
                type,
                "account-officer-service",
                "SYSTEM",
                entity,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(
                "audit.exchange",
                "audit.event",
                event
        );
    }
}
