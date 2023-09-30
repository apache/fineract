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

public class DateFormat {

    @Getter
    private final String dateFormat;

    public DateFormat(String rawDateFormat) {
        if (StringUtils.isBlank(rawDateFormat)) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.dateFormat.format",
                    "Dateformat is null", rawDateFormat);
            throw new PlatformApiDataValidationException("validation.msg.invalid.dateFormat.format", "Validation errors exist.",
                    List.of(error));
        } else {
            String compatibleDateFormat = rawDateFormat.replace("yyyy", "uuuu");
            validate(compatibleDateFormat);
            dateFormat = compatibleDateFormat;
        }
    }

    private void validate(String dateTimeFormat) {
        try {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(dateTimeFormat)
                    .optionalStart().appendPattern(" HH:mm:ss").optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);
        } catch (final IllegalArgumentException | DateTimeParseException e) {
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.dateFormat.format",
                    "Invalid dateFormat: `" + dateTimeFormat, dateTimeFormat);
            throw new PlatformApiDataValidationException("validation.msg.invalid.dateFormat.format", "Validation errors exist.",
                    List.of(error), e);
        }
    }
}
