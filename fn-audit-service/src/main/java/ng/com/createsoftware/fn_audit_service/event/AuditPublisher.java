package ng.com.createsoftware.fn_audit_service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publish(String action, Object payload) {
        rabbitTemplate.convertAndSend(
                "audit.exchange",
                "audi.event",
                Map.of(
                        "action", action,
                        "payload", payload,
                        "timestamp", Instant.now()
                )
        );
    }
}
