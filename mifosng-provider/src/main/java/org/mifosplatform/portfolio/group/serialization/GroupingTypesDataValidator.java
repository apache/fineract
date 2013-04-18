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
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.group.api.GroupingTypesApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class GroupingTypesDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GroupingTypesDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForCreateCenter(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.CENTER_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.staffIdParamName).value(staffId).integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateCenterGroup(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        final Long centerId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.centerIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.centerIdParamName).value(centerId).notNull().integerGreaterThanZero();

        // office is inherited from center
        final Long officeId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.staffIdParamName).value(staffId).integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateGroup(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        // office is inherited from center
        final Long officeId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.staffIdParamName).value(staffId).integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateCenter(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.CENTER_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        // dont support update of office at present when updating center?
        // final Long officeId =
        // this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.officeIdParamName,
        // element);
        // baseDataValidator.reset().parameter(GroupingTypesApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.staffIdParamName).value(staffId).integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateGroup(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        JsonElement element = command.parsedJson();

        final String name = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(GroupingTypesApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.officeIdParamName, element)) {
            final Long officeId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.officeIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.officeIdParamName).value(officeId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.staffIdParamName).value(staffId).integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForActivation(final JsonCommand command, final String resourceName) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GroupingTypesApiConstants.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(resourceName);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = fromApiJsonHelper
                .extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUnassignStaff(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<String>(Arrays.asList("staffId"));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");

        final String staffIdParameterName = "staffId";
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}