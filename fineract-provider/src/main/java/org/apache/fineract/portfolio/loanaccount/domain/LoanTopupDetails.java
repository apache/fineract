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

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_loan_topup")
public class LoanTopupDetails  extends AbstractPersistableCustom<Long> {

        @OneToOne
        @JoinColumn(name = "loan_id", nullable = false)
        private Loan loan;

        @Column(name = "closure_loan_id", nullable = false)
        private Long closureLoanId;

        @Column(name = "account_transfer_details_id", nullable = true)
        private Long accountTransferDetailsId;

        @Column(name = "topup_amount", nullable = true)
        private BigDecimal topupAmount;

        protected LoanTopupDetails(){};

        public LoanTopupDetails(final Loan loan, final Long loanIdToClose) {
                this.loan = loan;
                this.closureLoanId = loanIdToClose;
        }

        public Long getLoanIdToClose(){
                return this.closureLoanId;
        }

        public BigDecimal getTopupAmount() {
                return this.topupAmount;
        }

        public void setTopupAmount(BigDecimal topupAmount) {
                this.topupAmount = topupAmount;
        }

        public void setAccountTransferDetails(Long accountTransferDetailsId) {
                this.accountTransferDetailsId = accountTransferDetailsId;
        }

}
