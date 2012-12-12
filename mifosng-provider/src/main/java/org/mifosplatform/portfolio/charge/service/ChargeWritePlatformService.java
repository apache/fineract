package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface ChargeWritePlatformService {

    EntityIdentifier createCharge(JsonCommand command);

    EntityIdentifier updateCharge(Long chargeId, JsonCommand command);

    EntityIdentifier deleteCharge(Long chargeId);
}
