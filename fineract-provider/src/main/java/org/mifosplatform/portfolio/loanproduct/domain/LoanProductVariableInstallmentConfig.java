/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.loanproduct.LoanProductConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_product_loan_variable_installment_config")
public class LoanProductVariableInstallmentConfig extends AbstractPersistable<Long> {

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "minimum_gap")
    private Integer minimumGap;

    @Column(name = "maximum_gap")
    private Integer maximumGap;

    protected LoanProductVariableInstallmentConfig() {

    }

    public LoanProductVariableInstallmentConfig(final LoanProduct loanProduct, final Integer minimumGap, final Integer maximumGap) {
        this.loanProduct = loanProduct;
        this.minimumGap = minimumGap;
        this.maximumGap = maximumGap;
    }

    public void setLoanProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public Map<? extends String, ? extends Object> update(JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.minimumGapBetweenInstallments, this.minimumGap)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.minimumGapBetweenInstallments);
            actualChanges.put(LoanProductConstants.minimumGapBetweenInstallments, newValue);
            this.minimumGap = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(LoanProductConstants.maximumGapBetweenInstallments, this.maximumGap)) {
            final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.maximumGapBetweenInstallments);
            actualChanges.put(LoanProductConstants.maximumGapBetweenInstallments, newValue);
            this.maximumGap = newValue;
        }

        return actualChanges;
    }

    public Integer getMinimumGap() {
        return this.minimumGap;
    }

    public Integer getMaximumGap() {
        return this.maximumGap;
    }

}
