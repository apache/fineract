package org.mifosplatform.useradministration.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.UserCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

/**
 *
 */
@Component
public final class UserCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<UserCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public UserCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public UserCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public UserCommand commandFromCommandJson(final Long userId, final String commandAsJson) {
        return commandFromCommandJson(userId, commandAsJson, false);
    }

    @Override
    public UserCommand commandFromCommandJson(final Long userId, final String commandAsJson, final boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);

        final String username = fromJsonHelper.extractStringNamed("username", element, parametersPassedInRequest);
        final String firstname = fromJsonHelper.extractStringNamed("firstname", element, parametersPassedInRequest);
        final String lastname = fromJsonHelper.extractStringNamed("lastname", element, parametersPassedInRequest);
        final String password = fromJsonHelper.extractStringNamed("password", element, parametersPassedInRequest);
        final String repeatPassword = fromJsonHelper.extractStringNamed("repeatPassword", element, parametersPassedInRequest);
        final String email = fromJsonHelper.extractStringNamed("email", element, parametersPassedInRequest);
        final Long officeId = fromJsonHelper.extractLongNamed("officeId", element, parametersPassedInRequest);

        final String[] notSelectedRoles = fromJsonHelper.extractArrayNamed("notSelectedRoles", element, parametersPassedInRequest);
        final String[] roles = fromJsonHelper.extractArrayNamed("roles", element, parametersPassedInRequest);

        return new UserCommand(parametersPassedInRequest, makerCheckerApproval, userId, username, firstname, lastname, password,
                repeatPassword, email, officeId, notSelectedRoles, roles);
    }
}