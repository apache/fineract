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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanProductValidationTest {

    private WorkingCapitalLoanProductHelper wclProductHelper;

    @BeforeEach
    public void setup() {
        this.wclProductHelper = new WorkingCapitalLoanProductHelper();
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingName() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(null).build();
        request.setName(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [name] The parameter `name` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingShortName() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withShortName(null).build();
        request.setShortName(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("validation") || exception.getMessage().contains("required")
                || exception.getDeveloperMessage() != null);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingCurrencyCode() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withCurrencyCode(null).build();
        request.setCurrencyCode(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [currencyCode] The parameter `currencyCode` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingDecimalPlace() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withDecimalPlace(null).build();
        request.setDigitsAfterDecimal(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [digitsAfterDecimal] The parameter `digitsAfterDecimal` is mandatory.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidDecimalPlace() {
        // Given - decimalPlace must be in range 0-6 (as per LoanProduct logic)
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withDecimalPlace(-1).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [digitsAfterDecimal] The parameter `digitsAfterDecimal` must be between 0 and 6.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDecimalPlaceOutOfRange() {
        // Given - decimalPlace must be in range 0-6
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withDecimalPlace(7).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [digitsAfterDecimal] The parameter `digitsAfterDecimal` must be between 0 and 6.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingCurrencyInMultiplesOf() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withCurrencyInMultiplesOf(null)
                .build();
        request.setInMultiplesOf(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [inMultiplesOf] The parameter `inMultiplesOf` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidCurrencyInMultiplesOf() {
        // Given - currencyInMultiplesOf must be >= 0 (as per LoanProduct logic)
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withCurrencyInMultiplesOf(-1)
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [inMultiplesOf] The parameter `inMultiplesOf` must be zero or greater.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidAmortization() {
        // Given - Missing amortizationType (null) - API should return 400
        final PostWorkingCapitalLoanProductsRequest baseForNull = new WorkingCapitalLoanProductTestBuilder().withAmortizationType("EIR")
                .build();
        final PostWorkingCapitalLoanProductsRequest[] requestNullHolder = new PostWorkingCapitalLoanProductsRequest[1];
        try {
            final Field field = PostWorkingCapitalLoanProductsRequest.class.getDeclaredField("amortizationType");
            field.setAccessible(true);
            field.set(baseForNull, null);
            requestNullHolder[0] = baseForNull;
        } catch (final Exception e) {
            final PostWorkingCapitalLoanProductsRequest tempRequest = new PostWorkingCapitalLoanProductsRequest();
            tempRequest.setName("Test Product");
            tempRequest.setShortName("TP");
            tempRequest.setCurrencyCode("USD");
            tempRequest.setDigitsAfterDecimal(2);
            tempRequest.setInMultiplesOf(1);
            tempRequest.setNpvDayCount(360);
            tempRequest.setPrincipal(BigDecimal.valueOf(10000));
            tempRequest.setPeriodPaymentRate(BigDecimal.valueOf(1.0));
            tempRequest.setRepaymentEvery(30);
            tempRequest.setRepaymentFrequencyType(PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.DAYS);
            tempRequest.setLocale("en");
            tempRequest.setDateFormat("yyyy-MM-dd");
            requestNullHolder[0] = tempRequest;
        }

        final CallFailedRuntimeException exceptionNull = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(requestNullHolder[0]));
        assertEquals(400, exceptionNull.getStatus());
        assertNotNull(exceptionNull.getDeveloperMessage());
        assertEquals("Validation errors: [amortizationType] The parameter `amortizationType` is mandatory.",
                exceptionNull.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingNpvDayCount() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withNpvDayCount(null).build();
        request.setNpvDayCount(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [npvDayCount] The parameter `npvDayCount` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidNpvDayCount() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withNpvDayCount(0).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [npvDayCount] The parameter `npvDayCount` must be greater than 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingPrincipalAmountDefault() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withPrincipalAmountDefault(null)
                .build();
        request.setPrincipal(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] The parameter `principal` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithNegativePrincipalAmountDefault() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withPrincipalAmountDefault(BigDecimal.valueOf(-100)).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] The parameter `principal` must be greater than 0.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithZeroPrincipalAmountDefault() {
        // Given - principal must be > 0 (positiveAmount, not zeroOrPositiveAmount)
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withPrincipalAmountDefault(BigDecimal.ZERO).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] The parameter `principal` must be greater than 0.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidFundId() {
        // Given - fundId must be > 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withFundId(-1L).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [fundId] The parameter `fundId` must be greater than 0.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidDelinquencyBucket() {
        // Given - delinquencyBucket must be > 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withDelinquencyBucketId(-1L)
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [delinquencyBucketId] The parameter `delinquencyBucketId` must be greater than 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithNegativeDiscountDefault() {
        // Given - discount must be >= 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withDiscount(BigDecimal.valueOf(-1)).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [discount] The parameter `discount` must be greater than or equal to 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithZeroDiscountDefault() {
        // Given - discount can be 0 (zeroOrPositiveAmount)
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDiscount(BigDecimal.ZERO).build();

        // When & Then - Should succeed (0 is valid)
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request).getResourceId();
        assertTrue(productId != null && productId > 0);

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidPrincipalAmountMin() {
        // Given - minPrincipal must be > 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withPrincipalAmountMin(BigDecimal.ZERO).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [minPrincipal] The parameter `minPrincipal` must be greater than 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidPrincipalAmountMax() {
        // Given - maxPrincipal must be > 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withPrincipalAmountMax(BigDecimal.ZERO).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals(
                "Validation errors: [maxPrincipal] The parameter `maxPrincipal` must be greater than 0.; [principal] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithZeroPeriodPaymentRateMin() {
        // Given - minPeriodPaymentRate can be 0 (zeroOrPositiveAmount)
        // Note: periodPaymentRate must be >= min, so we set it to 0 as well
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withMinPeriodPaymentRate(BigDecimal.ZERO).withPeriodPaymentRate(BigDecimal.ZERO).build();

        // When & Then - Should succeed (0 is valid for rate)
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request).getResourceId();
        assertTrue(productId != null && productId > 0);

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithZeroPeriodPaymentRateMax() {
        // Given - maxPeriodPaymentRate can be 0 (zeroOrPositiveAmount)
        // Note: periodPaymentRate must be <= max, so we set it to 0 as well
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withMaxPeriodPaymentRate(BigDecimal.ZERO).withPeriodPaymentRate(BigDecimal.ZERO).build();

        // When & Then - Should succeed (0 is valid for rate)
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request).getResourceId();
        assertTrue(productId != null && productId > 0);

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithNegativePeriodPaymentRateMin() {
        // Given - minPeriodPaymentRate must be >= 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withMinPeriodPaymentRate(BigDecimal.valueOf(-1)).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [minPeriodPaymentRate] The parameter `minPeriodPaymentRate` must be greater than or equal to 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithNegativePeriodPaymentRateMax() {
        // Given - maxPeriodPaymentRate must be >= 0 if provided
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(-1)).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals(
                "Validation errors: [maxPeriodPaymentRate] The parameter `maxPeriodPaymentRate` must be greater than or equal to 0.; [periodPaymentRate] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingPeriodPaymentRateDefault() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withPeriodPaymentRate(null)
                .build();
        request.setPeriodPaymentRate(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] The parameter `periodPaymentRate` is mandatory.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithNegativePeriodPaymentRateDefault() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder()
                .withPeriodPaymentRate(BigDecimal.valueOf(-1.0)).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] The parameter `periodPaymentRate` must be greater than or equal to 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingPeriodPaymentFrequency() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withRepaymentEvery(null).build();
        request.setRepaymentEvery(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [repaymentEvery] The parameter `repaymentEvery` is mandatory.", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidPeriodPaymentFrequency() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withRepaymentEvery(0).build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [repaymentEvery] The parameter `repaymentEvery` must be greater than 0.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingPeriodPaymentFrequencyType() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withRepaymentFrequencyType(null)
                .build();
        request.setRepaymentFrequencyType(null); // Explicitly set to null

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [repaymentFrequencyType] The parameter `repaymentFrequencyType` is mandatory.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMissingPaymentAllocationTypes() {
        // Given
        final PostWorkingCapitalLoanProductsRequest baseRequest = new WorkingCapitalLoanProductTestBuilder().build();
        // Set paymentAllocation with empty paymentAllocationOrder
        final PostWorkingCapitalLoanProductsRequest request;
        try {
            final ObjectMapper objectMapper = ObjectMapperFactory.getShared();
            final String requestJson = objectMapper.writeValueAsString(baseRequest);
            final ObjectNode requestNode = (ObjectNode) objectMapper.readTree(requestJson);
            final ArrayNode paymentAllocationArray = objectMapper.createArrayNode();
            final ObjectNode paymentAllocationNode = objectMapper.createObjectNode();
            paymentAllocationNode.put("transactionType", "DEFAULT");
            paymentAllocationNode.set("paymentAllocationOrder", objectMapper.createArrayNode()); // Empty array
            paymentAllocationArray.add(paymentAllocationNode);
            requestNode.set("paymentAllocation", paymentAllocationArray);
            request = objectMapper.treeToValue(requestNode, PostWorkingCapitalLoanProductsRequest.class);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to set paymentAllocation with empty paymentAllocationOrder", e);
        }

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [id] Payment allocation order cannot be empty", exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithInvalidPaymentAllocationType() {
        // Given
        final PostWorkingCapitalLoanProductsRequest baseRequest = new WorkingCapitalLoanProductTestBuilder().build();
        // Set paymentAllocation with invalid allocation type
        final PostWorkingCapitalLoanProductsRequest request;
        try {
            final ObjectMapper objectMapper = ObjectMapperFactory.getShared();
            final String requestJson = objectMapper.writeValueAsString(baseRequest);
            final ObjectNode requestNode = (ObjectNode) objectMapper.readTree(requestJson);
            final ArrayNode paymentAllocationArray = objectMapper.createArrayNode();
            final ObjectNode paymentAllocationNode = objectMapper.createObjectNode();
            paymentAllocationNode.put("transactionType", "DEFAULT");
            final ArrayNode paymentAllocationOrderArray = objectMapper.createArrayNode();
            final ObjectNode orderItem = objectMapper.createObjectNode();
            orderItem.put("paymentAllocationRule", "INVALID_TYPE");
            orderItem.put("order", 1);
            paymentAllocationOrderArray.add(orderItem);
            paymentAllocationNode.set("paymentAllocationOrder", paymentAllocationOrderArray);
            paymentAllocationArray.add(paymentAllocationNode);
            requestNode.set("paymentAllocation", paymentAllocationArray);
            request = objectMapper.treeToValue(requestNode, PostWorkingCapitalLoanProductsRequest.class);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to set paymentAllocation with invalid type", e);
        }

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals(
                "Validation errors: [id] Each provided payment allocation must contain exactly 3 allocation rules, but 1 were provided",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMinGreaterThanMaxPrincipalAmount() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(500)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(2000)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDefaultLessThanMinPrincipalAmount() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(500)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(2000)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDefaultGreaterThanMaxPrincipalAmount() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(3000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(2000)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [principal] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithMinGreaterThanMaxPeriodPaymentRate() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withMinPeriodPaymentRate(BigDecimal.valueOf(2.0)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(1.0)) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(3.0)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDefaultLessThanMinPeriodPaymentRate() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withMinPeriodPaymentRate(BigDecimal.valueOf(1.0)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(0.5)) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2.0)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDefaultGreaterThanMaxPeriodPaymentRate() {
        // Given
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withMinPeriodPaymentRate(BigDecimal.valueOf(0.5)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(3.0)) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2.0)) //
                .build();

        // When & Then - Should throw CallFailedRuntimeException with status 400
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request));
        assertEquals(400, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                exception.getDeveloperMessage());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDuplicateName() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName1 = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final String uniqueShortName2 = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request1 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName1).build();
        final PostWorkingCapitalLoanProductsRequest request2 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName2).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request1).getResourceId();

        // When & Then - Duplicate name is a domain rule violation (403)
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request2));
        assertEquals(403, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertTrue(exception.getDeveloperMessage().contains("Validation errors: [id] Working Capital Loan Product with name"));
        assertTrue(exception.getDeveloperMessage().contains("already exists"));

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDuplicateShortName() {
        // Given
        final String uniqueName1 = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueName2 = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String shortName = Utils.uniqueRandomStringGenerator("", 4);
        final PostWorkingCapitalLoanProductsRequest request1 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName1)
                .withShortName(shortName).build();
        final PostWorkingCapitalLoanProductsRequest request2 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName2)
                .withShortName(shortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request1).getResourceId();

        // When & Then - Duplicate shortName is a domain rule violation (403)
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request2));
        assertEquals(403, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertTrue(exception.getDeveloperMessage().contains("Validation errors: [id] Working Capital Loan Product with short name"));
        assertTrue(exception.getDeveloperMessage().contains("already exists"));

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithDuplicateExternalId() {
        // Given
        final String uniqueName1 = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueName2 = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName1 = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final String uniqueShortName2 = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final String externalId = UUID.randomUUID().toString();
        final PostWorkingCapitalLoanProductsRequest request1 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName1)
                .withShortName(uniqueShortName1).withExternalId(externalId).build();
        final PostWorkingCapitalLoanProductsRequest request2 = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName2)
                .withShortName(uniqueShortName2).withExternalId(externalId).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request1).getResourceId();

        // When & Then - Duplicate externalId is a domain rule violation (403)
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.createWorkingCapitalLoanProduct(request2));
        assertEquals(403, exception.getStatus());
        assertNotNull(exception.getDeveloperMessage());
        assertTrue(exception.getDeveloperMessage().contains("Validation errors: [id] Working Capital Loan Product with external id"));
        assertTrue(exception.getDeveloperMessage().contains("already exists"));

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testUpdateWorkingCapitalLoanProductWithInvalidDateRange() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest createRequest = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(createRequest).getResourceId();

        // When - update with invalid date range (startDate > closeDate)
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).buildUpdateRequest();
        updateRequest.setStartDate("2025-12-31");
        updateRequest.setCloseDate("2025-01-01");

        // Then - Invalid date range is a domain rule violation (400 or 403)
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> wclProductHelper.updateWorkingCapitalLoanProductById(productId, updateRequest));
        assertTrue(exception.getStatus() == 400);
        assertNotNull(exception.getDeveloperMessage());
        assertEquals("Validation errors: [closeDate] Failed data validation due to: must.be.after.startDate.",
                exception.getDeveloperMessage());

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }
}
