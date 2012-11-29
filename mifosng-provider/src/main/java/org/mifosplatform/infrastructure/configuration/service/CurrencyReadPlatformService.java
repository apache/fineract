package org.mifosplatform.infrastructure.configuration.service;

import java.util.List;

import org.mifosplatform.infrastructure.configuration.data.CurrencyData;

public interface CurrencyReadPlatformService {

    List<CurrencyData> retrieveAllowedCurrencies();

    List<CurrencyData> retrieveAllPlatformCurrencies();
}