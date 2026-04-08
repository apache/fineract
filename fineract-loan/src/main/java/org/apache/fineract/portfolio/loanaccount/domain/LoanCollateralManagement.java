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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;

@Entity
@Table(name = "m_loan_collateral_management")
public class LoanCollateralManagement extends AbstractPersistableCustom<Long> {

    @Column(name = "quantity", nullable = false, scale = 5, precision = 20)
    private BigDecimal quantity;

    @ManyToOne()
    @JoinColumn(name = "transaction_id")
    private LoanTransaction loanTransaction = null;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable = false)
    private Loan loan;

    @Column(name = "is_released", nullable = false)
    private boolean isReleased = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_collateral_id", nullable = false)
    private ClientCollateralManagement clientCollateralManagement;

    public LoanCollateralManagement() {

    }

    public LoanCollateralManagement(final BigDecimal quantity, final boolean isReleased) {
        this.quantity = quantity;
        this.isReleased = isReleased;
    }

    private LoanCollateralManagement(final BigDecimal quantity, final ClientCollateralManagement clientCollateralManagement) {
        this.clientCollateralManagement = clientCollateralManagement;
        this.quantity = quantity;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public static LoanCollateralManagement from(final ClientCollateralManagement clientCollateralManagement, final BigDecimal quantity) {
        return new LoanCollateralManagement(quantity, clientCollateralManagement);
    }

    public static LoanCollateralManagement fromExisting(final ClientCollateralManagement clientCollateralManagement,
            final BigDecimal quantity, final Loan loan, final LoanTransaction transaction, final Long id) {
        LoanCollateralManagement loanCollateralManagementInstance = new LoanCollateralManagement(quantity, clientCollateralManagement);
        loanCollateralManagementInstance.setLoan(loan);
        loanCollateralManagementInstance.setLoanTransactionData(transaction);
        loanCollateralManagementInstance.setId(id);
        return loanCollateralManagementInstance;
    }

    public void setClientCollateralManagement(final ClientCollateralManagement clientCollateralManagement) {
        this.clientCollateralManagement = clientCollateralManagement;
    }

    public void setLoanTransactionData(final LoanTransaction loanTransaction) {
        this.loanTransaction = loanTransaction;
    }

    public void setIsReleased(final boolean isReleased) {
        this.isReleased = isReleased;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity() {
        return this.quantity;
    }

    public Loan getLoanData() {
        return this.loan;
    }

    public LoanTransaction getLoanTransaction() {
        return this.loanTransaction;
    }

    public ClientCollateralManagement getClientCollateralManagement() {
        return this.clientCollateralManagement;
    }

    public boolean isReleased() {
        return this.isReleased;
    }

    public LoanCollateralManagementData toCommand() {
        return new LoanCollateralManagementData(this.clientCollateralManagement.getId(), this.getQuantity(), null, null, getId());
    }

}
