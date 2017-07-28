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
@Table(name = "m_product_loan_guarantee_details")
public class LoanProductGuaranteeDetails extends AbstractPersistableCustom<Long> {

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "mandatory_guarantee", scale = 6, precision = 19, nullable = false)
    private BigDecimal mandatoryGuarantee;

    @Column(name = "minimum_guarantee_from_own_funds", scale = 6, precision = 19, nullable = true)
    private BigDecimal minimumGuaranteeFromOwnFunds;

    @Column(name = "minimum_guarantee_from_guarantor_funds", scale = 6, precision = 19, nullable = true)
    private BigDecimal minimumGuaranteeFromGuarantor;

    protected LoanProductGuaranteeDetails() {
        //
    }

    public static LoanProductGuaranteeDetails createFrom(final JsonCommand command) {

        final BigDecimal mandatoryGuarantee = command.bigDecimalValueOfParameterNamed(LoanProductConstants.mandatoryGuaranteeParamName);
        final BigDecimal minimumGuaranteeFromGuarantor = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName);
        final BigDecimal minimumGuaranteeFromOwnFunds = command
                .bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName);

        return new LoanProductGuaranteeDetails(mandatoryGuarantee, minimumGuaranteeFromOwnFunds, minimumGuaranteeFromGuarantor);
    }

    private LoanProductGuaranteeDetails(final BigDecimal mandatoryGuarantee, final BigDecimal minimumGuaranteeFromOwnFunds,
            final BigDecimal minimumGuaranteeFromGuarantor) {
        this.mandatoryGuarantee = mandatoryGuarantee;
        this.minimumGuaranteeFromGuarantor = minimumGuaranteeFromGuarantor;
        this.minimumGuaranteeFromOwnFunds = minimumGuaranteeFromOwnFunds;
    }

    public void updateProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges) {

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.mandatoryGuaranteeParamName, this.mandatoryGuarantee)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.mandatoryGuaranteeParamName);
            actualChanges.put(LoanProductConstants.mandatoryGuaranteeParamName, newValue);
            this.mandatoryGuarantee = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName,
                this.minimumGuaranteeFromGuarantor)) {
            final BigDecimal newValue = command
                    .bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromGuarantorParamName);
            actualChanges.put(LoanProductConstants.minimumGuaranteeFromGuarantorParamName, newValue);
            this.minimumGuaranteeFromGuarantor = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName,
                this.minimumGuaranteeFromOwnFunds)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName);
            actualChanges.put(LoanProductConstants.minimumGuaranteeFromOwnFundsParamName, newValue);
            this.minimumGuaranteeFromOwnFunds = newValue;
        }

    }

    public BigDecimal getMandatoryGuarantee() {
        return this.mandatoryGuarantee;
    }

    public BigDecimal getMinimumGuaranteeFromOwnFunds() {
        return this.minimumGuaranteeFromOwnFunds;
    }

    public BigDecimal getMinimumGuaranteeFromGuarantor() {
        return this.minimumGuaranteeFromGuarantor;
    }

}
