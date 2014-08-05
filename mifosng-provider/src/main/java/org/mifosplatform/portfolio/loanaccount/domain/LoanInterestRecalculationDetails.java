/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity for holding interest recalculation setting, details will be copied
 * from product directly
 * 
 * @author conflux
 */

@Entity
@Table(name = "m_loan_recalculation_details")
public class LoanInterestRecalculationDetails extends AbstractPersistable<Long> {

    @OneToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    /**
     * {@link InterestRecalculationCompoundingMethod}
     */
    @Column(name = "compound_type_enum", nullable = false)
    private Integer interestRecalculationCompoundingMethod;

    /**
     * {@link LoanRescheduleStrategyMethod}
     */
    @Column(name = "reschedule_strategy_enum", nullable = false)
    private Integer rescheduleStrategyMethod;

    protected LoanInterestRecalculationDetails() {
        // Default constructor for jpa repository
    }

    private LoanInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
    }

    public static LoanInterestRecalculationDetails createFrom(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod) {
        return new LoanInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod);
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public void update(final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
    }

}
