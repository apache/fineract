package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface ChargeWritePlatformService {

    CommandProcessingResult createCharge(JsonCommand command);

    CommandProcessingResult updateCharge(Long chargeId, JsonCommand command);

    CommandProcessingResult deleteCharge(Long chargeId);
}
