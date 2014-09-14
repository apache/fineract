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
