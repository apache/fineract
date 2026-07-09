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
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.DECLINED;
import static org.apache.fineract.client.models.ExternalTransferData.StatusEnum.PENDING;
import static org.apache.fineract.client.models.ExternalTransferData.SubStatusEnum.BALANCE_ZERO;
import static org.apache.fineract.client.models.ExternalTransferData.SubStatusEnum.SAMEDAY_TRANSFERS;
import static org.apache.fineract.client.models.ExternalTransferData.SubStatusEnum.UNSOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalOwnerJournalEntryData;
import org.apache.fineract.client.models.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryData;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostJournalEntriesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.client.models.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignReportHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventsExtension;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ ExternalEventsExtension.class })
@Order(1)
public class InitiateExternalAssetOwnerTransferTest extends ExternalAssetOwnerTransferTest {

    private static final String LOAN_OWNERSHIP_TRANSFER_EVENT = "LoanOwnershipTransferBusinessEvent";
    private static final String TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report with Asset Owner";
    private static final String SALE_COMMAND = "sale";
    private static final String BUYBACK_COMMAND = "buyback";

    private static final String LOAN_SUBMITTED_ON_DATE = "02 March 2020";
    private static final String PENALTY_AMOUNT = "10";
    private static final double OVERDUE_FEE_PERCENTAGE = 1.0;
    private static final double PARTIAL_REPAYMENT_AMOUNT = 5.0;
    private static final double FULL_PLUS_OVERPAYMENT_REPAYMENT_AMOUNT = 15777.42;
    private static final float OVERPAYING_REPAYMENT_AMOUNT = 16000.0f;

    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;

    private static final int PARALLEL_SALE_THREAD_COUNT = 10;
    private static final int PARALLEL_SALE_TIMEOUT_SECONDS = 30;
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 10;

    /**
     * Columns of the {@value #TRANSACTION_SUMMARY_REPORT} generic result set, in the order the report projects them.
     */
    private static final int REPORT_COLUMN_COUNT = 12;
    private static final int TRANSACTION_DATE_COLUMN = 0;
    private static final int PRODUCT_NAME_COLUMN = 1;
    private static final int TRANSACTION_TYPE_COLUMN = 2;
    private static final int PAYMENT_TYPE_COLUMN = 3;
    private static final int CHARGE_TYPE_COLUMN = 4;
    private static final int REVERSED_COLUMN = 5;
    private static final int ALLOCATION_TYPE_COLUMN = 6;
    private static final int CHARGE_OFF_REASON_COLUMN = 7;
    private static final int AMOUNT_COLUMN = 8;
    private static final int ASSET_OWNER_ID_COLUMN = 9;
    private static final int FROM_ASSET_OWNER_ID_COLUMN = 10;
    private static final int ORIGINATOR_EXTERNAL_IDS_COLUMN = 11;

    private static final String LOAN_PRODUCT_NAME_PATTERN = "^LOAN_PRODUCT_.{6}$";
    private static final String APPLY_CHARGES_TRANSACTION = "Apply Charges";
    private static final String ASSET_BUYBACK_TRANSACTION = "Asset Buyback";
    private static final String REPAYMENT_TRANSACTION = "Repayment";
    private static final String PRINCIPAL_ALLOCATION = "Principal";
    private static final String INTEREST_ALLOCATION = "Interest";
    private static final String FEES_ALLOCATION = "Fees";
    private static final String PENALTY_ALLOCATION = "Penalty";
    private static final String UNALLOCATED_CREDIT_ALLOCATION = "Unallocated Credit (UNC)";
    private static final double AMOUNT_TOLERANCE = 0.01;

    private static final String MANUAL_JOURNAL_ENTRY_DATE_FORMAT = "uuuu-MM-dd";
    private static final double MANUAL_ENTRY_LOAN_PRINCIPAL = 1000.0;
    private static final int MANUAL_ENTRY_LOAN_REPAYMENTS = 4;
    private static final double MANUAL_ENTRY_LOAN_INTEREST_RATE = 12.0;
    private static final int PRE_CLOSURE_INTEREST_CALCULATION_TILL_PRE_CLOSE_DATE = 1;

    private static Account assetAccount;
    private static Account feePenaltyAccount;
    private static Account expenseAccount;
    private static Account incomeAccount;
    private static Account overpaymentAccount;
    private static FeignOfficeHelper officeHelper;
    private static FeignReportHelper reportHelper;

