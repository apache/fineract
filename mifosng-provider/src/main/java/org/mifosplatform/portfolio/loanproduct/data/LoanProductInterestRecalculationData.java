/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.preCloseInterestCalculationStrategy;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;

public class LoanProductInterestRecalculationData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long productId;
    private final EnumOptionData interestRecalculationCompoundingType;
    private final EnumOptionData rescheduleStrategyType;
    private final EnumOptionData recalculationRestFrequencyType;
    private final Integer recalculationRestFrequencyInterval;
    private final LocalDate recalculationRestFrequencyDate;
    @SuppressWarnings("unused")
    private final boolean isArrearsBasedOnOriginalSchedule;
    @SuppressWarnings("unused")
    private final EnumOptionData preClosureInterestCalculationStrategy;

    public LoanProductInterestRecalculationData(final Long id, final Long productId,
            final EnumOptionData interestRecalculationCompoundingType, final EnumOptionData rescheduleStrategyType,
            final EnumOptionData recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final LocalDate recalculationRestFrequencyDate, boolean isArrearsBasedOnOriginalSchedule,
            final EnumOptionData preCloseInterestCalculationStrategy) {
        this.id = id;
        this.productId = productId;
        this.interestRecalculationCompoundingType = interestRecalculationCompoundingType;
        this.rescheduleStrategyType = rescheduleStrategyType;
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        this.recalculationRestFrequencyInterval = recalculationRestFrequencyInterval;
        this.recalculationRestFrequencyDate = recalculationRestFrequencyDate;
        this.isArrearsBasedOnOriginalSchedule = isArrearsBasedOnOriginalSchedule;
        this.preClosureInterestCalculationStrategy = preCloseInterestCalculationStrategy;
    }

    public static LoanProductInterestRecalculationData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final Long productId = null;
        final EnumOptionData interestRecalculationCompoundingType = interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.NONE);
        final EnumOptionData rescheduleStrategyType = rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT);
        final EnumOptionData recalculationRestFrequencyType = null;
        final Integer recalculationRestFrequencyInterval = null;
        final LocalDate recalculationRestFrequencyDate = null;
        final boolean isArrearsBasedOnOriginalSchedule = false;
        final EnumOptionData preCloseInterestCalculationStrategy = preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE);
        return new LoanProductInterestRecalculationData(id, productId, interestRecalculationCompoundingType, rescheduleStrategyType,
                recalculationRestFrequencyType, recalculationRestFrequencyInterval, recalculationRestFrequencyDate,
                isArrearsBasedOnOriginalSchedule, preCloseInterestCalculationStrategy);
    }

    public EnumOptionData getInterestRecalculationCompoundingType() {
        return this.interestRecalculationCompoundingType;
    }

    public EnumOptionData getRescheduleStrategyType() {
        return this.rescheduleStrategyType;
    }

    public LocalDate getRecalculationRestFrequencyDate() {
        return this.recalculationRestFrequencyDate;
    }

    public EnumOptionData getRecalculationRestFrequencyType() {
        return this.recalculationRestFrequencyType;
    }

    public Integer getRecalculationRestFrequencyInterval() {
        return this.recalculationRestFrequencyInterval;
    }

}
