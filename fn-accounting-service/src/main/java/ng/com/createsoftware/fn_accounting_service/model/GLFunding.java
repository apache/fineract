package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="gl-funding")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GLFunding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="gl_id")
    private GeneralLedger ledger;

    private BigDecimal amount;
    private String source;
    private LocalDateTime timestamp = LocalDateTime.now();
}
