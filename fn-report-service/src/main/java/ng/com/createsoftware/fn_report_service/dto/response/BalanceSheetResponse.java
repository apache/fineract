package ng.com.createsoftware.fn_report_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceSheetResponse {
    private BigDecimal assets;
    private BigDecimal liabilities;
    private BigDecimal equity;
}
