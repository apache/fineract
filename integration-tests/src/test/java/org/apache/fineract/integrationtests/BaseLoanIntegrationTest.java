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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public abstract class BaseLoanIntegrationTest {

    static {
        Utils.initializeRESTAssured();
    }

    protected static final String DATETIME_PATTERN = "dd MMMM yyyy";

    protected final ResponseSpecification responseSpec = createResponseSpecification(Matchers.is(200));
    protected final ResponseSpecification responseSpec204 = createResponseSpecification(Matchers.is(204));

    private final String fullAdminAuthKey = getFullAdminAuthKey();

    protected final RequestSpecification requestSpec = createRequestSpecification(fullAdminAuthKey);
    private final String nonByPassUserAuthKey = getNonByPassUserAuthKey(requestSpec, responseSpec);

    protected final AccountHelper accountHelper = new AccountHelper(requestSpec, responseSpec);
    protected final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
    protected final LoanProductHelper loanProductHelper = new LoanProductHelper();
    protected JournalEntryHelper journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    protected ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
    protected SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
    protected final InlineLoanCOBHelper inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);

    protected BusinessDateHelper businessDateHelper = new BusinessDateHelper();

    protected final LoanAccountLockHelper loanAccountLockHelper = new LoanAccountLockHelper(requestSpec,
            createResponseSpecification(Matchers.is(202)));
    protected static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    // asset
    protected final Account loansReceivableAccount = accountHelper.createAssetAccount("loanPortfolio");

    protected final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivable");
    protected final Account feeReceivableAccount = accountHelper.createAssetAccount("feeReceivable");
    protected final Account penaltyReceivableAccount = accountHelper.createAssetAccount("penaltyReceivable");
    protected final Account suspenseAccount = accountHelper.createAssetAccount("suspense");
    // liability
    protected final Account fundSource = accountHelper.createLiabilityAccount("fundSource");
    protected final Account overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");
    // income
    protected final Account interestIncomeAccount = accountHelper.createIncomeAccount("interestIncome");
    protected final Account feeIncomeAccount = accountHelper.createIncomeAccount("feeIncome");
    protected final Account penaltyIncomeAccount = accountHelper.createIncomeAccount("penaltyIncome");
    protected final Account feeChargeOffAccount = accountHelper.createIncomeAccount("feeChargeOff");
    protected final Account penaltyChargeOffAccount = accountHelper.createIncomeAccount("penaltyChargeOff");

    protected final Account recoveriesAccount = accountHelper.createIncomeAccount("recoveries");
    protected final Account interestIncomeChargeOffAccount = accountHelper.createIncomeAccount("interestIncomeChargeOff");
    // expense
    protected final Account chargeOffExpenseAccount = accountHelper.createExpenseAccount("chargeOff");
    protected final Account chargeOffFraudExpenseAccount = accountHelper.createExpenseAccount("chargeOffFraud");
    protected final Account writtenOffAccount = accountHelper.createExpenseAccount();
    protected final Account goodwillExpenseAccount = accountHelper.createExpenseAccount();

    private String getNonByPassUserAuthKey(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        // creates the user
        UserHelper.getSimpleUserWithoutBypassPermission(requestSpec, responseSpec);
        return Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(UserHelper.SIMPLE_USER_NAME, UserHelper.SIMPLE_USER_PASSWORD);
    }

    private String getFullAdminAuthKey() {
        return Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey();
    }

    // Loan product with proper accounting setup
    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() {
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Loan Product Description")//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(100000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 100)//
                .interestRateFrequencyType(2)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString()) //
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(true)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(Collections.emptyList())//
                .accountingRule(3)//
                .fundSourceAccountId(fundSource.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .dateFormat(DATETIME_PATTERN)//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50);
    }

    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() {
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")//
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()) //
                .addPaymentAllocationItem(defaultAllocation);
    }

    protected static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    protected static AdvancedPaymentData createDefaultPaymentAllocation(String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    protected static AdvancedPaymentData createDefaultPaymentAllocation(String transactionType, String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    protected PostLoanProductsRequest create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
            int interestType, int amortizationType) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .principal(1250.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue())//
                .interestType(interestType)//
                .amortizationType(amortizationType);
    }

    private RequestSpecification createRequestSpecification(String authKey) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + authKey);
        requestSpec.header("Fineract-Platform-TenantId", "default");
        return requestSpec;
    }

    protected ResponseSpecification createResponseSpecification(Matcher<Integer> statusCodeMatcher) {
        return new ResponseSpecBuilder().expectStatusCode(statusCodeMatcher).build();
    }

    protected void verifyUndoLastDisbursalShallFail(Long loanId, String expectedError) {
        ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
        LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(this.requestSpec, errorResponse);
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
            validationErrorHelper.undoLastDisbursalLoan(loanId, new PostLoansLoanIdRequest());
        });
        assertTrue(exception.getMessage().contains(expectedError));
    }

    protected void verifyNoTransactions(Long loanId) {
        verifyTransactions(loanId, (Transaction[]) null);
    }

    protected void verifyTransactions(Long loanId, Transaction... transactions) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        if (transactions == null || transactions.length == 0) {
            assertNull(loanDetails.getTransactions(), "No transaction is expected");
        } else {
            Assertions.assertEquals(transactions.length, loanDetails.getTransactions().size());
            Arrays.stream(transactions).forEach(tr -> {
                Optional<GetLoansLoanIdTransactions> optTx = loanDetails.getTransactions().stream()
                        .filter(item -> Objects.equals(item.getAmount(), tr.amount) //
                                && Objects.equals(item.getType().getValue(), tr.type) //
                                && Objects.equals(item.getDate(), LocalDate.parse(tr.date, dateTimeFormatter)))
                        .findFirst();
                Assertions.assertTrue(optTx.isPresent(), "Required transaction  not found: " + tr);

                GetLoansLoanIdTransactions tx = optTx.get();

                if (tr.reversed != null) {
                    Assertions.assertEquals(tr.reversed, tx.getManuallyReversed(), "Transaction is not reversed: " + tr);
                }
            });
        }
    }

    protected void verifyTransactions(Long loanId, TransactionExt... transactions) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        if (transactions == null || transactions.length == 0) {
            assertNull(loanDetails.getTransactions(), "No transaction is expected");
        } else {
            Assertions.assertEquals(transactions.length, loanDetails.getTransactions().size());
            Arrays.stream(transactions).forEach(tr -> {
                boolean found = loanDetails.getTransactions().stream().anyMatch(item -> Objects.equals(item.getAmount(), tr.amount) //
                        && Objects.equals(item.getType().getValue(), tr.type) //
                        && Objects.equals(item.getDate(), LocalDate.parse(tr.date, dateTimeFormatter)) //
                        && Objects.equals(item.getOutstandingLoanBalance(), tr.outstandingPrincipal) //
                        && Objects.equals(item.getPrincipalPortion(), tr.principalPortion) //
                        && Objects.equals(item.getInterestPortion(), tr.interestPortion) //
                        && Objects.equals(item.getFeeChargesPortion(), tr.feePortion) //
                        && Objects.equals(item.getPenaltyChargesPortion(), tr.penaltyPortion) //
                        && Objects.equals(item.getOverpaymentPortion(), tr.overpaymentPortion) //
                        && Objects.equals(item.getUnrecognizedIncomePortion(), tr.unrecognizedPortion) //
                );
                Assertions.assertTrue(found, "Required transaction not found: " + tr);
            });
        }
    }

    protected void placeHardLockOnLoan(Long loanId) {
        loanAccountLockHelper.placeSoftLockOnLoanAccount(loanId.intValue(), "LOAN_COB_CHUNK_PROCESSING");
    }

    protected void executeInlineCOB(Long loanId) {
        inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
    }

    protected void reAgeLoan(Long loanId, String frequencyType, int frequencyNumber, String startDate, Integer numberOfInstallments) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setDateFormat(DATETIME_PATTERN);
        request.setLocale("en");
        request.setFrequencyType(frequencyType);
        request.setFrequencyNumber(frequencyNumber);
        request.setStartDate(startDate);
        request.setNumberOfInstallments(numberOfInstallments);
        loanTransactionHelper.reAge(loanId, request);
    }

    protected void reAmortizeLoan(Long loanId) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setDateFormat(DATETIME_PATTERN);
        request.setLocale("en");
        loanTransactionHelper.reAmortize(loanId, request);
    }

    protected void undoReAgeLoan(Long loanId) {
        loanTransactionHelper.undoReAge(loanId, new PostLoansLoanIdTransactionsRequest());
    }

    protected void undoReAmortizeLoan(Long loanId) {
        loanTransactionHelper.undoReAmortize(loanId, new PostLoansLoanIdTransactionsRequest());
    }

    protected void verifyLastClosedBusinessDate(Long loanId, String lastClosedBusinessDate) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
        assertNotNull(loanDetails.getLastClosedBusinessDate());
        Assertions.assertEquals(lastClosedBusinessDate, loanDetails.getLastClosedBusinessDate().format(dateTimeFormatter));
    }

    protected void disburseLoan(Long loanId, BigDecimal amount, String date) {
        loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date).dateFormat(DATETIME_PATTERN)
                .transactionAmount(amount).locale("en"));
    }

    protected void undoDisbursement(Integer loanId) {
        loanTransactionHelper.undoDisbursal(loanId);
    }

    protected void verifyJournalEntries(Long loanId, Journal... entries) {
        GetJournalEntriesTransactionIdResponse journalEntriesForLoan = journalEntryHelper.getJournalEntriesForLoan(loanId);
        Assertions.assertEquals(entries.length, journalEntriesForLoan.getPageItems().size());
        Arrays.stream(entries).forEach(journalEntry -> {
            boolean found = journalEntriesForLoan.getPageItems().stream()
                    .anyMatch(item -> Objects.equals(item.getAmount(), journalEntry.amount)
                            && Objects.equals(item.getGlAccountId(), journalEntry.account.getAccountID().longValue())
                            && Objects.requireNonNull(item.getEntryType()).getValue().equals(journalEntry.type));
            Assertions.assertTrue(found, "Required journal entry not found: " + journalEntry);
        });
    }

    protected void verifyTRJournalEntries(Long transactionId, Journal... entries) {
        GetJournalEntriesTransactionIdResponse journalEntriesForLoan = journalEntryHelper.getJournalEntries("L" + transactionId.toString());
        Assertions.assertEquals(entries.length, journalEntriesForLoan.getPageItems().size());
        Arrays.stream(entries).forEach(journalEntry -> {
            boolean found = journalEntriesForLoan.getPageItems().stream()
                    .anyMatch(item -> Objects.equals(item.getAmount(), journalEntry.amount)
                            && Objects.equals(item.getGlAccountId(), journalEntry.account.getAccountID().longValue())
                            && Objects.requireNonNull(item.getEntryType()).getValue().equals(journalEntry.type));
            Assertions.assertTrue(found, "Required journal entry not found: " + journalEntry);
        });
    }

    protected Long addCharge(Long loanId, boolean isPenalty, double amount, String dueDate) {
        Integer chargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(amount), isPenalty));
        assertNotNull(chargeId);
        Integer loanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId.intValue(),
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(chargeId), dueDate, String.valueOf(amount)));
        assertNotNull(loanChargeId);
        return loanChargeId.longValue();
    }

    protected void verifyRepaymentSchedule(Long loanId, Installment... installments) {
        GetLoansLoanIdResponse loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        assertNotNull(loanResponse.getRepaymentSchedule());
        assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());
        Assertions.assertEquals(installments.length, loanResponse.getRepaymentSchedule().getPeriods().size(),
                "Expected installments are not matching with the installments configured on the loan");

        int installmentNumber = 0;
        for (int i = 1; i < installments.length; i++) {
            GetLoansLoanIdRepaymentPeriod period = loanResponse.getRepaymentSchedule().getPeriods().get(i);
            Double principalDue = period.getPrincipalDue();
            Double amount = installments[i].principalAmount;

            if (installments[i].completed == null) { // this is for the disbursement
                Assertions.assertEquals(amount, period.getPrincipalLoanBalanceOutstanding(),
                        "%d. installment's principal due is different, expected: %.2f, actual: %.2f".formatted(i, amount,
                                period.getPrincipalLoanBalanceOutstanding()));
            } else {
                Assertions.assertEquals(amount, principalDue,
                        "%d. installment's principal due is different, expected: %.2f, actual: %.2f".formatted(i, amount, principalDue));

                Double interestAmount = installments[i].interestAmount;
                Double interestDue = period.getInterestDue();
                if (interestAmount != null) {
                    Assertions.assertEquals(interestAmount, interestDue,
                            "%d. installment's interest due is different, expected: %.2f, actual: %.2f".formatted(i, interestAmount,
                                    interestDue));
                }

                Double feeAmount = installments[i].feeAmount;
                Double feeDue = period.getFeeChargesDue();
                if (feeAmount != null) {
                    Assertions.assertEquals(feeAmount, feeDue,
                            "%d. installment's fee charges due is different, expected: %.2f, actual: %.2f".formatted(i, feeAmount, feeDue));
                }

                Double penaltyAmount = installments[i].penaltyAmount;
                Double penaltyDue = period.getPenaltyChargesDue();
                if (penaltyAmount != null) {
                    Assertions.assertEquals(penaltyAmount, penaltyDue,
                            "%d. installment's penalty charges due is different, expected: %.2f, actual: %.2f".formatted(i, penaltyAmount,
                                    penaltyDue));
                }

                Double outstandingAmount = installments[i].totalOutstandingAmount;
                Double totalOutstanding = period.getTotalOutstandingForPeriod();
                if (outstandingAmount != null) {
                    Assertions.assertEquals(outstandingAmount, totalOutstanding,
                            "%d. installment's total outstanding is different, expected: %.2f, actual: %.2f".formatted(i, outstandingAmount,
                                    totalOutstanding));
                }

                Double outstandingPrincipalExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.principalOutstanding
                        : null;
                Double outstandingPrincipal = period.getPrincipalOutstanding();
                if (outstandingPrincipalExpected != null) {
                    Assertions.assertEquals(outstandingPrincipalExpected, outstandingPrincipal,
                            "%d. installment's outstanding principal is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingPrincipalExpected, outstandingPrincipal));
                }

                Double outstandingFeeExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.feeOutstanding
                        : null;
                Double outstandingFee = period.getFeeChargesOutstanding();
                if (outstandingFeeExpected != null) {
                    Assertions.assertEquals(outstandingFeeExpected, outstandingFee,
                            "%d. installment's outstanding fee is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingFeeExpected, outstandingFee));
                }

                Double outstandingPenaltyExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.penaltyOutstanding
                        : null;
                Double outstandingPenalty = period.getPenaltyChargesOutstanding();
                if (outstandingPenaltyExpected != null) {
                    Assertions.assertEquals(outstandingPenaltyExpected, outstandingPenalty,
                            "%d. installment's outstanding penalty is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingPenaltyExpected, outstandingPenalty));
                }

                Double outstandingTotalExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.totalOutstanding
                        : null;
                Double outstandingTotal = period.getTotalOutstandingForPeriod();
                if (outstandingTotalExpected != null) {
                    Assertions.assertEquals(outstandingTotalExpected, outstandingTotal,
                            "%d. installment's total outstanding is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingTotalExpected, outstandingTotal));
                }

                Double loanBalanceExpected = installments[i].loanBalance;
                Double loanBalance = period.getPrincipalLoanBalanceOutstanding();
                if (loanBalanceExpected != null) {
                    Assertions.assertEquals(loanBalanceExpected, loanBalance,
                            "%d. installment's loan balance is different, expected: %.2f, actual: %.2f".formatted(i, loanBalanceExpected,
                                    loanBalance));
                }
                installmentNumber++;
                Assertions.assertEquals(installmentNumber, period.getPeriod());
            }
            Assertions.assertEquals(installments[i].completed, period.getComplete());
            Assertions.assertEquals(LocalDate.parse(installments[i].dueDate, dateTimeFormatter), period.getDueDate());
        }
    }

    protected void runAt(String date, Runnable runnable) {
        try {
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 42, true);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, TRUE);
            businessDateHelper.updateBusinessDate(
                    new BusinessDateRequest().type(BUSINESS_DATE.getName()).date(date).dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.run();
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, FALSE);
            GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, 42, false);
        }
    }

    protected void runAsNonByPass(Runnable runnable) {
        RequestSpecificationImpl requestSpecImpl = (RequestSpecificationImpl) requestSpec;
        try {
            requestSpecImpl.replaceHeader("Authorization", "Basic " + nonByPassUserAuthKey);
            runnable.run();
        } finally {
            requestSpecImpl.replaceHeader("Authorization", "Basic " + fullAdminAuthKey);
        }
    }

    protected PostLoansRequest applyLoanRequest(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount,
            int numberOfRepayments) {
        return applyLoanRequest(clientId, loanProductId, loanDisbursementDate, amount, numberOfRepayments, null);
    }

    protected PostLoansRequest applyLoanRequest(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount,
            int numberOfRepayments, Consumer<PostLoansRequest> customizer) {

        PostLoansRequest postLoansRequest = new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .expectedDisbursementDate(loanDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY)
                .locale("en").submittedOnDate(loanDisbursementDate).amortizationType(1).interestRatePerPeriod(BigDecimal.ZERO)
                .interestCalculationPeriodType(1).interestType(0).repaymentEvery(30).repaymentFrequencyType(0)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(numberOfRepayments * 30).loanTermFrequencyType(0)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(amount)).principal(BigDecimal.valueOf(amount)).loanType("individual");
        if (customizer != null) {
            customizer.accept(postLoansRequest);
        }
        return postLoansRequest;
    }

    protected PostLoansLoanIdRequest approveLoanRequest(Double amount, String approvalDate) {
        return new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(amount)).dateFormat(DATETIME_PATTERN)
                .approvedOnDate(approvalDate).locale("en");
    }

    protected Long applyAndApproveLoan(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount,
            int numberOfRepayments) {
        return applyAndApproveLoan(clientId, loanProductId, loanDisbursementDate, amount, numberOfRepayments, null);
    }

    protected Long applyAndApproveLoan(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount,
            int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansResponse postLoansResponse = loanTransactionHelper
                .applyLoan(applyLoanRequest(clientId, loanProductId, loanDisbursementDate, amount, numberOfRepayments, customizer));

        PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                approveLoanRequest(amount, loanDisbursementDate));

        return approvedLoanResult.getLoanId();
    }

    protected Long applyAndApproveLoan(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount) {
        return applyAndApproveLoan(clientId, loanProductId, loanDisbursementDate, amount, 1);
    }

    protected Long addRepaymentForLoan(Long loanId, Double amount, String date) {
        String firstRepaymentUUID = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanRepayment(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(date).locale("en")
                        .transactionAmount(amount).externalId(firstRepaymentUUID));
        return response.getResourceId();
    }

    protected Long chargeOffLoan(Long loanId, String date) {
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();

        PostLoansLoanIdTransactionsResponse chargeOffTransaction = this.loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate(date).locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));
        return chargeOffTransaction.getResourceId();
    }

    protected void changeLoanFraudState(Long loanId, boolean fraudState) {
        String payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", fraudState ? "true" : "false");
        PutLoansLoanIdResponse response = loanTransactionHelper.modifyLoanCommand(Math.toIntExact(loanId), "markAsFraud", payload,
                responseSpec);
        assertNotNull(response);
    }

    protected Long addChargebackForLoan(Long loanId, Long transactionId, Double amount) {
        PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.chargebackLoanTransaction(loanId, transactionId,
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(amount).paymentTypeId(1L));
        return response.getResourceId();
    }

    protected PostChargesResponse createCharge(Double amount) {
        String payload = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, amount.toString(), false);
        return ChargesHelper.createLoanCharge(requestSpec, responseSpec, payload);
    }

    protected PostLoansLoanIdChargesResponse addLoanCharge(Long loanId, Long chargeId, String date, Double amount) {
        String payload = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), date, amount.toString());
        return loanTransactionHelper.addChargeForLoan(loanId.intValue(), payload, responseSpec);
    }

    protected void waiveLoanCharge(Long loanId, Long chargeId, Integer installmentNumber) {
        String payload = LoanTransactionHelper.getWaiveChargeJSON(installmentNumber.toString());
        loanTransactionHelper.waiveChargesForLoan(loanId.intValue(), chargeId.intValue(), payload);
    }

    protected void updateBusinessDate(String date) {
        businessDateHelper.updateBusinessDate(
                new BusinessDateRequest().type(BUSINESS_DATE.getName()).date(date).dateFormat(DATETIME_PATTERN).locale("en"));
    }

    protected Long getTransactionId(Long loanId, String type, String date) {
        GetLoansLoanIdResponse loan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        return loan.getTransactions().stream().filter(tr -> Objects.equals(tr.getType().getValue(), type)
                && Objects.equals(tr.getDate(), LocalDate.parse(date, dateTimeFormatter))).findAny().orElseThrow().getId();
    }

    protected Journal journalEntry(double amount, Account account, String type) {
        return new Journal(amount, account, type);
    }

    protected Journal debit(Account account, double amount) {
        return new Journal(amount, account, "DEBIT");
    }

    protected Journal credit(Account account, double amount) {
        return new Journal(amount, account, "CREDIT");
    }

    protected Transaction transaction(double principalAmount, String type, String date) {
        return new Transaction(principalAmount, type, date, null);
    }

    protected Transaction reversedTransaction(double principalAmount, String type, String date) {
        return new Transaction(principalAmount, type, date, true);
    }

    protected TransactionExt transaction(double amount, String type, String date, double outstandingPrincipal, double principalPortion,
            double interestPortion, double feePortion, double penaltyPortion, double unrecognizedIncomePortion, double overpaymentPortion) {
        return new TransactionExt(amount, type, date, outstandingPrincipal, principalPortion, interestPortion, feePortion, penaltyPortion,
                unrecognizedIncomePortion, overpaymentPortion, false);
    }

    protected TransactionExt transaction(double amount, String type, String date, double outstandingPrincipal, double principalPortion,
            double interestPortion, double feePortion, double penaltyPortion, double unrecognizedIncomePortion, double overpaymentPortion,
            boolean reversed) {
        return new TransactionExt(amount, type, date, outstandingPrincipal, principalPortion, interestPortion, feePortion, penaltyPortion,
                unrecognizedIncomePortion, overpaymentPortion, reversed);
    }

    protected Installment installment(double principalAmount, Boolean completed, String dueDate) {
        return new Installment(principalAmount, null, null, null, null, completed, dueDate, null, null);
    }

    protected Installment installment(double principalAmount, double interestAmount, double totalOutstandingAmount, Boolean completed,
            String dueDate) {
        return new Installment(principalAmount, interestAmount, null, null, totalOutstandingAmount, completed, dueDate, null, null);
    }

    protected Installment installment(double principalAmount, double interestAmount, double feeAmount, double totalOutstandingAmount,
            Boolean completed, String dueDate) {
        return new Installment(principalAmount, interestAmount, feeAmount, null, totalOutstandingAmount, completed, dueDate, null, null);
    }

    protected Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            double totalOutstandingAmount, Boolean completed, String dueDate) {
        return new Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, totalOutstandingAmount, completed, dueDate, null,
                null);
    }

    protected Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            OutstandingAmounts outstandingAmounts, Boolean completed, String dueDate) {
        return new Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, null, completed, dueDate, outstandingAmounts,
                null);
    }

    protected Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            double totalOutstanding, Boolean completed, String dueDate, double loanBalance) {
        return new Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, totalOutstanding, completed, dueDate, null,
                loanBalance);
    }

    protected OutstandingAmounts outstanding(double principal, double fee, double penalty, double total) {
        return new OutstandingAmounts(principal, fee, penalty, total);
    }

    protected BatchRequestBuilder batchRequest() {
        return new BatchRequestBuilder(requestSpec, responseSpec);
    }

    protected void validateLoanSummaryBalances(GetLoansLoanIdResponse loanDetails, Double totalOutstanding, Double totalRepayment,
            Double principalOutstanding, Double principalPaid, Double totalOverpaid) {
        assertEquals(totalOutstanding, loanDetails.getSummary().getTotalOutstanding());
        assertEquals(totalRepayment, loanDetails.getSummary().getTotalRepayment());
        assertEquals(principalOutstanding, loanDetails.getSummary().getPrincipalOutstanding());
        assertEquals(principalPaid, loanDetails.getSummary().getPrincipalPaid());
        assertEquals(totalOverpaid, loanDetails.getTotalOverpaid());
    }

    protected static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(dueDate, period.getDueDate());
        assertEquals(principalDue, period.getPrincipalDue());
        assertEquals(principalPaid, period.getPrincipalPaid());
        assertEquals(principalOutstanding, period.getPrincipalOutstanding());
        assertEquals(paidInAdvance, period.getTotalPaidInAdvanceForPeriod());
        assertEquals(paidLate, period.getTotalPaidLateForPeriod());
    }

    protected static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, double principalDue,
            double principalPaid, double principalOutstanding, double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(principalDue, period.getPrincipalDue());
        assertEquals(principalPaid, period.getPrincipalPaid());
        assertEquals(principalOutstanding, period.getPrincipalOutstanding());
        assertEquals(paidInAdvance, period.getTotalPaidInAdvanceForPeriod());
        assertEquals(paidLate, period.getTotalPaidLateForPeriod());
    }

    protected static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double feeDue, double feePaid, double feeOutstanding, double penaltyDue,
            double penaltyPaid, double penaltyOutstanding, double interestDue, double interestPaid, double interestOutstanding,
            double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(dueDate, period.getDueDate());
        assertEquals(principalDue, period.getPrincipalDue());
        assertEquals(principalPaid, period.getPrincipalPaid());
        assertEquals(principalOutstanding, period.getPrincipalOutstanding());
        assertEquals(feeDue, period.getFeeChargesDue());
        assertEquals(feePaid, period.getFeeChargesPaid());
        assertEquals(feeOutstanding, period.getFeeChargesOutstanding());
        assertEquals(penaltyDue, period.getPenaltyChargesDue());
        assertEquals(penaltyPaid, period.getPenaltyChargesPaid());
        assertEquals(penaltyOutstanding, period.getPenaltyChargesOutstanding());
        assertEquals(interestDue, period.getInterestDue());
        assertEquals(interestPaid, period.getInterestPaid());
        assertEquals(interestOutstanding, period.getInterestOutstanding());
        assertEquals(paidInAdvance, period.getTotalPaidInAdvanceForPeriod());
        assertEquals(paidLate, period.getTotalPaidLateForPeriod());
    }

    protected void checkMaturityDates(long loanId, LocalDate expectedMaturityDate, LocalDate actualMaturityDate) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

        assertEquals(expectedMaturityDate, loanDetails.getTimeline().getExpectedMaturityDate());
        assertEquals(actualMaturityDate, loanDetails.getTimeline().getActualMaturityDate());
    }

    protected void verifyLoanStatus(long loanId, LoanStatus loanStatus) {
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

        assertEquals(loanStatus.getCode(), loanDetails.getStatus().getCode());
    }

    @RequiredArgsConstructor
    public static class BatchRequestBuilder {

        private final RequestSpecification requestSpec;
        private final ResponseSpecification responseSpec;
        private List<BatchRequest> requests = new ArrayList<>();

        public BatchRequestBuilder rescheduleLoan(Long requestId, Long loanId, String submittedOnDate, String rescheduleFromDate,
                String adjustedDueDate) {
            BatchRequest bRequest = new BatchRequest();
            bRequest.setRequestId(requestId);
            bRequest.setRelativeUrl("rescheduleloans");
            bRequest.setMethod("POST");

            bRequest.setBody("""
                        {
                            "loanId": %d,
                            "rescheduleFromDate": "%s",
                            "rescheduleReasonId": 1,
                            "submittedOnDate": "%s",
                            "rescheduleReasonComment": "",
                            "adjustedDueDate": "%s",
                            "graceOnPrincipal": "",
                            "graceOnInterest": "",
                            "extraTerms": "",
                            "newInterestRate": "",
                            "dateFormat": "%s",
                            "locale": "en"
                        }
                    """.formatted(loanId, rescheduleFromDate, submittedOnDate, adjustedDueDate, DATETIME_PATTERN));

            requests.add(bRequest);
            return this;
        }

        public BatchRequestBuilder approveRescheduleLoan(Long requestId, Long rescheduleBatchRequestId, String approvedOnDate) {
            BatchRequest bRequest = new BatchRequest();
            bRequest.setRequestId(requestId);
            bRequest.setRelativeUrl("rescheduleloans/$.resourceId?command=approve");
            bRequest.setMethod("POST");
            bRequest.setReference(rescheduleBatchRequestId);

            bRequest.setBody("""
                        {
                            "approvedOnDate": "%s",
                            "dateFormat": "%s",
                            "locale": "en"
                        }
                    """.formatted(approvedOnDate, DATETIME_PATTERN));

            requests.add(bRequest);
            return this;
        }

        public List<BatchResponse> executeEnclosingTransaction() {
            return BatchHelper.postBatchRequestsWithEnclosingTransaction(requestSpec, responseSpec, BatchHelper.toJsonString(requests));
        }

        public ErrorResponse executeEnclosingTransactionError(ResponseSpecification responseSpec) {
            return BatchHelper.postBatchRequestsWithoutEnclosingTransactionError(requestSpec, responseSpec,
                    BatchHelper.toJsonString(requests));
        }
    }

    @ToString
    @AllArgsConstructor
    public static class Transaction {

        Double amount;
        String type;
        String date;
        Boolean reversed;
    }

    @ToString
    @AllArgsConstructor
    public static class TransactionExt {

        Double amount;
        String type;
        String date;
        Double outstandingPrincipal;
        Double principalPortion;
        Double interestPortion;
        Double feePortion;
        Double penaltyPortion;
        Double unrecognizedPortion;
        Double overpaymentPortion;
        Boolean reversed;
    }

    @ToString
    @AllArgsConstructor
    public static class Journal {

        Double amount;
        Account account;
        String type;
    }

    @ToString
    @AllArgsConstructor
    public static class Installment {

        Double principalAmount;
        Double interestAmount;
        Double feeAmount;
        Double penaltyAmount;
        Double totalOutstandingAmount;
        Boolean completed;
        String dueDate;
        OutstandingAmounts outstandingAmounts;
        Double loanBalance;
    }

    @AllArgsConstructor
    @ToString
    public static class OutstandingAmounts {

        Double principalOutstanding;
        Double feeOutstanding;
        Double penaltyOutstanding;
        Double totalOutstanding;
    }

    public static class AmortizationType {

        public static final Integer EQUAL_INSTALLMENTS = 1;
    }

    public static class InterestType {

        public static final Integer DECLINING_BALANCE = 0;
        public static final Integer FLAT = 1;
    }

    public static class RepaymentFrequencyType {

        public static final Integer MONTHS = 2;
        public static final String MONTHS_STRING = "MONTHS";
        public static final Integer DAYS = 0;
        public static final String DAYS_STRING = "DAYS";
    }

    public static class InterestCalculationPeriodType {

        public static final Integer SAME_AS_REPAYMENT_PERIOD = 1;
    }

    public static class InterestRateFrequencyType {

        public static final Integer MONTHS = 2;
        public static final Integer YEARS = 3;
    }

}
