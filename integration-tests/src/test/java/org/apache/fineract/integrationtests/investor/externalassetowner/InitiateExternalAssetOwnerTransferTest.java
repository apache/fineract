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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.ExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class InitiateExternalAssetOwnerTransferTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ExternalAssetOwnerHelper externalAssetOwnerHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private LocalDate todaysDate;

    @BeforeAll
    public static void setupInvestorBusinessStep() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        externalAssetOwnerHelper = new ExternalAssetOwnerHelper();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);

        todaysDate = Utils.getLocalDateOfTenant();
    }

    @Test
    public void saleActiveLoanToExternalAssetOwnerAndBuybackADayLater() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;

            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            String transferExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0"));
            assertEquals(transferExternalId, saleResponse.getResourceExternalId());

            PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());

            assertEquals(1, retrieveResponse.getNumberOfElements());
            ExternalTransferData externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 3));
            // Run the Loan COB Job
            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());

            assertEquals(2, retrieveResponse.getNumberOfElements());
            externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.ACTIVE, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());

            String buybackTransferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            PostInitiateTransferResponse buybackResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "buyback",
                    new PostInitiateTransferRequest().settlementDate("2020-03-03").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(buybackTransferExternalId));

            assertEquals(buybackResponse.getResourceExternalId(), buybackTransferExternalId);

            retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());

            assertEquals(3, retrieveResponse.getNumberOfElements());
            externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.ACTIVE, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(2);
            assertEquals(buybackTransferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.BUYBACK, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 4));

            schedulerJobHelper.executeAndAwaitJob(jobName);

            retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
            assertEquals(3, retrieveResponse.getNumberOfElements());
            externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.ACTIVE, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(2);
            assertEquals(buybackTransferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.BUYBACK, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 3), externalTransferData.getEffectiveTo());
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void saleIsNotAllowedWhenTransferIsAlreadyPending() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;

            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            String transferExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0"));
            assertEquals(transferExternalId, saleResponse.getResourceExternalId());

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                            new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                                    .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0")));
            assertTrue(exception.getMessage().contains("External asset owner transfer is already in PENDING state for this loan"));
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void saleIsNotAllowedWhenLoanIsNotActive() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;

            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 4));

            loanTransactionHelper.makeRepayment("04 March 2020", 16000.0f, loanID);

            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                            new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                                    .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0")));
            assertTrue(exception.getMessage().contains("Loan is not in active status"));
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void saleAndBuybackOnTheSameDay() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;

            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0"));
            assertEquals(transferExternalId, saleResponse.getResourceExternalId());

            PostInitiateTransferResponse buybackResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "buyback",
                    new PostInitiateTransferRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(transferExternalId));

            assertEquals(buybackResponse.getResourceExternalId(), transferExternalId);

            PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
            assertEquals(2, retrieveResponse.getNumberOfElements());
            ExternalTransferData externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.BUYBACK, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 3));
            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
            assertEquals(4, retrieveResponse.getNumberOfElements());
            externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.BUYBACK, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(2);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.CANCELLED, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(3);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.CANCELLED, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveTo());
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void saleAndBuybackMultipleTimes() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
            Assertions.assertNotNull(clientID);

            Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
            Assertions.assertNotNull(overdueFeeChargeId);

            final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
            Assertions.assertNotNull(loanProductID);
            HashMap loanStatusHashMap;

            final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null, "10 January 2020");

            Assertions.assertNotNull(loanID);

            loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

            String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
            loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    new PostInitiateTransferRequest().settlementDate("2020-03-04").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0"));

            assertEquals(transferExternalId, saleResponse.getResourceExternalId());

            String buybackTransferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            PostInitiateTransferResponse buybackResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "buyback",
                    new PostInitiateTransferRequest().settlementDate("2020-03-04").dateFormat("yyyy-MM-dd").locale("en")
                            .transferExternalId(buybackTransferExternalId));

            assertEquals(buybackResponse.getResourceExternalId(), buybackTransferExternalId);
            PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
            assertEquals(2, retrieveResponse.getNumberOfElements());
            ExternalTransferData externalTransferData = retrieveResponse.getContent().get(0);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.PENDING, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 4), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());
            externalTransferData = retrieveResponse.getContent().get(1);
            assertEquals(transferExternalId, externalTransferData.getTransferExternalId());
            assertEquals(ExternalTransferData.StatusEnum.BUYBACK, externalTransferData.getStatus());
            assertEquals(LocalDate.of(2020, 3, 4), externalTransferData.getSettlementDate());
            assertEquals(LocalDate.of(2020, 3, 2), externalTransferData.getEffectiveFrom());
            assertEquals(LocalDate.of(9999, 12, 31), externalTransferData.getEffectiveTo());

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                            new PostInitiateTransferRequest().settlementDate("2020-03-04").dateFormat("yyyy-MM-dd").locale("en")
                                    .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0")));

            assertTrue(exception.getMessage().contains("This loan cannot be sold, there is already an in progress transfer"));

            CallFailedRuntimeException exception2 = assertThrows(CallFailedRuntimeException.class,
                    () -> externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "buyback",
                            new PostInitiateTransferRequest().settlementDate("2020-03-04").dateFormat("yyyy-MM-dd").locale("en")
                                    .transferExternalId(buybackTransferExternalId)));
            assertTrue(exception2.getMessage()
                    .contains("This loan cannot be bought back, external asset owner buyback transfer is already in progress"));
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String savingsID, final String date) {

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID,
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(date).withSubmittedOnDate(date).withCollaterals(collaterals)
                .build(clientID, loanProductID, savingsID);
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
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
