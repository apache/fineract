package org.mifosplatform.infrastructure.commands.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.commands.api.data.CommandSourceData;

public interface PortfolioCommandsReadPlatformService {

    Collection<CommandSourceData> retrieveAllEntriesToBeChecked();

    CommandSourceData retrieveById(Long id);

    Collection<CommandSourceData> retrieveUnprocessChangesByResourceId(String apiResource, Long clientId);
}