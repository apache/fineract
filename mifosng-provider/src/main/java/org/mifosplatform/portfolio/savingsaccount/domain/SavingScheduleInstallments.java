package org.mifosplatform.portfolio.savingsaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_saving_schedule")
public class SavingScheduleInstallments extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
    @ManyToOne(optional = false)
    @JoinColumn(name = "saving_account_id", nullable = false)
    private SavingAccount savingAccount;

    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable = false)
    private final Date dueDate;

    @SuppressWarnings("unused")
    @Column(name = "installment", nullable = false)
    private final Integer installmentNumber;

    @Column(name = "deposit", scale = 6, precision = 19, nullable = false)
    private BigDecimal deposit;

    @Temporal(TemporalType.DATE)
    @Column(name = "payment_date", nullable = true)
    private Date paymentDate;

    @Column(name = "deposit_paid", scale = 6, precision = 19, nullable = true)
    private BigDecimal depositPaid;

    @SuppressWarnings("unused")
    @Column(name = "interest_accured", scale = 6, precision = 19, nullable = true)
    private BigDecimal interstAccured;

    @Column(name = "completed_derived", nullable = false)
    private boolean completed;

    public SavingScheduleInstallments() {
        this.dueDate = null;
        this.installmentNumber = null;
        this.paymentDate = null;
    }

    public SavingScheduleInstallments(SavingAccount account, Date dueDate, Integer installmentNumber, BigDecimal deposit,
            BigDecimal interstAccured) {
        this.savingAccount = account;
        this.dueDate = dueDate;
        this.installmentNumber = installmentNumber;
        this.deposit = deposit;
        this.paymentDate = null;
        this.depositPaid = BigDecimal.ZERO;
        this.completed = false;
        this.interstAccured = interstAccured;
    }

    public void updateAccount(SavingAccount account) {
        this.savingAccount = account;
    }

    public Date getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getDepositPaid() {
        return this.depositPaid;
    }

    public void setDepositPaid(BigDecimal depositPaid) {
        this.depositPaid = depositPaid;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public BigDecimal getDeposit() {
        return this.deposit;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}