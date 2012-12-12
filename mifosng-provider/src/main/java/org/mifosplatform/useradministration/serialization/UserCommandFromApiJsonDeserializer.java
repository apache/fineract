package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.command.UserCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link UserCommand}'s.
 */
@Component
public final class UserCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<UserCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("username", "firstname", "lastname", "password",
            "repeatPassword", "email", "officeId", "notSelectedRoles", "roles"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public UserCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public UserCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String username = fromApiJsonHelper.extractStringNamed("username", element);
        final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
        final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
        final String password = fromApiJsonHelper.extractStringNamed("password", element);
        final String repeatPassword = fromApiJsonHelper.extractStringNamed("repeatPassword", element);
        final String email = fromApiJsonHelper.extractStringNamed("email", element);
        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);

        final String[] notSelectedRoles = fromApiJsonHelper.extractArrayNamed("notSelectedRoles", element);
        final String[] roles = fromApiJsonHelper.extractArrayNamed("roles", element);

        return new UserCommand(username, firstname, lastname, password, repeatPassword, email, officeId, notSelectedRoles, roles);
    }
}