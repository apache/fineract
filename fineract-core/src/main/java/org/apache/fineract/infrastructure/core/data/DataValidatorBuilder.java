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
package org.apache.fineract.infrastructure.core.data;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.quartz.CronExpression;
import org.springframework.util.ObjectUtils;

public class DataValidatorBuilder {

    public static final String VALID_INPUT_SEPARATOR = "_";
    private final List<ApiParameterError> dataValidationErrors;
    private String resource;
    private String parameter;
    private String arrayPart;
    private Integer arrayIndex;
    private Object value;
    private boolean ignoreNullValue = false;

    /**
     * Default constructor used to start a new "validation chain".
     */
    public DataValidatorBuilder() {
        this(new ArrayList<>());
    }

    /**
     * Constructor used to "continue" an existing "validation chain".
     *
     * @param dataValidationErrors
     *            an existing list of {@link ApiParameterError} to add new validation errors to
     */

    public DataValidatorBuilder(final List<ApiParameterError> dataValidationErrors) {
        this.dataValidationErrors = dataValidationErrors;
    }

    public DataValidatorBuilder reset() {
        return new DataValidatorBuilder(this.dataValidationErrors).resource(this.resource);
    }

    public void merge(DataValidatorBuilder other) {
        dataValidationErrors.addAll(other.dataValidationErrors);
    }

    public boolean hasError() {
        return !dataValidationErrors.isEmpty();
    }

    public List<ApiParameterError> getDataValidationErrors() {
        return dataValidationErrors;
    }

    public DataValidatorBuilder resource(final String resource) {
        this.resource = resource;
        return this;
    }

    public DataValidatorBuilder parameter(final String parameter) {
        this.parameter = parameter;
        return this;
    }

    public DataValidatorBuilder parameterAtIndexArray(final String arrayPart, final Integer arrayIndex) {
        this.arrayPart = arrayPart;
        this.arrayIndex = arrayIndex;
        return this;
    }

    public DataValidatorBuilder value(final Object value) {
        this.value = value;
        return this;
    }

    public DataValidatorBuilder ignoreIfNull() {
        this.ignoreNullValue = true;
        return this;
    }

    public DataValidatorBuilder andNotBlank(final String linkedParameterName, final String linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (StringUtils.isBlank(linkedValue)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + linkedParameterName + ".cannot.be.empty.when."
                    + this.parameter + ".is.populated";
            String defaultEnglishMessage = "The parameter `" + linkedParameterName + "` cannot be empty when " + this.parameter
                    + " is populated.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage,
                    linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public void failWithCode(final String errorCode, final Object... defaultUserMessageArgs) {
        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + "." + errorCode;
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode,
                "Failed data validation due to: " + errorCode + ".", this.parameter, this.value, defaultUserMessageArgs);
        this.dataValidationErrors.add(error);
    }

    public void failWithCodeNoParameterAddedToErrorCode(final String errorCode, final Object... defaultUserMessageArgs) {
        String validationErrorCode = "validation.msg." + this.resource + "." + errorCode;
        String defaultEnglishMessage = "Failed data validation due to: " + errorCode + ".";
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                this.value, defaultUserMessageArgs);
        this.dataValidationErrors.add(error);
    }

