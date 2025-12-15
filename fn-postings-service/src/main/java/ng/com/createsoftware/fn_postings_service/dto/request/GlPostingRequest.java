package ng.com.createsoftware.fn_postings_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GlPostingRequest {
    private BigDecimal amount;
    private String narration;
}
