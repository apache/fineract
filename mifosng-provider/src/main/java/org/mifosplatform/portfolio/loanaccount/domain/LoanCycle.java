package org.mifosplatform.portfolio.loanaccount.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client_loan_counter")
public class LoanCycle extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "running_count", nullable = false)
    private Integer runningCount;

    public LoanCycle() {
        
    }

    private LoanCycle(final Client client, final LoanProduct loanProduct, final Loan loan, final Integer runningCounter) {
        this.client = client;
        this.loanProduct = loanProduct;
        this.loan = loan;
        this.runningCount = runningCounter;
    }

    public static LoanCycle create(final Client client, final LoanProduct loanProduct, final Loan loan, final Integer runningCounter) {
        return new LoanCycle(client, loanProduct, loan, runningCounter);
    }

    public Integer getRunningCounter() {
        return this.runningCount;
    }

    public Loan loan() {
        return this.loan;
    }

    public LoanCycle updateLoanCycleCounter(final Integer runningCount) {
        this.runningCount = runningCount;
        return this;
    }

}
