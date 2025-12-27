package ng.com.createsoftware.fn_account_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public  class AuditEvent {
    private  String eventType;
    private  String service;
    private  String system;
    private String entityId;
    private LocalDateTime timestamp;
}
