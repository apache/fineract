package ng.com.createsoftware.fn_loaning_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name="loan_repayment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loanId;
    private Double amount;
    private LocalDate date = LocalDate.now();

    public LoanRepayment(Long loanId, Double amount) {
        this.loanId = loanId;
        this.amount = amount;
    }
}
