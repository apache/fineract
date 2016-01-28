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
package org.apache.fineract.portfolio.loanaccount.domain;

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
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.joda.time.LocalDate;
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
