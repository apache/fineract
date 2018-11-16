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
package org.apache.fineract.portfolio.transfer.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.transfer.api.TransferApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class TransfersDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> TRANSFER_CLIENTS_BETWEEN_GROUPS_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.localeParamName, TransferApiConstants.dateFormatParamName,
					TransferApiConstants.destinationGroupIdParamName, TransferApiConstants.clients,
					TransferApiConstants.inheritDestinationGroupLoanOfficer, TransferApiConstants.newStaffIdParamName,
					TransferApiConstants.transferActiveLoans));

	private static final Set<String> PROPOSE_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.localeParamName, TransferApiConstants.dateFormatParamName,
					TransferApiConstants.destinationOfficeIdParamName, TransferApiConstants.transferActiveLoans,
					TransferApiConstants.note, TransferApiConstants.transferDate));

	private static final Set<String> ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.newStaffIdParamName, TransferApiConstants.destinationGroupIdParamName,
					TransferApiConstants.note));

	private static final Set<String> PROPOSE_AND_ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.localeParamName, TransferApiConstants.dateFormatParamName,
					TransferApiConstants.destinationOfficeIdParamName, TransferApiConstants.transferActiveLoans,
					TransferApiConstants.newStaffIdParamName, TransferApiConstants.destinationGroupIdParamName,
					TransferApiConstants.note));

	private static final Set<String> REJECT_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.note));

	private static final Set<String> WITHDRAW_CLIENT_TRANSFER_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(TransferApiConstants.note));

    @Autowired
    public TransfersDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public void validateForClientsTransferBetweenGroups(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				TRANSFER_CLIENTS_BETWEEN_GROUPS_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        final Long destinationGroupId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.destinationGroupIdParamName, element);
        baseDataValidator.reset().parameter(TransferApiConstants.destinationGroupIdParamName).value(destinationGroupId).notNull()
                .integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.newStaffIdParamName, element)) {
            final Long newStaffId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.newStaffIdParamName, element);
            baseDataValidator.reset().parameter(TransferApiConstants.newStaffIdParamName).value(newStaffId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.inheritDestinationGroupLoanOfficer, element)) {
            final Boolean inheritDestinationGroupLoanOfficer = this.fromApiJsonHelper.extractBooleanNamed(
                    TransferApiConstants.inheritDestinationGroupLoanOfficer, element);
            baseDataValidator.reset().parameter(TransferApiConstants.inheritDestinationGroupLoanOfficer)
                    .value(inheritDestinationGroupLoanOfficer).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForProposeClientTransfer(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, PROPOSE_CLIENT_TRANSFER_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final Long destinationOfficeId = this.fromApiJsonHelper
                .extractLongNamed(TransferApiConstants.destinationOfficeIdParamName, element);
        baseDataValidator.reset().parameter(TransferApiConstants.destinationOfficeIdParamName).value(destinationOfficeId).notNull()
                .integerGreaterThanZero();
        
        validateNote(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForAcceptClientTransfer(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.newStaffIdParamName, element)) {
            final Long newStaffId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.newStaffIdParamName, element);
            baseDataValidator.reset().parameter(TransferApiConstants.newStaffIdParamName).value(newStaffId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.destinationGroupIdParamName, element)) {
            final Long destinationGroupId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.destinationGroupIdParamName,
                    element);
            baseDataValidator.reset().parameter(TransferApiConstants.destinationGroupIdParamName).value(destinationGroupId).notNull()
                    .integerGreaterThanZero();
        }

        validateNote(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForProposeAndAcceptClientTransfer(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, PROPOSE_AND_ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long destinationOfficeId = this.fromApiJsonHelper
                .extractLongNamed(TransferApiConstants.destinationOfficeIdParamName, element);
        baseDataValidator.reset().parameter(TransferApiConstants.destinationOfficeIdParamName).value(destinationOfficeId).notNull()
                .integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.newStaffIdParamName, element)) {
            final Long newStaffId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.newStaffIdParamName, element);
            baseDataValidator.reset().parameter(TransferApiConstants.newStaffIdParamName).value(newStaffId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(TransferApiConstants.destinationGroupIdParamName, element)) {
            final Long destinationGroupId = this.fromApiJsonHelper.extractLongNamed(TransferApiConstants.destinationGroupIdParamName,
                    element);
            baseDataValidator.reset().parameter(TransferApiConstants.destinationGroupIdParamName).value(destinationGroupId).notNull()
                    .integerGreaterThanZero();
        }

        validateNote(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForRejectClientTransfer(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REJECT_CLIENT_TRANSFER_DATA_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateNote(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForWithdrawClientTransfer(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper
                .checkForUnsupportedParameters(typeOfMap, json, WITHDRAW_CLIENT_TRANSFER_DATA_PARAMETERS);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateNote(baseDataValidator, element);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateNote(final DataValidatorBuilder baseDataValidator, final JsonElement element) {
        final String note = this.fromApiJsonHelper.extractStringNamed(TransferApiConstants.note, element);
        baseDataValidator.reset().parameter(TransferApiConstants.note).value(note).notExceedingLengthOf(1000);
    }

}