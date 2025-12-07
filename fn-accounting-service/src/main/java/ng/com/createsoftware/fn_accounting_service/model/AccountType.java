package ng.com.createsoftware.fn_accounting_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="account_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
