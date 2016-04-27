package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_interest_recalculation_additional_details")
public class LoanInterestRecalcualtionAdditionalDetails extends AbstractPersistable<Long> {

    @Temporal(TemporalType.DATE)
    @Column(name = "effective_date")
    private Date effectiveDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected LoanInterestRecalcualtionAdditionalDetails() {

    }

    public LoanInterestRecalcualtionAdditionalDetails(final LocalDate effectiveDate, final BigDecimal amount) {
        if (effectiveDate != null) {
            this.effectiveDate = effectiveDate.toDate();
        }
        this.amount = amount;
    }

    public LocalDate getEffectiveDate() {
        return new LocalDate(this.effectiveDate);
    }

    public BigDecimal getAmount() {
        return this.amount;
    }
}
