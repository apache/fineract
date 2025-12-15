package ng.com.createsoftware.fn_loaning_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name="loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Double amount;;
    private Integer tenure;
    private Double interestRate;

    private LocalDate applicationDate = LocalDate.now();
    private LocalDate approvalDate;
    private LocalDate disbursementDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public Loan(Long customerId, Double amount, Integer tenure, Double interestRate) {
        this.customerId = customerId;
        this.amount = amount;
        this.tenure = tenure;
        this.interestRate = interestRate;
    }
}
