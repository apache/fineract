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
package org.apache.fineract.portfolio.calendar.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.calendar.CalendarConstants.CalendarSupportedParameters;
import org.apache.fineract.portfolio.calendar.command.CalendarCommand;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarRemindBy;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalendarCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CalendarCommand> {

    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = CalendarSupportedParameters.getAllValues();

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalendarCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CalendarCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String title = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.TITLE.getValue(), element);
        final String description = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.DESCRIPTION.getValue(), element);
        final String location = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.LOCATION.getValue(), element);
        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.START_DATE.getValue(),
                element);
        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.END_DATE.getValue(), element);
        final LocalDate createdDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.CREATED_DATE.getValue(),
                element);
        final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.DURATION.getValue(),
                element);
        final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.TYPE_ID.getValue(),
                element);
        final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CalendarSupportedParameters.REPEATING.getValue(), element);
        final Integer remindById = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.REMIND_BY_ID.getValue(),
                element);
        final Integer firstReminder = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(CalendarSupportedParameters.FIRST_REMINDER.getValue(), element);
        final Integer secondReminder = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(CalendarSupportedParameters.SECOND_REMINDER.getValue(), element);

        return new CalendarCommand(title, description, location, startDate, endDate, createdDate, duration, typeId, repeating, remindById,
                firstReminder, secondReminder);
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        final String title = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.TITLE.getValue(), element);
        baseDataValidator.reset().parameter(CalendarSupportedParameters.TITLE.getValue()).value(title).notBlank().notExceedingLengthOf(50);

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.LOCATION.getValue(), element)) {
            final String location = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.LOCATION.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.LOCATION.getValue()).value(location).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        final String startDateStr = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.START_DATE.getValue(), element);
        baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(startDateStr).notBlank();

        if (!StringUtils.isBlank(startDateStr)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.START_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(startDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.END_DATE.getValue(), element)) {
            final String endDateStr = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.END_DATE.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.END_DATE.getValue()).value(endDateStr).notBlank();

            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.END_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.END_DATE.getValue()).value(endDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.DURATION.getValue(), element)) {
            final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.DURATION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.DURATION.getValue()).value(duration).ignoreIfNull();
        }

        final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.TYPE_ID.getValue(),
                element);
        baseDataValidator.reset().parameter(CalendarSupportedParameters.TYPE_ID.getValue()).value(typeId).notNull()
                .inMinMaxRange(CalendarEntityType.getMinValue(), CalendarEntityType.getMaxValue());

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.REPEATING.getValue(), element)) {
            // FIXME - Throws NullPointerException when boolean value is null
            final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CalendarSupportedParameters.REPEATING.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATING.getValue()).value(repeating).notNull();

            if (repeating) {
                final Integer frequency = this.fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(CalendarSupportedParameters.FREQUENCY.getValue(), element);
                baseDataValidator.reset().parameter(CalendarSupportedParameters.FREQUENCY.getValue()).value(frequency).notBlank()
                        .inMinMaxRange(CalendarFrequencyType.getMinValue(), CalendarFrequencyType.getMaxValue());

                if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.INTERVAL.getValue(), element)) {
                    final Integer interval = this.fromApiJsonHelper
                            .extractIntegerSansLocaleNamed(CalendarSupportedParameters.INTERVAL.getValue(), element);
                    baseDataValidator.reset().parameter(CalendarSupportedParameters.INTERVAL.getValue()).value(interval).notNull()
                            .integerGreaterThanZero();
                }
                if (CalendarFrequencyType.fromInt(frequency).isWeekly()) {
                    final Integer repeatsOnDay = this.fromApiJsonHelper
                            .extractIntegerSansLocaleNamed(CalendarSupportedParameters.REPEATS_ON_DAY.getValue(), element);
                    baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATS_ON_DAY.getValue()).value(repeatsOnDay)
                            .notBlank().inMinMaxRange(CalendarWeekDaysType.getMinValue(), CalendarWeekDaysType.getMaxValue());
                } else if (CalendarFrequencyType.fromInt(frequency).isMonthly()) {
                    CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                            CalendarSupportedParameters.REPEATS_ON_NTH_DAY_OF_MONTH.getValue(),
                            CalendarSupportedParameters.REPEATS_ON_LAST_WEEKDAY_OF_MONTH.getValue(), element, this.fromApiJsonHelper);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.REMIND_BY_ID.getValue(), element)) {
            final Integer remindById = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.REMIND_BY_ID.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REMIND_BY_ID.getValue()).value(remindById).ignoreIfNull()
                    .inMinMaxRange(CalendarRemindBy.getMinValue(), CalendarRemindBy.getMaxValue());
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.FIRST_REMINDER.getValue(), element)) {
            final Integer firstReminder = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.FIRST_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.FIRST_REMINDER.getValue()).value(firstReminder).ignoreIfNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.SECOND_REMINDER.getValue(), element)) {
            final Integer secondReminder = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.SECOND_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.SECOND_REMINDER.getValue()).value(secondReminder).ignoreIfNull()
                    .integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.MEETING_TIME.getValue(), element)) {
            final LocalTime meetingTime = this.fromApiJsonHelper.extractLocalTimeNamed(CalendarSupportedParameters.MEETING_TIME.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.MEETING_TIME.getValue()).value(meetingTime).ignoreIfNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.RESCHEDULE_BASED_ON_MEETING_DATES.getValue(), element)) {
            final Boolean rescheduleBasedOnMeetingDates = this.fromApiJsonHelper
                    .extractBooleanNamed(CalendarSupportedParameters.RESCHEDULE_BASED_ON_MEETING_DATES.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.RESCHEDULE_BASED_ON_MEETING_DATES.getValue())
                    .value(rescheduleBasedOnMeetingDates).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.PRESENT_MEETING_DATE.getValue(), element)) {
            final String presentMeetingDate = this.fromApiJsonHelper
                    .extractStringNamed(CalendarSupportedParameters.PRESENT_MEETING_DATE.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.PRESENT_MEETING_DATE.getValue()).value(presentMeetingDate)
                    .notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.NEW_MEETING_DATE.getValue(), element)) {
            final String newMeetingDate = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.NEW_MEETING_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.NEW_MEETING_DATE.getValue()).value(newMeetingDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.TITLE.getValue(), element)) {
            final String title = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.TITLE.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.TITLE.getValue()).value(title).notBlank()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.LOCATION.getValue(), element)) {
            final String location = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.LOCATION.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.LOCATION.getValue()).value(location).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.START_DATE.getValue(), element)) {
            final String startDateStr = this.fromApiJsonHelper.extractStringNamed(CalendarSupportedParameters.START_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(startDateStr).notNull();

            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.START_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.START_DATE.getValue()).value(startDate).notNull();
        }
        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.END_DATE.getValue(), element)) {
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CalendarSupportedParameters.END_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.END_DATE.getValue()).value(endDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.DURATION.getValue(), element)) {
            final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.DURATION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.DURATION.getValue()).value(duration).ignoreIfNull();
        }
        // TODO: AA do not allow to change calendar type.
        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.TYPE_ID.getValue(), element)) {
            final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CalendarSupportedParameters.TYPE_ID.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.TYPE_ID.getValue()).value(typeId).notNull()
                    .inMinMaxRange(CalendarEntityType.getMinValue(), CalendarEntityType.getMaxValue());
        }
        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.REPEATING.getValue(), element)) {
            // FIXME - Throws NullPointerException when boolean value is null
            final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CalendarSupportedParameters.REPEATING.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATING.getValue()).value(repeating).notNull();

            if (repeating) {
                final Integer frequency = this.fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(CalendarSupportedParameters.FREQUENCY.getValue(), element);
                baseDataValidator.reset().parameter(CalendarSupportedParameters.FREQUENCY.getValue()).value(frequency).notBlank()
                        .inMinMaxRange(CalendarFrequencyType.getMinValue(), CalendarFrequencyType.getMaxValue());

                if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.INTERVAL.getValue(), element)) {
                    final Integer interval = this.fromApiJsonHelper
                            .extractIntegerSansLocaleNamed(CalendarSupportedParameters.INTERVAL.getValue(), element);
                    baseDataValidator.reset().parameter(CalendarSupportedParameters.INTERVAL.getValue()).value(interval).notNull()
                            .integerGreaterThanZero();
                }

                if (CalendarFrequencyType.fromInt(frequency).isWeekly()) {
                    if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.REPEATS_ON_DAY.getValue(), element)) {
                        final Integer repeatsOnDay = this.fromApiJsonHelper
                                .extractIntegerSansLocaleNamed(CalendarSupportedParameters.REPEATS_ON_DAY.getValue(), element);
                        baseDataValidator.reset().parameter(CalendarSupportedParameters.REPEATS_ON_DAY.getValue()).value(repeatsOnDay)
                                .notBlank().inMinMaxRange(CalendarWeekDaysType.getMinValue(), CalendarWeekDaysType.getMaxValue());
                    }
                } else if (CalendarFrequencyType.fromInt(frequency).isMonthly()) {
                    CalendarUtils.validateNthDayOfMonthFrequency(baseDataValidator,
                            CalendarSupportedParameters.REPEATS_ON_NTH_DAY_OF_MONTH.getValue(),
                            CalendarSupportedParameters.REPEATS_ON_LAST_WEEKDAY_OF_MONTH.getValue(), element, this.fromApiJsonHelper);
                }
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.REMIND_BY_ID.getValue(), element)) {
            final Integer remindById = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.REMIND_BY_ID.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.REMIND_BY_ID.getValue()).value(remindById).ignoreIfNull()
                    .inMinMaxRange(CalendarRemindBy.getMinValue(), CalendarRemindBy.getMaxValue());
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.FIRST_REMINDER.getValue(), element)) {
            final Integer firstReminder = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.FIRST_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.FIRST_REMINDER.getValue()).value(firstReminder).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.SECOND_REMINDER.getValue(), element)) {
            final Integer secondReminder = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(CalendarSupportedParameters.SECOND_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.SECOND_REMINDER.getValue()).value(secondReminder)
                    .ignoreIfNull();
        }
        if (this.fromApiJsonHelper.parameterExists(CalendarSupportedParameters.MEETING_TIME.getValue(), element)) {
            final LocalTime meetingTime = this.fromApiJsonHelper.extractLocalTimeNamed(CalendarSupportedParameters.MEETING_TIME.getValue(),
                    element);
            baseDataValidator.reset().parameter(CalendarSupportedParameters.MEETING_TIME.getValue()).value(meetingTime).ignoreIfNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

}
