package ng.com.createsoftware.fn_accounting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GLTransactionResponse {
    private Long id;
    private Long ledgerId;
    private BigDecimal amount;
    private String type;
    private String reference;
    private String narration;
    private LocalDateTime timestamp;
}
