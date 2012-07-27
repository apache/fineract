package org.mifosng.platform.loan.domain;

import java.util.List;

import org.mifosng.platform.currency.domain.MonetaryCurrency;

public interface LoanRepaymentScheduleTransactionProcessor {

	void handleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments);

	void handleTransaction(List<LoanTransaction> repaymentsOrWaivers, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);

}