    @BeforeAll
    public static void setupAssetOwnerAccounts() {
        assetAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("ASSET_", 5));
        feePenaltyAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("FEE_PENALTY_", 5));
        expenseAccount = accountHelper.createExpenseAccount(Utils.uniqueRandomStringGenerator("EXPENSE_", 5));
        incomeAccount = accountHelper.createIncomeAccount(Utils.uniqueRandomStringGenerator("INCOME_", 5));
        overpaymentAccount = accountHelper.createLiabilityAccount(Utils.uniqueRandomStringGenerator("OVERPAYMENT_", 5));
        officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());
        reportHelper = new FeignReportHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void saleActiveLoanToExternalAssetOwnerWithCancelAndBuybackADayLater() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));

            externalEventHelper.deleteAllExternalEvents();
            externalEventHelper.enableBusinessEvent(LOAN_OWNERSHIP_TRANSFER_EVENT);

            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);
            addPenaltyForLoan(loanId, PENALTY_AMOUNT);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            retrieveResponse.getContent().forEach(transfer -> getAndValidateThereIsNoJournalEntriesForTransfer(transfer.getTransferId()));

            EXTERNAL_ASSET_OWNER_HELPER.cancelTransferByTransferExternalId(saleTransferResponse.getResourceExternalId());

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            PostInitiateTransferResponse oldSaleTransferResponse = saleTransferResponse;
            saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            List<ExternalEventResponse> allExternalEvents = externalEventHelper.getAllExternalEvents();
            assertEquals(1, allExternalEvents.size());
            assertEquals(LOAN_OWNERSHIP_TRANSFER_EVENT, allExternalEvents.get(0).getType());
            assertEquals(loanId, allExternalEvents.get(0).getAggregateRootId());

            externalEventHelper.deleteAllExternalEvents();
            externalEventHelper.enableBusinessEvent(LOAN_OWNERSHIP_TRANSFER_EVENT);

            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            LocalDate expectedDate = LocalDate.of(2020, 3, 2);
            int initial = 2;
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(initial + 1).getTransferId(),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate));

            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-03");
            validateResponse(buybackTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            getAndValidateThereIsNoJournalEntriesForTransfer(retrieveResponse.getContent().get(initial + 2).getTransferId());

            makePartialRepayment(loanId, expectedDate);
            LocalDate repaymentSubmittedOnDate = expectedDate.plusDays(1);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, repaymentSubmittedOnDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), expectedDate,
                            repaymentSubmittedOnDate));

            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, oldSaleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "2020-03-03", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "2020-03-03", true, new BigDecimal("15762.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("5.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            expectedDate = LocalDate.of(2020, 3, 3);
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(initial + 2).getTransferId(),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15762.420000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15762.420000),
                            expectedDate, expectedDate));
            LocalDate previousDayDate = LocalDate.of(2020, 3, 2);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            previousDayDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), previousDayDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(9.680000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(incomeAccountId(), creditEntryTypeId(), BigDecimal.valueOf(9.680000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, expectedDate));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleActiveLoanToExternalAssetOwnerAndBuybackADayLater() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);
            addPenaltyForLoan(loanId, PENALTY_AMOUNT);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            retrieveResponse.getContent().forEach(transfer -> getAndValidateThereIsNoJournalEntriesForTransfer(transfer.getTransferId()));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            LocalDate expectedDate = LocalDate.of(2020, 3, 2);
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(1).getTransferId(),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate));

            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-03");
            validateResponse(buybackTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            getAndValidateThereIsNoJournalEntriesForTransfer(retrieveResponse.getContent().get(2).getTransferId());

            makePartialRepayment(loanId, expectedDate);
            LocalDate repaymentSubmittedOnDate = expectedDate.plusDays(1);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, repaymentSubmittedOnDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), expectedDate,
                            repaymentSubmittedOnDate));

            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "2020-03-03", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "2020-03-03", true, new BigDecimal("15762.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("5.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            expectedDate = LocalDate.of(2020, 3, 3);
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(2).getTransferId(),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15762.420000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15762.420000),
                            expectedDate, expectedDate));
            LocalDate previousDayDate = LocalDate.of(2020, 3, 2);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            previousDayDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(5.000000), previousDayDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(9.680000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(incomeAccountId(), creditEntryTypeId(), BigDecimal.valueOf(9.680000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(5.000000),
                            expectedDate, expectedDate));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleOverpaidLoanToExternalAssetOwnerAndBuybackADayLater() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);
            addPenaltyForLoan(loanId, PENALTY_AMOUNT);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            retrieveResponse.getContent().forEach(transfer -> getAndValidateThereIsNoJournalEntriesForTransfer(transfer.getTransferId()));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            LocalDate expectedDate = LocalDate.of(2020, 3, 2);
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(1).getTransferId(),
                    ExpectedJournalEntryData.expected(assetAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), creditEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(15767.420000),
                            expectedDate, expectedDate));

            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-03");
            validateResponse(buybackTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            getAndValidateThereIsNoJournalEntriesForTransfer(retrieveResponse.getContent().get(2).getTransferId());

            transactionHelper.makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                            .transactionDate(dateTimeFormatter.format(expectedDate)).locale(LoanTestData.LOCALE)
                            .transactionAmount(FULL_PLUS_OVERPAYMENT_REPAYMENT_AMOUNT));
            LocalDate repaymentSubmittedOnDate = expectedDate.plusDays(1);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(overpaymentAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            repaymentSubmittedOnDate, repaymentSubmittedOnDate));

            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "2020-03-03", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "2020-03-03", true, new BigDecimal("0.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("10.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            expectedDate = LocalDate.of(2020, 3, 3);
            getAndValidateThereIsJournalEntriesForTransfer(retrieveResponse.getContent().get(2).getTransferId(),
                    ExpectedJournalEntryData.expected(overpaymentAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), creditEntryTypeId(), BigDecimal.valueOf(10.000000), expectedDate,
                            expectedDate),
                    ExpectedJournalEntryData.expected(overpaymentAccountId(), creditEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate),
                    ExpectedJournalEntryData.expected(transferAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000), expectedDate,
                            expectedDate));
            LocalDate previousDayDate = LocalDate.of(2020, 3, 2);
            getAndValidateOwnerJournalEntries(ownerExternalId,
                    ExpectedJournalEntryData.expected(assetAccountId(), debitEntryTypeId(), BigDecimal.valueOf(15757.420000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(feePenaltyAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            previousDayDate, previousDayDate),
                    ExpectedJournalEntryData.expected(overpaymentAccountId(), debitEntryTypeId(), BigDecimal.valueOf(10.000000),
                            expectedDate, expectedDate));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleIsNotAllowedWhenTransferIsAlreadyPending() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            createSaleTransfer(loanId, "2020-03-02");

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createSaleTransfer(loanId, "2020-03-02"));
            assertEquals(FORBIDDEN, exception.getStatus());
            assertTrue(exception.getMessage().contains("External asset owner transfer is already in PENDING state for this loan"));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleIsNotAllowedWhenLoanIsNotActive() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            updateBusinessDateAndExecuteCOBJob("2020-03-04");

            makeRepayment("04 March 2020", OVERPAYING_REPAYMENT_AMOUNT, loanId);

            LoanStatus loanStatus = LoanStatus.fromInt(getLoanDetails(loanId).getStatus().getId().intValue());

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createSaleTransfer(loanId, "2020-03-02"));
            assertEquals(FORBIDDEN, exception.getStatus());
            assertTrue(exception.getMessage().contains(String.format("Loan status %s is not valid for transfer.", loanStatus)));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleIsDeclinedWhenLoanIsCancelled() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-06");
            updateBusinessDateAndExecuteCOBJob("2020-03-04");

            writeOffLoan("04 March 2020", loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-06", "2020-03-02",
                            "2020-03-04"),
                    ExpectedExternalTransferData.expected(DECLINED, saleTransferResponse.getResourceExternalId(), "2020-03-06",
                            "2020-03-04", "2020-03-04", BALANCE_ZERO));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void buybackIsExecutedWhenLoanIsCancelled() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-04");
            updateBusinessDateAndExecuteCOBJob("2020-03-05");
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-06");

            writeOffLoan("04 March 2020", loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-04", "2020-03-02",
                            "2020-03-04"),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-04", "2020-03-05",
                            "2020-03-05", true, new BigDecimal("15757.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("0.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-06",
                            "2020-03-05", "2020-03-05", true, new BigDecimal("0.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000"), new BigDecimal("0.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void buybackAndSaleIsCancelledWhenLoanIsCancelled() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-04");
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-06");

            writeOffLoan("02 March 2020", loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-04", "2020-03-02",
                            "2020-03-02"),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-06",
                            "2020-03-02", "2020-03-02"),
                    ExpectedExternalTransferData.expected(CANCELLED, buybackTransferResponse.getResourceExternalId(), "2020-03-06",
                            "2020-03-02", "2020-03-02", UNSOLD),
                    ExpectedExternalTransferData.expected(DECLINED, saleTransferResponse.getResourceExternalId(), "2020-03-04",
                            "2020-03-02", "2020-03-02", BALANCE_ZERO));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void sameDayBuybackAndSaleIsCancelledWhenLoanIsCancelled() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-03");
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-03");

            writeOffLoan("02 March 2020", loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-03", "2020-03-02",
                            "2020-03-02"),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-02", "2020-03-02"),
                    ExpectedExternalTransferData.expected(CANCELLED, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-02", "2020-03-02", SAMEDAY_TRANSFERS),
                    ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-02", "2020-03-02", SAMEDAY_TRANSFERS));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleAndBuybackOnTheSameDay() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-02");
            validateResponse(buybackTransferResponse, loanId);

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            getAndValidateThereIsNoActiveMapping(buybackTransferResponse.getResourceExternalId());

            updateBusinessDateAndExecuteCOBJob("2020-03-03");

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, buybackTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(CANCELLED, saleTransferResponse.getResourceExternalId(), "2020-03-02",
                            "2020-03-02", "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(loanId);
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleAndBuybackMultipleTimes() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-04");
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-04");

            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-04", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-04",
                            "2020-03-02", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createSaleTransfer(loanId, "2020-03-04"));
            assertEquals(FORBIDDEN, exception.getStatus());
            assertTrue(exception.getMessage().contains("This loan cannot be sold, there is already an in progress transfer"));

            CallFailedRuntimeException exception2 = assertThrows(CallFailedRuntimeException.class,
                    () -> createBuybackTransfer(loanId, "2020-03-04"));
            assertEquals(FORBIDDEN, exception2.getStatus());
            assertTrue(exception2.getMessage()
                    .contains("This loan cannot be bought back, external asset owner buyback transfer is already in progress"));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void buybackExceptionHandling() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createBuybackTransfer(FeignOfficeHelper.HEAD_OFFICE_ID, null));
            assertEquals(BAD_REQUEST, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `settlementDate` is mandatory."));

            CallFailedRuntimeException exception2 = assertThrows(CallFailedRuntimeException.class, () -> {
                Long clientId = createClient();
                Long loanId = createLoanForClient(clientId);
                createBuybackTransfer(loanId, "1970-01-01");
            });
            assertEquals(FORBIDDEN, exception2.getStatus());
            assertTrue(exception2.getMessage().contains("Settlement date cannot be in the past"));

            CallFailedRuntimeException exception3 = assertThrows(CallFailedRuntimeException.class, () -> {
                Long clientId = createClient();
                Long loanId = createLoanForClient(clientId);
                createSaleTransfer(loanId, "2020-03-03");
                createBuybackTransfer(loanId, "2020-03-02");
            });
            assertEquals(FORBIDDEN, exception3.getStatus());
            assertTrue(exception3.getMessage().contains(
                    "This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: 2020-03-03"));

            CallFailedRuntimeException exception4 = assertThrows(CallFailedRuntimeException.class, () -> {
                Long clientId = createClient();
                Long loanId = createLoanForClient(clientId);
                createBuybackTransfer(loanId, "2020-03-03");
            });
            assertEquals(FORBIDDEN, exception4.getStatus());
            assertTrue(exception4.getMessage().contains("This loan cannot be bought back, it is not owned by an external asset owner"));

            CallFailedRuntimeException exception5 = assertThrows(CallFailedRuntimeException.class,
                    () -> createBuybackTransfer(-1L, "2020-03-03"));
            assertEquals(NOT_FOUND, exception5.getStatus());
            assertTrue(exception5.getMessage().contains("Loan with identifier -1 does not exist"));

            String externalId = UUID.randomUUID().toString();
            String transferExternalGroupId = UUID.randomUUID().toString();

            CallFailedRuntimeException exception6 = assertThrows(CallFailedRuntimeException.class, () -> {
                Long clientId = createClient();
                Long loanId = createLoanForClient(clientId);
                createSaleTransfer(loanId, "2020-03-03", externalId, transferExternalGroupId, "1", "1.0");
                createBuybackTransfer(loanId, "2020-03-02", externalId);
            });
            assertEquals(FORBIDDEN, exception6.getStatus());
            assertTrue(exception6.getMessage()
                    .contains(String.format("Already existing an asset transfer with the provided transfer external id: %s", externalId)));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void saleExceptionHandling() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));
            Long clientId = createClient();
            Long loanId = createLoanForClient(clientId);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createSaleTransfer(loanId, null));
            assertEquals(BAD_REQUEST, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `settlementDate` is mandatory."));

            CallFailedRuntimeException exception2 = assertThrows(CallFailedRuntimeException.class, () -> createSaleTransfer(loanId,
                    "2020-03-02", UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, "1.0"));
            assertEquals(BAD_REQUEST, exception2.getStatus());
            assertTrue(exception2.getMessage().contains("The parameter `ownerExternalId` is mandatory."));

            CallFailedRuntimeException exception3 = assertThrows(CallFailedRuntimeException.class,
                    () -> createSaleTransfer(loanId, "2020-03-02", null, UUID.randomUUID().toString(), UUID.randomUUID().toString(), null));
            assertEquals(BAD_REQUEST, exception3.getStatus());
            assertTrue(exception3.getMessage().contains("The parameter `purchasePriceRatio` is mandatory."));

            CallFailedRuntimeException exception4 = assertThrows(CallFailedRuntimeException.class,
                    () -> createSaleTransfer(loanId, "1970-01-01"));
            assertEquals(FORBIDDEN, exception4.getStatus());
            assertTrue(exception4.getMessage().contains("Settlement date cannot be in the past"));

            CallFailedRuntimeException exception5 = assertThrows(CallFailedRuntimeException.class, () -> {
                createSaleTransfer(loanId, "2020-03-03");
                createBuybackTransfer(loanId, "2020-03-04");
                createSaleTransfer(loanId, "2020-03-05");
            });
            assertEquals(FORBIDDEN, exception5.getStatus());
            assertTrue(exception5.getMessage().contains("This loan cannot be sold, there is already an in progress transfer"));

            // Owner-to-owner transfer: selling a loan that is already owned by an external asset owner should
            // succeed at API time (a new PENDING is created; the actual ownership switch happens in COB)
            Long loanIdForOwnerTransfer = createLoanForClient(clientId);
            createSaleTransfer(loanIdForOwnerTransfer, "2020-03-03");
            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            PostInitiateTransferResponse ownerToOwnerSaleResponse = createSaleTransfer(loanIdForOwnerTransfer, "2020-03-05");
            assertNotNull(ownerToOwnerSaleResponse.getResourceId());

            String externalId = UUID.randomUUID().toString();
            String transferExternalGroupId = UUID.randomUUID().toString();
            CallFailedRuntimeException exception7 = assertThrows(CallFailedRuntimeException.class, () -> {
                Long anotherLoanId = createLoanForClient(clientId);
                createSaleTransfer(anotherLoanId, "2020-03-05", externalId, transferExternalGroupId, "1", "1.0");
                createSaleTransfer(anotherLoanId, "2020-03-05", externalId, transferExternalGroupId, "1", "1.0");
            });
            assertEquals(FORBIDDEN, exception7.getStatus());
            assertTrue(exception7.getMessage()
                    .contains(String.format("Already existing an asset transfer with the provided transfer external id: %s", externalId)));
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void transactionSummaryReportWithAssetOwner() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));

            externalEventHelper.deleteAllExternalEvents();
            externalEventHelper.enableBusinessEvent(LOAN_OWNERSHIP_TRANSFER_EVENT);

            Long officeId = officeHelper.createOffice(LocalDate.of(2020, 1, 1)).getResourceId();
            Long clientId = createClientInOffice(officeId);
            Long loanId = createLoanForClient(clientId);
            addPenaltyForLoan(loanId, PENALTY_AMOUNT);

            PostInitiateTransferResponse saleTransferResponse = createSaleTransfer(loanId, "2020-03-02");
            validateResponse(saleTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            retrieveResponse.getContent().forEach(transfer -> getAndValidateThereIsNoJournalEntriesForTransfer(transfer.getTransferId()));

            updateBusinessDateAndExecuteCOBJob("2020-03-03");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            List<ExternalEventResponse> loanOwnershipTransferBusinessEvents = externalEventHelper.getAllExternalEvents().stream()
                    .filter(event -> LOAN_OWNERSHIP_TRANSFER_EVENT.equals(event.getType())).toList();
            assertEquals(1, loanOwnershipTransferBusinessEvents.size());
            assertEquals(loanId, loanOwnershipTransferBusinessEvents.get(0).getAggregateRootId());

            getAndValidateThereIsActiveMapping(loanId);

            LocalDate repaymentDate = LocalDate.of(2020, 3, 2);

            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2020-03-03");
            validateResponse(buybackTransferResponse, loanId);
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "9999-12-31", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "9999-12-31", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsActiveMapping(loanId);
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            getAndValidateThereIsNoJournalEntriesForTransfer(retrieveResponse.getContent().get(2).getTransferId());

            makePartialRepayment(loanId, repaymentDate);

            updateBusinessDateAndExecuteCOBJob("2020-03-04");
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-02",
                            "2020-03-02", false, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(ACTIVE, saleTransferResponse.getResourceExternalId(), "2020-03-02", "2020-03-03",
                            "2020-03-03", true, new BigDecimal("15767.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("10.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")),
                    ExpectedExternalTransferData.expected(BUYBACK, buybackTransferResponse.getResourceExternalId(), "2020-03-03",
                            "2020-03-03", "2020-03-03", true, new BigDecimal("15762.420000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("5.000000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));
            getAndValidateThereIsNoActiveMapping(saleTransferResponse.getResourceExternalId());
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);

            RunReportsResponse report = runTransactionSummaryReport("2020-03-03", officeId);

            assertEquals(REPORT_COLUMN_COUNT, report.getColumnHeaders().size());
            for (ResultsetColumnHeaderData columnHeader : report.getColumnHeaders()) {
                assertNotNull(columnHeader.getColumnType());
                assertNotNull(columnHeader.getColumnDisplayType());
                assertFalse(columnHeader.getIsColumnNullable());
            }

            assertNotNull(retrieveResponse.getContent().get(0).getOwner());
            String ownerId = retrieveResponse.getContent().get(0).getOwner().getExternalId();
            assertNotNull(retrieveResponse.getContent().get(2).getPreviousOwner());
            String previousOwnerId = retrieveResponse.getContent().get(2).getPreviousOwner().getExternalId();

            List<List<Object>> rows = report.getData().stream().map(row -> row.getRow()).toList();
            assertEquals(9, rows.size());
            assertReportRow(rows.get(0), "2020-03-03", APPLY_CHARGES_TRANSACTION, INTEREST_ALLOCATION, 9.68, ownerId, null);
            assertReportRow(rows.get(1), "2020-03-03", ASSET_BUYBACK_TRANSACTION, INTEREST_ALLOCATION, -757.42, null, previousOwnerId);
            assertReportRow(rows.get(2), "2020-03-03", ASSET_BUYBACK_TRANSACTION, PENALTY_ALLOCATION, -5.00, null, previousOwnerId);
            assertReportRow(rows.get(3), "2020-03-03", ASSET_BUYBACK_TRANSACTION, PRINCIPAL_ALLOCATION, -15000.00, null, previousOwnerId);
            assertReportRow(rows.get(4), "2020-03-03", REPAYMENT_TRANSACTION, FEES_ALLOCATION, 0.00, ownerId, null);
            assertReportRow(rows.get(5), "2020-03-03", REPAYMENT_TRANSACTION, INTEREST_ALLOCATION, 0.00, ownerId, null);
            assertReportRow(rows.get(6), "2020-03-03", REPAYMENT_TRANSACTION, PENALTY_ALLOCATION, -5.00, ownerId, null);
            assertReportRow(rows.get(7), "2020-03-03", REPAYMENT_TRANSACTION, PRINCIPAL_ALLOCATION, 0.00, ownerId, null);
            assertReportRow(rows.get(8), "2020-03-03", REPAYMENT_TRANSACTION, UNALLOCATED_CREDIT_ALLOCATION, 0.00, ownerId, null);
        } finally {
            externalEventHelper.deleteAllExternalEvents();
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void transactionSummaryReportWithAssetOwnerCheckFromAssetOwnerIdForBuyback() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2023-08-16"));

            externalEventHelper.deleteAllExternalEvents();
            externalEventHelper.enableBusinessEvent(LOAN_OWNERSHIP_TRANSFER_EVENT);

            Long officeId = officeHelper.createOffice(LocalDate.of(2020, 1, 1)).getResourceId();
            Long clientId = createClientInOffice(officeId);
            Long loanId = createLoanForClient(clientId);

            // Create first sale transfer
            PostInitiateTransferResponse firstSaleTransferResponse = createSaleTransfer(loanId, "2023-08-16");
            validateResponse(firstSaleTransferResponse, loanId);

            // Verify the transfer is PENDING initially
            getAndValidateExternalAssetOwnerTransferByLoan(loanId, ExpectedExternalTransferData.expected(PENDING,
                    firstSaleTransferResponse.getResourceExternalId(), "2023-08-16", "2023-08-16", "9999-12-31"));

            // Execute COB job on the next day to activate the transfer
            updateBusinessDateAndExecuteCOBJob("2023-08-17");

            // Verify the transfer is ACTIVE after COB job
            getAndValidateExternalAssetOwnerTransferByLoan(loanId,
                    ExpectedExternalTransferData.expected(PENDING, firstSaleTransferResponse.getResourceExternalId(), "2023-08-16",
                            "2023-08-16", "2023-08-16"),
                    ExpectedExternalTransferData.expected(ACTIVE, firstSaleTransferResponse.getResourceExternalId(), "2023-08-16",
                            "2023-08-17", "9999-12-31", true, new BigDecimal("15914.980000"), new BigDecimal("15000.000000"),
                            new BigDecimal("757.420000"), new BigDecimal("157.560000"), new BigDecimal("0.000000"),
                            new BigDecimal("0.000000")));

            // Get the owner ID of the first transfer for later verification
            PageExternalTransferData retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            assertNotNull(retrieveResponse.getContent().get(1).getOwner());
            String firstOwnerId = retrieveResponse.getContent().get(1).getOwner().getExternalId();
            assertNull(retrieveResponse.getContent().get(1).getPreviousOwner(), "First sale transfer should not have previous_owner_id");

            // Create buyback transfer
            updateBusinessDateAndExecuteCOBJob("2023-08-18");
            PostInitiateTransferResponse buybackTransferResponse = createBuybackTransfer(loanId, "2023-08-18");
            validateResponse(buybackTransferResponse, loanId);

            // Execute COB job to process buyback
            updateBusinessDateAndExecuteCOBJob("2023-08-19");

            // Verify buyback has previous_owner_id set
            retrieveResponse = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
            assertNotNull(retrieveResponse.getContent().get(2).getPreviousOwner());
            assertEquals(firstOwnerId, retrieveResponse.getContent().get(2).getPreviousOwner().getExternalId(),
                    "Buyback transfer should have previous_owner_id set to first owner");

            // Run report on settlement date of buyback to check that from_asset_owner_id is populated
            RunReportsResponse report = runTransactionSummaryReport("2023-08-18", officeId);

            List<List<Object>> buybackRows = report.getData().stream().map(row -> row.getRow())
                    .filter(row -> ASSET_BUYBACK_TRANSACTION.equals(row.get(TRANSACTION_TYPE_COLUMN))
                            && "2023-08-18".equals(row.get(TRANSACTION_DATE_COLUMN)))
                    .toList();

            // Verify that Asset Buyback entries exist
            assertFalse(buybackRows.isEmpty(), "Asset Buyback entries should exist in the report");
            for (List<Object> row : buybackRows) {
                // Verify that from_asset_owner_id is populated with previous_owner_id for buyback
                assertEquals(firstOwnerId, row.get(FROM_ASSET_OWNER_ID_COLUMN),
                        "from_asset_owner_id should equal the first owner's external ID for buyback transfer");
            }
        } finally {
            externalEventHelper.deleteAllExternalEvents();
            cleanUpAndRestoreBusinessDate();
        }
    }

    @Test
    public void addManualJournalEntriesWithAssetExternalization() {
        runAt("10 April 2025", () -> {
            Account glAccountDebit = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("MANUAL_DEBIT_", 5));
            Account glAccountCredit = accountHelper.createLiabilityAccount(Utils.uniqueRandomStringGenerator("MANUAL_CREDIT_", 5));
            String externalAssetOwner = Utils.uniqueRandomStringGenerator("ASSET_EXTERNAL_", 5);

            CallFailedRuntimeException unknownOwnerException = assertThrows(CallFailedRuntimeException.class,
                    () -> journalHelper.createJournalEntry(manualJournalEntry(glAccountDebit, glAccountCredit, externalAssetOwner)));
            assertEquals(NOT_FOUND, unknownOwnerException.getStatus());
            assertTrue(unknownOwnerException.getMessage().contains("External asset owner with external id:"));

            Long clientId = createClient();
            String operationDate = "10 April 2025";

            Long loanProductId = createLoanProduct(manualEntryLoanProduct());
            Long loanId = applyForLoan(
                    applyLoanRequest(clientId, loanProductId, operationDate, MANUAL_ENTRY_LOAN_PRINCIPAL, MANUAL_ENTRY_LOAN_REPAYMENTS,
                            request -> request
                                    .transactionProcessingStrategyCode(
                                            LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                    .interestRatePerPeriod(BigDecimal.valueOf(MANUAL_ENTRY_LOAN_INTEREST_RATE))));
            approveLoan(loanId, approveLoanRequest(MANUAL_ENTRY_LOAN_PRINCIPAL, operationDate));
            disburseLoan(loanId, operationDate, MANUAL_ENTRY_LOAN_PRINCIPAL);

            PostInitiateTransferResponse transferResponse = EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId, SALE_COMMAND,
                    new ExternalAssetOwnerRequest().settlementDate("2025-04-20").dateFormat(DATE_FORMAT_ISO).locale(LoanTestData.LOCALE)
                            .transferExternalId(externalAssetOwner).ownerExternalId(externalAssetOwner).purchasePriceRatio("0.90"));
            assertEquals(externalAssetOwner, transferResponse.getResourceExternalId());

            PostJournalEntriesResponse journalEntriesResponse = journalHelper
                    .createJournalEntry(manualJournalEntry(glAccountDebit, glAccountCredit, externalAssetOwner));

            GetJournalEntriesTransactionIdResponse journalEntries = journalHelper
                    .getJournalEntriesByTransactionId(journalEntriesResponse.getTransactionId());
            assertNotNull(journalEntries);
            assertEquals(2, journalEntries.getPageItems().size());
            for (JournalEntryTransactionItem journalEntryItem : journalEntries.getPageItems()) {
                assertEquals(externalAssetOwner, journalEntryItem.getExternalAssetOwner());
            }
        });
    }

    @Test
    public void saleTransferWithSameOwnerExternalIdInParallelShouldNotFail() {
        try {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);
            setInitialBusinessDate(LocalDate.parse("2020-03-02"));

            String sharedOwnerExternalId = UUID.randomUUID().toString();

            List<Long> loanIds = new ArrayList<>();
            for (int i = 0; i < PARALLEL_SALE_THREAD_COUNT; i++) {
                loanIds.add(createLoanForClient(createClient()));
            }

            ExecutorService executorService = Executors.newFixedThreadPool(PARALLEL_SALE_THREAD_COUNT);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(PARALLEL_SALE_THREAD_COUNT);
            List<PostInitiateTransferResponse> results = Collections.synchronizedList(new ArrayList<>());
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            for (Long loanId : loanIds) {
                executorService.execute(() -> {
                    try {
                        startLatch.await();
                        results.add(EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId, SALE_COMMAND,
                                new ExternalAssetOwnerRequest().settlementDate("2020-03-02").dateFormat(DATE_FORMAT_ISO)
                                        .locale(LoanTestData.LOCALE).transferExternalId(UUID.randomUUID().toString())
                                        .transferExternalGroupId(UUID.randomUUID().toString()).ownerExternalId(sharedOwnerExternalId)
                                        .purchasePriceRatio("1.0")));
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(doneLatch.await(PARALLEL_SALE_TIMEOUT_SECONDS, TimeUnit.SECONDS), "All threads should complete within timeout");
            executorService.shutdown();
            assertTrue(executorService.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "ExecutorService should terminate");

            assertTrue(exceptions.isEmpty(),
                    "Expected no exceptions but got " + exceptions.size() + ": " + exceptions.stream().map(Exception::getMessage).toList());
            assertEquals(PARALLEL_SALE_THREAD_COUNT, results.size(), "All transfers should succeed");
            results.forEach(response -> {
                assertNotNull(response.getResourceId());
                assertNotNull(response.getResourceExternalId());
            });

            for (Long loanId : loanIds) {
                // Verify all transfers reference the same owner
                PageExternalTransferData transfers = EXTERNAL_ASSET_OWNER_HELPER.retrieveTransfersByLoanId(loanId);
                assertEquals(1, transfers.getTotalElements());
                assertNotNull(transfers.getContent());
                assertNotNull(transfers.getContent().getFirst().getOwner());
                assertEquals(sharedOwnerExternalId, transfers.getContent().getFirst().getOwner().getExternalId(),
                        "All transfers should reference the same owner");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            cleanUpAndRestoreBusinessDate();
        }
    }

    private Long createLoanForClient(Long clientId) {
        Long overdueFeeChargeId = chargesHelper.createLoanOverdueFeePercentageOfAmountAndInterest(OVERDUE_FEE_PERCENTAGE).getResourceId();
        return createLoanForClient(clientId, LOAN_SUBMITTED_ON_DATE, assetOwnerLoanProduct(overdueFeeChargeId));
    }

    /**
     * The transferrable loan product, booked against a single asset account plus a dedicated fee/penalty asset account,
     * so every sale, buyback and repayment journal entry can be attributed to exactly one of them.
     */
    private PostLoanProductsRequest assetOwnerLoanProduct(Long overdueFeeChargeId) {
        return withPeriodicAccrualAccounting(transferrableLoanProduct(), assetAccount, expenseAccount, incomeAccount, overpaymentAccount)//
                .receivableFeeAccountId(feePenaltyAccountId())//
                .receivablePenaltyAccountId(feePenaltyAccountId())//
                .charges(List.of(new LoanProductChargeData().id(overdueFeeChargeId)));
    }

    private PostLoanProductsRequest manualEntryLoanProduct() {
        return onePeriod30DaysPeriodicAccrualWithAdvancedAllocation()//
                .principal(MANUAL_ENTRY_LOAN_PRINCIPAL)//
                .numberOfRepayments(MANUAL_ENTRY_LOAN_REPAYMENTS)//
                .interestRatePerPeriod(MANUAL_ENTRY_LOAN_INTEREST_RATE)//
                .enableDownPayment(false)//
                .isInterestRecalculationEnabled(true)//
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE)//
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD)//
                .preClosureInterestCalculationStrategy(PRE_CLOSURE_INTEREST_CALCULATION_TILL_PRE_CLOSE_DATE)//
                .allowPartialPeriodInterestCalculation(true);
    }

    private JournalEntryCommand manualJournalEntry(Account debitAccount, Account creditAccount, String externalAssetOwner) {
        return new JournalEntryCommand().amount(BigDecimal.TEN).officeId(FeignOfficeHelper.HEAD_OFFICE_ID).currencyCode("USD")
                .locale(LoanTestData.LOCALE).dateFormat(MANUAL_JOURNAL_ENTRY_DATE_FORMAT).transactionDate(LocalDate.of(2024, 1, 1))
                .addCreditsItem(
                        new SingleDebitOrCreditEntryCommand().glAccountId(debitAccount.getAccountID().longValue()).amount(BigDecimal.TEN))
                .addDebitsItem(
                        new SingleDebitOrCreditEntryCommand().glAccountId(creditAccount.getAccountID().longValue()).amount(BigDecimal.TEN))
                .externalAssetOwner(externalAssetOwner);
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(ClientHelper.defaultClientCreationRequest().officeId(officeId).activationDate("01 January 2020"))
                .getClientId();
    }

    private void makePartialRepayment(Long loanId, LocalDate transactionDate) {
        transactionHelper.makeLoanRepayment(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate(dateTimeFormatter.format(transactionDate)).locale(LoanTestData.LOCALE)
                        .transactionAmount(PARTIAL_REPAYMENT_AMOUNT));
    }

    private RunReportsResponse runTransactionSummaryReport(String endDate, Long officeId) {
        return reportHelper.runReport(TRANSACTION_SUMMARY_REPORT,
                Map.of("R_endDate", endDate, "R_officeId", officeId.toString(), "output-type", "CSV"));
    }

    private void assertReportRow(List<Object> row, String transactionDate, String transactionType, String allocationType, double amount,
            String assetOwnerId, String fromAssetOwnerId) {
        assertEquals(transactionDate, row.get(TRANSACTION_DATE_COLUMN));
        assertTrue(((String) row.get(PRODUCT_NAME_COLUMN)).matches(LOAN_PRODUCT_NAME_PATTERN));
        assertEquals(transactionType, row.get(TRANSACTION_TYPE_COLUMN));
        assertNull(row.get(PAYMENT_TYPE_COLUMN));
        assertEquals("", row.get(CHARGE_TYPE_COLUMN));
        assertNotReversed(row.get(REVERSED_COLUMN));
        assertEquals(allocationType, row.get(ALLOCATION_TYPE_COLUMN));
        assertNull(row.get(CHARGE_OFF_REASON_COLUMN));
        assertEquals(amount, ((Number) row.get(AMOUNT_COLUMN)).doubleValue(), AMOUNT_TOLERANCE);
        assertEquals(assetOwnerId, row.get(ASSET_OWNER_ID_COLUMN));
        assertEquals(fromAssetOwnerId, row.get(FROM_ASSET_OWNER_ID_COLUMN));
        assertNull(row.get(ORIGINATOR_EXTERNAL_IDS_COLUMN));
    }

    /**
     * The report's "reversed" column is a database boolean, which the databases we test against do not agree on:
     * PostgreSQL reports it as {@code false} while MySQL and MariaDB report a {@code tinyint} {@code 0}. Accept either
     * rather than pinning the assertion to one database.
     */
    private void assertNotReversed(Object reversed) {
        boolean notReversed = reversed instanceof Number number ? number.intValue() == 0 : Boolean.FALSE.equals(reversed);
        assertTrue(notReversed, "Expected a non-reversed transaction, but the report reported reversed=" + reversed);
    }

    private PostInitiateTransferResponse createBuybackTransfer(Long loanId, String settlementDate) {
        return createBuybackTransfer(loanId, settlementDate, UUID.randomUUID().toString());
    }

    private PostInitiateTransferResponse createBuybackTransfer(Long loanId, String settlementDate, String transferExternalId) {
        PostInitiateTransferResponse buybackResponse = EXTERNAL_ASSET_OWNER_HELPER.initiateTransferByLoanId(loanId, BUYBACK_COMMAND,
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat(DATE_FORMAT_ISO).locale(LoanTestData.LOCALE)
                        .transferExternalId(transferExternalId));
        assertEquals(transferExternalId, buybackResponse.getResourceExternalId());
        return buybackResponse;
    }

    private void getAndValidateThereIsNoActiveMapping(Long loanId) {
        assertNull(EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByLoanId(loanId));
    }

    private void getAndValidateThereIsNoActiveMapping(String transferExternalId) {
        assertNull(EXTERNAL_ASSET_OWNER_HELPER.retrieveActiveTransferByTransferExternalId(transferExternalId));
    }

    private void getAndValidateOwnerJournalEntries(String ownerExternalId, ExpectedJournalEntryData... expectedItems) {
        ExternalOwnerJournalEntryData result = EXTERNAL_ASSET_OWNER_HELPER.retrieveJournalEntriesOfOwner(ownerExternalId);
        assertNotNull(result);
        assertEquals(expectedItems.length, result.getJournalEntryData().getTotalElements());
        assertEquals(ownerExternalId, result.getOwnerData().getExternalId());
        assertJournalEntriesMatch(result.getJournalEntryData().getContent(), expectedItems);
    }

    private void getAndValidateThereIsJournalEntriesForTransfer(Long transferId, ExpectedJournalEntryData... expectedItems) {
        ExternalOwnerTransferJournalEntryData result = EXTERNAL_ASSET_OWNER_HELPER.retrieveJournalEntriesOfTransfer(transferId);
        assertNotNull(result);
        assertEquals(expectedItems.length, result.getJournalEntryData().getTotalElements());
        assertEquals(transferId, result.getTransferData().getTransferId());
        assertJournalEntriesMatch(result.getJournalEntryData().getContent(), expectedItems);
    }

    private void assertJournalEntriesMatch(List<JournalEntryData> actualItems, ExpectedJournalEntryData... expectedItems) {
        for (int i = 0; i < expectedItems.length; i++) {
            ExpectedJournalEntryData expected = expectedItems[i];
            JournalEntryData actual = actualItems.get(i);
            assertEquals(0, expected.amount.compareTo(actual.getAmount()));
            assertEquals(expected.entryTypeId, actual.getEntryType().getId());
            assertEquals(expected.glAccountId, actual.getGlAccountId());
            assertEquals(expected.transactionDate, actual.getTransactionDate());
            assertEquals(expected.submittedOnDate, actual.getSubmittedOnDate());
        }
    }

    private void getAndValidateThereIsNoJournalEntriesForTransfer(Long transferId) {
        assertNull(EXTERNAL_ASSET_OWNER_HELPER.retrieveJournalEntriesOfTransfer(transferId).getJournalEntryData());
    }

    private static Long assetAccountId() {
        return assetAccount.getAccountID().longValue();
    }

    private static Long feePenaltyAccountId() {
        return feePenaltyAccount.getAccountID().longValue();
    }

    private static Long incomeAccountId() {
        return incomeAccount.getAccountID().longValue();
    }

    private static Long overpaymentAccountId() {
        return overpaymentAccount.getAccountID().longValue();
    }

    private static Long transferAccountId() {
        return transferAccount.getAccountID().longValue();
    }

    private static Long debitEntryTypeId() {
        return (long) JournalEntryType.DEBIT.getValue();
    }

    private static Long creditEntryTypeId() {
        return (long) JournalEntryType.CREDIT.getValue();
    }

    @RequiredArgsConstructor
    private static final class ExpectedJournalEntryData {

        private final Long glAccountId;
        private final Long entryTypeId;
        private final BigDecimal amount;
        private final LocalDate transactionDate;
        private final LocalDate submittedOnDate;

        static ExpectedJournalEntryData expected(Long glAccountId, Long entryTypeId, BigDecimal amount, LocalDate transactionDate,
                LocalDate submittedOnDate) {
            return new ExpectedJournalEntryData(glAccountId, entryTypeId, amount, transactionDate, submittedOnDate);
        }
    }
}
