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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetDelinquencyBucket;
import org.apache.fineract.client.models.GetDelinquencyRange;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanProductCRUDTest {

    private WorkingCapitalLoanProductHelper wclProductHelper;

    @BeforeEach
    public void setup() {
        this.wclProductHelper = new WorkingCapitalLoanProductHelper();
    }

    @Test
    public void testCreateWorkingCapitalLoanProduct() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).build();

        // When
        final PostWorkingCapitalLoanProductsResponse response = wclProductHelper.createWorkingCapitalLoanProduct(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        final Long productId = response.getResourceId();
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveWorkingCapitalLoanProductById() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).build();
        final PostWorkingCapitalLoanProductsResponse createResponse = wclProductHelper.createWorkingCapitalLoanProduct(request);
        final Long productId = createResponse.getResourceId();

        // When
        final GetWorkingCapitalLoanProductsProductIdResponse response = wclProductHelper.retrieveWorkingCapitalLoanProductById(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getId());
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveWorkingCapitalLoanProductByExternalId() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final String externalId = UUID.randomUUID().toString();
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withExternalId(externalId).build();
        final PostWorkingCapitalLoanProductsResponse createResponse = wclProductHelper.createWorkingCapitalLoanProduct(request);
        final Long productId = createResponse.getResourceId();

        // When
        final GetWorkingCapitalLoanProductsProductIdResponse response = wclProductHelper
                .retrieveWorkingCapitalLoanProductByExternalId(externalId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getId());
        assertEquals(externalId, response.getExternalId());
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveAllWorkingCapitalLoanProducts() {
        // Given
        final String uniqueId1 = UUID.randomUUID().toString().substring(0, 8);
        final String uniqueId2 = UUID.randomUUID().toString().substring(0, 8);
        final PostWorkingCapitalLoanProductsRequest request1 = new WorkingCapitalLoanProductTestBuilder()
                .withName("wcl Product 1 " + uniqueId1).withShortName("W1" + uniqueId1.substring(0, 2)).build();
        final PostWorkingCapitalLoanProductsRequest request2 = new WorkingCapitalLoanProductTestBuilder()
                .withName("wcl Product 2 " + uniqueId2).withShortName("W2" + uniqueId2.substring(0, 2)).build();
        final Long productId1 = wclProductHelper.createWorkingCapitalLoanProduct(request1).getResourceId();
        final Long productId2 = wclProductHelper.createWorkingCapitalLoanProduct(request2).getResourceId();

        // When
        final List<GetWorkingCapitalLoanProductsResponse> response = wclProductHelper.retrieveAllWorkingCapitalLoanProducts();

        // Then
        assertNotNull(response);
        assertTrue(response.size() >= 2);
        assertTrue(response.stream().anyMatch(p -> {
            assertNotNull(p.getId());
            return p.getId().equals(productId1);
        }));
        assertTrue(response.stream().anyMatch(p -> {
            assertNotNull(p.getId());
            return p.getId().equals(productId2);
        }));

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId1);
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId2);
    }

    @Test
    public void testRetrieveTemplate() {
        // When
        final GetWorkingCapitalLoanProductsTemplateResponse response = wclProductHelper.retrieveTemplate();

        // Then
        assertNotNull(response);
        // Verify options are present
        assertNotNull(response.getCurrencyOptions());
        assertFalse(response.getCurrencyOptions().isEmpty(), "Currency options should not be empty");
        assertNotNull(response.getAmortizationTypeOptions());
        assertFalse(response.getAmortizationTypeOptions().isEmpty(), "Amortization type options should not be empty");
        assertNotNull(response.getPeriodFrequencyTypeOptions());
        assertFalse(response.getPeriodFrequencyTypeOptions().isEmpty(), "Period frequency type options should not be empty");
        assertNotNull(response.getAdvancedPaymentAllocationTypes());
        assertFalse(response.getAdvancedPaymentAllocationTypes().isEmpty(), "Payment allocation type options should not be empty");
        assertNotNull(response.getAdvancedPaymentAllocationTransactionTypes());
        assertFalse(response.getAdvancedPaymentAllocationTransactionTypes().isEmpty(),
                "Payment allocation transaction type options should not be empty");
        // Verify payment allocation types contain expected values
        final List<String> expectedPaymentAllocationTypes = List.of("PENALTY", "FEE", "PRINCIPAL");
        final List<String> actualPaymentAllocationTypes = response.getAdvancedPaymentAllocationTypes().stream()
                .map(StringEnumOptionData::getCode).toList();
        assertTrue(actualPaymentAllocationTypes.containsAll(expectedPaymentAllocationTypes),
                "Payment allocation types should contain all expected types");
        assertEquals(3, actualPaymentAllocationTypes.size(), "Payment allocation types should have exactly 3 types");
    }

    @Test
    public void testUpdateWorkingCapitalLoanProduct() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest createRequest = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(createRequest).getResourceId();

        // When
        final String updatedName = "Updated wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder().withName(updatedName)
                .withShortName(uniqueShortName).buildUpdateRequest();
        final PutWorkingCapitalLoanProductsProductIdResponse updateResponse = wclProductHelper
                .updateWorkingCapitalLoanProductById(productId, updateRequest);

        // Then
        assertNotNull(updateResponse);
        assertEquals(productId, updateResponse.getResourceId());
        final GetWorkingCapitalLoanProductsProductIdResponse retrieved = wclProductHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertEquals(updatedName, retrieved.getName());
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testUpdateWorkingCapitalLoanProductByExternalId() {
        // Given
        final String externalId = UUID.randomUUID().toString();
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest createRequest = new WorkingCapitalLoanProductTestBuilder().withExternalId(externalId)
                .withName(uniqueName).withShortName(uniqueShortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(createRequest).getResourceId();

        // When
        final String updatedName = "Updated wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder().withName(updatedName)
                .withShortName(uniqueShortName).buildUpdateRequest();
        final PutWorkingCapitalLoanProductsProductIdResponse updateResponse = wclProductHelper
                .updateWorkingCapitalLoanProductByExternalId(externalId, updateRequest);

        // Then
        assertNotNull(updateResponse);
        assertEquals(productId, updateResponse.getResourceId());
        final GetWorkingCapitalLoanProductsProductIdResponse retrieved = wclProductHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertEquals(updatedName, retrieved.getName());
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testDeleteWorkingCapitalLoanProduct() {
        // Given
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request).getResourceId();

        // When
        final DeleteWorkingCapitalLoanProductsProductIdResponse response = wclProductHelper.deleteWorkingCapitalLoanProductById(productId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getResourceId());
    }

    @Test
    public void testDeleteWorkingCapitalLoanProductByExternalId() {
        // Given
        final String externalId = UUID.randomUUID().toString();
        final String uniqueName = "Test wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = "TW" + UUID.randomUUID().toString().substring(0, 2);
        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withExternalId(externalId)
                .withName(uniqueName).withShortName(uniqueShortName).build();
        final Long productId = wclProductHelper.createWorkingCapitalLoanProduct(request).getResourceId();

        // When
        final DeleteWorkingCapitalLoanProductsProductIdResponse response = wclProductHelper
                .deleteWorkingCapitalLoanProductByExternalId(externalId);

        // Then
        assertNotNull(response);
        assertEquals(productId, response.getResourceId());
    }

    @Test
    public void testCreateWorkingCapitalLoanProductWithAllFields() {
        // Given
        final String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        final String externalId = UUID.randomUUID().toString();
        final List<String> paymentAllocationTypes = List.of("PENALTY", "FEE", "PRINCIPAL");
        final HashMap<String, Boolean> allowAttributeOverrides = new HashMap<>();
        allowAttributeOverrides.put("amortizationType", true);
        allowAttributeOverrides.put("interestType", false);

        // Get fund and delinquency bucket from template
        final GetWorkingCapitalLoanProductsTemplateResponse template = wclProductHelper.retrieveTemplate();
        Long fundId = null;
        if (template.getFundOptions() != null && !template.getFundOptions().isEmpty()) {
            fundId = template.getFundOptions().getFirst().getId();
        }
        Long delinquencyBucketId = null;
        if (template.getDelinquencyBucketOptions() != null && !template.getDelinquencyBucketOptions().isEmpty()) {
            final GetDelinquencyBucket firstBucket = template.getDelinquencyBucketOptions().getFirst();
            if (firstBucket != null && firstBucket.getId() != null) {
                delinquencyBucketId = firstBucket.getId();
            }
        }

        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                .withName("Full wcl Product " + uniqueId) //
                .withShortName("FW" + uniqueId.substring(0, 2)) //
                .withDescription("Full description") //
                .withExternalId(externalId) //
                .withFundId(fundId) //
                .withAmortizationType("EIR") //
                .withFlatPercentageAmount(BigDecimal.valueOf(5.0)) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withNpvDayCount(365) //
                .withPaymentAllocationTypes(paymentAllocationTypes) //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(5000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(10000)) //
                .withMinPeriodPaymentRate(BigDecimal.valueOf(0.5)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(1.0)) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2.0)) //
                .withDiscount(BigDecimal.valueOf(0.1)) //
                .withRepaymentEvery(60) //
                .withRepaymentFrequencyType("DAYS") //
                .withAllowAttributeOverrides(allowAttributeOverrides) //
                .build();

        // When
        final PostWorkingCapitalLoanProductsResponse response = wclProductHelper.createWorkingCapitalLoanProduct(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        final Long productId = response.getResourceId();
        final GetWorkingCapitalLoanProductsProductIdResponse retrieved = wclProductHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(retrieved.getName());
        assertTrue(retrieved.getName().startsWith("Full wcl Product"));
        assertEquals(externalId, retrieved.getExternalId());
        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testHappyPath_CreateAndRetrieve_VerifyAllFields() {
        // Given - Create product with ALL possible fields
        final String productName = "Happy Path wcl Product " + UUID.randomUUID().toString().substring(0, 8);
        final String shortName = "HP" + UUID.randomUUID().toString().substring(0, 2);
        final String externalId = UUID.randomUUID().toString();
        final String description = "Comprehensive test product with all fields";
        final List<String> paymentAllocationTypes = List.of("PENALTY", "FEE", "PRINCIPAL");

        // Get fund and delinquency bucket from template
        final GetWorkingCapitalLoanProductsTemplateResponse template = wclProductHelper.retrieveTemplate();
        Long fundId = null;
        String fundName = null;
        if (template.getFundOptions() != null && !template.getFundOptions().isEmpty()) {
            fundId = template.getFundOptions().getFirst().getId();
            fundName = template.getFundOptions().getFirst().getName();
        }
        Long delinquencyBucketId = null;
        final GetDelinquencyBucket expectedBucket = template.getDelinquencyBucketOptions() != null
                && !template.getDelinquencyBucketOptions().isEmpty() ? template.getDelinquencyBucketOptions().getFirst() : null;
        if (expectedBucket != null) {
            delinquencyBucketId = expectedBucket.getId();
        }

        // All configurable attributes
        final HashMap<String, Boolean> allowAttributeOverrides = new HashMap<>();
        allowAttributeOverrides.put("flatPercentageAmount", true);
        allowAttributeOverrides.put("delinquencyBucketClassification", false);
        allowAttributeOverrides.put("discountDefault", true);
        allowAttributeOverrides.put("periodPaymentFrequency", false);
        allowAttributeOverrides.put("periodPaymentFrequencyType", true);

        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder() //
                // Details category
                .withName(productName) //
                .withShortName(shortName) //
                .withDescription(description) //
                .withExternalId(externalId) //
                .withFundId(fundId) //
                // Currency category
                .withCurrencyCode("USD") //
                .withDecimalPlace(2) //
                .withCurrencyInMultiplesOf(1) //
                // Settings category
                .withAmortizationType("FLAT") //
                .withFlatPercentageAmount(BigDecimal.valueOf(5.5)) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withNpvDayCount(365) //
                .withPaymentAllocationTypes(paymentAllocationTypes) //
                // Term category
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(5000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(10000)) //
                .withMinPeriodPaymentRate(BigDecimal.valueOf(0.5)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(1.0)) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2.0)) //
                .withDiscount(BigDecimal.valueOf(0.1)) //
                .withRepaymentEvery(30) //
                .withRepaymentFrequencyType("DAYS") //
                // Configurable attributes
                .withAllowAttributeOverrides(allowAttributeOverrides) //
                .build();

        // When - Create product
        final PostWorkingCapitalLoanProductsResponse createResponse = wclProductHelper.createWorkingCapitalLoanProduct(request);
        assertNotNull(createResponse);
        assertNotNull(createResponse.getResourceId());
        final Long productId = createResponse.getResourceId();
        assertTrue(productId > 0);

        // Then - Retrieve and verify ALL fields
        final GetWorkingCapitalLoanProductsProductIdResponse retrieved = wclProductHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(retrieved);

        // Verify Details category
        assertEquals(productId, retrieved.getId());
        assertEquals(productName, retrieved.getName());
        assertEquals(shortName, retrieved.getShortName());
        assertEquals(externalId, retrieved.getExternalId());
        assertEquals(description, retrieved.getDescription());
        if (fundId != null) {
            assertEquals(fundId, retrieved.getFundId());
            assertEquals(fundName, retrieved.getFundName());
        }
        if (expectedBucket != null) {
            assertNotNull(retrieved.getDelinquencyBucket(), "delinquencyBucket");
            assertEquals(expectedBucket.getId(), retrieved.getDelinquencyBucket().getId(), "delinquencyBucket.id");
            assertEquals(expectedBucket.getName(), retrieved.getDelinquencyBucket().getName(), "delinquencyBucket.name");
            if (expectedBucket.getRanges() != null && !expectedBucket.getRanges().isEmpty()) {
                assertNotNull(retrieved.getDelinquencyBucket().getRanges(), "delinquencyBucket.ranges");
                assertEquals(expectedBucket.getRanges().size(), retrieved.getDelinquencyBucket().getRanges().size(),
                        "delinquencyBucket.ranges.size");
                for (int i = 0; i < expectedBucket.getRanges().size(); i++) {
                    final GetDelinquencyRange expectedRange = expectedBucket.getRanges().get(i);
                    final GetDelinquencyRange actualRange = retrieved.getDelinquencyBucket().getRanges().get(i);
                    assertEquals(expectedRange.getId(), actualRange.getId());
                    assertEquals(expectedRange.getClassification(), actualRange.getClassification());
                    assertEquals(expectedRange.getMinimumAgeDays(), actualRange.getMinimumAgeDays());
                    assertEquals(expectedRange.getMaximumAgeDays(), actualRange.getMaximumAgeDays());
                }
            }
        }

        // Verify Currency category
        assertNotNull(retrieved.getCurrency());
        assertEquals("USD", retrieved.getCurrency().getCode());
        assertEquals(2, retrieved.getCurrency().getDecimalPlaces());
        assertEquals(1, retrieved.getCurrency().getInMultiplesOf());

        // Verify Settings category
        assertNotNull(retrieved.getAmortizationType());
        assertEquals("FLAT", retrieved.getAmortizationType().getCode());
        if (retrieved.getFlatPercentageAmount() != null) {
            assertEquals(0, BigDecimal.valueOf(5.5).compareTo(retrieved.getFlatPercentageAmount()));
        }
        assertEquals(365, retrieved.getNpvDayCount());

        // Verify Payment Allocation (if present)
        if (retrieved.getPaymentAllocation() != null && !retrieved.getPaymentAllocation().isEmpty()) {
            assertFalse(retrieved.getPaymentAllocation().isEmpty());
            final var paymentAllocation = retrieved.getPaymentAllocation().getFirst();
            assertNotNull(paymentAllocation.getTransactionType());
            assertNotNull(paymentAllocation.getPaymentAllocationOrder());
            assertFalse(paymentAllocation.getPaymentAllocationOrder().isEmpty());
            // Verify that paymentAllocationOrder contains the expected allocation types
            final List<String> expectedTypes = List.of("PENALTY", "FEE", "PRINCIPAL");
            final List<String> actualTypes = paymentAllocation.getPaymentAllocationOrder().stream()
                    .map(PaymentAllocationOrder::getPaymentAllocationRule).filter(Objects::nonNull).toList();
            assertTrue(actualTypes.containsAll(expectedTypes) || actualTypes.containsAll(paymentAllocationTypes));
        }

        // Verify Term category
        if (retrieved.getMinPrincipal() != null) {
            assertEquals(0, BigDecimal.valueOf(1000).compareTo(retrieved.getMinPrincipal()));
        }
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(retrieved.getPrincipal()));
        if (retrieved.getMaxPrincipal() != null) {
            assertEquals(0, BigDecimal.valueOf(10000).compareTo(retrieved.getMaxPrincipal()));
        }
        if (retrieved.getMinPeriodPaymentRate() != null) {
            assertEquals(0, BigDecimal.valueOf(0.5).compareTo(retrieved.getMinPeriodPaymentRate()));
        }
        assertEquals(0, BigDecimal.valueOf(1.0).compareTo(retrieved.getPeriodPaymentRate()));
        if (retrieved.getMaxPeriodPaymentRate() != null) {
            assertEquals(0, BigDecimal.valueOf(2.0).compareTo(retrieved.getMaxPeriodPaymentRate()));
        }
        if (retrieved.getDiscount() != null) {
            assertEquals(0, BigDecimal.valueOf(0.1).compareTo(retrieved.getDiscount()));
        }
        assertEquals(30, retrieved.getRepaymentEvery());
        assertNotNull(retrieved.getRepaymentFrequencyType());
        assertEquals("DAYS", retrieved.getRepaymentFrequencyType().getCode());

        // Verify Configurable Attributes (allowAttributeOverrides)
        if (retrieved.getAllowAttributeOverrides() != null) {
            // Configurable attributes
            assertEquals(Boolean.TRUE, retrieved.getAllowAttributeOverrides().getFlatPercentageAmount());
            assertEquals(Boolean.FALSE, retrieved.getAllowAttributeOverrides().getDelinquencyBucketClassification());
            assertEquals(Boolean.TRUE, retrieved.getAllowAttributeOverrides().getDiscountDefault());
            assertEquals(Boolean.FALSE, retrieved.getAllowAttributeOverrides().getPeriodPaymentFrequency());
            assertEquals(Boolean.TRUE, retrieved.getAllowAttributeOverrides().getPeriodPaymentFrequencyType());
        }

        wclProductHelper.deleteWorkingCapitalLoanProductById(productId);
    }
}
