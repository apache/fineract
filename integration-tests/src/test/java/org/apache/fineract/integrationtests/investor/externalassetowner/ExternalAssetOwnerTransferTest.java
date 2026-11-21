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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignMakerCheckerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;

@Order(1)
public class ExternalAssetOwnerTransferTest extends FeignLoanTestBase {

    protected static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    protected static final String LOAN_COB_JOB = "Loan COB";
    protected static final String BUYBACK_COMMAND = "buyback";
    protected static final String INTERMEDIARY_SALE_COMMAND = "intermediarySale";
    protected static final String PENALTY_DUE_DATE = "02 March 2020";
    protected static final Double LOAN_PRINCIPAL = 15000.0;
    protected static final Integer LOAN_REPAYMENTS = 4;
    protected static final BigDecimal LOAN_INTEREST_RATE = BigDecimal.valueOf(2);

    protected static final FeignExternalAssetOwnerHelper EXTERNAL_ASSET_OWNER_HELPER = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    protected static final FeignMakerCheckerHelper MAKER_CHECKER_HELPER = new FeignMakerCheckerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    /** The GL account the ASSET_TRANSFER financial activity is mapped to; every sale and buyback books against it. */
    protected static Account transferAccount;

    public String ownerExternalId;

    @BeforeAll
    public static void setupInvestorBusinessStep() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");

        transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        EXTERNAL_ASSET_OWNER_HELPER.setProperFinancialActivity(transferAccount);
    }

    protected void updateBusinessDateAndExecuteCOBJob(String date) {
        updateBusinessDate(date);
        schedulerHelper.executeAndAwaitJob(LOAN_COB_JOB);
    }

    protected PostInitiateTransferResponse createSaleTransfer(Long loanId, String settlementDate) {
        String transferExternalId = UUID.randomUUID().toString();
        String transferExternalGroupId = UUID.randomUUID().toString();
        ownerExternalId = UUID.randomUUID().toString();
        return createSaleTransfer(loanId, settlementDate, transferExternalId, transferExternalGroupId, ownerExternalId, "1.0");
    }

    protected PostInitiateTransferResponse createSaleTransfer(Long loanId, String settlementDate, String transferExternalId,
            String transferExternalGroupId, String ownerExternalId, String purchasePriceRatio) {
        PostInitiateTransferResponse saleResponse = EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId, "sale",
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale("en")
                        .transferExternalId(transferExternalId).transferExternalGroupId(transferExternalGroupId)
                        .ownerExternalId(ownerExternalId).purchasePriceRatio(purchasePriceRatio));
        assertEquals(transferExternalId, saleResponse.getResourceExternalId());
        return saleResponse;
    }

    protected PostInitiateTransferResponse createBuybackTransfer(Long loanId, String settlementDate) {
        return createBuybackTransfer(loanId, settlementDate, UUID.randomUUID().toString());
    }

    protected PostInitiateTransferResponse createBuybackTransfer(Long loanId, String settlementDate, String transferExternalId) {
        PostInitiateTransferResponse buybackResponse = EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId, BUYBACK_COMMAND,
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale(LoanTestData.LOCALE)
                        .transferExternalId(transferExternalId));
        assertEquals(transferExternalId, buybackResponse.getResourceExternalId());
        return buybackResponse;
    }

    protected PostInitiateTransferResponse createIntermediarySaleTransfer(Long loanId, String settlementDate, String ownerExternalId) {
        String transferExternalId = UUID.randomUUID().toString();
        PostInitiateTransferResponse intermediarySaleResponse = EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId,
                INTERMEDIARY_SALE_COMMAND,
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale(LoanTestData.LOCALE)
                        .transferExternalId(transferExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));
        assertEquals(transferExternalId, intermediarySaleResponse.getResourceExternalId());
        return intermediarySaleResponse;
    }

    protected void addPenaltyForLoan(Long loanId, String amount) {
        Long penaltyLoanChargeId = addCharge(loanId, true, Double.parseDouble(amount), PENALTY_DUE_DATE);
        assertNotNull(penaltyLoanChargeId);
    }

    protected void setInitialBusinessDate(LocalDate date) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(date.toString());
    }

    protected void cleanUpAndRestoreBusinessDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
    }

    protected Long createLoanForClient(Long clientId, String transactionDate) {
        return createLoanForClient(clientId, transactionDate, transferrableLoanProduct());
    }

    /** The loan product shape every transfer test relies on: 15000 principal over 4 monthly installments at 2%. */
    protected PostLoanProductsRequest transferrableLoanProduct() {
        return createOnePeriod30DaysPeriodicAccrualProduct(2.0)//
                .numberOfRepayments(LOAN_REPAYMENTS)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .installmentAmountInMultiplesOf(null)//
                .principal(LOAN_PRINCIPAL);
    }

    protected Long createLoanForClient(Long clientId, String transactionDate, PostLoanProductsRequest productRequest) {
        Long loanProductId = createLoanProduct(productRequest);

        PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductId, transactionDate, LOAN_PRINCIPAL, LOAN_REPAYMENTS,
                request -> request//
                        .interestRatePerPeriod(LOAN_INTEREST_RATE)//
                        .repaymentEvery(1)//
                        .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                        .loanTermFrequency(LOAN_REPAYMENTS)//
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS));
        Long loanId = applyForLoan(loanRequest);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        approveLoan(loanId, approveLoanRequest(LOAN_PRINCIPAL, transactionDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        disburseLoan(loanId, transactionDate, LOAN_PRINCIPAL);
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);
        return loanId;
    }

    protected void writeOffLoan(String date, Long loanId) {
        transactionHelper.writeOff(loanId, LoanRequestBuilders.writeOff(date));
    }

    protected void getAndValidateExternalAssetOwnerTransferByLoan(Long loanId, ExpectedExternalTransferData... expectedItems) {
        PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
        assertEquals(expectedItems.length, retrieveResponse.getNumberOfElements());
        validateExternalAssetOwnerTransfer(retrieveResponse, expectedItems);
    }

    protected void validateExternalAssetOwnerTransfer(PageExternalTransferData response, ExpectedExternalTransferData... expectedItems) {
        for (ExpectedExternalTransferData expected : expectedItems) {
            assertNotNull(response.getContent());
            Optional<ExternalTransferData> first = response.getContent().stream()
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
                assertEquals(expected.totalOutstanding, etd.getDetails().getTotalOutstanding());
                assertEquals(expected.totalPrincipalOutstanding, etd.getDetails().getTotalPrincipalOutstanding());
                assertEquals(expected.totalInterestOutstanding, etd.getDetails().getTotalInterestOutstanding());
                assertEquals(expected.totalPenaltyOutstanding, etd.getDetails().getTotalPenaltyChargesOutstanding());
                assertEquals(expected.totalFeeOutstanding, etd.getDetails().getTotalFeeChargesOutstanding());
                assertEquals(expected.totalOverpaid, etd.getDetails().getTotalOverpaid());
            }
            if (expected.subStatus != null) {
                assertEquals(expected.subStatus, etd.getSubStatus());
            }
        }
    }

    protected void getAndValidateThereIsActiveMapping(Long loanId) {
        ExternalTransferData activeTransfer = EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId);
        assertNotNull(activeTransfer);
        ExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId).getContent().stream()
                .filter(transfer -> ExternalTransferData.StatusEnum.ACTIVE.equals(transfer.getStatus())).findFirst().get();
        assertEquals(retrieveResponse.getTransferId(), activeTransfer.getTransferId());
    }

    protected void validateResponse(PostInitiateTransferResponse transferResponse, Long loanId) {
        assertNotNull(transferResponse);
        assertNotNull(transferResponse.getResourceId());
        assertNotNull(transferResponse.getResourceExternalId());
        assertNotNull(transferResponse.getSubResourceId());
        assertEquals(loanId, transferResponse.getSubResourceId());
        assertNotNull(transferResponse.getSubResourceExternalId());
        assertNull(transferResponse.getChanges());
    }

    /**
     * A progressive buy down fee product with periodic accrual accounting, booking everything against the given
     * accounts.
     */
    protected PostLoanProductsRequest buyDownFeeProduct(String namePrefix, Account assetAccount, Account incomeAccount,
            Account expenseAccount, Account liabilityAccount) {
        return withPeriodicAccrualAccounting(new PostLoanProductsRequest() //
                .name(Utils.uniqueRandomStringGenerator(namePrefix, 6)) //
                .shortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .currencyCode("USD") //
                .digitsAfterDecimal(2) //
                .principal(1000.0) //
                .numberOfRepayments(6) //
                .repaymentEvery(1) //
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L) //
                .interestRatePerPeriod(10.0) //
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS) //
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS) //
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE) //
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY) //
                .daysInMonthType(30) //
                .daysInYearType(360) //
                .isInterestRecalculationEnabled(false) //
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy") //
                .paymentAllocation(List.of(createDefaultPaymentAllocation("NEXT_INSTALLMENT"))) //
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL") //
                .enableBuyDownFee(true) //
                .merchantBuyDownFee(true) //
                .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT) //
                .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION) //
                .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE) //
                .locale(LoanTestData.LOCALE) //
                .dateFormat(LoanTestData.DATETIME_PATTERN), assetAccount, incomeAccount, expenseAccount, liabilityAccount) //
                .buyDownExpenseAccountId(expenseAccount.getAccountID().longValue()) //
                .incomeFromBuyDownAccountId(incomeAccount.getAccountID().longValue()) //
                .deferredIncomeLiabilityAccountId(liabilityAccount.getAccountID().longValue());
    }

    /** Neither the journal entries nor the event of the loan transaction may name an external asset owner. */
    protected void assertUntaggedOnBothSurfaces(Long loanTransactionId) {
        List<JournalEntryTransactionItem> entries = journalEntriesOf(loanTransactionId);
        assertFalse(entries.isEmpty(), "expected journal entries for loan transaction " + loanTransactionId);
        entries.forEach(entry -> assertNull(entry.getExternalAssetOwner(),
                "journal entry of loan transaction " + loanTransactionId + " must not be attributed to any owner"));
        assertEventOwner(loanTransactionId, null);
    }

    protected void assertTaggedOnBothSurfaces(Long loanTransactionId, String ownerExternalId) {
        assertJournalEntriesTagged(loanTransactionId, ownerExternalId);
        assertEventOwner(loanTransactionId, ownerExternalId);
    }

    protected void assertJournalEntriesTagged(Long loanTransactionId, String ownerExternalId) {
        List<JournalEntryTransactionItem> entries = journalEntriesOf(loanTransactionId);
        assertFalse(entries.isEmpty(), "expected journal entries for loan transaction " + loanTransactionId);
        entries.forEach(entry -> assertEquals(ownerExternalId, entry.getExternalAssetOwner(),
                "journal entry of loan transaction " + loanTransactionId + " must be attributed to the owner"));
    }

    protected List<JournalEntryTransactionItem> journalEntriesOf(Long loanTransactionId) {
        List<JournalEntryTransactionItem> items = journalHelper.getJournalEntriesByTransactionId("L" + loanTransactionId).getPageItems();
        return items == null ? List.of() : items;
    }

    /**
     * External events are flushed as the surrounding transaction commits, so the event may not be readable the instant
     * the API call returns.
     */
    protected void assertEventOwner(Long loanTransactionId, String expectedOwnerExternalId) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<Map<String, Object>> payloads = externalEventHelper.getAllExternalEvents().stream().map(ExternalEventResponse::getPayLoad)
                    .filter(payload -> isTransactionPayload(payload, loanTransactionId)).toList();
            assertFalse(payloads.isEmpty(), "expected an external event for loan transaction " + loanTransactionId);
            payloads.forEach(payload -> assertEquals(expectedOwnerExternalId, payload.get("externalOwnerId"),
                    "event of loan transaction " + loanTransactionId + " carries the wrong externalOwnerId"));
        });
    }

    /**
     * A loan transaction payload, identified by id. externalOwnerId is not required to be present: the serialised
     * record omits it when it is null.
     */
    protected boolean isTransactionPayload(Map<String, Object> payload, Long loanTransactionId) {
        if (payload == null || payload.get("id") == null || payload.get("loanId") == null) {
            return false;
        }
        return loanTransactionId.equals(Long.valueOf(String.valueOf(payload.get("id"))));
    }

    @RequiredArgsConstructor
    public static class ExpectedExternalTransferData {

        private final ExternalTransferData.StatusEnum status;
        private final String transferExternalId;
        private final String settlementDate;
        private final String effectiveFrom;
        private final String effectiveTo;
        private final ExternalTransferData.SubStatusEnum subStatus;
        private final boolean detailsExpected;
        private final BigDecimal totalOutstanding;
        private final BigDecimal totalPrincipalOutstanding;
        private final BigDecimal totalInterestOutstanding;
        private final BigDecimal totalPenaltyOutstanding;
        private final BigDecimal totalFeeOutstanding;
        private final BigDecimal totalOverpaid;

        static ExpectedExternalTransferData expected(ExternalTransferData.StatusEnum status, String transferExternalId,
                String settlementDate, String effectiveFrom, String effectiveTo, boolean detailsExpected, BigDecimal totalOutstanding,
                BigDecimal totalPrincipalOutstanding, BigDecimal totalInterestOutstanding, BigDecimal totalPenaltyOutstanding,
                BigDecimal totalFeeOutstanding, BigDecimal totalOverpaid) {
            return new ExpectedExternalTransferData(status, transferExternalId, settlementDate, effectiveFrom, effectiveTo, null,
                    detailsExpected, totalOutstanding, totalPrincipalOutstanding, totalInterestOutstanding, totalPenaltyOutstanding,
                    totalFeeOutstanding, totalOverpaid);
        }

        /** Expects a transfer that carries no captured loan details; its sub-status is not asserted. */
        static ExpectedExternalTransferData expected(ExternalTransferData.StatusEnum status, String transferExternalId,
                String settlementDate, String effectiveFrom, String effectiveTo) {
            return new ExpectedExternalTransferData(status, transferExternalId, settlementDate, effectiveFrom, effectiveTo, null, false,
                    null, null, null, null, null, null);
        }

        /** Expects a transfer that carries no captured loan details, and asserts the reason it reached its status. */
        static ExpectedExternalTransferData expected(ExternalTransferData.StatusEnum status, String transferExternalId,
                String settlementDate, String effectiveFrom, String effectiveTo, ExternalTransferData.SubStatusEnum subStatus) {
            return new ExpectedExternalTransferData(status, transferExternalId, settlementDate, effectiveFrom, effectiveTo, subStatus,
                    false, null, null, null, null, null, null);
        }
    }
}
