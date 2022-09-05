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

import java.io.Serializable;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;

@Getter
public class LoanProductInterestRecalculationData implements Serializable {

    private final Long id;
    private final Long productId;
    private final EnumOptionData interestRecalculationCompoundingType;
    private final EnumOptionData rescheduleStrategyType;
    private final EnumOptionData recalculationRestFrequencyType;
    private final Integer recalculationRestFrequencyInterval;
    private final EnumOptionData recalculationRestFrequencyNthDay;
    private final EnumOptionData recalculationRestFrequencyWeekday;
    private final Integer recalculationRestFrequencyOnDay;
    private final EnumOptionData recalculationCompoundingFrequencyType;
    private final Integer recalculationCompoundingFrequencyInterval;
    private final EnumOptionData recalculationCompoundingFrequencyNthDay;
    private final EnumOptionData recalculationCompoundingFrequencyWeekday;
    private final Integer recalculationCompoundingFrequencyOnDay;
    private final boolean isArrearsBasedOnOriginalSchedule;
    private final boolean isCompoundingToBePostedAsTransaction;
    private final EnumOptionData preClosureInterestCalculationStrategy;
    private final boolean allowCompoundingOnEod;

    public LoanProductInterestRecalculationData(final Long id, final Long productId,
            final EnumOptionData interestRecalculationCompoundingType, final EnumOptionData rescheduleStrategyType,
            final EnumOptionData recalculationRestFrequencyType, final Integer recalculationRestFrequencyInterval,
            final EnumOptionData recalculationRestFrequencyNthDay, final EnumOptionData recalculationRestFrequencyWeekday,
            final Integer recalculationRestFrequencyOnDay, final EnumOptionData recalculationCompoundingFrequencyType,
            final Integer recalculationCompoundingFrequencyInterval, final EnumOptionData recalculationCompoundingFrequencyNthDay,
            final EnumOptionData recalculationCompoundingFrequencyWeekday, final Integer recalculationCompoundingFrequencyOnDay,
            final boolean isArrearsBasedOnOriginalSchedule, boolean isCompoundingToBePostedAsTransaction,
            final EnumOptionData preCloseInterestCalculationStrategy, final boolean allowCompoundingOnEod) {
        this.id = id;
        this.productId = productId;
        this.interestRecalculationCompoundingType = interestRecalculationCompoundingType;
        this.rescheduleStrategyType = rescheduleStrategyType;
        this.recalculationRestFrequencyType = recalculationRestFrequencyType;
        this.recalculationRestFrequencyInterval = recalculationRestFrequencyInterval;
        this.recalculationRestFrequencyNthDay = recalculationRestFrequencyNthDay;
        this.recalculationRestFrequencyOnDay = recalculationRestFrequencyOnDay;
        this.recalculationRestFrequencyWeekday = recalculationRestFrequencyWeekday;
        this.recalculationCompoundingFrequencyType = recalculationCompoundingFrequencyType;
        this.recalculationCompoundingFrequencyInterval = recalculationCompoundingFrequencyInterval;
        this.recalculationCompoundingFrequencyNthDay = recalculationCompoundingFrequencyNthDay;
        this.recalculationCompoundingFrequencyOnDay = recalculationCompoundingFrequencyOnDay;
        this.recalculationCompoundingFrequencyWeekday = recalculationCompoundingFrequencyWeekday;
        this.isArrearsBasedOnOriginalSchedule = isArrearsBasedOnOriginalSchedule;
        this.preClosureInterestCalculationStrategy = preCloseInterestCalculationStrategy;
        this.isCompoundingToBePostedAsTransaction = isCompoundingToBePostedAsTransaction;
        this.allowCompoundingOnEod = allowCompoundingOnEod;
    }

    public static LoanProductInterestRecalculationData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final Long productId = null;
        final EnumOptionData interestRecalculationCompoundingType = interestRecalculationCompoundingType(
                InterestRecalculationCompoundingMethod.NONE);
        final EnumOptionData rescheduleStrategyType = rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT);
        final EnumOptionData recalculationRestFrequencyType = null;
        final Integer recalculationRestFrequencyInterval = null;
        final EnumOptionData recalculationRestFrequencyNthDay = null;
        final EnumOptionData recalculationRestFrequencyWeekday = null;
        final Integer recalculationRestFrequencyOnDay = null;
        final EnumOptionData recalculationCompoundingFrequencyType = null;
        final Integer recalculationCompoundingFrequencyInterval = null;
        final EnumOptionData recalculationCompoundingFrequencyNthDay = null;
        final EnumOptionData recalculationCompoundingFrequencyWeekday = null;
        final Integer recalculationCompoundingFrequencyOnDay = null;
        final boolean isArrearsBasedOnOriginalSchedule = false;
        final boolean isCompoundingToBePostedAsTransaction = false;
        final EnumOptionData preCloseInterestCalculationStrategy = preCloseInterestCalculationStrategy(
                LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE);
        final boolean allowCompoundingOnEod = false;
        return new LoanProductInterestRecalculationData(id, productId, interestRecalculationCompoundingType, rescheduleStrategyType,
                recalculationRestFrequencyType, recalculationRestFrequencyInterval, recalculationRestFrequencyNthDay,
                recalculationRestFrequencyWeekday, recalculationRestFrequencyOnDay, recalculationCompoundingFrequencyType,
                recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyNthDay,
                recalculationCompoundingFrequencyWeekday, recalculationCompoundingFrequencyOnDay, isArrearsBasedOnOriginalSchedule,
                isCompoundingToBePostedAsTransaction, preCloseInterestCalculationStrategy, allowCompoundingOnEod);
    }

    public boolean isArrearsBasedOnOriginalSchedule() {
        return isArrearsBasedOnOriginalSchedule;
    }

    public boolean isCompoundingToBePostedAsTransaction() {
        return isCompoundingToBePostedAsTransaction;
    }

    public boolean isIsArrearsBasedOnOriginalSchedule() {
        return isArrearsBasedOnOriginalSchedule;
    }

    public boolean isIsCompoundingToBePostedAsTransaction() {
        return isCompoundingToBePostedAsTransaction;
    }
}
