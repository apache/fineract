/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link ClientCommand} 
 * 's.
 */
@Component
public final class GroupCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<GroupCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "externalId", "name", "officeId", "staffId",
            "parentId", "clientMembers", "childGroups"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GroupCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public GroupCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String externalId = this.fromApiJsonHelper.extractStringNamed("externalId", element);
        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", element);
        final Long staffId = this.fromApiJsonHelper.extractLongNamed("staffId", element);
        final Long parentId = this.fromApiJsonHelper.extractLongNamed("parentId", element);
        final String[] clientMembers = this.fromApiJsonHelper.extractArrayNamed("clientMembers", element);
        final String[] childGroups = this.fromApiJsonHelper.extractArrayNamed("childGroups", element);

        return new GroupCommand(externalId, name, officeId, staffId, clientMembers, childGroups, parentId);
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");

        final String externalIdParameterName = "externalId";
        if (this.fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String officeIdParameterName = "officeId";
        if (this.fromApiJsonHelper.parameterExists(officeIdParameterName, element)) {
            final Long officeId = this.fromApiJsonHelper.extractLongNamed(officeIdParameterName, element);
            baseDataValidator.reset().parameter(officeIdParameterName).value(officeId).notNull().integerGreaterThanZero();
        }

        final String parentIdParameterName = "parentId";
        if (this.fromApiJsonHelper.parameterExists(parentIdParameterName, element)) {
            final Long parentId = this.fromApiJsonHelper.extractLongNamed(parentIdParameterName, element);
            baseDataValidator.reset().parameter(parentIdParameterName).value(parentId).notNull().integerGreaterThanZero();
        }

        final String staffIdParameterName = "stafftId";
        if (this.fromApiJsonHelper.parameterExists(staffIdParameterName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
            baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        /*
         * Office updated is not supported
         */
        final Set<String> supportedParametersForupdate = new HashSet<String>(Arrays.asList("id", "externalId", "name", "staffId",
                "parentId", "clientMembers", "childGroups"));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForupdate);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");

        final String externalIdParameterName = "externalId";
        if (this.fromApiJsonHelper.parameterExists(externalIdParameterName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(externalIdParameterName, element);
            baseDataValidator.reset().parameter(externalIdParameterName).value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        }

        final String parentIdParameterName = "parentId";
        if (this.fromApiJsonHelper.parameterExists(parentIdParameterName, element)) {
            final Long parentId = this.fromApiJsonHelper.extractLongNamed(parentIdParameterName, element);
            baseDataValidator.reset().parameter(parentIdParameterName).value(parentId).notNull().integerGreaterThanZero();
        }

        final String staffIdParameterName = "stafftId";
        if (this.fromApiJsonHelper.parameterExists(staffIdParameterName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
            baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUnassignStaff(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        /*
         * Office updated is not supported
         */
        final Set<String> supportedParametersUnassignStaff = new HashSet<String>(Arrays.asList("staffId"));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");

        final String staffIdParameterName = "stafftId";
        if (this.fromApiJsonHelper.parameterExists(staffIdParameterName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
            baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}