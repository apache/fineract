package org.mifosplatform.organisation.monetary.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface CurrencyWritePlatformService {

    EntityIdentifier updateAllowedCurrencies(JsonCommand command);

}