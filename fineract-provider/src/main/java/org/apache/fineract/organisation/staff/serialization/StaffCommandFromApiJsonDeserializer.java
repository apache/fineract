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
package org.apache.fineract.organisation.staff.serialization;

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
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class StaffCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("firstname", "lastname", "officeId", "externalId",
            "mobileNo", "isLoanOfficer", "isActive", "joiningDate", "dateFormat", "locale", "forceStatus"));

    private final FromJsonHelper fromApiJsonHelper;
    
    private final StaffReadPlatformService staffReadPlatformService;


    @Autowired
    public StaffCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final StaffReadPlatformService staffReadPlatformService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.staffReadPlatformService = staffReadPlatformService;        
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", element);
        baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();

        final String firstname = this.fromApiJsonHelper.extractStringNamed("firstname", element);
        baseDataValidator.reset().parameter("firstname").value(firstname).notBlank().notExceedingLengthOf(50);

        final String lastname = this.fromApiJsonHelper.extractStringNamed("lastname", element);
        baseDataValidator.reset().parameter("lastname").value(lastname).notBlank().notExceedingLengthOf(50);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists("isLoanOfficer", element)) {
            final Boolean loanOfficerFlag = this.fromApiJsonHelper.extractBooleanNamed("isLoanOfficer", element);
            baseDataValidator.reset().parameter("isLoanOfficer").value(loanOfficerFlag).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("isActive", element)) {
            final Boolean activeFlag = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
            baseDataValidator.reset().parameter("isActive").value(activeFlag).notNull();
        }
        
        final LocalDate joiningDate = this.fromApiJsonHelper.extractLocalDateNamed("joiningDate", element);
        baseDataValidator.reset().parameter("joiningDate").value(joiningDate).notNull();
       
        if (this.fromApiJsonHelper.parameterExists("dateFormat", element)) {
        	final String dateFormat = this.fromApiJsonHelper.extractStringNamed("dateFormat", element);
        	baseDataValidator.reset().parameter("dateFormat").value(dateFormat).notBlank();
        }
        
        if (this.fromApiJsonHelper.parameterExists("locale", element)) {
        	final String locale = this.fromApiJsonHelper.extractStringNamed("locale", element);
        	baseDataValidator.reset().parameter("locale").value(locale).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        validateForUpdate(json, null); 
    }
    
    public void validateForUpdate(final String json,Long staffId) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (this.fromApiJsonHelper.parameterExists("officeId", element)) {
            final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", element);
            baseDataValidator.reset().parameter("officeId").value(officeId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("firstname", element)) {
            final String firstname = this.fromApiJsonHelper.extractStringNamed("firstname", element);
            baseDataValidator.reset().parameter("firstname").value(firstname).notBlank().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists("lastname", element)) {
            final String lastname = this.fromApiJsonHelper.extractStringNamed("lastname", element);
            baseDataValidator.reset().parameter("lastname").value(lastname).notBlank().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists("isLoanOfficer", element)) {
            final Boolean loanOfficerFlag = this.fromApiJsonHelper.extractBooleanNamed("isLoanOfficer", element);
            baseDataValidator.reset().parameter("isLoanOfficer").value(loanOfficerFlag).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("isActive", element)) {
            final Boolean activeFlag = this.fromApiJsonHelper.extractBooleanNamed("isActive", element);
            //Need to add here check to see if any clients, group, account and loans are assigned to this staff if staff is being set to inactive --LJB
            final Boolean forceStatus = this.fromApiJsonHelper.extractBooleanNamed("forceStatus", element);
            if ((!activeFlag && forceStatus == null) || 
                (!activeFlag && forceStatus)) {           
            	 Object[] result = staffReadPlatformService.hasAssociatedItems(staffId);
            	
            	if (result != null && result.length > 0) {
            		baseDataValidator.reset().parameter("isactive").failWithCode("staff.is.assigned",result);
            	}
            	
            }
            baseDataValidator.reset().parameter("isActive").value(activeFlag).notNull();
        }
                
        if (this.fromApiJsonHelper.parameterExists("joiningDate", element)) {
            final LocalDate joiningDate = this.fromApiJsonHelper.extractLocalDateNamed("joiningDate", element);
            baseDataValidator.reset().parameter("joiningDate").value(joiningDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists("dateFormat", element)) {
        	final String dateFormat = this.fromApiJsonHelper.extractStringNamed("dateFormat", element);
        	baseDataValidator.reset().parameter("dateFormat").value(dateFormat).notBlank();
        }
        
        if (this.fromApiJsonHelper.parameterExists("locale", element)) {
        	final String locale = this.fromApiJsonHelper.extractStringNamed("locale", element);
        	baseDataValidator.reset().parameter("locale").value(locale).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}