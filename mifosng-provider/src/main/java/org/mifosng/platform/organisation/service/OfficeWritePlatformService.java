package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.OfficeCommand;

public interface OfficeWritePlatformService {

	Long createOffice(final OfficeCommand command);
	
	Long updateOffice(final OfficeCommand command);
}