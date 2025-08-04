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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import org.apache.commons.lang3.StringUtils;

public class AgeValidator implements ConstraintValidator<ValidAge, Object> {

    private int min;
    private int max;
    private String dateField;
    private String formatField;
    private String localeField;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.dateField = constraintAnnotation.dateField();
        this.formatField = constraintAnnotation.formatField();
        this.localeField = constraintAnnotation.localeField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            final var dateAttr = value.getClass().getDeclaredField(dateField);
            final var formatAttr = value.getClass().getDeclaredField(formatField);
            final var localeAttr = value.getClass().getDeclaredField(localeField);

            dateAttr.setAccessible(true);
            formatAttr.setAccessible(true);
            localeAttr.setAccessible(true);

            final var dateStr = (String) dateAttr.get(value);
            final var format = (String) formatAttr.get(value);
            final var locale = (String) localeAttr.get(value);

            if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(format) || StringUtils.isBlank(locale)) {
                return true;
            }

            final LocalDate parsedDate = toLocalDate(dateStr, format, locale);
            return isAgeInRange(parsedDate);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Invalid configuration for @Date of Birth", e);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAgeInRange(final LocalDate date) {
        final LocalDate now = LocalDate.now(ZoneId.systemDefault());
        if (date.isAfter(now)) {
            return false;
        }
        final int age = Period.between(date, now).getYears();
        return age >= min && age <= max;
    }

    private LocalDate toLocalDate(final String date, final String format, final String locale) {
        final var formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(format.replace("y", "u"))
                .optionalStart().appendPattern(" HH:mm:ss").optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(java.util.Locale.forLanguageTag(locale)).withResolverStyle(ResolverStyle.STRICT);
        return java.time.LocalDate.parse(date, formatter);
    }
}
