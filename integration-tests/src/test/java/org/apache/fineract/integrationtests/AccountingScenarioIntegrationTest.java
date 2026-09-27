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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetRecurringDepositProductsProductIdResponse;
import org.apache.fineract.client.models.GetSavingsAccountsSavingsAccountIdChargesResponse;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostRecurringDepositProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRecurringDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRecurringDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsChargeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestValidators;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(2)
public class AccountingScenarioIntegrationTest extends FeignLoanTestBase {

    private static final int PRE_CLOSURE_PENAL_INTEREST_ON_WHOLE_TERM = 1;

    private static final Logger LOG = LoggerFactory.getLogger(AccountingScenarioIntegrationTest.class);

    private static final String DATE_OF_JOINING = "01 January 2011";

    private static final Float LP_PRINCIPAL = 10000.0f;
    private static final String LP_REPAYMENTS = "5";
    private static final String LP_REPAYMENT_PERIOD = "2";
    private static final String LP_INTEREST_RATE = "1";
    private static final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "03 March 2011";
    private static final String TRANSACTION_DATE = "01 March 2013";
    private static final int LOAN_TERM_FREQUENCY = 10;
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String DEPOSIT_AMOUNT = "7000";
    public static final String WITHDRAWAL_AMOUNT = "3000";
    public static final String WITHDRAWAL_AMOUNT_ADJUSTED = "2000";
    private static final String SAVINGS_SUBMITTED_ON_DATE = "08 January 2013";
    private static final String SAVINGS_APPROVED_ON_DATE = "09 January 2013";
    private static final String SAVINGS_CHARGE_AMOUNT = "100";
    private static final String SAVINGS_CHARGE_FEE_ON_MONTH_DAY = "15 January";
    private static final String SHARE_DATE = "01 Jan 2016";

    static Float SP_BALANCE = Float.valueOf(MINIMUM_OPENING_BALANCE);
    static Float SP_DEPOSIT_AMOUNT = Float.valueOf(DEPOSIT_AMOUNT);
    static Float SP_WITHDRAWAL_AMOUNT = Float.valueOf(WITHDRAWAL_AMOUNT);
    static Float SP_WITHDRAWAL_AMOUNT_ADJUSTED = Float.valueOf(WITHDRAWAL_AMOUNT_ADJUSTED);

    private static final String[] REPAYMENT_DATE = { "", "04 May 2011", "04 July 2011", "04 September 2011", "04 November 2011",
            "04 January 2012" };
    private static final Float[] REPAYMENT_AMOUNT = { .0f, 2200.0f, 3000.0f, 900.0f, 2000.0f, 2500.0f };

    private static final Float AMOUNT_TO_BE_WAIVE = 400.0f;
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";

    private FeignCollateralHelper collateralHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsTransactionHelper savingsTransactionHelper;
    private FeignSavingsChargeHelper savingsChargeHelper;
    private FeignFixedDepositHelper fixedDepositHelper;
    private FeignFixedDepositProductHelper fixedDepositProductHelper;
    private FeignRecurringDepositHelper recurringDepositHelper;
    private FeignRecurringDepositProductHelper recurringDepositProductHelper;
    private FeignShareAccountHelper shareAccountHelper;

    private TimeZone tenantTimeZone;

    @BeforeAll
    public void setup() {
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        this.savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient());
        this.savingsChargeHelper = new FeignSavingsChargeHelper(fineractClient());
        this.fixedDepositHelper = new FeignFixedDepositHelper(fineractClient());
        this.fixedDepositProductHelper = new FeignFixedDepositProductHelper(fineractClient());
        this.recurringDepositHelper = new FeignRecurringDepositHelper(fineractClient());
        this.recurringDepositProductHelper = new FeignRecurringDepositProductHelper(fineractClient());
        this.shareAccountHelper = new FeignShareAccountHelper(fineractClient());

