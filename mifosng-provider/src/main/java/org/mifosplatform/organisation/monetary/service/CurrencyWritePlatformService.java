package org.mifosplatform.organisation.monetary.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CurrencyWritePlatformService {

    CommandProcessingResult updateAllowedCurrencies(JsonCommand command);

}