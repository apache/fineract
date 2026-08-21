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

import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.BUYBACK;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.CANCELLED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ LoanTestLifecycleExtension.class })
public class ExternalAssetOwnerTransferCancelTest extends FeignLoanTestBase {

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    private static final String SALE_DATE = "2020-03-02";
    private static final String SALE_DATE_TEXT = "02 March 2020";
    private static final String SUBMIT_DATE_TEXT = "01 March 2020";

    private final FeignExternalAssetOwnerHelper externalAssetOwnerHelper = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @BeforeAll
    public static void setupInvestorBusinessStep() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @Test
    public void successCancelSale() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);
        try {
            runAt(SALE_DATE_TEXT, () -> {
                Long loanId = createLoanForClient();
                addCharge(loanId, true, 10.0, SALE_DATE_TEXT);

                PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, SALE_DATE);
                validateResponse(saleTransferResponse, loanId);
                getAndValidateExternalAssetOwnerTransferByLoan(loanId, ExpectedExternalTransferData.expected(PENDING,
                        saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE, "9999-12-31"));

                PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransfersByLoanId(loanId);
                retrieveResponse.getContent()
                        .forEach(transfer -> getAndValidateThereIsNoJournalEntriesForTransfer(transfer.getTransferId()));

                externalAssetOwnerHelper.cancelTransferByTransferExternalId(saleTransferResponse.getResourceExternalId());

                // updateBusinessDateAndExecuteCOBJob("2020-03-03");
                getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                        ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                SALE_DATE),
                        ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                SALE_DATE));
            });
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void saleAndBuybackOnTheSameDay() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
        Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);
        try {
            runAt(SALE_DATE_TEXT, () -> {
                Long loanId = createLoanForClient();

                PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, SALE_DATE);
                validateResponse(saleTransferResponse, loanId);
                PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, SALE_DATE);
                validateResponse(buybackTransferResponse, loanId);

                getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                        ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                "9999-12-31"),
                        ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), SALE_DATE,
                                SALE_DATE, "9999-12-31"));
                getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
                getAndValidateThereIsNoActiveMapping(buybackTransferResponse.getResourceExternalId());

                externalAssetOwnerHelper.cancelTransferByTransferExternalIdError(saleTransferResponse.getResourceExternalId());

                externalAssetOwnerHelper.cancelTransferByTransferExternalId(buybackTransferResponse.getResourceExternalId());

                getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                        ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                "9999-12-31"),
                        ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), SALE_DATE,
                                SALE_DATE, SALE_DATE),
                        ExpectedExternalTransferData.expected(CANCELLED, buybackTransferResponse.getResourceExternalId(), SALE_DATE,
                                SALE_DATE, SALE_DATE));

                externalAssetOwnerHelper.cancelTransferByTransferExternalId(saleTransferResponse.getResourceExternalId());

                getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                        ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                SALE_DATE),
                        ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), SALE_DATE,
                                SALE_DATE, SALE_DATE),
                        ExpectedExternalTransferData.expected(CANCELLED, buybackTransferResponse.getResourceExternalId(), SALE_DATE,
                                SALE_DATE, SALE_DATE),
                        ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), SALE_DATE, SALE_DATE,
                                SALE_DATE));
                getAndValidateThereIsNoActiveMapping(loanId);
            });
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    private Long createLoanForClient() {
        Long clientId = createClient();
        PostLoanProductsRequest productRequest = createOnePeriod30DaysPeriodicAccrualProduct(2.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .principal(15000.0);
        Long loanProductId = createLoanProduct(productRequest);

        PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductId, SUBMIT_DATE_TEXT, 15000.0, 4, request -> request//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .dateFormat(LoanTestData.DATETIME_PATTERN).expectedDisbursementDate(SALE_DATE_TEXT).submittedOnDate(SUBMIT_DATE_TEXT));
        Long loanId = applyForLoan(loanRequest);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        approveLoan(loanId, approveLoanRequest(15000.0, SUBMIT_DATE_TEXT));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        disburseLoan(loanId, SALE_DATE_TEXT, 15000.0);
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);
        return loanId;
    }

    private PostInitiateTransferResponse createSaleTransfer(Long loanId, String settlementDate) {
        String transferExternalId = UUID.randomUUID().toString();
        String ownerExternalId = UUID.randomUUID().toString();
        PostInitiateTransferResponse saleResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanId, "sale",
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale("en")
                        .transferExternalId(transferExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));
        assertEquals(transferExternalId, saleResponse.getResourceExternalId());
        return saleResponse;
    }

    private PostInitiateTransferResponse createBuybackTransfer(Long loanId, String settlementDate) {
        String transferExternalId = UUID.randomUUID().toString();
        PostInitiateTransferResponse buybackResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanId, "buyback",
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale("en")
                        .transferExternalId(transferExternalId));
        assertEquals(transferExternalId, buybackResponse.getResourceExternalId());
        return buybackResponse;
    }

    private void getAndValidateThereIsNoJournalEntriesForTransfer(Long transferId) {
        ExternalOwnerTransferJournalEntryData result = externalAssetOwnerHelper.retrieveJournalEntriesOfTransfer(transferId);
        // Add Charge Penalty
        assertNull(result.getJournalEntryData());
    }

    private void getAndValidateExternalAssetOwnerTransferByLoan(Long loanId, ExpectedExternalTransferData... expectedItems) {
        PageExternalTransferData retrieveResponse = externalAssetOwnerHelper.retrieveTransfersByLoanId(loanId);
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
            assertNull(etd.getDetails());
        }
    }

    private void getAndValidateThereIsNoActiveMapping(Long loanId) {
        ExternalTransferData activeTransfer = externalAssetOwnerHelper.retrieveActiveTransferByLoanId(loanId);
        assertNull(activeTransfer);
    }

    private void getAndValidateThereIsNoActiveMapping(String transferExternalId) {
        ExternalTransferData activeTransfer = externalAssetOwnerHelper.retrieveActiveTransferByTransferExternalId(transferExternalId);
        assertNull(activeTransfer);
    }

    private void validateResponse(PostInitiateTransferResponse transferResponse, Long loanId) {
        assertNotNull(transferResponse);
        assertNotNull(transferResponse.getResourceId());
        assertNotNull(transferResponse.getResourceExternalId());
        assertNotNull(transferResponse.getSubResourceId());
        assertEquals(loanId, transferResponse.getSubResourceId());
        assertNotNull(transferResponse.getSubResourceExternalId());
        assertNull(transferResponse.getChanges());
    }

    @RequiredArgsConstructor
    private static final class ExpectedExternalTransferData {

        private final ExternalTransferData.StatusEnum status;
        private final String transferExternalId;
        private final String settlementDate;
        private final String effectiveFrom;
        private final String effectiveTo;

        static ExpectedExternalTransferData expected(ExternalTransferData.StatusEnum status, String transferExternalId,
                String settlementDate, String effectiveFrom, String effectiveTo) {
            return new ExpectedExternalTransferData(status, transferExternalId, settlementDate, effectiveFrom, effectiveTo);
        }
    }
}
