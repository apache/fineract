package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CapitalRequest {
    private Long id;
    private String name;
    private BigDecimal amount;
}
