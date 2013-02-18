/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.data;

import java.math.BigDecimal;
import java.util.Date;

public class LoanTransactionDTO {

    private String transactionId;
    private Date transactionDate;

    private BigDecimal amount;
    /*** Breakup of amounts in case of repayments **/
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal fees;
    private BigDecimal penalties;

    /*** Boolean values determine type of loan transaction ***/
    private boolean disbursement;
    private boolean repayment;
    private boolean repaymentAtDisbursement;
    private boolean contra;
    private boolean writeOff;

    public LoanTransactionDTO(final String transactionId, final Date transactionDate, final BigDecimal amount, final BigDecimal principal,
            final BigDecimal interest, final BigDecimal fees, final BigDecimal penalties, final boolean disbursement,
            final boolean repayment, final boolean repaymentAtDisbursement, final boolean contra, final boolean writeOff) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.principal = principal;
        this.interest = interest;
        this.fees = fees;
        this.penalties = penalties;
        this.disbursement = disbursement;
        this.repayment = repayment;
        this.repaymentAtDisbursement = repaymentAtDisbursement;
        this.contra = contra;
        this.writeOff = writeOff;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public void setTransactionDate(final Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(final BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public void setInterest(final BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getFees() {
        return this.fees;
    }

    public void setFees(final BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getPenalties() {
        return this.penalties;
    }

    public void setPenalties(final BigDecimal penalties) {
        this.penalties = penalties;
    }

    public boolean isDisbursement() {
        return this.disbursement;
    }

    public void setDisbursement(final boolean disbursement) {
        this.disbursement = disbursement;
    }

    public boolean isRepayment() {
        return this.repayment;
    }

    public void setRepayment(final boolean repayment) {
        this.repayment = repayment;
    }

    public boolean isContra() {
        return this.contra;
    }

    public void setContra(final boolean contra) {
        this.contra = contra;
    }

    public boolean isWriteOff() {
        return this.writeOff;
    }

    public void setWriteOff(final boolean writeOff) {
        this.writeOff = writeOff;
    }

    public boolean isRepaymentAtDisbursement() {
        return this.repaymentAtDisbursement;
    }

    public void setRepaymentAtDisbursement(final boolean repaymentAtDisbursement) {
        this.repaymentAtDisbursement = repaymentAtDisbursement;
    }

}
