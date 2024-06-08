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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "m_product_loan_variable_installment_config")
public class LoanProductVariableInstallmentConfig extends AbstractPersistableCustom<Long> {

    @Setter
    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "minimum_gap")
    private Integer minimumGap;

    @Column(name = "maximum_gap")
    private Integer maximumGap;

    public Map<? extends String, ?> update(JsonCommand command) {
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
}
