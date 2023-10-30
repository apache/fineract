package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;

public record LoanTermVariationParams(boolean skipPeriod, boolean recalculateAmounts, LocalDate scheduledDueDate,
        List<LoanTermVariationsData> variationsData) {
}
