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
package org.apache.fineract.infrastructure.campaigns.email.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

@Component
public class EmailCampaignValidator {

   

    public static final String RESOURCE_NAME = "email";
    public static final String campaignName  = "campaignName";
    public static final String campaignType = "campaignType";
    public static final String businessRuleId = "businessRuleId";
    public static final String stretchyReportId = "stretchyReportId";
    public static final String stretchyReportParamMap = "stretchyReportParamMap";
    public static final String paramValue = "paramValue";
    public static final String emailSubject = "emailSubject";
    public static final String emailMessage   = "emailMessage";
    public static final String emailAttachmentFileFormatId = "emailAttachmentFileFormatId";
    public static final String activationDateParamName = "activationDate";
    public static final String recurrenceStartDate = "recurrenceStartDate";
    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String closureDateParamName = "closureDate";
    public static final String recurrenceParamName = "recurrence";
    public static final String statusParamName = "status";

    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";


    private final FromJsonHelper fromApiJsonHelper;


    public static final Set<String> supportedParams = new HashSet<String>(Arrays.asList(campaignName, campaignType,localeParamName,dateFormatParamName,
            businessRuleId,paramValue,emailMessage,recurrenceStartDate,activationDateParamName,submittedOnDateParamName,closureDateParamName,recurrenceParamName,
            emailSubject,stretchyReportId,stretchyReportParamMap,emailAttachmentFileFormatId));

    public static final Set<String> supportedParamsForUpdate = new HashSet<>(Arrays.asList(campaignName, campaignType,localeParamName,dateFormatParamName,
            businessRuleId,paramValue,emailMessage,recurrenceStartDate,activationDateParamName,recurrenceParamName));

    public static final Set<String> ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            dateFormatParamName, activationDateParamName));

    public static final Set<String> CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            dateFormatParamName, closureDateParamName));

    public static final Set<String> PREVIEW_REQUEST_DATA_PARAMETERS= new HashSet<String>(Arrays.asList(paramValue,emailMessage));

    @Autowired
    public EmailCampaignValidator(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }


    public void validateCreate(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.supportedParams);
        
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);
        
        final String campaignName =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.campaignName,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.campaignName).value(campaignName).notBlank().notExceedingLengthOf(100);
        
        
        final Long campaignType = this.fromApiJsonHelper.extractLongNamed(EmailCampaignValidator.campaignType,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.campaignType).value(campaignType).notNull().integerGreaterThanZero();

        if(campaignType.intValue() == EmailCampaignType.SCHEDULE.getValue()){
            final String recurrenceParamName =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.recurrenceParamName, element);
            baseDataValidator.reset().parameter(EmailCampaignValidator.recurrenceParamName).value(recurrenceParamName).notBlank();

            final String recurrenceStartDate =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.recurrenceStartDate, element);
            baseDataValidator.reset().parameter(EmailCampaignValidator.recurrenceStartDate).value(recurrenceStartDate).notBlank();
        }

        final Long businessRuleId = this.fromApiJsonHelper.extractLongNamed(EmailCampaignValidator.businessRuleId,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.businessRuleId).value(businessRuleId).notNull().integerGreaterThanZero();

		final String emailSubject = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.emailSubject, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.emailSubject).value(emailSubject).notBlank().notExceedingLengthOf(50);
		
        final String emailMessage = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.emailMessage, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.emailMessage).value(emailMessage).notBlank().notExceedingLengthOf(480);

        final String paramValue = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.paramValue, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.paramValue).value(paramValue).notBlank();



        if (this.fromApiJsonHelper.parameterExists(EmailCampaignValidator.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(EmailCampaignValidator.submittedOnDateParamName,
                    element);
            baseDataValidator.reset().parameter(EmailCampaignValidator.submittedOnDateParamName).value(submittedOnDate).notNull();
        }



        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.supportedParamsForUpdate);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);

        final String campaignName =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.campaignName,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.campaignName).value(campaignName).notBlank().notExceedingLengthOf(100);


        final Long campaignType = this.fromApiJsonHelper.extractLongNamed(EmailCampaignValidator.campaignType,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.campaignType).value(campaignType).notNull().integerGreaterThanZero();

        if(campaignType.intValue() == EmailCampaignType.SCHEDULE.getValue()){
            final String recurrenceParamName =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.recurrenceParamName, element);
            baseDataValidator.reset().parameter(EmailCampaignValidator.recurrenceParamName).value(recurrenceParamName).notBlank();

            final String recurrenceStartDate =  this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.recurrenceStartDate, element);
            baseDataValidator.reset().parameter(EmailCampaignValidator.recurrenceStartDate).value(recurrenceStartDate).notBlank();
        }

        final Long runReportId = this.fromApiJsonHelper.extractLongNamed(EmailCampaignValidator.businessRuleId,element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.businessRuleId).value(runReportId).notNull().integerGreaterThanZero();

        final String message = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.emailMessage, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.emailMessage).value(message).notBlank().notExceedingLengthOf(480);

        final String paramValue = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.paramValue, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.paramValue).value(paramValue).notBlank();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);


    }

    public void validatePreviewMessage(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.PREVIEW_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);

        final String paramValue = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.paramValue, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.paramValue).value(paramValue).notBlank();

        final String message = this.fromApiJsonHelper.extractStringNamed(EmailCampaignValidator.emailMessage, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.emailMessage).value(message).notBlank().notExceedingLengthOf(480);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);


    }

    public void validateClosedDate(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate closeDate = this.fromApiJsonHelper.extractLocalDateNamed(EmailCampaignValidator.closureDateParamName, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.closureDateParamName).value(closeDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    public void validateActivation(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate activationDate = this.fromApiJsonHelper.extractLocalDateNamed(EmailCampaignValidator.activationDateParamName, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void ValidateClosure(String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, EmailCampaignValidator.CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmailCampaignValidator.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final LocalDate closeDate = this.fromApiJsonHelper.extractLocalDateNamed(EmailCampaignValidator.closureDateParamName, element);
        baseDataValidator.reset().parameter(EmailCampaignValidator.closureDateParamName).value(closeDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}
