package ng.com.createsoftware.fn_accounting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GLSummaryRow {
    private String code;
    private String name;
    private BigDecimal debits;
    private BigDecimal credits;
    private BigDecimal endingBalance;
}
