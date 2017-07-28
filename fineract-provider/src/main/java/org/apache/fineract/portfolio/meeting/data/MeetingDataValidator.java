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
package org.apache.fineract.portfolio.meeting.data;

import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.MEETING_RESOURCE_NAME;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.attendanceTypeParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.calendarIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.clientsAttendanceParamName;
import static org.apache.fineract.portfolio.meeting.MeetingApiConstants.meetingDateParamName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.meeting.MeetingApiConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Component
public class MeetingDataValidator {

	private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> MEETING_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(MeetingApiConstants.meetingDateParamName, MeetingApiConstants.localeParamName,
					MeetingApiConstants.dateFormatParamName, MeetingApiConstants.calendarIdParamName,
					MeetingApiConstants.clientsAttendanceParamName));

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