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
package org.apache.fineract.portfolio.client.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ClientDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ClientDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientNonPersonDetailsParamName, element)) {
	        final String clientNonPersonJson = this.fromApiJsonHelper.toJson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
	        if(clientNonPersonJson != null)
	        	this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, clientNonPersonJson, ClientApiConstants.CLIENT_NON_PERSON_CREATE_REQUEST_DATA_PARAMETERS);
        }
        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.groupIdParamName, element)) {
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.groupIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.groupIdParamName).value(groupId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.savingsProductIdParamName, element)) {
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.savingsProductIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.savingsProductIdParamName).value(savingsProductId).ignoreIfNull()
                    .longGreaterThanZero();
            /*if (savingsProductId != null && this.fromApiJsonHelper.parameterExists(ClientApiConstants.datatables, element)) {
                final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(ClientApiConstants.datatables, element);
                if (datatables.size() > 0) {
                    baseDataValidator.reset().parameter(ClientApiConstants.savingsProductIdParamName).value(savingsProductId)
                            .failWithCodeNoParameterAddedToErrorCode("should.not.be.used.with.datatables.parameter");
                }
            }*/
        }

        if (isFullnameProvided(element) || isIndividualNameProvided(element)) {

            // 1. No individual name part provided and fullname provided
            if (isFullnameProvided(element) && !isIndividualNameProvided(element)) {
                fullnameCannotBeBlank(element, baseDataValidator);
            }

            // 2. no fullname provided and individual name part provided
            if (isIndividualNameProvided(element) && !isFullnameProvided(element)) {
                validateRequiredIndividualNamePartsExist(element, baseDataValidator);
            }

            // 3. both provided
            if (isFullnameProvided(element) && isIndividualNameProvided(element)) {
                validateIndividualNamePartsCannotBeUsedWithFullname(element, baseDataValidator);
            }
        } else {

            if (isFullnameParameterPassed(element) || isIndividualNamePartParameterPassed(element)) {

                // 1. No individual name parameter passed and fullname passed
                if (isFullnameParameterPassed(element) && !isIndividualNamePartParameterPassed(element)) {
                    fullnameCannotBeBlank(element, baseDataValidator);
                }

                // 2. no fullname passed and individual name part passed
                if (isIndividualNamePartParameterPassed(element) && !isFullnameParameterPassed(element)) {
                    validateRequiredIndividualNamePartsExist(element, baseDataValidator);
                }

                // 3. both parameter types passed
                if (isFullnameParameterPassed(element) && isIndividualNamePartParameterPassed(element)) {
                    baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
                }

            } else {
                baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
                /*if(this.fromApiJsonHelper.parameterExists(ClientApiConstants.datatables,element)){
                    baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active)
                            .failWithCodeNoParameterAddedToErrorCode("should.not.be.used.with.datatables.parameter");
                }*/
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.submittedOnDateParamName, element)) {
            final LocalDate submittedOnDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.submittedOnDateParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.submittedOnDateParamName).value(submittedOnDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateOfBirthParamName, element)) {
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.dateOfBirthParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateOfBirthParamName).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.genderIdParamName, element)) {
            final Integer genderId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.genderIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.genderIdParamName).value(genderId).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientTypeIdParamName, element)) {
            final Integer clientType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.clientTypeIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientTypeIdParamName).value(clientType).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientClassificationIdParamName, element)) {
            final Integer clientClassification = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    ClientApiConstants.clientClassificationIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientClassificationIdParamName).value(clientClassification)
                    .integerGreaterThanZero();
        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.legalFormIdParamName, element)) {
        	final Integer legalFormId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.legalFormIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.legalFormIdParamName).value(legalFormId).ignoreIfNull().inMinMaxRange(1, 2);
        }

        if(this.fromApiJsonHelper.parameterExists(ClientApiConstants.datatables, element)){
            final JsonArray datatables = this.fromApiJsonHelper.extractJsonArrayNamed(ClientApiConstants.datatables, element);
            baseDataValidator.reset().parameter(ClientApiConstants.datatables).value(datatables).notNull().jsonArrayNotEmpty();
        }

		if (this.fromApiJsonHelper.parameterExists("isStaff", element)) {
            final Boolean isStaffFlag = this.fromApiJsonHelper.extractBooleanNamed("isStaff", element);
            baseDataValidator.reset().parameter("isStaff").value(isStaffFlag).notNull();
        }
		
        List<ApiParameterError> dataValidationErrorsForClientNonPerson = getDataValidationErrorsForCreateOnClientNonPerson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
        dataValidationErrors.addAll(dataValidationErrorsForClientNonPerson);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    List<ApiParameterError> getDataValidationErrorsForCreateOnClientNonPerson(JsonElement element)
    {
    	
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpNumberParamName, element)) {
            final String incorpNumber = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpNumberParamName).value(incorpNumber).ignoreIfNull()
            		.notExceedingLengthOf(50);
        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.remarksParamName, element)) {
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.remarksParamName).value(remarks).ignoreIfNull()
                    .notExceedingLengthOf(150);
        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpValidityTillParamName, element)) {
            final LocalDate incorpValidityTill = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpValidityTillParamName).value(incorpValidityTill).ignoreIfNull();
        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.constitutionIdParamName, element)) {
            final Integer constitution = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.constitutionIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.constitutionIdParamName).value(constitution).ignoreIfNull().integerGreaterThanZero();
        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mainBusinessLineIdParamName, element)) {
            final Integer mainBusinessLine = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.mainBusinessLineIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.mainBusinessLineIdParamName).value(mainBusinessLine).integerGreaterThanZero();
        }

		return dataValidationErrors;
    }

    private void validateIndividualNamePartsCannotBeUsedWithFullname(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final String firstnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        if (StringUtils.isNotBlank(firstnameParam)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.firstnameParamName, firstnameParam);
        }

        final String middlenameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        if (StringUtils.isNotBlank(middlenameParam)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.middlenameParamName, middlenameParam);
        }

        final String lastnameParamName = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);
        if (StringUtils.isNotBlank(lastnameParamName)) {
            final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam)
                    .mustBeBlankWhenParameterProvided(ClientApiConstants.lastnameParamName, lastnameParamName);
        }
    }

    private void validateRequiredIndividualNamePartsExist(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final String firstnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.firstnameParamName).value(firstnameParam).notBlank()
                .notExceedingLengthOf(50);

        final String middlenameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.middlenameParamName).value(middlenameParam).ignoreIfNull()
                .notExceedingLengthOf(50);

        final String lastnameParamName = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.lastnameParamName).value(lastnameParamName).notBlank()
                .notExceedingLengthOf(50);
    }

    private void fullnameCannotBeBlank(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final String fullnameParam = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.fullnameParamName).value(fullnameParam).notBlank().notExceedingLengthOf(100);
    }

    private boolean isIndividualNamePartParameterPassed(final JsonElement element) {
        return this.fromApiJsonHelper.parameterExists(ClientApiConstants.firstnameParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.middlenameParamName, element)
                || this.fromApiJsonHelper.parameterExists(ClientApiConstants.lastnameParamName, element);
    }

    private boolean isFullnameParameterPassed(final JsonElement element) {
        return this.fromApiJsonHelper.parameterExists(ClientApiConstants.fullnameParamName, element);
    }

    private boolean isIndividualNameProvided(final JsonElement element) {
        final String firstname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.firstnameParamName, element);
        final String middlename = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.middlenameParamName, element);
        final String lastname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.lastnameParamName, element);

        return StringUtils.isNotBlank(firstname) || StringUtils.isNotBlank(middlename) || StringUtils.isNotBlank(lastname);
    }

    private boolean isFullnameProvided(final JsonElement element) {
        final String fullname = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.fullnameParamName, element);
        return StringUtils.isNotBlank(fullname);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientNonPersonDetailsParamName, element)) {
	        final String clientNonPersonJson = this.fromApiJsonHelper.toJson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
	        if(clientNonPersonJson != null)
	        	this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, clientNonPersonJson, ClientApiConstants.CLIENT_NON_PERSON_UPDATE_REQUEST_DATA_PARAMETERS);
        }
	        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (isFullnameProvided(element) || isIndividualNameProvided(element)) {

            // 1. No individual name part provided and fullname provided
            if (isFullnameProvided(element) && !isIndividualNameProvided(element)) {
                fullnameCannotBeBlank(element, baseDataValidator);
            }

            // 2. no fullname provided and individual name part provided
            if (isIndividualNameProvided(element) && !isFullnameProvided(element)) {
                validateRequiredIndividualNamePartsExist(element, baseDataValidator);
            }

            // 3. both provided
            if (isFullnameProvided(element) && isIndividualNameProvided(element)) {
                validateIndividualNamePartsCannotBeUsedWithFullname(element, baseDataValidator);
            }
        } else {

            if (isFullnameParameterPassed(element) || isIndividualNamePartParameterPassed(element)) {

                // 1. No individual name parameter passed and fullname passed
                if (isFullnameParameterPassed(element) && !isIndividualNamePartParameterPassed(element)) {
                    fullnameCannotBeBlank(element, baseDataValidator);
                }

                // 2. no fullname passed and individual name part passed
                if (isIndividualNamePartParameterPassed(element) && !isFullnameParameterPassed(element)) {
                    validateRequiredIndividualNamePartsExist(element, baseDataValidator);
                }

                // 3. both parameter types passed
                if (isFullnameParameterPassed(element) && isIndividualNamePartParameterPassed(element)) {
                    baseDataValidator.reset().parameter(ClientApiConstants.idParamName).failWithCode(".no.name.details.passed");
                }

            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.fullnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.lastnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.middlenameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.firstnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).notExceedingLengthOf(50);
        }

        final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            atLeastOneParameterPassedForUpdate = true;
            if (active.booleanValue()) {
                final LocalDate joinedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName,
                        element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.staffIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).ignoreIfNull().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.savingsProductIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.savingsProductIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.savingsProductIdParamName).value(savingsProductId).ignoreIfNull()
                    .longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.dateOfBirthParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate dateOfBirth = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.dateOfBirthParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.dateOfBirthParamName).value(dateOfBirth).notNull()
                    .validateDateBefore(DateUtils.getLocalDateOfTenant());
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.genderIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer genderId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.genderIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.genderIdParamName).value(genderId).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientTypeIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer clientType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.clientTypeIdParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientTypeIdParamName).value(clientType).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.clientClassificationIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer clientClassification = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    ClientApiConstants.clientClassificationIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.clientClassificationIdParamName).value(clientClassification)
                    .integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.submittedOnDateParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate submittedDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.submittedOnDateParamName,
                    element);
            baseDataValidator.reset().parameter(ClientApiConstants.submittedOnDateParamName).value(submittedDate).notNull();

        }
        
        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.legalFormIdParamName, element)) {
        	atLeastOneParameterPassedForUpdate = true;
        	final Integer legalFormId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.legalFormIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.legalFormIdParamName).value(legalFormId).ignoreIfNull().inMinMaxRange(1, 2);
        }

		if (this.fromApiJsonHelper.parameterExists("isStaff", element)) {
            final Boolean isStaffFlag = this.fromApiJsonHelper.extractBooleanNamed("isStaff", element);
            baseDataValidator.reset().parameter("isStaff").value(isStaffFlag).notNull();
        }

        Map<String, Object> parameterUpdateStatusDetails = getParameterUpdateStatusAndDataValidationErrorsForUpdateOnClientNonPerson(element.getAsJsonObject().get(ClientApiConstants.clientNonPersonDetailsParamName));
        boolean atLeastOneParameterPassedForClientNonPersonUpdate = (boolean) parameterUpdateStatusDetails.get("parameterUpdateStatus");
               
        if (!atLeastOneParameterPassedForUpdate && !atLeastOneParameterPassedForClientNonPersonUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }
        
        @SuppressWarnings("unchecked")
		List<ApiParameterError> dataValidationErrorsForClientNonPerson = (List<ApiParameterError>) parameterUpdateStatusDetails.get("dataValidationErrors");        
        dataValidationErrors.addAll(dataValidationErrorsForClientNonPerson);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
    
    Map<String, Object> getParameterUpdateStatusAndDataValidationErrorsForUpdateOnClientNonPerson(JsonElement element)
    {
    	boolean atLeastOneParameterPassedForUpdate = false;
    	final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);
    	
    	if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpNumberParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String incorpNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpNumberParamName).value(incorpNo).ignoreIfNull().notExceedingLengthOf(50);
        }
    	
    	if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.remarksParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.remarksParamName).value(remarks).notExceedingLengthOf(150);
        }
    	
    	if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.incorpValidityTillParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final LocalDate incorpValidityTill = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.incorpValidityTillParamName).value(incorpValidityTill);
        }
    	
    	if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.constitutionIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer constitutionId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.constitutionIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.constitutionIdParamName).value(constitutionId).ignoreIfNull().integerGreaterThanZero();
        }
    	
    	if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mainBusinessLineIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Integer mainBusinessLineId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(ClientApiConstants.mainBusinessLineIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mainBusinessLineIdParamName).value(mainBusinessLineId).integerGreaterThanZero();
        }
    	
    	Map<String, Object> parameterUpdateStatusDetails = new HashMap<>();
    	parameterUpdateStatusDetails.put("parameterUpdateStatus", atLeastOneParameterPassedForUpdate);
    	parameterUpdateStatusDetails.put("dataValidationErrors", dataValidationErrors);
    	
		return parameterUpdateStatusDetails;
    	
    }

    public void validateActivation(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForUnassignStaff(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<>(Arrays.asList(ClientApiConstants.staffIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final String staffIdParameterName = ClientApiConstants.staffIdParamName;
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }

    public void validateForAssignStaff(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParametersUnassignStaff = new HashSet<>(Arrays.asList(ClientApiConstants.staffIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersUnassignStaff);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final String staffIdParameterName = ClientApiConstants.staffIdParamName;
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(staffIdParameterName, element);
        baseDataValidator.reset().parameter(staffIdParameterName).value(staffId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }

    public void validateClose(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_CLOSE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate closureDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.closureDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.closureDateParamName).value(closureDate).notNull();

        final Long closureReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.closureReasonIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.closureReasonIdParamName).value(closureReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForSavingsAccount(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Set<String> supportedParameters = new HashSet<>(Arrays.asList(ClientApiConstants.savingsAccountIdParamName));

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final String savingsIdParameterName = ClientApiConstants.savingsAccountIdParamName;
        final Long savingsId = this.fromApiJsonHelper.extractLongNamed(savingsIdParameterName, element);
        baseDataValidator.reset().parameter(savingsIdParameterName).value(savingsId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }

    }

    public void validateRejection(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_REJECT_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate rejectionDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.rejectionDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.rejectionDateParamName).value(rejectionDate).notNull();

        final Long rejectionReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.rejectionReasonIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.rejectionReasonIdParamName).value(rejectionReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateWithdrawn(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_WITHDRAW_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate withdrawalDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.withdrawalDateParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.withdrawalDateParamName).value(withdrawalDate).notNull();

        final Long withdrawalReasonId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.withdrawalReasonIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.withdrawalReasonIdParamName).value(withdrawalReasonId).notNull()
                .longGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateReactivate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.REACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate reactivationDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reactivationDateParamName,
                element);
        baseDataValidator.reset().parameter(ClientApiConstants.reactivationDateParamName).value(reactivationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }
    
    
    public void validateUndoRejection(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.UNDOREJECTION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate undoRejectionDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reopenedDateParamName,
                element);
		baseDataValidator.reset().parameter(ClientApiConstants.reopenedDateParamName).value(undoRejectionDate).notNull()
				.validateDateBeforeOrEqual(DateUtils.getLocalDateOfTenant());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }
    public void validateUndoWithDrawn(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.UNDOWITHDRAWN_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate undoWithdrawnDate = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.reopenedDateParamName,
                element);
		baseDataValidator.reset().parameter(ClientApiConstants.reopenedDateParamName).value(undoWithdrawnDate).notNull()
				.validateDateBeforeOrEqual(DateUtils.getLocalDateOfTenant());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }
    
}