package org.mifosng.platform.configuration.service;

import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ConfigurationWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_CURRENCY')")
	void updateOrganisationCurrencies(OrganisationCurrencyCommand command);
	
}