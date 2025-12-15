package ng.com.createsoftware.fn_postings_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountTransferRequest {
    private String fromCustomerId;
    private String fromAccountNumber;
    private String toCustomerId;
    private String toAccountNumber;
    private BigDecimal amount;
}
