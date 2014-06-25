/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.data;

import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.MEETING_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.MEETING_RESOURCE_NAME;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.calendarIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.mifosplatform.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class MeetingDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public MeetingDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, MEETING_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(MEETING_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String meetingDateStr = this.fromApiJsonHelper.extractStringNamed(meetingDateParamName, element);
        baseDataValidator.reset().parameter(meetingDateParamName).value(meetingDateStr).notBlank();

        if (!StringUtils.isBlank(meetingDateStr)) {
            final LocalDate meetingDate = this.fromApiJsonHelper.extractLocalDateNamed(meetingDateParamName, element);
            baseDataValidator.reset().parameter(meetingDateParamName).value(meetingDate).notNull();
        }

        final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParamName, element);
        baseDataValidator.reset().parameter(calendarIdParamName).value(calendarId).notNull();

        validateAttendanceDetails(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, MEETING_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(MEETING_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(meetingDateParamName, element)) {
            final String meetingDateStr = this.fromApiJsonHelper.extractStringNamed(meetingDateParamName, element);
            baseDataValidator.reset().parameter(meetingDateParamName).value(meetingDateStr).notBlank();

            if (!StringUtils.isBlank(meetingDateStr)) {
                final LocalDate meetingDate = this.fromApiJsonHelper.extractLocalDateNamed(meetingDateParamName, element);
                baseDataValidator.reset().parameter(meetingDateParamName).value(meetingDate).notNull();
            }
        }

        if (this.fromApiJsonHelper.parameterExists(calendarIdParamName, element)) {
            final Long calendarId = this.fromApiJsonHelper.extractLongNamed(calendarIdParamName, element);
            baseDataValidator.reset().parameter(calendarIdParamName).value(calendarId).notNull();
        }

        validateAttendanceDetails(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdateAttendance(final JsonCommand command) {
        final String json = command.json();
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, MEETING_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource(MEETING_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        validateAttendanceDetails(element, baseDataValidator);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateAttendanceDetails(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has(clientsAttendanceParamName) && topLevelJsonElement.get(clientsAttendanceParamName).isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get(clientsAttendanceParamName).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject attendanceElement = array.get(i).getAsJsonObject();
                    final Long clientId = this.fromApiJsonHelper.extractLongNamed(clientIdParamName, attendanceElement);
                    final Long attendanceType = this.fromApiJsonHelper.extractLongNamed(attendanceTypeParamName, attendanceElement);
                    baseDataValidator.reset().parameter(clientsAttendanceParamName + "[" + i + "]." + clientIdParamName).value(clientId)
                            .notNull().integerGreaterThanZero();
                    baseDataValidator.reset().parameter(clientsAttendanceParamName + "[" + i + "]." + attendanceTypeParamName)
                            .value(attendanceType).notNull().integerGreaterThanZero();
                }
            }
        }
    }
}