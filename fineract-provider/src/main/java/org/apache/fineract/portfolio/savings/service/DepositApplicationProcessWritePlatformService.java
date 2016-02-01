/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savings.DepositAccountType;

public interface DepositApplicationProcessWritePlatformService {

    CommandProcessingResult submitFDApplication(JsonCommand command);

    CommandProcessingResult submitRDApplication(JsonCommand command);

    CommandProcessingResult modifyFDApplication(Long accountId, JsonCommand command);

    CommandProcessingResult modifyRDApplication(Long accountId, JsonCommand command);

    CommandProcessingResult deleteApplication(Long accountId, DepositAccountType depositAccountType);

    CommandProcessingResult approveApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult undoApplicationApproval(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult rejectApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);

    CommandProcessingResult applicantWithdrawsFromApplication(Long accountId, JsonCommand command, DepositAccountType depositAccountType);
}