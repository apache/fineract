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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetAccountsPurchasedShares;
import org.apache.fineract.client.models.GetAccountsPurchasedSharesPaymentDetailData;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostProductsPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutAccountsTypeAccountIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.ShareAccountCommandsApi.RedeemSharesRequest;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Share applications, purchases and redemptions accept payment detail, and a share product's payment channel to fund
 * source mappings route the share reference entry to the payment type specific GL account.
 */
public class ShareAccountPaymentDetailTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2026";
    private static final String PURCHASED = "purchasedSharesType.purchased";
    private static final String REDEEMED = "purchasedSharesType.redeemed";
    private static final String APPLIED = "purchasedSharesStatusType.applied";

    private static FeignShareAccountHelper shareAccountHelper;

    private Long clientId;
    private Account shareReference;
    private Account fundSource;
    private Long mappedPaymentTypeId;
    private Long unmappedPaymentTypeId;

    @BeforeAll
    public static void setupShareAccountHelper() {
        shareAccountHelper = new FeignShareAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @BeforeEach
    public void setup() {
        clientId = createClient();
        shareReference = accountHelper.createAssetAccount("Share Reference " + Utils.uniqueRandomStringGenerator("", 8));
        fundSource = accountHelper.createAssetAccount("Bank " + Utils.uniqueRandomStringGenerator("", 8));
        mappedPaymentTypeId = createPaymentType(false);
        unmappedPaymentTypeId = createPaymentType(true);
    }

    @Test
    public void purchaseWithPaymentDetail_persistsAndReadsBackPaymentDetail() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(false));

            GetAccountsPurchasedShares purchase = purchase(shareAccountId,
                    sharesRequest(5L).paymentTypeId(mappedPaymentTypeId).accountNumber("ACC-2105").checkNumber("CHK-2105")
                            .routingCode("RT-2105").receiptNumber("RC-2105").bankNumber("BNK-2105"));

            GetAccountsPurchasedSharesPaymentDetailData paymentDetail = purchase.getPaymentDetailData();
            assertNotNull(paymentDetail);
            assertEquals(mappedPaymentTypeId, paymentDetail.getPaymentType().getId());
            assertEquals("ACC-2105", paymentDetail.getAccountNumber());
            assertEquals("CHK-2105", paymentDetail.getCheckNumber());
            assertEquals("RT-2105", paymentDetail.getRoutingCode());
            assertEquals("RC-2105", paymentDetail.getReceiptNumber());
            assertEquals("BNK-2105", paymentDetail.getBankNumber());
        });
    }

    @Test
    public void purchaseWithoutPaymentDetail_remainsCompatible() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(true));

            GetAccountsPurchasedShares purchase = purchase(shareAccountId, sharesRequest(5L));

            assertNull(purchase.getPaymentDetailData());
            assertCashSideHits(purchase.getId(), shareReference, JournalEntryType.DEBIT);
        });
    }

    @Test
    public void purchaseWithMappedPaymentType_debitsFundSourceAccount() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(true));

            GetAccountsPurchasedShares purchase = purchase(shareAccountId, sharesRequest(5L).paymentTypeId(mappedPaymentTypeId));

            assertCashSideHits(purchase.getId(), fundSource, JournalEntryType.DEBIT);
        });
    }

    @Test
    public void purchaseWithUnmappedPaymentType_debitsShareReferenceAccount() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(true));

            GetAccountsPurchasedShares purchase = purchase(shareAccountId, sharesRequest(5L).paymentTypeId(unmappedPaymentTypeId));

            assertCashSideHits(purchase.getId(), shareReference, JournalEntryType.DEBIT);
        });
    }

    @Test
    public void redeemWithMappedPaymentType_creditsFundSourceAccount() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(true));

            shareAccountHelper.redeemShares(shareAccountId,
                    sharesRequest(5L).paymentTypeId(mappedPaymentTypeId).receiptNumber("RC-REDEEM"));
            GetAccountsPurchasedShares redemption = latestTransaction(shareAccountId, REDEEMED);

            assertEquals("RC-REDEEM", redemption.getPaymentDetailData().getReceiptNumber());
            assertCashSideHits(redemption.getId(), fundSource, JournalEntryType.CREDIT);
        });
    }

    @Test
    public void paymentDetailWithoutPaymentType_isRejected() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(false));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> shareAccountHelper.applyAdditionalShares(shareAccountId, sharesRequest(5L).accountNumber("ACC-2105")));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("paymentTypeId"));
        });
    }

    @Test
    public void approveOrRejectAdditionalSharesWithPaymentDetail_isRejected() {
        runAt(DATE, () -> {
            Long shareAccountId = createActiveShareAccount(createShareProduct(false));
            shareAccountHelper.applyAdditionalShares(shareAccountId, sharesRequest(5L));
            Set<Long> applications = Set.of(latestTransaction(shareAccountId, PURCHASED).getId());
            CallFailedRuntimeException approve = assertThrows(CallFailedRuntimeException.class,
                    () -> shareAccountHelper.approveAdditionalShares(shareAccountId,
                            FeignShareAccountHelper.additionalSharesRequest(applications).paymentTypeId(mappedPaymentTypeId)));
            assertEquals(400, approve.getStatus());
            assertTrue(approve.getMessage().contains("paymentTypeId"));
            CallFailedRuntimeException reject = assertThrows(CallFailedRuntimeException.class,
                    () -> shareAccountHelper.rejectAdditionalShares(shareAccountId,
                            FeignShareAccountHelper.additionalSharesRequest(applications).paymentTypeId(mappedPaymentTypeId)));
            assertEquals(400, reject.getStatus());
            assertTrue(reject.getMessage().contains("paymentTypeId"));
        });
    }

    @Test
    public void applicationWithMappedPaymentType_debitsFundSourceAccount() {
        runAt(DATE, () -> {
            Long shareAccountId = shareAccountHelper.applyShareAccount(
                    shareAccountRequest(createShareProduct(true)).paymentTypeId(mappedPaymentTypeId).receiptNumber("RC-APPLY"));
            shareAccountHelper.approve(shareAccountId, DATE, "approved", SavingsTestData.DATETIME_PATTERN, "en");

            GetAccountsPurchasedShares application = latestTransaction(shareAccountId, PURCHASED);

            assertEquals("RC-APPLY", application.getPaymentDetailData().getReceiptNumber());
            assertCashSideHits(application.getId(), fundSource, JournalEntryType.DEBIT);
        });
    }

    @Test
    public void rejectedApplicationWithMappedPaymentType_creditsFundSourceAccount() {
        runAt(DATE, () -> {
            Long shareAccountId = shareAccountHelper
                    .applyShareAccount(shareAccountRequest(createShareProduct(true)).paymentTypeId(mappedPaymentTypeId));
            shareAccountHelper.reject(shareAccountId);
            GetAccountsPurchasedShares application = latestTransaction(shareAccountId, PURCHASED);

            assertCashSideHits(application.getId(), fundSource, JournalEntryType.CREDIT);
        });

    }

    @Test
    public void modifiedApplication_keepsPaymentDetail() {
        runAt(DATE, () -> {
            Long shareAccountId = shareAccountHelper.applyShareAccount(
                    shareAccountRequest(createShareProduct(true)).paymentTypeId(mappedPaymentTypeId).receiptNumber("RC-APPLY"));
            GetAccountsPurchasedShares application = latestTransaction(shareAccountId, PURCHASED);
            shareAccountHelper.update(shareAccountId, new PutAccountsTypeAccountIdRequest().requestedShares(25));
            GetAccountsPurchasedShares newApplication = latestTransaction(shareAccountId, PURCHASED);
            assertNotEquals(application.getId(), newApplication.getId());
            assertEquals("RC-APPLY", newApplication.getPaymentDetailData().getReceiptNumber());
            assertCashSideHits(newApplication.getId(), fundSource, JournalEntryType.DEBIT);
        });
    }

    @Test
    public void fundSourceMappingWithoutFundSourceAccount_isRejected() {
        PostProductsTypeRequest request = shareProductRequest().paymentChannelToFundSourceMappings(
                Set.of(new PostProductsPaymentChannelToFundSourceMappings().paymentTypeId(mappedPaymentTypeId)));

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> shareAccountHelper.createShareProduct(request));

        assertEquals(400, exception.getStatus());
        assertTrue(exception.getMessage().contains("fundSourceAccountId"));
    }

    private GetAccountsPurchasedShares purchase(Long shareAccountId, RedeemSharesRequest request) {
        shareAccountHelper.applyAdditionalShares(shareAccountId, request);
        GetAccountsPurchasedShares pending = latestTransaction(shareAccountId, PURCHASED);
        assertEquals(APPLIED, pending.getStatus().getCode());
        shareAccountHelper.approveAdditionalShares(shareAccountId, Set.of(pending.getId()));
        return latestTransaction(shareAccountId, PURCHASED);
    }

    private GetAccountsPurchasedShares latestTransaction(Long shareAccountId, String typeCode) {
        return shareAccountHelper.getShareAccount(shareAccountId).getPurchasedShares().stream()
                .filter(t -> typeCode.equals(t.getType().getCode())).max((a, b) -> Long.compare(a.getId(), b.getId())).orElseThrow();
    }

    /** The cash side of a share transaction posts to the share reference account or a payment type specific one. */
    private void assertCashSideHits(Long transactionId, Account expected, JournalEntryType entryType) {
        List<JournalEntryTransactionItem> entries = journalEntryHelper
                .getJournalEntriesByTransactionId(AccountingProcessorHelper.SHARE_TRANSACTION_IDENTIFIER + transactionId).getPageItems();
        Account other = expected == shareReference ? fundSource : shareReference;
        assertTrue(entries.stream().anyMatch(e -> isFor(e, expected) && entryType.getCode().equals(e.getEntryType().getCode())),
                "expected a " + entryType + " on GL account " + expected.getAccountID());
        assertTrue(entries.stream().noneMatch(e -> isFor(e, other)), "unexpected entry on GL account " + other.getAccountID());
    }

    private static boolean isFor(JournalEntryTransactionItem entry, Account account) {
        return entry.getGlAccountId().equals(SavingsRequestBuilders.accountId(account));
    }

    private RedeemSharesRequest sharesRequest(long shares) {
        return new RedeemSharesRequest().requestedDate(DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale("en")
                .requestedShares(shares);
    }

    private Long createActiveShareAccount(Long shareProductId) {
        Long shareAccountId = shareAccountHelper.applyShareAccount(shareAccountRequest(shareProductId));
        shareAccountHelper.approve(shareAccountId, DATE, "approved", SavingsTestData.DATETIME_PATTERN, "en");
        shareAccountHelper.activate(shareAccountId, DATE, SavingsTestData.DATETIME_PATTERN, "en");
        return shareAccountId;
    }

    /** A share application for 20 shares with the mandatory fields filled in; callers add what their test is about. */
    private AccountRequest shareAccountRequest(Long shareProductId) {
        Long savingsProductId = savingsProductHelper.createSavingsProduct(new PostSavingsProductsRequest()//
                .locale(SavingsTestData.LOCALE)//
                .name(Utils.uniqueRandomStringGenerator("SAVINGS_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .nominalAnnualInterestRate(0.0)//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.DAILY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.AVERAGE_DAILY_BALANCE)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.DAILY)).getResourceId();
        Long savingsAccountId = createApproveActivateSavings(clientId, savingsProductId, DATE);

        return new AccountRequest().clientId(clientId).productId(shareProductId).savingsAccountId(savingsAccountId).requestedShares(20L)
                .submittedDate(DATE).applicationDate(DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale("en");
    }

    private Long createShareProduct(boolean withFundSourceMapping) {
        PostProductsTypeRequest request = shareProductRequest();
        if (withFundSourceMapping) {
            request.paymentChannelToFundSourceMappings(Set.of(new PostProductsPaymentChannelToFundSourceMappings()
                    .paymentTypeId(mappedPaymentTypeId).fundSourceAccountId(SavingsRequestBuilders.accountId(fundSource))));
        }
        return shareAccountHelper.createShareProduct(request);
    }

    private PostProductsTypeRequest shareProductRequest() {
        String suffix = Utils.uniqueRandomStringGenerator("", 8);
        Account shareSuspense = accountHelper.createLiabilityAccount("Share Suspense " + suffix);
        Account shareEquity = accountHelper.createEquityAccount("Share Equity " + suffix);
        Account feeIncome = accountHelper.createIncomeAccount("Share Fee Income " + suffix);

        return new PostProductsTypeRequest()//
                .name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Share payment detail test")//
                .currencyCode("USD")//
                .locale("en")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .totalShares(10000)//
                .sharesIssued(10000)//
                .unitPrice(10)//
                .minimumShares(1)//
                .nominalShares(20)//
                .maximumShares(1000)//
                .allowDividendCalculationForInactiveClients(true)//
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .shareReferenceId(SavingsRequestBuilders.accountId(shareReference))//
                .shareSuspenseId(SavingsRequestBuilders.accountId(shareSuspense))//
                .shareEquityId(SavingsRequestBuilders.accountId(shareEquity))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(feeIncome));
    }

    private Long createPaymentType(boolean isCashPayment) {
        return paymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest().name(PaymentTypeHelper.randomNameGenerator("P_T", 5))
                .description("Share payment detail test").isCashPayment(isCashPayment).position(1L)).getResourceId();
    }
}
