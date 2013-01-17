package org.mifosplatform.accounting.api.data;

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

    public LoanTransactionDTO(String transactionId, Date transactionDate, BigDecimal amount, BigDecimal principal, BigDecimal interest,
            BigDecimal fees, BigDecimal penalties, boolean disbursement, boolean repayment, boolean repaymentAtDisbursement,
            boolean contra, boolean writeOff) {
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

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getFees() {
        return this.fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getPenalties() {
        return this.penalties;
    }

    public void setPenalties(BigDecimal penalties) {
        this.penalties = penalties;
    }

    public boolean isDisbursement() {
        return this.disbursement;
    }

    public void setDisbursement(boolean disbursement) {
        this.disbursement = disbursement;
    }

    public boolean isRepayment() {
        return this.repayment;
    }

    public void setRepayment(boolean repayment) {
        this.repayment = repayment;
    }

    public boolean isContra() {
        return this.contra;
    }

    public void setContra(boolean contra) {
        this.contra = contra;
    }

    public boolean isWriteOff() {
        return this.writeOff;
    }

    public void setWriteOff(boolean writeOff) {
        this.writeOff = writeOff;
    }

    public boolean isRepaymentAtDisbursement() {
        return this.repaymentAtDisbursement;
    }

    public void setRepaymentAtDisbursement(boolean repaymentAtDisbursement) {
        this.repaymentAtDisbursement = repaymentAtDisbursement;
    }

}
