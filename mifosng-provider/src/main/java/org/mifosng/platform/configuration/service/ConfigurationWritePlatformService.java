package org.mifosng.platform.configuration.service;

import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;

public interface ConfigurationWritePlatformService {

	void updateOrganisationCurrencies(OrganisationCurrencyCommand command);
	
}