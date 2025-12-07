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
public class IncomeStatement {
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}
