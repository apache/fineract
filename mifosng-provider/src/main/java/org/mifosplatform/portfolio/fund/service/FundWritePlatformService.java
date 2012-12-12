package org.mifosplatform.portfolio.fund.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface FundWritePlatformService {

    EntityIdentifier createFund(JsonCommand command);

    EntityIdentifier updateFund(Long fundId, JsonCommand command);
}