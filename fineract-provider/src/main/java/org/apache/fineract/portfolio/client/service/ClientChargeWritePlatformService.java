/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.transaction.annotation.Transactional;

public interface ClientChargeWritePlatformService {

    @Transactional
    CommandProcessingResult addCharge(Long clientId, JsonCommand command);

    @Transactional
    CommandProcessingResult updateCharge(Long clientId, JsonCommand command);

    @Transactional
    CommandProcessingResult deleteCharge(Long clientId, Long clientChargeId);

    @Transactional
    CommandProcessingResult waiveCharge(Long clientId, Long clientChargeId);

    @Transactional
    CommandProcessingResult payCharge(Long clientId, Long clientChargeId, JsonCommand command);

    @Transactional
    CommandProcessingResult inactivateCharge(Long clientId, Long clientChargeId);

}
