package ng.com.createsoftware.fn_investment_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddFixedDepositRequest {
    private Long customerId;
    private Long productId;
    private BigDecimal amount;
    private Integer tenureMonths;
}
