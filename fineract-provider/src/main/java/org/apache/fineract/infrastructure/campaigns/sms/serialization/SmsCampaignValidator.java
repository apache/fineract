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
package org.apache.fineract.infrastructure.campaigns.sms.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistration;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistrationRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SmsCampaignValidator {

    public static final String RESOURCE_NAME = "sms";
    public static final String campaignName = "campaignName";
    public static final String campaignType = "campaignType";
    public static final String triggerType = "triggerType";
    public static final String triggerEntityType = "triggerEntityType";
    public static final String triggerActionType = "triggerActionType";
    public static final String providerId = "providerId";
    public static final String runReportId = "runReportId";
    public static final String paramValue = "paramValue";
    public static final String message = "message";
    public static final String activationDateParamName = "activationDate";
    public static final String recurrenceStartDate = "recurrenceStartDate";
    public static final String dateTimeFormat = "dateTimeFormat";
    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String closureDateParamName = "closureDate";
    public static final String recurrenceParamName = "recurrence";
    public static final String statusParamName = "status";

    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    public static final String frequencyParamName = "frequency";
    public static final String intervalParamName = "interval";
    public static final String repeatsOnDayParamName = "repeatsOnDay";
    public static final String isNotificationParamName = "isNotification";

    private final FromJsonHelper fromApiJsonHelper;
    private final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository;

    protected static final Set<String> supportedParams = new HashSet<>(Arrays.asList(campaignName, campaignType,
            localeParamName,
            dateFormatParamName, runReportId, paramValue, message, recurrenceStartDate, activationDateParamName, submittedOnDateParamName,
            closureDateParamName, recurrenceParamName, providerId, triggerType, frequencyParamName, intervalParamName,
            repeatsOnDayParamName, triggerEntityType, triggerActionType, dateTimeFormat, isNotificationParamName));

    protected static final Set<String> supportedParamsForUpdate = new HashSet<>(Arrays.asList(campaignName, campaignType,
            localeParamName,
            dateFormatParamName, runReportId, paramValue, message, recurrenceStartDate, activationDateParamName, recurrenceParamName,
            providerId, triggerType, triggerEntityType, triggerActionType, dateTimeFormat, isNotificationParamName));

    protected static final Set<String> ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName,
            activationDateParamName));

    protected static final Set<String> CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName,
            closureDateParamName));

    protected static final Set<String> PREVIEW_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(paramValue, message));

    @Autowired
    public SmsCampaignValidator(FromJsonHelper fromApiJsonHelper, final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.deviceRegistrationRepository = deviceRegistrationRepository;
    }

    public void validateCreate(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.supportedParams);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);

        final String campaignName = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.campaignName, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.campaignName).value(campaignName).notBlank().notExceedingLengthOf(100);

        final Long campaignType = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.campaignType, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.campaignType).value(campaignType).notNull().integerGreaterThanZero();

        final Long triggerType = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.triggerType, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.triggerType).value(triggerType).notNull().integerGreaterThanZero();

        if (triggerType.intValue() == SmsCampaignTriggerType.SCHEDULE.getValue()) {

            final Integer frequencyParam = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(SmsCampaignValidator.frequencyParamName,
                    element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.frequencyParamName).value(frequencyParam).notNull()
                    .integerGreaterThanZero();

            final String intervalParam = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.intervalParamName, element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.intervalParamName).value(intervalParam).notBlank();

            if (frequencyParam != null && frequencyParam.equals(CalendarFrequencyType.WEEKLY.getValue())) {
                final String repeatsOnDayParam = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.repeatsOnDayParamName,
                        element);
                baseDataValidator.reset().parameter(SmsCampaignValidator.repeatsOnDayParamName).value(repeatsOnDayParam).notBlank();
            }
            final String recurrenceStartDate = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.recurrenceStartDate, element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.recurrenceStartDate).value(recurrenceStartDate).notBlank();
        }

        final Long runReportId = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.runReportId, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.runReportId).value(runReportId).notNull().integerGreaterThanZero();
        
        final String message = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.message, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.message).value(message).notBlank().notExceedingLengthOf(480);

        final JsonElement paramValueJsonObject = this.fromApiJsonHelper.extractJsonObjectNamed(SmsCampaignValidator.paramValue, element);
        if (triggerType.intValue() != SmsCampaignTriggerType.TRIGGERED.getValue()) {
            baseDataValidator.reset().parameter(SmsCampaignValidator.paramValue).value(paramValueJsonObject).notBlank();
            if (paramValueJsonObject != null && paramValueJsonObject.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : paramValueJsonObject.getAsJsonObject().entrySet()) {
                    final JsonElement inner = entry.getValue();
                    baseDataValidator.reset().parameter(entry.getKey()).value(inner).notBlank();
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(SmsCampaignValidator.submittedOnDateParamName,
                    element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.submittedOnDateParamName).value(submittedOnDate).notNull();
        }
        
        if (this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.isNotificationParamName, element)) {
            final Boolean isNotification = this.fromApiJsonHelper.extractBooleanNamed(SmsCampaignValidator.isNotificationParamName,
                    element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.submittedOnDateParamName).trueOrFalseRequired(isNotification);
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.supportedParamsForUpdate);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);

        final String campaignName = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.campaignName, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.campaignName).value(campaignName).notBlank().notExceedingLengthOf(100);

        final Long campaignType = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.campaignType, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.campaignType).value(campaignType).notNull().integerGreaterThanZero();

        final Long triggerType = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.triggerType, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.triggerType).value(triggerType).notNull().integerGreaterThanZero();

        if (triggerType.intValue() == SmsCampaignTriggerType.SCHEDULE.getValue()) {
            if (this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.recurrenceParamName, element)) {
                final String recurrenceParamName = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.recurrenceParamName,
                        element);
                baseDataValidator.reset().parameter(SmsCampaignValidator.recurrenceParamName).value(recurrenceParamName).notBlank();
            }
            if (this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.recurrenceStartDate, element)) {
                final String recurrenceStartDate = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.recurrenceStartDate,
                        element);
                baseDataValidator.reset().parameter(SmsCampaignValidator.recurrenceStartDate).value(recurrenceStartDate).notBlank();
            }
        }

        if(this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.runReportId, element)) {
            final Long runReportId = this.fromApiJsonHelper.extractLongNamed(SmsCampaignValidator.runReportId, element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.runReportId).value(runReportId).notNull().integerGreaterThanZero();
        }

        final String message = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.message, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.message).value(message).notBlank().notExceedingLengthOf(480);

        final JsonElement paramValueJsonObject = this.fromApiJsonHelper.extractJsonObjectNamed(SmsCampaignValidator.paramValue, element);
        if (triggerType.intValue() != SmsCampaignTriggerType.TRIGGERED.getValue()) {
            baseDataValidator.reset().parameter(SmsCampaignValidator.paramValue).value(paramValueJsonObject).notBlank();
            if (paramValueJsonObject != null && paramValueJsonObject.isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : paramValueJsonObject.getAsJsonObject().entrySet()) {
                    final JsonElement inner = entry.getValue();
                    baseDataValidator.reset().parameter(entry.getKey()).value(inner).notBlank();
                }
            }
        }
        if (this.fromApiJsonHelper.parameterExists(SmsCampaignValidator.isNotificationParamName, element)) {
            final Boolean isNotification = this.fromApiJsonHelper.extractBooleanNamed(SmsCampaignValidator.isNotificationParamName,
                    element);
            baseDataValidator.reset().parameter(SmsCampaignValidator.submittedOnDateParamName).trueOrFalseRequired(isNotification);
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validatePreviewMessage(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.PREVIEW_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);
        final JsonElement paramValueJsonObject = this.fromApiJsonHelper.extractJsonObjectNamed(SmsCampaignValidator.paramValue, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.paramValue).value(paramValueJsonObject).notBlank();
        if (!paramValueJsonObject.isJsonNull() && paramValueJsonObject.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : paramValueJsonObject.getAsJsonObject().entrySet()) {
                final JsonElement inner = entry.getValue();
                baseDataValidator.reset().parameter(entry.getKey()).value(inner).notBlank();
            }
        }

        final String message = this.fromApiJsonHelper.extractStringNamed(SmsCampaignValidator.message, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.message).value(message).notBlank().notExceedingLengthOf(480);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateClosedDate(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate closeDate = this.fromApiJsonHelper.extractLocalDateNamed(SmsCampaignValidator.closureDateParamName, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.closureDateParamName).value(closeDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateActivation(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate activationDate = this.fromApiJsonHelper
                .extractLocalDateNamed(SmsCampaignValidator.activationDateParamName, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void ValidateClosure(String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsCampaignValidator.CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate closeDate = this.fromApiJsonHelper.extractLocalDateNamed(SmsCampaignValidator.closureDateParamName, element);
        baseDataValidator.reset().parameter(SmsCampaignValidator.closureDateParamName).value(closeDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
    
	public boolean isValidNotificationOrSms(Client client, SmsCampaign smsCampaign, Object mobileNo) {
		if (smsCampaign.isNotification()) {
			if (client != null) {
				DeviceRegistration deviceRegistration = this.deviceRegistrationRepository
						.findDeviceRegistrationByClientId(client.getId());
				return (deviceRegistration != null);
			}
			return false;
		}
		return (mobileNo != null);
	}
    
}
