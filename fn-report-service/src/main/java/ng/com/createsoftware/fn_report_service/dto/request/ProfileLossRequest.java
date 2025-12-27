package ng.com.createsoftware.fn_report_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfileLossRequest {
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal profit;
}
