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
package org.apache.fineract.portfolio.group.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class GroupRolesDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> GROUP_ROLES_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(GroupingTypesApiConstants.roleParamName, GroupingTypesApiConstants.clientIdParamName));

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
                .checkForUnsupportedParameters(typeOfMap, json, GROUP_ROLES_REQUEST_DATA_PARAMETERS);

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
                .checkForUnsupportedParameters(typeOfMap, json, GROUP_ROLES_REQUEST_DATA_PARAMETERS);

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
