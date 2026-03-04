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
package org.apache.fineract.validation.constraints;

import static java.time.LocalDateTime.parse;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class LocalDateValidator implements ConstraintValidator<LocalDate, Object> {
    // Field names provided by the @LocalDate annotation.
    // These indicate which attributes of the validated object contain:
    // - the date string
    // - the expected format
    // - the locale to use for parsing
    private String dateField;
    private String formatField;
    private String localeField;

    @Override
    public void initialize(LocalDate annotation) {
        // Store the field names declared in the annotation so they can be accessed via reflection.
        this.dateField = annotation.dateField();
        this.formatField = annotation.formatField();
        this.localeField = annotation.localeField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // Access the fields dynamically using reflection. 
            // This allows the validator to be reused across different DTOs.
            var dateAttr = value.getClass().getDeclaredField(dateField);
            var formatAttr = value.getClass().getDeclaredField(formatField);
            var localeAttr = value.getClass().getDeclaredField(localeField);
            // Make private fields accessible for reading.
            dateAttr.setAccessible(true);
            formatAttr.setAccessible(true);
            localeAttr.setAccessible(true);
            // Extract the actual values from the object being validated.
            var date = (String) dateAttr.get(value);
            var format = (String) formatAttr.get(value);
            var locale = (String) localeAttr.get(value);
            // Basic validation: if any required field is missing or blank, the date is invalid.
            if (StringUtils.isBlank(date) || StringUtils.isBlank(format) || StringUtils.isBlank(locale)) {
                return false;
            }
            // Attempt to parse the date. If parsing fails, an exception is thrown and caught below.
            toLocalDate(date, format, locale);

            return true;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // This indicates a misconfiguration of the annotation or DTO.
            throw new RuntimeException("Invalid configuration for @LocalDate", e);
        } catch (Exception e) {
            // Any parsing or formatting error means the date is invalid.
            return false;
        }
    }
/**
     * Converts the provided date string into a LocalDateTime using the given format and locale.
     * This method throws an exception if the date does not match the expected pattern.
     *
     * The return value is intentionally used (instead of ignored) to comply with Sonar rule java:S2201.
     */
    
        private java.time.LocalDateTime toLocalDate(String date, String format, String locale) {   
        // Build a formatter that: 
        // - is case-insensitive
        //- accepts lenient parsing for patterns
        // - replaces 'y' with 'u' to avoid issues with year interpretation
        // - optionally parses time if present
        // - defaults missing time components to 00:00:00 
        // - uses strict resolver style to avoid ambiguous dates
        var formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(format.replace("y", "u"))
                .optionalStart().appendPattern(" HH:mm:ss").optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.forLanguageTag(locale)).withResolverStyle(ResolverStyle.STRICT);

        return parse(date, formatter);
    }
}
