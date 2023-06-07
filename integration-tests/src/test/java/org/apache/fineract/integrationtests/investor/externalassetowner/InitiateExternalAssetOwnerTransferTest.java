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

import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.ACTIVE;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.BUYBACK;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.CANCELLED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.PENDING;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.apache.fineract.integrationtests.investor.externalassetowner.InitiateExternalAssetOwnerTransferTest.ExpectedExternalTransferData.expected;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("rawtypes")
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
            setInitialBusinessDate("2020-03-02");
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID);
            addPenaltyForLoan(loanID, "10");

            String transferExternalId = createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-02");
            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "9999-12-31", false));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(ACTIVE, transferExternalId, "2020-03-02", "2020-03-03", "9999-12-31", true));

            String buybackTransferExternalId = createExternalAssetOwnerTransfer(loanID, "buyback", "2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(ACTIVE, transferExternalId, "2020-03-02", "2020-03-03", "9999-12-31", true),
                    expected(BUYBACK, buybackTransferExternalId, "2020-03-03", "2020-03-03", "9999-12-31", false));

            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(ACTIVE, transferExternalId, "2020-03-02", "2020-03-03", "2020-03-03", true),
                    expected(BUYBACK, buybackTransferExternalId, "2020-03-03", "2020-03-03", "2020-03-03", true));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleIsNotAllowedWhenTransferIsAlreadyPending() {
        try {
            setInitialBusinessDate("2020-03-02");
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID);

            createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-02");

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-02"));
            assertTrue(exception.getMessage().contains("External asset owner transfer is already in PENDING state for this loan"));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleIsNotAllowedWhenLoanIsNotActive() {
        try {
            setInitialBusinessDate("2020-03-02");
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID);

            updateBusinessDateAndExecuteCOBJob("2020-03-04");

            loanTransactionHelper.makeRepayment("04 March 2020", 16000.0f, loanID);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-02"));
            assertTrue(exception.getMessage().contains("Loan is not in active status"));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleAndBuybackOnTheSameDay() {
        try {
            setInitialBusinessDate("2020-03-02");
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID);

            String transferExternalId = createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-02");
            String buyBackTransferExternalId = createExternalAssetOwnerTransfer(loanID, "buyback", "2020-03-02");

            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "9999-12-31", false),
                    expected(BUYBACK, buyBackTransferExternalId, "2020-03-02", "2020-03-02", "9999-12-31", false));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");

            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(BUYBACK, buyBackTransferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(CANCELLED, buyBackTransferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false),
                    expected(CANCELLED, transferExternalId, "2020-03-02", "2020-03-02", "2020-03-02", false));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleAndBuybackMultipleTimes() {
        try {
            setInitialBusinessDate("2020-03-02");
            Integer clientID = createClient();
            Integer loanID = createLoanForClient(clientID);

            String transferExternalId = createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-04");
            String buybackTransferExternalId = createExternalAssetOwnerTransfer(loanID, "buyback", "2020-03-04");

            getAndValidateExternalAssetOwnerTransferByLoan(loanID,
                    expected(PENDING, transferExternalId, "2020-03-04", "2020-03-02", "9999-12-31", false),
                    expected(BUYBACK, buybackTransferExternalId, "2020-03-04", "2020-03-02", "9999-12-31", false));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createExternalAssetOwnerTransfer(loanID, "sale", "2020-03-04"));
            assertTrue(exception.getMessage().contains("This loan cannot be sold, there is already an in progress transfer"));

            CallFailedRuntimeException exception2 = assertThrows(CallFailedRuntimeException.class,
                    () -> createExternalAssetOwnerTransfer(loanID, "buyback", "2020-03-04"));
            assertTrue(exception2.getMessage()
                    .contains("This loan cannot be bought back, external asset owner buyback transfer is already in progress"));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    private void updateBusinessDateAndExecuteCOBJob(String date) {
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BUSINESS_DATE, LocalDate.parse(date));
        schedulerJobHelper.executeAndAwaitJob("Loan COB");
    }

    private String createExternalAssetOwnerTransfer(Integer loanID, String command, String settlementDate) {
        String transferExternalId = UUID.randomUUID().toString();
        PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanID.longValue(), command,
                new PostInitiateTransferRequest().settlementDate(settlementDate).dateFormat("yyyy-MM-dd").locale("en")
                        .transferExternalId(transferExternalId).ownerExternalId("1234567890").purchasePriceRatio("1.0"));
        assertEquals(transferExternalId, saleResponse.getResourceExternalId());
        return transferExternalId;
    }

    private void addPenaltyForLoan(Integer loanID, String amount) {
        // Add Charge Penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, amount, true));
        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "02 March 2020", amount));
        assertNotNull(penalty1LoanChargeId);
    }

    private void setInitialBusinessDate(String date) {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BUSINESS_DATE, LocalDate.parse(date));
        GlobalConfigurationHelper.updateValueForGlobalConfiguration(requestSpec, responseSpec, "10", "0");
    }

    private void cleanUpAndRestoreBusinessDate() {
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BUSINESS_DATE, todaysDate);
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
    }

    @NotNull
    private Integer createClient() {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        Assertions.assertNotNull(clientID);
        return clientID;
    }

    @NotNull
    private Integer createLoanForClient(Integer clientID) {
        Integer overdueFeeChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanOverdueFeeJSONWithCalculationTypePercentage("1"));
        Assertions.assertNotNull(overdueFeeChargeId);

        Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        Assertions.assertNotNull(loanProductID);
        HashMap loanStatusHashMap;

        Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), "10 January 2020");

        Assertions.assertNotNull(loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = loanTransactionHelper.approveLoan("01 March 2020", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        String loanDetails = loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = loanTransactionHelper.disburseLoanWithNetDisbursalAmount("02 March 2020", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        return loanID;
    }

    private Integer createLoanProduct(final String chargeId) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .build(chargeId);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String date) {
        List<HashMap> collaterals = new ArrayList<>();
        Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID, collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15,000.00").withLoanTermFrequency("4")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("4").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(date).withSubmittedOnDate(date).withCollaterals(collaterals)
                .build(clientID, loanProductID, null);
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

    @RequiredArgsConstructor()
    public static class ExpectedExternalTransferData {

        private final ExternalTransferData.StatusEnum status;
        private final String transferExternalId;
        private final String settlementDate;
        private final String effectiveFrom;
        private final String effectiveTo;
        private final boolean detailsExpected;

        static ExpectedExternalTransferData expected(ExternalTransferData.StatusEnum status, String transferExternalId,
                String settlementDate, String effectiveFrom, String effectiveTo, boolean detailsExpected) {
            return new ExpectedExternalTransferData(status, transferExternalId, settlementDate, effectiveFrom, effectiveTo,
                    detailsExpected);
        }

    }

    private void getAndValidateExternalAssetOwnerTransferByLoan(Integer loanID, ExpectedExternalTransferData... expectedItems) {
        PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransferByLoanId(loanID.longValue());
        assertEquals(expectedItems.length, retrieveResponse.getNumberOfElements());

        for (ExpectedExternalTransferData expected : expectedItems) {
            assertNotNull(retrieveResponse.getContent());
            Optional<ExternalTransferData> first = retrieveResponse.getContent().stream()
                    .filter(e -> Objects.equals(e.getTransferExternalId(), expected.transferExternalId)
                            && Objects.equals(e.getStatus(), expected.status))
                    .findFirst();
            assertTrue(first.isPresent());
            ExternalTransferData etd = first.get();
            assertEquals(expected.transferExternalId, etd.getTransferExternalId());
            assertEquals(expected.status, etd.getStatus());
            assertEquals(LocalDate.parse(expected.settlementDate), etd.getSettlementDate());
            assertEquals(LocalDate.parse(expected.effectiveFrom), etd.getEffectiveFrom());
            assertEquals(LocalDate.parse(expected.effectiveTo), etd.getEffectiveTo());
            if (!expected.detailsExpected) {
                assertNull(etd.getDetails());
            } else {
                assertNotNull(etd.getDetails());
                assertEquals(new BigDecimal("15767.420000"), etd.getDetails().getTotalOutstanding());
                assertEquals(new BigDecimal("15000.000000"), etd.getDetails().getTotalPrincipalOutstanding());
                assertEquals(new BigDecimal("757.420000"), etd.getDetails().getTotalInterestOutstanding());
                assertEquals(new BigDecimal("10.000000"), etd.getDetails().getTotalPenaltyChargesOutstanding());
                assertEquals(new BigDecimal("0.000000"), etd.getDetails().getTotalFeeChargesOutstanding());
                assertEquals(new BigDecimal("0.000000"), etd.getDetails().getTotalOverpaid());
            }
        }
    }

}
