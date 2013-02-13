/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation for deserializing users {@link JsonCommand} for validation.
 */
@Component
public final class UserCommandFromApiJsonDeserializerHelper {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("username", "firstname", "lastname", "password",
            "repeatPassword", "email", "officeId", "notSelectedRoles", "roles"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public UserCommandFromApiJsonDeserializerHelper(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String username = fromApiJsonHelper.extractStringNamed("username", element);
        baseDataValidator.reset().parameter("username").value(username).notBlank().notExceedingLengthOf(100);

        final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
        baseDataValidator.reset().parameter("firstname").value(firstname).notBlank().notExceedingLengthOf(100);

        final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
        baseDataValidator.reset().parameter("lastname").value(lastname).notBlank().notExceedingLengthOf(100);

        final String email = fromApiJsonHelper.extractStringNamed("email", element);
        baseDataValidator.reset().parameter("email").value(email).notBlank().notExceedingLengthOf(100);

        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);
        baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();

        final String[] roles = fromApiJsonHelper.extractArrayNamed("roles", element);
        baseDataValidator.reset().parameter("roles").value(roles).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        final JsonElement element = fromApiJsonHelper.parse(json);

        if (fromApiJsonHelper.parameterExists("officeId", element)) {
            final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);
            baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();
        }

        if (fromApiJsonHelper.parameterExists("username", element)) {
            final String username = fromApiJsonHelper.extractStringNamed("username", element);
            baseDataValidator.reset().parameter("username").value(username).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("firstname", element)) {
            final String firstname = fromApiJsonHelper.extractStringNamed("firstname", element);
            baseDataValidator.reset().parameter("firstname").value(firstname).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("lastname", element)) {
            final String lastname = fromApiJsonHelper.extractStringNamed("lastname", element);
            baseDataValidator.reset().parameter("lastname").value(lastname).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("email", element)) {
            final String email = fromApiJsonHelper.extractStringNamed("email", element);
            baseDataValidator.reset().parameter("email").value(email).notBlank().notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists("roles", element)) {
            final String[] roles = fromApiJsonHelper.extractArrayNamed("roles", element);
            baseDataValidator.reset().parameter("roles").value(roles).arrayNotEmpty();
        }

        if (fromApiJsonHelper.parameterExists("password", element)) {
            final String password = fromApiJsonHelper.extractStringNamed("password", element);
            final String repeatPassword = fromApiJsonHelper.extractStringNamed("repeatPassword", element);
            baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(50);
            if (StringUtils.isNotBlank(password)) {
                baseDataValidator.reset().parameter("password").value(password).equalToParameter("repeatPassword", repeatPassword);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}