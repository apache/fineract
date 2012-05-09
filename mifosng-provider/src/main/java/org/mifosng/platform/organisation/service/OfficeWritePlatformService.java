package org.mifosng.platform.organisation.service;

import org.mifosng.data.command.OfficeCommand;

public interface OfficeWritePlatformService {

	Long createOffice(OfficeCommand command);

	Long updateOffice(OfficeCommand command);
}