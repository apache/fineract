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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.GetAccountTransfersPageItems;
import org.apache.fineract.client.models.GetAccountTransfersPageItemsPaymentDetailData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.Account.AccountType;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * JUnit Test Cases for Account Transfer for.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(LoanTestLifecycleExtension.class)
public class AccountTransferTest extends FeignSavingsTestBase {

    private static final Integer LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID = FinancialActivity.LIABILITY_TRANSFER.getValue();

    private static final String MINIMUM_OPENING_BALANCE = "30000.0";
    private static final String ACCOUNT_TRANSFER_AMOUNT = "15000.0";
    private static final String ACCOUNT_TRANSFER_NEGATIVE_AMOUNT = "-15000.0";
    private static final String ACCOUNT_TRANSFER_AMOUNT_ADJUST = "3000.0";
    private static final String ACCOUNT_TRANSFER_LARGE_AMOUNT = "100000.0";
    private static final String INVALID_ACCOUNT_TYPE = "999";
    private static final Long INVALID_LOAN_ID = 123123123L;

    /** The invisible default the RestAssured {@code LoanApplicationTestBuilder} put on every application. */
    private static final BigDecimal MAX_OUTSTANDING_LOAN_BALANCE = new BigDecimal("36000");

    private static final String ACCOUNT_TRANSFER_DATE = "01 March 2013";
    private static final String ACCOUNT_TRANSFER_INVALID_DATE = "01 05 2013";
    private static final String LOAN_APPROVAL_DATE = "10 January 2013";
    private static final String LOAN_DISBURSAL_DATE = "10 January 2013";
    private static final String LOAN_SUBMITTED_ON_DATE = "10 January 2013";
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2011";
    private static final String SAVINGS_SUBMITTED_ON_DATE = "08 January 2013";
    private static final String SAVINGS_APPROVED_ON_DATE = "09 January 2013";
    private static final String SAVINGS_ACTIVATED_ON_DATE = "01 March 2013";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2011, 1, 1);

    private static FeignOfficeHelper officeHelper;
    private static FeignLoanHelper loanHelper;
    private static FeignFinancialActivityAccountHelper financialActivityAccountHelper;
    private static FeignCollateralHelper collateralHelper;

    private static Long financialActivityAccountId;
    private static Account liabilityTransferAccount;

    /** Account transfers post to the liability transfer account, so the mapping has to exist before any test runs. */
    @BeforeAll
    public static void setupLiabilityTransferAccount() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        officeHelper = new FeignOfficeHelper(client);
        loanHelper = new FeignLoanHelper(client);
        financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(client);
        collateralHelper = new FeignCollateralHelper(client);

        GetFinancialActivityAccountsResponse existing = financialActivityAccountHelper.getAllMappings().stream()
                .filter(mapping -> mapping.getFinancialActivityData() != null
                        && LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID.equals(mapping.getFinancialActivityData().getId()))
                .findFirst().orElse(null);

        if (existing == null) {
            liabilityTransferAccount = accountHelper.createLiabilityAccount();
            financialActivityAccountId = financialActivityAccountHelper
                    .createMapping(LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID, liabilityTransferAccount).getResourceId();
        } else {
            liabilityTransferAccount = new Account(existing.getGlAccountData().getId().intValue(), AccountType.LIABILITY);
            financialActivityAccountId = existing.getId();
        }
        assertNotNull(financialActivityAccountId);
    }

    @AfterAll
    public static void tearDown() {
        assertEquals(financialActivityAccountId, financialActivityAccountHelper.deleteMapping(financialActivityAccountId).getResourceId());
    }

    @Test
    public void testFromSavingsToSavingsAccountTransfer() {
        Long toOfficeId = createOffice();
        Long toClientId = createClientInOffice(toOfficeId);
        Long toSavingsId = createActiveSavingsAccount(toClientId, createCashBasedSavingsProduct());

        Long fromOfficeId = createOffice();
        Long fromClientId = createClientInOffice(fromOfficeId);
        Long fromSavingsId = createActiveSavingsAccount(fromClientId, createCashBasedSavingsProduct());

        accountTransferHelper
                .createAccountTransfer(savingsToSavings(fromClientId, fromSavingsId, fromClientId, toSavingsId, ACCOUNT_TRANSFER_AMOUNT));

        BigDecimal openingBalance = new BigDecimal(MINIMUM_OPENING_BALANCE);
        BigDecimal transferred = new BigDecimal(ACCOUNT_TRANSFER_AMOUNT);
        SavingsTestValidators.verifyAmount(openingBalance.subtract(transferred),
                savingsHelper.getSavingsSummary(fromSavingsId).getAccountBalance(),
                "Verifying From Savings Account Balance after Account Transfer");
        SavingsTestValidators.verifyAmount(openingBalance.add(transferred),
                savingsHelper.getSavingsSummary(toSavingsId).getAccountBalance(),
                "Verifying To Savings Account Balance after Account Transfer");

        verifyLiabilityTransferEntries(fromOfficeId, toOfficeId, ACCOUNT_TRANSFER_AMOUNT);
    }

    @Test
    public void testFromSavingsToSavingsAccountTransferWithoutPaymentDetailsReadResponseRemainsCompatible() {
        SavingsTransferFixture fixture = createSavingsTransferFixture();

        Long accountTransferDetailId = accountTransferHelper.createAccountTransfer(savingsToSavings(fixture.fromClientId,
                fixture.fromSavingsId, fixture.toClientId, fixture.toSavingsId, ACCOUNT_TRANSFER_AMOUNT)).getResourceId();

        List<GetAccountTransfersPageItems> transfers = accountTransferHelper.retrieveTransfersByAccountDetailId(accountTransferDetailId);
        assertEquals(1, transfers.size());
        assertNull(transfers.get(0).getPaymentDetailData(), "A transfer made without payment details should report none");
    }

    @Test
    public void testFromSavingsToSavingsAccountTransferWithPaymentDetailsPersistsAndReadsPaymentDetails() {
        SavingsTransferFixture fixture = createSavingsTransferFixture();
        Long paymentTypeId = createPaymentType();

        AccountTransferRequest request = AccountTransferRequestBuilders.withPaymentDetails(savingsToSavings(fixture.fromClientId,
                fixture.fromSavingsId, fixture.toClientId, fixture.toSavingsId, ACCOUNT_TRANSFER_AMOUNT), paymentTypeId, "ACC-2733",
                "CHK-2733", "RT-2733", "RC-2733", "BNK-2733");
        Long accountTransferDetailId = accountTransferHelper.createAccountTransfer(request).getResourceId();

        List<GetAccountTransfersPageItems> transfers = accountTransferHelper.retrieveTransfersByAccountDetailId(accountTransferDetailId);
        assertEquals(1, transfers.size());

        GetAccountTransfersPageItemsPaymentDetailData paymentDetailData = transfers.get(0).getPaymentDetailData();
        assertNotNull(paymentDetailData);
        assertEquals(paymentTypeId, paymentDetailData.getPaymentType().getId());
        assertEquals("ACC-2733", paymentDetailData.getAccountNumber());
        assertEquals("CHK-2733", paymentDetailData.getCheckNumber());
        assertEquals("RT-2733", paymentDetailData.getRoutingCode());
        assertEquals("RC-2733", paymentDetailData.getReceiptNumber());
        assertEquals("BNK-2733", paymentDetailData.getBankNumber());
    }

    /**
     * Payment details are rejected before the accounts are resolved, so the transfer names seeded ids rather than
     * building two accounts it never reaches.
     */
    @Test
    public void testAccountTransferRejectsPaymentDetailsWithoutPaymentType() {
        AccountTransferRequest request = AccountTransferRequestBuilders
                .transfer(ACCOUNT_TRANSFER_DATE, 1L, 1L, PortfolioAccountType.SAVINGS, 1L, 2L, PortfolioAccountType.SAVINGS, "100.0")
                .accountNumber("ACC-2733");
        assertEquals(400, accountTransferHelper.createAccountTransferExpectingError(request).getStatus());
    }

    @Test
    public void testFromSavingsToLoanAccountTransfer() {
        Long toOfficeId = createOffice();
        Long toClientId = createClientInOffice(toOfficeId);
        Long toLoanId = createActiveLoan(toClientId);

        Long fromOfficeId = createOffice();
        Long fromClientId = createClientInOffice(fromOfficeId);
        Long fromSavingsId = createActiveSavingsAccount(fromClientId, createCashBasedSavingsProduct());

        accountTransferHelper
                .createAccountTransfer(AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fromClientId, fromSavingsId,
                        PortfolioAccountType.SAVINGS, toClientId, toLoanId, PortfolioAccountType.LOAN, ACCOUNT_TRANSFER_AMOUNT_ADJUST));

        BigDecimal transferred = new BigDecimal(ACCOUNT_TRANSFER_AMOUNT_ADJUST);
        SavingsTestValidators.verifyAmount(new BigDecimal(MINIMUM_OPENING_BALANCE).subtract(transferred),
                savingsHelper.getSavingsSummary(fromSavingsId).getAccountBalance(),
                "Verifying From Savings Account Balance after Account Transfer");
        SavingsTestValidators.verifyAmount(transferred, loanHelper.getLoanDetails(toLoanId).getSummary().getTotalRepayment(),
                "Verifying To Loan Repayment Amount after Account Transfer");

        verifyLiabilityTransferEntries(fromOfficeId, toOfficeId, ACCOUNT_TRANSFER_AMOUNT_ADJUST);
    }

    @Test
    public void testFromLoanToSavingsAccountTransfer() {
        Long toOfficeId = createOffice();
        Long toClientId = createClientInOffice(toOfficeId);
        Long toSavingsId = createActiveSavingsAccount(toClientId, createCashBasedSavingsProduct());

        Long fromOfficeId = createOffice();
        Long fromClientId = createClientInOffice(fromOfficeId);
        Long loanId = createActiveLoan(fromClientId);
        Long fromSavingsId = createActiveSavingsAccount(fromClientId, createCashBasedSavingsProduct());

        accountTransferHelper.createAccountTransfer(AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fromClientId,
                fromSavingsId, PortfolioAccountType.SAVINGS, fromClientId, loanId, PortfolioAccountType.LOAN, ACCOUNT_TRANSFER_AMOUNT));

        SavingsTestValidators.verifyAmount(new BigDecimal(MINIMUM_OPENING_BALANCE).subtract(new BigDecimal(ACCOUNT_TRANSFER_AMOUNT)),
                savingsHelper.getSavingsSummary(fromSavingsId).getAccountBalance(),
                "Verifying From Savings Account Balance after Account Transfer");

        accountTransferHelper.createAccountTransfer(AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fromClientId, loanId,
                PortfolioAccountType.LOAN, toClientId, toSavingsId, PortfolioAccountType.SAVINGS, ACCOUNT_TRANSFER_AMOUNT_ADJUST));

        SavingsTestValidators.verifyAmount(new BigDecimal(MINIMUM_OPENING_BALANCE).add(new BigDecimal(ACCOUNT_TRANSFER_AMOUNT_ADJUST)),
                savingsHelper.getSavingsSummary(toSavingsId).getAccountBalance(),
                "Verifying From Savings Account Balance after Account Transfer");

        verifyLiabilityTransferEntries(fromOfficeId, toOfficeId, ACCOUNT_TRANSFER_AMOUNT_ADJUST);
    }

    @Test
    public void testTransferWithNegativeAmount() {
        SavingsToLoanFixture fixture = createSavingsToLoanFixture();
        assertNotNull(accountTransferHelper.createAccountTransferExpectingError(AccountTransferRequestBuilders.transfer(
                ACCOUNT_TRANSFER_DATE, fixture.clientId, fixture.savingsId, PortfolioAccountType.SAVINGS, fixture.clientId, fixture.loanId,
                PortfolioAccountType.LOAN, ACCOUNT_TRANSFER_NEGATIVE_AMOUNT)));
    }

    @Test
    public void testTransferWithInsufficientBalance() {
        SavingsToLoanFixture fixture = createSavingsToLoanFixture();
        assertNotNull(accountTransferHelper.createAccountTransferExpectingError(AccountTransferRequestBuilders.transfer(
                ACCOUNT_TRANSFER_DATE, fixture.clientId, fixture.savingsId, PortfolioAccountType.SAVINGS, fixture.clientId, fixture.loanId,
                PortfolioAccountType.LOAN, ACCOUNT_TRANSFER_LARGE_AMOUNT)));
    }

    @Test
    public void testTransferToInvalidAccountTypes() {
        SavingsToLoanFixture fixture = createSavingsToLoanFixture();
        assertNotNull(accountTransferHelper.createAccountTransferExpectingError(
                AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fixture.clientId, fixture.savingsId, INVALID_ACCOUNT_TYPE,
                        fixture.clientId, fixture.loanId, INVALID_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT)));
    }

    @Test
    public void testTransferToNonExistentAccount() {
        SavingsToLoanFixture fixture = createSavingsToLoanFixture();
        assertNotNull(accountTransferHelper.createAccountTransferExpectingError(AccountTransferRequestBuilders.transfer(
                ACCOUNT_TRANSFER_DATE, fixture.clientId, fixture.savingsId, PortfolioAccountType.SAVINGS, fixture.clientId, INVALID_LOAN_ID,
                PortfolioAccountType.LOAN, ACCOUNT_TRANSFER_AMOUNT)));
    }

    /**
     * The name is a misnomer kept from the RestAssured test: the server does not reject this date. It parses
     * {@code 01 05 2013} leniently as 1 May 2013 even though the body declares {@code dd MMMM yyyy}, and the original
     * expected the 200 this asserts.
     */
    @Test
    public void testFromSavingsToSavingsAccountTransferWithInvalidTransferDate() {
        SavingsTransferFixture fixture = createSavingsTransferFixture();
        assertNotNull(accountTransferHelper.createAccountTransfer(AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_INVALID_DATE,
                fixture.fromClientId, fixture.fromSavingsId, PortfolioAccountType.SAVINGS, fixture.fromClientId, fixture.toSavingsId,
                PortfolioAccountType.SAVINGS, ACCOUNT_TRANSFER_AMOUNT)).getResourceId());
    }

    private AccountTransferRequest savingsToSavings(Long fromClientId, Long fromSavingsId, Long toClientId, Long toSavingsId,
            String amount) {
        return AccountTransferRequestBuilders.transfer(ACCOUNT_TRANSFER_DATE, fromClientId, fromSavingsId, PortfolioAccountType.SAVINGS,
                toClientId, toSavingsId, PortfolioAccountType.SAVINGS, amount);
    }

    /**
     * Both offices post to the same liability transfer account: the sending one credits it, the receiving one debits.
     */
    private void verifyLiabilityTransferEntries(Long fromOfficeId, Long toOfficeId, String amount) {
        journalEntryHelper.checkJournalEntryForLiabilityAccount(fromOfficeId, liabilityTransferAccount, ACCOUNT_TRANSFER_DATE,
                LoanTestData.Journal.credit(liabilityTransferAccount.getAccountID().longValue(), Double.parseDouble(amount)));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(toOfficeId, liabilityTransferAccount, ACCOUNT_TRANSFER_DATE,
                LoanTestData.Journal.debit(liabilityTransferAccount.getAccountID().longValue(), Double.parseDouble(amount)));
    }

    private SavingsTransferFixture createSavingsTransferFixture() {
        Long savingsProductId = createCashBasedSavingsProduct();
        Long fromClientId = createClientInOffice(createOffice());
        Long fromSavingsId = createActiveSavingsAccount(fromClientId, savingsProductId);
        Long toClientId = createClientInOffice(createOffice());
        Long toSavingsId = createActiveSavingsAccount(toClientId, savingsProductId);
        return new SavingsTransferFixture(fromClientId, fromSavingsId, toClientId, toSavingsId);
    }

    /** The shape every rejection test needs: one funded savings account and one active loan of the same client. */
    private SavingsToLoanFixture createSavingsToLoanFixture() {
        Long clientId = createClientInOffice(createOffice());
        Long loanId = createActiveLoan(clientId);
        Long savingsId = createActiveSavingsAccount(clientId, createCashBasedSavingsProduct());
        return new SavingsToLoanFixture(clientId, savingsId, loanId);
    }

    private Long createOffice() {
        return officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(ClientRequestBuilders.createActivePersonClient(CLIENT_ACTIVATION_DATE).officeId(officeId))
                .getResourceId();
    }

    private Long createCashBasedSavingsProduct() {
        PostSavingsProductsRequest request = SavingsRequestBuilders.withCashBasedAccounting(
                SavingsRequestBuilders.defaultSavingsProduct().withdrawalFeeForTransfers(true).withHoldTax(false)
                        .minRequiredOpeningBalance(new BigDecimal(MINIMUM_OPENING_BALANCE)),
                accountHelper.createAssetAccount(), accountHelper.createLiabilityAccount(), accountHelper.createIncomeAccount(),
                accountHelper.createExpenseAccount());
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private Long createActiveSavingsAccount(Long clientId, Long savingsProductId) {
        Long savingsId = submitSavingsApplication(clientId, savingsProductId, SAVINGS_SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        approveSavings(savingsId, SAVINGS_APPROVED_ON_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        activateSavings(savingsId, SAVINGS_ACTIVATED_ON_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createActiveLoan(Long clientId) {
        Long loanProductId = loanHelper.createLoanProduct(cashBasedLoanProduct()).getResourceId();
        Long loanId = loanHelper.applyForLoan(loanApplication(clientId, loanProductId)).getLoanId();

        loanHelper.approveLoan(loanId, new PostLoansLoanIdRequest().approvedOnDate(LOAN_APPROVAL_DATE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE));
        loanHelper.disburseLoan(loanId,
                new PostLoansLoanIdRequest().actualDisbursementDate(LOAN_DISBURSAL_DATE)
                        .transactionAmount(loanHelper.getLoanDetails(loanId).getNetDisbursalAmount())
                        .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE));
        return loanId;
    }

    /** Cash-based accounting maps one account per type, exactly as the RestAssured loan product builder did. */
    private PostLoanProductsRequest cashBasedLoanProduct() {
        Account assetAccount = accountHelper.createAssetAccount();
        Account incomeAccount = accountHelper.createIncomeAccount();
        Account expenseAccount = accountHelper.createExpenseAccount();
        Account overpaymentAccount = accountHelper.createLiabilityAccount();
        return new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Account transfer loan product")//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .principal(8000.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .daysInMonthType(LoanTestData.DaysInMonthType.ACTUAL)//
                .daysInYearType(LoanTestData.DaysInYearType.ACTUAL)//
                .isInterestRecalculationEnabled(false)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .fundSourceAccountId(SavingsRequestBuilders.accountId(assetAccount))//
                .loanPortfolioAccountId(SavingsRequestBuilders.accountId(assetAccount))//
                .transfersInSuspenseAccountId(SavingsRequestBuilders.accountId(assetAccount))//
                .interestOnLoanAccountId(SavingsRequestBuilders.accountId(incomeAccount))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(incomeAccount))//
                .incomeFromPenaltyAccountId(SavingsRequestBuilders.accountId(incomeAccount))//
                .incomeFromRecoveryAccountId(SavingsRequestBuilders.accountId(incomeAccount))//
                .writeOffAccountId(SavingsRequestBuilders.accountId(expenseAccount))//
                .goodwillCreditAccountId(SavingsRequestBuilders.accountId(expenseAccount))//
                .chargeOffExpenseAccountId(SavingsRequestBuilders.accountId(expenseAccount))//
                .chargeOffFraudExpenseAccountId(SavingsRequestBuilders.accountId(expenseAccount))//
                .overpaymentLiabilityAccountId(SavingsRequestBuilders.accountId(overpaymentAccount))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private PostLoansRequest loanApplication(Long clientId, Long loanProductId) {
        Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .loanType("individual")//
                .principal(new BigDecimal("8000.00"))//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(new BigDecimal("2"))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .maxOutstandingLoanBalance(MAX_OUTSTANDING_LOAN_BALANCE)//
                .expectedDisbursementDate(LOAN_SUBMITTED_ON_DATE)//
                .submittedOnDate(LOAN_SUBMITTED_ON_DATE)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)))//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private Long createPaymentType() {
        return paymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest()//
                .name(Utils.uniqueRandomStringGenerator("P_T", 5))//
                .description(Utils.uniqueRandomStringGenerator("PT_Desc", 15))//
                .isCashPayment(false)//
                .position(1L)).getResourceId();
    }

    private record SavingsTransferFixture(Long fromClientId, Long fromSavingsId, Long toClientId, Long toSavingsId) {
    }

    private record SavingsToLoanFixture(Long clientId, Long savingsId, Long loanId) {
    }
}
