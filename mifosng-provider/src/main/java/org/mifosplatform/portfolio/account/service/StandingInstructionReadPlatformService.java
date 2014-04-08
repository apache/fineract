/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.account.data.StandingInstructionDTO;
import org.mifosplatform.portfolio.account.data.StandingInstructionData;
import org.mifosplatform.portfolio.account.data.StandingInstructionDuesData;

public interface StandingInstructionReadPlatformService {

    StandingInstructionData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType, Integer transferType);

    Page<StandingInstructionData> retrieveAll(StandingInstructionDTO standingInstructionDTO);

    StandingInstructionData retrieveOne(Long instructionId);

    Collection<StandingInstructionData> retrieveAll(Integer status);

    StandingInstructionDuesData retriveLoanDuesData(Long loanId);

}