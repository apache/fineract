package org.mifosng.platform.currency.service;

import java.util.List;

import org.mifosng.platform.api.data.CurrencyData;

public interface CurrencyReadPlatformService {
	
	List<CurrencyData> retrieveAllowedCurrencies();
	
	List<CurrencyData> retrieveAllPlatformCurrencies();
}