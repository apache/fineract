/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_product_loan_variations_borrower_cycle")
public class LoanProductBorrowerCycleVariations extends AbstractPersistable<Long> {

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
        final LoanProductBorrowerCycleVariations borrowerCycleVariations = (LoanProductBorrowerCycleVariations) obj;
        boolean minValequal = false;
        if (borrowerCycleVariations.minValue == null && this.minValue == null) {
            minValequal = true;
        } else if (borrowerCycleVariations.minValue != null && this.minValue != null) {
            minValequal = borrowerCycleVariations.minValue.equals(this.minValue);
        }

        boolean maxValequal = false;
        if (borrowerCycleVariations.maxValue == null && this.maxValue == null) {
            maxValequal = true;
        } else if (borrowerCycleVariations.maxValue != null && this.maxValue != null) {
            maxValequal = borrowerCycleVariations.maxValue.equals(this.maxValue);
        }
        if (borrowerCycleVariations.borrowerCycleNumber.equals(this.borrowerCycleNumber)
                && borrowerCycleVariations.defaultValue.equals(this.defaultValue) && minValequal && maxValequal
                && borrowerCycleVariations.valueConditionType.equals(this.valueConditionType)
                && borrowerCycleVariations.paramType.equals(this.paramType)) { return true; }
        return false;
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