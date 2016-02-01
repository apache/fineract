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
package org.apache.fineract.portfolio.loanproduct.domain;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.joda.time.LocalDate;
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

    @Column(name = "rest_frequency_type_enum", nullable = false)
    private Integer restFrequencyType;

    @Column(name = "rest_frequency_interval", nullable = false)
    private Integer restInterval;

    @Temporal(TemporalType.DATE)
    @Column(name = "rest_freqency_date")
    private Date restFrequencyDate;

    @Column(name = "compounding_frequency_type_enum", nullable = true)
    private Integer compoundingFrequencyType;

    @Column(name = "compounding_frequency_interval", nullable = true)
    private Integer compoundingInterval;

    @Temporal(TemporalType.DATE)
    @Column(name = "compounding_freqency_date")
    private Date compoundingFrequencyDate;

    @Column(name = "arrears_based_on_original_schedule")
    private boolean isArrearsBasedOnOriginalSchedule;

    @Column(name = "pre_close_interest_calculation_strategy")
    private Integer preClosureInterestCalculationStrategy;

    protected LoanProductInterestRecalculationDetails() {
        //
    }

    public static LoanProductInterestRecalculationDetails createFrom(final JsonCommand command) {

        final Integer interestRecalculationCompoundingMethod = InterestRecalculationCompoundingMethod.fromInt(
                command.integerValueOfParameterNamed(LoanProductConstants.interestRecalculationCompoundingMethodParameterName)).getValue();

        final Integer loanRescheduleStrategyMethod = LoanRescheduleStrategyMethod.fromInt(
                command.integerValueOfParameterNamed(LoanProductConstants.rescheduleStrategyMethodParameterName)).getValue();

        final Integer recurrenceFrequency = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName);
        final LocalDate recurrenceOnLocalDate = command
                .localDateValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName);
        Integer recurrenceInterval = command
                .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName);
        final boolean isArrearsBasedOnOriginalSchedule = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName);
        RecalculationFrequencyType frequencyType = RecalculationFrequencyType.fromInt(recurrenceFrequency);
        Date recurrenceOnDate = null;
        if (recurrenceOnLocalDate != null) {
            if (!frequencyType.isSameAsRepayment()) {
                recurrenceOnDate = recurrenceOnLocalDate.toDate();
            }
        }
        if (frequencyType.isSameAsRepayment()) {
            recurrenceInterval = 0;
        }

        InterestRecalculationCompoundingMethod compoundingMethod = InterestRecalculationCompoundingMethod
                .fromInt(interestRecalculationCompoundingMethod);
        Integer compoundingRecurrenceFrequency = null;
        Integer compoundingInterval = null;
        Date recurrenceOnCompoundingDate = null;
        if (compoundingMethod.isCompoundingEnabled()) {
            compoundingRecurrenceFrequency = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName);
            compoundingInterval = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName);
            RecalculationFrequencyType compoundingFrequencyType = RecalculationFrequencyType.fromInt(compoundingRecurrenceFrequency);
            if (compoundingFrequencyType.isSameAsRepayment()) {
                recurrenceInterval = 0;
            }
            final LocalDate compoundingRecurrenceOnLocalDate = command
                    .localDateValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName);

            if (compoundingRecurrenceOnLocalDate != null) {
                if (!compoundingFrequencyType.isSameAsRepayment()) {
                    recurrenceOnCompoundingDate = compoundingRecurrenceOnLocalDate.toDate();
                }
            }
        }

        Integer preCloseInterestCalculationStrategy = command
                .integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
        if (preCloseInterestCalculationStrategy == null) {
            preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
        }

        return new LoanProductInterestRecalculationDetails(interestRecalculationCompoundingMethod, loanRescheduleStrategyMethod,
                recurrenceFrequency, recurrenceInterval, recurrenceOnDate, compoundingRecurrenceFrequency, compoundingInterval,
                recurrenceOnCompoundingDate, isArrearsBasedOnOriginalSchedule, preCloseInterestCalculationStrategy);
    }

    private LoanProductInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod, final Integer restFrequencyType, final Integer restInterval,
            final Date restFrequencyDate, Integer compoundingFrequencyType, Integer compoundingInterval, Date compoundingFrequencyDate,
            final boolean isArrearsBasedOnOriginalSchedule, final Integer preCloseInterestCalculationStrategy) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.restFrequencyType = restFrequencyType;
        this.restInterval = restInterval;
        this.restFrequencyDate = restFrequencyDate;
        this.compoundingFrequencyDate = compoundingFrequencyDate;
        this.compoundingFrequencyType = compoundingFrequencyType;
        this.compoundingInterval = compoundingInterval;
        this.isArrearsBasedOnOriginalSchedule = isArrearsBasedOnOriginalSchedule;
        this.preClosureInterestCalculationStrategy = preCloseInterestCalculationStrategy;
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

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName,
                this.restFrequencyType)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyTypeParameterName);
            actualChanges.put(LoanProductConstants.recalculationRestFrequencyTypeParameterName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.restFrequencyType = RecalculationFrequencyType.fromInt(newValue).getValue();
        }
        RecalculationFrequencyType frequencyType = RecalculationFrequencyType.fromInt(this.restFrequencyType);
        if (frequencyType.isSameAsRepayment()) {
            this.restInterval = 0;
            this.restFrequencyDate = null;
        } else {
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
                    this.restInterval)) {
                Integer newValue = command
                        .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyIntervalParameterName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.restInterval = newValue;
            }

            if (command.isChangeInLocalDateParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName,
                    getRestFrequencyLocalDate())) {
                final LocalDate newValue = command
                        .localDateValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName);
                Date recurrenceOnDate = null;
                if (newValue != null) {
                    recurrenceOnDate = newValue.toDate();
                }
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyDateParamName, newValue);
                this.restFrequencyDate = recurrenceOnDate;
            }
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName,
                this.compoundingFrequencyType)) {
            final Integer newValue = command
                    .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName);
            actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyTypeParameterName, newValue);
            this.compoundingFrequencyType = RecalculationFrequencyType.fromInt(newValue).getValue();
        }

        InterestRecalculationCompoundingMethod compoundingMethod = InterestRecalculationCompoundingMethod
                .fromInt(this.interestRecalculationCompoundingMethod);
        if (compoundingMethod.isCompoundingEnabled()) {
            RecalculationFrequencyType compoundingfrequencyType = RecalculationFrequencyType.fromInt(this.compoundingFrequencyType);
            if (compoundingfrequencyType.isSameAsRepayment()) {
                this.compoundingInterval = null;
                this.compoundingFrequencyDate = null;
            } else {
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
                        this.compoundingInterval)) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, newValue);
                    this.compoundingInterval = newValue;
                }

                if (command.isChangeInLocalDateParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName,
                        getCompoundingFrequencyLocalDate())) {
                    final LocalDate newValue = command
                            .localDateValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName);
                    Date recurrenceOnDate = null;
                    if (newValue != null) {
                        recurrenceOnDate = newValue.toDate();
                    }
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyDateParamName, newValue);
                    this.compoundingFrequencyDate = recurrenceOnDate;
                }
            }
        } else {
            this.compoundingFrequencyType = null;
            this.compoundingInterval = null;
            this.compoundingFrequencyDate = null;
        }

        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName,
                this.isArrearsBasedOnOriginalSchedule)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName);
            actualChanges.put(LoanProductConstants.isArrearsBasedOnOriginalScheduleParamName, newValue);
            this.isArrearsBasedOnOriginalSchedule = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName,
                this.preClosureInterestCalculationStrategy)) {
            Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
            if (newValue == null) {
                newValue = LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
            }
            actualChanges.put(LoanProductConstants.preClosureInterestCalculationStrategyParamName, newValue);
            this.preClosureInterestCalculationStrategy = newValue;
        }

    }

    public LocalDate getRestFrequencyLocalDate() {
        LocalDate recurrenceOnLocalDate = null;
        if (this.restFrequencyDate != null) {
            recurrenceOnLocalDate = new LocalDate(this.restFrequencyDate);
        }
        return recurrenceOnLocalDate;
    }

    public RecalculationFrequencyType getRestFrequencyType() {
        return RecalculationFrequencyType.fromInt(this.restFrequencyType);
    }

    public Integer getRestInterval() {
        return this.restInterval;
    }

    public LocalDate getCompoundingFrequencyLocalDate() {
        LocalDate recurrenceOnLocalDate = null;
        if (this.compoundingFrequencyDate != null) {
            recurrenceOnLocalDate = new LocalDate(this.compoundingFrequencyDate);
        }
        return recurrenceOnLocalDate;
    }

    public RecalculationFrequencyType getCompoundingFrequencyType() {
        return RecalculationFrequencyType.fromInt(this.compoundingFrequencyType);
    }

    public Integer getCompoundingInterval() {
        return this.compoundingInterval;
    }

    public boolean isArrearsBasedOnOriginalSchedule() {
        return this.isArrearsBasedOnOriginalSchedule;
    }

    public LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        return LoanPreClosureInterestCalculationStrategy.fromInt(this.preClosureInterestCalculationStrategy);
    }
}
