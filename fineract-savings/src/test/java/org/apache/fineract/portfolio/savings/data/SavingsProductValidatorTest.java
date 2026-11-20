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
package org.apache.fineract.portfolio.savings.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.junit.jupiter.api.Test;

class SavingsProductValidatorTest {

    @Test
    void productWithoutLockinPasses() {
        SavingsProduct product = product(null, null);

        assertDoesNotThrow(() -> SavingsProductValidator.validateLockinDetails(product));
    }

    @Test
    void productWithFrequencyAndTypePasses() {
        SavingsProduct product = product(6, SavingsPeriodFrequencyType.MONTHS);

        assertDoesNotThrow(() -> SavingsProductValidator.validateLockinDetails(product));
    }

    @Test
    void frequencyWithoutTypeIsRejected() {
        SavingsProduct product = product(6, null);

        List<ApiParameterError> errors = errors(product);

        assertEquals(1, errors.size());
        assertEquals("lockinPeriodFrequencyType", errors.get(0).getParameterName());
        assertEquals("validation.msg.savingsproduct.lockinPeriodFrequencyType.cannot.be.blank",
                errors.get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void frequencyWithInvalidTypeIsRejected() {
        SavingsProduct product = product(6, SavingsPeriodFrequencyType.INVALID);

        List<ApiParameterError> errors = errors(product);

        assertEquals(1, errors.size());
        assertEquals("lockinPeriodFrequencyType", errors.get(0).getParameterName());
        assertEquals("validation.msg.savingsproduct.lockinPeriodFrequencyType.is.not.within.expected.range",
                errors.get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void typeWithoutFrequencyIsRejected() {
        SavingsProduct product = product(null, SavingsPeriodFrequencyType.MONTHS);

        List<ApiParameterError> errors = errors(product);

        assertEquals(1, errors.size());
        assertEquals("lockinPeriodFrequency", errors.get(0).getParameterName());
        assertEquals("validation.msg.savingsproduct.lockinPeriodFrequency.cannot.be.blank",
                errors.get(0).getUserMessageGlobalisationCode());
    }

    private static SavingsProduct product(Integer lockinPeriodFrequency, SavingsPeriodFrequencyType lockinPeriodFrequencyType) {
        SavingsProduct product = mock(SavingsProduct.class);
        when(product.lockinPeriodFrequency()).thenReturn(lockinPeriodFrequency);
        when(product.lockinPeriodFrequencyType()).thenReturn(lockinPeriodFrequencyType);
        return product;
    }

    private static List<ApiParameterError> errors(SavingsProduct product) {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> SavingsProductValidator.validateLockinDetails(product));
        return exception.getErrors();
    }
}
