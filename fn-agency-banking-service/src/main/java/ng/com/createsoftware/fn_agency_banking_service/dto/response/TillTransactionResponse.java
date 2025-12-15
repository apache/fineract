package ng.com.createsoftware.fn_agency_banking_service.dto.response;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ng.com.createsoftware.fn_agency_banking_service.model.Till;
import ng.com.createsoftware.fn_agency_banking_service.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TillTransactionResponse {
    private Long id;

    private String reference;
    private TransactionType type;

    private BigDecimal amount = BigDecimal.ZERO;
    private String accountNumber;
    private String performedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String tillName;
}
