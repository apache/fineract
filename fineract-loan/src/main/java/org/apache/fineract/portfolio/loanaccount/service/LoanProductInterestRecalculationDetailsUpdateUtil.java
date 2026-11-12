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

import java.util.Map;
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
public class LoanProductInterestRecalculationDetailsUpdateUtil {

    public void update(final LoanProductInterestRecalculationDetails loanProductInterestRecalculationDetails, final JsonCommand command,
            final Map<String, Object> actualChanges, final String localeAsInput) {

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName,
                loanProductInterestRecalculationDetails.getInterestRecalculationCompoundingMethod())) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName);
            actualChanges.put(LoanProductConstants.interestRecalculationCompoundingMethodParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductInterestRecalculationDetails
                    .setInterestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.fromInt(newValue).getValue());
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName,
                loanProductInterestRecalculationDetails.getRescheduleStrategyMethod())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName);
            actualChanges.put(LoanProductConstants.rescheduleStrategyMethodParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductInterestRecalculationDetails.setRescheduleStrategyMethod(LoanRescheduleStrategyMethod.fromInt(newValue).getValue());
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName,
                loanProductInterestRecalculationDetails.getRestFrequencyType().getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName);
            actualChanges.put(LoanProductConstants.recalculationRestFrequencyTypeParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            loanProductInterestRecalculationDetails.setRestFrequencyType(RecalculationFrequencyType.fromInt(newValue).getValue());
        }
        RecalculationFrequencyType frequencyType = loanProductInterestRecalculationDetails.getRestFrequencyType();
        if (frequencyType.isSameAsRepayment()) {
            loanProductInterestRecalculationDetails.setRestInterval(0);
            loanProductInterestRecalculationDetails.setRestFrequencyNthDay(null);
            loanProductInterestRecalculationDetails.setRestFrequencyWeekday(null);
            loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
        } else {
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
                    loanProductInterestRecalculationDetails.getRestInterval())) {
                Integer newValue = command
                        .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyIntervalParameterName, newValue);
                actualChanges.put("locale", localeAsInput);
                loanProductInterestRecalculationDetails.setRestInterval(newValue);
            }

            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyNthDayParamName,
                    loanProductInterestRecalculationDetails.getRestFrequencyNthDay())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyNthDayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyNthDayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                loanProductInterestRecalculationDetails.setRestFrequencyNthDay(newValue);
                loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
            }
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyWeekdayParamName,
                    loanProductInterestRecalculationDetails.getRestFrequencyWeekday())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyWeekdayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyWeekdayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                loanProductInterestRecalculationDetails.setRestFrequencyWeekday(newValue);
                loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
            }
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName,
                    loanProductInterestRecalculationDetails.getRestFrequencyOnDay())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyOnDayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                loanProductInterestRecalculationDetails.setRestFrequencyOnDay(newValue);
                loanProductInterestRecalculationDetails.setRestFrequencyNthDay(null);
                loanProductInterestRecalculationDetails.setRestFrequencyWeekday(null);
            }

            if (frequencyType.isWeekly()) {
                loanProductInterestRecalculationDetails.setRestFrequencyNthDay(null);
                loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
            } else if (frequencyType.isMonthly()) {
                if (command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName) != null) {
                    loanProductInterestRecalculationDetails.setRestFrequencyNthDay(null);
                    loanProductInterestRecalculationDetails.setRestFrequencyWeekday(null);
                } else {
                    loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
                }
            } else if (frequencyType.isDaily()) {
                loanProductInterestRecalculationDetails.setRestFrequencyNthDay(null);
                loanProductInterestRecalculationDetails.setRestFrequencyWeekday(null);
                loanProductInterestRecalculationDetails.setRestFrequencyOnDay(null);
            }
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
                loanProductInterestRecalculationDetails.getCompoundingFrequencyType().getValue())) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName);
            actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, newValue);
            loanProductInterestRecalculationDetails.setCompoundingFrequencyType(RecalculationFrequencyType.fromInt(newValue).getValue());
        }

        InterestRecalculationCompoundingMethod compoundingMethod = InterestRecalculationCompoundingMethod
                .fromInt(loanProductInterestRecalculationDetails.getInterestRecalculationCompoundingMethod());
        if (compoundingMethod.isCompoundingEnabled()) {
            RecalculationFrequencyType compoundingFrequencyType = loanProductInterestRecalculationDetails.getCompoundingFrequencyType();
            if (compoundingFrequencyType.isSameAsRepayment()) {
                loanProductInterestRecalculationDetails.setCompoundingInterval(null);
                loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
                loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(null);
                loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
            } else {
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
                        loanProductInterestRecalculationDetails.getCompoundingInterval())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, newValue);
                    loanProductInterestRecalculationDetails.setCompoundingInterval(newValue);
                }

                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                        loanProductInterestRecalculationDetails.getCompoundingFrequencyNthDay())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(newValue);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
                }
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
                        loanProductInterestRecalculationDetails.getCompoundingFrequencyWeekday())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(newValue);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
                }
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
                        loanProductInterestRecalculationDetails.getCompoundingFrequencyOnDay())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(newValue);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(null);
                }

                if (compoundingFrequencyType.isWeekly()) {
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
                } else if (compoundingFrequencyType.isMonthly()) {
                    if (command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName) != null) {
                        loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
                        loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(null);
                    } else {
                        loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
                    }
                } else if (compoundingFrequencyType.isDaily()) {
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(null);
                    loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
                }
            }
            if (!compoundingFrequencyType.isDaily()) {
                if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName,
                        loanProductInterestRecalculationDetails.getAllowCompoundingOnEod())) {
                    boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName);
                    actualChanges.put(LoanProductConstants.allowCompoundingOnEodParamName, newValue);
                    loanProductInterestRecalculationDetails.setAllowCompoundingOnEod(newValue);
                }
            } else {
                loanProductInterestRecalculationDetails.setAllowCompoundingOnEod(false);
            }
        } else {
            loanProductInterestRecalculationDetails.setCompoundingFrequencyType(null);
            loanProductInterestRecalculationDetails.setCompoundingInterval(null);
            loanProductInterestRecalculationDetails.setCompoundingFrequencyNthDay(null);
            loanProductInterestRecalculationDetails.setCompoundingFrequencyWeekday(null);
            loanProductInterestRecalculationDetails.setCompoundingFrequencyOnDay(null);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName,
                loanProductInterestRecalculationDetails.isArrearsBasedOnOriginalSchedule())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName);
            actualChanges.put(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, newValue);
            loanProductInterestRecalculationDetails.setArrearsBasedOnOriginalSchedule(newValue);
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName,
                loanProductInterestRecalculationDetails.getPreCloseInterestCalculationStrategy().getValue())) {
            Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
            if (newValue == null) {
                newValue = LoanPreCloseInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
            }
            actualChanges.put(LoanProductConstants.preClosureInterestCalculationStrategyParamName, newValue);
            loanProductInterestRecalculationDetails.setPreCloseInterestCalculationStrategy(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName,
                loanProductInterestRecalculationDetails.getIsCompoundingToBePostedAsTransaction())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName);
            actualChanges.put(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, newValue);
            loanProductInterestRecalculationDetails.setIsCompoundingToBePostedAsTransaction(newValue);
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.disallowInterestCalculationOnPastDueParamName,
                loanProductInterestRecalculationDetails.getDisallowInterestCalculationOnPastDue())) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.disallowInterestCalculationOnPastDueParamName);
            actualChanges.put(LoanProductConstants.disallowInterestCalculationOnPastDueParamName, newValue);
            loanProductInterestRecalculationDetails.setDisallowInterestCalculationOnPastDue(newValue);
        }
    }
}
