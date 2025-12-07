package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralLedgerRequest {
    private String code;
    private String name;
    private Long categoryId;
    private BigDecimal balance;
}
