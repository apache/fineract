/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity for capturing interest recalculation settings
 * 
 * @author conflux
 */

@Entity
@Table(name = "m_product_loan_recalculation_details")
public class LoanProductInterestRecalculationDetails extends AbstractPersistable<Long> {

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

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

    protected LoanProductInterestRecalculationDetails() {
        //
    }

    public static LoanProductInterestRecalculationDetails createFrom(final JsonCommand command) {

        final Integer interestRecalculationCompoundingMethod = InterestRecalculationCompoundingMethod.fromInt(
                command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName)).getValue();

        final Integer loanRescheduleStrategyMethod = LoanRescheduleStrategyMethod.fromInt(
                command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName)).getValue();

        return new LoanProductInterestRecalculationDetails(interestRecalculationCompoundingMethod, loanRescheduleStrategyMethod);
    }

    private LoanProductInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
    }

    public void updateProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public Integer getInterestRecalculationCompoundingMethod() {
        return this.interestRecalculationCompoundingMethod;
    }

    public Integer getRescheduleStrategyMethod() {
        return this.rescheduleStrategyMethod;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final String localeAsInput) {

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
                this.interestRecalculationCompoundingMethod)) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName);
            actualChanges.put(LoanProductConstants.interestRecalculationCompoundingMethodParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.interestRecalculationCompoundingMethod = InterestRecalculationCompoundingMethod.fromInt(newValue).getValue();
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName,
                this.rescheduleStrategyMethod)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName);
            actualChanges.put(LoanProductConstants.rescheduleStrategyMethodParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.rescheduleStrategyMethod = LoanRescheduleStrategyMethod.fromInt(newValue).getValue();
        }
    }
}
