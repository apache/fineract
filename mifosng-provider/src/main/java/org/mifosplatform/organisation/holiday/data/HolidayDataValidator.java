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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, HolidayApiConstants.HOLIDAY_CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(HolidayApiConstants.HOLIDAY_RESOURCE_NAME);

        final String name = this.fromApiJsonHelper.extractStringNamed(HolidayApiConstants.name, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.name).value(name).notNull().notExceedingLengthOf(100);

        final LocalDate fromDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.fromDate, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.fromDate).value(fromDate).notNull();

        final LocalDate toDate = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.toDate, element);
        baseDataValidator.reset().parameter(HolidayApiConstants.toDate).value(toDate).notNull();

        final LocalDate repaymentsRescheduledTo = this.fromApiJsonHelper.extractLocalDateNamed(HolidayApiConstants.repaymentsRescheduledTo,
                element);
        baseDataValidator.reset().parameter(HolidayApiConstants.repaymentsRescheduledTo).value(repaymentsRescheduledTo).notNull();

        Set<Long> offices = null;
        final JsonObject topLevelJsonElement = element.getAsJsonObject();

        if (topLevelJsonElement.has(HolidayApiConstants.offices) && topLevelJsonElement.get(HolidayApiConstants.offices).isJsonArray()) {

            final JsonArray array = topLevelJsonElement.get(HolidayApiConstants.offices).getAsJsonArray();
            if (array.size() > 0) {
                offices = new HashSet<Long>(array.size());
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject officeElement = array.get(i).getAsJsonObject();
                    final Long officeId = this.fromApiJsonHelper.extractLongNamed(HolidayApiConstants.officeId, officeElement);
                    baseDataValidator.reset().parameter(HolidayApiConstants.offices).value(officeId).notNull();
                    offices.add(officeId);
                }
            }
        }
        baseDataValidator.reset().parameter(HolidayApiConstants.offices).value(offices).notNull();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
