/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;

public interface LoanRepaymentScheduleTransactionProcessor {

    void handleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges);

    ChangedTransactionDetail handleTransaction(LocalDate disbursementDate, List<LoanTransaction> repaymentsOrWaivers, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Set<LoanCharge> charges);

    void handleWriteOff(LoanTransaction loanTransaction, MonetaryCurrency loanCurrency,
            List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments);

}