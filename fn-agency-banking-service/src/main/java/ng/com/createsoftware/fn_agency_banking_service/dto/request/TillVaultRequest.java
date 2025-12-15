package ng.com.createsoftware.fn_agency_banking_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TillVaultRequest {
    private Long vaultId;
    private Long tillId;
    private BigDecimal amount;
    private String performedBy;
}
