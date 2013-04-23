/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.calendar.CalendarConstants.CALENDAR_SUPPORTED_PARAMETERS;
import org.mifosplatform.portfolio.calendar.command.CalendarCommand;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class CalendarCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CalendarCommand> {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = CALENDAR_SUPPORTED_PARAMETERS.getAllValues();

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CalendarCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CalendarCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String title = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), element);
        final String description = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(), element);
        final String location = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), element);
        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(),
                element);
        final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(), element);
        final LocalDate createdDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.CREATED_DATE.getValue(),
                element);
        final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue(),
                element);
        final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue(),
                element);
        final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue(), element);
        final String recurrence = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.RECURRENCE.getValue(), element);
        final Integer remindById = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue(), element);
        final Integer firstReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue(), element);
        final Integer secondReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue(), element);

        return new CalendarCommand(title, description, location, startDate, endDate, createdDate, duration, typeId, repeating, recurrence,
                remindById, firstReminder, secondReminder);
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        final String title = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), element);
        baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue()).value(title).notBlank()
                .notExceedingLengthOf(50);

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), element)) {
            final String location = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue()).value(location).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        final String startDateStr = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(), element);
        baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue()).value(startDateStr).notBlank();

        if(!StringUtils.isBlank(startDateStr)){
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue()).value(startDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(), element)) {
            final String endDateStr = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue()).value(endDateStr).notBlank();
            
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue()).value(endDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue(), element)) {
            final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue()).value(duration).ignoreIfNull();
        }

        final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue(),
                element);
        baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue()).value(typeId).notNull()
                .inMinMaxRange(CalendarEntityType.getMinValue(), CalendarEntityType.getMaxValue());

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue(), element)) {
            // FIXME - Throws NullPointerException when boolean value is null
            final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue()).value(repeating).notNull();
            
            if(repeating){
	            final String recurrence = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.RECURRENCE.getValue(), element);
	            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.RECURRENCE.getValue()).value(recurrence).notNull()
	                    .notExceedingLengthOf(100).isValidRecurringRule(recurrence);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue(), element)) {
            final Integer remindById = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue()).value(remindById).ignoreIfNull()
                    .inMinMaxRange(CalendarRemindBy.getMinValue(), CalendarRemindBy.getMaxValue());
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue(), element)) {
            final Integer firstReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue()).value(firstReminder).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue(), element)) {
            final Integer secondReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue()).value(secondReminder).ignoreIfNull().integerGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("calendar");

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), element)) {
            final String title = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.TITLE.getValue()).value(title).notBlank()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), element)) {
            final String location = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.LOCATION.getValue()).value(location).ignoreIfNull()
                    .notExceedingLengthOf(50);
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(), element)) {
            final String startDateStr = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue()).value(startDateStr).notNull();

            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.START_DATE.getValue()).value(startDate).notNull();
        }
        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(), element)) {
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.END_DATE.getValue()).value(endDate).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue(), element)) {
            final Integer duration = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.DURATION.getValue()).value(duration).ignoreIfNull();
        }
        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue(), element)) {
            final Integer typeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.TYPE_ID.getValue()).value(typeId).notNull()
                    .inMinMaxRange(CalendarEntityType.getMinValue(), CalendarEntityType.getMaxValue());
        }
        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue(), element)) {
            // FIXME - Throws NullPointerException when boolean value is null
            final boolean repeating = this.fromApiJsonHelper.extractBooleanNamed(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue(),
                    element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REPEATING.getValue()).value(repeating).notNull();
            
            if(repeating){
	            final String recurrence = this.fromApiJsonHelper.extractStringNamed(CALENDAR_SUPPORTED_PARAMETERS.RECURRENCE.getValue(), element);
	            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.RECURRENCE.getValue()).value(recurrence).notNull()
	                    .notExceedingLengthOf(100).isValidRecurringRule(recurrence);
            }
        }
        
        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue(), element)) {
            final Integer remindById = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.REMIND_BY_ID.getValue()).value(remindById).ignoreIfNull()
                    .inMinMaxRange(CalendarRemindBy.getMinValue(), CalendarRemindBy.getMaxValue());
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue(), element)) {
            final Integer firstReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.FIRST_REMINDER.getValue()).value(firstReminder).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue(), element)) {
            final Integer secondReminder = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue(), element);
            baseDataValidator.reset().parameter(CALENDAR_SUPPORTED_PARAMETERS.SECOND_REMINDER.getValue()).value(secondReminder).ignoreIfNull();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
