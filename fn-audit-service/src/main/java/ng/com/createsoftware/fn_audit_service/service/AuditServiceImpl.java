package ng.com.createsoftware.fn_audit_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_audit_service.dto.request.AuditRequest;
import ng.com.createsoftware.fn_audit_service.model.AuditLog;
import ng.com.createsoftware.fn_audit_service.repository.AuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService{
    private final AuditRepository auditRepository;

    @Override
    public void auditLog(AuditRequest request) {
        var log  = new AuditLog();
        log.setUsername(request.getUsername());
        log.setAction(request.getAction());
        log.setDetails(request.getDetails());
        log.setTimestamp(LocalDateTime.now());
        auditRepository.save(log);
    }

    @Override
    public List<AuditLog> allAudits() {
        return auditRepository.findAll();
    }
}
