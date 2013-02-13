/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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