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
@ContextConfiguration(classes = { AgeValidatorTest.TestConfig.class })
public class AgeValidatorTest {

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
    void validAllBlank_sendsValidationToNextValidator() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("").dob("  ").locale(null).build();
        final var errors = validator.validate(request);
        assertThat(errors).isEmpty();
    }

    @Test
    void blankLocale_sendsValidationToNextValidator() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("dd-MM-yyyy").dob("12-05-2025").locale("").build();
        final var errors = validator.validate(request);
        assertThat(errors).isEmpty();
    }

    @Test
    void invalidDateFormat() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("dd/MM/yyyy").dob("12-05-2025").locale("en").build();
        final var errors = validator.validate(request);
        assertThat(errors).hasSize(1);
    }

    @Test
    void valid_happyPath() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("dd MMMM yyyy").dob("01" + " January 2000").locale("en")
                .build();
        final var errors = validator.validate(request);
        assertThat(errors).isEmpty();
    }

    @Test
    void invalidLowerAgeRange() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("dd MMMM yyyy").dob("01" + " January 2025").locale("en")
                .build();
        final var errors = validator.validate(request);
        assertThat(errors).hasSize(1);
    }

    @Test
    void invalidHigherAgeRange() {
        final var request = AgeValidatorTest.ValidAgeModel.builder().dateFormat("dd MMMM yyyy").dob("01" + " January 1900").locale("en")
                .build();
        final var errors = validator.validate(request);
        assertThat(errors).hasSize(1);
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidAge(dateField = "dob", formatField = "dateFormat", localeField = "locale", min = 15, max = 75, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.dob.validAge}")
    static class ValidAgeModel {

        private String dob;
        private String dateFormat;
        private String locale;
    }
}
