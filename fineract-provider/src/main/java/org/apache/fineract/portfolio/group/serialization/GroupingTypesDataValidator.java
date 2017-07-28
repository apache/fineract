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

import com.google.gson.JsonArray;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class GroupingTypesDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    private static final Set<String> CENTER_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(GroupingTypesApiConstants.localeParamName, GroupingTypesApiConstants.dateFormatParamName,
					GroupingTypesApiConstants.idParamName, GroupingTypesApiConstants.nameParamName,
					GroupingTypesApiConstants.externalIdParamName, GroupingTypesApiConstants.officeIdParamName,
					GroupingTypesApiConstants.staffIdParamName, GroupingTypesApiConstants.activeParamName,
					GroupingTypesApiConstants.activationDateParamName, GroupingTypesApiConstants.groupMembersParamName,
					GroupingTypesApiConstants.submittedOnDateParamName, GroupingTypesApiConstants.datatables));

    private static final Set<String> GROUP_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			GroupingTypesApiConstants.localeParamName, GroupingTypesApiConstants.dateFormatParamName,
			GroupingTypesApiConstants.idParamName, GroupingTypesApiConstants.nameParamName,
			GroupingTypesApiConstants.externalIdParamName, GroupingTypesApiConstants.centerIdParamName,
			GroupingTypesApiConstants.officeIdParamName, GroupingTypesApiConstants.staffIdParamName,
			GroupingTypesApiConstants.activeParamName, GroupingTypesApiConstants.activationDateParamName,
			GroupingTypesApiConstants.clientMembersParamName, GroupingTypesApiConstants.collectionMeetingCalendar,
			GroupingTypesApiConstants.submittedOnDateParamName, GroupingTypesApiConstants.datatables));


    private static final Set<String> ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(GroupingTypesApiConstants.localeParamName, GroupingTypesApiConstants.dateFormatParamName,
					GroupingTypesApiConstants.activationDateParamName));

    private static final Set<String> GROUP_CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			GroupingTypesApiConstants.localeParamName, GroupingTypesApiConstants.dateFormatParamName,
			GroupingTypesApiConstants.closureDateParamName, GroupingTypesApiConstants.closureReasonIdParamName));

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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CENTER_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

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

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        GroupingTypesApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                final boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(
                    GroupingTypesApiConstants.submittedOnDateParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();
        }

        if(this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.datatables, element)){
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(GroupingTypesApiConstants.datatables, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateCenterGroup(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

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

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        GroupingTypesApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                final boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(
                    GroupingTypesApiConstants.submittedOnDateParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();
        }

        if(this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.datatables, element)){
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(GroupingTypesApiConstants.datatables, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateGroup(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

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

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        GroupingTypesApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            } else {
                // TODO - KW - not using config for now - just supporting move
                // to pending or active out of box.
                final boolean isPendingApprovalEnabled = true;
                if (!isPendingApprovalEnabled) {
                    baseDataValidator.reset().parameter(GroupingTypesApiConstants.activeParamName)
                            .failWithCode(".pending.status.not.allowed");
                }
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        if (this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(
                    GroupingTypesApiConstants.submittedOnDateParamName, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();
        }

        if(this.fromApiJsonHelper.parameterExists(GroupingTypesApiConstants.datatables, element)){
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(GroupingTypesApiConstants.datatables, element);
            baseDataValidator.reset().parameter(GroupingTypesApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateCenter(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CENTER_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

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

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        GroupingTypesApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateGroup(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, GROUP_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

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

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(GroupingTypesApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(
                        GroupingTypesApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForActivation(final JsonCommand command, final String resourceName) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(resourceName);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = this.fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.activationDateParamName,
                element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUnassignStaff(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<>(Arrays.asList("staffId"));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");

        final String staffIdParameterName = "staffId";
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForAssignStaff(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersAssignStaff = new HashSet<>(Arrays.asList(GroupingTypesApiConstants.staffIdParamName,GroupingTypesApiConstants.inheritStaffForClientAccounts));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersAssignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final String staffIdParameterName = GroupingTypesApiConstants.staffIdParamName;
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().longGreaterThanZero();

        final String inheritStaffForClientAccountsParamName = GroupingTypesApiConstants.inheritStaffForClientAccounts;
        final Boolean inheritStaffForClientAccounts= this.fromApiJsonHelper.extractBooleanNamed(inheritStaffForClientAccountsParamName,element);
        baseDataValidator.reset().parameter(inheritStaffForClientAccountsParamName).value(inheritStaffForClientAccounts).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);


        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForAssociateClients(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(GroupingTypesApiConstants.clientMembersParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        final String[] clients = this.fromApiJsonHelper.extractArrayNamed(GroupingTypesApiConstants.clientMembersParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.clientMembersParamName).value(clients).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForDisassociateClients(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(GroupingTypesApiConstants.clientMembersParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        final String[] clients = this.fromApiJsonHelper.extractArrayNamed(GroupingTypesApiConstants.clientMembersParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.clientMembersParamName).value(clients).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForGroupClose(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper
                .checkForUnsupportedParameters(typeOfMap, json, GROUP_CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate closureDate = this.fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.closureDateParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.closureDateParamName).value(closureDate).notNull();

        final Long closureReasonId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.closureReasonIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.closureReasonIdParamName).value(closureReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForCenterClose(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper
                .checkForUnsupportedParameters(typeOfMap, json, GROUP_CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate closureDate = this.fromApiJsonHelper.extractLocalDateNamed(GroupingTypesApiConstants.closureDateParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.closureDateParamName).value(closureDate).notNull();

        final Long closureReasonId = this.fromApiJsonHelper.extractLongNamed(GroupingTypesApiConstants.closureReasonIdParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.closureReasonIdParamName).value(closureReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForAssociateGroups(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(GroupingTypesApiConstants.groupMembersParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        final String[] groups = this.fromApiJsonHelper.extractArrayNamed(GroupingTypesApiConstants.groupMembersParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.groupMembersParamName).value(groups).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForDisassociateGroups(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(GroupingTypesApiConstants.groupMembersParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("group");
        final String[] groups = this.fromApiJsonHelper.extractArrayNamed(GroupingTypesApiConstants.groupMembersParamName, element);
        baseDataValidator.reset().parameter(GroupingTypesApiConstants.groupMembersParamName).value(groups).arrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}