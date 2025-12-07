package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="batch_upload_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUploadRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private BatchType type;

    private Integer rowsProcessed = 0;
    private Integer rowsCreated = 0;

    @Column(columnDefinition = "TEXT")
    private String errors;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    public BatchUploadRecord(String filename, BatchType type) {
        this.filename = filename;
        this.type = type;
        this.uploadedAt = LocalDateTime.now();
    }
}
