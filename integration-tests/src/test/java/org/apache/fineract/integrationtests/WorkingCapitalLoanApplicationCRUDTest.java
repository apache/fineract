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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanApplicationCRUDTest {

    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static Long delinquencyBucketId;
    private static Long fundId;

    private final WorkingCapitalLoanHelper applicationHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();

    @BeforeAll
    static void initDelinquency() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        fundId = (long) FundsResourceHandler.createFund(requestSpec, responseSpec);
    }

    @Test
    public void testSubmitWorkingCapitalLoanApplication() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

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
        final BigDecimal periodPaymentRate = BigDecimal.ONE;
        final BigDecimal totalPayment = BigDecimal.valueOf(5500);
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);

        // Submit with only mandatory fields — no repaymentEvery, repaymentFrequencyType,
        // discount, delinquencyBucketId
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPayment(totalPayment) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .buildSubmitJson();

        final Long loanId = applicationHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);

        assertEquals(productRepaymentEvery.intValue(), data.get("repaymentEvery").getAsInt(), "repaymentEvery should come from product");
        assertRepaymentFrequencyTypeEquals(productRepaymentFrequencyType, data.get("repaymentFrequencyType"));
        assertEqualBigDecimal(productDiscount, data.get("discount"));
        assertTrue(data.has("delinquencyBucket") && !data.get("delinquencyBucket").isJsonNull(),
                "delinquencyBucket should come from product");
        assertEquals(delinquencyBucketId.longValue(), data.getAsJsonObject("delinquencyBucket").get("id").getAsLong(),
                "delinquencyBucket.id should come from product");

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithoutBreachParamsUsesProductBreachDefaults() {
        final String breachName = "Default WCL Breach";
        final Integer breachFrequency = 30;
        final String breachFrequencyType = "DAYS";
        final String breachAmountCalculationType = "PERCENTAGE";
        final BigDecimal breachAmount = BigDecimal.valueOf(10);
        final Long breachId = createBreach(breachName, breachFrequency, breachFrequencyType, breachAmountCalculationType, breachAmount);
        final Long productId = createProductWithBreach(breachId);
        final Long clientId = createClient();

        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

        final Long loanId = applicationHelper.submit(json);
        final JsonObject data = new Gson().fromJson(applicationHelper.retrieveById(loanId), JsonObject.class);

        final JsonObject breach = data.getAsJsonObject("breach");
        assertEquals(breachName, breach.get("name").getAsString());
        assertEquals(breachFrequency.intValue(), breach.get("breachFrequency").getAsInt());
        assertRepaymentFrequencyTypeEquals(breachFrequencyType, breach.get("breachFrequencyType"));
        assertRepaymentFrequencyTypeEquals(breachAmountCalculationType, breach.get("breachAmountCalculationType"));
        assertEqualBigDecimal(breachAmount, breach.get("breachAmount"));

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        breachHelper.delete(breachId);
    }

    @Test
    public void testRetrieveWorkingCapitalLoanByIdWithAllFieldsVerified() {
        final Long productId = createProductWithAllOverridables();
        final Long clientId = createClient();
        final String accountNo = "wcl-get-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId = "wcl-get-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(6000);
        final BigDecimal periodPaymentRate = BigDecimal.valueOf(1.05);
        final BigDecimal totalPayment = BigDecimal.valueOf(6300);
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
                .withTotalPayment(totalPayment) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withPaymentAllocationTypes(List.of("PENALTY", "FEE", "PRINCIPAL")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitJson());

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPayment, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(3150)) //
                .withAccountNo(accountNo) //
                .buildSubmitJson());

        assertNotNull(loanId);

        final String accountsJson = ClientHelper.getClientAccountsRaw(requestSpec, responseSpec, clientId);
        assertNotNull(accountsJson);
        final JsonObject data = new Gson().fromJson(accountsJson, JsonObject.class);
        assertTrue(data.has("workingCapitalLoanAccounts"), "Response should contain workingCapitalLoanAccounts");
        final JsonArray wclAccounts = data.getAsJsonArray("workingCapitalLoanAccounts");
        assertNotNull(wclAccounts);
        assertFalse(wclAccounts.isEmpty(), "Client should have at least one working capital loan");
        boolean found = false;
        for (JsonElement el : wclAccounts) {
            if (el.isJsonObject() && el.getAsJsonObject().get("id").getAsLong() == loanId) {
                found = true;
                assertEquals(accountNo, el.getAsJsonObject().get("accountNo").getAsString());
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(1100)) //
                .buildSubmitJson());
        final Long loanId2 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(2000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(2200)) //
                .buildSubmitJson());
        final Long loanId3 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(3000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(3300)) //
                .buildSubmitJson());

        assertNotNull(loanId1);
        assertNotNull(loanId2);
        assertNotNull(loanId3);

        try {
            final String accountsJson = ClientHelper.getClientAccountsRaw(requestSpec, responseSpec, clientId);
            assertNotNull(accountsJson);
            final JsonObject data = new Gson().fromJson(accountsJson, JsonObject.class);
            assertTrue(data.has("workingCapitalLoanAccounts"), "Response should contain workingCapitalLoanAccounts");
            final JsonArray wclAccounts = data.getAsJsonArray("workingCapitalLoanAccounts");
            assertNotNull(wclAccounts);
            assertTrue(wclAccounts.size() >= 3, "Client should have at least 3 working capital loans");

            final List<JsonObject> ourLoans = new ArrayList<>();
            for (JsonElement el : wclAccounts) {
                if (el.isJsonObject()) {
                    final JsonObject obj = el.getAsJsonObject();
                    final long id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsLong() : -1;
                    if (id == loanId1 || id == loanId2 || id == loanId3) {
                        ourLoans.add(obj);
                    }
                }
            }
            assertEquals(3, ourLoans.size(), "Should find exactly 3 WCL loans for this client");
            ourLoans.sort(Comparator.comparing(o -> o.get("id").getAsLong()));

            assertTrue(ourLoans.get(0).has("loanCycle"), "First loan should have loanCycle");
            assertEquals(1, ourLoans.get(0).get("loanCycle").getAsInt(), "First WCL loan should have loanCycle 1");
            assertEquals(2, ourLoans.get(1).get("loanCycle").getAsInt(), "Second WCL loan should have loanCycle 2");
            assertEquals(3, ourLoans.get(2).get("loanCycle").getAsInt(), "Third WCL loan should have loanCycle 3");
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
        final BigDecimal periodPaymentRate = BigDecimal.valueOf(1.15);
        final BigDecimal totalPayment = BigDecimal.valueOf(8050);
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
                .withTotalPayment(totalPayment) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withPaymentAllocationTypes(List.of("PENALTY", "FEE", "PRINCIPAL")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitJson());

        final String response = applicationHelper.retrieveByExternalId(externalId);
        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);
        final Long loanId = data.get("id").getAsLong();

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPayment, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
                delinquencyGraceDays, delinquencyStartType);

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testRetrieveTemplate() {
        // Ensure at least one product exists so productOptions is not empty
        final Long productId = createProduct();
        try {
            final String response = applicationHelper.retrieveTemplateRaw(Map.of());

            assertNotNull(response);
            final JsonObject data = new Gson().fromJson(response, JsonObject.class);

            final JsonArray productOptions = data.has("productOptions") && !data.get("productOptions").isJsonNull()
                    ? data.getAsJsonArray("productOptions")
                    : new JsonArray();
            assertNotNull(productOptions);
            final JsonObject firstProduct = productOptions.get(0).getAsJsonObject();
            assertTrue(firstProduct.has("id"));
            assertTrue(firstProduct.get("id").getAsLong() > 0);
            assertTrue(firstProduct.has("name"));
            assertFalse(firstProduct.get("name").getAsString().isBlank());
            assertTrue(firstProduct.has("shortName"));
            assertFalse(firstProduct.get("shortName").getAsString().isBlank());

            final JsonArray fundOptions = data.has("fundOptions") && !data.get("fundOptions").isJsonNull()
                    ? data.getAsJsonArray("fundOptions")
                    : new JsonArray();
            assertNotNull(fundOptions);
            final JsonObject firstFund = fundOptions.get(0).getAsJsonObject();
            assertTrue(firstFund.has("id"));
            assertTrue(firstFund.get("id").getAsLong() > 0);
            assertTrue(firstFund.has("name"));
            assertFalse(firstFund.get("name").getAsString().isBlank());
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

        final String response = applicationHelper.retrieveTemplateRaw(Map.of("productId", productId));

        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);

        final JsonArray productOptions = data.has("productOptions") && !data.get("productOptions").isJsonNull()
                ? data.getAsJsonArray("productOptions")
                : new JsonArray();
        assertNotNull(productOptions);
        assertFalse(productOptions.isEmpty());

        final JsonObject loanData = data.has("loanData") && !data.get("loanData").isJsonNull() ? data.getAsJsonObject("loanData") : null;
        if (loanData != null && loanData.has("product") && !loanData.get("product").isJsonNull()) {
            assertEquals(productId, loanData.getAsJsonObject("product").get("id").getAsLong());
            assertEquals(productName, loanData.getAsJsonObject("product").get("name").getAsString());
        }
        if (loanData != null) {
            if (loanData.has("periodPaymentRate") && !loanData.get("periodPaymentRate").isJsonNull()) {
                assertEqualBigDecimal(BigDecimal.valueOf(1.0), loanData.get("periodPaymentRate"));
            }
            if (loanData.has("repaymentEvery") && !loanData.get("repaymentEvery").isJsonNull()) {
                assertEquals(30, loanData.get("repaymentEvery").getAsInt());
            }
            if (loanData.has("repaymentFrequencyType") && !loanData.get("repaymentFrequencyType").isJsonNull()) {
                assertRepaymentFrequencyTypeEquals("DAYS", loanData.get("repaymentFrequencyType"));
            }
            if (loanData.has("currency") && !loanData.get("currency").isJsonNull()) {
                assertEquals("USD", loanData.get("currency").getAsJsonObject().get("code").getAsString());
            }
            if (loanData.has("paymentAllocation") && !loanData.get("paymentAllocation").isJsonNull()) {
                assertFalse(loanData.getAsJsonArray("paymentAllocation").isEmpty());
            }
        }

        final JsonArray fundOptions = data.has("fundOptions") && !data.get("fundOptions").isJsonNull() ? data.getAsJsonArray("fundOptions")
                : new JsonArray();
        assertNotNull(fundOptions);
        assertFalse(fundOptions.isEmpty());

        final JsonArray periodFrequencyTypeOptions = data.has("periodFrequencyTypeOptions")
                && !data.get("periodFrequencyTypeOptions").isJsonNull() ? data.getAsJsonArray("periodFrequencyTypeOptions")
                        : new JsonArray();
        assertNotNull(periodFrequencyTypeOptions);
        assertFalse(periodFrequencyTypeOptions.isEmpty());

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
            final String response = applicationHelper.retrieveTemplateRaw(Map.of("productId", productId));
            assertNotNull(response);

            final JsonObject data = new Gson().fromJson(response, JsonObject.class);
            final JsonArray productOptions = data.has("productOptions") && !data.get("productOptions").isJsonNull()
                    ? data.getAsJsonArray("productOptions")
                    : new JsonArray();
            assertNotNull(productOptions);
            assertFalse(productOptions.isEmpty());

            JsonObject matchedProduct = null;
            for (int i = 0; i < productOptions.size(); i++) {
                final JsonObject option = productOptions.get(i).getAsJsonObject();
                if (option.has("id") && !option.get("id").isJsonNull() && option.get("id").getAsLong() == productId) {
                    matchedProduct = option;
                    break;
                }
            }

            assertNotNull(matchedProduct);
            assertTrue(matchedProduct.has("allowAttributeOverrides"));
            assertFalse(matchedProduct.get("allowAttributeOverrides").isJsonNull());

            final JsonObject allowAttributeOverrides = matchedProduct.getAsJsonObject("allowAttributeOverrides");
            assertTrue(allowAttributeOverrides.has("periodPaymentFrequency"));
            assertTrue(allowAttributeOverrides.get("periodPaymentFrequency").getAsBoolean());

            assertTrue(allowAttributeOverrides.has("discountDefault"));
            assertFalse(allowAttributeOverrides.get("discountDefault").getAsBoolean());
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withSubmittedOnNote("Updated note") //
                .buildModifyJson();
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String newAccountNo = "wcl-mod-" + UUID.randomUUID().toString().substring(0, 8);
        final String newExternalId = "wcl-mod-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final BigDecimal principal = BigDecimal.valueOf(9000);
        final BigDecimal periodPaymentRate = BigDecimal.valueOf(1.2);
        final BigDecimal totalPayment = BigDecimal.valueOf(10800);
        final BigDecimal discount = BigDecimal.valueOf(100);
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(14);
        final String submittedOnNote = "Modified all fields note";
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final List<String> paymentAllocationTypes = List.of("PENALTY", "FEE", "PRINCIPAL");
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withFundId(fundId) //
                .withAccountNo(newAccountNo) //
                .withExternalId(newExternalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPayment(totalPayment) //
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
                .buildModifyJson();

        final Long modifiedId = applicationHelper.modifyById(loanId, modifyJson);
        assertEquals(loanId, modifiedId);

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, newAccountNo, newExternalId, fundId, principal, periodPaymentRate,
                totalPayment, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

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
        final BigDecimal periodPaymentRate = BigDecimal.valueOf(1.1);
        final BigDecimal totalPayment = BigDecimal.valueOf(8250);
        final BigDecimal discount = BigDecimal.valueOf(50);
        final LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
        final LocalDate expectedDisbursementDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final String submittedOnNote = "Full fields test note";
        final Integer repaymentEvery = 30;
        final String repaymentFrequencyType = "DAYS";
        final List<String> paymentAllocationTypes = List.of("PENALTY", "FEE", "PRINCIPAL");
        final Integer delinquencyGraceDays = 1;
        final String delinquencyStartType = "DISBURSEMENT";

        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(fundId) //
                .withAccountNo(accountNo) //
                .withExternalId(externalId) //
                .withPrincipal(principal) //
                .withPeriodPaymentRate(periodPaymentRate) //
                .withTotalPayment(totalPayment) //
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
                .buildSubmitJson();

        final Long loanId = applicationHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final String response = applicationHelper.retrieveById(loanId);
        assertNotNull(response);
        final JsonObject data = new Gson().fromJson(response, JsonObject.class);

        assertAllLoanFieldsInResponse(data, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPayment, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
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
        final BigDecimal periodPaymentRate = BigDecimal.valueOf(1.05);
        final BigDecimal totalPayment = BigDecimal.valueOf(5775);
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
                .withTotalPayment(totalPayment) //
                .withDiscount(discount) //
                .withSubmittedOnDate(submittedOnDate) //
                .withExpectedDisbursementDate(expectedDisbursementDate) //
                .withRepaymentEvery(repaymentEvery) //
                .withRepaymentFrequencyType(repaymentFrequencyType) //
                .withPaymentAllocationTypes(List.of("PENALTY", "FEE", "PRINCIPAL")) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .buildSubmitJson());

        final String response = applicationHelper.retrieveAllPagedRaw(Map.of("clientId", clientId));
        assertNotNull(response);
        final JsonObject page = new Gson().fromJson(response, JsonObject.class);
        assertTrue(page.has("content"));
        final JsonArray content = page.getAsJsonArray("content");
        assertTrue(page.has("totalElements"));
        assertTrue(page.get("totalElements").getAsLong() >= 1);

        JsonObject foundLoan = null;
        for (JsonElement el : content) {
            if (el.getAsJsonObject().get("id").getAsLong() == loanId) {
                foundLoan = el.getAsJsonObject();
                break;
            }
        }
        assertNotNull(foundLoan, "Submitted loan should appear in paged list");

        assertAllLoanFieldsInResponse(foundLoan, loanId, clientId, productId, accountNo, externalId, fundId, principal, periodPaymentRate,
                totalPayment, discount, submittedOnDate, expectedDisbursementDate, repaymentEvery, repaymentFrequencyType,
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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withSubmittedOnNote("Modified via external id") //
                .buildModifyJson();
        final Long modifiedId = applicationHelper.modifyByExternalId(externalId, modifyJson);

        assertEquals(loanId, modifiedId);
        final String retrieved = applicationHelper.retrieveByExternalId(externalId);
        assertNotNull(retrieved);
        final JsonObject data = new Gson().fromJson(retrieved, JsonObject.class);
        assertEquals(loanId, data.get("id").getAsLong());

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
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final Long deletedId = applicationHelper.deleteByExternalId(externalId);

        assertEquals(loanId, deletedId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    private static void assertAllLoanFieldsInResponse(final JsonObject data, final long loanId, final long clientId, final long productId,
            final String accountNo, final String externalId, final Long fundId, final BigDecimal principal,
            final BigDecimal periodPaymentRate, final BigDecimal totalPayment, final BigDecimal discount, final LocalDate submittedOnDate,
            final LocalDate expectedDisbursementDate, final Integer repaymentEvery, final String repaymentFrequencyType,
            final Integer delinquencyGraceDays, final String delinquencyStartType) {
        assertEquals(loanId, data.get("id").getAsLong());
        assertTrue(data.has("client") && !data.get("client").isJsonNull());
        assertEquals(clientId, data.getAsJsonObject("client").get("id").getAsLong());
        assertTrue(data.has("product") && !data.get("product").isJsonNull());
        assertEquals(productId, data.getAsJsonObject("product").get("id").getAsLong());
        assertEquals(accountNo, data.get("accountNo").getAsString());
        assertEquals(externalId, getExternalIdString(data));
        if (fundId != null) {
            assertEquals(fundId.longValue(), data.get("fundId").getAsLong());
        }
        assertTrue(data.has("balance") && !data.get("balance").isJsonNull(), "balance should be present");
        assertEqualBigDecimal(principal, data.getAsJsonObject("balance").get("principalOutstanding"));
        assertEqualBigDecimal(totalPayment, data.getAsJsonObject("balance").get("totalPayment"));
        assertEqualBigDecimal(periodPaymentRate, data.get("periodPaymentRate"));
        assertEqualBigDecimal(discount, data.get("discount"));
        assertDateEquals(submittedOnDate, data.get("submittedOnDate"));
        assertTrue(data.has("disbursementDetails") && !data.get("disbursementDetails").isJsonNull(),
                "disbursementDetails should be present");
        assertFalse(data.getAsJsonArray("disbursementDetails").isEmpty(), "disbursementDetails should not be empty");
        assertDateEquals(expectedDisbursementDate,
                data.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject().get("expectedDisbursementDate"));
        assertTrue(data.has("timeline") && !data.get("timeline").isJsonNull(), "timeline should be present in response");
        final JsonObject timeline = data.getAsJsonObject("timeline");
        assertDateEquals(submittedOnDate, timeline.get("submittedOnDate"));
        assertDateEquals(expectedDisbursementDate, timeline.get("expectedDisbursementDate"));
        assertTrue(timeline.has("disbursementDetails") && !timeline.get("disbursementDetails").isJsonNull(),
                "timeline.disbursementDetails should be present");
        assertFalse(timeline.getAsJsonArray("disbursementDetails").isEmpty(), "timeline.disbursementDetails should not be empty");
        assertDateEquals(expectedDisbursementDate,
                timeline.getAsJsonArray("disbursementDetails").get(0).getAsJsonObject().get("expectedDisbursementDate"));
        assertEquals(repaymentEvery.intValue(), data.get("repaymentEvery").getAsInt());
        assertRepaymentFrequencyTypeEquals(repaymentFrequencyType, data.get("repaymentFrequencyType"));
        assertTrue(data.has("status") && !data.get("status").isJsonNull());
        assertEquals("loanStatusType.submitted.and.pending.approval", data.getAsJsonObject("status").get("code").getAsString());
        assertNotNull(data.getAsJsonObject("product").get("name"));
        assertFalse(data.getAsJsonObject("product").get("name").getAsString().isBlank());
        assertNotNull(data.getAsJsonObject("client").get("displayName"));
        assertFalse(data.getAsJsonObject("client").get("displayName").getAsString().isBlank());
        if (data.has("paymentAllocation") && !data.get("paymentAllocation").isJsonNull()) {
            assertFalse(data.getAsJsonArray("paymentAllocation").isEmpty());
        }
        if (data.has("delinquencyGraceDays") && !data.get("delinquencyGraceDays").isJsonNull()) {
            assertEquals(delinquencyGraceDays.intValue(), data.get("delinquencyGraceDays").getAsInt());
        }
        if (data.has("delinquencyStartType") && !data.get("delinquencyStartType").isJsonNull()) {
            assertDelinquencyStartTypeEquals(delinquencyStartType, data.get("delinquencyStartType"));
        }
    }

    private static String getExternalIdString(final JsonObject data) {
        if (!data.has("externalId") || data.get("externalId").isJsonNull()) {
            return null;
        }
        if (data.get("externalId").isJsonObject()) {
            return data.getAsJsonObject("externalId").has("value") ? data.getAsJsonObject("externalId").get("value").getAsString() : null;
        }
        return data.get("externalId").getAsString();
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final JsonElement actual) {
        assertNotNull(actual, "Expected value for field");
        assertFalse(actual.isJsonNull(), "Expected non-null value");
        assertEquals(0, expected.compareTo(actual.getAsJsonPrimitive().getAsBigDecimal()),
                "Expected " + expected + " but got " + actual.getAsString());
    }

    private static void assertDateEquals(final LocalDate expected, final JsonElement actual) {
        assertNotNull(actual, "Expected date value");
        assertFalse(actual.isJsonNull(), "Expected non-null date");
        if (actual.isJsonArray()) {
            final JsonArray arr = actual.getAsJsonArray();
            assertEquals(expected.getYear(), arr.get(0).getAsInt());
            assertEquals(expected.getMonthValue(), arr.get(1).getAsInt());
            assertEquals(expected.getDayOfMonth(), arr.get(2).getAsInt());
        } else {
            assertEquals(expected.format(DateTimeFormatter.ISO_LOCAL_DATE), actual.getAsString());
        }
    }

    private static void assertRepaymentFrequencyTypeEquals(final String expectedCode, final JsonElement actual) {
        assertNotNull(actual);
        if (actual.isJsonObject()) {
            final JsonObject obj = actual.getAsJsonObject();
            String code = obj.has("code") ? obj.get("code").getAsString() : null;
            if (code == null && obj.has("value")) {
                code = obj.get("value").getAsString();
            }
            assertNotNull(code);
            assertTrue(expectedCode.equalsIgnoreCase(code) || obj.toString().contains(expectedCode),
                    "Expected repaymentFrequencyType " + expectedCode + " but got " + obj);
        }
    }

    private static void assertDelinquencyStartTypeEquals(final String expectedCode, final JsonElement actual) {
        assertNotNull(actual);
        if (actual.isJsonObject()) {
            final JsonObject obj = actual.getAsJsonObject();
            String code = obj.has("code") ? obj.get("code").getAsString() : null;
            if (code == null && obj.has("value")) {
                code = obj.get("value").getAsString();
            }
            assertNotNull(code);
            assertTrue(expectedCode.equalsIgnoreCase(code) || obj.toString().contains(expectedCode),
                    "Expected delinquencyStartType " + expectedCode + " but got " + obj);
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
                .withMinPeriodPaymentRate(BigDecimal.ONE) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
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
                .withMinPeriodPaymentRate(BigDecimal.ONE) //
                .withMaxPeriodPaymentRate(BigDecimal.valueOf(2)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
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

    private Long createProductWithBreach(final Long breachId) {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(10000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withRepaymentEvery(1) //
                .withRepaymentFrequencyType("MONTHS") //
                .withBreachId(breachId) //
                .build()) //
                .getResourceId();
    }

    private Long createBreach(final String name, final Integer breachFrequency, final String breachFrequencyType,
            final String breachAmountCalculationType, final BigDecimal breachAmount) {
        final JsonObject payload = new JsonObject();
        payload.addProperty("name", name);
        payload.addProperty("breachFrequency", breachFrequency);
        payload.addProperty("breachFrequencyType", breachFrequencyType);
        payload.addProperty("breachAmountCalculationType", breachAmountCalculationType);
        payload.addProperty("breachAmount", breachAmount);
        return breachHelper.create(payload);
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
