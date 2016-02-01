/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savings.data.SavingsAccountDataDTO;

public interface SavingsApplicationProcessWritePlatformService {

    CommandProcessingResult submitApplication(JsonCommand command);

    CommandProcessingResult modifyApplication(Long savingsId, JsonCommand command);

    CommandProcessingResult deleteApplication(Long savingsId);

    CommandProcessingResult approveApplication(Long savingsId, JsonCommand command);

    CommandProcessingResult undoApplicationApproval(Long savingsId, JsonCommand command);

    CommandProcessingResult rejectApplication(Long savingsId, JsonCommand command);

    CommandProcessingResult applicantWithdrawsFromApplication(Long savingsId, JsonCommand command);

    CommandProcessingResult createActiveApplication(SavingsAccountDataDTO savingsAccountDataDTO);
}