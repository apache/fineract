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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = { DateFormatValidationTest.TestConfig.class })
class DateFormatValidationTest {

    @Configuration
    @Import({ MessageSourceAutoConfiguration.class })
    static class TestConfig {

        @Bean
        public Validator validator() {
            return Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory().getValidator();
        }
    }

    @Autowired
    private Validator validator;

    @Test
    void blankIsValid() {
        var request = DateFormatModel.builder().dateFormat("").build();
        var errors = validator.validate(request);
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "02 February 2026", // literal date, not a pattern
            "invalid", "dd bbb yyyy", // 'b' is not a valid pattern letter
    })
    void invalidPatterns(String dateFormat) {
        var request = DateFormatModel.builder().dateFormat(dateFormat).build();
        var errors = validator.validate(request);
        assertThat(errors).as("Expected dateFormat '%s' to be invalid", dateFormat).hasSize(1);
        assertThat(errors).anyMatch(e -> "dateFormat".equals(e.getPropertyPath().toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "dd MMMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MMM dd, yyyy" })
    void validPatterns(String dateFormat) {
        var request = DateFormatModel.builder().dateFormat(dateFormat).build();
        var errors = validator.validate(request);
        assertThat(errors).as("Expected dateFormat '%s' to be valid", dateFormat).isEmpty();
    }

    @Test
    void staticIsValidPattern_invalid() {
        assertThat(DateFormatValidator.isValidPattern("02 February 2026")).isFalse();
        assertThat(DateFormatValidator.isValidPattern("unknown")).isFalse();
    }

    @Test
    void staticIsValidPattern_valid() {
        assertThat(DateFormatValidator.isValidPattern("dd MMMM yyyy")).isTrue();
        assertThat(DateFormatValidator.isValidPattern("yyyy-MM-dd")).isTrue();
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class DateFormatModel {

        @DateFormat
        private String dateFormat;
    }
}
