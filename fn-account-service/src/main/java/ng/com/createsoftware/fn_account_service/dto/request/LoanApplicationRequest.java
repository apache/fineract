package ng.com.createsoftware.fn_account_service.dto.request;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {
    private Long clientId;
    private Long productId;
    private BigDecimal principal;
    private Integer loanTermFrequency;
}
