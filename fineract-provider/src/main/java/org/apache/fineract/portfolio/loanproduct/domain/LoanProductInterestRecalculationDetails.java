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

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Entity for capturing interest recalculation settings
 * 
 * @author conflux
 */

@Entity
@Table(name = "m_product_loan_recalculation_details")
public class LoanProductInterestRecalculationDetails extends AbstractPersistableCustom<Long> {

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

    @Column(name = "rest_frequency_nth_day_enum", nullable = true)
    private Integer restFrequencyNthDay;

    @Column(name = "rest_frequency_weekday_enum", nullable = true)
    private Integer restFrequencyWeekday;

    @Column(name = "rest_frequency_on_day", nullable = true)
    private Integer restFrequencyOnDay;

    @Column(name = "compounding_frequency_type_enum", nullable = true)
    private Integer compoundingFrequencyType;

    @Column(name = "compounding_frequency_interval", nullable = true)
    private Integer compoundingInterval;

    @Column(name = "compounding_frequency_nth_day_enum", nullable = true)
    private Integer compoundingFrequencyNthDay;

    @Column(name = "compounding_frequency_weekday_enum", nullable = true)
    private Integer compoundingFrequencyWeekday;

    @Column(name = "compounding_frequency_on_day", nullable = true)
    private Integer compoundingFrequencyOnDay;

    @Column(name = "arrears_based_on_original_schedule")
    private boolean isArrearsBasedOnOriginalSchedule;

    @Column(name = "pre_close_interest_calculation_strategy")
    private Integer preClosureInterestCalculationStrategy;

    @Column(name = "is_compounding_to_be_posted_as_transaction")
    private Boolean isCompoundingToBePostedAsTransaction;

