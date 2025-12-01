package ng.com.createsoftware.fn_account_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="account_officers")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountOfficer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String firstName;
    private String lastName;

    private String phone;
    @Email
    private String email;

    private String branchCode;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    @CreationTimestamp
    private Instant createdAt = Instant.now();
    @UpdateTimestamp
    private Instant updatedAt = Instant.now();


}