        this.tenantTimeZone = TimeZone.getTimeZone(Utils.TENANT_TIME_ZONE);
    }

    @Test
    public void checkUpfrontAccrualAccountingFlow() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithUpfrontAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        approveAndDisburse(loanID, EXPECTED_DISBURSAL_DATE);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, journal(TOTAL_INTEREST, assetAccount, DEBIT),
                journal(LP_PRINCIPAL, assetAccount, CREDIT), journal(LP_PRINCIPAL, assetAccount, DEBIT));
        LOG.info("CHECKING INCOME: ******************************************");
        checkJournalEntryForIncomeAccount(incomeAccount, EXPECTED_DISBURSAL_DATE, journal(TOTAL_INTEREST, incomeAccount, CREDIT));

        // MAKE 1
        LOG.info("Repayment 1 ......");
        makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], journal(REPAYMENT_AMOUNT[1], assetAccount, DEBIT),
                journal(FIRST_INTEREST + FIRST_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], journal(REPAYMENT_AMOUNT[2], assetAccount, DEBIT),
                journal(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        addInterestWaiver(loanID, waiveInterest(AMOUNT_TO_BE_WAIVE, REPAYMENT_DATE[4]));

        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], journal(AMOUNT_TO_BE_WAIVE, assetAccount, CREDIT));

        checkJournalEntryForExpenseAccount(expenseAccount, REPAYMENT_DATE[4], journal(AMOUNT_TO_BE_WAIVE, expenseAccount, DEBIT));
        LOG.info("Waive Interest Done......");

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], journal(REPAYMENT_AMOUNT[3], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[3], assetAccount, CREDIT));
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], journal(REPAYMENT_AMOUNT[4], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[4], assetAccount, CREDIT));
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], journal(REPAYMENT_AMOUNT[5], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[5], assetAccount, CREDIT));
        LOG.info("Repayment 5 Done  ......");
    }

    private Long createLoanProductWithUpfrontAccrualAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleUpfrontAccrual(accounts)
                .buildRequest());
    }

    private Long applyForLoanApplication(final Long clientID, final Long loanProductID, final Long clientCollateralId) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanApplication = LoanRequestBuilders
                .legacyIndividualApplication(clientID, loanProductID, LP_PRINCIPAL.toString(), Integer.parseInt(LP_REPAYMENTS),
                        new BigDecimal(LP_INTEREST_RATE), EXPECTED_DISBURSAL_DATE)//
                .loanTermFrequency(LOAN_TERM_FREQUENCY)//
                .repaymentEvery(Integer.parseInt(LP_REPAYMENT_PERIOD))//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .submittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));
        return applyForLoan(loanApplication);
    }

    @Test
    public void checkAccountingWithSavingsFlow() {

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        final Long savingsProductID = createSavingsProduct(SavingsRequestBuilders.withCashBasedAccounting(savingsProductRequest(),
                assetAccount, liabilityAccount, incomeAccount, expenseAccount));

        verifySavingsAccountingFlow(savingsProductID, assetAccount, incomeAccount, liabilityAccount);
    }

    @Test
    public void checkAccountingWithSavingsFlowUsingAccrualAccounting() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        final Long savingsProductID = createSavingsProduct(SavingsRequestBuilders.withAccrualAccountingMappings(
                savingsProductRequest().accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC), assetAccount, liabilityAccount,
                incomeAccount, expenseAccount));
        final GetSavingsProductsProductIdResponse savingsProductsResponse = savingsProductHelper.getSavingsProduct(savingsProductID);
        Assertions.assertNotNull(savingsProductsResponse);
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getSavingsControlAccount());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getInterestPayableAccount());

        verifySavingsAccountingFlow(savingsProductID, assetAccount, incomeAccount, liabilityAccount);
    }

    private void verifySavingsAccountingFlow(final Long savingsProductID, final Account assetAccount, final Account incomeAccount,
            final Account liabilityAccount) {
        final Long clientID = createClient(DATE_OF_JOINING);
        final Long savingsID = savingsHelper.submitApplication(clientID, savingsProductID, SAVINGS_SUBMITTED_ON_DATE).getSavingsId();

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsID));

        savingsHelper.approveSavings(savingsID, SAVINGS_APPROVED_ON_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsID));

        savingsHelper.activateSavings(savingsID, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsID));

        // Checking initial Account entries.
        checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, journal(SP_BALANCE, assetAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, journal(SP_BALANCE, liabilityAccount, CREDIT));

        // First Transaction-Deposit
        savingsTransactionHelper.deposit(savingsID, DEPOSIT_AMOUNT, TRANSACTION_DATE);
        Float balance = SP_BALANCE + SP_DEPOSIT_AMOUNT;
        assertBalance(balance, savingsID, "Verifying Balance after Deposit");

        LOG.info("----------------------Verifying Journal Entry after the Transaction Deposit----------------------------");
        checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, journal(SP_DEPOSIT_AMOUNT, assetAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, journal(SP_DEPOSIT_AMOUNT, liabilityAccount, CREDIT));

        // Second Transaction-Withdrawal
        savingsTransactionHelper.withdraw(savingsID, WITHDRAWAL_AMOUNT, TRANSACTION_DATE);
        balance -= SP_WITHDRAWAL_AMOUNT;
        assertBalance(balance, savingsID, "Verifying Balance after Withdrawal");

        LOG.info("-------------------Verifying Journal Entry after the Transaction Withdrawal----------------------");
        checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, journal(SP_WITHDRAWAL_AMOUNT, assetAccount, CREDIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, journal(SP_WITHDRAWAL_AMOUNT, liabilityAccount, DEBIT));

        // Third Transaction-Add Charges for Withdrawal Fee
        final Long withdrawalChargeId = chargesHelper.createCharge(SavingsRequestBuilders.savingsWithdrawalFeeCharge()).getResourceId();
        Assertions.assertNotNull(withdrawalChargeId);

        savingsChargeHelper.addChargeWithFeeOnMonthDay(savingsID, withdrawalChargeId, SAVINGS_CHARGE_AMOUNT,
                SAVINGS_CHARGE_FEE_ON_MONTH_DAY);
        List<GetSavingsAccountsSavingsAccountIdChargesResponse> chargesPendingState = savingsHelper.getSavingsCharges(savingsID);
        assertEquals(1, chargesPendingState.size());
        GetSavingsAccountsSavingsAccountIdChargesResponse savingsChargeForPay = chargesPendingState.get(0);
        Float chargeAmount = savingsHelper.getSavingsAccountCharge(savingsID, savingsChargeForPay.getId()).getAmount().floatValue();

        // Withdrawal after adding Charge of type Withdrawal Fee
        savingsTransactionHelper.withdraw(savingsID, WITHDRAWAL_AMOUNT_ADJUSTED, TRANSACTION_DATE);
        balance = balance - SP_WITHDRAWAL_AMOUNT_ADJUSTED - chargeAmount;

        checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, journal(SP_WITHDRAWAL_AMOUNT_ADJUSTED, assetAccount, CREDIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, journal(chargeAmount, liabilityAccount, DEBIT),
                journal(SP_WITHDRAWAL_AMOUNT_ADJUSTED, liabilityAccount, DEBIT));
        checkJournalEntryForIncomeAccount(incomeAccount, TRANSACTION_DATE, journal(chargeAmount, incomeAccount, CREDIT));

        // Verifying Balance after applying Charge for Withdrawal Fee
        assertBalance(balance, savingsID, "Verifying Balance");

        // "Post Interest For Savings" is a server wide job: an account left active on a 2013 date forces every
        // later run to replay more than a decade of interest, which times out other tests sharing the instance
        savingsHelper.closeSavings(savingsID, TRANSACTION_DATE, true);
    }

    @Test
    public void testFixedDepositAccountingFlow() {
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(3);
        final String VALID_FROM = dateFormat.format(todaysDate);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = dateFormat.format(todaysDate);

        todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate);
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate);

        todaysDate = todaysDate.plusMonths(1).withDayOfMonth(1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate);

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        Long clientId = createClient();
        Assertions.assertEquals(clientId, clientHelper.getClient(clientId).getId());

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount, incomeAccount,
                expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = fixedDepositHelper.submitApplication(DepositRequestBuilders.fixedDepositAccount(clientId,
                fixedDepositProductId, SUBMITTED_ON_DATE, PRE_CLOSURE_PENAL_INTEREST_ON_WHOLE_TERM)).getSavingsId();
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus());

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus());

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus());

        Float depositAmount = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalDeposits().floatValue();

        // Checking initial Journal entries after Activation.
        checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, journal(depositAmount, assetAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, journal(depositAmount, liabilityAccount, CREDIT));

        Long transactionIdForPostInterest = fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId();
        Assertions.assertNotNull(transactionIdForPostInterest);

        Float totalInterestPosted = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalInterestPosted().floatValue();

        // Checking initial Journal entries after Interest Posting.
        checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, journal(totalInterestPosted, expenseAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                journal(totalInterestPosted, liabilityAccount, CREDIT));

    }

    @Test
    public void testRecurringDepositAccountingFlow() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(3);
        final String VALID_FROM = dateFormat.format(todaysDate);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = dateFormat.format(todaysDate);

        todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate);
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate);
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate);

        todaysDate = todaysDate.plusMonths(1).withDayOfMonth(1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate);

        Long clientId = createClient();
        Assertions.assertEquals(clientId, clientHelper.getClient(clientId).getId());

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount, incomeAccount,
                expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);
        final GetRecurringDepositProductsProductIdResponse recurringDepositProductsProduct = recurringDepositProductHelper
                .getProduct(recurringDepositProductId);
        Assertions.assertNotNull(recurringDepositProductsProduct);
        Assertions.assertNotNull(recurringDepositProductsProduct.getAccountingMappings());
        Assertions.assertNotNull(recurringDepositProductsProduct.getAccountingMappings().getSavingsControlAccount());
        Assertions.assertNull(recurringDepositProductsProduct.getAccountingMappings().getInterestPayableAccount());

        Long recurringDepositAccountId = recurringDepositHelper.submitApplication(DepositRequestBuilders.recurringDepositAccount(clientId,
                recurringDepositProductId, SUBMITTED_ON_DATE, EXPECTED_FIRST_DEPOSIT_ON_DATE, PRE_CLOSURE_PENAL_INTEREST_ON_WHOLE_TERM))
                .getSavingsId();
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(recurringDepositHelper.getAccount(recurringDepositAccountId).getStatus());

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(recurringDepositHelper.getAccount(recurringDepositAccountId).getStatus());

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(recurringDepositHelper.getAccount(recurringDepositAccountId).getStatus());

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();

        Long depositTransactionId = recurringDepositHelper.deposit(recurringDepositAccountId, EXPECTED_FIRST_DEPOSIT_ON_DATE, depositAmount)
                .getResourceId();
        Assertions.assertNotNull(depositTransactionId);

        // Checking initial Journal entries after Activation.
        checkJournalEntryForAssetAccount(assetAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE,
                journal(depositAmount.floatValue(), assetAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE,
                journal(depositAmount.floatValue(), liabilityAccount, CREDIT));

        Long interestPostingTransactionId = recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId();
        Assertions.assertNotNull(interestPostingTransactionId);

        Float totalInterestPosted = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalInterestPosted().floatValue();

        // Checking initial Journal entries after Interest Posting.
        checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, journal(totalInterestPosted, expenseAccount, DEBIT));
        checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                journal(totalInterestPosted, liabilityAccount, CREDIT));

    }

    private Long createSavingsProduct(final PostSavingsProductsRequest request) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private static PostSavingsProductsRequest savingsProductRequest() {
        return SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.QUARTERLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .minRequiredOpeningBalance(new BigDecimal(MINIMUM_OPENING_BALANCE));
    }

    private Long createFixedDepositProduct(final String validFrom, final String validTo, final Account assetAccount,
            final Account liabilityAccount, final Account incomeAccount, final Account expenseAccount) {
        LOG.info("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        PostFixedDepositProductsRequest request = DepositRequestBuilders.withCashBasedAccounting(
                DepositRequestBuilders.fixedDepositProduct(), assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        return fixedDepositProductHelper
                .createProduct(DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.periodRangeChartSlabs()))
                .getResourceId();
    }

    private Long createRecurringDepositProduct(final String validFrom, final String validTo, final Account assetAccount,
            final Account liabilityAccount, final Account incomeAccount, final Account expenseAccount) {
        LOG.info("------------------------------CREATING NEW RECURRING DEPOSIT PRODUCT ---------------------------------------");
        PostRecurringDepositProductsRequest request = DepositRequestBuilders.withCashBasedAccounting(
                DepositRequestBuilders.recurringDepositProduct(), assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        return recurringDepositProductHelper
                .createProduct(
                        DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.recurringChartSlabsFor("period")))
                .getResourceId();
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow() throws InterruptedException, ParseException {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        approveAndDisburse(loanID, EXPECTED_DISBURSAL_DATE);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, journal(LP_PRINCIPAL, assetAccount, CREDIT),
                journal(LP_PRINCIPAL, assetAccount, DEBIT));

        final String jobName = "Add Accrual Transactions";

        schedulerHelper.executeAndAwaitJob(jobName);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[1]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], journal(REPAYMENT_AMOUNT[1], assetAccount, DEBIT),
                journal(FIRST_INTEREST + FIRST_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[2]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[3]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], journal(REPAYMENT_AMOUNT[2], assetAccount, DEBIT),
                journal(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[4]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[5]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        addInterestWaiver(loanID, waiveInterest(AMOUNT_TO_BE_WAIVE, REPAYMENT_DATE[4]));

        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], journal(AMOUNT_TO_BE_WAIVE, assetAccount, CREDIT));

        checkJournalEntryForExpenseAccount(expenseAccount, REPAYMENT_DATE[4], journal(AMOUNT_TO_BE_WAIVE, expenseAccount, DEBIT));
        LOG.info("Waive Interest Done......");

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], journal(REPAYMENT_AMOUNT[3], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[3], assetAccount, CREDIT));
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], journal(REPAYMENT_AMOUNT[4], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[4], assetAccount, CREDIT));
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], journal(REPAYMENT_AMOUNT[5], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[5], assetAccount, CREDIT));
        LOG.info("Repayment 5 Done  ......");
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow_OVER_PAYMENT() throws InterruptedException, ParseException {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        approveAndDisburse(loanID, EXPECTED_DISBURSAL_DATE);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, journal(LP_PRINCIPAL, assetAccount, CREDIT),
                journal(LP_PRINCIPAL, assetAccount, DEBIT));

        final String jobName = "Add Accrual Transactions";

        schedulerHelper.executeAndAwaitJob(jobName);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[1]), FIRST_INTEREST, FEE_PORTION, PENALTY_PORTION, loanID);
        makeRepayment(REPAYMENT_DATE[1], 15000f, loanID);
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], journal(15000f, assetAccount, DEBIT),
                journal(11000f, assetAccount, CREDIT));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, REPAYMENT_DATE[1], journal(4000f, overpaymentAccount, CREDIT));
        LOG.info("Repayment  Done......");

    }

    @Test
    public void checkPeriodicAccrualAccountingTillCurrentDateFlow() throws InterruptedException, ParseException {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        Long flat = createLoanSpecifiedDueDateCharge(FEE_PORTION, false);
        Long flatSpecifiedDueDate = createLoanSpecifiedDueDateCharge(PENALTY_PORTION, true);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approve(loanID, EXPECTED_DISBURSAL_DATE);

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        final LocalDate localDate = LocalDate.now(this.tenantTimeZone.toZoneId());
        final ZonedDateTime currentDate = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, this.tenantTimeZone.toZoneId());
        ZonedDateTime zonedDate = currentDate.minusDays(4);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(zonedDate);

        zonedDate = currentDate.minusDays(2);

        disburse(loanID, LOAN_DISBURSEMENT_DATE);

        addLoanCharge(loanID, flatSpecifiedDueDate, dateFormat.format(zonedDate), (double) PENALTY_PORTION);
        zonedDate = zonedDate.plusDays(1);
        addLoanCharge(loanID, flat, dateFormat.format(zonedDate), (double) FEE_PORTION);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, journal(LP_PRINCIPAL, assetAccount, CREDIT),
                journal(LP_PRINCIPAL, assetAccount, DEBIT));

        final String jobName = "Add Periodic Accrual Transactions";

        schedulerHelper.executeAndAwaitJob(jobName);

        final GetLoansLoanIdRepaymentPeriod firstPeriod = getRepaymentPeriods(loanID).get(1);
        // MAKE 1
        int totalDaysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(firstPeriod.getFromDate(), firstPeriod.getDueDate()));

        float totalInterest = firstPeriod.getInterestOriginalDue().floatValue();
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float interest4Days = totalInterest / totalDaysInPeriod * 4;
        interest4Days = Float.parseFloat(numberFormat.format(interest4Days));

        checkAccrualTransactionForRepayment(currentDate.toLocalDate(), interest4Days, FEE_PORTION, PENALTY_PORTION, loanID);

    }

    @Test
    public void checkPeriodicAccrualAccountingAPIFlow() throws ParseException {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        final float NEXT_FEE_PORTION = 55.0f;
        final float NEXT_PENALTY_PORTION = 105.0f;

        Long flat = createLoanSpecifiedDueDateCharge(FEE_PORTION, false);
        Long flatSpecifiedDueDate = createLoanSpecifiedDueDateCharge(PENALTY_PORTION, true);

        Long flatNext = createLoanSpecifiedDueDateCharge(NEXT_FEE_PORTION, false);
        Long flatSpecifiedDueDateNext = createLoanSpecifiedDueDateCharge(NEXT_PENALTY_PORTION, true);

        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approve(loanID, EXPECTED_DISBURSAL_DATE);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance(this.tenantTimeZone);

        todayDate.add(Calendar.DATE, -4);

        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.MONTH, 2);

        todayDate = Calendar.getInstance(this.tenantTimeZone);
        todayDate.add(Calendar.DATE, -2);

        disburse(loanID, LOAN_DISBURSEMENT_DATE);

        addLoanCharge(loanID, flatSpecifiedDueDate, dateFormat.format(todayDate.getTime()), (double) PENALTY_PORTION);
        todayDate.add(Calendar.DATE, 1);
        String runOndate = dateFormat.format(todayDate.getTime());

        addLoanCharge(loanID, flat, runOndate, (double) FEE_PORTION);

        todayDate.add(Calendar.DATE, 1);
        addLoanCharge(loanID, flatSpecifiedDueDateNext, dateFormat.format(todayDate.getTime()), (double) NEXT_PENALTY_PORTION);

        addLoanCharge(loanID, flatNext, dateFormat.format(todayDate.getTime()), (double) NEXT_FEE_PORTION);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, journal(LP_PRINCIPAL, assetAccount, CREDIT),
                journal(LP_PRINCIPAL, assetAccount, DEBIT));

        runPeriodicAccrualAccounting(runOndate);

        final GetLoansLoanIdRepaymentPeriod firstPeriod = getRepaymentPeriods(loanID).get(1);
        // MAKE 1
        int totalDaysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(firstPeriod.getFromDate(), firstPeriod.getDueDate()));

        float totalInterest = firstPeriod.getInterestOriginalDue().floatValue();
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float interest3Days = totalInterest / totalDaysInPeriod * 3;
        interest3Days = Float.parseFloat(numberFormat.format(interest3Days));
        checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), interest3Days, FEE_PORTION, PENALTY_PORTION, loanID);

        runOndate = dateFormat.format(todayDate.getTime());

        runPeriodicAccrualAccounting(runOndate);
        float interestPerDay = (totalInterest / totalDaysInPeriod * 4) - interest3Days;
        interestPerDay = Float.parseFloat(numberFormat.format(interestPerDay));
        checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), interestPerDay, NEXT_FEE_PORTION, NEXT_PENALTY_PORTION, loanID);

    }

    private Long createLoanProductWithPeriodicAccrualAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").buildRequest());
    }

    @Test
    public void checkCashBasedAccountingFlow() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        final Long loanProductID = createLoanProductWithCashBasedAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, createClientCollateral(clientID));

        approveAndDisburse(loanID, EXPECTED_DISBURSAL_DATE);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, journal(LP_PRINCIPAL, assetAccount, CREDIT),
                journal(LP_PRINCIPAL, assetAccount, DEBIT));

        // MAKE 1
        LOG.info("Repayment 1 ......");
        makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], journal(REPAYMENT_AMOUNT[1], assetAccount, DEBIT),
                journal(FIRST_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("CHECKING INCOME: ******************************************");
        checkJournalEntryForIncomeAccount(incomeAccount, REPAYMENT_DATE[1], journal(FIRST_INTEREST, incomeAccount, CREDIT));
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], journal(REPAYMENT_AMOUNT[2], assetAccount, DEBIT),
                journal(SECOND_PRINCIPAL, assetAccount, CREDIT));
        LOG.info("CHECKING INCOME: ******************************************");
        checkJournalEntryForIncomeAccount(incomeAccount, REPAYMENT_DATE[2], journal(SECOND_AND_THIRD_INTEREST, incomeAccount, CREDIT));
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        Long transactionId = addInterestWaiver(loanID, waiveInterest(AMOUNT_TO_BE_WAIVE, REPAYMENT_DATE[4]));
        // waive of fees and interest are not considered in cash based
        // accounting,
        Assertions.assertTrue(getJournalEntries("L" + transactionId).getPageItems().isEmpty(), "Tranasactions are is not empty");

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], journal(REPAYMENT_AMOUNT[3], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[3], assetAccount, CREDIT));
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], journal(REPAYMENT_AMOUNT[4], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[4], assetAccount, CREDIT));
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], journal(REPAYMENT_AMOUNT[5], assetAccount, DEBIT),
                journal(REPAYMENT_AMOUNT[5], assetAccount, CREDIT));
        LOG.info("Repayment 5 Done  ......");
    }

    private Long createLoanProductWithCashBasedAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsCashBased(accounts)
                .buildRequest());
    }

    private LocalDate getDateAsLocalDate(String dateAsString) {
        return LocalDate.parse(dateAsString, Utils.dateFormatter);
    }

    @Test
    public void checkAccountingWithSharingFlow() {

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account equityAccount = accountHelper.createEquityAccount(Utils.uniqueRandomStringGenerator("EQUITY_", 6));
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        final Long shareProductID = createSharesProduct(assetAccount, incomeAccount, equityAccount, liabilityAccount);

        final Long clientID = createClient(DATE_OF_JOINING);
        Assertions.assertNotNull(clientID);
        final Long savingsAccountId = openSavingsAccount(clientID);
        Assertions.assertNotNull(savingsAccountId);
        final Long shareAccountId = createShareAccount(clientID, shareProductID, savingsAccountId);
        Assertions.assertNotNull(shareAccountId);
        Assertions.assertNotNull(shareAccountHelper.getShareAccount(shareAccountId));
        // Approve share Account
        shareAccountHelper.approve(shareAccountId, SHARE_DATE, "Share Account Approval Note", "dd MMMM yyyy", "en");
        // Activate Share Account
        shareAccountHelper.activate(shareAccountId, SHARE_DATE, "dd MMMM yyyy", "en");

        // Checking sharing entries.
        final LoanTestData.Journal assetAccountEntry = journal(200f, assetAccount, DEBIT);
        checkJournalEntryForAssetAccount(assetAccount, SHARE_DATE, assetAccountEntry);
        checkJournalEntryForLiabilityAccount(liabilityAccount, SHARE_DATE, journal(200f, liabilityAccount, CREDIT));
        journalHelper.checkJournalEntryForEquityAccount(equityAccount, SHARE_DATE, journal(200f, equityAccount, CREDIT));

        final String transactionId = journalHelper.getJournalEntryTransactionIdByAccount(assetAccount, SHARE_DATE, assetAccountEntry);
        Assertions.assertNotEquals("", transactionId);

        final GetJournalEntriesTransactionIdResponse journalEntriesTransactionIdResponse = getJournalEntries(transactionId);
        Assertions.assertNotNull(journalEntriesTransactionIdResponse);
    }

    /** The cash-based share product the RestAssured {@code ShareProductHelper} built by default. */
    private Long createSharesProduct(final Account assetAccount, final Account incomeAccount, final Account equityAccount,
            final Account liabilityAccount) {
        LOG.info("------------------------------CREATING NEW SHARE PRODUCT ---------------------------------------");
        return shareAccountHelper.createShareProduct(new PostProductsTypeRequest()//
                .name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description(Utils.randomStringGenerator("", 20))//
                .currencyCode("USD")//
                .locale("en_GB")//
                .digitsAfterDecimal(4)//
                .inMultiplesOf(0)//
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
                .shareReferenceId(SavingsRequestBuilders.accountId(assetAccount))//
                .shareSuspenseId(SavingsRequestBuilders.accountId(liabilityAccount))//
                .shareEquityId(SavingsRequestBuilders.accountId(equityAccount))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(incomeAccount)));
    }

    private Long createShareAccount(final Long clientId, final Long productId, final Long savingsAccountId) {
        return shareAccountHelper.applyShareAccount(new AccountRequest().clientId(clientId).productId(productId).externalId("External1")
                .savingsAccountId(savingsAccountId).submittedDate(SHARE_DATE).applicationDate(SHARE_DATE).requestedShares(100L)
                .dateFormat("dd MMMM yyyy").locale("en"));
    }

    private Long openSavingsAccount(final Long clientId) {
        final Long savingsProductId = createSavingsProduct(
                SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.valueOf(1000)));
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, SAVINGS_SUBMITTED_ON_DATE).getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.approveSavings(savingsId, SAVINGS_APPROVED_ON_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.activateSavings(savingsId, TRANSACTION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createClientCollateral(final Long clientId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        return clientCollateralId;
    }

    private Long createLoanSpecifiedDueDateCharge(final float amount, final boolean penalty) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, amount, penalty))
                .getResourceId();
    }

    private void approveAndDisburse(final Long loanId, final String date) {
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        approve(loanId, date);
        disburse(loanId, date);
    }

    private void approve(final Long loanId, final String date) {
        approveLoan(loanId,
                new PostLoansLoanIdRequest().approvedOnDate(date).locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);
    }

    private void disburse(final Long loanId, final String date) {
        disburseLoan(loanId, LoanRequestBuilders.disburseLoanWithNetDisbursalAmount(date, getLoanDetails(loanId).getNetDisbursalAmount())
                .note("DISBURSE NOTE"));
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
    }

    private List<GetLoansLoanIdRepaymentPeriod> getRepaymentPeriods(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
    }

    private void verifyRepaymentScheduleEntryFor(final int repaymentNumber, final float expectedPrincipalOutstanding, final Long loanId) {
        final BigDecimal actual = getRepaymentPeriods(loanId).get(repaymentNumber).getPrincipalLoanBalanceOutstanding();
        Assertions.assertEquals(0, BigDecimal.valueOf(expectedPrincipalOutstanding).compareTo(actual),
                () -> "Mismatch in Principal Loan Balance Outstanding: expected " + expectedPrincipalOutstanding + " but was " + actual);
    }

    private void checkAccrualTransactionForRepayment(final LocalDate transactionDate, final float interestPortion, final float feePortion,
            final float penaltyPortion, final Long loanId) {
        GetLoansLoanIdTransactions accrual = getLoanDetails(loanId).getTransactions().stream()
                .filter(transaction -> Boolean.TRUE.equals(transaction.getType().getAccrual()))
                .filter(transaction -> transactionDate.equals(transaction.getDate())).findFirst().orElse(null);
        Assertions.assertNotNull(accrual, "No Accrual entries are posted");
        assertPortion(interestPortion, accrual.getInterestPortion());
        assertPortion(feePortion, accrual.getFeeChargesPortion());
        assertPortion(penaltyPortion, accrual.getPenaltyChargesPortion());
    }

    private static void assertPortion(final float expected, final BigDecimal actual) {
        Assertions.assertEquals(0, new BigDecimal(String.valueOf(expected)).compareTo(actual),
                () -> "Mismatch in transaction amounts: expected " + expected + " but was " + actual);
    }

    private void assertBalance(final Float expected, final Long savingsId, final String message) {
        final BigDecimal actual = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();
        Assertions.assertEquals(0, new BigDecimal(String.valueOf(expected)).compareTo(actual),
                () -> message + ": expected " + expected + " but was " + actual);
    }

    private LoanTestData.Journal journal(final float amount, final Account account, final String type) {
        return journalEntry(amount, account, type);
    }
}
