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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.validator.WorkingCapitalNearBreachParseAndValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAmountCalculationStrategy;
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
    @Mock
    private WorkingCapitalNearBreachParseAndValidator workingCapitalNearBreachValidator;

    @BeforeEach
    void setUp() {
        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        validator = new WorkingCapitalLoanProductDataValidator(fromApiJsonHelper, repository, advancedPaymentAllocationsJsonParser,
                paymentAllocationDataValidator, workingCapitalNearBreachValidator);
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
    void testValidateForCreate_WithMinGreaterThanMaxAnnualEir_ShouldThrowException() {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "ANNUAL_EIR");
        jsonObject.remove(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.annualEirParamName, BigDecimal.valueOf(43.7562));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.discountParamName, BigDecimal.valueOf(1000));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.minAnnualEirParamName, BigDecimal.valueOf(50));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.maxAnnualEirParamName, BigDecimal.valueOf(40));

        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(toJsonAndSetupMocks(jsonObject)));
    }

    @Test
    void testValidateForCreate_WithAnnualEirOutsideMinMax_ShouldThrowException() {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "ANNUAL_EIR");
        jsonObject.remove(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.annualEirParamName, BigDecimal.valueOf(10));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.discountParamName, BigDecimal.valueOf(1000));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.minAnnualEirParamName, BigDecimal.valueOf(20));
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.maxAnnualEirParamName, BigDecimal.valueOf(50));

        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(toJsonAndSetupMocks(jsonObject)));
    }

    @Test
    void testValidateForUpdate_WithValidData_ShouldNotThrowException() {
        // Given
        final String json = createValidJson();

        // When & Then
        assertDoesNotThrow(() -> validator.validateForUpdate(json, null));
    }

    @Test
    void testValidateForUpdate_WithEmptyJson_ShouldThrowException() {
        // Given
        final String json = "";

        // When & Then
        assertThrows(InvalidJsonException.class, () -> validator.validateForUpdate(json, null));
    }

    @Test
    void testValidateForUpdate_WithInvalidDateRange_ShouldThrowException() {
        // Given
        final LocalDate startDate = LocalDate.now(ZoneId.systemDefault()).plusDays(10);
        final LocalDate closeDate = LocalDate.now(ZoneId.systemDefault());
        final String json = createJsonWithDates(startDate, closeDate);

        // When & Then
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForUpdate(json, null));
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

    @Test
    void testValidateForCreate_WithInvalidBreachId_ShouldThrowException() {
        final JsonObject jsonObject = createBaseJsonObject();
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.breachIdParamName, 0);
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(jsonObject.toString()));
    }

    @Test
    void paymentAmountStrategy_WithValidInputs_ShouldNotThrow() {
        assertDoesNotThrow(() -> validator.validateForCreate(paymentAmountJson(BigDecimal.valueOf(47.22), BigDecimal.valueOf(1000))));
    }

    @Test
    void paymentAmountStrategy_WithZeroPaymentAmount_ShouldReportNotGreaterThanZero() {
        assertCodes(paymentAmountJson(BigDecimal.ZERO, BigDecimal.valueOf(1000)), WCLP + "paymentAmount.not.greater.than.zero");
    }

    @Test
    void paymentAmountStrategy_WithZeroDiscount_ShouldReportDiscountMustBePositive() {
        assertCodes(paymentAmountJson(BigDecimal.valueOf(47.22), BigDecimal.ZERO),
                WCLP + "discount.must.be.greater.than.zero.for.payment.amount.strategy");
    }

    @Test
    void paymentAmountStrategy_WithBoundsFinerThanTheCurrency_ShouldReportScaleErrors() {
        final JsonObject json = paymentAmountJsonObject(BigDecimal.valueOf(47.22), BigDecimal.valueOf(1000));
        json.addProperty(WorkingCapitalLoanProductConstants.minPaymentAmountParamName, new BigDecimal("40.005"));
        json.addProperty(WorkingCapitalLoanProductConstants.maxPaymentAmountParamName, new BigDecimal("60.0001"));
        assertCodes(json.toString(), WCLP + "minPaymentAmount.scale.is.greater.than.2", WCLP + "maxPaymentAmount.scale.is.greater.than.2");
    }

    @Test
    void paymentAmountStrategy_WithoutPaymentAmount_ShouldReportItMandatory() {
        assertCodes(paymentAmountJson(null, BigDecimal.valueOf(1000)), WCLP + "paymentAmount.cannot.be.blank");
    }

    @Test
    void tpvStrategy_RejectsPaymentAmountAndItsMinMax() {
        assertCodes(tpvJsonWith(WorkingCapitalLoanProductConstants.paymentAmountParamName, BigDecimal.valueOf(47.22)),
                WCLP + "paymentAmount.not.allowed.for.tpv.strategy");
        assertCodes(tpvJsonWith(WorkingCapitalLoanProductConstants.minPaymentAmountParamName, BigDecimal.valueOf(40)),
                WCLP + "minPaymentAmount.not.allowed.for.tpv.strategy");
        assertCodes(tpvJsonWith(WorkingCapitalLoanProductConstants.maxPaymentAmountParamName, BigDecimal.valueOf(60)),
                WCLP + "maxPaymentAmount.not.allowed.for.tpv.strategy");
    }

    @Test
    void annualEirStrategy_RejectsPaymentAmountAndItsMinMax() {
        assertCodes(annualEirJsonWith(WorkingCapitalLoanProductConstants.paymentAmountParamName, BigDecimal.valueOf(47.22)),
                WCLP + "paymentAmount.not.allowed.for.annual.eir.strategy");
        assertCodes(annualEirJsonWith(WorkingCapitalLoanProductConstants.minPaymentAmountParamName, BigDecimal.valueOf(40)),
                WCLP + "minPaymentAmount.not.allowed.for.annual.eir.strategy");
        assertCodes(annualEirJsonWith(WorkingCapitalLoanProductConstants.maxPaymentAmountParamName, BigDecimal.valueOf(60)),
                WCLP + "maxPaymentAmount.not.allowed.for.annual.eir.strategy");
    }

    @Test
    void paymentAmountStrategy_WithNonPositiveMin_ShouldReportNotGreaterThanZero() {
        final JsonObject json = paymentAmountJsonObject(BigDecimal.valueOf(47.22), BigDecimal.valueOf(1000));
        json.addProperty(WorkingCapitalLoanProductConstants.minPaymentAmountParamName, BigDecimal.ZERO);
        assertCodes(json.toString(), WCLP + "minPaymentAmount.not.greater.than.zero");
    }

    @Test
    void paymentAmountStrategy_WithNonPositiveMax_ShouldReportNotGreaterThanZero() {
        final JsonObject json = paymentAmountJsonObject(BigDecimal.valueOf(47.22), BigDecimal.valueOf(1000));
        json.addProperty(WorkingCapitalLoanProductConstants.maxPaymentAmountParamName, BigDecimal.valueOf(-1));
        assertCodes(json.toString(), WCLP + "maxPaymentAmount.not.greater.than.zero",
                WCLP + "paymentAmount.must.be.less.than.or.equal.to.max");
    }

    @Test
    void storedPaymentAmountProduct_UpdatingOnlyTheValue_ShouldNotThrow() {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountParamName, BigDecimal.valueOf(55.00));
        assertDoesNotThrow(() -> validator.validateForUpdate(json.toString(),
                storedProduct(WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, 2)));
    }

    @Test
    void storedPaymentAmountProduct_UpdatingPeriodPaymentRate_ShouldReportNotAllowed() {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, BigDecimal.valueOf(1.0));
        assertUpdateCodes(json, WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, 2,
                WCLP + "periodPaymentRate.not.allowed.for.payment.amount.strategy");
    }

    @Test
    void storedTpvProduct_UpdatingPaymentAmount_ShouldReportNotAllowed() {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountParamName, BigDecimal.valueOf(47.22));
        assertUpdateCodes(json, WorkingCapitalPaymentAmountCalculationStrategy.TPV, 2, WCLP + "paymentAmount.not.allowed.for.tpv.strategy");
    }

    @Test
    void storedPaymentAmountProduct_UpdatingValueFinerThanTheStoredCurrency_ShouldReportScaleError() {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountParamName, BigDecimal.valueOf(47.22));
        assertUpdateCodes(json, WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, 0,
                WCLP + "paymentAmount.scale.is.greater.than.0");
    }

    private static final String WCLP = "validation.msg." + WorkingCapitalLoanProductConstants.WCLP_RESOURCE_NAME + ".";

    private WorkingCapitalLoanProduct storedProduct(final WorkingCapitalPaymentAmountCalculationStrategy strategy,
            final int digitsAfterDecimal) {
        final WorkingCapitalLoanProduct product = mock(WorkingCapitalLoanProduct.class);
        final WorkingCapitalLoanProductRelatedDetail relatedDetail = mock(WorkingCapitalLoanProductRelatedDetail.class);
        lenient().when(relatedDetail.getPaymentAmountCalculationStrategy()).thenReturn(strategy);
        lenient().when(product.getRelatedDetail()).thenReturn(relatedDetail);
        final MonetaryCurrency currency = mock(MonetaryCurrency.class);
        lenient().when(currency.getDigitsAfterDecimal()).thenReturn(digitsAfterDecimal);
        lenient().when(product.getCurrency()).thenReturn(currency);
        return product;
    }

    private void assertUpdateCodes(final JsonObject json, final WorkingCapitalPaymentAmountCalculationStrategy storedStrategy,
            final int storedDigitsAfterDecimal, final String... expectedCodes) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate(json.toString(), storedProduct(storedStrategy, storedDigitsAfterDecimal)));
        assertEquals(List.of(expectedCodes),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList(),
                "expected exactly these validation codes for " + json);
    }

    private JsonObject paymentAmountJsonObject(final BigDecimal paymentAmount, final BigDecimal discount) {
        final JsonObject json = createBaseJsonObject();
        json.remove(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "PAYMENT_AMOUNT");
        if (paymentAmount != null) {
            json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountParamName, paymentAmount);
        }
        if (discount != null) {
            json.addProperty(WorkingCapitalLoanProductConstants.discountParamName, discount);
        }
        return json;
    }

    private String paymentAmountJson(final BigDecimal paymentAmount, final BigDecimal discount) {
        return paymentAmountJsonObject(paymentAmount, discount).toString();
    }

    private String tpvJsonWith(final String parameter, final BigDecimal value) {
        final JsonObject json = createBaseJsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "TPV");
        json.addProperty(parameter, value);
        return json.toString();
    }

    private String annualEirJsonWith(final String parameter, final BigDecimal value) {
        final JsonObject json = createBaseJsonObject();
        json.remove(WorkingCapitalLoanProductConstants.periodPaymentRateParamName);
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "ANNUAL_EIR");
        json.addProperty(WorkingCapitalLoanProductConstants.annualEirParamName, BigDecimal.valueOf(43.7562));
        json.addProperty(WorkingCapitalLoanProductConstants.discountParamName, BigDecimal.valueOf(1000));
        json.addProperty(parameter, value);
        return json.toString();
    }

    private void assertCodes(final String json, final String... expectedCodes) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(json));
        assertEquals(List.of(expectedCodes),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList(),
                "expected exactly these validation codes for " + json);
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
        jsonObject.addProperty(WorkingCapitalLoanProductConstants.accountingRuleParamName, "NONE");
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
