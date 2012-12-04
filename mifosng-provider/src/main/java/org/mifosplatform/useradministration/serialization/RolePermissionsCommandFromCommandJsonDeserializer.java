package org.mifosplatform.useradministration.serialization;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public final class RolePermissionsCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<RolePermissionCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public RolePermissionsCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public RolePermissionCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public RolePermissionCommand commandFromCommandJson(final Long roleId, final String commandAsJson) {
        return commandFromCommandJson(roleId, commandAsJson, false);
    }

    @Override
    public RolePermissionCommand commandFromCommandJson(final Long roleId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final RolePermissionCommand command = fromJsonHelper.fromJson(commandAsJson, RolePermissionCommand.class);

        return new RolePermissionCommand(roleId, command.getPermissions(), makerCheckerApproval);
    }
}