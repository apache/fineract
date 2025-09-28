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
package org.apache.fineract.portfolio.loanaccount.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.api.request.ReAgePreviewRequest;
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
@ContextConfiguration(classes = { ReAgePreviewRequestValidationTest.TestConfig.class })
class ReAgePreviewRequestValidationTest {

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
        var params = ReAgePreviewRequest.builder().build();

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(6);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyNumber"));
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyType"));
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("startDate"));
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("numberOfInstallments"));
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("dateFormat"));
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("locale"));
    }

    @Test
    void invalidFrequencyNumberNull() {
        var params = validParams();
        params.setFrequencyNumber(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyNumber"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'frequencyNumber' is mandatory."));
    }

    @Test
    void invalidFrequencyNumberZero() {
        var params = validParams();
        params.setFrequencyNumber(0);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyNumber"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'frequencyNumber' must be at least 1."));
    }

    @Test
    void invalidFrequencyNumberNegative() {
        var params = validParams();
        params.setFrequencyNumber(-1);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyNumber"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'frequencyNumber' must be at least 1."));
    }

    @Test
    void invalidFrequencyTypeNull() {
        var params = validParams();
        params.setFrequencyType(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyType"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'frequencyType' is mandatory."));
    }

    @Test
    void invalidFrequencyTypeEmpty() {
        var params = validParams();
        params.setFrequencyType("");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyType"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'frequencyType' is mandatory."));
    }

    @Test
    void invalidFrequencyTypeBlank() {
        var params = validParams();
        params.setFrequencyType("  ");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyType"));
    }

    @Test
    void invalidFrequencyTypeInvalidEnum() {
        var params = validParams();
        params.setFrequencyType("NOT_A_VALID_ENUM");

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("frequencyType"));
        assertThat(errors).anyMatch(e -> e.getMessage()
                .equals("The parameter 'frequencyType' must be valid PeriodFrequencyType value. Provided value: 'NOT_A_VALID_ENUM'."));
    }

    @Test
    void invalidStartDateNull() {
        var params = validParams();
        params.setStartDate(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("startDate"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'startDate' is mandatory."));
    }

    @Test
    void invalidStartDateEmpty() {
        var params = validParams();
        params.setStartDate("");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("startDate"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'startDate' is mandatory."));
    }

    @Test
    void invalidStartDateBlank() {
        var params = validParams();
        params.setStartDate("  ");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("startDate"));
    }

    @Test
    void invalidStartDateFormat() {
        var params = validParams();
        params.setStartDate("2025-05-12");

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getMessage().equals("Wrong local date fields."));
    }

    @Test
    void invalidNumberOfInstallmentsNull() {
        var params = validParams();
        params.setNumberOfInstallments(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("numberOfInstallments"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'numberOfInstallments' is mandatory."));
    }

    @Test
    void invalidNumberOfInstallmentsZero() {
        var params = validParams();
        params.setNumberOfInstallments(0);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("numberOfInstallments"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'numberOfInstallments' must be at least 1."));
    }

    @Test
    void invalidNumberOfInstallmentsNegative() {
        var params = validParams();
        params.setNumberOfInstallments(-1);

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("numberOfInstallments"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'numberOfInstallments' must be at least 1."));
    }

    @Test
    void invalidDateFormatNull() {
        var params = validParams();
        params.setDateFormat(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("dateFormat"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'dateFormat' is mandatory."));
    }

    @Test
    void invalidDateFormatEmpty() {
        var params = validParams();
        params.setDateFormat("");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("dateFormat"));
    }

    @Test
    void invalidDateFormatMismatch() {
        var params = validParams();
        params.setDateFormat("yyyy-MM-dd");

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getMessage().equals("Wrong local date fields."));
    }

    @Test
    void invalidLocaleNull() {
        var params = validParams();
        params.setLocale(null);

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("locale"));
        assertThat(errors).anyMatch(e -> e.getMessage().equals("The parameter 'locale' is mandatory."));
    }

    @Test
    void invalidLocaleEmpty() {
        var params = validParams();
        params.setLocale("");

        var errors = validator.validate(params);

        assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("locale"));
    }

    @Test
    void invalidLocaleInvalidFormat() {
        var params = validParams();
        params.setLocale("invalid-locale");

        var errors = validator.validate(params);

        assertThat(errors).hasSize(1);
        assertThat(errors).anyMatch(e -> e.getPropertyPath().toString().equals("locale"));
    }

    @Test
    void validAllParams() {
        var params = validParams();

        var errors = validator.validate(params);

        assertThat(errors).isEmpty();
    }

    @Test
    void validParamsWithDaysFrequency() {
        var params = validParams();
        params.setFrequencyType("DAYS");
        params.setFrequencyNumber(30);

        var errors = validator.validate(params);

        assertThat(errors).isEmpty();
    }

    @Test
    void validParamsWithWeeksFrequency() {
        var params = validParams();
        params.setFrequencyType("WEEKS");
        params.setFrequencyNumber(4);

        var errors = validator.validate(params);

        assertThat(errors).isEmpty();
    }

    @Test
    void validParamsWithYearsFrequency() {
        var params = validParams();
        params.setFrequencyType("YEARS");
        params.setFrequencyNumber(1);

        var errors = validator.validate(params);

        assertThat(errors).isEmpty();
    }

    @Test
    void validParamsWithAlternativeLocale() {
        var params = validParams();
        params.setLocale("en_US");

        var errors = validator.validate(params);

        assertThat(errors).isEmpty();
    }

    private ReAgePreviewRequest validParams() {
        return ReAgePreviewRequest.builder().frequencyNumber(1).frequencyType("MONTHS").startDate("12-05-2025").numberOfInstallments(6)
                .dateFormat("dd-MM-yyyy").locale("en").build();
    }

}
