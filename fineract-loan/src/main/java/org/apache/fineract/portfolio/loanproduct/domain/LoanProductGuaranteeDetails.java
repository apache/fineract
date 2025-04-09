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
import java.math.BigDecimal;
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
@Table(name = "m_product_loan_guarantee_details")
public class LoanProductGuaranteeDetails extends AbstractPersistableCustom<Long> {

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @Column(name = "mandatory_guarantee", scale = 6, precision = 19, nullable = false)
    private BigDecimal mandatoryGuarantee;

    @Column(name = "minimum_guarantee_from_own_funds", scale = 6, precision = 19)
    private BigDecimal minimumGuaranteeFromOwnFunds;

    @Column(name = "minimum_guarantee_from_guarantor_funds", scale = 6, precision = 19)
    private BigDecimal minimumGuaranteeFromGuarantor;

    protected LoanProductGuaranteeDetails() {
        //
    }

    public LoanProductGuaranteeDetails(final BigDecimal mandatoryGuarantee, final BigDecimal minimumGuaranteeFromOwnFunds,
            final BigDecimal minimumGuaranteeFromGuarantor) {
        this.mandatoryGuarantee = mandatoryGuarantee;
        this.minimumGuaranteeFromGuarantor = minimumGuaranteeFromGuarantor;
        this.minimumGuaranteeFromOwnFunds = minimumGuaranteeFromOwnFunds;
    }

    public void updateProduct(final LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

}
