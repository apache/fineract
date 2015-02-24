/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_disbursement_detail")
public class LoanDisbursementDetails extends AbstractPersistable<Long> {

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
        return new DisbursementData(getId(), expectedDisburseDate, actualDisburseDate, this.principal);
    }

    public void updateActualDisbursementDate(Date actualDisbursementDate) {
        this.actualDisbursementDate = actualDisbursementDate;
    }

    public void updateExpectedDisbursementDateAndAmount(Date expectedDisbursementDate, BigDecimal principal) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.principal = principal;
    }

}