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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_loan_tranche_disbursement_charge")
public class LoanTrancheDisbursementCharge extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_charge_id", referencedColumnName = "id", nullable = false)
    private LoanCharge loancharge;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "disbursement_detail_id", referencedColumnName = "id", nullable = false)
    private LoanDisbursementDetails loanDisbursementDetails;

    public LoanTrancheDisbursementCharge() {

    }

    public LoanTrancheDisbursementCharge(final LoanCharge loancharge, final LoanDisbursementDetails loanDisbursementDetails) {
        this.loancharge = loancharge;
        this.loanDisbursementDetails = loanDisbursementDetails;
    }

    public LoanDisbursementDetails getloanDisbursementDetails() {
        return this.loanDisbursementDetails;
    }

}
