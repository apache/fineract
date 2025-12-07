package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="capitals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal amount;

    private LocalDateTime createdAt = LocalDateTime.now();
}
