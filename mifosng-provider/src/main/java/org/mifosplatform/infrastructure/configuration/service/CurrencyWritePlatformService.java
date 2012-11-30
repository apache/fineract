package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.configuration.command.CurrencyCommand;

public interface CurrencyWritePlatformService {

    void updateAllowedCurrencies(CurrencyCommand command);

}