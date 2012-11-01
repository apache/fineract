package org.mifosng.platform.loan.domain;

import java.util.List;
import java.util.Set;

import org.mifosng.platform.currency.domain.MonetaryCurrency;

public interface LoanRepaymentScheduleTransactionProcessor {

	void handleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges);

	void handleTransaction(List<LoanTransaction> repaymentsOrWaivers, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges);

	void handleWriteOff(LoanTransaction loanTransaction,
			MonetaryCurrency loanCurrency,
			List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);

}