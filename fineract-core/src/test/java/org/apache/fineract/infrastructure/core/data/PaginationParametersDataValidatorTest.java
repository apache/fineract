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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;

class PaginationParametersDataValidatorTest {

    private final PaginationParametersDataValidator validator = new PaginationParametersDataValidator();

    @Test
    void rejectsNegativeOffset() {
        PaginationParameters parameters = PaginationParameters.builder().offset(-5).limit(5).build();

        assertThatThrownBy(() -> validator.validateParameterValues(parameters, Set.of(), "test"))
                .isInstanceOf(PlatformApiDataValidationException.class)
                .satisfies(ex -> assertThat(((PlatformApiDataValidationException) ex).getErrors())
                        .extracting(ApiParameterError::getUserMessageGlobalisationCode)
                        .contains("validation.msg.pagination.offset.must.not.be.negative"));
    }

    @Test
    void acceptsZeroPositiveAndMissingOffset() {
        PaginationParameters zeroOffset = PaginationParameters.builder().offset(0).limit(5).build();
        PaginationParameters positiveOffset = PaginationParameters.builder().offset(10).limit(5).build();
        PaginationParameters missingOffset = PaginationParameters.builder().limit(5).build();

        validator.validateParameterValues(zeroOffset, Set.of(), "test");
        validator.validateParameterValues(positiveOffset, Set.of(), "test");
        validator.validateParameterValues(missingOffset, Set.of(), "test");
    }
}
