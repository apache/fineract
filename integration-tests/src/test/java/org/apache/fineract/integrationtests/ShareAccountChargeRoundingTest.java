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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountChargesRequest;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetAccountsCharges;
import org.apache.fineract.client.models.GetAccountsPurchasedShares;
import org.apache.fineract.client.models.GetAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ShareAccountChargeRoundingTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2026";
    private static final String LATER_DATE = "01 June 2026";

    private static FeignShareAccountHelper shareAccountHelper;

    private Long clientId;

    @BeforeAll
    public static void setupShareAccountHelper() {
        shareAccountHelper = new FeignShareAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @BeforeEach
    public void setup() {
        clientId = createClient();
    }

    /** ACTIVATION CHARGE - FLAT **/
    @Test
    public void shouldApplyRoundingRules_forFlatActivationCharge() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createActivationFeeFlatCharge(19.8);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 19.8, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("19.8"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpFlatActivationCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createActivationFeeFlatCharge(0.55);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.55, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.55"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddFlatActivationCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createActivationFeeFlatCharge(0.5);

            Long shareProductId = createShareProduct(0, 1);
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.5, DATE));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.share.charge.amount.rounded.to.zero"));
        });
    }

    @Test
    void verifyActivationChargeRoundedCorrectlyWithAccountingEnabled() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createActivationFeeFlatCharge(19.8);

            Long shareProductId = createShareProductWithAccountingRule2(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 19.8, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());
            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("19.8"), 0, 1);
            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);

            GetAccountsTypeAccountIdResponse accountData = getShareAccount(shareAccountId);
            assertShareAccountActive(accountData);

            // Find the charge payment transaction
            GetAccountsPurchasedShares chargeTransaction = getChargePaymentTransaction(accountData);

            // Verify accounting journal entries
            GetJournalEntriesTransactionIdResponse journalResponse = getJournalEntriesForShareTransaction(chargeTransaction.getId());
            assertNotNull(journalResponse);
            assertEquals(2L, journalResponse.getTotalFilteredRecords());

            List<JournalEntryTransactionItem> entries = journalResponse.getPageItems();
            assertNotNull(entries);
            assertEquals(2, entries.size());

            JournalEntryTransactionItem debit = getDebitEntry(entries);
            JournalEntryTransactionItem credit = getCreditEntry(entries);

            assertActivationChargeAccounting(debit, credit, expectedChargeAmount);
        });
    }

    /** PURCHASE CHARGE - FLAT **/
    @Test
    public void shouldApplyRoundingRules_forFlatPurchaseCharge() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 3);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeeFlatCharge(19.8);

            Long shareProductId = createShareProduct(0, 3);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 19.8, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("19.8"), 0, 3);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpFlatPurchaseCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeeFlatCharge(0.51);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.51, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.51"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddFlatPurchaseCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeeFlatCharge(0.5);

            Long shareProductId = createShareProduct(0, 1);
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.5, DATE));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.share.charge.amount.rounded.to.zero"));
        });
    }

    /** PURCHASE CHARGE - PERCENTAGE **/
    @Test
    public void shouldApplyRoundingRules_forPercentagePurchaseCharge() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeePercentCharge(2.5);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 2.5, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("100", "0.025", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpPercentagePurchaseCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeePercentCharge(0.51);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.51, DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("100", "0.0051", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddPercentagePurchaseCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createPurchaseFeePercentCharge(0.5);

            Long shareProductId = createShareProduct(0, 1);
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.5, DATE));

            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.share.charge.amount.rounded.to.zero"));
        });
    }

    /** REDEEM CHARGE - FLAT **/
    @Test
    public void shouldApplyRoundingRules_forFlatRedeemCharge() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeeFlatCharge(10.7);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 10.7, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("10.7"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpFlatRedeemCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeeFlatCharge(0.6);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.6, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.6"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldIgnoreFlatRedeemCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeeFlatCharge(0.5);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 0.5, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.5"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ZERO, expectedChargeAmount);
        });
    }

    /** REDEEM CHARGE - PERCENT **/
    @Test
    public void shouldApplyRoundingRules_forPercentageRedeemCharge() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeePercentCharge(5.5);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 5.5, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("50", "0.055", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpPercentageRedeemCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeePercentCharge(1.5);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 1.5, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("50", "0.015", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldIgnorePercentageRedeemCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long savingsProductId = createSavingsProduct(0, 1);
            Long savingsAccountId = createAndActivateSavingsAccount(savingsProductId, DATE);

            PostChargesResponse chargeResponse = createRedeemFeePercentCharge(1);

            Long shareProductId = createShareProduct(0, 1);
            Long shareAccountId = applyShareAccount(clientId, shareProductId, savingsAccountId, chargeResponse.getResourceId(), 1, DATE);
            approveShareAccount(shareAccountId);
            activateShareAccount(shareAccountId, DATE);
            redeemShares(shareAccountId, 50, LATER_DATE);

            BigDecimal actualChargeAmount = getShareChargeAmount(shareAccountId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("50", "0.01", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ZERO, expectedChargeAmount);
        });
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private Long createSavingsProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return savingsProductHelper.createSavingsProduct(baseSavingsProduct(digitsAfterDecimal, inMultiplesOf)).getResourceId();
    }

    /** The daily-interest product the RestAssured base built, in USD and with the rounding under test. */
    private PostSavingsProductsRequest baseSavingsProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return new PostSavingsProductsRequest()//
                .locale(SavingsTestData.LOCALE)//
                .name(Utils.uniqueRandomStringGenerator("DAILY_INTEREST", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Daily interest posting product")//
                .nominalAnnualInterestRate(10.0)//
                .digitsAfterDecimal(digitsAfterDecimal)//
                .inMultiplesOf(inMultiplesOf)//
                .currencyCode("USD")//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.DAILY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.AVERAGE_DAILY_BALANCE)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.DAILY)//
                .withdrawalFeeForTransfers(false)//
                .enforceMinRequiredBalance(false)//
                .allowOverdraft(false)//
                .withHoldTax(false)//
                .isDormancyTrackingActive(false);
    }

    private Long createAndActivateSavingsAccount(Long productId, String date) {
        return createApproveActivateSavings(clientId, productId, date);
    }

    private PostChargesResponse createActivationFeeFlatCharge(double amount) {
        String uniqueChargeName = "Share Account Activation Fee Flat " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(4) // SHARE
                .chargeTimeType(13) // ACTIVATION
                .chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createPurchaseFeeFlatCharge(double amount) {
        String uniqueChargeName = "Share Account Purchase Fee Flat " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(4) // SHARE
                .chargeTimeType(14) // PURCHASE
                .chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createPurchaseFeePercentCharge(double amount) {
        String uniqueChargeName = "Share Account Purchase Fee Percent " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(4) // SHARE
                .chargeTimeType(14) // PURCHASE
                .chargeCalculationType(2) // PERCENT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createRedeemFeeFlatCharge(double amount) {
        String uniqueChargeName = "Share Account Redeem Fee Flat " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(4) // SHARE
                .chargeTimeType(15) // REDEEM
                .chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private PostChargesResponse createRedeemFeePercentCharge(double amount) {
        String uniqueChargeName = "Share Account Redeem Fee Percent " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(4) // SHARE
                .chargeTimeType(15) // REDEEM
                .chargeCalculationType(2) // PERCENT
                .amount(amount).currencyCode("USD").locale("en").active(true).penalty(false));
    }

    private Long createShareProduct(int digitsAfterDecimal, int inMultiplesOf) {

        PostProductsTypeRequest request = new PostProductsTypeRequest().name("Share Product " + UUID.randomUUID()).shortName("SP")
                .description("Description").currencyCode("USD").digitsAfterDecimal(digitsAfterDecimal).inMultiplesOf(inMultiplesOf)
                .locale("en").totalShares(1000).unitPrice(1).nominalShares(20).allowDividendCalculationForInactiveClients(true)
                .accountingRule(SavingsTestData.AccountingRule.NONE);

        return shareAccountHelper.createShareProduct(request);
    }

    /** Mirrors the RestAssured {@code ShareProductHelper} defaults, which differ from the plain share product above. */
    private Long createShareProductWithAccountingRule2(int digitsAfterDecimal, int inMultiplesOf) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Account shareReference = accountHelper.createAssetAccount("Share Reference " + suffix);
        Account shareSuspense = accountHelper.createLiabilityAccount("Share Suspense " + suffix);
        Account feeIncome = accountHelper.createIncomeAccount("Share Fee Income " + suffix);
        Account shareEquity = accountHelper.createEquityAccount("Share Equity " + suffix);

        return shareAccountHelper.createShareProduct(new PostProductsTypeRequest()//
                .name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description(Utils.randomStringGenerator("", 20))//
                .currencyCode("USD")//
                .locale("en_GB")//
                .digitsAfterDecimal(digitsAfterDecimal)//
                .inMultiplesOf(inMultiplesOf)//
                .totalShares(10000)//
                .sharesIssued(10000)//
                .unitPrice(2)//
                .minimumShares(10)//
                .nominalShares(20)//
                .maximumShares(3000)//
                .allowDividendCalculationForInactiveClients(true)//
                .minimumActivePeriodForDividends(1)//
                .minimumactiveperiodFrequencyType(0)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(0)//
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .shareReferenceId(SavingsRequestBuilders.accountId(shareReference))//
                .shareSuspenseId(SavingsRequestBuilders.accountId(shareSuspense))//
                .shareEquityId(SavingsRequestBuilders.accountId(shareEquity))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(feeIncome)));
    }

    private Long applyShareAccount(Long clientId, Long productId, Long savingsAccountId, Long chargeId, double chargeAmount, String date) {
        AccountChargesRequest charge = new AccountChargesRequest().chargeId(chargeId).amount(new BigDecimal(chargeAmount));

        AccountRequest request = new AccountRequest().clientId(clientId).productId(productId).submittedDate(date).locale("en")
                .dateFormat(SavingsTestData.DATETIME_PATTERN).savingsAccountId(savingsAccountId).requestedShares(100L).applicationDate(date)
                .charges(List.of(charge));

        return shareAccountHelper.applyShareAccount(request);
    }

    private void approveShareAccount(Long shareAccountId) {
        shareAccountHelper.approve(shareAccountId);
    }

    private void activateShareAccount(Long shareAccountId, String activationDate) {
        shareAccountHelper.activate(shareAccountId, activationDate, SavingsTestData.DATETIME_PATTERN, "en");
    }

    private void redeemShares(Long shareAccountId, long shares, String requestedDate) {
        shareAccountHelper.redeemShares(shareAccountId, shares, requestedDate, SavingsTestData.DATETIME_PATTERN, "en");
    }

    private GetAccountsTypeAccountIdResponse getShareAccount(Long shareAccountId) {
        return shareAccountHelper.getShareAccount(shareAccountId);
    }

    private BigDecimal getShareChargeAmount(Long shareAccountId, Long chargeId) {
        GetAccountsTypeAccountIdResponse response = getShareAccount(shareAccountId);

        Set<GetAccountsCharges> charges = response.getCharges();
        assertNotNull(charges);

        GetAccountsCharges charge = charges.stream().filter(c -> Objects.equals(c.getChargeId(), chargeId)).findFirst()
                .orElseThrow(() -> new AssertionError("Share charge not found: " + chargeId));

        BigDecimal amount = BigDecimal.valueOf(charge.getAmount());
        assertNotNull(amount);

        return amount;
    }

    private BigDecimal applyRoundingRules(BigDecimal amount, int digitsAfterDecimal, int inMultiplesOf) {
        BigDecimal scaled;

        if (digitsAfterDecimal == 0) {
            BigDecimal fractionPart = amount.remainder(BigDecimal.ONE);

            if (fractionPart.compareTo(new BigDecimal("0.5")) <= 0) {
                scaled = amount.setScale(0, RoundingMode.DOWN);
            } else {
                scaled = amount.setScale(0, RoundingMode.UP);
            }
        } else {
            scaled = amount.setScale(digitsAfterDecimal, RoundingMode.HALF_UP);
        }

        if (digitsAfterDecimal == 0 && inMultiplesOf > 0) {
            BigDecimal divisor = new BigDecimal(inMultiplesOf);
            BigDecimal remainder = scaled.remainder(divisor);

            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                scaled = scaled.add(divisor.subtract(remainder));
            }
        }
        return scaled;
    }

    private BigDecimal calculateExpectedPercentageCharge(String baseAmount, String percentageAsDecimal, int digitsAfterDecimal,
            int inMultiplesOf) {
        BigDecimal base = new BigDecimal(baseAmount);
        BigDecimal percentage = new BigDecimal(percentageAsDecimal);
        BigDecimal rawCharge = base.multiply(percentage);
        return applyRoundingRules(rawCharge, digitsAfterDecimal, inMultiplesOf);
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    private void assertShareAccountActive(GetAccountsTypeAccountIdResponse accountData) {
        assertNotNull(accountData);
        assertNotNull(accountData.getStatus());
        assertEquals((long) ShareAccountStatusType.ACTIVE.getValue(), accountData.getStatus().getId());
        assertEquals(ShareAccountStatusType.ACTIVE.getCode(), accountData.getStatus().getCode());
        assertEquals(Boolean.TRUE, accountData.getStatus().getActive());
    }

    private GetAccountsPurchasedShares getChargePaymentTransaction(GetAccountsTypeAccountIdResponse accountData) {
        assertNotNull(accountData.getPurchasedShares());
        return accountData.getPurchasedShares().stream().filter(tx -> {
            assertNotNull(tx.getType());
            return "charge.payment".equals(tx.getType().getCode());
        }).findFirst().orElseThrow(() -> new AssertionError("Charge payment transaction not found"));
    }

    private GetJournalEntriesTransactionIdResponse getJournalEntriesForShareTransaction(Long id) {
        return journalEntryHelper.getJournalEntriesByTransactionId(AccountingProcessorHelper.SHARE_TRANSACTION_IDENTIFIER + id);
    }

    private JournalEntryTransactionItem getDebitEntry(List<JournalEntryTransactionItem> entries) {
        return entries.stream().filter(e -> {
            assertNotNull(e.getEntryType());
            return JournalEntryType.DEBIT.getCode().equals(e.getEntryType().getCode());
        }).findFirst().orElseThrow(() -> new AssertionError("Debit entry not found"));
    }

    private JournalEntryTransactionItem getCreditEntry(List<JournalEntryTransactionItem> entries) {
        return entries.stream().filter(e -> {
            assertNotNull(e.getEntryType());
            return JournalEntryType.CREDIT.getCode().equals(e.getEntryType().getCode());
        }).findFirst().orElseThrow(() -> new AssertionError("Credit entry not found"));
    }

    private void assertActivationChargeAccounting(JournalEntryTransactionItem debit, JournalEntryTransactionItem credit,
            BigDecimal expectedChargeAmount) {
        assertNotNull(debit.getAmount());
        assertBigDecimalEquals(expectedChargeAmount, BigDecimal.valueOf(debit.getAmount()));
        assertNotNull(credit.getAmount());
        assertBigDecimalEquals(expectedChargeAmount, BigDecimal.valueOf(credit.getAmount()));

        assertNotNull(debit.getGlAccountName());
        assertTrue(debit.getGlAccountName().startsWith("Share Reference"));
        assertNotNull(credit.getGlAccountName());
        assertTrue(credit.getGlAccountName().startsWith("Share Fee Income"));
    }
}
