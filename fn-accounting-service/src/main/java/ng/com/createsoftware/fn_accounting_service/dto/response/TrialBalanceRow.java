package ng.com.createsoftware.fn_accounting_service.dto.response;

import java.math.BigDecimal;

public record TrialBalanceRow(String code, String name, BigDecimal balance) {
}
