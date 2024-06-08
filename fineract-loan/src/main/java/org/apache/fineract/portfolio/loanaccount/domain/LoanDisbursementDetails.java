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
import java.time.LocalDate;
import java.util.Objects;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;

@Entity
@Table(name = "m_loan_disbursement_detail")
public class LoanDisbursementDetails extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "expected_disburse_date")
    private LocalDate expectedDisbursementDate;

    @Column(name = "disbursedon_date")
    private LocalDate actualDisbursementDate;

    @Column(name = "principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal principal;

    @Column(name = "net_disbursal_amount", scale = 6, precision = 19)
    private BigDecimal netDisbursalAmount;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    protected LoanDisbursementDetails() {

    }

    public LoanDisbursementDetails(final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final BigDecimal principal, final BigDecimal netDisbursalAmount, final boolean reversed) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.principal = principal;
        this.netDisbursalAmount = netDisbursalAmount;
        this.reversed = reversed;
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LoanDisbursementDetails loanDisbursementDetails)) {
            return false;
        }
        return loanDisbursementDetails.principal.equals(this.principal)
                && DateUtils.isEqual(loanDisbursementDetails.expectedDisbursementDate, this.expectedDisbursementDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedDisbursementDate, principal);
    }

    public void copy(final LoanDisbursementDetails disbursementDetails) {
        this.principal = disbursementDetails.principal;
        this.expectedDisbursementDate = disbursementDetails.expectedDisbursementDate;
        this.actualDisbursementDate = disbursementDetails.actualDisbursementDate;
        this.reversed = disbursementDetails.reversed;
    }

    public LocalDate expectedDisbursementDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate expectedDisbursementDateAsLocalDate() {
        LocalDate expectedDisburseDate = null;
        if (this.expectedDisbursementDate != null) {
            expectedDisburseDate = this.expectedDisbursementDate;
        }
        return expectedDisburseDate;
    }

    public LocalDate actualDisbursementDate() {
        return this.actualDisbursementDate;
    }

    public BigDecimal principal() {
        return this.principal;
    }

    public void updatePrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public LocalDate getDisbursementDate() {
        return this.actualDisbursementDate;
    }

    public DisbursementData toData() {
        LocalDate expectedDisburseDate = expectedDisbursementDateAsLocalDate();
        BigDecimal waivedChargeAmount = null;
        return new DisbursementData(getId(), expectedDisburseDate, this.actualDisbursementDate, this.principal, this.netDisbursalAmount,
                null, null, waivedChargeAmount);
    }

    public void updateActualDisbursementDate(LocalDate actualDisbursementDate) {
        this.actualDisbursementDate = actualDisbursementDate;
    }

    public BigDecimal getNetDisbursalAmount() {
        return this.netDisbursalAmount;
    }

    public void setNetDisbursalAmount(BigDecimal netDisbursalAmount) {
        this.netDisbursalAmount = netDisbursalAmount;
    }

    public void updateExpectedDisbursementDateAndAmount(LocalDate expectedDisbursementDate, BigDecimal principal) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.principal = principal;
    }

    public void reverse() {
        this.reversed = true;
    }

    public boolean isReversed() {
        return reversed;
    }
}
