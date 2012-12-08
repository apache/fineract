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

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable = false)
    private final Date dueDate;

    @SuppressWarnings("unused")
    @Column(name = "installment", nullable = false)
    private final Integer installmentNumber;

    @SuppressWarnings("unused")
    @Column(name = "deposit", scale = 6, precision = 19, nullable = false)
    private BigDecimal deposit;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "payment_date", nullable = true)
    private final Date paymentDate;

    @SuppressWarnings("unused")
    @Column(name = "deposit_paid", scale = 6, precision = 19, nullable = true)
    private BigDecimal depositPaid;

    @SuppressWarnings("unused")
    @Column(name = "completed_derived", nullable = false)
    private boolean completed;

    public SavingScheduleInstallments(SavingAccount account, Date dueDate, Integer installmentNumber, BigDecimal deposit) {
        this.savingAccount = account;
        this.dueDate = dueDate;
        this.installmentNumber = installmentNumber;
        this.deposit = deposit;
        this.paymentDate = null;
        this.depositPaid = BigDecimal.ZERO;
        this.completed = false;
    }

    public void updateAccount(SavingAccount account) {
        this.savingAccount = account;
    }
}

