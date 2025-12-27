package ng.com.createsoftware.fn_audit_service.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_audit_service.model.AuditLog;
import ng.com.createsoftware.fn_audit_service.repository.AuditRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {
    private final AuditRepository auditRepository;

    public void consumeAuditEvent(AuditEvent event ){
        log.info("Received Audit event: {}", event);
        AuditLog auditLog = new AuditLog();
        auditLog.setEventType(event.getEventType());
        auditLog.setAction(event.getActor());
        auditLog.setUsername(event.getService());
        auditLog.setDetails(event.getEntityId());
        auditLog.setTimestamp(event.getTimestamp());

        auditRepository.save(auditLog);
    }
}
