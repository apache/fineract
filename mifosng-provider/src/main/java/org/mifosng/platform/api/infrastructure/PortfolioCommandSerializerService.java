package org.mifosng.platform.api.infrastructure;

import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.command.UserCommand;

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