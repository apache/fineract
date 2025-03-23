/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.useradministration.service;

import static org.apache.fineract.useradministration.service.AppUserConstants.CLIENTS;
import static org.apache.fineract.useradministration.service.AppUserConstants.PASSWORD;
import static org.apache.fineract.useradministration.service.AppUserConstants.REPEAT_PASSWORD;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicy;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class UserDataValidator {

    public static final String USERNAME = "username";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String EMAIL = "email";
    public static final String OFFICE_ID = "officeId";
    public static final String NOT_SELECTED_ROLES = "notSelectedRoles";
    public static final String ROLES = "roles";
    public static final String SEND_PASSWORD_TO_EMAIL = "sendPasswordToEmail";
    public static final String STAFF_ID = "staffId";
    public static final String PASSWORD_NEVER_EXPIRES = "passwordNeverExpires";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> CREATE_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(USERNAME, FIRSTNAME, LASTNAME, PASSWORD, REPEAT_PASSWORD, EMAIL, OFFICE_ID, NOT_SELECTED_ROLES, ROLES,
                    SEND_PASSWORD_TO_EMAIL, STAFF_ID, PASSWORD_NEVER_EXPIRES, AppUserConstants.IS_SELF_SERVICE_USER, CLIENTS));
    private static final Set<String> UPDATE_SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(USERNAME, FIRSTNAME, LASTNAME, PASSWORD, REPEAT_PASSWORD, EMAIL, OFFICE_ID, NOT_SELECTED_ROLES, ROLES,
                    SEND_PASSWORD_TO_EMAIL, STAFF_ID, PASSWORD_NEVER_EXPIRES, AppUserConstants.IS_SELF_SERVICE_USER, CLIENTS));
    private static final Set<String> CHANGE_PASSWORD_SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(PASSWORD, REPEAT_PASSWORD));
    public static final String PASSWORD_NEVER_EXPIRE = "passwordNeverExpire";

    private final FromJsonHelper fromApiJsonHelper;

    private final PasswordValidationPolicyRepository passwordValidationPolicy;

    @Autowired
    public UserDataValidator(final FromJsonHelper fromApiJsonHelper, final PasswordValidationPolicyRepository passwordValidationPolicy) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.passwordValidationPolicy = passwordValidationPolicy;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String username = this.fromApiJsonHelper.extractStringNamed(USERNAME, element);
        baseDataValidator.reset().parameter(USERNAME).value(username).notBlank().notExceedingLengthOf(100);

        final String firstname = this.fromApiJsonHelper.extractStringNamed(FIRSTNAME, element);
        baseDataValidator.reset().parameter(FIRSTNAME).value(firstname).notBlank().notExceedingLengthOf(100);

        final String lastname = this.fromApiJsonHelper.extractStringNamed(LASTNAME, element);
        baseDataValidator.reset().parameter(LASTNAME).value(lastname).notBlank().notExceedingLengthOf(100);

        final Boolean sendPasswordToEmail = this.fromApiJsonHelper.extractBooleanNamed(SEND_PASSWORD_TO_EMAIL, element);
        if (sendPasswordToEmail != null) {
            if (sendPasswordToEmail.booleanValue()) {
                final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
                baseDataValidator.reset().parameter(EMAIL).value(email).notBlank().notExceedingLengthOf(100);
            } else {
                validatePassword(baseDataValidator, element);
            }
        } else {
            baseDataValidator.reset().parameter(SEND_PASSWORD_TO_EMAIL).value(sendPasswordToEmail).trueOrFalseRequired(false);
        }

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
        baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(STAFF_ID, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(STAFF_ID, element);
            baseDataValidator.reset().parameter(STAFF_ID).value(staffId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(AppUserConstants.PASSWORD_NEVER_EXPIRES, element)) {
            final boolean passwordNeverExpire = this.fromApiJsonHelper.extractBooleanNamed(AppUserConstants.PASSWORD_NEVER_EXPIRES,
                    element);
            baseDataValidator.reset().parameter(PASSWORD_NEVER_EXPIRE).value(passwordNeverExpire).validateForBooleanValue();
        }

        Boolean isSelfServiceUser = null;
        if (this.fromApiJsonHelper.parameterExists(AppUserConstants.IS_SELF_SERVICE_USER, element)) {
            isSelfServiceUser = this.fromApiJsonHelper.extractBooleanNamed(AppUserConstants.IS_SELF_SERVICE_USER, element);
            if (isSelfServiceUser == null) {
                baseDataValidator.reset().parameter(AppUserConstants.IS_SELF_SERVICE_USER).trueOrFalseRequired(false);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CLIENTS, element)) {
            if (isSelfServiceUser == null || !isSelfServiceUser) {
                baseDataValidator.reset().parameter(CLIENTS).failWithCode("not.supported.when.isSelfServiceUser.is.false",
                        "clients parameter is not supported when isSelfServiceUser parameter is false");
            } else {
                final JsonArray clientsArray = this.fromApiJsonHelper.extractJsonArrayNamed(CLIENTS, element);
                baseDataValidator.reset().parameter(CLIENTS).value(clientsArray).jsonArrayNotEmpty();

                for (JsonElement client : clientsArray) {
                    Long clientId = client.getAsLong();
                    baseDataValidator.reset().parameter(CLIENTS).value(clientId).longGreaterThanZero();
                }
            }
        }

        final String[] roles = this.fromApiJsonHelper.extractArrayNamed(ROLES, element);
        baseDataValidator.reset().parameter(ROLES).value(roles).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private Set<String> getParamNamesFromRequest(final String json) {
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (element.isJsonObject()) {
            return ((JsonObject) element).keySet();
        }
        return Set.of();
    }

    void validateFieldLevelACL(final String json, AppUser authenticatedUser) {
        if (!authenticatedUser.hasAnyPermission("ALL_FUNCTIONS", "UPDATE_USER")) {
            Set<String> paramNamesFromRequest = getParamNamesFromRequest(json);
            if (authenticatedUser.isSelfServiceUser()) {
                // selfService user can change the clients and the password
                paramNamesFromRequest.removeAll(Set.of(CLIENTS, PASSWORD, REPEAT_PASSWORD));
            } else {
                // user without permission can only change the password
                paramNamesFromRequest.removeAll(Set.of(PASSWORD, REPEAT_PASSWORD));
            }
            if (paramNamesFromRequest.size() > 0) {
                throw new PlatformApiDataValidationException(
                        List.of(ApiParameterError.parameterError("not.enough.permission.to.update.fields",
                                "Current user has no permission to update fields", String.join(",", paramNamesFromRequest))));
            }
        }
    }

    public void validateForChangePassword(final String json, AppUser authenticatedUser) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CHANGE_PASSWORD_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validatePassword(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        validateFieldLevelACL(json, authenticatedUser);
    }

    public void validateForUpdate(final String json, AppUser authenticatedUser) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(OFFICE_ID, element)) {
            final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
            baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(STAFF_ID, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(STAFF_ID, element);
            baseDataValidator.reset().parameter(STAFF_ID).value(staffId).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(USERNAME, element)) {
            final String username = this.fromApiJsonHelper.extractStringNamed(USERNAME, element);
            baseDataValidator.reset().parameter(USERNAME).value(username).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(FIRSTNAME, element)) {
            final String firstname = this.fromApiJsonHelper.extractStringNamed(FIRSTNAME, element);
            baseDataValidator.reset().parameter(FIRSTNAME).value(firstname).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(LASTNAME, element)) {
            final String lastname = this.fromApiJsonHelper.extractStringNamed(LASTNAME, element);
            baseDataValidator.reset().parameter(LASTNAME).value(lastname).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(EMAIL, element)) {
            final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
            baseDataValidator.reset().parameter(EMAIL).value(email).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(ROLES, element)) {
            final String[] roles = this.fromApiJsonHelper.extractArrayNamed(ROLES, element);
            baseDataValidator.reset().parameter(ROLES).value(roles).arrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(PASSWORD, element)) {
            validatePassword(baseDataValidator, element);
        }

        if (this.fromApiJsonHelper.parameterExists(PASSWORD_NEVER_EXPIRE, element)) {
            final boolean passwordNeverExpire = this.fromApiJsonHelper.extractBooleanNamed(PASSWORD_NEVER_EXPIRE, element);
            baseDataValidator.reset().parameter(PASSWORD_NEVER_EXPIRE).value(passwordNeverExpire).validateForBooleanValue();
        }

        Boolean isSelfServiceUser = null;
        if (this.fromApiJsonHelper.parameterExists(AppUserConstants.IS_SELF_SERVICE_USER, element)) {
            isSelfServiceUser = this.fromApiJsonHelper.extractBooleanNamed(AppUserConstants.IS_SELF_SERVICE_USER, element);
            if (isSelfServiceUser == null) {
                baseDataValidator.reset().parameter(AppUserConstants.IS_SELF_SERVICE_USER).trueOrFalseRequired(false);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CLIENTS, element)) {
            if (isSelfServiceUser != null && !isSelfServiceUser) {
                baseDataValidator.reset().parameter(CLIENTS).failWithCode("not.supported.when.isSelfServiceUser.is.false",
                        "clients parameter is not supported when isSelfServiceUser parameter is false");
            } else {
                final JsonArray clientsArray = this.fromApiJsonHelper.extractJsonArrayNamed(CLIENTS, element);
                baseDataValidator.reset().parameter(CLIENTS).value(clientsArray).jsonArrayNotEmpty();

                for (JsonElement client : clientsArray) {
                    Long clientId = client.getAsLong();
                    baseDataValidator.reset().parameter(CLIENTS).value(clientId).longGreaterThanZero();
                }
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        validateFieldLevelACL(json, authenticatedUser);
    }

    private void validatePassword(DataValidatorBuilder baseDataValidator, JsonElement element) {
        final String password = this.fromApiJsonHelper.extractStringNamed(PASSWORD, element);
        final String repeatPassword = this.fromApiJsonHelper.extractStringNamed(REPEAT_PASSWORD, element);

        final PasswordValidationPolicy validationPolicy = this.passwordValidationPolicy.findActivePasswordValidationPolicy();
        final String regex = validationPolicy.getRegex();
        final String description = validationPolicy.getDescription();
        DataValidatorBuilder validator = baseDataValidator.reset().parameter(PASSWORD).value(password).matchesRegularExpression(regex,
                description);
        if (StringUtils.isNotBlank(password)) {
            validator.equalToParameter(REPEAT_PASSWORD, repeatPassword);
        }
    }
}
