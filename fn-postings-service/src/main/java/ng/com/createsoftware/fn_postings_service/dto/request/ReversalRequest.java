package ng.com.createsoftware.fn_postings_service.dto.request;

import lombok.Data;

@Data
public class ReversalRequest {
    private String transactionId;
    private String reason;
}
