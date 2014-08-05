/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import org.mifosplatform.organisation.holiday.api.HolidayApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class HolidayDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public HolidayDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                HolidayApiConstants.HOLIDAY_CREATE_OR_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(HolidayApiConstants.HOLIDAY_RESOURCE_NAME);

        final String name = this.fromApiJsonHelper.extractStringNamed(HolidayApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);

        final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.fromDateParamName, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.fromDateParamName).value(fromDate).notNull();

        final LocalDate toDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.toDateParamName, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.toDateParamName).value(toDate).notNull();

        final LocalDate repaymentsRescheduledTo = this.fromApiJsonHelper.extractLocalDateNamed(
                HolidayApiConstants.repaymentsRescheduledToParamName, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.repaymentsRescheduledToParamName).value(repaymentsRescheduledTo).notNull();

        Set<Long> offices = null;
        final JsonObject topLevelJsonElement = element.getAsJsonObject();

        if (topLevelJsonElement.has(HolidayApiConstants.officesParamName)
                && topLevelJsonElement.get(HolidayApiConstants.officesParamName).isJsonArray()) {

            final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.officesParamName).getAsJsonArray();
            if (array.size() > 0) {
                offices = new HashSet<>(array.size());
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject officeElement = array.get(i).getAsJsonObject();
                    final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeIdParamName, officeElement);
                    baseDataValidator.reset().parameter(HolidayApiConstants.officesParamName).value(officeId).notNull();
                    offices.add(officeId);
                }
            }
        }
        baseDataValidator.reset().parameter(HolidayApiConstants.officesParamName).value(offices).notNull();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                HolidayApiConstants.HOLIDAY_CREATE_OR_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(HolidayApiConstants.HOLIDAY_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(HolidayApiConstants.nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(HolidayApiConstants.nameParamName, element);
            baseDataValidator.reset().parameter(HolidayApiConstants.nameParamName).value(name).notNull().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(HolidayApiConstants.fromDateParamName, element)) {
            final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.fromDateParamName, element);
            baseDataValidator.reset().parameter(HolidayApiConstants.fromDateParamName).value(fromDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(HolidayApiConstants.toDateParamName, element)) {
            final LocalDate toDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.toDateParamName, element);
            baseDataValidator.reset().parameter(HolidayApiConstants.toDateParamName).value(toDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(HolidayApiConstants.repaymentsRescheduledToParamName, element)) {
            final LocalDate repaymentsRescheduledTo = this.fromApiJsonHelper.extractLocalDateNamed(
                    HolidayApiConstants.repaymentsRescheduledToParamName, element);
            baseDataValidator.reset().parameter(HolidayApiConstants.repaymentsRescheduledToParamName).value(repaymentsRescheduledTo)
                    .notNull();
        }

        Set<Long> offices = null;
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (this.fromApiJsonHelper.parameterExists(HolidayApiConstants.officesParamName, element)) {
            if (topLevelJsonElement.has(HolidayApiConstants.officesParamName)
                    && topLevelJsonElement.get(HolidayApiConstants.officesParamName).isJsonArray()) {

                final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.officesParamName).getAsJsonArray();
                if (array.size() > 0) {
                    offices = new HashSet<>(array.size());
                    for (int i = 0; i < array.size(); i++) {
                        final JsonObject officeElement = array.get(i).getAsJsonObject();
                        final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeIdParamName, officeElement);
                        baseDataValidator.reset().parameter(HolidayApiConstants.officesParamName).value(officeId).notNull();
                        offices.add(officeId);
                    }
                }
            }
            baseDataValidator.reset().parameter(HolidayApiConstants.officesParamName).value(offices).notNull();
            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
