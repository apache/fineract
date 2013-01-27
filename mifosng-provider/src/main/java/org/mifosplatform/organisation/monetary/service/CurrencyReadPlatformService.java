package org.mifosplatform.organisation.monetary.service;

import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

public interface CurrencyReadPlatformService {

    Collection<CurrencyData> retrieveAllowedCurrencies();

    Collection<CurrencyData> retrieveAllPlatformCurrencies();
}