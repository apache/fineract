package ng.com.createsoftware.fn_customer_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="customer_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private String productCode; //eg SAVINGS-001
    private String currency = "NGN";
    private BigDecimal balance = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @ManyToOne
    @JoinColumn(name="customer_id")
    private Customer customer;

    private LocalDateTime createdAt = LocalDateTime.now();

    public CustomerAccount(String accountNumber, String productCode, Customer customer) {
        this.accountNumber = accountNumber;
        this.productCode = productCode;
        this.customer = customer;
    }
}
