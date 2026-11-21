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
package org.apache.fineract.portfolio.loanaccount.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductInterestRecalculationDetailsAssembler {

    public LoanProductInterestRecalculationDetails createFrom(final JsonCommand command) {
        final Integer interestRecalculationCompoundingMethod = InterestRecalculationCompoundingMethod
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName))
                .getValue();

        final Integer loanRescheduleStrategyMethod = LoanRescheduleStrategyMethod
                .fromInt(command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName)).getValue();

        final Integer recurrenceFrequency = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName);
        final Integer recurrenceOnNthDay = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyNthDayParamName);
        final Integer recurrenceOnDay = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName);
        final Integer recurrenceOnWeekday = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyWeekdayParamName);
        Integer recurrenceInterval = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName);
        final boolean isArrearsBasedOnOriginalSchedule = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName);
        RecalculationFrequencyType frequencyType = RecalculationFrequencyType.fromInt(recurrenceFrequency);
        if (frequencyType.isSameAsRepayment()) {
            recurrenceInterval = 0;
        }

        InterestRecalculationCompoundingMethod compoundingMethod = InterestRecalculationCompoundingMethod
                .fromInt(interestRecalculationCompoundingMethod);
        Integer compoundingRecurrenceFrequency = null;
        Integer compoundingInterval = null;
        Integer compoundingRecurrenceOnNthDay = null;
        Integer compoundingRecurrenceOnDay = null;
        Integer compoundingRecurrenceOnWeekday = null;
        boolean allowCompoundingOnEod = false;
        if (compoundingMethod.isCompoundingEnabled()) {
            compoundingRecurrenceFrequency = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName);
            compoundingInterval = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName);
            RecalculationFrequencyType compoundingFrequencyType = RecalculationFrequencyType.fromInt(compoundingRecurrenceFrequency);
            if (compoundingFrequencyType.isSameAsRepayment()) {
                recurrenceInterval = 0;
            }
            compoundingRecurrenceOnNthDay = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName);
            compoundingRecurrenceOnDay = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName);
            compoundingRecurrenceOnWeekday = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName);
            if (!compoundingFrequencyType.isDaily()) {
                allowCompoundingOnEod = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName);
            }
        }

        Integer preCloseInterestCalculationStrategy = command
                .integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
        if (preCloseInterestCalculationStrategy == null) {
            preCloseInterestCalculationStrategy = LoanPreCloseInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
        }

        final boolean isCompoundingToBePostedAsTransaction = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName);

        final boolean disallowInterestCalculationOnPastDue = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.disallowInterestCalculationOnPastDueParamName);

        return new LoanProductInterestRecalculationDetails(interestRecalculationCompoundingMethod, loanRescheduleStrategyMethod,
                recurrenceFrequency, recurrenceInterval, recurrenceOnNthDay, recurrenceOnDay, recurrenceOnWeekday,
                compoundingRecurrenceFrequency, compoundingInterval, compoundingRecurrenceOnNthDay, compoundingRecurrenceOnDay,
                compoundingRecurrenceOnWeekday, isArrearsBasedOnOriginalSchedule, preCloseInterestCalculationStrategy,
                isCompoundingToBePostedAsTransaction, allowCompoundingOnEod, disallowInterestCalculationOnPastDue);
    }
}
