package ng.com.createsoftware.fn_account_service.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ng.com.createsoftware.fn_account_service.model.Status;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class AccountOfficerRequest {
//    private String id;

    @Column(nullable = true)
    private Long staffId;

    @NotBlank
    private String code;
    @NotBlank
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    private String branchCode;
    private String status;

//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
}

