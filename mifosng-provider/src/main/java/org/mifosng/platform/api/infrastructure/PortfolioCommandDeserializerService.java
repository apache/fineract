package org.mifosng.platform.api.infrastructure;

import org.mifosng.platform.api.commands.RoleCommand;

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
}