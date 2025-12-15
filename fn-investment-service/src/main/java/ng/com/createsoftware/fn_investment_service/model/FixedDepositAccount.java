package ng.com.createsoftware.fn_investment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="fixed_deposits")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FixedDepositAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private Long fineractAccountId;
    private BigDecimal principal;
    private Integer tenureMonths;
    private Status status;
    private LocalDate startDate;
    private LocalDate maturityDate;
}
