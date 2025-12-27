package ng.com.createsoftware.fn_report_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossResponse {
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal profit;
}
