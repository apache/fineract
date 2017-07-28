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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_product_loan_variable_installment_config")
public class LoanProductVariableInstallmentConfig extends AbstractPersistableCustom<Long> {

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
