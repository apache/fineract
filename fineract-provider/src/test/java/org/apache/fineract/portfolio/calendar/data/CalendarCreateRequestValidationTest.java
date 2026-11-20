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
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CalendarCreateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void missingDateFormatAndLocaleAreRejected() {
        CalendarCreateRequest request = CalendarCreateRequest.builder().title("t").startDate("01 January 2024").typeId(1).build();

        Set<ConstraintViolation<CalendarCreateRequest>> violations = validator.validate(request);

        Set<String> invalidProperties = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
        assertThat(invalidProperties).contains("dateFormat", "locale");
    }

    @Test
    void missingTypeIdIsRejected() {
        CalendarCreateRequest request = CalendarCreateRequest.builder().title("t").startDate("01 January 2024").dateFormat("dd MMMM yyyy")
                .locale("en").build();

        Set<ConstraintViolation<CalendarCreateRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("typeId"));
    }

    @Test
    void nonPositiveTypeIdIsRejected() {
        CalendarCreateRequest request = CalendarCreateRequest.builder().title("t").startDate("01 January 2024").dateFormat("dd MMMM yyyy")
                .locale("en").typeId(0).build();

        Set<ConstraintViolation<CalendarCreateRequest>> violations = validator.validate(request);

        assertThat(violations).anySatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("typeId"));
    }

    @Test
    void fullyPopulatedRequestIsValid() {
        CalendarCreateRequest request = CalendarCreateRequest.builder().title("t").startDate("01 January 2024").dateFormat("dd MMMM yyyy")
                .locale("en").typeId(1).build();

        assertThat(validator.validate(request)).isEmpty();
    }
}
