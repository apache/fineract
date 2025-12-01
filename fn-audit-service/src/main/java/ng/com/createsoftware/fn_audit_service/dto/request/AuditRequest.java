package ng.com.createsoftware.fn_audit_service.dto.request;

import lombok.Data;

@Data
public class AuditRequest {
    private String username;
    private String action;
    private String details;
}
