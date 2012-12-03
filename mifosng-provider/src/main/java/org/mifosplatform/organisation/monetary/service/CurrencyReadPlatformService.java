package org.mifosplatform.organisation.monetary.service;

import java.util.List;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

public interface CurrencyReadPlatformService {

    List<CurrencyData> retrieveAllowedCurrencies();

    List<CurrencyData> retrieveAllPlatformCurrencies();
}