package org.mifosplatform.organisation.monetary.service;

import org.mifosplatform.organisation.monetary.command.CurrencyCommand;

public interface CurrencyWritePlatformService {

    void updateAllowedCurrencies(CurrencyCommand command);

}