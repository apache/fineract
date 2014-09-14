package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;

public interface LoanScheduleHistoryReadPlatformService {

    Integer fetchCurrentVersionNumber(Long loanId);

    LoanScheduleData retrieveRepaymentArchiveSchedule(Long loanId, RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
            Collection<DisbursementData> disbursementData);
}
