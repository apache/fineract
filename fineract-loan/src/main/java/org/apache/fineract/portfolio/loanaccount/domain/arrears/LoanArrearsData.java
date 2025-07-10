package org.apache.fineract.portfolio.loanaccount.domain.arrears;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanArrearsData {
    private BigDecimal principalOverdue;
    private BigDecimal interestOverdue;
    private BigDecimal feeOverdue;
    private BigDecimal penaltyOverdue;
    private BigDecimal totalOverdue;

    private LocalDate overDueSince;

    private boolean isOverdue;
}
