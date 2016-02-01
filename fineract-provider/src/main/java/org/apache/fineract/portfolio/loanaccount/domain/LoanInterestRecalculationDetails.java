/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

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
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.mifosplatform.portfolio.loanproduct.domain.RecalculationFrequencyType;
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

    protected LoanInterestRecalculationDetails() {
        // Default constructor for jpa repository
    }

    private LoanInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod, final Integer rescheduleStrategyMethod,
            final Integer restFrequencyType, final Integer restInterval, final Date restFrequencyDate, Integer compoundingFrequencyType,
            Integer compoundingInterval, Date compoundingFrequencyDate) {
        this.interestRecalculationCompoundingMethod = interestRecalculationCompoundingMethod;
        this.rescheduleStrategyMethod = rescheduleStrategyMethod;
        this.restFrequencyDate = restFrequencyDate;
        this.restFrequencyType = restFrequencyType;
        this.restInterval = restInterval;
        this.compoundingFrequencyDate = compoundingFrequencyDate;
        this.compoundingFrequencyType = compoundingFrequencyType;
        this.compoundingInterval = compoundingInterval;
    }

    public static LoanInterestRecalculationDetails createFrom(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod, final Integer restFrequencyType, final Integer restInterval,
            final Date restFrequencyDate, final Integer compoundingFrequencyType, final Integer compoundingInterval,
            final Date compoundingFrequencyDate) {
        return new LoanInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod, restFrequencyType,
                restInterval, restFrequencyDate, compoundingFrequencyType, compoundingInterval, compoundingFrequencyDate);
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {
        if (command.isChangeInLocalDateParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName,
                getRestFrequencyLocalDate())) {
            final String dateFormatAsInput = command.dateFormat();
            final String localeAsInput = command.locale();
            final String valueAsInput = command.stringValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName);
            actualChanges.put(LoanProductConstants.recalculationRestFrequencyDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(LoanProductConstants.recalculationRestFrequencyDateParamName);
            if (newValue == null || getRestFrequencyType().isSameAsRepayment()) {
                this.restFrequencyDate = null;
            } else {
                this.restFrequencyDate = newValue.toDate();
            }
        }

        if (command.isChangeInLocalDateParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName,
                getCompoundingFrequencyLocalDate())) {
            final String dateFormatAsInput = command.dateFormat();
            final String localeAsInput = command.locale();
            final String valueAsInput = command
                    .stringValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName);
            actualChanges.put(LoanProductConstants.recalculationCompoundingFrequencyDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command
                    .localDateValueOfParameterNamed(LoanProductConstants.recalculationCompoundingFrequencyDateParamName);
            if (newValue == null || !getInterestRecalculationCompoundingMethod().isCompoundingEnabled()
                    || getCompoundingFrequencyType().isSameAsRepayment()) {
                this.compoundingFrequencyDate = null;
            } else {
                this.compoundingFrequencyDate = newValue.toDate();
            }
        }
    }

    public InterestRecalculationCompoundingMethod getInterestRecalculationCompoundingMethod() {
        return InterestRecalculationCompoundingMethod.fromInt(this.interestRecalculationCompoundingMethod);
    }

    public LoanRescheduleStrategyMethod getRescheduleStrategyMethod() {
        return LoanRescheduleStrategyMethod.fromInt(this.rescheduleStrategyMethod);
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
}
