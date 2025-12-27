package ng.com.createsoftware.fn_report_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanBalanceRequest {
    private Long loanId;
    private BigDecimal outstanding;
}
