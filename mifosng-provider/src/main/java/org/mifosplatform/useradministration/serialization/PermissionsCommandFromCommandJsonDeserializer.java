package org.mifosplatform.useradministration.serialization;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public final class PermissionsCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<PermissionsCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public PermissionsCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public PermissionsCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public PermissionsCommand commandFromCommandJson(final Long roleId, final String commandAsJson) {
        return commandFromCommandJson(roleId, commandAsJson, false);
    }

    @Override
    public PermissionsCommand commandFromCommandJson(@SuppressWarnings("unused") final Long resourceId, final String commandAsJson,
            final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final PermissionsCommand command = fromJsonHelper.fromJson(commandAsJson, PermissionsCommand.class);

        return new PermissionsCommand(command.getPermissions(), makerCheckerApproval);
    }
}