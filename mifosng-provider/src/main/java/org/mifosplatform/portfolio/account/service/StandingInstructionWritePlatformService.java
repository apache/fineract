/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;

public interface StandingInstructionWritePlatformService {

    CommandProcessingResult create(JsonCommand command);

    CommandProcessingResult update(Long id, JsonCommand command);

    void executeStandingInstructions() throws JobExecutionException;

    CommandProcessingResult delete(Long id);

}