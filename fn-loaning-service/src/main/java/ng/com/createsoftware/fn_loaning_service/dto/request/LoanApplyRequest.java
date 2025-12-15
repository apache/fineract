package ng.com.createsoftware.fn_loaning_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplyRequest {
    private Long customerId;
    private Long productId;
    private BigDecimal amount;
    private Integer tenureMonths;
}
