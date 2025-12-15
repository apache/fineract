package ng.com.createsoftware.fn_agency_banking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="till_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TillTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private BigDecimal amount = BigDecimal.ZERO;
    private String accountNumber;
    private String performedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name="till_id")
    private Till till;

    public TillTransaction(String reference, TransactionType type, BigDecimal amount, Till till, String accountNumber, String performedBy) {
        this.reference = reference;
        this.type = type;
        this.amount = amount;
        this.till = till;
        this.performedBy = performedBy;
        this.accountNumber = accountNumber;
    }
}
