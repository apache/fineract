/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity for capturing interest recalculation settings
 * 
 * @author conflux
 */

@Entity
@Table(name = "m_product_loan_guarantee_details")
public class LoanProductGuaranteeDetails extends AbstractPersistable<Long> {

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "mandatory_guarantee", scale = 6, precision = 19, nullable = false)
    private BigDecimal mandatoryGuarantee;

    @Column(name = "minimum_guarantee_from_own_funds", scale = 6, precision = 19, nullable = false)
    private BigDecimal minimumGuaranteeFromOwnFunds;

    @Column(name = "minimum_guarantee_from_guarantor_funds", scale = 6, precision = 19, nullable = false)
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
