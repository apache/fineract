package ng.com.createsoftware.fn_postings_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargePostingRequest {
    private String customerId;
    private String accountNumber;
    private BigDecimal amount;
    private String chargeType;
}
