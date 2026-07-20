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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DeleteLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.DelinquencyRangeResponse;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoansApprovalTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupCenterHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.junit.jupiter.api.Test;

public class ExternalIdSupportIntegrationTest extends FeignLoanTestBase {

    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void test() {
        runAt("07 September 2022", () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final Account assetAccount = accountHelper.createAssetAccount("extIdAsset");
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount("extIdFeePenaltyAsset");
            final Account incomeAccount = accountHelper.createIncomeAccount("extIdIncome");
            final Account expenseAccount = accountHelper.createExpenseAccount("extIdExpense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("extIdOverpayment");

            Long penalty = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(10.0).penalty(true)).getResourceId();

            Long penalty2 = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(10.0, true))
                    .getResourceId();

            final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                    .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                    .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
            final Integer loanProductID = getLoanProductId(loanProductJSON);

            final Long clientId = createClient();

            String loanExternalIdStr = UUID.randomUUID().toString();
            final PostLoansResponse loan = applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr);
            Integer loanId = loan.getResourceId().intValue();

            approveLoan("02 September 2022", loanId);
            String txnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdResponse disbursedLoanResult = disburseLoan("03 September 2022", loanId, "1000", txnExternalIdStr);

            // Check whether the provided external id was retrieved
            assertEquals(txnExternalIdStr, disbursedLoanResult.getSubResourceExternalId());

            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            String penalty1LoanChargeExternalId = UUID.randomUUID().toString();
            Integer penalty1LoanChargeId = addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), penaltyCharge1AddedDate, "10", penalty1LoanChargeExternalId)).intValue();

            // Get loan charges
            List<GetLoansLoanIdChargesChargeIdResponse> loanChargesResult = getLoanCharges((long) loanId);
            assertEquals(penalty1LoanChargeExternalId, loanChargesResult.get(0).getExternalId());
            loanChargesResult = getLoanCharges(loanExternalIdStr);
            assertEquals(penalty1LoanChargeExternalId, loanChargesResult.get(0).getExternalId());

            // Get loan charge template
            GetLoansLoanIdChargesTemplateResponse loanChargeTemplateResult = getLoanChargeTemplate((long) loanId);
            assertNotNull(loanChargeTemplateResult);
            loanChargeTemplateResult = getLoanChargeTemplate(loanExternalIdStr);
            assertNotNull(loanChargeTemplateResult);

            // Get loan charge
            GetLoansLoanIdChargesChargeIdResponse loanChargeResult = getLoanCharge((long) loanId, (long) penalty1LoanChargeId);
            assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
            loanChargeResult = getLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId);
            assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
            loanChargeResult = getLoanCharge((long) loanId, penalty1LoanChargeExternalId);
            assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());
            loanChargeResult = getLoanCharge(loanExternalIdStr, penalty1LoanChargeExternalId);
            assertEquals(penalty1LoanChargeExternalId, loanChargeResult.getExternalId());

            PostLoansLoanIdChargesResponse penalty2Result = addLoanCharge(loanExternalIdStr, new PostLoansLoanIdChargesRequest()
                    .chargeId((long) penalty).amount(10.0).dueDate(penaltyCharge1AddedDate).dateFormat("dd MMMM yyyy").locale("en"));
            assertNotNull(penalty2Result.getResourceExternalId());

            // Check whether we can fetch transaction templates with proper result http code (HTTP 200..300)
            retrieveTransactionTemplate(loanExternalIdStr, "repayment", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "payoutRefund", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "waiveinterest", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "close-rescheduled", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "disburse", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "recoverypayment", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "refundbycash", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "foreclosure", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "creditBalanceRefund", null, null, null);
            retrieveTransactionTemplate(loanExternalIdStr, "charge-off", null, null, null);

            // Check whether an external id was generated
            String waiveChargeExternalIdStr = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse waiveLoanChargeResult = waiveLoanCharge((long) loanId, (long) penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().externalId(waiveChargeExternalIdStr));
            assertEquals(waiveChargeExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
            assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

            GetLoansLoanIdTransactionsTransactionIdResponse response = getLoanTransactionDetails((long) loanId, waiveChargeExternalIdStr);
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, waiveChargeExternalIdStr);
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            PutChargeTransactionChangesResponse undoWaiveLoanChargeResult = undoWaiveLoanCharge((long) loanId, waiveChargeExternalIdStr,
                    new PutChargeTransactionChangesRequest());
            assertEquals(waiveChargeExternalIdStr, undoWaiveLoanChargeResult.getSubResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, waiveChargeExternalIdStr);
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, undoWaiveLoanChargeResult.getSubResourceId());
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, waiveChargeExternalIdStr);
            assertEquals(waiveChargeExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            waiveLoanChargeResult = waiveLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest());
            assertNotNull(waiveLoanChargeResult.getSubResourceExternalId());
            assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

            // Check whether an external id was generated
            undoWaiveLoanChargeResult = undoWaiveLoanCharge(loanExternalIdStr, waiveLoanChargeResult.getSubResourceId(),
                    new PutChargeTransactionChangesRequest());
            assertNotNull(undoWaiveLoanChargeResult.getSubResourceExternalId());

            // Check whether an external id was generated
            waiveChargeExternalIdStr = UUID.randomUUID().toString();
            waiveLoanChargeResult = waiveLoanCharge(loanExternalIdStr, penalty1LoanChargeExternalId,
                    new PostLoansLoanIdChargesChargeIdRequest().externalId(waiveChargeExternalIdStr));
            assertEquals(waiveChargeExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId());
            assertEquals(penalty1LoanChargeExternalId, waiveLoanChargeResult.getResourceExternalId());

            // Check whether an external id was generated
            undoWaiveLoanChargeResult = undoWaiveLoanCharge(loanExternalIdStr, waiveLoanChargeResult.getSubResourceExternalId(),
                    new PutChargeTransactionChangesRequest());
            assertEquals(waiveChargeExternalIdStr, undoWaiveLoanChargeResult.getSubResourceExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse repaymentResult = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(repaymentResult.getResourceExternalId());

            String repaymentExternalId = repaymentResult.getResourceExternalId();
            response = getLoanTransactionDetails((long) loanId, repaymentExternalId);
            assertEquals(repaymentExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, repaymentResult.getResourceId());
            assertEquals(repaymentExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, repaymentExternalId);
            assertEquals(repaymentExternalId, response.getExternalId());

            // Check whether the provided external id was retrieved
            String transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse repaymentResultWithExternalId = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, repaymentResultWithExternalId.getResourceExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResult = makeMerchantIssuedRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(merchantIssuedRefundResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResultWithExternalId = makeMerchantIssuedRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, merchantIssuedRefundResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, merchantIssuedRefundResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse payoutRefundResult = makePayoutRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(payoutRefundResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse payoutRefundResultWithExternalId = makePayoutRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, payoutRefundResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, payoutRefundResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse goodWillCreditResult = makeGoodwillCredit(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(goodWillCreditResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse goodWillCreditResultWithExternalId = makeGoodwillCredit(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, goodWillCreditResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, goodWillCreditResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse writeoffResult = makeWriteoff(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en"));
            assertNotNull(writeoffResult.getResourceExternalId());

            transactionExternalIdStr = writeoffResult.getResourceExternalId();
            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, writeoffResult.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse makeRecoveryPaymentResult = makeRecoveryPayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(makeRecoveryPaymentResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse makeRecoveryPaymentResultWithExternalId = makeRecoveryPayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, makeRecoveryPaymentResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, makeRecoveryPaymentResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse undoWriteoffResult = makeUndoWriteoff(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest());
            assertNotNull(undoWriteoffResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse writeoffResultWithExternalId = makeWriteoff(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, writeoffResultWithExternalId.getResourceExternalId());

            // Check whether the provided external id was retrieved
            final PostLoansLoanIdTransactionsResponse undoWriteoffResultWithExternalId = makeUndoWriteoff(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest());
            assertEquals(transactionExternalIdStr, undoWriteoffResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, undoWriteoffResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Overpay the account
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse overpaymentResultWithExternalId = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5000.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, overpaymentResultWithExternalId.getResourceExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse makeCreditBalanceRefundResult = makeCreditBalanceRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                            .transactionAmount(5.0));
            assertNotNull(makeCreditBalanceRefundResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse makeCreditBalanceRefundResultWithExternalId = makeCreditBalanceRefund(
                    loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("06 September 2022").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, makeCreditBalanceRefundResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, makeCreditBalanceRefundResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse chargeRefundResult = makeChargeRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").locale("en")
                            .loanChargeId(Long.valueOf(penalty1LoanChargeId)).transactionAmount(1.0));
            assertNotNull(chargeRefundResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse chargeRefundResultWithExternalId = makeChargeRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").locale("en")
                            .loanChargeId(Long.valueOf(penalty1LoanChargeId)).transactionAmount(1.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, chargeRefundResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, chargeRefundResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());

            // Create a loan with interest and test the rest of the transactions

            final String loanProductWithInterestJSON = new LoanProductTestBuilder().withPrincipal("10000.0").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("2").withNumberOfRepayments("5").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccounting("1", null).build(null);
            final Integer loanProductWithInterestID = getLoanProductId(loanProductWithInterestJSON);

            LocalDate aMonthBefore = LocalDate.of(2022, 8, 7);
            String formattedDate = dateFormatter.format(aMonthBefore);

            final Integer savingsId = openSavingsAccount(clientId, "10000.0", "01 August 2022");

            loanExternalIdStr = UUID.randomUUID().toString();
            final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("10000.0").withLoanTermFrequency("10")
                    .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("2")
                    .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("1").withInterestTypeAsFlatBalance()
                    .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                    .withExpectedDisbursementDate(formattedDate).withSubmittedOnDate(formattedDate).withLoanType("individual")
                    .withExternalId(loanExternalIdStr)
                    .build(clientId.toString(), loanProductWithInterestID.toString(), savingsId.toString());
            final PostLoansResponse loanWithInterest = getLoanIdFromApplication(loanApplicationJSON);
            Integer loanWithInterestId = loanWithInterest.getResourceId().intValue();

            String chargeExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdChargesResponse loanChargeForApprovedLoanResult = addLoanCharge(loanExternalIdStr,
                    new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                            .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

            PutLoansLoanIdChargesChargeIdResponse updatedLoanChargeForApprovedLoanResult = updateLoanCharge((long) loanWithInterestId,
                    loanChargeForApprovedLoanResult.getResourceId(), new PutLoansLoanIdChargesChargeIdRequest().amount(2.0));
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                    updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

            DeleteLoansLoanIdChargesChargeIdResponse deleteLoanChargeResult = deleteLoanCharge((long) loanWithInterestId,
                    loanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

            chargeExternalId = UUID.randomUUID().toString();
            loanChargeForApprovedLoanResult = addLoanCharge(loanExternalIdStr,
                    new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                            .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

            updatedLoanChargeForApprovedLoanResult = updateLoanCharge((long) loanWithInterestId,
                    loanChargeForApprovedLoanResult.getResourceExternalId(), new PutLoansLoanIdChargesChargeIdRequest().amount(1.0));
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                    updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

            deleteLoanChargeResult = deleteLoanCharge((long) loanWithInterestId, loanChargeForApprovedLoanResult.getResourceExternalId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

            chargeExternalId = UUID.randomUUID().toString();
            loanChargeForApprovedLoanResult = addLoanCharge(loanExternalIdStr,
                    new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                            .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

            updatedLoanChargeForApprovedLoanResult = updateLoanCharge(loanExternalIdStr, loanChargeForApprovedLoanResult.getResourceId(),
                    new PutLoansLoanIdChargesChargeIdRequest().amount(1.0));
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                    updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

            deleteLoanChargeResult = deleteLoanCharge(loanExternalIdStr, loanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

            chargeExternalId = UUID.randomUUID().toString();
            loanChargeForApprovedLoanResult = addLoanCharge(loanExternalIdStr,
                    new PostLoansLoanIdChargesRequest().externalId(chargeExternalId).amount(1.0).chargeId((long) penalty)
                            .dateFormat("dd MMMM yyyy").locale("en").dueDate(formattedDate));

            updatedLoanChargeForApprovedLoanResult = updateLoanCharge(loanExternalIdStr,
                    loanChargeForApprovedLoanResult.getResourceExternalId(), new PutLoansLoanIdChargesChargeIdRequest().amount(2.0));
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), updatedLoanChargeForApprovedLoanResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(),
                    updatedLoanChargeForApprovedLoanResult.getResourceExternalId());

            deleteLoanChargeResult = deleteLoanCharge(loanExternalIdStr, loanChargeForApprovedLoanResult.getResourceExternalId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceId(), deleteLoanChargeResult.getResourceId());
            assertEquals(loanChargeForApprovedLoanResult.getResourceExternalId(), deleteLoanChargeResult.getResourceExternalId());

            approveLoan(formattedDate, loanWithInterestId);

            final PostLoansLoanIdResponse disbursedLoanWithInterestResult = disburseLoan(formattedDate, loanWithInterestId, "1000");
            // Check whether an external id was generated
            assertNotNull(disbursedLoanWithInterestResult.getSubResourceExternalId());
            LocalDate aMonthBeforePlus3Days = aMonthBefore.plusDays(3);
            formattedDate = dateFormatter.format(aMonthBeforePlus3Days);

            String penalty3LoanChargeExternalId = UUID.randomUUID().toString();
            Integer penalty3LoanChargeId = addChargesForLoan(loanWithInterestId, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), formattedDate, "10", penalty3LoanChargeExternalId))
                    .intValue();

            Integer penalty4LoanChargeId = addChargesForLoan(loanWithInterestId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty2), formattedDate, "1000"))
                    .intValue();

            String penalty5LoanChargeExternalId = UUID.randomUUID().toString();
            Integer penalty5LoanChargeId = addChargesForLoan(loanWithInterestId, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty2), formattedDate, "1000", penalty5LoanChargeExternalId))
                    .intValue();

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse waiveInterestResult = makeWaiveInterest(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(5.0));
            assertNotNull(waiveInterestResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            String waiveInterestTxnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse waiveInterestResultWithExternalId = makeWaiveInterest(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(1.0).externalId(waiveInterestTxnExternalIdStr));
            assertEquals(waiveInterestTxnExternalIdStr, waiveInterestResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, waiveInterestTxnExternalIdStr);
            assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, waiveInterestResultWithExternalId.getResourceId());
            assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, waiveInterestTxnExternalIdStr);
            assertEquals(waiveInterestTxnExternalIdStr, response.getExternalId());

            String inAdvanceRepaymentTxnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse inAdvanceRepaymentResult = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(500.0).externalId(inAdvanceRepaymentTxnExternalIdStr));

            String inAdvanceRepayment2TxnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse inAdvanceRepayment2Result = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(50.0).externalId(inAdvanceRepayment2TxnExternalIdStr));

            // Check whether an external id was generated
            final PostLoansLoanIdTransactionsResponse makeRefundByCashResult = makeRefundByCash(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(1.0));
            assertNotNull(makeRefundByCashResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            String makeRefundTxnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse makeRefundByCashResultWithExternalId = makeRefundByCash(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(5.0).externalId(makeRefundTxnExternalIdStr));
            assertEquals(makeRefundTxnExternalIdStr, makeRefundByCashResultWithExternalId.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, makeRefundTxnExternalIdStr);
            assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, makeRefundByCashResultWithExternalId.getResourceId());
            assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, makeRefundTxnExternalIdStr);
            assertEquals(makeRefundTxnExternalIdStr, response.getExternalId());

            PostLoansLoanIdTransactionsResponse adjustmentResult = reverseLoanTransaction((long) loanWithInterestId,
                    inAdvanceRepayment2TxnExternalIdStr, new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .transactionDate(formattedDate).locale("en").dateFormat("dd MMMM yyyy").transactionAmount(0.0));
            assertEquals(inAdvanceRepayment2TxnExternalIdStr, adjustmentResult.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, inAdvanceRepayment2TxnExternalIdStr);
            assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, adjustmentResult.getResourceId());
            assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, inAdvanceRepayment2TxnExternalIdStr);
            assertEquals(inAdvanceRepayment2TxnExternalIdStr, response.getExternalId());

            adjustmentResult = reverseLoanTransaction(loanExternalIdStr, inAdvanceRepaymentResult.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en")
                            .dateFormat("dd MMMM yyyy").transactionAmount(0.0));
            assertEquals(inAdvanceRepaymentTxnExternalIdStr, adjustmentResult.getResourceExternalId());

            String adjustTransactionExternalId = UUID.randomUUID().toString();
            adjustmentResult = adjustLoanTransaction(loanExternalIdStr, waiveInterestTxnExternalIdStr,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en")
                            .dateFormat("dd MMMM yyyy").transactionAmount(2.0).externalId(adjustTransactionExternalId));
            assertEquals(adjustTransactionExternalId, adjustmentResult.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, adjustTransactionExternalId);
            assertEquals(adjustTransactionExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, adjustmentResult.getResourceId());
            assertEquals(adjustTransactionExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, adjustTransactionExternalId);
            assertEquals(adjustTransactionExternalId, response.getExternalId());

            adjustmentResult = adjustLoanTransaction(loanExternalIdStr, adjustmentResult.getResourceExternalId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(formattedDate).locale("en")
                            .dateFormat("dd MMMM yyyy").transactionAmount(1.0));
            assertNotNull(adjustmentResult.getResourceExternalId());

            String repaymentForChargeback = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse repaymentForChargebackResult = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(formattedDate).locale("en")
                            .transactionAmount(5.0).externalId(repaymentForChargeback));

            String chargebackTransactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargebackResult = chargebackLoanTransaction(loanExternalIdStr,
                    repaymentForChargebackResult.getResourceExternalId(), new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en")
                            .transactionAmount(2.0).externalId(chargebackTransactionExternalId).paymentTypeId(1L));
            assertEquals(chargebackTransactionExternalId, chargebackResult.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, chargebackTransactionExternalId);
            assertEquals(chargebackTransactionExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, chargebackResult.getResourceId());
            assertEquals(chargebackTransactionExternalId, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, chargebackTransactionExternalId);
            assertEquals(chargebackTransactionExternalId, response.getExternalId());

            chargebackResult = chargebackLoanTransaction(loanExternalIdStr, repaymentForChargebackResult.getResourceExternalId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(2.0).paymentTypeId(2L));
            assertNotNull(chargebackResult.getResourceExternalId());

            // Check whether the provided external id was retrieved
            String chargeAdjustmentExternalIdStr = UUID.randomUUID().toString();
            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = chargeAdjustment((long) loanWithInterestId,
                    (long) penalty3LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().externalId(chargeAdjustmentExternalIdStr).amount(1.0).locale("en"));
            assertEquals(chargeAdjustmentExternalIdStr, chargeAdjustmentResult.getSubResourceExternalId());
            assertEquals(penalty3LoanChargeExternalId, chargeAdjustmentResult.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, chargeAdjustmentExternalIdStr);
            assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, chargeAdjustmentResult.getSubResourceExternalId());
            assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, chargeAdjustmentExternalIdStr);
            assertEquals(chargeAdjustmentExternalIdStr, response.getExternalId());

            // Check whether an external id was generated
            chargeAdjustmentResult = chargeAdjustment(loanExternalIdStr, penalty3LoanChargeExternalId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(1.0).locale("en"));
            assertNotNull(chargeAdjustmentResult.getSubResourceExternalId());
            assertEquals(penalty3LoanChargeExternalId, chargeAdjustmentResult.getResourceExternalId());

            PostLoansLoanIdChargesChargeIdResponse payChargeResult = payLoanCharge(loanExternalIdStr, (long) penalty4LoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate(formattedDate));
            assertNotNull(payChargeResult.getSubResourceExternalId());
            assertNotNull(payChargeResult.getResourceExternalId());

            String payChargeExternalIdStr = UUID.randomUUID().toString();
            payChargeResult = payLoanCharge(loanExternalIdStr, penalty5LoanChargeExternalId, new PostLoansLoanIdChargesChargeIdRequest()
                    .locale("en").dateFormat("dd MMMM yyyy").transactionDate(formattedDate).externalId(payChargeExternalIdStr));
            assertEquals(payChargeExternalIdStr, payChargeResult.getSubResourceExternalId());
            assertEquals(penalty5LoanChargeExternalId, payChargeResult.getResourceExternalId());

            response = getLoanTransactionDetails((long) loanWithInterestId, payChargeExternalIdStr);
            assertEquals(payChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, payChargeResult.getSubResourceId());
            assertEquals(payChargeExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, payChargeExternalIdStr);
            assertEquals(payChargeExternalIdStr, response.getExternalId());

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
        });
    }

    @Test
    public void negativeTest() {
        runAt("07 September 2022", () -> {
            // INIT
            final Account assetAccount = accountHelper.createAssetAccount("extIdAsset");
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount("extIdFeePenaltyAsset");
            final Account incomeAccount = accountHelper.createIncomeAccount("extIdIncome");
            final Account expenseAccount = accountHelper.createExpenseAccount("extIdExpense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("extIdOverpayment");

            final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                    .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                    .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
            final Integer loanProductID = getLoanProductId(loanProductJSON);

            final Long clientId = createClient();

            String loanExternalIdStr = UUID.randomUUID().toString();
            final PostLoansResponse loan = applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr);
            Integer loanId = loan.getResourceId().intValue();

            approveLoan("02 September 2022", loanId);
            String txnExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdResponse disbursedLoanResult = disburseLoan("03 September 2022", loanId, "1000", txnExternalIdStr);

            // Check whether the provided external id was retrieved
            assertEquals(txnExternalIdStr, disbursedLoanResult.getSubResourceExternalId());

            // Second loan
            final PostLoansResponse loan2 = applyForLoanApplication(clientId.intValue(), loanProductID, null);
            Integer loan2Id = loan2.getResourceId().intValue();
            approveLoan("02 September 2022", loan2Id);
            final PostLoansLoanIdResponse disbursedLoan2Result = disburseLoan("03 September 2022", loan2Id, "1000", null);

            Long penalty = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(10.0).penalty(true)).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 7);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            String penalty1LoanChargeExternalId = UUID.randomUUID().toString();
            Integer penalty1LoanChargeId = addChargesForLoan(loan2Id, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), penaltyCharge1AddedDate, "10", penalty1LoanChargeExternalId)).intValue();

            // NEGATIVE SCENARIOS

            // GET
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> retrieveTransactionTemplate("randomNonExistingLoanExternalId", "disburse", null, null, null));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> getLoanTransactionDetails("randomNonExistingLoanExternalId", "randomNonExistingLoanTransactionExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> getLoanTransactionDetails(loanExternalIdStr, "randomNonExistingLoanTransactionExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

            // POST
            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeChargeRefund("randomNonExistingLoanExternalId", new PostLoansLoanIdTransactionsRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> adjustLoanTransaction("randomNonExistingLoanExternalId",
                    "randomNonExistingLoanTransactionExternalId", new PostLoansLoanIdTransactionsTransactionIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> adjustLoanTransaction(loanExternalIdStr,
                    "randomNonExistingLoanTransactionExternalId", new PostLoansLoanIdTransactionsTransactionIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

            // PUT
            exception = assertThrows(CallFailedRuntimeException.class, () -> undoWaiveLoanCharge("randomNonExistingLoanExternalId",
                    "randomNonExistingLoanTransactionExternalId", new PutChargeTransactionChangesRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> undoWaiveLoanCharge(loanExternalIdStr,
                    "randomNonExistingLoanTransactionExternalId", new PutChargeTransactionChangesRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.transaction.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> getLoanCharges("randomNonExistingLoanExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> getLoanChargeTemplate("randomNonExistingLoanExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> getLoanCharge("randomNonExistingLoanExternalId", "randomNonExistingLoanChargeExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> getLoanCharge(-1L, (long) penalty1LoanChargeId));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> getLoanCharge(loanExternalIdStr, (long) penalty1LoanChargeId));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.id.invalid.for.given.loan"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> getLoanCharge(loanExternalIdStr, "randomNonExistingLoanChargeExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> payLoanCharge("randomNonExistingLoanExternalId",
                    "randomNonExistingLoanChargeExternalId", new PostLoansLoanIdChargesChargeIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> payLoanCharge(loanExternalIdStr,
                    "randomNonExistingLoanChargeExternalId", new PostLoansLoanIdChargesChargeIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> updateLoanCharge("randomNonExistingLoanExternalId",
                    "randomNonExistingLoanChargeExternalId", new PutLoansLoanIdChargesChargeIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class, () -> updateLoanCharge(loanExternalIdStr,
                    "randomNonExistingLoanChargeExternalId", new PutLoansLoanIdChargesChargeIdRequest()));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> deleteLoanCharge("randomNonExistingLoanExternalId", "randomNonExistingLoanChargeExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loan.external.id.invalid"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> deleteLoanCharge(loanExternalIdStr, "randomNonExistingLoanChargeExternalId"));
            assertEquals(404, exception.getStatus());
            assertTrue(exception.getMessage().contains("error.msg.loanCharge.external.id.invalid"));
        });
    }

    @Test
    public void loan() {
        runAt("10 October 2022", () -> {

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            ArrayList<Long> rangeIds = new ArrayList<>();
            // First Range
            PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                    .minimumAgeDays(1).maximumAgeDays(3).locale("en").classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());
            DelinquencyRangeResponse range = DelinquencyRangesHelper.getRange(delinquencyRangeResponse.getResourceId());

            // Second Range
            delinquencyRangeResponse = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest().minimumAgeDays(4)
                    .maximumAgeDays(60).locale("en").classification(Utils.randomStringGenerator("DLQ_R_", 10)));
            rangeIds.add(delinquencyRangeResponse.getResourceId());

            PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper
                    .createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds));

            final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment()
                    .withInterestTypeAsDecliningBalance().withAccountingRuleAsNone()
                    .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                    .withMoratorium("0", "0").withDelinquencyBucket(delinquencyBucketResponse.getResourceId())
                    .withInArrearsTolerance("1001").withMultiDisburse().withDisallowExpectedDisbursements(true).build(null);
            final Integer loanProductID = getLoanProductId(loanProductJSON);

            final Long clientId = createClient();

            String loanExternalIdStr = UUID.randomUUID().toString();
            final PostLoansResponse loan = applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr);
            Integer loanId = loan.getResourceId().intValue();
            String resourceExternalId = (String) loan.getResourceExternalId();
            assertEquals(loanExternalIdStr, resourceExternalId);

            LocalDate actualDate = LocalDate.of(2022, 10, 10);

            GetLoansApprovalTemplateResponse loanApprovalResult = getLoanApprovalTemplate(loanExternalIdStr);
            assertEquals(actualDate, loanApprovalResult.getApprovalDate());
            assertEquals(1000.0, Utils.getDoubleValue(loanApprovalResult.getApprovalAmount()));
            assertEquals(1000.0, Utils.getDoubleValue(loanApprovalResult.getNetDisbursalAmount()));
            assertNotNull(loanApprovalResult.getCurrency());
            assertNotNull(loanApprovalResult.getCurrency().getCode());
            assertEquals("USD", loanApprovalResult.getCurrency().getCode());

            GetLoansLoanIdResponse loanDetailsResult = getLoanDetails(loanExternalIdStr);
            assertEquals(loanExternalIdStr, loanDetailsResult.getExternalId());

            approveLoan("02 September 2022", loanId);
            String txnExternalIdStr = UUID.randomUUID().toString();

            GetLoansLoanIdTransactionsTemplateResponse disburseTemplate = retrieveTransactionTemplate(loanExternalIdStr, "disburse", null,
                    null, null);
            assertEquals(1000.0, disburseTemplate.getAmount().doubleValue());
            assertNotNull(disburseTemplate.getCurrency());
            assertNotNull(disburseTemplate.getCurrency().getCode());
            assertEquals("USD", disburseTemplate.getCurrency().getCode());

            final PostLoansLoanIdResponse disbursedLoanResult = disburseLoan("03 September 2022", loanId, "1000", txnExternalIdStr);

            // Check whether the provided external id was retrieved
            assertEquals(txnExternalIdStr, disbursedLoanResult.getSubResourceExternalId());

            String txnExternalIdStr2 = UUID.randomUUID().toString();
            final PostLoansLoanIdResponse disbursedLoanResult2 = disburseLoan("04 September 2022", loanId, "1000", txnExternalIdStr2);

            // Check whether the provided external id was retrieved
            assertEquals(txnExternalIdStr2, disbursedLoanResult2.getSubResourceExternalId());

            PutLoansLoanIdResponse markLoanAsFraudResult = modifyLoanApplication(loanExternalIdStr, "markAsFraud",
                    new PutLoansLoanIdRequest().fraud(true));
            assertEquals(loanExternalIdStr, markLoanAsFraudResult.getResourceExternalId());

            List<GetDelinquencyTagHistoryResponse> delinquencyTagHistoryResponseResult = getLoanDelinquencyTags(loanExternalIdStr);
            assertEquals(1, delinquencyTagHistoryResponseResult.size());
            assertEquals((long) loanId, delinquencyTagHistoryResponseResult.get(0).getLoanId());

            String loanExternalIdStr2 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr2);

            PutLoansLoanIdResponse modifyLoanApplicationResult = modifyLoanApplication(loanExternalIdStr2, "modify",
                    new PutLoansLoanIdRequest().submittedOnDate("31 August 2022").dateFormat("dd MMMM yyyy").locale("en")
                            .loanType("individual").productId(loanProductID.longValue()).clientId(clientId).interestType(0)
                            .interestCalculationPeriodType(1).interestRatePerPeriod(BigDecimal.ZERO).isEqualAmortization(false)
                            .loanTermFrequency(30).loanTermFrequencyType(0).maxOutstandingLoanBalance(10000L).numberOfRepayments(1)
                            .principal(10000L).repaymentEvery(30).repaymentFrequencyType(0)
                            .transactionProcessingStrategyCode("mifos-standard-strategy").expectedDisbursementDate("2 September 2022")
                            .amortizationType(1));

            assertEquals(loanExternalIdStr2, modifyLoanApplicationResult.getResourceExternalId());
            DeleteLoansLoanIdResponse deleteLoanApplicationResult = deleteLoanApplication(loanExternalIdStr2);
            assertEquals(loanExternalIdStr2, deleteLoanApplicationResult.getResourceExternalId());

            String loanExternalIdStr3 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr3);
            PostLoansLoanIdResponse result = rejectLoan(loanExternalIdStr3,
                    new PostLoansLoanIdRequest().rejectedOnDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr3, result.getResourceExternalId());

            String loanExternalIdStr4 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr4);
            result = withdrawnByApplicantLoan(loanExternalIdStr4,
                    new PostLoansLoanIdRequest().withdrawnOnDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr4, result.getResourceExternalId());

            String loanExternalIdStr5 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr5);
            approveLoan(loanExternalIdStr5,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            result = disburseLoan(loanExternalIdStr5, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            // It's commented out for now, till it got fixed to return the loan externalId as well
            // assertEquals(loanExternalIdStr5, result.getResourceExternalId());

            String loanExternalIdStr6 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr6);
            approveLoan(loanExternalIdStr6,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            result = undoApprovalLoan(loanExternalIdStr6, new PostLoansLoanIdRequest());
            assertEquals(loanExternalIdStr6, result.getResourceExternalId());

            final Integer savingsId = openSavingsAccount(clientId, "10000.0", "02 September 2022");

            String loanExternalIdStr7 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr7, savingsId.toString());
            approveLoan(loanExternalIdStr7,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            result = disburseToSavingsLoan(loanExternalIdStr7, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr7, result.getResourceExternalId());

            String loanExternalIdStr8 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr8);
            approveLoan(loanExternalIdStr8,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr8, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            result = undoDisbursalLoan(loanExternalIdStr8, new PostLoansLoanIdRequest());
            assertEquals(loanExternalIdStr8, result.getResourceExternalId());

            String loanExternalIdStr9 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr9);
            approveLoan(loanExternalIdStr9,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr9, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr9, new PostLoansLoanIdRequest().actualDisbursementDate("3 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            result = undoLastDisbursalLoan(loanExternalIdStr9, new PostLoansLoanIdRequest());
            assertEquals(loanExternalIdStr9, result.getResourceExternalId());

            Integer loanOfficerId = FeignGroupCenterHelper.createStaff(1).intValue();
            String loanExternalIdStr10 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr10);
            result = assignLoanOfficerLoan(loanExternalIdStr10, new PostLoansLoanIdRequest().assignmentDate("2 September 2022").locale("en")
                    .dateFormat("dd MMMM yyyy").toLoanOfficerId(loanOfficerId.longValue()));
            assertEquals(loanExternalIdStr10, result.getResourceExternalId());
            result = unassignLoanOfficerLoan(loanExternalIdStr10,
                    new PostLoansLoanIdRequest().unassignedDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr10, result.getResourceExternalId());

            String loanExternalIdStr11 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr11);
            result = recoverGuaranteesLoan(loanExternalIdStr11, new PostLoansLoanIdRequest());
            assertEquals(loanExternalIdStr11, result.getResourceExternalId());

            String loanExternalIdStr12 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr12);
            result = assignDelinquencyLoan(loanExternalIdStr12, new PostLoansLoanIdRequest());
            assertEquals(loanExternalIdStr12, result.getResourceExternalId());

            String loanExternalIdStr13 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr13);
            result = approveLoan(loanExternalIdStr13,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr13, result.getResourceExternalId());

            PostLoansLoanIdTransactionsResponse closeRescheduleResult = closeRescheduledLoan(loanExternalIdStr13,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr13, closeRescheduleResult.getResourceExternalId());

            String loanExternalIdStr14 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr14);
            String transactionExternalId = UUID.randomUUID().toString();
            result = approveLoan(loanExternalIdStr14,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr14, result.getResourceExternalId());
            disburseLoan(loanExternalIdStr14, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            PostLoansLoanIdTransactionsResponse closeResult = closeLoan(loanExternalIdStr14, new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("3 September 2022").locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId));
            assertEquals(transactionExternalId, closeResult.getResourceExternalId());

            String loanExternalIdStr15 = UUID.randomUUID().toString();
            String transactionExternalId2 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr15);
            result = approveLoan(loanExternalIdStr15,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr15, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            assertEquals(loanExternalIdStr15, result.getResourceExternalId());
            PostLoansLoanIdTransactionsResponse forecloseResult = forecloseLoan(loanExternalIdStr15,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId2));
            assertEquals(transactionExternalId2, forecloseResult.getResourceExternalId());

            String loanExternalIdStr16 = UUID.randomUUID().toString();
            String transactionExternalId3 = UUID.randomUUID().toString();
            applyForLoanApplication(clientId.intValue(), loanProductID, loanExternalIdStr16);
            approveLoan(loanExternalIdStr16,
                    new PostLoansLoanIdRequest().approvedOnDate("2 September 2022").approvedLoanAmount(new BigDecimal("1000"))
                            .expectedDisbursementDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr16, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            disburseLoan(loanExternalIdStr16, new PostLoansLoanIdRequest().actualDisbursementDate("2 September 2022")
                    .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));
            PostLoansLoanIdTransactionsResponse chargeOffResult = chargeOffLoan(loanExternalIdStr16,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("2 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId3));
            assertEquals(transactionExternalId3, chargeOffResult.getResourceExternalId());

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(false));
        });
    }

}
