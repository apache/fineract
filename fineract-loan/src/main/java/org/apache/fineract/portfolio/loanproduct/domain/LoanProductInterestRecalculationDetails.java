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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Entity for capturing interest recalculation settings
 *
 * @author conflux
 */

@Getter
@Setter
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

    @Column(name = "rest_frequency_nth_day_enum")
    private Integer restFrequencyNthDay;

    @Column(name = "rest_frequency_weekday_enum")
    private Integer restFrequencyWeekday;

    @Column(name = "rest_frequency_on_day")
    private Integer restFrequencyOnDay;

    @Column(name = "compounding_frequency_type_enum")
    private Integer compoundingFrequencyType;

    @Column(name = "compounding_frequency_interval")
    private Integer compoundingInterval;

    @Column(name = "compounding_frequency_nth_day_enum")
    private Integer compoundingFrequencyNthDay;

    @Column(name = "compounding_frequency_weekday_enum")
    private Integer compoundingFrequencyWeekday;

    @Column(name = "compounding_frequency_on_day")
    private Integer compoundingFrequencyOnDay;

    @Column(name = "arrears_based_on_original_schedule")
    private boolean isArrearsBasedOnOriginalSchedule;

    @Column(name = "pre_close_interest_calculation_strategy")
    private Integer preCloseInterestCalculationStrategy;

    @Column(name = "is_compounding_to_be_posted_as_transaction")
    private Boolean isCompoundingToBePostedAsTransaction;

    @Column(name = "allow_compounding_on_eod")
    private Boolean allowCompoundingOnEod;

    @Column(name = "disallow_interest_calc_on_past_due")
    private Boolean disallowInterestCalculationOnPastDue;

    protected LoanProductInterestRecalculationDetails() {
        //
    }

    public LoanProductInterestRecalculationDetails(final Integer interestRecalculationCompoundingMethod,
            final Integer rescheduleStrategyMethod, final Integer restFrequencyType, final Integer restInterval,
            final Integer restFrequencyNthDay, final Integer restFrequencyOnDay, final Integer restFrequencyWeekday,
            Integer compoundingFrequencyType, Integer compoundingInterval, final Integer compoundingFrequencyNthDay,
            final Integer compoundingFrequencyOnDay, final Integer compoundingFrequencyWeekday,
            final boolean isArrearsBasedOnOriginalSchedule, final Integer preCloseInterestCalculationStrategy,
            final boolean isCompoundingToBePostedAsTransaction, final boolean allowCompoundingOnEod,
            final boolean disallowInterestCalculationOnPastDue) {
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
        this.preCloseInterestCalculationStrategy = preCloseInterestCalculationStrategy;
        this.isCompoundingToBePostedAsTransaction = isCompoundingToBePostedAsTransaction;
        this.allowCompoundingOnEod = allowCompoundingOnEod;
        this.disallowInterestCalculationOnPastDue = disallowInterestCalculationOnPastDue;
    }

    public void updateProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public RecalculationFrequencyType getRestFrequencyType() {
        return RecalculationFrequencyType.fromInt(this.restFrequencyType);
    }

    public RecalculationFrequencyType getCompoundingFrequencyType() {
        return RecalculationFrequencyType.fromInt(this.compoundingFrequencyType);
    }

    public LoanPreCloseInterestCalculationStrategy getPreCloseInterestCalculationStrategy() {
        return LoanPreCloseInterestCalculationStrategy.fromInt(this.preCloseInterestCalculationStrategy);
    }
}
