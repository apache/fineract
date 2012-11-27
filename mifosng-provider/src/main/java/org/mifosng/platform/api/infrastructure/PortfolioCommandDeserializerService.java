package org.mifosng.platform.api.infrastructure;

import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.command.UserCommand;

/**
 * Service for de-serializing JSON for a command into the platforms internal
 * Java object representation of the command.
 * 
 * <p>
 * Known implementations:
 * </p>
 * 
 * @see PortfolioCommandDeerializerServiceGoogleGson
 */
public interface PortfolioCommandDeserializerService {

    RoleCommand deserializeRoleCommand(Long roleId, String commandAsJson, boolean makerCheckerApproval);

    RolePermissionCommand deserializeRolePermissionCommand(Long roleId, String commandAsJson, boolean makerCheckerApproval);

    UserCommand deserializeUserCommand(Long resourceId, String jsonOfChangesOnly, boolean makerCheckerApproval);
}