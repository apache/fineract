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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanApplicationValidationTest {

    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static Long delinquencyBucketId;

    private final WorkingCapitalLoanApplicationHelper applicationHelper = new WorkingCapitalLoanApplicationHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();

    @BeforeAll
    static void initDelinquency() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        delinquencyBucketId = (long) DelinquencyBucketsHelper.createDefaultBucket();
    }

    @Test
    public void testSubmitWithMissingClientId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "clientId");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("Validation errors: [clientId] The parameter `clientId` is mandatory."));
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithMissingProductId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "productId");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [productId] The parameter `productId` is mandatory.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithMissingPrincipal() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "principalAmount");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] The parameter `principalAmount` is mandatory.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithMissingPeriodPaymentRate() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "periodPaymentRate");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] The parameter `periodPaymentRate` is mandatory.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithMissingTotalPayment() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "totalPayment");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [totalPayment] The parameter `totalPayment` is mandatory.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithMissingExpectedDisbursementDate() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = buildSubmitJsonWithoutField(new WorkingCapitalLoanApplicationTestBuilder().withClientId(clientId)
                .withProductId(productId).withPrincipal(BigDecimal.valueOf(5000)).withPeriodPaymentRate(BigDecimal.ONE)
                .withTotalPayment(BigDecimal.valueOf(5500)).buildSubmitJson(), "expectedDisbursementDate");

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [expectedDisbursementDate] The parameter `expectedDisbursementDate` is mandatory.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithNegativePrincipal() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(-100)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] The parameter `principalAmount` must be greater than 0.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithZeroPrincipal() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.ZERO) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] The parameter `principalAmount` must be greater than 0.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithNegativePeriodPaymentRate() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(-1)) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] The parameter `periodPaymentRate` must be greater than or equal to 0.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithNegativeDiscount() {
        final Long productId = createProductWithDiscountAllowed();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.valueOf(-1)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount") && ex.getDeveloperMessage().contains("must be greater than or equal to 0"),
                "Expected discount >= 0 error: " + ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithInvalidFundId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withFundId(-1L) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [fundId] The parameter `fundId` must be greater than 0.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithInvalidDelinquencyBucketId() {
        final Long productId = createProductWithDelinquencyBucketOverride();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDelinquencyBucketId(-1L) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [delinquencyBucketId] The parameter `delinquencyBucketId` must be greater than 0.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithSubmittedOnNoteExceedingLength() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String longNote = "a".repeat(501);
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withSubmittedOnNote(longNote) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [submittedOnNote] The parameter `submittedOnNote` exceeds max length of 500.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithPrincipalBelowProductMin() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.ONE, BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(500)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(550)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithPrincipalAboveProductMax() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.ONE, BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(25000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(27500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithPeriodPaymentRateBelowProductMin() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(0.2)) //
                .withTotalPayment(BigDecimal.valueOf(5100)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithPeriodPaymentRateAboveProductMax() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.valueOf(3)) //
                .withTotalPayment(BigDecimal.valueOf(6500)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * When product has overridable attribute set to false, submitting a loan with that attribute must be rejected
     * (cannot override product value).
     */
    @Test
    public void testSubmitWithOverrideNotAllowedByProduct() {
        final Long productId = createProductWithOverridableFalseForDiscountDefault();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.ONE) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("override.not.allowed.by.product"),
                "Expected override.not.allowed.by.product in: " + ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("discount"), "Expected discount in: " + ex.getDeveloperMessage());

        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithDuplicateAccountNo() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String accountNo = "wcl-acc-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withAccountNo(accountNo) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withAccountNo(accountNo) //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(6600)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] Loan with account number is already registered.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithDuplicateExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId = "wcl-ext-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId) //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(6600)) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] Loan with externalId is already registered.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testSubmitWithSubmittedOnDateAfterExpectedDisbursementDate() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final LocalDate submittedOn = LocalDate.now(ZoneId.systemDefault()).plusDays(14);
        final LocalDate expectedDisbursement = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withSubmittedOnDate(submittedOn) //
                .withExpectedDisbursementDate(expectedDisbursement) //
                .buildSubmitJson();

        final CallFailedRuntimeException ex = applicationHelper.runSubmitExpectingFailure(json);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] The date on which a loan is submitted cannot be in the future.", ex.getDeveloperMessage());
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithEmptyJson() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, "{}");
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] No parameters passed for update.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithInvalidDateRange() {
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
                .withSubmittedOnDate(LocalDate.now(ZoneId.systemDefault()).plusDays(14)) //
                .withExpectedDisbursementDate(LocalDate.now(ZoneId.systemDefault()).plusDays(7)) //
                .buildModifyJson();

        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] The date on which a loan is submitted cannot be in the future.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    // ---------- Modify validation: min/max and invalid values ----------

    @Test
    public void testModifyWithPrincipalBelowProductMin() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.ONE, BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPrincipal(BigDecimal.valueOf(500)).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithPrincipalAboveProductMax() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.ONE, BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPrincipal(BigDecimal.valueOf(25000)).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithPeriodPaymentRateBelowProductMin() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPeriodPaymentRate(BigDecimal.valueOf(0.2))
                .buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.greater.than.or.equal.to.min.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithPeriodPaymentRateAboveProductMax() {
        final Long productId = createProductWithMinMax(1000, 20000, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2));
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPeriodPaymentRate(BigDecimal.valueOf(3))
                .buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] Failed data validation due to: must.be.less.than.or.equal.to.max.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithNegativePrincipal() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPrincipal(BigDecimal.valueOf(-100)).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [principalAmount] The parameter `principalAmount` must be greater than 0.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithNegativePeriodPaymentRate() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withPeriodPaymentRate(BigDecimal.valueOf(-1))
                .buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [periodPaymentRate] The parameter `periodPaymentRate` must be greater than or equal to 0.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithNegativeDiscount() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withDiscount(BigDecimal.valueOf(-1)).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [discount] The parameter `discount` must be greater than or equal to 0.",
                ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    /**
     * Modify with submittedOnNote exceeding 500 chars must be rejected (same as standard Loan).
     */
    @Test
    public void testModifyWithSubmittedOnNoteExceedingLength() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String longNote = "a".repeat(501);
        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder() //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withSubmittedOnNote(longNote) //
                .buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertTrue(ex.getDeveloperMessage().contains("submittedOnNote") && ex.getDeveloperMessage().contains("exceeds max length of 500"),
                "Expected submittedOnNote length error: " + ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithInvalidFundId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withFundId(-1L).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [fundId] The parameter `fundId` must be greater than 0.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithDuplicateExternalId() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String externalId1 = "wcl-me1-" + UUID.randomUUID().toString().substring(0, 8);
        final String externalId2 = "wcl-me2-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId1 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId1) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());
        final Long loanId2 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withExternalId(externalId2) //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(6600)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withExternalId(externalId1).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId2, modifyJson);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] Loan with externalId is already registered.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId1);
        applicationHelper.deleteById(loanId2);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithBlankAccountNo() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final Long loanId = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withAccountNo("").buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId, modifyJson);
        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [accountNo] The parameter `accountNo` is mandatory.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyWithDuplicateAccountNo() {
        final Long productId = createProduct();
        final Long clientId = createClient();
        final String accountNo1 = "wcl-mda-" + UUID.randomUUID().toString().substring(0, 8);
        final String accountNo2 = "wcl-mdb-" + UUID.randomUUID().toString().substring(0, 8);
        final Long loanId1 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withAccountNo(accountNo1) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .buildSubmitJson());
        final Long loanId2 = applicationHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withAccountNo(accountNo2) //
                .withPrincipal(BigDecimal.valueOf(6000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(6600)) //
                .buildSubmitJson());

        final String modifyJson = new WorkingCapitalLoanApplicationTestBuilder().withAccountNo(accountNo1).buildModifyJson();
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(loanId2, modifyJson);
        assertEquals(403, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        assertEquals("Validation errors: [id] Loan with account number is already registered.", ex.getDeveloperMessage());

        applicationHelper.deleteById(loanId1);
        applicationHelper.deleteById(loanId2);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    @Test
    public void testModifyNonExistentLoan() {
        final long nonExistentLoanId = 999_999_999L;
        final CallFailedRuntimeException ex = applicationHelper.runModifyExpectingFailure(nonExistentLoanId,
                new WorkingCapitalLoanApplicationTestBuilder().withPrincipal(BigDecimal.valueOf(1000)).buildModifyJson());
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
        final String msg = ex.getDeveloperMessage().toLowerCase();
        assertTrue(msg.contains("not found") || msg.contains("not available") || msg.contains("resource not found")
                || msg.contains("does not exist"), "Expected message to indicate resource not found: " + ex.getDeveloperMessage());
    }

    // ---------- Delete / Retrieve non-existent resource ----------

    @Test
    public void testDeleteByNonExistentId() {
        final long nonExistentLoanId = 999_999_999L;
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> applicationHelper.deleteById(nonExistentLoanId));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testDeleteByNonExistentExternalId() {
        final String nonExistentExternalId = "non-existent-ext-" + UUID.randomUUID();
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> applicationHelper.deleteByExternalId(nonExistentExternalId));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testRetrieveByNonExistentId() {
        final long nonExistentLoanId = 999_999_999L;
        final CallFailedRuntimeException ex = assertThrows(CallFailedRuntimeException.class,
                () -> applicationHelper.retrieveById(nonExistentLoanId));
        assertEquals(404, ex.getStatus());
        assertNotNull(ex.getDeveloperMessage());
    }

    @Test
    public void testSubmitWithValidZeroDiscount() {
        final Long productId = createProductWithDiscountAllowed();
        final Long clientId = createClient();
        final String json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(BigDecimal.ONE) //
                .withTotalPayment(BigDecimal.valueOf(5500)) //
                .withDiscount(BigDecimal.ZERO) //
                .buildSubmitJson();

        final Long loanId = applicationHelper.submit(json);

        assertNotNull(loanId);
        assertTrue(loanId > 0);
        applicationHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
    }

    private String buildSubmitJsonWithoutField(final String fullJson, final String fieldToOmit) {
        final JsonObject json = JsonParser.parseString(fullJson).getAsJsonObject();
        json.remove(fieldToOmit);
        return json.toString();
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
    }

    private Long createProductWithMinMax(final int minPrincipal, final int maxPrincipal, final BigDecimal minRate,
            final BigDecimal maxRate) {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountMin(BigDecimal.valueOf(minPrincipal)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(maxPrincipal)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf((minPrincipal + maxPrincipal) / 2)) //
                .withMinPeriodPaymentRate(minRate) //
                .withMaxPeriodPaymentRate(maxRate) //
                .withPeriodPaymentRate(minRate) //
                .build()) //
                .getResourceId();
    }

    private Long createProductWithDelinquencyBucketOverride() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withAllowAttributeOverrides(Map.of("delinquencyBucketClassification", true)) //
                .build()) //
                .getResourceId();
    }

    private Long createProductWithDiscountAllowed() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)) //
                .build()) //
                .getResourceId();
    }

    private Long createProductWithOverridableFalseForDiscountDefault() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withAllowAttributeOverrides(Map.of("discountDefault", false)) //
                .build()) //
                .getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
