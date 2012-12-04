package org.mifosplatform.useradministration.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

/**
 *
 */
@Component
public final class RoleCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<RoleCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public RoleCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public RoleCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public RoleCommand commandFromCommandJson(final Long roleId, final String commandAsJson) {
        return commandFromCommandJson(roleId, commandAsJson, false);
    }

    @Override
    public RoleCommand commandFromCommandJson(final Long roleId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        
        final String name = fromJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);
        final String description = fromJsonHelper.extractStringNamed("description", element, parametersPassedInRequest);

        return new RoleCommand(parametersPassedInRequest, makerCheckerApproval, roleId, name, description);
    }
}