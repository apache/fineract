package ng.com.createsoftware.fn_report_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanBalanceResponse {
    private Long loanId;
    private BigDecimal outstanding;
}
