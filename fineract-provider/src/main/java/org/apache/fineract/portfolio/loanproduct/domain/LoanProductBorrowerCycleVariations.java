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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_product_loan_variations_borrower_cycle")
public class LoanProductBorrowerCycleVariations extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "borrower_cycle_number", nullable = false)
    private Integer borrowerCycleNumber;

    @Column(name = "param_type", nullable = false)
    private Integer paramType;

    @Column(name = "value_condition", nullable = false)
    private Integer valueConditionType;

    @Column(name = "min_value", scale = 6, precision = 19, nullable = true)
    private BigDecimal minValue;

    @Column(name = "max_value", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxValue;

    @Column(name = "default_value", scale = 6, precision = 19, nullable = false)
    private BigDecimal defaultValue;

    protected LoanProductBorrowerCycleVariations() {
    }

    public LoanProductBorrowerCycleVariations(final Integer borrowerCycleNumber, final Integer paramType, final Integer valueConditionType,
            final BigDecimal minValue, final BigDecimal maxValue, final BigDecimal defaultValue) {
        this.borrowerCycleNumber = borrowerCycleNumber;
        this.paramType = paramType;
        this.valueConditionType = valueConditionType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
    }

    public void updateLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public LoanProductParamType getParamType() {
        return LoanProductParamType.fromInt(this.paramType);
    }

    public LoanProductValueConditionType getValueConditionType() {
        return LoanProductValueConditionType.fromInt(this.valueConditionType);
    }

    @Override
    public boolean equals(final Object obj) {
        final LoanProductBorrowerCycleVariations other = (LoanProductBorrowerCycleVariations) obj;
        return Objects.equals(loanProduct, other.loanProduct)
            && Objects.equals(borrowerCycleNumber, other.borrowerCycleNumber)
            && Objects.equals(paramType, other.paramType)
            && Objects.equals(valueConditionType, other.valueConditionType)
            && Objects.equals(minValue, other.minValue)
            && Objects.equals(maxValue, other.maxValue)
            && Objects.equals(defaultValue, other.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanProduct, borrowerCycleNumber, paramType, valueConditionType, minValue, maxValue, defaultValue);
    }

    public void copy(final LoanProductBorrowerCycleVariations borrowerCycleVariations) {
        this.defaultValue = borrowerCycleVariations.defaultValue;
        this.minValue = borrowerCycleVariations.minValue;
        this.maxValue = borrowerCycleVariations.maxValue;
        this.valueConditionType = borrowerCycleVariations.valueConditionType;
        this.borrowerCycleNumber = borrowerCycleVariations.borrowerCycleNumber;
    }

    public Integer getBorrowerCycleNumber() {
        return this.borrowerCycleNumber;
    }

    public BigDecimal getMinValue() {
        return this.minValue;
    }

    public BigDecimal getMaxValue() {
        return this.maxValue;
    }

    public BigDecimal getDefaultValue() {
        return this.defaultValue;
    }
}