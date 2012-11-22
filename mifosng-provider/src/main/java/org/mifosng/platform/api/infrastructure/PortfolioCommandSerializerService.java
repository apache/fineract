package org.mifosng.platform.api.infrastructure;

import org.mifosng.platform.api.commands.RoleCommand;

/**
 * Service for serializing commands into another format.
 * 
 * <p>Known implementations:</p>
 * @see PortfolioCommandSerializerServiceJson 
 */
public interface PortfolioCommandSerializerService {

    String serializeRoleCommandToJson(RoleCommand command);
}