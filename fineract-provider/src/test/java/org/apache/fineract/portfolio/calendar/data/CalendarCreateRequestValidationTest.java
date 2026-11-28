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
package org.apache.fineract.portfolio.calendar.data;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The cross-field rules (dateFormat/locale required together with a date, frequency required when repeating, ...) are
 * checked by the write service instead; see {@code CalendarWritePlatformServiceJpaRepositoryImplTest}.
 */
class CalendarCreateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static CalendarCreateRequest.CalendarCreateRequestBuilder valid() {
        return CalendarCreateRequest.builder().title("t").startDate("01 January 2024").typeId(1);
    }

    @Test
    void blankTitleIsRejected() {
        CalendarCreateRequest request = valid().title("").build();

        assertThat(propertyPaths(request)).contains("title");
    }

    @Test
    void titleOverFiftyCharactersIsRejected() {
        CalendarCreateRequest request = valid().title("x".repeat(51)).build();

        assertThat(propertyPaths(request)).contains("title");
    }

    @Test
    void descriptionOverOneHundredCharactersIsRejected() {
        CalendarCreateRequest request = valid().description("x".repeat(101)).build();

        assertThat(propertyPaths(request)).contains("description");
    }

    @Test
    void locationOverFiftyCharactersIsRejected() {
        CalendarCreateRequest request = valid().location("x".repeat(51)).build();

        assertThat(propertyPaths(request)).contains("location");
    }

    @Test
    void blankStartDateIsRejected() {
        CalendarCreateRequest request = valid().startDate("").build();

        assertThat(propertyPaths(request)).contains("startDate");
    }

    @Test
    void missingTypeIdIsRejected() {
        CalendarCreateRequest request = valid().typeId(null).build();

        assertThat(propertyPaths(request)).contains("typeId");
    }

    @Test
    void typeIdOutsideOneToFourIsRejected() {
        assertThat(propertyPaths(valid().typeId(0).build())).contains("typeId");
        assertThat(propertyPaths(valid().typeId(5).build())).contains("typeId");
    }

    @Test
    void frequencyOutsideOneToFourIsRejected() {
        assertThat(propertyPaths(valid().frequency(0).build())).contains("frequency");
        assertThat(propertyPaths(valid().frequency(5).build())).contains("frequency");
    }

    @Test
    void nonPositiveIntervalIsRejected() {
        assertThat(propertyPaths(valid().interval(0).build())).contains("interval");
    }

    @Test
    void repeatsOnDayOutsideOneToSevenIsRejected() {
        assertThat(propertyPaths(valid().repeatsOnDay(0).build())).contains("repeatsOnDay");
        assertThat(propertyPaths(valid().repeatsOnDay(8).build())).contains("repeatsOnDay");
    }

    @Test
    void remindByIdOutsideOneToThreeIsRejected() {
        assertThat(propertyPaths(valid().remindById(0).build())).contains("remindById");
        assertThat(propertyPaths(valid().remindById(4).build())).contains("remindById");
    }

    @Test
    void nonPositiveRemindersAreRejected() {
        assertThat(propertyPaths(valid().firstReminder(0).build())).contains("firstReminder");
        assertThat(propertyPaths(valid().secondReminder(0).build())).contains("secondReminder");
    }

    @Test
    void minimalRequestIsValid() {
        assertThat(validator.validate(valid().build())).isEmpty();
    }

    @Test
    void fullyPopulatedRequestIsValid() {
        CalendarCreateRequest request = valid().description("d").location("l").frequency(2).interval(1).repeatsOnDay(3).remindById(1)
                .firstReminder(1).secondReminder(1).dateFormat("dd MMMM yyyy").locale("en").build();

        assertThat(validator.validate(request)).isEmpty();
    }

    private Set<String> propertyPaths(final CalendarCreateRequest request) {
        final Set<ConstraintViolation<CalendarCreateRequest>> violations = validator.validate(request);
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
    }
}
