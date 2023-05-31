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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InitiateExternalAssetOwnerTransferTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecError;
    private ResponseSpecification responseSpecNotFound;
    private RequestSpecification requestSpec;
    private ExternalAssetOwnerHelper externalAssetOwnerHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LocalDate todaysDate;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        responseSpecError = new ResponseSpecBuilder().expectStatusCode(403).build();
        responseSpecNotFound = new ResponseSpecBuilder().expectStatusCode(404).build();
        externalAssetOwnerHelper = new ExternalAssetOwnerHelper(requestSpec, responseSpec);
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);

        todaysDate = Utils.getLocalDateOfTenant();
    }

    @Test
    public void saleActiveLoanToExternalAssetOwner() {
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
            String saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    getSaleRequestJson("04 March 2020", transferExternalId));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> responseMap = new Gson().fromJson(saleResponse, type);
            assertEquals(responseMap.get("resourceExternalId"), transferExternalId);

            PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
            List<ExternalTransferData> retrieveResponseMap = retrieveResponse.getContent();
            assertEquals(1, retrieveResponse.getTotalElements());
            assertEquals(retrieveResponseMap.get(0).getTransferExternalId(), transferExternalId);
            assertEquals(retrieveResponseMap.get(0).getStatus(), ExternalTransferData.StatusEnum.PENDING);
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

            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            String saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    getSaleRequestJson("04 March 2020", transferExternalId));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> responseMap = new Gson().fromJson(saleResponse, type);
            assertEquals(responseMap.get("resourceExternalId"), transferExternalId);

            externalAssetOwnerHelper = new ExternalAssetOwnerHelper(requestSpec, responseSpecError);
            String errorResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    getSaleRequestJson("04 March 2020", transferExternalId));
            Map<String, Object> errorResponseMap = new Gson().fromJson(errorResponse, type);
            assertEquals("External asset owner transfer is already in PENDING state for this loan.",
                    errorResponseMap.get("developerMessage"));
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

            externalAssetOwnerHelper = new ExternalAssetOwnerHelper(requestSpec, responseSpecError);
            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            String saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    getSaleRequestJson("05 March 2020", transferExternalId));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> errorResponseMap = new Gson().fromJson(saleResponse, type);
            assertEquals("Loan is not in active status", errorResponseMap.get("developerMessage"));
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
            String saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "sale",
                    getSaleRequestJson("04 March 2020", transferExternalId));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> responseMap = new Gson().fromJson(saleResponse, type);
            assertEquals(responseMap.get("resourceExternalId"), transferExternalId);

            String buybackTransferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            String buybackResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), "buyback",
                    getSaleRequestJson("04 March 2020", buybackTransferExternalId));
            Map<String, Object> buybackResponseMap = new Gson().fromJson(buybackResponse, type);
            assertEquals(buybackResponseMap.get("resourceExternalId"), buybackTransferExternalId);

            PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue(), 0, 1);
            List<ExternalTransferData> retrieveResponseMap = retrieveResponse.getContent();

            assertEquals(2, retrieveResponse.getTotalElements());
            assertEquals(1, retrieveResponse.getNumberOfElements());
            assertEquals(retrieveResponseMap.get(0).getTransferExternalId(), transferExternalId);
            assertEquals(retrieveResponseMap.get(0).getStatus(), ExternalTransferData.StatusEnum.PENDING);

            retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue(), 1, 1);
            retrieveResponseMap = retrieveResponse.getContent();

            assertEquals(2, retrieveResponse.getTotalElements());
            assertEquals(1, retrieveResponse.getNumberOfElements());
            assertEquals(retrieveResponseMap.get(0).getTransferExternalId(), buybackTransferExternalId);
            assertEquals(retrieveResponseMap.get(0).getStatus(), ExternalTransferData.StatusEnum.BUYBACK);
        } finally {
            requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void getNotFoundErrorIfTheLoanDoesNotExistWithTheGivenID() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 3, 2));
            GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");

            externalAssetOwnerHelper = new ExternalAssetOwnerHelper(requestSpec, responseSpecNotFound);
            String transferExternalId = "36efeb06-d835-48a1-99eb-09bd1d348c1e";
            String nonExistingLoanExternalID = "NonExistingLoanExternalID";
            String saleResponse = externalAssetOwnerHelper.initiateTransferByLoanExternalId(nonExistingLoanExternalID, "sale",
                    getSaleRequestJson("05 March 2020", transferExternalId));
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> errorResponseMap = new Gson().fromJson(saleResponse, type);
            assertEquals("The requested resource is not available.", errorResponseMap.get("developerMessage"));
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

    private String getSaleRequestJson(String date, String transferExternalId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("settlementDate", date);
        map.put("ownerExternalId", "1234567890987654321");
        map.put("transferExternalId", transferExternalId);
        map.put("purchasePriceRatio", "1.234");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        return new Gson().toJson(map);
    }

}
