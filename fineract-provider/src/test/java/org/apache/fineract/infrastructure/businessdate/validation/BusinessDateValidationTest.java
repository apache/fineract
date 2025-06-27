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
package org.apache.fineract.infrastructure.businessdate.validation;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.validation.config.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.Validator;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = { ValidationConfig.class })
class BusinessDateValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void invalidAllBlank() {
        var request = BusinessDateUpdateRequest.builder().dateFormat("").type(null).date("  ").locale(null).build();

        var errors = validator.validateObject(request);

        assertThat(errors.getFieldErrorCount()).isEqualTo(5);

        assertThat(errors.getFieldErrors()).anyMatch(e -> e.getField().equals("date"));
        assertThat(errors.getFieldErrors()).anyMatch(e -> e.getField().equals("dateFormat"));
        assertThat(errors.getFieldErrors()).anyMatch(e -> e.getField().equals("locale"));
        assertThat(errors.getFieldErrors()).anyMatch(e -> e.getField().equals("type"));
    }

    @Test
    void invalidLocale() {
        var request = BusinessDateUpdateRequest.builder().dateFormat("dd-MM-yyyy").type(BusinessDateType.BUSINESS_DATE).date("12-05-2025")
                .locale("EN").build();

        var errors = validator.validateObject(request);

        assertThat(errors.getFieldErrorCount()).isEqualTo(1);

        assertThat(errors.getFieldErrors()).anyMatch(e -> e.getField().equals("locale"));
    }

    @Test
    void invalidDateFormat() {
        var request = BusinessDateUpdateRequest.builder().dateFormat("dd/MM/yyyy").type(BusinessDateType.BUSINESS_DATE).date("12-05-2025")
                .locale("en").build();

        var errors = validator.validateObject(request);

        assertThat(errors.getErrorCount()).isEqualTo(1);

        assertThat(errors.getAllErrors()).anyMatch(e -> "Wrong local date fields.".equals(e.getDefaultMessage()));
    }

    @Test
    void valid() {
        var request = BusinessDateUpdateRequest.builder().dateFormat("dd-MM-yyyy").type(BusinessDateType.BUSINESS_DATE).date("12-05-2025")
                .locale("en").build();

        var errors = validator.validateObject(request);

        assertThat(errors.getFieldErrorCount()).isEqualTo(0);
    }
}
