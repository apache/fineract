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
package org.apache.fineract.infrastructure.core.data;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;

class PaginationParametersDataValidatorTest {

    private static final Set<String> SUPPORTED_ORDER_BY = Set.of("id", "name");

    private final PaginationParametersDataValidator validator = new PaginationParametersDataValidator();

    @Test
    void acceptsSupportedOrderByAndSortOrder() {
        final PaginationParameters parameters = PaginationParameters.builder().orderBy("name").sortOrder("desc").build();

        assertThatCode(() -> validator.validateParameterValues(parameters, SUPPORTED_ORDER_BY, "centers")).doesNotThrowAnyException();
    }

    @Test
    void reportsAnUnsupportedOrderByExpressionAsAValidationError() {
        final PaginationParameters parameters = PaginationParameters.builder().orderBy("(select 1)").build();

        assertThatThrownBy(() -> validator.validateParameterValues(parameters, SUPPORTED_ORDER_BY, "centers"))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void reportsAnUnsupportedSortOrderExpressionAsAValidationError() {
        final PaginationParameters parameters = PaginationParameters.builder().orderBy("id").sortOrder("ASC, (select 1)").build();

        assertThatThrownBy(() -> validator.validateParameterValues(parameters, SUPPORTED_ORDER_BY, "centers"))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }
}
