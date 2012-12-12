package org.mifosplatform.useradministration.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserCommandHandler implements NewCommandSourceHandler {

    private final AppUserWritePlatformService writePlatformService;
    private final PlatformSecurityContext context;

    @Autowired
    public UpdateUserCommandHandler(final PlatformSecurityContext context, final AppUserWritePlatformService writePlatformService) {
        this.context = context;
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        final AppUser loggedInUser = context.authenticatedUser();

        final Long userId = command.resourceId();
        EntityIdentifier result = null;
        if (loggedInUser.hasIdOf(userId)) {
            result = this.writePlatformService.updateUsersOwnAccountDetails(userId, command);
        } else {
            result = this.writePlatformService.updateUser(userId, command);
        }

        return result;
    }
}