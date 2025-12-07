package ng.com.createsoftware.fn_accounting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private  String username;
    private String action;
    private String details;
    private Long timestamp = System.currentTimeMillis();

    public AuditEvent(String username, String action, String details) {
        this.username = username;
        this.action = action;
        this.details = details;
    }
}
