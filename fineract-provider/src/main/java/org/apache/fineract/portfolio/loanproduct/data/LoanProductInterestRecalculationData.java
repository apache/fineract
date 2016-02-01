/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanproduct.data;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.preCloseInterestCalculationStrategy;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.joda.time.LocalDate;

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
    private final EnumOptionData recalculationCompoundingFrequencyType;
    private final Integer recalculationCompoundingFrequencyInterval;
    private final LocalDate recalculationCompoundingFrequencyDate;
    @SuppressWarnings("unused")
    private final boolean isArrearsBasedOnOriginalSchedule;
    @SuppressWarnings("unused")
    private final EnumOptionData preClosureInterestCalculationStrategy;

    public LoanProductInterestRecalculationData(final Long id, final Long productId,
            final EnumOptionData interestRecalculationCompoundingType, final EnumOptionData rescheduleStrategyType,
            final EnumOptionData recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final LocalDate recalculationRestFrequencyDate, final EnumOptionData recalculationCompoundingFrequencyType,
            final Integer recalculationCompoundingFrequencyInterval, final LocalDate recalculationCompoundingFrequencyDate,
            final boolean isArrearsBasedOnOriginalSchedule, final EnumOptionData preCloseInterestCalculationStrategy) {
        this.id = id;
        this.productId = productId;
        this.interestRecalculationCompoundingType = interestRecalculationCompoundingType;
        this.rescheduleStrategyType = rescheduleStrategyType;
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        this.recalculationRestFrequencyInterval = recalculationRestFrequencyInterval;
        this.recalculationRestFrequencyDate = recalculationRestFrequencyDate;
        this.recalculationCompoundingFrequencyType = recalculationCompoundingFrequencyType;
        this.recalculationCompoundingFrequencyInterval = recalculationCompoundingFrequencyInterval;
        this.recalculationCompoundingFrequencyDate = recalculationCompoundingFrequencyDate;
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
        final EnumOptionData recalculationCompoundingFrequencyType = null;
        final Integer recalculationCompoundingFrequencyInterval = null;
        final LocalDate recalculationCompoundingFrequencyDate = null;
        final boolean isArrearsBasedOnOriginalSchedule = false;
        final EnumOptionData preCloseInterestCalculationStrategy = preCloseInterestCalculationStrategy(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE);
        return new LoanProductInterestRecalculationData(id, productId, interestRecalculationCompoundingType, rescheduleStrategyType,
                recalculationRestFrequencyType, recalculationRestFrequencyInterval, recalculationRestFrequencyDate,
                recalculationCompoundingFrequencyType, recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyDate,
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

    public EnumOptionData getRecalculationCompoundingFrequencyType() {
        return this.recalculationCompoundingFrequencyType;
    }

    public Integer getRecalculationCompoundingFrequencyInterval() {
        return this.recalculationCompoundingFrequencyInterval;
    }

    public LocalDate getRecalculationCompoundingFrequencyDate() {
        return this.recalculationCompoundingFrequencyDate;
    }

}
