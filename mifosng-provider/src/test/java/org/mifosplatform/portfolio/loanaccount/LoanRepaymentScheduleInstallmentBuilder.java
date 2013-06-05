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
        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, installmentNumber, fromDate, dueDate,
                principal.getAmount(), interest.getAmount(), feeCharges.getAmount(), penaltyCharges.getAmount());
        if (completed) {
            installment.payPrincipalComponent(latestTransactionDate, principal);
            installment.payInterestComponent(latestTransactionDate, interest);
        }
        return installment;
    }

    public LoanRepaymentScheduleInstallmentBuilder withPrincipal(final String withPrincipal) {
        this.principal = new MoneyBuilder().with(currencyDetail).with(withPrincipal).build();
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withInterest(final String withInterest) {
        this.interest = new MoneyBuilder().with(currencyDetail).with(withInterest).build();
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