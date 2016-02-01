/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.group.api.GroupingTypesApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class GroupRolesDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GroupRolesDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForCreateGroupRole(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper
                .checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.GROUP_ROLES_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_ROLE_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final Long roleId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.roleParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.roleParamName).value(roleId).notNull().longGreaterThanZero();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.clientIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.clientIdParamName).value(clientId).notNull().longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateRole(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper
                .checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.GROUP_ROLES_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_ROLE_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final Long roleId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.roleParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.roleParamName).value(roleId).ignoreIfNull().notBlank()
                .longGreaterThanZero();

        final Long clientId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.clientIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.clientIdParamName).value(clientId).ignoreIfNull().notBlank()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
