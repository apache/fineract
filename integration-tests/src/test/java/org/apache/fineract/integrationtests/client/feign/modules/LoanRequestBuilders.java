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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;

public final class LoanRequestBuilders {

    /** The JSON disburse builders this replaced always sent this note; keep the payload unchanged. */
    private static final String DISBURSE_NOTE = "DISBURSE NOTE";

    private LoanRequestBuilders() {}

    public static PostLoansRequest applyLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(BigDecimal.valueOf(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansRequest applyCumulativeLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments, Double interestRate) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(BigDecimal.valueOf(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(interestRate))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .transactionProcessingStrategyCode("due-penalty-fee-interest-principal-in-advance-principal-penalty-fee-interest-strategy")//
                .loanType("individual")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansRequest applyProgressiveLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments, Double interestRate) {
        return applyCumulativeLoan(clientId, productId, submittedOnDate, principal, numberOfRepayments, interestRate)
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
    }

    public static PostLoansRequest applyProgressiveLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments) {
        return applyLoan(clientId, productId, submittedOnDate, principal, numberOfRepayments)
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
    }

    public static PostLoansLoanIdRequest approveLoan(Double approvedAmount, String approvedOnDate) {
        return new PostLoansLoanIdRequest()//
                .approvedLoanAmount(BigDecimal.valueOf(approvedAmount))//
                .approvedOnDate(approvedOnDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest approveLoan(Double approvedAmount, String approvedOnDate, String expectedDisbursementDate) {
        return approveLoan(approvedAmount, approvedOnDate)//
                .expectedDisbursementDate(expectedDisbursementDate);
    }

    public static PostLoansLoanIdRequest disburseLoan(Double disbursedAmount, String disbursedOnDate) {
        return new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursedOnDate)//
                .transactionAmount(BigDecimal.valueOf(disbursedAmount))//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    /**
     * The equal-installment declining-balance application that LoanApplicationTestBuilder produced, including the
     * fields its build() always emitted but no call site named: maxOutstandingLoanBalance, an empty collateral list,
     * the default strategy and the en_GB locale.
     */
    public static PostLoansRequest legacyIndividualApplication(Long clientId, Long productId, String principal, int repayments,
            BigDecimal interestRatePerPeriod, String date) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                // the JSON builder sent grouped amounts like "12,000.00" for the server to parse under en_GB
                .principal(new BigDecimal(principal.replace(",", "")))//
                .loanTermFrequency(repayments)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(repayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(interestRatePerPeriod)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)//
                .expectedDisbursementDate(date)//
                .submittedOnDate(date)//
                .loanType("individual")//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    /**
     * The days-based equal-principal application the JSON builder produced, with the same invisible build() defaults as
     * {@link #legacyIndividualApplication}. Interest type defaults to flat; override it on the returned request.
     */
    public static PostLoansRequest legacyDaysBasedApplication(Long clientId, Long productId, String principal, int termDays, int repayments,
            int repaymentEveryDays, String expectedDisbursementDate, String submittedOnDate) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .principal(new BigDecimal(principal.replace(",", "")))//
                .loanTermFrequency(termDays)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .numberOfRepayments(repayments)//
                .repaymentEvery(repaymentEveryDays)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .submittedOnDate(submittedOnDate)//
                .loanType("individual")//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest disburseLoanWithRepaymentReschedule(String disbursedOnDate, String adjustRepaymentDate) {
        return new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursedOnDate)//
                .adjustRepaymentDate(adjustRepaymentDate)//
                .note(DISBURSE_NOTE)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest disburseLoanWithNetDisbursalAmount(String disbursedOnDate, BigDecimal netDisbursalAmount) {
        return new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursedOnDate)//
                .netDisbursalAmount(netDisbursalAmount)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansDisbursementData applyTrancheDetail(String expectedDisbursementDate, double principal) {
        return new PostLoansDisbursementData()//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principal(BigDecimal.valueOf(principal));
    }

    public static PostLoansLoanIdDisbursementData approveTrancheDetail(String expectedDisbursementDate, double principal) {
        return new PostLoansLoanIdDisbursementData()//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principal(BigDecimal.valueOf(principal));
    }

    public static PostLoansLoanIdRequest approveLoanWithTranches(Double approvedAmount, String approvedOnDate,
            String expectedDisbursementDate, List<PostLoansLoanIdDisbursementData> tranches) {
        return approveLoan(approvedAmount, approvedOnDate, expectedDisbursementDate)//
                .disbursementData(tranches);
    }

    public static PostLoansLoanIdTransactionsRequest repayLoan(Double amount, String transactionDate) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setTransactionDate(transactionDate);
        request.setTransactionAmount(amount);
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    public static PostLoansLoanIdTransactionsRequest makeWaiver(Double amount, String transactionDate) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setTransactionDate(transactionDate);
        request.setTransactionAmount(amount);
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    public static PostLoansLoanIdTransactionsRequest chargeOff(String transactionDate) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setTransactionDate(transactionDate);
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    public static PostLoansLoanIdTransactionsRequest addChargeback(Long transactionId, Double amount, String transactionDate) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setTransactionDate(transactionDate);
        request.setTransactionAmount(amount);
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    public static PostLoansLoanIdTransactionsRequest waiveInterest(Double amount, String transactionDate) {
        return makeWaiver(amount, transactionDate);
    }

    /**
     * Creates a reschedule request that shifts a due date. Uses rescheduleReasonId=1 (default seed data).
     */
    public static PostCreateRescheduleLoansRequest rescheduleRequest(Long loanId, String submittedOnDate, String rescheduleFromDate,
            String adjustedDueDate) {
        return rescheduleRequest(loanId, submittedOnDate, rescheduleFromDate, adjustedDueDate, 1L);
    }

    public static PostCreateRescheduleLoansRequest rescheduleRequest(Long loanId, String submittedOnDate, String rescheduleFromDate,
            String adjustedDueDate, Long rescheduleReasonId) {
        return new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .submittedOnDate(submittedOnDate)//
                .rescheduleFromDate(rescheduleFromDate)//
                .adjustedDueDate(adjustedDueDate)//
                .rescheduleReasonId(rescheduleReasonId)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostUpdateRescheduleLoansRequest approveReschedule(String approvedOnDate) {
        return new PostUpdateRescheduleLoansRequest()//
                .approvedOnDate(approvedOnDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostCreateRescheduleLoansRequest rescheduleWithExtraTerms(Long loanId, String submittedOnDate, String rescheduleFromDate,
            Integer extraTerms) {
        return new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .submittedOnDate(submittedOnDate)//
                .rescheduleFromDate(rescheduleFromDate)//
                .extraTerms(extraTerms)//
                .rescheduleReasonId(1L)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostCreateRescheduleLoansRequest rescheduleWithRecalculateInterest(Long loanId, String submittedOnDate,
            String rescheduleFromDate, String adjustedDueDate) {
        return rescheduleRequest(loanId, submittedOnDate, rescheduleFromDate, adjustedDueDate).recalculateInterest(true);
    }

    public static PostCreateRescheduleLoansRequest rescheduleWithFixedEmiAndRecalculateInterest(Long loanId, String submittedOnDate,
            String rescheduleFromDate, String adjustedDueDate, BigDecimal emi, String emiEndDate) {
        return rescheduleWithRecalculateInterest(loanId, submittedOnDate, rescheduleFromDate, adjustedDueDate)//
                .emi(emi)//
                .endDate(emiEndDate);
    }

    /**
     * Creates a reAge request for non-interest-bearing loans (no interest handling needed).
     */
    public static PostLoansLoanIdTransactionsRequest reAge(String startDate, String frequencyType, Integer frequencyNumber,
            Integer numberOfInstallments) {
        return reAge(startDate, frequencyType, frequencyNumber, numberOfInstallments, null);
    }

    /**
     * Creates a reAge request with explicit interest handling.
     *
     * @param reAgeInterestHandling
     *            e.g. "EQUAL_AMORTIZATION_PAYABLE_INTEREST", "EQUAL_AMORTIZATION_FULL_INTEREST", or null for
     *            non-interest-bearing loans
     */
    public static PostLoansLoanIdTransactionsRequest reAge(String startDate, String frequencyType, Integer frequencyNumber,
            Integer numberOfInstallments, String reAgeInterestHandling) {
        return reAge(startDate, frequencyType, frequencyNumber, numberOfInstallments, reAgeInterestHandling, null);
    }

    public static PostLoansLoanIdTransactionsRequest reAge(String startDate, String frequencyType, Integer frequencyNumber,
            Integer numberOfInstallments, String reAgeInterestHandling, Double transactionAmount) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setStartDate(startDate);
        request.setFrequencyType(frequencyType);
        request.setFrequencyNumber(frequencyNumber);
        request.setNumberOfInstallments(numberOfInstallments);
        if (reAgeInterestHandling != null) {
            request.setReAgeInterestHandling(reAgeInterestHandling);
        }
        if (transactionAmount != null) {
            request.transactionAmount(transactionAmount);
        }
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    /**
     * Creates a DEFAULT payment allocation with NEXT_INSTALLMENT future rule. Suitable for most progressive loan
     * products using advanced-payment-allocation-strategy.
     */
    public static AdvancedPaymentData defaultPaymentAllocation() {
        return paymentAllocation("DEFAULT", "NEXT_INSTALLMENT");
    }

    /**
     * Creates a payment allocation for a specific transaction type and future installment allocation rule.
     *
     * @param transactionType
     *            e.g. "DEFAULT", "REPAYMENT", "DOWN_PAYMENT", "MERCHANT_ISSUED_REFUND"
     * @param futureInstallmentAllocationRule
     *            e.g. "NEXT_INSTALLMENT", "LAST_INSTALLMENT", "NEXT_LAST_INSTALLMENT"
     */
    public static AdvancedPaymentData paymentAllocation(String transactionType, String futureInstallmentAllocationRule) {
        AdvancedPaymentData data = new AdvancedPaymentData();
        data.setTransactionType(transactionType);
        data.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);
        data.setPaymentAllocationOrder(defaultPaymentAllocationOrder());
        return data;
    }

    public static AdvancedPaymentData paymentAllocation(String transactionType, String futureInstallmentAllocationRule,
            String... paymentAllocationRules) {
        AdvancedPaymentData data = new AdvancedPaymentData();
        data.setTransactionType(transactionType);
        data.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);
        AtomicInteger order = new AtomicInteger(1);
        List<PaymentAllocationOrder> orders = Stream.of(paymentAllocationRules)
                .map(rule -> new PaymentAllocationOrder().paymentAllocationRule(rule).order(order.getAndIncrement())).toList();
        data.setPaymentAllocationOrder(orders);
        return data;
    }

    public static List<PaymentAllocationOrder> defaultPaymentAllocationOrder() {
        return paymentAllocationOrder("PAST_DUE_PENALTY", "PAST_DUE_FEE", "PAST_DUE_PRINCIPAL", "PAST_DUE_INTEREST", "DUE_PENALTY",
                "DUE_FEE", "DUE_PRINCIPAL", "DUE_INTEREST", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_INTEREST");
    }

    public static AdvancedPaymentData repaymentPaymentAllocation() {
        AdvancedPaymentData data = new AdvancedPaymentData();
        data.setTransactionType("REPAYMENT");
        data.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");
        data.setPaymentAllocationOrder(paymentAllocationOrder("PAST_DUE_PENALTY", "PAST_DUE_FEE", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL",
                "DUE_PENALTY", "DUE_FEE", "DUE_INTEREST", "DUE_PRINCIPAL", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_INTEREST"));
        return data;
    }

    public static AdvancedPaymentData interestPaymentWaiverAllocation() {
        AdvancedPaymentData data = new AdvancedPaymentData();
        data.setTransactionType("INTEREST_PAYMENT_WAIVER");
        data.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");
        data.setPaymentAllocationOrder(paymentAllocationOrder("PAST_DUE_FEE", "PAST_DUE_PENALTY", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL",
                "DUE_PENALTY", "DUE_FEE", "DUE_INTEREST", "DUE_PRINCIPAL", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL",
                "IN_ADVANCE_INTEREST"));
        return data;
    }

    public static List<PaymentAllocationOrder> paymentAllocationOrder(String... paymentAllocationRules) {
        AtomicInteger order = new AtomicInteger(1);
        return Stream.of(paymentAllocationRules)
                .map(rule -> new PaymentAllocationOrder().paymentAllocationRule(rule).order(order.getAndIncrement())).toList();
    }

    /** Type-safe variant of {@link #paymentAllocationOrder(String...)}, ordered as given. */
    public static List<PaymentAllocationOrder> paymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        return paymentAllocationOrder(Stream.of(paymentAllocationTypes).map(PaymentAllocationType::name).toArray(String[]::new));
    }

    public static CreditAllocationData creditAllocation(String transactionType, String... creditAllocationRules) {
        CreditAllocationData data = new CreditAllocationData();
        data.setTransactionType(transactionType);
        AtomicInteger order = new AtomicInteger(1);
        List<CreditAllocationOrder> orders = Stream.of(creditAllocationRules)
                .map(rule -> new CreditAllocationOrder().creditAllocationRule(rule).order(order.getAndIncrement())).toList();
        data.setCreditAllocationOrder(orders);
        return data;
    }

    public static PostLoansLoanIdTransactionsRequest reAmortize(String reAmortizationInterestHandling) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest();
        request.setReAmortizationInterestHandling(reAmortizationInterestHandling);
        request.setLocale(LoanTestData.LOCALE);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        return request;
    }

    public static PostLoansLoanIdTransactionsRequest writeOff(String transactionDate) {
        return new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(transactionDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansRequest applyCumulativeLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            Double interestRate, int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = applyCumulativeLoan(clientId, productId, submittedOnDate, principal, numberOfRepayments, interestRate);
        if (customizer != null) {
            customizer.accept(request);
        }
        return request;
    }

    public static PostLoansLoanIdRequest rejectLoan(String rejectedOnDate) {
        return new PostLoansLoanIdRequest()//
                .rejectedOnDate(rejectedOnDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest withdrawLoan(String withdrawnOnDate) {
        return new PostLoansLoanIdRequest()//
                .withdrawnOnDate(withdrawnOnDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdTransactionsRequest closeLoan(String transactionDate) {
        return new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(transactionDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdTransactionsRequest forecloseLoan(String transactionDate) {
        return new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(transactionDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest assignLoanOfficer(Long toLoanOfficerId, String assignmentDate) {
        return new PostLoansLoanIdRequest()//
                .toLoanOfficerId(toLoanOfficerId)//
                .assignmentDate(assignmentDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest unassignLoanOfficer(String unassignedDate) {
        return new PostLoansLoanIdRequest()//
                .unassignedDate(unassignedDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdChargesRequest addLoanCharge(Long chargeId, double amount, String dueDate) {
        return new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .dueDate(dueDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdChargesRequest addLoanCharge(Long chargeId, double amount) {
        return new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdChargesChargeIdRequest waiveLoanCharge(double amount) {
        PostLoansLoanIdChargesChargeIdRequest request = new PostLoansLoanIdChargesChargeIdRequest();
        request.setAmount(amount);
        request.setLocale(LoanTestData.LOCALE);
        return request;
    }

    public static PostLoansLoanIdChargesChargeIdRequest payLoanCharge(double amount, String transactionDate) {
        PostLoansLoanIdChargesChargeIdRequest request = new PostLoansLoanIdChargesChargeIdRequest();
        request.setAmount(amount);
        request.setTransactionDate(transactionDate);
        request.setDateFormat(LoanTestData.DATETIME_PATTERN);
        request.setLocale(LoanTestData.LOCALE);
        return request;
    }

    public static PostLoansLoanIdChargesChargeIdRequest adjustLoanCharge(double amount) {
        PostLoansLoanIdChargesChargeIdRequest request = new PostLoansLoanIdChargesChargeIdRequest();
        request.setAmount(amount);
        request.setLocale(LoanTestData.LOCALE);
        return request;
    }

    public static PostLoansRequest applyLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .expectedDisbursementDate(submittedOnDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .transactionProcessingStrategyCode("due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy")//
                .locale(LoanTestData.LOCALE)//
                .submittedOnDate(submittedOnDate)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .numberOfRepayments(numberOfRepayments)//
                .loanTermFrequency(numberOfRepayments * 30)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .maxOutstandingLoanBalance(BigDecimal.valueOf(principal))//
                .principal(BigDecimal.valueOf(principal))//
                .loanType("individual")//
                .graceOnArrearsAgeing(0);
        if (customizer != null) {
            customizer.accept(request);
        }
        return request;
    }

    public static PostLoansRequest applyLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            int numberOfRepayments) {
        return applyLoanRequest(clientId, productId, submittedOnDate, principal, numberOfRepayments, null);
    }

    public static PostLoansRequest applyLP2ProgressiveLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            Double interestRate, int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = new PostLoansRequest()//
                .clientId(clientId)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .productId(productId)//
                .expectedDisbursementDate(submittedOnDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE)//
                .submittedOnDate(submittedOnDate)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestRatePerPeriod(BigDecimal.valueOf(interestRate))//
                .numberOfRepayments(numberOfRepayments)//
                .principal(BigDecimal.valueOf(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .loanType("individual");
        if (customizer != null) {
            customizer.accept(request);
        }
        return request;
    }

}
