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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DataValidatorBuilderScaleTest {

    private static final String RESOURCE = "test";
    private static final String PARAMETER = "amount";

    @ParameterizedTest
    @CsvSource({ "100, 0", "100.000000, 0", "47.220, 2", "47.22, 2", "12345678901234.56, 2", "1E+7, 0" })
    public void scaleNotGreaterThanShouldAcceptValuesWithinTheScale(final String value, final int scale) {
        assertThat(validate(new BigDecimal(value), scale)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "100.5, 0", "47.225, 2", "12345678901234.567, 2" })
    public void scaleNotGreaterThanShouldRejectValuesFinerThanTheScale(final String value, final int scale) {
        final List<ApiParameterError> errors = validate(new BigDecimal(value), scale);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg." + RESOURCE + "." + PARAMETER + ".scale.is.greater.than." + scale);
    }

    private static List<ApiParameterError> validate(final BigDecimal value, final int scale) {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors).resource(RESOURCE).parameter(PARAMETER).value(value).scaleNotGreaterThan(scale);
        return errors;
    }
}