    public DataValidatorBuilder equalToParameter(final String linkedParameterName, final Object linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.equals(linkedValue)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + linkedParameterName + ".not.equal.to." + this.parameter;
            String defaultEnglishMessage = "The parameter `" + linkedParameterName + "` is not equal to " + this.parameter + ".";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage,
                    linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notSameAsParameter(final String linkedParameterName, final Object linkedValue) {
        if (this.value == null && linkedValue == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && this.value.equals(linkedValue)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + linkedParameterName + ".same.as." + this.parameter;
            String defaultEnglishMessage = "The parameter `" + linkedParameterName + "` is same as " + this.parameter + ".";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage,
                    linkedParameterName, linkedValue, this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder trueOrFalseRequired(final Object trueOfFalseField) {

        if (trueOfFalseField != null && !trueOfFalseField.toString().equalsIgnoreCase("true")
                && !trueOfFalseField.toString().equalsIgnoreCase("false")) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".must.be.true.or.false";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be set as true or false.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter);
            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder notNull() {
        if (this.value == null && !this.ignoreNullValue) {

            String realParameterName = this.parameter;
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter);
            if (this.arrayIndex != null && StringUtils.isNotBlank(this.arrayPart)) {
                validationErrorCode.append(".").append(this.arrayPart);
                realParameterName = this.parameter + '[' + this.arrayIndex + "][" + this.arrayPart + ']';
            }

            validationErrorCode.append(".cannot.be.blank");
            String defaultEnglishMessage = "The parameter `" + realParameterName + "` is mandatory.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage,
                    realParameterName, this.arrayIndex);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notBlank() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null || StringUtils.isBlank(this.value.toString())) {
            String realParameterName = this.parameter;
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(this.resource).append(".")
                    .append(this.parameter);
            if (this.arrayIndex != null && StringUtils.isNotBlank(this.arrayPart)) {
                validationErrorCode.append(".").append(this.arrayPart);
                realParameterName = this.parameter + '[' + this.arrayIndex + "][" + this.arrayPart + ']';
            }

            validationErrorCode.append(".cannot.be.blank");
            String defaultEnglishMessage = "The parameter `" + realParameterName + "` is mandatory.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage,
                    realParameterName, this.arrayIndex);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notExceedingLengthOf(final Integer maxLength) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && this.value.toString().trim().length() > maxLength) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".exceeds.max.length";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` exceeds max length of " + maxLength + ".";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    maxLength, this.value.toString());
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder notExceedingListLengthOf(final Integer maxLength) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value instanceof List && ((List<?>) this.value).size() > maxLength) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".exceeds.max.length.allowed";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` exceeds allowed max length of " + maxLength + ".";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder inMinMaxRange(final Integer min, final Integer max) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < min || number > max) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.within.expected.range";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be between " + min + " and " + max + ".";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, min, max);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder isOneOfTheseValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final List<Object> rawValuesList = Arrays.asList(values);

        if (this.value == null || !rawValuesList.contains(this.value)) {
            final List<String> valuesList = Arrays.stream(values).map(Object::toString).toList();
            final String valuesListStr = String.join(", ", valuesList);
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.one.of.expected.enumerations";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be one of [ " + valuesListStr + " ] " + ".";

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value, values);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isOneOfEnumValues(Class<? extends Enum<?>> e) {
        final List<String> enumValuesList = Arrays.asList(Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new));
        return isOneOfTheseStringValues(enumValuesList);
    }

    public DataValidatorBuilder isOneOfTheseStringValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final List<Object> rawValuesList = Arrays.asList(values);

        if (this.value == null || !rawValuesList.contains(this.value.toString().toLowerCase())) {
            final List<String> valuesList = Arrays.stream(values).map(Object::toString).toList();
            final String valuesListStr = String.join(", ", valuesList);
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.one.of.expected.enumerations";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be one of [ " + valuesListStr + " ] " + ".";

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value, values);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isOneOfTheseStringValues(final List<String> valuesList) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        List<String> valuesListLowercase = valuesList.stream().map(String::toLowerCase).toList();

        if (this.value == null || !valuesListLowercase.contains(this.value.toString().toLowerCase())) {
            final String valuesListStr = String.join(", ", valuesList);
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.one.of.expected.enumerations";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be one of [ " + valuesListStr + " ] " + ".";

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value, valuesList);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder isNotOneOfTheseValues(final Object... values) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final List<Object> rawValuesList = Arrays.asList(values);

