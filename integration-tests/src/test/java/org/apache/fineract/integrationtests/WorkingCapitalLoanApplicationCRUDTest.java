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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsWorkingCapitalLoanAccounts;
import org.apache.fineract.client.models.GetConfigurableAttributes;
import org.apache.fineract.client.models.GetWorkingCapitalLoanBreach;
import org.apache.fineract.client.models.GetWorkingCapitalLoanNearBreach;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdTimeline;
import org.apache.fineract.client.models.GetWorkingCapitalLoansPagedResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansTemplateResponse;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalNearBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanApplicationCRUDTest {

    private static Long delinquencyBucketId;
    private static Long fundId;

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
    private final WorkingCapitalNearBreachHelper nearBreachHelper = new WorkingCapitalNearBreachHelper();

    @BeforeAll
    static void initDelinquency() {
        delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        fundId = FundsResourceHandler.createFund().getResourceId();
    }

    @Test
    public void testSubmitWorkingCapitalLoanApplication() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest();

        final Long loanId = applicationHelper.submit(json);

        assertNotNull(loanId);
        assertTrue(loanId > 0);
        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Submit loan without overridable params (repaymentEvery, repaymentFrequencyType, discount, delinquencyBucketId).
     * Asserts that the created loan has these values taken from the product.
     */
    @Test
    public void testSubmitWithoutOverridableParamsUsesProductDefaults() {
        final Integer productRepaymentEvery = 1;
        final String productRepaymentFrequencyType = "MONTHS";
        final BigDecimal productDiscount = BigDecimal.valueOf(15);
        final Long productId = createProductWithKnownDefaults(productRepaymentEvery, productRepaymentFrequencyType, productDiscount);
        final Long clientId = createClient();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(5500);
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);

        // Submit with only mandatory fields — no repaymentEvery, repaymentFrequencyType,
        // discount, delinquencyBucketId
        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .buildSubmitRequest();

        final Long loanId = applicationHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
        assertNotNull(data);

        assertEquals(productRepaymentEvery.intValue(), data.getRepaymentEvery(), "repaymentEvery should come from product");
        assertNotNull(data.getRepaymentFrequencyType());
        assertEquals(productRepaymentFrequencyType, data.getRepaymentFrequencyType().getValue());
        assertNull(data.getProposedDiscountFee());
        assertNotNull(data.getDelinquencyBucket(), "delinquencyBucket should come from product");
        assertEquals(delinquencyBucketId.longValue(), data.getDelinquencyBucket().getId(), "delinquencyBucket.id should come from product");

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithoutBreachAndNearBreachParamsUsesProductBreachDefaults() {
        final String breachName = Utils.randomStringGenerator("Breach", 20);
        final Integer breachFrequency = 30;
        final String breachFrequencyType = "DAYS";
        final String breachAmountCalculationType = "PERCENTAGE";
        final BigDecimal breachAmount = BigDecimal.valueOf(10);
        final Long breachId = createBreach(breachName, breachFrequency, breachFrequencyType, breachAmountCalculationType, breachAmount);
        final String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final Long nearBreachId = nearBreachHelper.create(new WorkingCapitalNearBreachRequest() //
                .nearBreachName(nearBreachName) //
                .nearBreachFrequency(breachFrequency - 10) //
                .nearBreachFrequencyType(breachFrequencyType)//
                .nearBreachThreshold(BigDecimal.valueOf(30.0)))//
                .getResourceId(); //
        final Long productId = createProductWithBreachAndNearBreach(breachId, nearBreachId, Boolean.FALSE);
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest();

        final Long loanId = applicationHelper.submit(json);
        final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);

        final GetWorkingCapitalLoanBreach breach = data.getBreach();
        assertNotNull(breach);
        assertEquals(breachName, breach.getName());
        assertEquals(breachFrequency.intValue(), breach.getBreachFrequency());
        assertNotNull(breach.getBreachFrequencyType());
        assertEquals(breachFrequencyType, breach.getBreachFrequencyType().getValue());
        assertNotNull(breach.getBreachAmountCalculationType());
        assertEquals(breachAmountCalculationType, breach.getBreachAmountCalculationType().getCode());
        assertEquals(0, breachAmount.compareTo(breach.getBreachAmount()));
        final GetWorkingCapitalLoanNearBreach nearBreach = data.getNearBreach();
        assertNotNull(nearBreach);
        assertEquals(nearBreachName, nearBreach.getName());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        breachHelper.delete(breachId);
        nearBreachHelper.delete(nearBreachId);
    }

    @Test
    public void testNegativeSubmitWithBreachAndNearBreachParams() {
        final String breachName = Utils.randomStringGenerator("Breach", 20);
        final Integer breachFrequency = 30;
        final String breachFrequencyType = "DAYS";
        final String breachAmountCalculationType = "PERCENTAGE";
        final BigDecimal breachAmount = BigDecimal.valueOf(10);
        final Long breachId = createBreach(breachName, breachFrequency, breachFrequencyType, breachAmountCalculationType, breachAmount);
        final Long productId = createProductWithBreachAndNearBreach(breachId, null, Boolean.TRUE);
        final Long clientId = createClient();
        final String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final Long nearBreachId = nearBreachHelper.create(new WorkingCapitalNearBreachRequest() //
                .nearBreachName(nearBreachName) //
                .nearBreachFrequency(breachFrequency + 10) //
                .nearBreachFrequencyType(breachFrequencyType)//
                .nearBreachThreshold(BigDecimal.valueOf(30.0))) //
                .getResourceId();//

        final var json1 = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withBreachId(null) //
                .withNearBreachId(nearBreachId) //
                .buildSubmitRequest();
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applicationHelper.submit(json1));

        // Then
        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains("cannot.enable.near.breach.without.breach");

        final var json2 = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withBreachId(breachId) //
                .withNearBreachId(nearBreachId) //
                .buildSubmitRequest();
        exception = assertThrows(CallFailedRuntimeException.class, () -> applicationHelper.submit(json2));

        // Then
        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains("near.breach.frequency.must.be.lower.than.breach.frequency");

        productHelper.deleteWorkingCapitalLoanProductById(productId);
        breachHelper.delete(breachId);
        nearBreachHelper.delete(nearBreachId);
    }

    @Test
    public void testRetrieveWorkingCapitalLoanByIdWithAllFieldsVerified() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final String accountNo = "wcl-get-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId = "wcl-get-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(6000);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(100000);
        final BigDecimal discount = BigDecimal.valueOf(25);
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(fundId) //
                .withAccountNo(accountNo) //
                .withExternalId(externalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withPaymentAllocationTypes(
                        List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_FEE", "IN_ADVANCE_PENALTY")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitRequest());

        final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
        assertNotNull(data);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPaymentVolume, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testClientAccountsIncludeWorkingCapitalLoans() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String accountNo = "wcl-acc-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(3000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(3150)) //
                .withAccountNo(accountNo) //
                .buildSubmitRequest());

        assertNotNull(loanId);

        GetClientsClientIdAccountsResponse clientAccounts = ClientHelper.getClientAccounts(clientId);
        assertNotNull(clientAccounts);
        assertFalse(clientAccounts.getWorkingCapitalLoanAccounts().isEmpty(), "Client should have at least one working capital loan");
        boolean found = false;
        for (GetClientsWorkingCapitalLoanAccounts el : clientAccounts.getWorkingCapitalLoanAccounts()) {
            if (el.getId().equals(loanId)) {
                found = true;
                assertEquals(accountNo, el.getAccountNo());
                break;
            }
        }
        assertTrue(found, "workingCapitalLoanAccounts should contain loan id " + loanId);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Verifies that loanCycle in client account summary is set per client: first WCL loan gets 1, second 2, third 3.
     * (WCL implementation: loanCycle = count of existing WC loans for this client + 1 at creation.)
     */
    @Test
    public void testWorkingCapitalLoanCycleInClientAccountSummary() {
        final Long productId = createProduct();
        final Long clientId = createClient();

        final Long loanId1 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(1000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(1100)) //
                .buildSubmitRequest());
        final Long loanId2 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(2000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(2200)) //
                .buildSubmitRequest());
        final Long loanId3 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(3000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(3300)) //
                .buildSubmitRequest());

        assertNotNull(loanId1);
        assertNotNull(loanId2);
        assertNotNull(loanId3);

        try {
            GetClientsClientIdAccountsResponse clientAccounts = ClientHelper.getClientAccounts(clientId);
            assertNotNull(clientAccounts);
            assertTrue(clientAccounts.getWorkingCapitalLoanAccounts().size() >= 3, "Client should have at least 3 working capital loans");
            final List<GetClientsWorkingCapitalLoanAccounts> ourLoans = new ArrayList<>();
            for (GetClientsWorkingCapitalLoanAccounts el : clientAccounts.getWorkingCapitalLoanAccounts()) {
                if (el.getId().equals(loanId1) || el.getId().equals(loanId2) || el.getId().equals(loanId3)) {
                    ourLoans.add(el);
                }
            }

            assertEquals(3, ourLoans.size(), "Should find exactly 3 WCL loans for this client");
            ourLoans.sort(Comparator.comparing(GetClientsWorkingCapitalLoanAccounts::getId));

            assertEquals(1, ourLoans.get(0).getLoanCycle(), "First WCL loan should have loanCycle 1");
            assertEquals(2, ourLoans.get(1).getLoanCycle(), "Second WCL loan should have loanCycle 2");
            assertEquals(3, ourLoans.get(2).getLoanCycle(), "Third WCL loan should have loanCycle 3");
        } finally {
            applicationHelper.deleteById(loanId1);
            applicationHelper.deleteById(loanId2);
            applicationHelper.deleteById(loanId3);
            productHelper.deleteWorkingCapitalLoanProductById(productId);
        }
    }

    @Test
    public void testRetrieveWorkingCapitalLoanByExternalIdWithAllFieldsVerified() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final String accountNo = "wcl-extget-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId = "wcl-by-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(7000);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(100000);
        final BigDecimal discount = BigDecimal.ZERO;
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(10);
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(fundId) //
                .withAccountNo(accountNo) //
                .withExternalId(externalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withPaymentAllocationTypes(
                        List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_FEE", "IN_ADVANCE_PENALTY")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitRequest());

        final GetWorkingCapitalLoansLoanIdResponse response = applicationHelper.retrieveByExternalId(externalId);
        assertNotNull(response);
        final Long loanId = response.getId();

        assertAllLoanFieldsInResponse(response, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPaymentVolume, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveTemplate() {
        // Ensure at least one product exists so productOptions is not empty
        final Long productId = createProduct();
        try {
            final GetWorkingCapitalLoansTemplateResponse template = applicationHelper.retrieveTemplateRaw(Map.of());

            assertNotNull(template);
            assertNotNull(template.getProductOptions());
            assertFalse(template.getProductOptions().isEmpty());
            final GetWorkingCapitalLoanProductsResponse firstProduct = template.getProductOptions().getFirst();
            assertNotNull(firstProduct.getId());
            assertTrue(firstProduct.getId() > 0);
            assertNotNull(firstProduct.getName());
            assertFalse(firstProduct.getName().isBlank());
            assertNotNull(firstProduct.getShortName());
            assertFalse(firstProduct.getShortName().isBlank());

            assertNotNull(template.getFundOptions());
            assertFalse(template.getFundOptions().isEmpty());
            final var firstFund = template.getFundOptions().getFirst();
            assertNotNull(firstFund.getId());
            assertTrue(firstFund.getId() > 0);
            assertNotNull(firstFund.getName());
            assertFalse(firstFund.getName().isBlank());
        } finally {
            productHelper.deleteWorkingCapitalLoanProductById(productId);
        }
    }

    @Test
    public void testRetrieveTemplateWithProductId() {
        final String productName = "WCL Template Product " + UUID.randomUUID().toString().substring(0, 8);
        final String shortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName(productName).withShortName(shortName).build()).getResourceId();
        assertNotNull(productId);

        final GetWorkingCapitalLoansTemplateResponse template = applicationHelper.retrieveTemplateRaw(Map.of("productId", productId));

        assertNotNull(template);
        assertNotNull(template.getProductOptions());
        assertFalse(template.getProductOptions().isEmpty());

        final GetWorkingCapitalLoansLoanIdResponse loanData = template.getLoanData();
        if (loanData != null && loanData.getProduct() != null) {
            assertEquals(productId, loanData.getProduct().getId());
            assertEquals(productName, loanData.getProduct().getName());
        }
        if (loanData != null) {
            if (loanData.getPaymentRate() != null) {
                assertEquals(0,
                        WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT.compareTo(loanData.getPaymentRate()));
            }
            if (loanData.getRepaymentEvery() != null) {
                assertEquals(30, loanData.getRepaymentEvery());
            }
            if (loanData.getRepaymentFrequencyType() != null) {
                assertEquals("Days", loanData.getRepaymentFrequencyType().getValue());
            }
            if (loanData.getCurrency() != null) {
                assertEquals("USD", loanData.getCurrency().getCode());
            }
            if (loanData.getPaymentAllocation() != null) {
                assertFalse(loanData.getPaymentAllocation().isEmpty());
            }
        }

        assertNotNull(template.getFundOptions());
        assertFalse(template.getFundOptions().isEmpty());

        assertNotNull(template.getPeriodFrequencyTypeOptions());
        assertFalse(template.getPeriodFrequencyTypeOptions().isEmpty());

        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveTemplateProductOptionsContainAllowAttributeOverrides() {
        final String productName = "WCL Template Product Overrides " + UUID.randomUUID().toString().substring(0, 8);
        final String shortName = Utils.uniqueRandomStringGenerator("", 4);

        final Map<String, Boolean> allowOverrides = Map.of(//
                "periodPaymentFrequency", Boolean.TRUE, //
                "discountDefault", Boolean.FALSE);

        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(productName) //
                .withShortName(shortName) //
                .withAllowAttributeOverrides(allowOverrides) //
                .build()).getResourceId();
        assertNotNull(productId);

        try {
            final GetWorkingCapitalLoansTemplateResponse template = applicationHelper.retrieveTemplateRaw(Map.of("productId", productId));
            assertNotNull(template);
            assertNotNull(template.getProductOptions());
            assertFalse(template.getProductOptions().isEmpty());

            final GetWorkingCapitalLoanProductsResponse matchedProduct = template.getProductOptions().stream()
                    .filter(option -> productId.equals(option.getId())).findFirst().orElse(null);

            assertNotNull(matchedProduct);
            final GetConfigurableAttributes allowAttributeOverrides = matchedProduct.getAllowAttributeOverrides();
            assertNotNull(allowAttributeOverrides);
            assertEquals(Boolean.TRUE, allowAttributeOverrides.getPeriodPaymentFrequency());
            assertNotEquals(Boolean.TRUE, allowAttributeOverrides.getDiscountDefault());
        } finally {
            productHelper.deleteWorkingCapitalLoanProductById(productId);
        }
    }

    @Test
    public void testModifyWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final var modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withSubmittedOnNote("Updated note") //
                .buildModifyRequest();
        final Long modifiedId = applicationHelper.modifyById(loanId, modifyJson);

        assertEquals(loanId, modifiedId);
        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithAllFieldsAndVerifyEachField() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .buildSubmitRequest());

        final String newAccountNo = "wcl-mod-" + UUID.randomUUID().toString().substring(0, 8);
        final String newExternalId = "wcl-mod-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(9000);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(100000);
        final BigDecimal discount = BigDecimal.valueOf(100);
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(14);
        final String submittedOnNote = "Modified all fields note";
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final List<String> paymentAllocationTypes = List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_FEE", "IN_ADVANCE_PENALTY");
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final var modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withFundId(fundId) //
                .withAccountNo(newAccountNo) //
                .withExternalId(newExternalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withSubmittedOnNote(submittedOnNote) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withPaymentAllocationTypes(paymentAllocationTypes) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildModifyRequest();

        final Long modifiedId = applicationHelper.modifyById(loanId, modifyJson);
        assertEquals(loanId, modifiedId);

        final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
        assertNotNull(data);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, newAccountNo, newExternalId, fundId, principal, periodPaymentRate,
                totalPaymentVolume, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testDeleteWorkingCapitalLoan() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final Long deletedId = applicationHelper.deleteById(loanId);

        assertEquals(loanId, deletedId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithAllFieldsAndVerifyEachField() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final String accountNo = "wcl-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId = "wcl-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(7500);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(8250);
        final BigDecimal discount = BigDecimal.valueOf(50);
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final String submittedOnNote = "Full fields test note";
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final List<String> paymentAllocationTypes = List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_FEE", "IN_ADVANCE_PENALTY");
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(fundId) //
                .withAccountNo(accountNo) //
                .withExternalId(externalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withSubmittedOnNote(submittedOnNote) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withPaymentAllocationTypes(paymentAllocationTypes) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitRequest();

        final Long loanId = applicationHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final GetWorkingCapitalLoansLoanIdResponse data = applicationHelper.retrieveById(loanId);
        assertNotNull(data);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPaymentVolume, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveAllPaged() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final String accountNo = "wcl-paged-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId = "wcl-paged-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(5500);
        final BigDecimal periodPaymentRate = WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT;
        final BigDecimal totalPaymentVolume = BigDecimal.valueOf(100000);
        final BigDecimal discount = BigDecimal.ZERO;
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(fundId) //
                .withAccountNo(accountNo) //
                .withExternalId(externalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPaymentVolume(totalPaymentVolume) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withPaymentAllocationTypes(
                        List.of("DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_FEE", "IN_ADVANCE_PENALTY")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitRequest());

        final GetWorkingCapitalLoansPagedResponse page = applicationHelper.retrieveAllPagedRaw(Map.of("clientId", clientId));
        assertNotNull(page);
        assertNotNull(page.getContent());
        assertTrue(page.getTotalElements() >= 1);

        final GetWorkingCapitalLoansLoanIdResponse foundLoan = page.getContent().stream().filter(loan -> loanId.equals(loan.getId()))
                .findFirst().orElse(null);
        assertNotNull(foundLoan, "Submitted loan should appear in paged list");

        assertAllLoanFieldsInResponse(foundLoan, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPaymentVolume, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-mod-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final var modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withSubmittedOnNote("Modified via external id") //
                .buildModifyRequest();
        final Long modifiedId = applicationHelper.modifyByExternalId(externalId, modifyJson);

        assertEquals(loanId, modifiedId);
        final GetWorkingCapitalLoansLoanIdResponse retrieved = applicationHelper.retrieveByExternalId(externalId);
        assertNotNull(retrieved);
        assertEquals(loanId, retrieved.getId());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testDeleteByExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-del-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest());

        final Long deletedId = applicationHelper.deleteByExternalId(externalId);

        assertEquals(loanId, deletedId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testWorkingCapitalDiscountAttributes() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        BigDecimal discountProposed = BigDecimal.valueOf(100);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(100000)) //
                .withDiscount(discountProposed) //
                .buildSubmitRequest());

        GetWorkingCapitalLoansLoanIdResponse loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        final LocalDate operationDate = loanData.getTimeline().getSubmittedOnDate();

        discountProposed = BigDecimal.valueOf(99);
        final var modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withDiscount(discountProposed) //
                .buildModifyRequest();

        final Long modifiedId = applicationHelper.modifyById(loanId, modifyJson);
        assertEquals(loanId, modifiedId);
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));

        // Approve the WC Loan with specific discount
        BigDecimal discountApproved = BigDecimal.valueOf(97);
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(operationDate, null, discountApproved));
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        assertEquals(0, discountApproved.compareTo(loanData.getApprovedDiscountFee()));

        // Undo WC Loan Approval
        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        // Null as reset of Approval amount
        assertNull(loanData.getApprovedDiscountFee());

        // ReApprove the WC Loan with specific discount
        discountApproved = BigDecimal.valueOf(95);
        applicationHelper.approveById(loanId,
                WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(operationDate, null, discountApproved));
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        assertEquals(0, discountApproved.compareTo(loanData.getApprovedDiscountFee()));

        // Disburse the WC Loan without specific discount then It will use discountApproved
        applicationHelper.disburseById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(operationDate, BigDecimal.valueOf(5000)));
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        assertEquals(0, discountApproved.compareTo(loanData.getApprovedDiscountFee()));
        assertNull(loanData.getDiscountFee());

        // Undo Disburse the WC Loan
        applicationHelper.undoDisbursalById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest("Undo disbursal note"));
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        assertEquals(0, discountApproved.compareTo(loanData.getApprovedDiscountFee()));
        assertNull(loanData.getDiscountFee());

        // ReDisburse the WC Loan with specific discount
        BigDecimal discountDisbursement = BigDecimal.valueOf(80);
        applicationHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(operationDate,
                BigDecimal.valueOf(5000), discountDisbursement, null, null, null, null, null, null, null));
        loanData = applicationHelper.retrieveLoan(loanId);
        assertEquals(0, discountProposed.compareTo(loanData.getProposedDiscountFee()));
        assertEquals(0, discountApproved.compareTo(loanData.getApprovedDiscountFee()));
        assertEquals(0, discountDisbursement.compareTo(loanData.getDiscountFee()));

        // Undo Disbursement for delete it
        applicationHelper.undoDisbursalById(loanId,
                WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest("Undo disbursal note"));
        applicationHelper.undoApprovalById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
    }

    private static void assertAllLoanFieldsInResponse(final GetWorkingCapitalLoansLoanIdResponse data, final long loanId,
            final long clientId, final long productId, final String accountNo, final String externalId, final Long fundId,
            final BigDecimal principal, final BigDecimal periodPaymentRate, final BigDecimal totalPaymentVolume,
            final BigDecimal discountProposed, final LocalDate submittedOnDate, final LocalDate expectedDisbursementDate,
            final Integer repaymentEvery, final String repaymentFrequencyType, final Integer delinquencyGraceDays,
            final String delinquencyStartType) {
        assertEquals(loanId, data.getId());
        assertNotNull(data.getClientId());
        assertEquals(clientId, data.getClientId());
        assertNotNull(data.getLoanProductId());
        assertEquals(productId, data.getLoanProductId());
        assertEquals(accountNo, data.getAccountNo());
        assertEquals(externalId, data.getExternalId());
        if (fundId != null) {
            assertEquals(fundId.longValue(), data.getFundId());
        }
        assertNotNull(data.getBalance());
        assertEqualBigDecimal(principal, data.getProposedPrincipal());
        assertEqualBigDecimal(totalPaymentVolume, data.getTotalPaymentVolume());
        assertEqualBigDecimal(periodPaymentRate, data.getPaymentRate());
        assertEqualBigDecimal(discountProposed, data.getProposedDiscountFee());
        assertNotNull(data.getDisbursementDetails());
        assertFalse(data.getDisbursementDetails().isEmpty(), "disbursementDetails should not be empty");
        assertEquals(expectedDisbursementDate, data.getDisbursementDetails().getFirst().getExpectedDisbursementDate());
        assertNotNull(data.getTimeline());
        final GetWorkingCapitalLoansLoanIdTimeline timeline = data.getTimeline();
        assertEquals(submittedOnDate, timeline.getSubmittedOnDate());
        assertEquals(expectedDisbursementDate, timeline.getExpectedDisbursementDate());
        assertEquals(repaymentEvery.intValue(), data.getRepaymentEvery());
        assert data.getRepaymentFrequencyType() != null;
        assertEquals(repaymentFrequencyType, data.getRepaymentFrequencyType().getCode());
        assertNotNull(data.getStatus());
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getStatus().getCode());
        assertNotNull(data.getLoanProductName());
        assertFalse(data.getLoanProductName().isBlank());
        assertNotNull(data.getClientName());
        assertFalse(data.getClientName().isBlank());
        if (data.getPaymentAllocation() != null) {
            assertFalse(data.getPaymentAllocation().isEmpty());
        }
        if (data.getDelinquencyGraceDays() != null) {
            assertEquals(delinquencyGraceDays.intValue(), data.getDelinquencyGraceDays());
        }
        if (data.getDelinquencyStartType() != null) {
            assertEquals(delinquencyStartType, data.getDelinquencyStartType().getCode());
        }
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
    }

    private Long createProductWithAllOverridables() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(50000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(10000)) //
                .withMinPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MIN_PERIOD_PAYMENT_RATE_PERCENT) //
                .withMaxPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MAX_PERIOD_PAYMENT_RATE_PERCENT) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withAllowAttributeOverrides(Map.of(//
                        "delinquencyBucketClassification", Boolean.TRUE, //
                        "periodPaymentFrequency", Boolean.TRUE, //
                        "periodPaymentFrequencyType", Boolean.TRUE, //
                        "discountDefault", Boolean.TRUE)) //
                .build()) //
                .getResourceId();
    }

    /**
     * Product with explicit default values for overridable attributes (used to verify loan gets them when not sent in
     * submit).
     */
    private Long createProductWithKnownDefaults(final Integer repaymentEvery, final String repaymentFrequencyType,
            final BigDecimal discount) {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(50000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(10000)) //
                .withMinPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MIN_PERIOD_PAYMENT_RATE_PERCENT) //
                .withMaxPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MAX_PERIOD_PAYMENT_RATE_PERCENT) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withDiscount(discount) //
                .withAllowAttributeOverrides(Map.of(//
                        "delinquencyBucketClassification", Boolean.TRUE, //
                        "periodPaymentFrequency", Boolean.TRUE, //
                        "periodPaymentFrequencyType", Boolean.TRUE, //
                        "discountDefault", Boolean.TRUE)) //
                .build()) //
                .getResourceId();
    }

    private Long createProductWithBreachAndNearBreach(final Long breachId, final Long nearBreachId, final boolean allowOverrideBreach) {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(10000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withRepaymentEvery(1) //
                .withRepaymentFrequencyType("MONTHS") //
                .withBreachId(breachId) //
                .withNearBreachId(nearBreachId) //
                .withAllowAttributeOverrides(Map.of("breach", allowOverrideBreach)) //
                .build()) //
                .getResourceId();
    }

    private Long createBreach(final String name, final Integer breachFrequency, final String breachFrequencyType,
            final String breachAmountCalculationType, final BigDecimal breachAmount) {
        return breachHelper.create(
                new WorkingCapitalBreachRequest().name(name).breachFrequency(breachFrequency).breachFrequencyType(breachFrequencyType)
                        .breachAmountCalculationType(breachAmountCalculationType).breachAmount(breachAmount));
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual) {
        assertNotNull(actual, "Expected non-null BigDecimal");
        assertEquals(0, expected.compareTo(actual), "Expected " + expected + " but got " + actual);
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
