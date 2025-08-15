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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = { LocalDateValidationTest.TestConfig.class })
class LocalDateValidationTest {

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
    void invalidAllBlank() {
        var request = LocalDateModel.builder().format("").date("  ").locale(null).build();

        var errors = validator.validate(request);

        assertThat(errors).hasSize(1);

        assertThat(errors).allMatch(e -> "Wrong local date fields.".equals(e.getMessage()));
    }

    @Test
    void invalidLocaleFormat() {
        var request = LocalDateModel.builder().format("dd-MM-yyyy").date("12-05-2025").locale("").build();

        var errors = validator.validate(request);

        assertThat(errors).hasSize(1);

        assertThat(errors).allMatch(e -> "Wrong local date fields.".equals(e.getMessage()));
    }

    @Test
    void invalidDateFormat() {
        var request = LocalDateModel.builder().format("dd/MM/yyyy").date("12-05-2025").locale("en").build();

        var errors = validator.validate(request);

        assertThat(errors).hasSize(1);

        assertThat(errors).allMatch(e -> "Wrong local date fields.".equals(e.getMessage()));
    }

    @Test
    void valid() {
        var request = LocalDateModel.builder().format("dd-MM-yyyy").date("12-05-2025").locale("en").build();

        var errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @LocalDate(dateField = "date", formatField = "format", localeField = "locale")
    static class LocalDateModel {

        private String date;
        private String format;
        private String locale;
    }
}
