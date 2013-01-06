package org.mifosplatform.portfolio.fund.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface FundWritePlatformService {

    CommandProcessingResult createFund(JsonCommand command);

    CommandProcessingResult updateFund(Long fundId, JsonCommand command);
}