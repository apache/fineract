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

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

@Getter
public class DateFormat {

    private static final String INVALID_DATE_FORMAT_MESSAGE = "validation.msg.invalid.dateFormat.format";
    private static final String VALIDATION_ERROR_MESSAGE = "Validation errors exist.";
    private static final String DATE_FORMAT_NULL_MESSAGE = "Dateformat is null";
    private static final String INVALID_DATE_FORMAT_PREFIX = "Invalid dateFormat: `";
    private static final String TIME_PATTERN = " HH:mm:ss";
    private static final String YEAR_PATTERN_OLD = "yyyy";
    private static final String YEAR_PATTERN_NEW = "uuuu";

    private final String dateFormat;

    public DateFormat(String rawDateFormat) {
        if (StringUtils.isBlank(rawDateFormat)) {
            final ApiParameterError error = ApiParameterError.parameterError(INVALID_DATE_FORMAT_MESSAGE, DATE_FORMAT_NULL_MESSAGE,
                    rawDateFormat);
            throw new PlatformApiDataValidationException(INVALID_DATE_FORMAT_MESSAGE, VALIDATION_ERROR_MESSAGE, List.of(error));
        } else {
            String compatibleDateFormat = rawDateFormat.replace(YEAR_PATTERN_OLD, YEAR_PATTERN_NEW);
            validate(compatibleDateFormat);
            dateFormat = compatibleDateFormat;
        }
    }

    private void validate(String dateTimeFormat) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(dateTimeFormat)
                    .optionalStart().appendPattern(TIME_PATTERN).optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);
        } catch (final IllegalArgumentException | DateTimeParseException e) {
            final ApiParameterError error = ApiParameterError.parameterError(INVALID_DATE_FORMAT_MESSAGE,
                    INVALID_DATE_FORMAT_PREFIX + dateTimeFormat, dateTimeFormat);
            throw new PlatformApiDataValidationException(INVALID_DATE_FORMAT_MESSAGE, VALIDATION_ERROR_MESSAGE, List.of(error), e);
        }
    }
}
