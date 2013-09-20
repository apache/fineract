package org.mifosplatform.portfolio.loanaccount;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

public class LoanRepaymentScheduleInstallmentBuilder {

    private final Loan loan = null;
    private Integer installmentNumber = Integer.valueOf(1);
    private final LocalDate fromDate = LocalDate.now();
    private LocalDate dueDate = LocalDate.now();
    private final LocalDate latestTransactionDate = LocalDate.now();
    private MonetaryCurrency currencyDetail = new MonetaryCurrencyBuilder().build();
    private Money principal = new MoneyBuilder().build();
    private Money interest = new MoneyBuilder().build();
    private final Money feeCharges = new MoneyBuilder().build();
    private final Money penaltyCharges = new MoneyBuilder().build();
    private boolean completed = false;

    public LoanRepaymentScheduleInstallmentBuilder(final MonetaryCurrency currencyDetail) {
        this.currencyDetail = currencyDetail;
        this.principal = new MoneyBuilder().with(currencyDetail).build();
        this.interest = new MoneyBuilder().with(currencyDetail).build();
    }

    public LoanRepaymentScheduleInstallment build() {
        final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this.loan, this.installmentNumber,
                this.fromDate, this.dueDate, this.principal.getAmount(), this.interest.getAmount(), this.feeCharges.getAmount(),
                this.penaltyCharges.getAmount());
        if (this.completed) {
            installment.payPrincipalComponent(this.latestTransactionDate, this.principal);
            installment.payInterestComponent(this.latestTransactionDate, this.interest);
        }
        return installment;
    }

    public LoanRepaymentScheduleInstallmentBuilder withPrincipal(final String withPrincipal) {
        this.principal = new MoneyBuilder().with(this.currencyDetail).with(withPrincipal).build();
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withInterest(final String withInterest) {
        this.interest = new MoneyBuilder().with(this.currencyDetail).with(withInterest).build();
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withDueDate(final LocalDate withDueDate) {
        this.dueDate = withDueDate;
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder completed() {
        this.completed = true;
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withInstallmentNumber(final int withInstallmentNumber) {
        this.installmentNumber = withInstallmentNumber;
        return this;
    }
}