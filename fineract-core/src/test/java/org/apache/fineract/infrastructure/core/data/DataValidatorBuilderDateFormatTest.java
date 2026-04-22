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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DataValidatorBuilderDateFormatTest {

    private static final String RESOURCE = "test";
    private static final String PARAMETER = "dateFormat";

    @ParameterizedTest
    @ValueSource(strings = { "dd MMMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy HH:mm:ss" })
    void validDateTimeFormatPatternShouldAcceptValidPatterns(final String pattern) {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(pattern).validDateTimeFormatPattern();
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "02 February 2026", "not-a-pattern", "P!@#$", "{{invalid}}" })
    void validDateTimeFormatPatternShouldRejectInvalidPatterns(final String pattern) {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(pattern).validDateTimeFormatPattern();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getParameterName()).isEqualTo(PARAMETER);
        assertThat(errors.get(0).getDeveloperMessage()).contains("invalid date/time pattern");
    }

    @Test
    void validDateTimeFormatPatternShouldAcceptNullValueWhenIgnoreNullEnabled() {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(null).ignoreIfNull().validDateTimeFormatPattern();
        assertThat(errors).isEmpty();
    }

    @Test
    void validDateTimeFormatPatternShouldNotFailOnNullValue() {
        final List<ApiParameterError> errors = new ArrayList<>();
        // value is null but ignoreIfNull is NOT set — should still not throw NPE
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(null).validDateTimeFormatPattern();
        assertThat(errors).isEmpty();
    }

    @Test
    void validDateTimeFormatPatternShouldNotFailOnBlankValue() {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value("   ").validDateTimeFormatPattern();
        assertThat(errors).isEmpty();
    }
}
