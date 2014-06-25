/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.transfer.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.group.api.GroupingTypesApiConstants;
import org.mifosplatform.portfolio.transfer.api.TransferApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class TransfersDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

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
                TransferApiConstants.TRANSFER_CLIENTS_BETWEEN_GROUPS_DATA_PARAMETERS);
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, TransferApiConstants.PROPOSE_CLIENT_TRANSFER_DATA_PARAMETERS);
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, TransferApiConstants.ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS);
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                TransferApiConstants.PROPOSE_AND_ACCEPT_CLIENT_TRANSFER_DATA_PARAMETERS);
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, TransferApiConstants.REJECT_CLIENT_TRANSFER_DATA_PARAMETERS);
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
                .checkForUnsupportedParameters(typeOfMap, json, TransferApiConstants.WITHDRAW_CLIENT_TRANSFER_DATA_PARAMETERS);
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