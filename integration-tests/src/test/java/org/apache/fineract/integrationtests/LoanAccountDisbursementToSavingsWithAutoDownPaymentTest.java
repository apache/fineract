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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanAccountDisbursementToSavingsWithAutoDownPaymentTest extends FeignLoanTestBase {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);
    private static final String LOAN_BALANCE_CHANGED_EVENT = "LoanBalanceChangedBusinessEvent";

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupSavingsAndFinancialActivityHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        financialActivityAccountHelper = new FinancialActivityAccountHelper(null);
    }

    @Test
    public void loanDisbursementToSavingsWithAutoDownPaymentAndStandingInstructionsTest() {
        runAt("01 March 2023", () -> {
            externalEventHelper.enableBusinessEvent(LOAN_BALANCE_CHANGED_EVENT);
            deleteAllExternalEvents();

            String loanExternalIdStr = UUID.randomUUID().toString();

            Long clientId = createClient();

            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment();

            Long savingsAccountId = createApproveActivateSavingsAccountDailyPosting(clientId, "01 March 2023");

            mapLiabilityTransferFinancialActivity(loanProductId);

            Long loanId = applyForLoan(new PostLoansRequest()//
                    .clientId(clientId)//
                    .productId(loanProductId)//
                    .principal(new BigDecimal("1000"))//
                    .loanTermFrequency(45)//
                    .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                    .numberOfRepayments(3)//
                    .repaymentEvery(15)//
                    .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                    .interestRatePerPeriod(BigDecimal.ZERO)//
                    .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)//
                    .expectedDisbursementDate("01 March 2023")//
                    .submittedOnDate("01 March 2023")//
                    .loanType("individual")//
                    .externalId(loanExternalIdStr)//
                    .createStandingInstructionAtDisbursement(true)//
                    .linkAccountId(savingsAccountId)//
                    .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                    .collateral(List.of())//
                    .locale("en_GB")//
                    .dateFormat("dd MMMM yyyy"));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 March 2023"));

            PostLoansLoanIdResponse responseLoanDisburseToSavings = disburseToSavings(loanId,
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 March 2023").transactionAmount(new BigDecimal("1000"))
                            .netDisbursalAmount(new BigDecimal("1000")).note("DISBURSE NOTE").locale("en").dateFormat("dd MMMM yyyy"));

            assertEquals(loanExternalIdStr, responseLoanDisburseToSavings.getResourceExternalId());

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            verifyTransactionIsAccountTransfer(LocalDate.of(2023, 3, 1), 1000.0, loanId, "disbursement");

            verifyTransactionIsAccountTransfer(LocalDate.of(2023, 3, 1), 250.0, loanId, "downPayment");

            verifySavingsTransactions(savingsAccountId);

            verifyBusinessEvent();
            externalEventHelper.disableBusinessEvent(LOAN_BALANCE_CHANGED_EVENT);
        });
    }

    private void verifySavingsTransactions(final Long savingsId) {
        SavingsAccountData savingsAccount = savingsHelper.getSavingsDetails(savingsId);
        List<SavingsAccountTransactionData> pageItemsList = savingsAccount.getTransactions();

        Assertions.assertNotNull(pageItemsList);
        assertEquals(2, pageItemsList.size());

        SavingsAccountTransactionData withDrawalTransaction = pageItemsList.get(0);
        assertEquals("savingsAccountTransactionType.withdrawal", withDrawalTransaction.getTransactionType().getCode());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(250), withDrawalTransaction.getAmount()));
        assertEquals(SavingsAccountTransactionData.EntryTypeEnum.DEBIT, withDrawalTransaction.getEntryType());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(750), withDrawalTransaction.getRunningBalance()));

        SavingsAccountTransactionData depositTransaction = pageItemsList.get(1);
        assertEquals("savingsAccountTransactionType.deposit", depositTransaction.getTransactionType().getCode());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(1000), depositTransaction.getAmount()));
        assertEquals(SavingsAccountTransactionData.EntryTypeEnum.CREDIT, depositTransaction.getEntryType());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(1000), depositTransaction.getRunningBalance()));
    }

    private void mapLiabilityTransferFinancialActivity(Long loanProductId) {
        Long fundSourceAccountId = retrieveLoanProduct(loanProductId).getAccountingMappings().getFundSourceAccount().getId();
        PostFinancialActivityAccountsResponse response = financialActivityAccountHelper
                .createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId((long) AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue())
                        .glAccountId(fundSourceAccountId));
        assertNotNull(response.getResourceId());
    }

    private Long createApproveActivateSavingsAccountDailyPosting(final Long clientId, final String startDate) {
        final Long savingsProductId = createSavingsProductDailyPosting();
        assertNotNull(savingsProductId);
        return savingsHelper.createApproveActivateSavings(clientId, savingsProductId, startDate);
    }

    private Long createSavingsProductDailyPosting() {
        return savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct()).getResourceId();
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment() {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(true);
        product.setNumberOfRepayments(3);
        product.setRepaymentEvery(15);
        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(true);
        return createLoanProduct(product);
    }

    private void verifyTransactionIsAccountTransfer(final LocalDate transactionDate, final double transactionAmount, final Long loanId,
            final String transactionOfType) {
        // The generated Feign transaction model does not expose the "transfer" sub-object, so fetch the raw loan JSON
        // to
        // assert the disbursement/down-payment was actually posted as an account transfer to the linked savings
        // account.
        String loanJson = FeignRawHttpHelper.get("/loans/" + loanId + "?associations=transactions");
        JsonArray transactions = JsonParser.parseString(loanJson).getAsJsonObject().getAsJsonArray("transactions");
        boolean isTransactionFound = false;
        for (JsonElement element : transactions) {
            JsonObject transaction = element.getAsJsonObject();
            JsonObject type = transaction.getAsJsonObject("type");
            boolean isTransaction = type.has(transactionOfType) && type.get(transactionOfType).getAsBoolean();
            if (isTransaction && transactionDate.equals(toLocalDate(transaction.getAsJsonArray("date")))) {
                isTransactionFound = true;
                assertEquals(transactionAmount, transaction.get("amount").getAsDouble(), "Mismatch in transaction amounts");

                // verify the transfer details: this is the behaviour the test exists to prove
                assertTrue(transaction.has("transfer") && transaction.get("transfer").isJsonObject(),
                        "Transaction is not an account transfer");
                JsonObject transfer = transaction.getAsJsonObject("transfer");
                assertEquals(transactionAmount, transfer.get("transferAmount").getAsDouble(), "Mismatch in transfer amount");
                assertEquals(transactionDate, toLocalDate(transfer.getAsJsonArray("transferDate")), "Mismatch in transfer date");
                break;
            }
        }
        assertTrue(isTransactionFound, "No Transaction entries are posted");
    }

    private static LocalDate toLocalDate(final JsonArray dateArray) {
        return LocalDate.of(dateArray.get(0).getAsInt(), dateArray.get(1).getAsInt(), dateArray.get(2).getAsInt());
    }

    private void verifyBusinessEvent() {
        List<ExternalEventResponse> allExternalEvents = externalEventHelper.getAllExternalEvents();
        String type = LOAN_BALANCE_CHANGED_EVENT;

        final Optional<ExternalEventResponse> optionalExternalEventDTO = allExternalEvents.stream()
                .filter(event -> event.getType().equals(type)).findFirst();
        Assertions.assertTrue(optionalExternalEventDTO.isPresent());

        final ExternalEventResponse externalEventDTO = optionalExternalEventDTO.get();
        Assertions.assertEquals(externalEventDTO.getPayLoad().get("enableDownPayment"), Boolean.TRUE);
        Assertions.assertEquals(externalEventDTO.getPayLoad().get("enableAutoRepaymentForDownPayment"), Boolean.TRUE);
        Assertions.assertEquals(externalEventDTO.getPayLoad().get("disbursedAmountPercentageForDownPayment"),
                DOWN_PAYMENT_PERCENTAGE.doubleValue());
    }

    @AfterEach
    public void tearDown() {
        List<GetFinancialActivityAccountsResponse> financialActivities = financialActivityAccountHelper.getAllFinancialActivityAccounts();
        for (GetFinancialActivityAccountsResponse financialActivity : financialActivities) {
            DeleteFinancialActivityAccountsResponse deletedFinancialActivityAccount = financialActivityAccountHelper
                    .deleteFinancialActivityAccount(financialActivity.getId());
            Assertions.assertNotNull(deletedFinancialActivityAccount);
            Assertions.assertEquals(financialActivity.getId(), deletedFinancialActivityAccount.getResourceId());
        }
    }
}
