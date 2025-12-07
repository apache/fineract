package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Builder;
import lombok.Data;
import ng.com.createsoftware.fn_accounting_service.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GLTransactionRequest {
    private Long ledgerId;
    private BigDecimal amount;
    private TransactionType type;
    private String reference;
    private String narration;
    private LocalDateTime timestamp;
}
