package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_loan_repayment_reminder")
public final class LoanRepaymentReminder extends AbstractAuditableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private Loan loan;
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;
    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_product_id", referencedColumnName = "id")
    private LoanProduct loanProduct;
    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_schedule_id", referencedColumnName = "id")
    private LoanRepaymentScheduleInstallment scheduleInstallment;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "installment", nullable = false)
    private Integer installment;
    @Column(name = "principal_amount_outstanding", nullable = false)
    private BigDecimal principalAmountOutStanding;
    @Column(name = "interest_amount_outstanding", nullable = false)
    private BigDecimal interestAmountOutStanding;
    @Column(name = "fees_charge_amount_outstanding", nullable = false)
    private BigDecimal feesChargeAmountOutStanding;
    @Column(name = "penalty_charge_amount_outstanding", nullable = false)
    private BigDecimal penaltyChargeAmountOutStanding;
    @Column(name = "total_amount_outstanding", nullable = false)
    private BigDecimal totalAmountOutStanding;
    @Column(name = "total_amount_outstanding", nullable = false)
    private Long batchId;

    public LoanRepaymentReminder() {
        // default
    }

    public LoanRepaymentReminder(Loan loan, Client client, Group group, LoanProduct loanProduct,
            LoanRepaymentScheduleInstallment scheduleInstallment, LocalDate dueDate, Integer installment,
            BigDecimal principalAmountOutStanding, BigDecimal interestAmountOutStanding, BigDecimal feesChargeAmountOutStanding,
            BigDecimal penaltyChargeAmountOutStanding, BigDecimal totalAmountOutStanding, Long batchId) {
        this.loan = loan;
        this.client = client;
        this.group = group;
        this.loanProduct = loanProduct;
        this.scheduleInstallment = scheduleInstallment;
        this.dueDate = dueDate;
        this.installment = installment;
        this.principalAmountOutStanding = principalAmountOutStanding;
        this.interestAmountOutStanding = interestAmountOutStanding;
        this.feesChargeAmountOutStanding = feesChargeAmountOutStanding;
        this.penaltyChargeAmountOutStanding = penaltyChargeAmountOutStanding;
        this.totalAmountOutStanding = totalAmountOutStanding;
        this.batchId = batchId;
    }

    public Loan getLoan() {
        return loan;
    }

    public Client getClient() {
        return client;
    }

    public Group getGroup() {
        return group;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public LoanRepaymentScheduleInstallment getScheduleInstallment() {
        return scheduleInstallment;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getInstallment() {
        return installment;
    }

    public BigDecimal getPrincipalAmountOutStanding() {
        return principalAmountOutStanding;
    }

    public BigDecimal getInterestAmountOutStanding() {
        return interestAmountOutStanding;
    }

    public BigDecimal getFeesChargeAmountOutStanding() {
        return feesChargeAmountOutStanding;
    }

    public BigDecimal getPenaltyChargeAmountOutStanding() {
        return penaltyChargeAmountOutStanding;
    }

    public BigDecimal getTotalAmountOutStanding() {
        return totalAmountOutStanding;
    }

    public Long getBatchId() {
        return batchId;
    }
}
