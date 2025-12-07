package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String timestamp;
}
