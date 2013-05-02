/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.serialization;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.format.number.NumberFormatter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Helper class to extract values of json named attributes.
 */
public class JsonParserHelper {

    public boolean parameterExists(final String parameterName, final JsonElement element) {
        return element.getAsJsonObject().has(parameterName);
    }

    public Boolean extractBooleanNamed(final String parameterName, final JsonElement element, final Set<String> requestParamatersDetected) {
        Boolean value = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                requestParamatersDetected.add(parameterName);

                JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                value = primitive.getAsBoolean();
            }
        }
        return value;
    }

    public Long extractLongNamed(final String parameterName, final JsonElement element, final Set<String> parametersPassedInRequest) {
        Long longValue = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInRequest.add(parameterName);
                JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
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
            JsonObject object = element.getAsJsonObject();
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
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                modifiedParameters.add(parameterName);
                JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    value = convertFrom(valueAsString, parameterName, locale);
                }
            }
        }
        return value;
    }

    public Integer extractIntegerWithLocaleNamed(final String parameterName, final JsonElement element, final Set<String> modifiedParameters) {
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
                JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    value = convertToInteger(valueAsString, parameterName, locale);
                }
            }
        }
        return value;
    }

    /**
     * Method used to extract integers from unformatted strings. Ex: "1" ,
     * "100002" etc
     * 
     * Please note that this method does not support extracting Integers from
     * locale specific formatted strings Ex "1,000" etc
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
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {
                parametersPassedInRequest.add(parameterName);
                JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
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
            JsonObject object = element.getAsJsonObject();

            final String dateFormatParameter = "dateFormat";
            if (object.has(dateFormatParameter) && object.get(dateFormatParameter).isJsonPrimitive()) {
                final JsonPrimitive primitive = object.get(dateFormatParameter).getAsJsonPrimitive();
                value = primitive.getAsString();
            }
        }
        return value;
    }

    public String extractMonthDayFormatParameter(final JsonObject element) {
        String value = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

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
            JsonObject object = element.getAsJsonObject();

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
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName)) {
                parametersPassedInRequest.add(parameterName);
                JsonArray array = object.get(parameterName).getAsJsonArray();
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
            JsonObject object = element.getAsJsonObject();
            if (object.has(parameterName)) {
                jsonArray = object.get(parameterName).getAsJsonArray();
            }
        }

        return jsonArray;
    }

    /**
     * Used with the local date is in array format
     */
    public LocalDate extractLocalDateAsArrayNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {
        LocalDate value = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (object.has(parameterName) && object.get(parameterName).isJsonArray()) {

                parametersPassedInCommand.add(parameterName);

                final JsonArray dateArray = object.get(parameterName).getAsJsonArray();

                Integer year = dateArray.get(0).getAsInt();
                Integer month = dateArray.get(1).getAsInt();
                Integer day = dateArray.get(2).getAsInt();

                value = new LocalDate().withYearOfEra(year).withMonthOfYear(month).withDayOfMonth(day);
            }

        }
        return value;
    }

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonElement element) {

        MonthDay value = null;

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            final String monthDayFormat = extractMonthDayFormatParameter(object);
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractMonthDayNamed(parameterName, object, monthDayFormat, clientApplicationLocale);
        }
        return value;
    }

    public MonthDay extractMonthDayNamed(final String parameterName, final JsonObject element, final String dateFormat,
            final Locale clientApplicationLocale) {
        MonthDay value = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {

                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();
                final String valueAsString = primitive.getAsString();
                if (StringUtils.isNotBlank(valueAsString)) {
                    DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withLocale(clientApplicationLocale);
                    value = MonthDay.parse(valueAsString.toLowerCase(clientApplicationLocale), formatter);
                }
            }

        }
        return value;
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonElement element,
            final Set<String> parametersPassedInCommand) {

        LocalDate value = null;

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            final String dateFormat = extractDateFormatParameter(object);
            final Locale clientApplicationLocale = extractLocaleParameter(object);
            value = extractLocalDateNamed(parameterName, object, dateFormat, clientApplicationLocale, parametersPassedInCommand);
        }
        return value;
    }

    public LocalDate extractLocalDateNamed(final String parameterName, final JsonObject element, final String dateFormat,
            final Locale clientApplicationLocale, final Set<String> parametersPassedInCommand) {
        LocalDate value = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

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

    public LocalDate convertFrom(final String dateAsString, final String parameterName, final String dateFormat,
            final Locale clientApplicationLocale) {

        if (StringUtils.isBlank(dateFormat) || clientApplicationLocale == null) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            if (StringUtils.isBlank(dateFormat)) {
                String defaultMessage = new StringBuilder("The parameter '" + parameterName
                        + "' requires a 'dateFormat' parameter to be passed with it.").toString();
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.dateFormat.parameter", defaultMessage,
                        parameterName);
                dataValidationErrors.add(error);
            }
            if (clientApplicationLocale == null) {
                String defaultMessage = new StringBuilder("The parameter '" + parameterName
                        + "' requires a 'locale' parameter to be passed with it.").toString();
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                        parameterName);
                dataValidationErrors.add(error);
            }
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        LocalDate eventLocalDate = null;
        if (StringUtils.isNotBlank(dateAsString)) {
            try {
                // Locale locale = LocaleContextHolder.getLocale();
                eventLocalDate = DateTimeFormat.forPattern(dateFormat).withLocale(clientApplicationLocale)
                        .parseLocalDate(dateAsString.toLowerCase(clientApplicationLocale));
            } catch (IllegalArgumentException e) {
                List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.format", "The parameter "
                        + parameterName + " is invalid based on the dateFormat: '" + dateFormat + "' and locale: '"
                        + clientApplicationLocale + "' provided:", parameterName, dateAsString, dateFormat);
                dataValidationErrors.add(error);

                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors);
            }
        }

        return eventLocalDate;
    }

    public Integer convertToInteger(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {

        if (clientApplicationLocale == null) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            String defaultMessage = new StringBuilder("The parameter '" + parameterName
                    + "' requires a 'locale' parameter to be passed with it.").toString();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                    parameterName);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        try {
            Integer number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {

                String source = numericalValueFormatted.trim();

                NumberFormat format = NumberFormat.getInstance(clientApplicationLocale);
                DecimalFormat df = (DecimalFormat) format;
                DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                df.setParseBigDecimal(true);

                // http://bugs.sun.com/view_bug.do?bug_id=4510618
                char groupingSeparator = symbols.getGroupingSeparator();
                if (groupingSeparator == '\u00a0') {
                    source = source.replaceAll(" ", Character.toString('\u00a0'));
                }

                Number parsedNumber = df.parse(source);

                double parsedNumberDouble = parsedNumber.doubleValue();
                int parsedNumberInteger = parsedNumber.intValue();

                if (source.contains(Character.toString(symbols.getDecimalSeparator()))) { throw new ParseException(source, 0); }

                if (!Double.valueOf(parsedNumberDouble).equals(Double.valueOf(Integer.valueOf(parsedNumberInteger)))) { throw new ParseException(
                        source, 0); }

                number = parsedNumber.intValue();
            }

            return number;
        } catch (ParseException e) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.integer.format", "The parameter "
                    + parameterName + " has value: " + numericalValueFormatted + " which is invalid integer value for provided locale of ["
                    + clientApplicationLocale.toString() + "].", parameterName, numericalValueFormatted, clientApplicationLocale);
            error.setValue(numericalValueFormatted);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public Integer convertToIntegerSanLocale(final String numericalValueFormatted, final String parameterName) {

        try {
            Integer number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {
                number = Integer.valueOf(numericalValueFormatted);
            }

            return number;
        } catch (NumberFormatException e) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.integer", "The parameter " + parameterName
                    + " has value: " + numericalValueFormatted + " which is invalid integer.", parameterName, numericalValueFormatted);
            error.setValue(numericalValueFormatted);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public BigDecimal convertFrom(final String numericalValueFormatted, final String parameterName, final Locale clientApplicationLocale) {

        if (clientApplicationLocale == null) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            String defaultMessage = new StringBuilder("The parameter '" + parameterName
                    + "' requires a 'locale' parameter to be passed with it.").toString();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.missing.locale.parameter", defaultMessage,
                    parameterName);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        try {
            BigDecimal number = null;

            if (StringUtils.isNotBlank(numericalValueFormatted)) {

                String source = numericalValueFormatted.trim();

                NumberFormat format = NumberFormat.getNumberInstance(clientApplicationLocale);
                DecimalFormat df = (DecimalFormat) format;
                DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                // http://bugs.sun.com/view_bug.do?bug_id=4510618
                char groupingSeparator = symbols.getGroupingSeparator();
                if (groupingSeparator == '\u00a0') {
                    source = source.replaceAll(" ", Character.toString('\u00a0'));
                }

                NumberFormatter numberFormatter = new NumberFormatter();
                Number parsedNumber = numberFormatter.parse(source, clientApplicationLocale);
                number = BigDecimal.valueOf(Double.valueOf(parsedNumber.doubleValue()));
            }

            return number;
        } catch (ParseException e) {

            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.decimal.format", "The parameter "
                    + parameterName + " has value: " + numericalValueFormatted + " which is invalid decimal value for provided locale of ["
                    + clientApplicationLocale.toString() + "].", parameterName, numericalValueFormatted, clientApplicationLocale);
            error.setValue(numericalValueFormatted);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private Locale localeFromString(final String localeAsString) {

        if (StringUtils.isBlank(localeAsString)) {
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                    "The parameter locale is invalid. It cannot be blank.", "locale");
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        String languageCode = "";
        String courntryCode = "";
        String variantCode = "";

        String[] localeParts = localeAsString.split("_");

        if (localeParts != null && localeParts.length == 1) {
            languageCode = localeParts[0];
        }

        if (localeParts != null && localeParts.length == 2) {
            languageCode = localeParts[0];
            courntryCode = localeParts[1];
        }

        if (localeParts != null && localeParts.length == 3) {
            languageCode = localeParts[0];
            courntryCode = localeParts[1];
            variantCode = localeParts[2];
        }

        return localeFrom(languageCode, courntryCode, variantCode);
    }

    private Locale localeFrom(final String languageCode, final String courntryCode, final String variantCode) {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        List<String> allowedLanguages = Arrays.asList(Locale.getISOLanguages());
        if (!allowedLanguages.contains(languageCode.toLowerCase())) {
            ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                    "The parameter locale has an invalid language value " + languageCode + " .", "locale", languageCode);
            dataValidationErrors.add(error);
        }

        if (StringUtils.isNotBlank(courntryCode.toUpperCase())) {
            List<String> allowedCountries = Arrays.asList(Locale.getISOCountries());
            if (!allowedCountries.contains(courntryCode)) {
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.locale.format",
                        "The parameter locale has an invalid country value " + courntryCode + " .", "locale", courntryCode);
                dataValidationErrors.add(error);
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

        return new Locale(languageCode.toLowerCase(), courntryCode.toUpperCase(), variantCode);
    }

    private Locale extractLocaleValue(final JsonObject object) {
        Locale clientApplicationLocale = null;
        String locale = null;
        if (object.has("locale") && object.get("locale").isJsonPrimitive()) {
            JsonPrimitive primitive = object.get("locale").getAsJsonPrimitive();
            locale = primitive.getAsString();
            clientApplicationLocale = localeFromString(locale);
        }
        return clientApplicationLocale;
    }
}