package org.mifosng.platform.configuration.service;

import org.mifosng.data.command.OrganisationCurrencyCommand;

public interface ConfigurationWritePlatformService {

	void updateOrganisationCurrencies(OrganisationCurrencyCommand command);
	
}