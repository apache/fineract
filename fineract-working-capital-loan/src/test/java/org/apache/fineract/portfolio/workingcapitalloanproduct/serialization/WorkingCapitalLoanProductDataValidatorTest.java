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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanProductDataValidatorTest {

    @Mock
    private WorkingCapitalLoanProductRepository repository;
    @Mock
    private WorkingCapitalAdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;
    @Mock
    private WorkingCapitalPaymentAllocationDataValidator paymentAllocationDataValidator;
    private WorkingCapitalLoanProductDataValidator validator;

    @BeforeEach
    void setUp() {
        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        validator = new WorkingCapitalLoanProductDataValidator(fromApiJsonHelper, repository, advancedPaymentAllocationsJsonParser,
                paymentAllocationDataValidator);
    }

    @Test
    void testValidateForCreate_WithValidData_ShouldNotThrowException() {
        // Given
        final String json = createValidJson();

        // When & Then
        assertDoesNotThrow(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithEmptyJson_ShouldThrowException() {
        // Given
        final String json = "";

        // When & Then
        assertThrows(InvalidJsonException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithNullJson_ShouldThrowException() {
        // Given
        final String json = null;

        // When & Then
        assertThrows(InvalidJsonException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithMissingName_ShouldThrowException() {
        // Given
        final String json = createJsonWithoutField(WorkingCapitalLoanProductConstants.nameParamName);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithMissingShortName_ShouldThrowException() {
        // Given
        final String json = createJsonWithoutField(WorkingCapitalLoanProductConstants.shortNameParamName);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithMissingCurrencyCode_ShouldThrowException() {
        // Given
        final String json = createJsonWithoutField(WorkingCapitalLoanProductConstants.currencyCodeParamName);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithInvalidAmortization_ShouldThrowException() {
        // Given
        final String json = createJsonWithField(WorkingCapitalLoanProductConstants.amortizationTypeParamName, "INVALID");

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithMinGreaterThanMaxPrincipalAmount_ShouldThrowException() {
        // Given
        final String json = createJsonWithPrincipalAmounts(BigDecimal.valueOf(1000), BigDecimal.valueOf(500), BigDecimal.valueOf(2000));

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithMinGreaterThanMaxPeriodPaymentRate_ShouldThrowException() {
        // Given
        final String json = createJsonWithPeriodPaymentRates(BigDecimal.valueOf(2.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(3.0));

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForUpdate_WithValidData_ShouldNotThrowException() {
        // Given
        final String json = createValidJson();

        // When & Then
        assertDoesNotThrow(() -> validator.validateForUpdate(json));
    }

    @Test
    void testValidateForUpdate_WithEmptyJson_ShouldThrowException() {
        // Given
        final String json = "";

        // When & Then
        assertThrows(InvalidJsonException.class, () -> validator.validateForUpdate(json));
    }

    @Test
    void testValidateForUpdate_WithInvalidDateRange_ShouldThrowException() {
        // Given
        final LocalDate startDate = LocalDate.now(ZoneId.systemDefault()).plusDays(10);
        final LocalDate closeDate = LocalDate.now(ZoneId.systemDefault());
        final String json = createJsonWithDates(startDate, closeDate);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForUpdate(json));
    }

    @Test
    void testValidateConfigurableAttributes_WithUnsupportedAttribute_ShouldThrowException() {
        // Given
        final String json = createJsonWithUnsupportedConfigurableAttribute();

        // When & Then
        assertThrows(UnsupportedParameterException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithDecimalPlaceZero_ShouldNotThrowException() {
        // Given - decimalPlace can be 0 (as per LoanProduct logic: inMinMaxRange(0, 6))
        final String json = createJsonWithDecimalPlace(0);

        // When & Then
        assertDoesNotThrow(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithDecimalPlaceOutOfRange_ShouldThrowException() {
        // Given - decimalPlace must be in range 0-6
        final String json = createJsonWithDecimalPlace(7);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithDecimalPlaceNegative_ShouldThrowException() {
        // Given - decimalPlace must be >= 0
        final String json = createJsonWithDecimalPlace(-1);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithCurrencyInMultiplesOfZero_ShouldNotThrowException() {
        // Given - currencyInMultiplesOf can be 0 (as per LoanProduct logic: integerZeroOrGreater())
        final String json = createJsonWithCurrencyInMultiplesOf(0);

        // When & Then
        assertDoesNotThrow(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithCurrencyInMultiplesOfNegative_ShouldThrowException() {
        // Given - currencyInMultiplesOf must be >= 0
        final String json = createJsonWithCurrencyInMultiplesOf(-1);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_WithCurrencyCodeExceedingLength_ShouldThrowException() {
        // Given - currencyCode must not exceed 3 characters (as per LoanProduct logic)
        final String json = createJsonWithCurrencyCode("USDD");

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(json));
    }

    // Helper methods

    private JsonObject createBaseJsonObject() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.nameParamName, "Test WC Product");
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.shortNameParamName, "TWCP");
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.currencyCodeParamName, "USD");
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, 2);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.inMultiplesOfParamName, 1);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.amortizationTypeParamName, "EIR");
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.npvDayCountParamName, 360);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.principalParamName, 1000);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, 1.0);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.repaymentEveryParamName, 30);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, "DAYS");
        jsonObject.add(WorkingCapitalLoanProductConstants.paymentAllocationParamName, createDefaultPaymentAllocationJson());
        return jsonObject;
    }

    private JsonArray createDefaultPaymentAllocationJson() {
        final JsonArray paymentAllocation = new JsonArray();
        final JsonObject rule = new JsonObject();
        rule.addProperty("transactionType", "DEFAULT");
        final JsonArray order = new JsonArray();
        order.add(createPaymentAllocationOrderItem("PENALTY", 1));
        order.add(createPaymentAllocationOrderItem("FEE", 2));
        order.add(createPaymentAllocationOrderItem("PRINCIPAL", 3));
        rule.add("paymentAllocationOrder", order);
        paymentAllocation.add(rule);
        return paymentAllocation;
    }

    private JsonObject createPaymentAllocationOrderItem(String paymentAllocationRule, int order) {
        final JsonObject item = new JsonObject();
        item.addProperty("paymentAllocationRule", paymentAllocationRule);
        item.addProperty("order", order);
        return item;
    }

    private String toJsonAndSetupMocks(final JsonObject jsonObject) {
        return jsonObject.toString();
    }

    private String createValidJson() {
        return toJsonAndSetupMocks(createBaseJsonObject());
    }

    private String createJsonWithoutField(final String fieldName) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.remove(fieldName);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithField(final String fieldName, final String value) {
        final JsonObject jsonObject = createBaseJsonObject();
        if (fieldName.equals(WorkingCapitalLoanProductConstants.amortizationTypeParamName) && value.equals("FLAT")) {
            jsonObject.addProperty(WorkingCapitalLoanProductConstants.amortizationTypeParamName, "FLAT");
        }
        jsonObject.addProperty(fieldName, value);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithPrincipalAmounts(final BigDecimal min, final BigDecimal defaultVal, final BigDecimal max) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.minPrincipalParamName, min);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.principalParamName, defaultVal);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.maxPrincipalParamName, max);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithPeriodPaymentRates(final BigDecimal min, final BigDecimal defaultVal, final BigDecimal max) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.minPeriodPaymentRateParamName, min);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, defaultVal);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.maxPeriodPaymentRateParamName, max);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithDates(final LocalDate startDate, final LocalDate closeDate) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.startDateParamName, startDate.toString());
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.closeDateParamName, closeDate.toString());
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithUnsupportedConfigurableAttribute() {
        final JsonObject jsonObject = createBaseJsonObject();
        final JsonObject allowOverrides = new JsonObject();
        allowOverrides.addProperty("unsupportedAttribute", true);
        jsonObject.add(WorkingCapitalLoanProductConstants.allowAttributeOverridesParamName, allowOverrides);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithDecimalPlace(final Integer decimalPlace) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.digitsAfterDecimalParamName, decimalPlace);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithCurrencyInMultiplesOf(final Integer currencyInMultiplesOf) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.inMultiplesOfParamName, currencyInMultiplesOf);
        return toJsonAndSetupMocks(jsonObject);
    }

    private String createJsonWithCurrencyCode(final String currencyCode) {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.currencyCodeParamName, currencyCode);
        return toJsonAndSetupMocks(jsonObject);
    }
}
