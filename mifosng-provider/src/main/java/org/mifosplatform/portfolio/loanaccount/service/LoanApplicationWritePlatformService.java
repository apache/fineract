package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface LoanApplicationWritePlatformService {

    EntityIdentifier submitLoanApplication(JsonCommand command);

    EntityIdentifier modifyLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier deleteLoanApplication(Long loanId);

    EntityIdentifier approveLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier undoLoanApplicationApproval(Long loanId, JsonCommand command);

    EntityIdentifier rejectLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier applicantWithdrawsFromLoanApplication(Long loanId, JsonCommand command);
}