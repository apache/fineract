package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="gl_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GLTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name="gl_id")
    private GeneralLedger ledger;

    private BigDecimal amount;

    private TransactionType type;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String narration;

    private LocalDateTime timestamp = LocalDateTime.now();
}