            if (rawValuesList.contains(this.value)) {
                final List<String> valuesList = Arrays.stream(values).map(Object::toString).toList();
                final String valuesListStr = String.join(", ", valuesList);
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.one.of.unwanted.enumerations";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must not be any of [ " + valuesListStr + " ] " + ".";

                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        this.value, values);

                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder positiveAmount() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (number.compareTo(BigDecimal.ZERO) <= 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.greater.than.zero";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than 0.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder zeroOrPositiveAmount() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (number.compareTo(BigDecimal.ZERO) < 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.zero.or.greater";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than or equal to 0.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder integerZeroOrGreater() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final int number = Integer.parseInt(this.value.toString());
            if (number < 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.zero.or.greater";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be zero or greater.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder integerGreaterThanZero() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final int number = Integer.parseInt(this.value.toString());
            if (number < 1) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.greater.than.zero";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than 0.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerGreaterThanNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final int intValue = Integer.parseInt(this.value.toString());
            if (intValue < number + 1) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter
                        + ".not.greater.than.specified.number";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than " + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerEqualToOrGreaterThanNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (intValue < number) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter
                        + ".not.equal.to.or.greater.than.specified.number";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be equal to or greater than" + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerSameAsNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (!intValue.equals(number)) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.equal.to.specified.number";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be same as" + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder integerInMultiplesOfNumber(Integer number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final Integer intValue = Integer.valueOf(this.value.toString());
            if (intValue < number || intValue % number != 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter
                        + ".not.in.multiples.of.specified.number";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be multiples of " + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        intValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder longGreaterThanZero() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final long number = Long.parseLong(this.value.toString());
            if (number < 1) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.greater.than.zero";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than 0.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    /*
     * should be used with .notNull() before it
     */
    public DataValidatorBuilder longZeroOrGreater() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final long number = Long.parseLong(this.value.toString());
            if (number < 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.equal.or.greater.than.zero";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be equal or greater than 0.";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, 0);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder longGreaterThanNumber(Long number) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final long longValue = Long.parseLong(this.value.toString());
            if (longValue < number + 1) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter
                        + ".not.greater.than.specified.number";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than " + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        longValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder longGreaterThanNumber(String paramName, Long number, int index) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null) {
            final long longValue = Long.parseLong(this.value.toString());
            if (longValue < number + 1) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".not.greater.than.specified."
                        + paramName + ".at Index." + index;
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than " + number;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        longValue, number);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder arrayNotEmpty() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final Object[] array = (Object[]) this.value;
        if (ObjectUtils.isEmpty(array)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".cannot.be.empty";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` cannot be empty. You must select at least one.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder jsonArrayNotEmpty() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        final JsonArray array = (JsonArray) this.value;
        if (this.value != null && !array.iterator().hasNext()) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".cannot.be.empty";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` cannot be empty. You must select at least one.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public void expectedArrayButIsNot() {
        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.an.array";
        String defaultEnglishMessage = "The parameter `" + this.parameter + "` is not an array.";
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter);
        this.dataValidationErrors.add(error);
    }

    public DataValidatorBuilder anyOfNotNull(final Object... object) {
        boolean hasData = false;
        for (final Object obj : object) {
            if (obj != null) {
                hasData = true;
                break;
            }
        }

        if (!hasData) {
            String validationErrorCode = "validation.msg." + this.resource + ".no.parameters.for.update";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, "No parameters passed for update.", "id");
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder inValidValue(final String parameterValueCode, final Object invalidValue) {
        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".invalid." + parameterValueCode;
        String defaultEnglishMessage = "The parameter `" + this.parameter + "` has an invalid value.";
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                invalidValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder mustBeBlankWhenParameterProvided(final String parameterName, final Object parameterValue) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null && parameterValue != null) {
            return this;
        }

        if (this.value != null && StringUtils.isBlank(this.value.toString()) && parameterValue != null
                && StringUtils.isNotBlank(parameterValue.toString())) {
            return this;
        }

        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".cannot.also.be.provided.when."
                + parameterName + ".is.populated";
        String defaultEnglishMessage = "The parameter `" + this.parameter + "` cannot also be provided when `" + parameterName
                + "` is populated.";
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder mustBeBlankWhenParameterProvidedIs(final String parameterName, final Object parameterValue) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value == null && parameterValue != null) {
            return this;
        }

        if (this.value != null && StringUtils.isBlank(this.value.toString()) && parameterValue != null
                && StringUtils.isNotBlank(parameterValue.toString())) {
            return this;
        }

        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".cannot.also.be.provided.when."
                + parameterName + ".is." + parameterValue;
        String defaultEnglishMessage = "The parameter `" + this.parameter + "` cannot also be provided when `" + parameterName + "` is "
                + parameterValue;
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder cantBeBlankWhenParameterProvidedIs(final String parameterName, final Object parameterValue) {
        if (this.value != null && StringUtils.isNotBlank(this.value.toString())) {
            return this;
        }

        String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".must.be.provided.when." + parameterName
                + ".is." + parameterValue;
        String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be provided when `" + parameterName + "` is "
                + parameterValue;
        final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                this.value, parameterName, parameterValue);
        this.dataValidationErrors.add(error);
        return this;
    }

    public DataValidatorBuilder compareMinimumAndMaximumAmounts(final BigDecimal minimumBalance, final BigDecimal maximumBalance) {
        if (minimumBalance != null && maximumBalance != null) {
            if (maximumBalance.compareTo(minimumBalance) < 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.within.expected.range";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` minimum amount " + minimumBalance
                        + " should less than the maximum amount " + maximumBalance + ".";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        minimumBalance, maximumBalance);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder inMinAndMaxAmountRange(final BigDecimal minimumAmount, final BigDecimal maximumAmount) {
        if (minimumAmount != null && maximumAmount != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(minimumAmount) < 0 || amount.compareTo(maximumAmount) > 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter
                        + ".amount.is.not.within.min.max.range";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` amount " + amount + " must be between "
                        + minimumAmount + " and " + maximumAmount + " .";
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        amount, minimumAmount, maximumAmount);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notLessThanMin(final BigDecimal min) {
        if (min != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(min) < 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.less.than.min";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` value " + amount
                        + " must not be less than the minimum value " + min;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        amount, min);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notGreaterThanMax(final BigDecimal max) {
        if (max != null && this.value != null) {
            final BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
            if (amount.compareTo(max) > 0) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.greater.than.max";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` value " + amount
                        + " must not be more than maximum value " + max;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        amount, max);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder compareMinAndMaxOfTwoBigDecmimalNos(final BigDecimal min, final BigDecimal max) {
        if (min != null && max != null && max.compareTo(min) < 0) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.within.expected.range";
            String defaultEnglishMessage = "The " + " min number " + min + " should less than max number " + max + ".";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    min, max);
            this.dataValidationErrors.add(error);
            return this;
        }
        return this;
    }

    public DataValidatorBuilder isValidRecurringRule(final String recurringRule) {
        if (StringUtils.isNotBlank(recurringRule)) {
            try {
                final RRule rRule = new RRule(recurringRule);
                rRule.validate();
            } catch (final ValidationException e) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.recurring.rule",
                        "The Recurring Rule value: " + recurringRule + " is not valid.", this.parameter, recurringRule);
                this.dataValidationErrors.add(error);
                return this;
            } catch (final ParseException e) {
                final ApiParameterError error = ApiParameterError.parameterError("validation.msg.recurring.rule.parsing.error",
                        "Error in parsing the Recurring Rule value: " + recurringRule + ".", this.parameter, recurringRule);
                this.dataValidationErrors.add(error);
                return this;
            }
        }
        return this;
    }

    public DataValidatorBuilder notLessThanMin(final Integer min) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && min != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number < min) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.less.than.min";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than the minimum value " + min;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, min);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder notGreaterThanMax(final Integer max) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && max != null) {
            final Integer number = Integer.valueOf(this.value.toString());
            if (number > max) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.greater.than.max";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be less than the maximum value " + max;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        number, max);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder matchesRegularExpression(final String expression) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.toString().matches(expression)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".does.not.match.regexp";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` must match the provided regular expression [ "
                    + expression + " ] " + ".";

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value, expression);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    public DataValidatorBuilder matchesRegularExpression(final String expression, final String Message) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && !this.value.toString().matches(expression)) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".does.not.match.regexp";

            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, Message, this.parameter, this.value,
                    expression);

            this.dataValidationErrors.add(error);
        }

        return this;
    }

    private DataValidatorBuilder validateStringFor(final String validInputs) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        final Iterable<String> inputs = Splitter.onPattern(VALID_INPUT_SEPARATOR).split(validInputs);
        boolean validationErr = true;
        for (final String input : inputs) {
            if (input.equalsIgnoreCase(this.value.toString().trim())) {
                validationErr = false;
                break;
            }
        }
        if (validationErr) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".value.should.true.or.false";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` value should true or false ";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateForBooleanValue() {
        return validateStringFor("TRUE" + VALID_INPUT_SEPARATOR + "FALSE");
    }

    public DataValidatorBuilder validatePhoneNumber() {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }
        boolean validationErr = true;
        /*
         * supports numbers, parentheses(), hyphens and may contain + sign in the beginning and can contain whitespaces
         * in between and length allowed is 0-25 chars.
         */
        final String regex = "^\\+?[0-9. ()-]{0,25}$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(this.value.toString());
        if (matcher.matches()) {
            validationErr = false;
        }
        if (validationErr) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".format.is.invalid";
            String defaultEnglishMessage = "The parameter `" + this.parameter
                    + "` is in invalid format, should contain '-','+','()' and numbers only.";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateCronExpression() {
        if (this.value != null && !CronExpression.isValidExpression(this.value.toString().trim())) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".invalid";
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` value is not a valid cron expression";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    this.value);
            this.dataValidationErrors.add(error);
        }
        return this;
    }

    public DataValidatorBuilder validateDateAfter(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (DateUtils.isAfter(date, dateVal)) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.less.than.date";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be greater than the provided date" + date;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateBefore(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (DateUtils.isBefore(date, dateVal)) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.greater.than.date";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be less than the provided date" + date;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateBeforeOrEqual(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (DateUtils.isAfter(dateVal, date)) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.greater.than.date";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be less than or equal to the provided date: "
                        + date;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder validateDateForEqual(final LocalDate date) {
        if (this.value == null && this.ignoreNullValue) {
            return this;
        }

        if (this.value != null && date != null) {
            final LocalDate dateVal = (LocalDate) this.value;
            if (!DateUtils.isEqual(dateVal, date)) {
                String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".is.not.equal.to.date";
                String defaultEnglishMessage = "The parameter `" + this.parameter + "` must be equal to the provided date" + date;
                final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                        dateVal, date);
                this.dataValidationErrors.add(error);
            }
        }
        return this;
    }

    public DataValidatorBuilder scaleNotGreaterThan(Integer scale) {
        final BigDecimal value = BigDecimal.valueOf(Double.parseDouble(this.value.toString()));
        if (value.scale() > scale.intValue()) {
            String validationErrorCode = "validation.msg." + this.resource + "." + this.parameter + ".scale.is.greater.than." + scale;
            String defaultEnglishMessage = "The parameter `" + this.parameter + "` value " + value + " decimal place must not be more than "
                    + scale + " places";
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode, defaultEnglishMessage, this.parameter,
                    value, scale);
            this.dataValidationErrors.add(error);
            return this;
        }
        return this;
    }

    /**
     * Throws Exception if validation errors.
     *
     * @throws PlatformApiDataValidationException
     *             unchecked exception (RuntimeException) thrown if there are any validation error
     */
    public void throwValidationErrors() throws PlatformApiDataValidationException {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
