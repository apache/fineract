package org.mifosng.platform.makerchecker.service;

import java.util.Collection;

import org.mifosng.platform.api.data.CommandSourceData;

public interface PortfolioCommandsReadPlatformService {

	Collection<CommandSourceData> retrieveAllEntriesToBeChecked();

	CommandSourceData retrieveById(Long id);

	Collection<CommandSourceData> retrieveUnprocessChangesByResourceId(String apiResource, Long clientId);

}
