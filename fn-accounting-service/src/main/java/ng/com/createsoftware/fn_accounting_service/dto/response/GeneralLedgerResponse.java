package ng.com.createsoftware.fn_accounting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralLedgerResponse {
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private BigDecimal balance;
}
