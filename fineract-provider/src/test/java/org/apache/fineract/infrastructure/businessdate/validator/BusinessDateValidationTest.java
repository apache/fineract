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
package org.apache.fineract.infrastructure.businessdate.validator;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = { ValidationConfig.class })
public class BusinessDateValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void shouldFailValidationWhenFieldsAreBlankOrNull() {
        BusinessDateUpdateRequest request = BusinessDateUpdateRequest.builder().dateFormat("").type(null).date("  ").locale(null).build();

        Set<ConstraintViolation<BusinessDateUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(4);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dateFormat"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("date"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("locale"));
    }

    @Test
    void shouldFailValidationWhenLocaleHasWrongFormat() {
        BusinessDateUpdateRequest request = BusinessDateUpdateRequest.builder().dateFormat("dd-MM-yyyy")
                .type(BusinessDateType.BUSINESS_DATE).date("12-05-2025").locale("EN").build();

        Set<ConstraintViolation<BusinessDateUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("locale"));
    }

    @Test
    void shouldFailValidationWhenDateFormatNotCorrect() {
        BusinessDateUpdateRequest request = BusinessDateUpdateRequest.builder().dateFormat("dd/MM/yyyy")
                .type(BusinessDateType.BUSINESS_DATE).date("12-05-2025").locale("en").build();

        Set<ConstraintViolation<BusinessDateUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreValid() {
        BusinessDateUpdateRequest request = BusinessDateUpdateRequest.builder().dateFormat("dd-MM-yyyy")
                .type(BusinessDateType.BUSINESS_DATE).date("12-05-2025").locale("en").build();

        Set<ConstraintViolation<BusinessDateUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
