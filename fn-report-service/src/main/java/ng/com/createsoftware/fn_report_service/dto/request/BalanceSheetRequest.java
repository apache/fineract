package ng.com.createsoftware.fn_report_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceSheetRequest {
    private BigDecimal assets;
    private BigDecimal liabilities;
    private BigDecimal equity;
}
