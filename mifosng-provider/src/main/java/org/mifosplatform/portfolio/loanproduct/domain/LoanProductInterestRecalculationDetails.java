/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
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

    @SuppressWarnings("unused")
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

        Integer preCloseInterestCalculationStrategy = command
                .integerValueOfParameterNamed(LoanProductConstants.preClosureInterestCalculationStrategyParamName);
        if (preCloseInterestCalculationStrategy == null) {
            preCloseInterestCalculationStrategy = LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue();
        }

        return new LoanProductInterestRecalculationDetails(interestRecalculationCompoundingMethod, loanRescheduleStrategyMethod,
                recurrenceFrequency, recurrenceInterval, recurrenceOnDate, isArrearsBasedOnOriginalSchedule,
                preCloseInterestCalculationStrategy);
    }

    private LoanProductInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod, final Integer restFrequencyType, final Integer restInterval,
            final Date restFrequencyDate, final boolean isArrearsBasedOnOriginalSchedule, final Integer preCloseInterestCalculationStrategy) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.restFrequencyType = restFrequencyType;
        this.restInterval = restInterval;
        this.restFrequencyDate = restFrequencyDate;
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
                if (frequencyType.isSameAsRepayment()) {
                    newValue = 0;
                }
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
                    if (!frequencyType.isSameAsRepayment()) {
                        recurrenceOnDate = newValue.toDate();
                    }
                }
                actualChanges.put(LoanProductConstants.recalculationRestFrequencyDateParamName, newValue);
                this.restFrequencyDate = recurrenceOnDate;
            }
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

    public boolean isArrearsBasedOnOriginalSchedule() {
        return this.isArrearsBasedOnOriginalSchedule;
    }

    public LoanPreClosureInterestCalculationStrategy preCloseInterestCalculationStrategy() {
        return LoanPreClosureInterestCalculationStrategy.fromInt(this.preClosureInterestCalculationStrategy);
    }
}
