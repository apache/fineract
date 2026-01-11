package ng.com.createsoftware.fn_accounting_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JournalEntryRequest {
    private String officeName;
    private String paymentDetails;
    private Long glAccountId;
    private String currencyCode;
    private Long reversalJournalEntryId;
    private String transactionId;
    private Long loanTransactionId;
    private Long savingsTransactionId;
    private Long clientTransactionId;
    private Long shareTransactionId;
    private boolean reversed = false;
    private boolean manualEntry = false;
    private LocalDate transactionDate;
    private Integer type;
    private BigDecimal amount;
    private String description;
    private Integer entityType;
    private Long entityId;
    private String referenceNumber;
    private LocalDate submittedOnDate;
}
