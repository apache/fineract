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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_product_loan_floating_rates")
public class LoanProductFloatingRates extends AbstractPersistableCustom<Long> {

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne
    @JoinColumn(name = "floating_rates_id", nullable = false)
    private FloatingRate floatingRate;

    @Column(name = "interest_rate_differential", nullable = false)
    private BigDecimal interestRateDifferential;

    @Column(name = "min_differential_lending_rate", nullable = false)
    private BigDecimal minDifferentialLendingRate;

    @Column(name = "default_differential_lending_rate", nullable = false)
    private BigDecimal defaultDifferentialLendingRate;

    @Column(name = "max_differential_lending_rate", nullable = false)
    private BigDecimal maxDifferentialLendingRate;

    @Column(name = "is_floating_interest_rate_calculation_allowed", nullable = false)
	private boolean isFloatingInterestRateCalculationAllowed;
    
	public LoanProductFloatingRates(){
		
	}
	public LoanProductFloatingRates(FloatingRate floatingRate, LoanProduct loanProduct, BigDecimal interestRateDifferential, 
            BigDecimal minDifferentialLendingRate, BigDecimal maxDifferentialLendingRate, BigDecimal defaultDifferentialLendingRate, 
            boolean isFloatingInterestRateCalculationAllowed){
		this.floatingRate = floatingRate;
		this.loanProduct = loanProduct;
		this.interestRateDifferential = interestRateDifferential;
		this.minDifferentialLendingRate = minDifferentialLendingRate;
		this.maxDifferentialLendingRate = maxDifferentialLendingRate;
		this.defaultDifferentialLendingRate = defaultDifferentialLendingRate;
		this.isFloatingInterestRateCalculationAllowed = isFloatingInterestRateCalculationAllowed;
	}

    public LoanProduct getLoanProduct() {
        return this.loanProduct;
    }

    public FloatingRate getFloatingRate() {
        return this.floatingRate;
    }

    public BigDecimal getInterestRateDifferential() {
        return this.interestRateDifferential;
    }

    public BigDecimal getMinDifferentialLendingRate() {
        return this.minDifferentialLendingRate;
    }

    public BigDecimal getDefaultDifferentialLendingRate() {
        return this.defaultDifferentialLendingRate;
    }

    public BigDecimal getMaxDifferentialLendingRate() {
        return this.maxDifferentialLendingRate;
    }

    public boolean isFloatingInterestRateCalculationAllowed() {
        return this.isFloatingInterestRateCalculationAllowed;
    }

    public Map<? extends String, ? extends Object> update(JsonCommand command, FloatingRate floatingRate) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);
        if (floatingRate != null) {
            final String floatingRatesId = "floatingRatesId";
            if (this.floatingRate == null || command.isChangeInLongParameterNamed(floatingRatesId, this.floatingRate.getId())) {
                final long newValue = command.longValueOfParameterNamed(floatingRatesId);
                actualChanges.put(floatingRatesId, newValue);
                this.floatingRate = floatingRate;
            }
        }

        final String interestRateDifferential = "interestRateDifferential";
        if (command.isChangeInBigDecimalParameterNamed(interestRateDifferential, this.interestRateDifferential)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestRateDifferential);
            actualChanges.put(interestRateDifferential, newValue);
            this.interestRateDifferential = newValue;
        }
        final String minDifferentialLendingRate = "minDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(minDifferentialLendingRate, this.minDifferentialLendingRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minDifferentialLendingRate);
            actualChanges.put(minDifferentialLendingRate, newValue);
            this.minDifferentialLendingRate = newValue;
        }
        final String defaultDifferentialLendingRate = "defaultDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(defaultDifferentialLendingRate, this.defaultDifferentialLendingRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(defaultDifferentialLendingRate);
            actualChanges.put(defaultDifferentialLendingRate, newValue);
            this.defaultDifferentialLendingRate = newValue;
        }
        final String maxDifferentialLendingRate = "maxDifferentialLendingRate";
        if (command.isChangeInBigDecimalParameterNamed(maxDifferentialLendingRate, this.maxDifferentialLendingRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxDifferentialLendingRate);
            actualChanges.put(maxDifferentialLendingRate, newValue);
            this.maxDifferentialLendingRate = newValue;
        }
        final String isFloatingInterestRateCalculationAllowed = "isFloatingInterestRateCalculationAllowed";
        if (command
                .isChangeInBooleanParameterNamed(isFloatingInterestRateCalculationAllowed, this.isFloatingInterestRateCalculationAllowed)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isFloatingInterestRateCalculationAllowed);
            actualChanges.put(isFloatingInterestRateCalculationAllowed, newValue);
            this.isFloatingInterestRateCalculationAllowed = newValue;
        }

        return actualChanges;
    }

    public Collection<FloatingRatePeriodData> fetchInterestRates(final FloatingRateDTO floatingRateDTO) {
        floatingRateDTO.addInterestRateDiff(this.interestRateDifferential);
        return floatingRate.fetchInterestRates(floatingRateDTO);

    }

}
