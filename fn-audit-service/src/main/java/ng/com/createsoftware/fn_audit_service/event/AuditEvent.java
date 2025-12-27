package ng.com.createsoftware.fn_audit_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String eventType;
    private String service;
    private String actor;
    private String entityId;
    private LocalDateTime timestamp;
}
