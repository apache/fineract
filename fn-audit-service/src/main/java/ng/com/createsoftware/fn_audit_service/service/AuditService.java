package ng.com.createsoftware.fn_audit_service.service;

import ng.com.createsoftware.fn_audit_service.dto.request.AuditRequest;
import ng.com.createsoftware.fn_audit_service.model.AuditLog;

import java.util.List;

public interface AuditService {
    void auditLog(AuditRequest request);
    List<AuditLog> allAudits();
}
