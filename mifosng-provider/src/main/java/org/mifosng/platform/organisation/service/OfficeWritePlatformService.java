package org.mifosng.platform.organisation.service;

import org.mifosng.data.command.OfficeCommand;

public interface OfficeWritePlatformService {

	Long createOffice(final OfficeCommand command);
	
	Long updateOffice(final OfficeCommand command);
}