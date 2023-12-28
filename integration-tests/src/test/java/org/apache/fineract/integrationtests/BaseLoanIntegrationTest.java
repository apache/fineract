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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public abstract class BaseLoanIntegrationTest {

    static {
        Utils.initializeRESTAssured();
    }

    protected static final String DATETIME_PATTERN = "dd MMMM yyyy";

    protected final ResponseSpecification responseSpec = createResponseSpecification(200);
    protected final RequestSpecification requestSpec = createRequestSpecification();

    protected final AccountHelper accountHelper = new AccountHelper(requestSpec, responseSpec);
    protected final LoanTransactionHelper loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
    protected final LoanProductHelper loanProductHelper = new LoanProductHelper();
    protected JournalEntryHelper journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    protected ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);

    protected BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    protected DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    // asset
    protected final Account loansReceivableAccount = accountHelper.createAssetAccount();
    protected final Account interestFeeReceivableAccount = accountHelper.createAssetAccount();
    protected final Account suspenseAccount = accountHelper.createAssetAccount();
    // liability
    protected final Account suspenseClearingAccount = accountHelper.createLiabilityAccount();
    protected final Account overpaymentAccount = accountHelper.createLiabilityAccount();
    // income
    protected final Account interestIncomeAccount = accountHelper.createIncomeAccount();
    protected final Account feeIncomeAccount = accountHelper.createIncomeAccount();
    protected final Account feeChargeOffAccount = accountHelper.createIncomeAccount();
    protected final Account recoveriesAccount = accountHelper.createIncomeAccount();
    protected final Account interestIncomeChargeOffAccount = accountHelper.createIncomeAccount();
    // expense
    protected final Account creditLossBadDebtAccount = accountHelper.createExpenseAccount();
    protected final Account creditLossBadDebtFraudAccount = accountHelper.createExpenseAccount();
    protected final Account writtenOffAccount = accountHelper.createExpenseAccount();
    protected final Account goodwillExpenseAccount = accountHelper.createExpenseAccount();

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
                .fundSourceAccountId(suspenseClearingAccount.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(interestFeeReceivableAccount.getAccountID().longValue())//
                .dateFormat(DATETIME_PATTERN)//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(creditLossBadDebtAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(creditLossBadDebtFraudAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue());
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

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private AdvancedPaymentData createDefaultPaymentAllocation(String futureInstallmentAllocationRule) {
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

    private static RequestSpecification createRequestSpecification() {
        RequestSpecification request = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        request.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        request.header("Fineract-Platform-TenantId", "default");
        return request;
    }

    private static ResponseSpecification createResponseSpecification(int statusCode) {
        return new ResponseSpecBuilder().expectStatusCode(statusCode).build();
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
                boolean found = loanDetails.getTransactions().stream()
                        .anyMatch(item -> Objects.equals(item.getAmount(), tr.amount) && Objects.equals(item.getType().getValue(), tr.type)
                                && Objects.equals(item.getDate(), LocalDate.parse(tr.date, dateTimeFormatter)));
                Assertions.assertTrue(found, "Required transaction  not found: " + tr);
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
                        && Objects.equals(item.getOutstandingLoanBalance(), tr.outstandingAmount) //
                        && Objects.equals(item.getPrincipalPortion(), tr.principalPortion) //
                        && Objects.equals(item.getInterestPortion(), tr.interestPortion) //
                        && Objects.equals(item.getFeeChargesPortion(), tr.feePortion) //
                        && Objects.equals(item.getPenaltyChargesPortion(), tr.penaltyPortion) //
                        && Objects.equals(item.getUnrecognizedIncomePortion(), tr.unrecognizedPortion) //
                );
                Assertions.assertTrue(found, "Required transaction  not found: " + tr);
            });
        }
    }

    protected void disburseLoan(Long loanId, BigDecimal amount, String date) {
        loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date).dateFormat(DATETIME_PATTERN)
                .transactionAmount(amount).locale("en"));
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

    protected Long addCharge(Long loanId, boolean isPenalty, double amount, String dueDate) {
        Integer chargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(amount), isPenalty));
        Assertions.assertNotNull(chargeId);
        Integer loanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId.intValue(),
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(chargeId), dueDate, String.valueOf(amount)));
        Assertions.assertNotNull(loanChargeId);
        return loanChargeId.longValue();
    }

    protected void verifyRepaymentSchedule(Long loanId, Installment... installments) {
        GetLoansLoanIdResponse loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        Assertions.assertNotNull(loanResponse.getRepaymentSchedule());
        Assertions.assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());
        Assertions.assertEquals(installments.length, loanResponse.getRepaymentSchedule().getPeriods().size(),
                "Expected installments are not matching with the installments configured on the loan");

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

                Double outstandingAmount = installments[i].totalOutstandingAmount;
                Double totalOutstanding = period.getTotalOutstandingForPeriod();
                if (outstandingAmount != null) {
                    Assertions.assertEquals(outstandingAmount, totalOutstanding,
                            "%d. installment's total outstanding is different, expected: %.2f, actual: %.2f".formatted(i, outstandingAmount,
                                    totalOutstanding));
                }

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

    protected void addRepaymentForLoan(Long loanId, Double amount, String date) {
        String firstRepaymentUUID = UUID.randomUUID().toString();
        loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                .transactionDate(date).locale("en").transactionAmount(amount).externalId(firstRepaymentUUID));
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

    protected Journal journalEntry(double principalAmount, Account account, String type) {
        return new Journal(principalAmount, account, type);
    }

    protected Transaction transaction(double principalAmount, String type, String date) {
        return new Transaction(principalAmount, type, date);
    }

    protected TransactionExt transaction(double amount, String type, String date, double outstandingAmount, double principalPortion,
            double interestPortion, double feePortion, double penaltyPortion, double unrecognizedIncomePortion) {
        return new TransactionExt(amount, type, date, outstandingAmount, principalPortion, interestPortion, feePortion, penaltyPortion,
                unrecognizedIncomePortion);
    }

    protected Installment installment(double principalAmount, Boolean completed, String dueDate) {
        return new Installment(principalAmount, null, null, null, completed, dueDate);
    }

    protected Installment installment(double principalAmount, double interestAmount, double totalOutstandingAmount, Boolean completed,
            String dueDate) {
        return new Installment(principalAmount, interestAmount, null, totalOutstandingAmount, completed, dueDate);
    }

    protected Installment installment(double principalAmount, double interestAmount, double feeAmount, double totalOutstandingAmount,
            Boolean completed, String dueDate) {
        return new Installment(principalAmount, interestAmount, feeAmount, totalOutstandingAmount, completed, dueDate);
    }

    @ToString
    @AllArgsConstructor
    public static class Transaction {

        Double amount;
        String type;
        String date;
    }

    @ToString
    @AllArgsConstructor
    public static class TransactionExt {

        Double amount;
        String type;
        String date;
        Double outstandingAmount;
        Double principalPortion;
        Double interestPortion;
        Double feePortion;
        Double penaltyPortion;
        Double unrecognizedPortion;
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
        Double totalOutstandingAmount;
        Boolean completed;
        String dueDate;
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
    }

    public static class InterestCalculationPeriodType {

        public static final Integer SAME_AS_REPAYMENT_PERIOD = 1;
    }

    public static class InterestRateFrequencyType {

        public static final Integer MONTHS = 2;
        public static final Integer YEARS = 3;
    }

}
