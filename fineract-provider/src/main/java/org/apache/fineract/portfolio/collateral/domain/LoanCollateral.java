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
package org.apache.fineract.portfolio.collateral.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.collateral.api.CollateralApiConstants.COLLATERAL_JSON_INPUT_PARAMS;
import org.apache.fineract.portfolio.collateral.data.CollateralData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@Entity
@Table(name = "m_loan_collateral")
public class LoanCollateral extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "type_cv_id", nullable = false)
    private CodeValue type;

    @Column(name = "value", scale = 6, precision = 19)
    private BigDecimal value;

    @Column(name = "description", length = 500)
    private String description;

    public static LoanCollateral from(final CodeValue collateralType, final BigDecimal value, final String description) {
        return new LoanCollateral(null, collateralType, value, description);
    }

    protected LoanCollateral() {
        //
    }

    private LoanCollateral(final Loan loan, final CodeValue collateralType, final BigDecimal value, final String description) {
        this.loan = loan;
        this.type = collateralType;
        this.value = value;
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void assembleFrom(final CodeValue collateralType, final BigDecimal value, final String description) {
        this.type = collateralType;
        this.description = description;
        this.value = value;
    }

    public void associateWith(final Loan loan) {
        this.loan = loan;
    }

    public static LoanCollateral fromJson(final Loan loan, final CodeValue collateralType, final JsonCommand command) {
        final String description = command.stringValueOfParameterNamed(COLLATERAL_JSON_INPUT_PARAMS.DESCRIPTION.getValue());
        final BigDecimal value = command.bigDecimalValueOfParameterNamed(COLLATERAL_JSON_INPUT_PARAMS.VALUE.getValue());
        return new LoanCollateral(loan, collateralType, value, description);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String collateralTypeIdParamName = COLLATERAL_JSON_INPUT_PARAMS.COLLATERAL_TYPE_ID.getValue();
        if (command.isChangeInLongParameterNamed(collateralTypeIdParamName, this.type.getId())) {
            final Long newValue = command.longValueOfParameterNamed(collateralTypeIdParamName);
            actualChanges.put(collateralTypeIdParamName, newValue);
        }

        final String descriptionParamName = COLLATERAL_JSON_INPUT_PARAMS.DESCRIPTION.getValue();
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String valueParamName = COLLATERAL_JSON_INPUT_PARAMS.VALUE.getValue();
        if (command.isChangeInBigDecimalParameterNamed(valueParamName, this.value)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(valueParamName);
            actualChanges.put(valueParamName, newValue);
            this.value = newValue;
        }

        return actualChanges;
    }

    public CollateralData toData() {
        final CodeValueData typeData = this.type.toData();
        return CollateralData.instance(getId(), typeData, this.value, this.description, null);
    }

    public void setCollateralType(final CodeValue type) {
        this.type = type;
    }

   /* @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final LoanCollateral rhs = (LoanCollateral) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.type.getId(), rhs.type.getId()) //
                .append(this.description, rhs.description) //
                .append(this.value, this.value)//
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 5) //
                .append(getId()) //
                .append(this.type.getId()) //
                .append(this.description) //
                .append(this.value)//
                .toHashCode();
    }*/
}