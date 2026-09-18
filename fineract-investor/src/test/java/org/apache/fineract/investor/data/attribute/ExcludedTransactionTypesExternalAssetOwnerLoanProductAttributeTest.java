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
package org.apache.fineract.investor.data.attribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ExcludedTransactionTypesExternalAssetOwnerLoanProductAttributeTest {

    private final ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute underTest = new ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute();

    @Test
    public void testAttributeKeyIsExcludedTransactionTypes() {
        assertEquals("EXCLUDED_TRANSACTION_TYPES", underTest.getAttributeKey());
    }

    @Test
    public void testAttributeIsMultiValue() {
        assertTrue(underTest.isMultiValue());
    }

    @Test
    public void testAttributeValuesContainEveryLoanTransactionTypeExceptInvalid() {
        List<String> expected = Arrays.stream(LoanTransactionType.values()).filter(type -> !LoanTransactionType.INVALID.equals(type))
                .map(Enum::name).toList();

        List<String> actual = underTest.getAttributeValues();

        assertEquals(expected, actual);
        assertFalse(actual.contains(LoanTransactionType.INVALID.name()));
    }

    @ParameterizedTest
    @MethodSource("validAttributeValues")
    public void testValidValuesAreAcceptedAndNormalized(String testName, String attributeValue, String expectedNormalizedValue) {
        assertTrue(underTest.validate(attributeValue), testName);
        assertEquals(expectedNormalizedValue, underTest.normalize(attributeValue), testName);
    }

    private static Stream<Arguments> validAttributeValues() {
        return Stream.of(Arguments.of("single value", "BUY_DOWN_FEE", "BUY_DOWN_FEE"), //
                Arguments.of("the buy down fee family", //
                        "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT", //
                        "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT"), //
                Arguments.of("mixed case and padding", "buy_down_fee, BUY_DOWN_FEE_ADJUSTMENT", "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT"), //
                Arguments.of("surrounding whitespace", "  BUY_DOWN_FEE  ", "BUY_DOWN_FEE"), //
                Arguments.of("order is preserved", "REPAYMENT,ACCRUAL", "REPAYMENT,ACCRUAL"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", ",", "BUY_DOWN_FEE,", ",BUY_DOWN_FEE", "BUY_DOWN_FEE,,ACCRUAL", "BUY_DOWN_FEE,BUY_DOWN_FEE",
            "buy_down_fee,BUY_DOWN_FEE", "BUY_DOWN_FEE,NOT_A_TYPE", "NOT_A_TYPE", "INVALID", "BUY_DOWN_FEE;ACCRUAL" })
    public void testInvalidValuesAreRejected(String attributeValue) {
        assertFalse(underTest.validate(attributeValue));
    }
}
