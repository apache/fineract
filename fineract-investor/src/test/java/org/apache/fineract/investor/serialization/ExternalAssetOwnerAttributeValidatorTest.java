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
package org.apache.fineract.investor.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidValueException;
import org.apache.fineract.investor.service.ExternalAssetOwnerLoanProductAttributeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExternalAssetOwnerAttributeValidatorTest {

    private static final String SETTLEMENT_MODEL = "SETTLEMENT_MODEL";
    private static final String EXCLUDED_TRANSACTION_TYPES = "EXCLUDED_TRANSACTION_TYPES";
    private static final String BUY_DOWN_FEE_AMORTIZATION_STRATEGY = "BUY_DOWN_FEE_AMORTIZATION_STRATEGY";
    private static final String CAPITALIZED_INCOME_AMORTIZATION_STRATEGY = "CAPITALIZED_INCOME_AMORTIZATION_STRATEGY";

    private final ExternalAssetOwnerAttributeValidator underTest = new ExternalAssetOwnerAttributeValidator(new FromJsonHelper(),
            new ExternalAssetOwnerLoanProductAttributeProvider());

    @Test
    void validRequestPasses() {
        assertDoesNotThrow(
                () -> underTest.validateRequest(json(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT"), SETTLEMENT_MODEL, "DELAYED_SETTLEMENT"));
    }

    @Test
    void unsupportedParameterIsRejected() {
        final String body = "{\"attributeKey\":\"SETTLEMENT_MODEL\",\"attributeValue\":\"DELAYED_SETTLEMENT\",\"loanId\":1}";

        final UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> underTest.validateRequest(body, SETTLEMENT_MODEL, "DELAYED_SETTLEMENT"));

        assertEquals(List.of("loanId"), exception.getUnsupportedParameters());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequestFields")
    void invalidRequestFieldIsReported(final String testName, final String attributeKey, final String attributeValue,
            final String expectedErrorCode) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateRequest(json(attributeKey, attributeValue), attributeKey, attributeValue));

        assertEquals(List.of(expectedErrorCode),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
    }

    private static Stream<Arguments> invalidRequestFields() {
        return Stream.of(
                Arguments.of("blank key", " ", "DELAYED_SETTLEMENT", "validation.msg.loanproductattribute.attributeKey.cannot.be.blank"),
                Arguments.of("blank value", SETTLEMENT_MODEL, " ", "validation.msg.loanproductattribute.attributeValue.cannot.be.blank"),
                Arguments.of("key longer than 255", "K".repeat(256), "DELAYED_SETTLEMENT",
                        "validation.msg.loanproductattribute.attributeKey.exceeds.max.length"),
                Arguments.of("value longer than 2000", EXCLUDED_TRANSACTION_TYPES, "V".repeat(2001),
                        "validation.msg.loanproductattribute.attributeValue.exceeds.max.length"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validValues")
    void validValueIsNormalised(final String testName, final String attributeKey, final String attributeValue,
            final String expectedNormalisedValue) {
        assertEquals(expectedNormalisedValue, underTest.validateAndNormalize(attributeKey, attributeValue));
    }

    private static Stream<Arguments> validValues() {
        return Stream.of(Arguments.of("settlement model kept as is", SETTLEMENT_MODEL, "DEFAULT_SETTLEMENT", "DEFAULT_SETTLEMENT"),
                Arguments.of("excluded types trimmed", EXCLUDED_TRANSACTION_TYPES, "REPAYMENT , BUY_DOWN_FEE", "REPAYMENT,BUY_DOWN_FEE"),
                Arguments.of("buy down fee strategy upper-cased", BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "immediate", "IMMEDIATE"),
                Arguments.of("capitalized income strategy upper-cased", CAPITALIZED_INCOME_AMORTIZATION_STRATEGY, "Deferred", "DEFERRED"));
    }

    @Test
    void invalidValueOfKnownKeyIsRejectedWithInvalidAttributeValue() {
        final ExternalAssetOwnerLoanProductAttributeInvalidValueException exception = assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidValueException.class,
                () -> underTest.validateAndNormalize(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "SOMETIMES"));

        assertEquals("error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue", exception.getGlobalisationMessageCode());
    }

    @Test
    void duplicateExcludedTypeIsRejectedWithInvalidAttributeValue() {
        final ExternalAssetOwnerLoanProductAttributeInvalidValueException exception = assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidValueException.class,
                () -> underTest.validateAndNormalize(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT,REPAYMENT"));

        assertEquals("error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue", exception.getGlobalisationMessageCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSettlementAttributes")
    void unknownKeyOrInvalidSettlementModelKeepsTheSettlementAttributeErrorCode(final String testName, final String attributeKey,
            final String attributeValue) {
        final ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException exception = assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException.class,
                () -> underTest.validateAndNormalize(attributeKey, attributeValue));

        assertEquals("error.msg.externalAssetOwnerLoanProductAttribute.invalidSettlementAttribute",
                exception.getGlobalisationMessageCode());
    }

    private static Stream<Arguments> invalidSettlementAttributes() {
        return Stream.of(Arguments.of("unknown key", "UNKNOWN_KEY", "DELAYED_SETTLEMENT"),
                Arguments.of("invalid settlement model", SETTLEMENT_MODEL, "WEEKLY_SETTLEMENT"));
    }

    private static String json(final String attributeKey, final String attributeValue) {
        return "{\"attributeKey\":\"" + attributeKey + "\",\"attributeValue\":\"" + attributeValue + "\"}";
    }
}
