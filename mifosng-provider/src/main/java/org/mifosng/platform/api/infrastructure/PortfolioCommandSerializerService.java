package org.mifosng.platform.api.infrastructure;

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.commands.RolePermissionCommand;

/**
 * Service for serializing commands into another format.
 * 
 * <p>Known implementations:</p>
 * @see PortfolioCommandSerializerServiceJson 
 */
public interface PortfolioCommandSerializerService {

    String serializeRoleCommandToJson(RoleCommand command);

    String serializeRolePermissionCommandToJson(RolePermissionCommand command);

    String serializeUserCommandToJson(UserCommand command);
}