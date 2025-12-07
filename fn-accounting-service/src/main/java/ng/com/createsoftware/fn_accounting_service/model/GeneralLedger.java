package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="general_ledgers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String name;

    @ManyToOne
    @JoinColumn(name="account_category_id")
    private AccountCategory category;

    private BigDecimal balance = BigDecimal.ZERO;
}
