/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;

public interface LoanScheduleHistoryWritePlatformService {

    List<LoanRepaymentScheduleHistory> createLoanScheduleArchive(
            final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, final Loan loan,
            final LoanRescheduleRequest loanRescheduleRequest);

    void createAndSaveLoanScheduleArchive(List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments, Loan loan,
            LoanRescheduleRequest loanRescheduleRequest);

}
