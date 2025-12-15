package ng.com.createsoftware.fn_agency_banking_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TillCustomerRequest {
    private Long tillId;
    private String accountNumber;
    private BigDecimal amount;
    private String performedBy;
}
