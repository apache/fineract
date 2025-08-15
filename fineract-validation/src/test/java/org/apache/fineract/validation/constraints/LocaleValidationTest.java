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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class LocaleValidationTest {

    @Configuration
    @Import({ MessageSourceAutoConfiguration.class })
    static class TestConfig {

        @Bean
        public jakarta.validation.Validator validator() {
            return Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory().getValidator();
        }
    }

    @Autowired
    private Validator validator;

    @Test
    void invalidBlank() {
        var request = LocaleModel.builder().locale(null).build();

        var errors = validator.validate(request);

        assertThat(errors).hasSize(1);

        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("locale"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid-locale", // invalid format
            "xx-YY", // non-existent locale
            "random text", // random text
            "123", // numbers
            "en-US-extra" // extra segment
    })
    void invalidFormats(String locale) {
        var request = LocaleModel.builder().locale(locale).build();
        var errors = validator.validate(request);
        assertThat(errors).as("Expected locale '%s' to be invalid but it was valid", locale).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "en", // language only
            "EN", // uppercase language only
            "en-US", // language with country (hyphen)
            "en_US", // language with country (underscore)
    })
    void validLocales(String locale) {
        var request = LocaleModel.builder().locale(locale).build();
        var errors = validator.validate(request);
        assertThat(errors).as("Expected locale '%s' to be valid but it was invalid", locale).hasSize(0);
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class LocaleModel {

        @Locale
        private String locale;
    }
}
