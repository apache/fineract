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
package org.apache.fineract.infrastructure.core.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.format.number.NumberStyleFormatter;

/**
 * Helper class to extract values of json named attributes.
 */
public class JsonParserHelper {

    public boolean parameterExists(final String parameterName, final JsonElement element) {
        if (element == null) {
            return false;
        }
        return element.getAsJsonObject().has(parameterName);
    }

    /**
     * Check Parameter has a non-blank value
     */
    public boolean parameterHasValue(final String parameterName, final JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return false;
        }

        var valueObject = element.getAsJsonObject().get(parameterName);
        if (valueObject == null || valueObject.isJsonNull()) {
            return false;
        }
        if (valueObject instanceof JsonArray) {
            return !valueObject.getAsJsonArray().isEmpty();
        }
        if (valueObject instanceof JsonObject) {
            return !valueObject.getAsJsonObject().isEmpty();
        }
        return valueObject.isJsonPrimitive() && !valueObject.getAsJsonPrimitive().getAsString().isBlank();
    }

    public Boolean extractBooleanNamed(final String parameterName, final JsonElement element, final Set<String> requestParamatersDetected) {
        Boolean value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                requestParamatersDetected.add(parameterName);

                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                value = primitive.getAsBoolean();
            }
        }
        return value;
    }

    public Long extractLongNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        Long longValue = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInRequest.add(parameterName);
                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String stringValue = primitive.getAsString();
                if (StringUtils.isNotBlank(stringValue)) {
                    longValue = Long.valueOf(stringValue);
                }
            }
        }
        return longValue;
    }

    public String extractStringNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        String stringValue = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInRequest.add(parameterName);
                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    stringValue = valueAsString;
                }
            }
        }
        return stringValue;
    }

    public BigDecimal extractBigDecimalWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> modifiedParameters) {
        BigDecimal value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final Locale locale = extractLocaleValue(object);
            value = extractBigDecimalNamed(parameterName, object, locale, modifiedParameters);
        }
        return value;
    }

    public BigDecimal extractBigDecimalNamed(final String parameterName, final JsonObject element, final Locale locale,
            final Set<String> modifiedParameters) {
        BigDecimal value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                modifiedParameters.add(parameterName);
                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                if (!primitive.isJsonNull()) {
                    if (primitive.isNumber()) {
                        value = primitive.getAsBigDecimal();
                    } else {
                        final String valueAsString = primitive.getAsString();
                        if (StringUtils.isNotBlank(valueAsString)) {
                            value = convertFrom(valueAsString, parameterName, locale);
                        }
                    }
                }
            }
        }
        return value;
    }

    public Integer extractIntegerWithLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> modifiedParameters) {
        Integer value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final Locale locale = extractLocaleValue(object);
            value = extractIntegerNamed(parameterName, object, locale, modifiedParameters);
        }
        return value;
    }

    public Integer extractIntegerNamed(final String parameterName, final JsonElement element, final Locale locale,
            final Set<String> modifiedParameters) {
        Integer value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                modifiedParameters.add(parameterName);
                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    value = convertToInteger(valueAsString, parameterName, locale);
                }
            }
        }
        return value;
    }

    /**
     * Method used to extract integers from unformatted strings. Ex: "1" , "100002" etc
     *
     * Please note that this method does not support extracting Integers from locale specific formatted strings Ex
     * "1,000" etc
     *
     * @param parameterName
     * @param element
     * @param parametersPassedInRequest
     * @return
     */
    public Integer extractIntegerSansLocaleNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInRequest) {
        Integer intValue = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInRequest.add(parameterName);
                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String stringValue = primitive.getAsString();
                if (StringUtils.isNotBlank(stringValue)) {
                    intValue = convertToIntegerSanLocale(stringValue, parameterName);
                }
            }
        }
        return intValue;
    }

    public String extractDateFormatParameter(final JsonObject element) {
        String value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final String dateFormatParameter = "dateFormat";
            if (object.has(dateFormatParameter) && object.get(dateFormatParameter).isJsonPrimitive()) {
                final JsonPrimitive primitive = object.get(dateFormatParameter).getAsJsonPrimitive();
                value = primitive.getAsString();
            }
        }
        return value;
    }

    public String extractTimeFormatParameter(final JsonObject element) {
        String value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final String timeFormatParameter = "timeFormat";
            if (object.has(timeFormatParameter) && object.get(timeFormatParameter).isJsonPrimitive()) {
                final JsonPrimitive primitive = object.get(timeFormatParameter).getAsJsonPrimitive();
                value = primitive.getAsString();
            }
        }
        return value;
    }

    public String extractMonthDayFormatParameter(final JsonObject element) {
        String value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final String monthDayFormatParameter = "monthDayFormat";
            if (object.has(monthDayFormatParameter) && object.get(monthDayFormatParameter).isJsonPrimitive()) {
                final JsonPrimitive primitive = object.get(monthDayFormatParameter).getAsJsonPrimitive();
                value = primitive.getAsString();
            }
        }
        return value;
    }

    public Locale extractLocaleParameter(final JsonObject element) {
        Locale clientApplicationLocale = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            String locale = null;
            final String localeParameter = "locale";
            if (object.has(localeParameter) && object.get(localeParameter).isJsonPrimitive()) {
                final JsonPrimitive primitive = object.get(localeParameter).getAsJsonPrimitive();
                locale = primitive.getAsString();
                clientApplicationLocale = localeFromString(locale);
            }
        }
        return clientApplicationLocale;
    }

    public String[] extractArrayNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        String[] arrayValue = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName)) {
                parametersPassedInRequest.add(parameterName);
                final JsonArray array = object.get(parameterName).getAsJsonArray();
                arrayValue = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    arrayValue[i] = array.get(i).getAsString();
                }
            }
        }
        return arrayValue;
    }

    public JsonArray extractJsonArrayNamed(final String parameterName, final JsonElement element) {
        JsonArray jsonArray = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName)) {
                jsonArray = object.get(parameterName).getAsJsonArray();
            }
        }

        return jsonArray;
    }

    public JsonObject extractJsonObjectNamed(final String parameterName, final JsonElement element) {
        JsonObject jsonObject = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName)) {
                jsonObject = object.get(parameterName).getAsJsonObject();
            }
        }

        return jsonObject;
    }

    /**
     * Used with the local date is in array format
     */
    public LocalDate extractLocalDateAsArrayNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {
        LocalDate value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            if (object.has(parameterName) && object.get(parameterName).isJsonArray()) {
                parametersPassedInCommand.add(parameterName);
                final JsonArray dateArray = object.get(parameterName).getAsJsonArray();
                final int year = dateArray.get(0).getAsInt();
                final int month = dateArray.get(1).getAsInt();
                final int day = dateArray.get(2).getAsInt();

                value = LocalDate.of(year, month, day);
            }
        }
        return value;
    }

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonElement element) {

        MonthDay value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final String monthDayFormat = extractMonthDayFormatParameter(object);
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractMonthDayNamed(parameterName, object, monthDayFormat, clientApplicationLocale);
        }
        return value;
    }

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonObject element, String dateFormat,
            final Locale clientApplicationLocale) {
        MonthDay value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {

                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    try {
                        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                                .appendPattern(dateFormat).toFormatter(clientApplicationLocale).withResolverStyle(ResolverStyle.STRICT);
                        value = MonthDay.parse(valueAsString, formatter);
                    } catch (final IllegalArgumentException | DateTimeException e) {
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                        final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.month.day",
                                "The parameter `" + parameterName + "` is invalid based on the monthDayFormat: `" + dateFormat
                                        + "` and locale: `" + clientApplicationLocale + "` provided:",
                                parameterName, valueAsString, dateFormat);
                        dataValidationErrors.add(error);

                        throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                                dataValidationErrors, e);
                    }
                }
            }

        }
        return value;
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {

        LocalDate value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            final String dateFormat = extractDateFormatParameter(object);
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractLocalDateNamed(parameterName, object, dateFormat, clientApplicationLocale, parametersPassedInCommand);
        }
        return value;
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {

        LocalTime value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            value = extractLocalTimeNamed(parameterName, element, extractTimeFormatParameter(object), parametersPassedInCommand);
        }
        return value;
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {

        LocalDateTime value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            value = extractLocalDateTimeNamed(parameterName, element, extractDateFormatParameter(object), parametersPassedInCommand);
        }
        return value;
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element, String timeFormat,
            final Set<String> parametersPassedInCommand) {

        LocalTime value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractLocalTimeNamed(parameterName, object, timeFormat, clientApplicationLocale, parametersPassedInCommand);
        }
        return value;
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element, String timeFormat,
            final Set<String> parametersPassedInCommand) {

        LocalDateTime value = null;

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractLocalDateTimeNamed(parameterName, object, timeFormat, clientApplicationLocale, parametersPassedInCommand);
        }
        return value;
    }

    public LocalTime extractLocalTimeNamed(final String parameterName, final JsonElement element, final String timeFormat,
            final Locale clientApplicationLocale, final Set<String> parametersPassedInCommand) {
        LocalTime value = null;
        String timeValueAsString = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInCommand.add(parameterName);

                try {
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
                    final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                    timeValueAsString = primitive.getAsString();
                    if (StringUtils.isNotBlank(timeValueAsString)) {
                        value = LocalTime.parse(timeValueAsString, timeFormatter);
                    }
                } catch (IllegalArgumentException e) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    final String defaultMessage = new StringBuilder("The parameter `" + timeValueAsString + "` is not in correct format.")
                            .toString();
                    final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.TimeFormat", defaultMessage,
                            parameterName);
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors, e);
                }

            }
        }
        return value;
    }

    public LocalDateTime extractLocalDateTimeNamed(final String parameterName, final JsonElement element, String timeFormat,
            final Locale clientApplicationLocale, final Set<String> parametersPassedInCommand) {
        LocalDateTime value = null;
        String timeValueAsString;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInCommand.add(parameterName);

                try {
                    String strictResolveCompatibleTimeFormat = timeFormat.replace("y", "u");
                    final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                    timeValueAsString = primitive.getAsString();
                    DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                            .appendPattern(strictResolveCompatibleTimeFormat).toFormatter(clientApplicationLocale)
                            .withResolverStyle(ResolverStyle.STRICT);
                    if (StringUtils.isNotBlank(timeValueAsString)) {
                        value = LocalDateTime.parse(timeValueAsString, timeFormatter);
                    }
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    final ApiParameterError error = ApiParameterError
                            .parameterError("validation.msg.invalid.dateFormat.format",
                                    "The parameter `" + parameterName + "` is invalid based on the dateFormat: `" + timeFormat
                                            + "` and locale: `" + clientApplicationLocale + "` provided:",
                                    parameterName, value, timeFormat);
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors, e);
                }

            }
        }
        return value;
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element, final String dateFormat,
            final Locale clientApplicationLocale, final Set<String> parametersPassedInCommand) {
        LocalDate value = null;
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();

            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {

                parametersPassedInCommand.add(parameterName);

                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    value = convertFrom(valueAsString, parameterName, dateFormat, clientApplicationLocale);
                }
            }

        }
        return value;
    }

    public static LocalDate convertFrom(final String dateAsString, final String parameterName, final String dateFormat,
            final Locale clientApplicationLocale) {

        return convertDateTimeFrom(dateAsString, parameterName, dateFormat, clientApplicationLocale).toLocalDate();
    }

    public static LocalDate convertFrom(final String dateAsString, final String parameterName, final DateFormat dateFormat,
            final Locale clientApplicationLocale) {

        String rawDateFormat = Objects.isNull(dateFormat) ? null : dateFormat.getDateFormat();

        return convertDateTimeFrom(dateAsString, parameterName, rawDateFormat, clientApplicationLocale).toLocalDate();
    }

    public static LocalDateTime convertDateTimeFrom(final String dateTimeAsString, final String parameterName, String dateTimeFormat,
            final Locale clientApplicationLocale) {

        validateDateFormatAndLocale(parameterName, dateTimeFormat, clientApplicationLocale);
        LocalDateTime eventLocalDateTime = null;
        if (StringUtils.isNotBlank(dateTimeAsString)) {
            try {
                String strictResolveCompatibleDateTimeFormat = dateTimeFormat.replace("y", "u");
                DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                        .appendPattern(strictResolveCompatibleDateTimeFormat).optionalStart().appendPattern(" HH:mm:ss").optionalEnd()
                        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter(clientApplicationLocale)
                        .withResolverStyle(ResolverStyle.STRICT);
                eventLocalDateTime = LocalDateTime.parse(dateTimeAsString, formatter);
            } catch (final IllegalArgumentException | DateTimeParseException e) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                final ApiParameterError error = ApiParameterError
                        .parameterError("validation.msg.invalid.dateFormat.format",
                                "The parameter `" + parameterName + "` is invalid based on the dateFormat: `" + dateTimeFormat
                                        + "` and locale: `" + clientApplicationLocale + "` provided:",
                                parameterName, eventLocalDateTime, dateTimeFormat);
                dataValidationErrors.add(error);

                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }

        return eventLocalDateTime;
    }

    private static void validateDateFormatAndLocale(final String parameterName, final String dateFormat,
            final Locale clientApplicationLocale) {
        if (StringUtils.isBlank(dateFormat) || clientApplicationLocale == null) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            if (StringUtils.isBlank(dateFormat)) {
                final String defaultMessage = new StringBuilder(
                        "The parameter `" + parameterName + "` requires a `dateFormat` parameter to be passed with it.").toString();
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.dateFormat.parameter",
                        defaultMessage, parameterName);
                dataValidationErrors.add(error);
            }
            if (clientApplicationLocale == null) {
                final String defaultMessage = new StringBuilder(
                        "The parameter `" + parameterName + "` requires a `locale` parameter to be passed with it.").toString();
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                        parameterName);
                dataValidationErrors.add(error);
            }
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

    }

    public Integer convertToInteger(final String numericalValueFormatted, final String parameterName,
            final Locale clientApplicationLocale) {

        if (clientApplicationLocale == null) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final String defaultMessage = new StringBuilder(
                    "The parameter `" + parameterName + "` requires a `locale` parameter to be passed with it.").toString();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                    parameterName);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        try {
            Integer number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {

                String source = numericalValueFormatted.trim();

                final NumberFormat format = NumberFormat.getInstance(clientApplicationLocale);
                final DecimalFormat df = (DecimalFormat) format;
                final DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                df.setParseBigDecimal(true);

                // http://bugs.sun.com/view_bug.do?bug_id=4510618
                final char groupingSeparator = symbols.getGroupingSeparator();
                if (groupingSeparator == '\u00a0') {
                    source = source.replaceAll(" ", Character.toString('\u00a0'));
                }

                final Number parsedNumber = df.parse(source);

                final double parsedNumberDouble = parsedNumber.doubleValue();
                final int parsedNumberInteger = parsedNumber.intValue();

                if (source.contains(Character.toString(symbols.getDecimalSeparator()))) {
                    throw new ParseException(source, 0);
                }

                if (!Double.valueOf(parsedNumberDouble).equals(Double.valueOf(Integer.valueOf(parsedNumberInteger)))) {
                    throw new ParseException(source, 0);
                }

                number = parsedNumber.intValue();
            }

            return number;
        } catch (final ParseException e) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterErrorWithValue("validation.msg.invalid.integer.format",
                    "The parameter `" + parameterName + "` has value: " + numericalValueFormatted
                            + " which is invalid integer value for provided locale of [" + clientApplicationLocale.toString() + "].",
                    parameterName, numericalValueFormatted, numericalValueFormatted, clientApplicationLocale);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

    public Integer convertToIntegerSanLocale(final String numericalValueFormatted, final String parameterName) {

        try {
            Integer number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {
                number = Integer.valueOf(numericalValueFormatted);
            }

            return number;
        } catch (final NumberFormatException e) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterErrorWithValue("validation.msg.invalid.integer",
                    "The parameter `" + parameterName + "` has value: " + numericalValueFormatted + " which is invalid integer.",
                    parameterName, numericalValueFormatted, numericalValueFormatted);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

    public BigDecimal convertFrom(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {

        if (clientApplicationLocale == null) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final String defaultMessage = new StringBuilder(
                    "The parameter `" + parameterName + "` requires a `locale` parameter to be passed with it.").toString();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                    parameterName);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        try {
            BigDecimal number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {

                String source = numericalValueFormatted.trim();

                final NumberFormat format = NumberFormat.getNumberInstance(clientApplicationLocale);
                final DecimalFormat df = (DecimalFormat) format;
                final DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                // http://bugs.sun.com/view_bug.do?bug_id=4510618
                final char groupingSeparator = symbols.getGroupingSeparator();
                if (groupingSeparator == '\u00a0') {
                    source = source.replaceAll(" ", Character.toString('\u00a0'));
                }

                final NumberStyleFormatter numberFormatter = new NumberStyleFormatter();
                final Number parsedNumber = numberFormatter.parse(source, clientApplicationLocale);
                if (parsedNumber instanceof BigDecimal) {
                    number = (BigDecimal) parsedNumber;
                } else {
                    number = BigDecimal.valueOf(parsedNumber.doubleValue());
                }
            }

            return number;
        } catch (final ParseException e) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterErrorWithValue("validation.msg.invalid.decimal.format",
                    "The parameter `" + parameterName + "` has value: " + numericalValueFormatted
                            + " which is invalid decimal value for provided locale of [" + clientApplicationLocale + "].",
                    parameterName, numericalValueFormatted, numericalValueFormatted, clientApplicationLocale);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

    /***
     * TODO: Vishwas move all Locale related code to a separate Utils class
     ***/
    @SuppressWarnings("StringSplitter")
    public static Locale localeFromString(final String localeAsString) {

        if (StringUtils.isBlank(localeAsString)) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                    "The parameter `locale` is invalid. It cannot be blank.", "locale");
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        String languageCode = "";
        String countryCode = "";
        String variantCode = "";

        final String[] localeParts = localeAsString.split("_");

        if (localeParts.length == 1) {
            languageCode = localeParts[0];
        }

        if (localeParts.length == 2) {
            languageCode = localeParts[0];
            countryCode = localeParts[1];
        }

        if (localeParts.length == 3) {
            languageCode = localeParts[0];
            countryCode = localeParts[1];
            variantCode = localeParts[2];
        }

        return localeFrom(languageCode, countryCode, variantCode);
    }

    private static Locale localeFrom(final String languageCode, final String courntryCode, final String variantCode) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final List<String> allowedLanguages = Arrays.asList(Locale.getISOLanguages());
        if (!allowedLanguages.contains(languageCode.toLowerCase())) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                    "The parameter `locale` has an invalid language value " + languageCode + " .", "locale", languageCode);
            dataValidationErrors.add(error);
        }

        if (StringUtils.isNotBlank(courntryCode.toUpperCase())) {
            final List<String> allowedCountries = Arrays.asList(Locale.getISOCountries());
            if (!allowedCountries.contains(courntryCode)) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                        "The parameter `locale` has an invalid country value " + courntryCode + " .", "locale", courntryCode);
                dataValidationErrors.add(error);
            }
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        return new Locale(languageCode.toLowerCase(), courntryCode.toUpperCase(), variantCode);
    }

    private Locale extractLocaleValue(final JsonObject object) {
        Locale clientApplicationLocale = null;
        String locale = null;
        if (object.has("locale") && object.get("locale").isJsonPrimitive()) {
            final JsonPrimitive primitive = object.get("locale").getAsJsonPrimitive();
            locale = primitive.getAsString();
            clientApplicationLocale = localeFromString(locale);
        }
        return clientApplicationLocale;
    }
}
