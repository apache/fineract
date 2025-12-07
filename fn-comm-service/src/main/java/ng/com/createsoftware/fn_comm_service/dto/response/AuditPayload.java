package ng.com.createsoftware.fn_comm_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor@NoArgsConstructor
public class AuditPayload {
    private String eventType;
    private String details;
    private String timestamp;
}
