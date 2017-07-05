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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_loan_disbursement_detail")
public class LoanDisbursementDetails extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Temporal(TemporalType.DATE)
    @Column(name = "expected_disburse_date")
    private Date expectedDisbursementDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "disbursedon_date")
    private Date actualDisbursementDate;

    @Column(name = "principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal principal;

    protected LoanDisbursementDetails() {

    }

    public LoanDisbursementDetails(final Date expectedDisbursementDate, final Date actualDisbursementDate, final BigDecimal principal) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.principal = principal;
     }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    @Override
    public boolean equals(final Object obj) {
        final LoanDisbursementDetails loanDisbursementDetails = (LoanDisbursementDetails) obj;
        if (loanDisbursementDetails.principal.equals(this.principal)
                && loanDisbursementDetails.expectedDisbursementDate.equals(this.expectedDisbursementDate)) 
        { return true; }
        return false;
    }

    public void copy(final LoanDisbursementDetails disbursementDetails) {
        this.principal = disbursementDetails.principal;
        this.expectedDisbursementDate = disbursementDetails.expectedDisbursementDate;
        this.actualDisbursementDate = disbursementDetails.actualDisbursementDate;
    }

    public Date expectedDisbursementDate() {
        return this.expectedDisbursementDate;
    }

    public LocalDate expectedDisbursementDateAsLocalDate() {
        LocalDate expectedDisburseDate = null;
        if (this.expectedDisbursementDate != null) {
            expectedDisburseDate = new LocalDate(this.expectedDisbursementDate);
        }
        return expectedDisburseDate;
    }

    public Date actualDisbursementDate() {
        return this.actualDisbursementDate;
    }

    public BigDecimal principal() {
        return this.principal;
    }

    public void updatePrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public Date getDisbursementDate() {
        Date disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public DisbursementData toData() {
        LocalDate expectedDisburseDate = expectedDisbursementDateAsLocalDate();
        LocalDate actualDisburseDate = null;
        if (this.actualDisbursementDate != null) {
            actualDisburseDate = new LocalDate(this.actualDisbursementDate);
        }
        BigDecimal waivedChargeAmount = null;
        return new DisbursementData(getId(), expectedDisburseDate, actualDisburseDate, this.principal, null, null, waivedChargeAmount);
    }

    public void updateActualDisbursementDate(Date actualDisbursementDate) {
        this.actualDisbursementDate = actualDisbursementDate;
    }

    public void updateExpectedDisbursementDateAndAmount(Date expectedDisbursementDate, BigDecimal principal) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.principal = principal;
    }

}