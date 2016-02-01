/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface LoanApplicationWritePlatformService {

    CommandProcessingResult submitApplication(JsonCommand command);

    CommandProcessingResult modifyApplication(Long loanId, JsonCommand command);

    CommandProcessingResult deleteApplication(Long loanId);

    CommandProcessingResult approveApplication(Long loanId, JsonCommand command);

    CommandProcessingResult undoApplicationApproval(Long loanId, JsonCommand command);

    CommandProcessingResult rejectApplication(Long loanId, JsonCommand command);

    CommandProcessingResult applicantWithdrawsFromApplication(Long loanId, JsonCommand command);
}