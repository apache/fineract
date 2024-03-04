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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionReverseReplayTest {

    private static final String DATE_PATTERN = "dd MMMM yyyy";
    private final BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_PATTERN).toFormatter();
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private InlineLoanCOBHelper inlineLoanCOBHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        clientHelper = new ClientHelper(requestSpec, responseSpec);
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
        journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
    }

    /**
     * 1. Loan created and disbursed. // 2. Loan repayment on expected maturity date. // 3. Merchant issues refund
     * post-maturity. // 4. CBR the next morning (after COB) // 5. Loan repayment reverses triggering reverse-replay of
     * MIR and CBR. This also creates the additional repayment schedule. // 6. Charge added AFTER (NOT on the same day
     * of) CBR. // 7. When the COB runs on the charge date, accruals are created. //
     */
    @Test
    public void loanTransactionReverseReplayWithAdditionalInstallmentAndChargesTest() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("04 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse loanProductsProductResponse = createLoanProduct(loanTransactionHelper);

            final Integer loanId = createLoanAccount(clientId, loanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_PATTERN).transactionDate("03 October 2022").locale("en")
                            .transactionAmount(1000.0));

            loanTransactionHelper.makeMerchantIssuedRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATE_PATTERN).transactionDate("04 October 2022").locale("en").transactionAmount(500.0));

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("05 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            loanTransactionHelper.makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATE_PATTERN).transactionDate("05 October 2022").locale("en").transactionAmount(500.0));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("06 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale("en")
                            .dateFormat(DATE_PATTERN).transactionAmount(0.0));

            LocalDate targetDate = LocalDate.of(2022, 10, 6);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);
            loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            GetLoansLoanIdResponse loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            int lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertTrue(loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getType().getAccrual());
            assertEquals(10.0, loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    /**
     * 1 create a loan account - approve and disburse // 2. make repayment (fully paid) // 3. add the charge greater
     * than the maturity date // 4. make 2nd payment with excess amount - the loan will move to overpaid state. // 5. Do
     * a CBR transaction // 6. reverse the 2nd payment // 7. check the repayment schedule due date. 8. add chargeback
     * for 1st repayment 9. check the repayment schedule due date //
     */
    @Test
    public void loanTransactionReverseReplayWithAdditionalInstallmentAndChargesScheduleDueDateTest() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("04 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse loanProductsProductResponse = createLoanProduct(loanTransactionHelper);

            final Integer loanId = createLoanAccount(clientId, loanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            // make repayment
            String loanTransactionExternalIdStr = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_PATTERN)
                    .transactionDate("03 October 2022").locale("en").transactionAmount(1000.0).externalId(loanTransactionExternalIdStr));

            LocalDate targetDate = LocalDate.of(2022, 10, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);
            loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            GetLoansLoanIdResponse loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            int lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertTrue(loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getType().getAccrual());
            assertEquals(10.0, loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount());
            int lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 10),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("06 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_PATTERN).transactionDate("06 October 2022").locale("en")
                            .transactionAmount(500.0));

            loanTransactionHelper.makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATE_PATTERN).transactionDate("06 October 2022").locale("en").transactionAmount(490.0));

            loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale("en")
                            .dateFormat(DATE_PATTERN).transactionAmount(0.0));

            loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 10),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("11 October 2022").dateFormat(DATE_PATTERN).locale("en"));
            loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr, loanTransactionExternalIdStr,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(100.0).paymentTypeId(1L));

            loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 11),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void loanTransactionReverseReplayWithChargeOffAndCBR() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("04 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                    .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                    .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                    .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                    .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                    .withFeeAndPenaltyAssetAccount(assetFeeAndPenaltyAccount).build(null);
            final Integer loanProductID = loanTransactionHelper.getLoanProductId(loanProductJSON);

            final Integer loanId = createLoanAccount(clientId, loanProductID.longValue(), loanExternalIdStr);

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffResponse = loanTransactionHelper.chargeOffLoan(loanId.longValue(),
                    new PostLoansLoanIdTransactionsRequest().transactionDate("03 October 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_PATTERN).transactionDate("03 October 2022").locale("en")
                            .transactionAmount(1500.0));

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("05 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse cbrTransactionResponse = loanTransactionHelper.makeCreditBalanceRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_PATTERN).transactionDate("05 October 2022").locale("en")
                            .transactionAmount(500.0));

            GetLoansLoanIdResponse loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            int lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertEquals(500.0, loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount());

            ArrayList<HashMap> journalEntriesForCBR = journalEntryHelper
                    .getJournalEntriesByTransactionId("L" + cbrTransactionResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            List<HashMap> cbrExpenseJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> assetAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            List<HashMap> cbrAssetJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> overpaymentAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            assertEquals(1, cbrExpenseJournalEntries.size());
            assertEquals(1, cbrAssetJournalEntries.size());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("06 October 2022").dateFormat(DATE_PATTERN).locale("en"));

            loanTransactionHelper.reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale("en")
                            .dateFormat(DATE_PATTERN).transactionAmount(0.0));

            // check if the original CBR got reversed
            journalEntriesForCBR = journalEntryHelper
                    .getJournalEntriesByTransactionId("L" + cbrTransactionResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            cbrExpenseJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> assetAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            cbrAssetJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> overpaymentAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            assertEquals(2, cbrExpenseJournalEntries.size());
            assertEquals(2, cbrAssetJournalEntries.size());

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            loansLoanIdResponse = loanTransactionHelper.getLoanDetails(loanExternalIdStr);
            lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertEquals(500.0, loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount());

            // replayed CBR transaction
            GetLoansLoanIdTransactions newCBRTransaction = loansLoanIdResponse.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getCreditBalanceRefund()).findFirst().orElse(null);

            assertNotNull(newCBRTransaction);

            Long newCBRTransactionId = newCBRTransaction.getId();

            journalEntriesForCBR = journalEntryHelper.getJournalEntriesByTransactionId("L" + newCBRTransactionId);
            ArrayList<HashMap> journalEntriesForChargeOff = journalEntryHelper
                    .getJournalEntriesByTransactionId("L" + chargeOffResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            assertNotNull(journalEntriesForChargeOff);

            String expenseGlAccountCodeForChargeOff = (String) journalEntriesForChargeOff.get(0).get("glAccountCode");
            String assetGlAccountCodeForChargeOff = (String) journalEntriesForChargeOff.get(1).get("glAccountCode");

            cbrExpenseJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> expenseGlAccountCodeForChargeOff.equals(journalEntry.get("glAccountCode"))
                            && expenseAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            cbrAssetJournalEntries = journalEntriesForCBR.stream() //
                    .filter(journalEntry -> assetGlAccountCodeForChargeOff.equals(journalEntry.get("glAccountCode"))
                            && assetAccount.getAccountID().equals(journalEntry.get("glAccountId"))) //
                    .toList();

            assertEquals(1, cbrExpenseJournalEntries.size());
            assertEquals(1, cbrAssetJournalEntries.size());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

}
