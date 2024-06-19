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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanCOBCatchUpHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanCOBAccountLockCatchupInlineCOBTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanCOBCatchUpHelper loanCOBCatchUpHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanAccountLockHelper loanAccountLockHelper;
    private InlineLoanCOBHelper inlineLoanCOBHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        loanCOBCatchUpHelper = new LoanCOBCatchUpHelper();
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
    }

    @Test
    public void testCatchUpInLockedInstanceLastCOBDateIsNull() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
            loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_INLINE_COB_PROCESSING", "Sample error");

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 5));

            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanCOBCatchUpHelper.executeLoanCOBCatchUp();

            Utils.conditionalSleepWithMaxWait(30, 5, () -> loanCOBCatchUpHelper.isLoanCOBCatchUpRunning());

            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 4), loan.getLastClosedBusinessDate());
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    @Test
    public void testInlineCOBInLockedInstanceLastCOBDateIsNull() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
            loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_COB_CHUNK_PROCESSING", "Sample error");

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 5));

            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 4), loan.getLastClosedBusinessDate());

        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    @Test
    public void testCatchUpInLockedInstanceLastCOBDateIsNotNull() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());

            // create client
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            // create loan product
            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            // approve loan
            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);

            // disburse loan
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            // update business date 2020-03-02
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));

            // execute inline cob for the loan
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 2), loan.getLastClosedBusinessDate());

            // update business date to 2020-03-05
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 5));

            // apply lock on the loan
            loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_INLINE_COB_PROCESSING", "Sample error");

            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

            // execute catchup which sets the last cob date first 2020-03-04 and then 2020-03-05, as this loan is two
            // days behind
            loanCOBCatchUpHelper.executeLoanCOBCatchUp();

            Awaitility.await().atMost(Duration.ofSeconds(30)).with().pollInterval(Duration.ofSeconds(5)) //
                    .until(() -> loanCOBCatchUpHelper.isLoanCOBCatchUpFinishedFor(LocalDate.of(2020, 3, 4))); //

            loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 4), loan.getLastClosedBusinessDate());
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    @Test
    public void testInlineCOBInLockedInstanceLastCOBDateIsNotNull() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 2), loan.getLastClosedBusinessDate());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 5));
            loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_COB_CHUNK_PROCESSING", "Sample error");

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));
            loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 4), loan.getLastClosedBusinessDate());

        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    @Test
    public void testLoanCOBNoLock() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());
            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));

            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
            Assertions.assertEquals(LocalDate.of(2020, 3, 2), loan.getLastClosedBusinessDate());

        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    @Test
    public void testLoanCOBWithLoanAccountLockedWithInlineCOB() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "0");
            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
            loanAccountLockHelper = new LoanAccountLockHelper(requestSpec, new ResponseSpecBuilder().expectStatusCode(202).build());
            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;
            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "1 March 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.COB_DATE, LocalDate.of(2020, 3, 2));
            loanAccountLockHelper.placeSoftLockOnLoanAccount(loanID, "LOAN_INLINE_COB_PROCESSING");

            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

            GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);

            Assertions.assertNull(loan.getLastClosedBusinessDate());

        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(this.requestSpec, this.responseSpec, "10", "2");
        }
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String savingsID, final String date) {

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec, clientID,
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(date).withSubmittedOnDate(date).withCollaterals(collaterals)
                .build(clientID, loanProductID, savingsID);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

}
