package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface LoanApplicationWritePlatformService {

    CommandProcessingResult submitLoanApplication(JsonCommand command);

    CommandProcessingResult modifyLoanApplication(Long loanId, JsonCommand command);

    CommandProcessingResult deleteLoanApplication(Long loanId);

    CommandProcessingResult approveLoanApplication(Long loanId, JsonCommand command);

    CommandProcessingResult undoLoanApplicationApproval(Long loanId, JsonCommand command);

    CommandProcessingResult rejectLoanApplication(Long loanId, JsonCommand command);

    CommandProcessingResult applicantWithdrawsFromLoanApplication(Long loanId, JsonCommand command);
}