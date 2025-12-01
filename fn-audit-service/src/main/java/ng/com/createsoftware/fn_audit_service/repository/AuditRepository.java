package ng.com.createsoftware.fn_audit_service.repository;

import ng.com.createsoftware.fn_audit_service.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {
}
