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

public class DataValidatorBuilderBooleanValueTest {

    private static final String RESOURCE = "test";
    private static final String PARAMETER = "active";

    @ParameterizedTest
    @ValueSource(strings = { "true", "false", "TRUE", " false " })
    public void validateForBooleanValueShouldAcceptBooleanValues(final String value) {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(value).validateForBooleanValue();
        assertThat(errors).isEmpty();
    }

    @Test
    public void validateForBooleanValueShouldRejectNonBooleanValue() {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value("yes").validateForBooleanValue();
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg." + RESOURCE + "." + PARAMETER + ".value.should.true.or.false");
    }

    @Test
    public void validateForBooleanValueShouldNotFailOnNullValue() {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(null).validateForBooleanValue();
        assertThat(errors).isEmpty();
    }

    @Test
    public void notBlankFollowedByValidateForBooleanValueShouldReportOnlyTheBlankErrorForNullValue() {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(null).notBlank().validateForBooleanValue();
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg." + RESOURCE + "." + PARAMETER + ".cannot.be.blank");
    }
}
