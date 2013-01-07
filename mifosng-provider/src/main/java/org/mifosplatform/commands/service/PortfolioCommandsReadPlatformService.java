package org.mifosplatform.commands.service;

import java.util.Collection;

import org.mifosplatform.commands.data.CommandSourceData;

public interface PortfolioCommandsReadPlatformService {

    Collection<CommandSourceData> retrieveAllEntriesToBeChecked();

    CommandSourceData retrieveById(Long id);

    Collection<CommandSourceData> retrieveUnprocessChangesByEntityNameAndId(String entityName, Long entityId);
}