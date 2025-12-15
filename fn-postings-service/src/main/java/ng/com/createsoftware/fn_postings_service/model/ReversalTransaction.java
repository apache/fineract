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
public class ReversalTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String originalTransactionId;

    private BigDecimal amount;


    private LocalDateTime timestamp;
    private String reason;
}
