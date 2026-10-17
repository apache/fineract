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

package org.apache.fineract.infrastructure.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;

/**
 * A negative offset ends up as "offset -5" in the SQL, which the database rejects; the request was answered with HTTP
 * 500. It is a client error and is now reported as a validation error (400).
 */
class SearchParametersOffsetTest {

    @Test
    void negativeOffsetOfSearchParametersIsAValidationError() {
        SearchParameters parameters = SearchParameters.builder().offset(-5).limit(5).build();

        assertThatThrownBy(parameters::getOffset).isInstanceOfSatisfying(PlatformApiDataValidationException.class,
                e -> assertThat(e.getErrors().get(0).getUserMessageGlobalisationCode())
                        .isEqualTo("validation.msg.pagination.offset.must.not.be.negative"));
    }

    @Test
    void negativeOffsetOfPaginationParametersIsAValidationError() {
        PaginationParameters parameters = PaginationParameters.builder().paged(true).offset(-1).limit(5).build();

        assertThatThrownBy(parameters::getOffset).isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void zeroPositiveAndMissingOffsetsAreUnchanged() {
        assertThat(SearchParameters.builder().offset(0).build().getOffset()).isZero();
        assertThat(SearchParameters.builder().offset(40).build().getOffset()).isEqualTo(40);
        assertThat(SearchParameters.builder().build().getOffset()).isNull();
        assertThat(PaginationParameters.builder().offset(7).build().getOffset()).isEqualTo(7);
        assertThat(PaginationParameters.builder().build().getOffset()).isNull();
    }
}
