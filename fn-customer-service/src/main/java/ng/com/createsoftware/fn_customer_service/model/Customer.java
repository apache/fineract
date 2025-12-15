package ng.com.createsoftware.fn_customer_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="customers")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    @Column(unique = true, length = 11)
    private String bvn;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();
}
