package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="outbox_event")
@Setter
@Getter
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;
    private boolean published = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime publishedAt;

}