    @Column(name = "allow_compounding_on_eod")
    private Boolean allowCompoundingOnEod;

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
            if (!compoundingFrequencyType.isDaily())
                allowCompoundingOnEod = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName);
        }

        Integer preCloseInterestCalculationStrategy = command
                .integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
        if (preCloseInterestCalculationStrategy == null) {
            preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
        }

        final boolean isCompoundingToBePostedAsTransaction = command
                .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName);

        return new LoanProductInterestRecalculationDetails(interestRecalculationCompoundingMethod, loanRescheduleStrategyMethod,
                recurrenceFrequency, recurrenceInterval, recurrenceOnNthDay, recurrenceOnDay, recurrenceOnWeekday,
                compoundingRecurrenceFrequency, compoundingInterval, compoundingRecurrenceOnNthDay, compoundingRecurrenceOnDay,
                compoundingRecurrenceOnWeekday, isArrearsBasedOnOriginalSchedule, preCloseInterestCalculationStrategy,
                isCompoundingToBePostedAsTransaction, allowCompoundingOnEod);
    }

    private LoanProductInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod, final Integer restFrequencyType, final Integer restInterval,
            final Integer restFrequencyNthDay, final Integer restFrequencyOnDay, final Integer restFrequencyWeekday,
            Integer compoundingFrequencyType, Integer compoundingInterval, final Integer compoundingFrequencyNthDay,
            final Integer compoundingFrequencyOnDay, final Integer compoundingFrequencyWeekday,
            final boolean isArrearsBasedOnOriginalSchedule, final Integer preCloseInterestCalculationStrategy,
            final boolean isCompoundingToBePostedAsTransaction, final boolean allowCompoundingOnEod) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.restFrequencyType = restFrequencyType;
        this.restInterval = restInterval;
        this.restFrequencyNthDay = restFrequencyNthDay;
        this.restFrequencyOnDay = restFrequencyOnDay;
        this.restFrequencyWeekday = restFrequencyWeekday;
        this.compoundingFrequencyType = compoundingFrequencyType;
        this.compoundingInterval = compoundingInterval;
        this.compoundingFrequencyNthDay = compoundingFrequencyNthDay;
        this.compoundingFrequencyOnDay = compoundingFrequencyOnDay;
        this.compoundingFrequencyWeekday = compoundingFrequencyWeekday;
        this.isArrearsBasedOnOriginalSchedule = isArrearsBasedOnOriginalSchedule;
        this.preClosureInterestCalculationStrategy = preCloseInterestCalculationStrategy;
        this.isCompoundingToBePostedAsTransaction = isCompoundingToBePostedAsTransaction;
        this.allowCompoundingOnEod = allowCompoundingOnEod;
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
            this.restFrequencyNthDay = null;
            this.restFrequencyWeekday = null;
            this.restFrequencyOnDay = null;
        } else {
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName,
                    this.restInterval)) {
                Integer newValue = command
                        .integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyIntervalParameterName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyIntervalParameterName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.restInterval = newValue;
            }

            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyNthDayParamName,
                    getRestFrequencyNthDay())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyNthDayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyNthDayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.restFrequencyNthDay = newValue;
                this.restFrequencyOnDay = null;
            }
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyWeekdayParamName,
                    getRestFrequencyWeekday())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyWeekdayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyWeekdayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.restFrequencyWeekday = newValue;
                this.restFrequencyOnDay = null;
            }
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName,
                    getRestFrequencyOnDay())) {
                Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName);
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyOnDayParamName, newValue);
                actualChanges.put("locale", localeAsInput);
                this.restFrequencyOnDay = newValue;
                this.restFrequencyNthDay = null;
                this.restFrequencyWeekday = null;
            }

            if (frequencyType.isWeekly()) {
            	this.restFrequencyNthDay = null;
            	this.restFrequencyOnDay = null;
            } else if (frequencyType.isMonthly()) {
            	if(command.integerValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyOnDayParamName) != null) {
            		this.restFrequencyNthDay = null;
            		this.restFrequencyWeekday = null;
            	} else {
            		this.restFrequencyOnDay = null;
            	}
            } else if (frequencyType.isDaily()) {
            	this.restFrequencyNthDay = null;
        		this.restFrequencyWeekday = null;
        		this.restFrequencyOnDay = null;
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
                this.compoundingFrequencyNthDay = null;
                this.compoundingFrequencyWeekday = null;
                this.compoundingFrequencyOnDay = null;
            } else {
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName,
                        this.compoundingInterval)) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyIntervalParameterName, newValue);
                    this.compoundingInterval = newValue;
                }

                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName,
                        getCompoundingFrequencyNthDay())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyNthDayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    this.compoundingFrequencyNthDay = newValue;
                    this.compoundingFrequencyOnDay = null;
                }
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName,
                        getCompoundingFrequencyWeekday())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyWeekdayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    this.compoundingFrequencyWeekday = newValue;
                    this.compoundingFrequencyOnDay = null;
                }
                if (command.isChangeInIntegerParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName,
                        getCompoundingFrequencyOnDay())) {
                    Integer newValue = command
                            .integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName);
                    actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName, newValue);
                    actualChanges.put("locale", localeAsInput);
                    this.compoundingFrequencyOnDay = newValue;
                    this.compoundingFrequencyNthDay = null;
                    this.compoundingFrequencyWeekday = null;
                }
                
                if (compoundingfrequencyType.isWeekly()) {
                	this.compoundingFrequencyNthDay = null;
                	this.compoundingFrequencyOnDay = null;
                } else if (compoundingfrequencyType.isMonthly()) {
                	if(command.integerValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyOnDayParamName) != null) {
                		this.compoundingFrequencyNthDay = null;
                		this.compoundingFrequencyWeekday = null;
                	} else {
                		this.compoundingFrequencyOnDay = null;
                	}
                } else if (compoundingfrequencyType.isDaily()) {
                	this.compoundingFrequencyNthDay = null;
            		this.compoundingFrequencyWeekday = null;
            		this.compoundingFrequencyOnDay = null;
                }
            }
            if (!compoundingfrequencyType.isDaily()) {
                if (command.isChangeInBooleanParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName, allowCompoundingOnEod())) {
                    boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.allowCompoundingOnEodParamName);
                    actualChanges.put(LoanProductConstants.allowCompoundingOnEodParamName, newValue);
                    this.allowCompoundingOnEod = newValue;
                }
            } else {
                this.allowCompoundingOnEod = false;
            }
        } else {
            this.compoundingFrequencyType = null;
            this.compoundingInterval = null;
            this.compoundingFrequencyNthDay = null;
    		this.compoundingFrequencyWeekday = null;
    		this.compoundingFrequencyOnDay = null;
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
        
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName,
                this.isCompoundingToBePostedAsTransaction)) {
            final boolean newValue = command
                    .booleanPrimitiveValueOfParameterNamed(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName);
            actualChanges.put(LoanProductConstants.isCompoundingToBePostedAsTransactionParamName, newValue);
            this.isCompoundingToBePostedAsTransaction = newValue;
        }

    }

    public RecalculationFrequencyType getRestFrequencyType() {
        return RecalculationFrequencyType.fromInt(this.restFrequencyType);
    }

    public Integer getRestInterval() {
        return this.restInterval;
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

    public Integer getRestFrequencyNthDay() {
        return this.restFrequencyNthDay;
    }

    public Integer getRestFrequencyWeekday() {
        return this.restFrequencyWeekday;
    }

    public Integer getRestFrequencyOnDay() {
        return this.restFrequencyOnDay;
    }

    public Integer getCompoundingFrequencyNthDay() {
        return this.compoundingFrequencyNthDay;
    }

    public Integer getCompoundingFrequencyWeekday() {
        return this.compoundingFrequencyWeekday;
    }

    public Integer getCompoundingFrequencyOnDay() {
        return this.compoundingFrequencyOnDay;
    }

    public Boolean getIsCompoundingToBePostedAsTransaction() {
        return this.isCompoundingToBePostedAsTransaction;
    }

    public Boolean allowCompoundingOnEod() {
        return this.allowCompoundingOnEod;
    }
}
