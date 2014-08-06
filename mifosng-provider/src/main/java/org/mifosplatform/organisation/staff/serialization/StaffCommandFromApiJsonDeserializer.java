/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
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
            "mobileNo", "isLoanOfficer", "isActive", "joiningDate", "dateFormat", "locale"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public StaffCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
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

    public void validateForUpdate(final String json) {
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