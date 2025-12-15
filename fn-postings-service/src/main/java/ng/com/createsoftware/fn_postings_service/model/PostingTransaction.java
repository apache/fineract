package ng.com.createsoftware.fn_postings_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String customerId;
    private String accountNumber;

    private String glAccountId;
    private BigDecimal amount;

    private  PostingType type;
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    private String reference;
}
