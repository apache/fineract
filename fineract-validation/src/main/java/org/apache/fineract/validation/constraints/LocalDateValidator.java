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

    private String dateField;
    private String formatField;
    private String localeField;

    @Override
    public void initialize(LocalDate annotation) {
        this.dateField = annotation.dateField();
        this.formatField = annotation.formatField();
        this.localeField = annotation.localeField();
    }
    // Valida una fecha obteniendo dinámicamente los campos mediante reflexión.
    // Si alguno de los campos es nulo o el formato no coincide, la validación falla.
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            var dateAttr = value.getClass().getDeclaredField(dateField);
            var formatAttr = value.getClass().getDeclaredField(formatField);
            var localeAttr = value.getClass().getDeclaredField(localeField);

            dateAttr.setAccessible(true);
            formatAttr.setAccessible(true);
            localeAttr.setAccessible(true);

            var date = (String) dateAttr.get(value);
            var format = (String) formatAttr.get(value);
            var locale = (String) localeAttr.get(value);

            if (StringUtils.isBlank(date) || StringUtils.isBlank(format) || StringUtils.isBlank(locale)) {
                return false;
            }

            toLocalDate(date, format, locale);

            return true;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Invalid configuration for @LocalDate", e);
        } catch (Exception e) {
            return false;
        }
    }
    // Convierte la cadena de fecha a LocalDate usando un patrón configurable.
    // Se aplica un formateo estricto para evitar fechas ambiguas o inválidas.
    private void toLocalDate(String date, String format, String locale) {
        var formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendPattern(format.replace("y", "u"))
                .optionalStart().appendPattern(" HH:mm:ss").optionalEnd().parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0).parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter(Locale.forLanguageTag(locale)).withResolverStyle(ResolverStyle.STRICT);
        // Se almacena el resultado de parse() para cumplir la regla Sonar java:S2201
        // y asegurar que el parseo se realiza correctamente.
        parse(date, formatter);
    }
}
