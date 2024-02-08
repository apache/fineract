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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class StaffCommandFromApiJsonDeserializer {

    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String OFFICE_ID = "officeId";
    public static final String EXTERNAL_ID = "externalId";
    public static final String MOBILE_NO = "mobileNo";
    public static final String IS_LOAN_OFFICER = "isLoanOfficer";
    public static final String IS_ACTIVE = "isActive";
    public static final String JOINING_DATE = "joiningDate";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String LOCALE = "locale";
    public static final String FORCE_STATUS = "forceStatus";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(Arrays.asList(FIRSTNAME, LASTNAME, OFFICE_ID, EXTERNAL_ID,
            MOBILE_NO, IS_LOAN_OFFICER, IS_ACTIVE, JOINING_DATE, DATE_FORMAT, LOCALE, FORCE_STATUS));

    private final FromJsonHelper fromApiJsonHelper;

    private final StaffReadPlatformService staffReadPlatformService;

    @Autowired
    public StaffCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,
            final StaffReadPlatformService staffReadPlatformService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.staffReadPlatformService = staffReadPlatformService;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
        baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().integerGreaterThanZero();

        final String firstname = this.fromApiJsonHelper.extractStringNamed(FIRSTNAME, element);
        baseDataValidator.reset().parameter(FIRSTNAME).value(firstname).notBlank().notExceedingLengthOf(50);

        final String lastname = this.fromApiJsonHelper.extractStringNamed(LASTNAME, element);
        baseDataValidator.reset().parameter(LASTNAME).value(lastname).notBlank().notExceedingLengthOf(50);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(IS_LOAN_OFFICER, element)) {
            final String loanOfficerFlag = this.fromApiJsonHelper.extractStringNamed(IS_LOAN_OFFICER, element);
            baseDataValidator.reset().parameter(IS_LOAN_OFFICER).trueOrFalseRequired(loanOfficerFlag);
        }

        if (this.fromApiJsonHelper.parameterExists(IS_ACTIVE, element)) {
            final String activeFlag = this.fromApiJsonHelper.extractStringNamed(IS_ACTIVE, element);
            baseDataValidator.reset().parameter(IS_ACTIVE).trueOrFalseRequired(activeFlag);
        }

        final LocalDate joiningDate = this.fromApiJsonHelper.extractLocalDateNamed(JOINING_DATE, element);
        baseDataValidator.reset().parameter(JOINING_DATE).value(joiningDate).notNull();

        if (this.fromApiJsonHelper.parameterExists(DATE_FORMAT, element)) {
            final String dateFormat = this.fromApiJsonHelper.extractStringNamed(DATE_FORMAT, element);
            baseDataValidator.reset().parameter(DATE_FORMAT).value(dateFormat).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(LOCALE, element)) {
            final String locale = this.fromApiJsonHelper.extractStringNamed(LOCALE, element);
            baseDataValidator.reset().parameter(LOCALE).value(locale).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EXTERNAL_ID, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EXTERNAL_ID, element);
            baseDataValidator.reset().parameter(EXTERNAL_ID).value(externalId).notBlank().notExceedingLengthOf(100);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        validateForUpdate(json, null);
    }

    public void validateForUpdate(final String json, Long staffId) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        if (this.fromApiJsonHelper.parameterExists(OFFICE_ID, element)) {
            final Long officeId = this.fromApiJsonHelper.extractLongNamed(OFFICE_ID, element);
            baseDataValidator.reset().parameter(OFFICE_ID).value(officeId).notNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(FIRSTNAME, element)) {
            final String firstname = this.fromApiJsonHelper.extractStringNamed(FIRSTNAME, element);
            baseDataValidator.reset().parameter(FIRSTNAME).value(firstname).notBlank().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(LASTNAME, element)) {
            final String lastname = this.fromApiJsonHelper.extractStringNamed(LASTNAME, element);
            baseDataValidator.reset().parameter(LASTNAME).value(lastname).notBlank().notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.mobileNoParamName, element)) {
            final String mobileNo = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.mobileNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.mobileNoParamName).value(mobileNo).notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(IS_LOAN_OFFICER, element)) {
            final String loanOfficerFlag = this.fromApiJsonHelper.extractStringNamed(IS_LOAN_OFFICER, element);
            baseDataValidator.reset().parameter(IS_LOAN_OFFICER).trueOrFalseRequired(loanOfficerFlag);
        }

        if (this.fromApiJsonHelper.parameterExists(IS_ACTIVE, element)) {
            final String activeFlagStr = this.fromApiJsonHelper.extractStringNamed(IS_ACTIVE, element);
            baseDataValidator.reset().parameter(IS_ACTIVE).trueOrFalseRequired(activeFlagStr);

            final Boolean activeFlag = this.fromApiJsonHelper.extractBooleanNamed(IS_ACTIVE, element);

            // Need to add here check to see if any clients, group, account and
            // loans are assigned to this staff if staff is being set to
            // inactive --LJB
            final Boolean forceStatus = this.fromApiJsonHelper.extractBooleanNamed(FORCE_STATUS, element);
            if ((!activeFlag && forceStatus == null) || (!activeFlag && forceStatus)) {
                Object[] result = staffReadPlatformService.hasAssociatedItems(staffId);

                if (result != null && result.length > 0) {
                    baseDataValidator.reset().parameter("isactive").failWithCode("staff.is.assigned", result);
                }

            }
            baseDataValidator.reset().parameter(IS_ACTIVE).value(activeFlag).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(JOINING_DATE, element)) {
            final LocalDate joiningDate = this.fromApiJsonHelper.extractLocalDateNamed(JOINING_DATE, element);
            baseDataValidator.reset().parameter(JOINING_DATE).value(joiningDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(DATE_FORMAT, element)) {
            final String dateFormat = this.fromApiJsonHelper.extractStringNamed(DATE_FORMAT, element);
            baseDataValidator.reset().parameter(DATE_FORMAT).value(dateFormat).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(LOCALE, element)) {
            final String locale = this.fromApiJsonHelper.extractStringNamed(LOCALE, element);
            baseDataValidator.reset().parameter(LOCALE).value(locale).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EXTERNAL_ID, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EXTERNAL_ID, element);
            baseDataValidator.reset().parameter(EXTERNAL_ID).value(externalId).notBlank().notExceedingLengthOf(100);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
