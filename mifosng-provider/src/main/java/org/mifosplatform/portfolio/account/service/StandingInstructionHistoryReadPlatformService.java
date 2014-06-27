/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.account.data.StandingInstructionDTO;
import org.mifosplatform.portfolio.account.data.StandingInstructionHistoryData;

public interface StandingInstructionHistoryReadPlatformService {

    Page<StandingInstructionHistoryData> retrieveAll(StandingInstructionDTO standingInstructionDTO);

}