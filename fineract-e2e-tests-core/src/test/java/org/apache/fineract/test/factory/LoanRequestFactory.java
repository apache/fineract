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
package org.apache.fineract.test.factory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.InterestType;
import org.apache.fineract.test.data.LoanTermFrequencyType;
import org.apache.fineract.test.data.RepaymentFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanRequestFactory {

    @Autowired
    private LoanProductResolver loanProductResolver;

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final DefaultLoanProduct DEFAULT_LOAN_PRODUCT = DefaultLoanProduct.valueOf("LP1");
    public static final Double DEFAULT_PAYMENT_TRANSACTION_AMOUNT = 200.00;
    public static final Double DEFAULT_UNDO_TRANSACTION_AMOUNT = 0.0;
    public static final Double DEFAULT_REPAYMENT_TRANSACTION_AMOUNT = 200.00;
    public static final Double DEFAULT_CHARGEBACK_TRANSACTION_AMOUNT = 250.00;
    public static final Double DEFAULT_CHARGE_ADJUSTMENT_TRANSACTION_AMOUNT = 10.00;
    public static final String DEFAULT_EXTERNAL_ID = "";
    public static final Long DEFAULT_PAYMENT_TYPE_ID = 4L;
    public static final Long DEFAULT_PAYMENT_TYPE_ID_CHARGEBACK = 8L;
    public static final BigDecimal DEFAULT_PRINCIPAL = BigDecimal.valueOf(1000L);
    public static final BigDecimal DEFAULT_APPROVED_AMOUNT = BigDecimal.valueOf(1000L);
    public static final BigDecimal DEFAULT_DISBURSED_AMOUNT = BigDecimal.valueOf(1000L);
    public static final Integer DEFAULT_LOAN_TERM_FREQUENCY = 30;
    public static final Integer DEFAULT_LOAN_TERM_FREQUENCY_TYPE = LoanTermFrequencyType.DAYS.value;
    public static final Integer DEFAULT_REPAYMENT_FREQUENCY_TYPE = RepaymentFrequencyType.DAYS.value;
    public static final Integer DEFAULT_REAGING_FREQUENCY_NUMBER = 1;
    public static final String DEFAULT_REAGING_FREQUENCY_TYPE = "MONTHS";
    public static final BigDecimal DEFAULT_INTEREST_RATE_PER_PERIOD = new BigDecimal(0);
    public static final Integer DEFAULT_INTEREST_TYPE = InterestType.FLAT.value;
    public static final Integer DEFAULT_INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT_PERIOD = InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value;
    public static final Integer DEFAULT_AMORTIZATION_TYPE = 1;
    public static final String DEFAULT_LOAN_TYPE = "individual";
    public static final Integer DEFAULT_NUMBER_OF_REPAYMENTS = 1;
    public static final Integer DEFAULT_NUMBER_OF_INSTALLMENTS = 5;
    public static final Integer DEFAULT_REPAYMENT_FREQUENCY = 30;
    public static final String DEFAULT_TRANSACTION_PROCESSING_STRATEGY_CODE = TransactionProcessingStrategyCode.PENALTIES_FEES_INTEREST_PRINCIPAL_ORDER.value;

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final String DATE_SUBMIT_STRING = FORMATTER.format(LocalDate.now(Clock.systemUTC()).minusMonths(1L));
    public static final String DEFAULT_TRANSACTION_DATE = FORMATTER.format(LocalDate.now(Clock.systemUTC()).minusMonths(1L));

    public PostLoansRequest defaultLoansRequest(Long clientId) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductResolver.resolve(DEFAULT_LOAN_PRODUCT))//
                .submittedOnDate(DATE_SUBMIT_STRING)//
                .expectedDisbursementDate(DATE_SUBMIT_STRING)//
                .principal(DEFAULT_PRINCIPAL)//
                .locale(DEFAULT_LOCALE)//
                .loanTermFrequency(DEFAULT_LOAN_TERM_FREQUENCY)//
                .loanTermFrequencyType(DEFAULT_LOAN_TERM_FREQUENCY_TYPE)//
                .loanType(DEFAULT_LOAN_TYPE)//
                .numberOfRepayments(DEFAULT_NUMBER_OF_REPAYMENTS)//
                .repaymentEvery(DEFAULT_REPAYMENT_FREQUENCY)//
                .repaymentFrequencyType(DEFAULT_REPAYMENT_FREQUENCY_TYPE)//
                .interestRatePerPeriod(DEFAULT_INTEREST_RATE_PER_PERIOD)//
                .interestType(DEFAULT_INTEREST_TYPE)//
                .interestCalculationPeriodType(DEFAULT_INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT_PERIOD)//
                .amortizationType(DEFAULT_AMORTIZATION_TYPE)//
                .transactionProcessingStrategyCode(DEFAULT_TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .dateFormat(DATE_FORMAT)//
                .graceOnArrearsAgeing(3)//
                .maxOutstandingLoanBalance(new BigDecimal(10000));
    }

    public PutLoansLoanIdRequest modifySubmittedOnDateOnLoan(Long clientId, String newSubmittedOnDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String dateDisburseStr = formatter.format(LocalDate.now(Clock.systemUTC()));

        return new PutLoansLoanIdRequest()//
                .productId(loanProductResolver.resolve(DEFAULT_LOAN_PRODUCT))//
                .submittedOnDate(newSubmittedOnDate)//
                .expectedDisbursementDate(dateDisburseStr)//
                .linkAccountId(null)//
                .createStandingInstructionAtDisbursement(null)//
                .loanTermFrequency(DEFAULT_LOAN_TERM_FREQUENCY)//
                .loanTermFrequencyType(DEFAULT_LOAN_TERM_FREQUENCY_TYPE)//
                .numberOfRepayments(DEFAULT_NUMBER_OF_REPAYMENTS)//
                .repaymentEvery(DEFAULT_REPAYMENT_FREQUENCY)//
                .repaymentFrequencyType(DEFAULT_REPAYMENT_FREQUENCY_TYPE)//
                .repaymentFrequencyNthDayType(null)//
                .repaymentFrequencyDayOfWeekType(null)//
                .repaymentsStartingFromDate(null)//
                .interestChargedFromDate(null)//
                .interestRatePerPeriod(DEFAULT_INTEREST_RATE_PER_PERIOD)//
                .interestType(DEFAULT_INTEREST_TYPE)//
                .interestCalculationPeriodType(DEFAULT_INTEREST_CALCULATION_PERIOD_TYPE_SAME_AS_REPAYMENT_PERIOD)//
                .amortizationType(DEFAULT_AMORTIZATION_TYPE)//
                .isEqualAmortization(false)//
                .transactionProcessingStrategyCode(DEFAULT_TRANSACTION_PROCESSING_STRATEGY_CODE)//
                .graceOnArrearsAgeing(3)//
                .loanIdToClose(null)//
                .isTopup(null)//
                .maxOutstandingLoanBalance(10000L)//
                .charges(new ArrayList<>())//
                .collateral(new ArrayList<>())//
                .disbursementData(new ArrayList<>())//
                .clientId(clientId)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en")//
                .loanType("individual")//
                .principal(DEFAULT_PRINCIPAL.longValue());//
    }

    public static PutLoansLoanIdRequest enableFraudFlag() {
        return new PutLoansLoanIdRequest().fraud(true);
    }

    public static PutLoansLoanIdRequest disableFraudFlag() {
        return new PutLoansLoanIdRequest().fraud(false);
    }

    public static PostLoansLoanIdRequest defaultLoanApproveRequest() {
        return new PostLoansLoanIdRequest()//
                .approvedOnDate(DATE_SUBMIT_STRING)//
                .expectedDisbursementDate(DATE_SUBMIT_STRING)//
                .approvedLoanAmount(DEFAULT_APPROVED_AMOUNT)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE);//
    }

    public static PostLoansLoanIdRequest defaultLoanDisburseRequest() {
        return new PostLoansLoanIdRequest().actualDisbursementDate(DATE_SUBMIT_STRING).transactionAmount(DEFAULT_DISBURSED_AMOUNT)
                .paymentTypeId(Math.toIntExact(DEFAULT_PAYMENT_TYPE_ID)).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsRequest defaultPaymentTransactionRequest() {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_PAYMENT_TRANSACTION_AMOUNT).paymentTypeId(DEFAULT_PAYMENT_TYPE_ID).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsRequest defaultRepaymentRequest() {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_REPAYMENT_TRANSACTION_AMOUNT).paymentTypeId(DEFAULT_PAYMENT_TYPE_ID).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsRequest defaultRefundRequest() {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_REPAYMENT_TRANSACTION_AMOUNT).paymentTypeId(DEFAULT_PAYMENT_TYPE_ID).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultRepaymentUndoRequest() {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_UNDO_TRANSACTION_AMOUNT).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultRepaymentAdjustRequest(double amount) {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(DEFAULT_TRANSACTION_DATE).transactionAmount(amount)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultTransactionUndoRequest() {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_UNDO_TRANSACTION_AMOUNT).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultRefundUndoRequest() {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_UNDO_TRANSACTION_AMOUNT).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultChargebackRequest() {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionAmount(DEFAULT_CHARGEBACK_TRANSACTION_AMOUNT)
                .locale(DEFAULT_LOCALE).paymentTypeId(DEFAULT_PAYMENT_TYPE_ID_CHARGEBACK);
    }

    public static PostLoansLoanIdChargesChargeIdRequest defaultChargeAdjustmentRequest() {
        return new PostLoansLoanIdChargesChargeIdRequest().amount(DEFAULT_CHARGE_ADJUSTMENT_TRANSACTION_AMOUNT)
                .externalId(DEFAULT_EXTERNAL_ID);
    }

    public static PostLoansLoanIdTransactionsTransactionIdRequest defaultChargeAdjustmentTransactionUndoRequest() {
        return new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate(DEFAULT_TRANSACTION_DATE)
                .transactionAmount(DEFAULT_UNDO_TRANSACTION_AMOUNT).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsRequest defaultChargeOffRequest() {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(DEFAULT_TRANSACTION_DATE).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);
    }

    public static PostLoansLoanIdTransactionsRequest defaultUndoChargeOffRequest() {
        return new PostLoansLoanIdTransactionsRequest();
    }

    public static PostLoansLoanIdTransactionsRequest defaultReAgingRequest() {
        return new PostLoansLoanIdTransactionsRequest()//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE)//
                .frequencyNumber(DEFAULT_REAGING_FREQUENCY_NUMBER)//
                .frequencyType(DEFAULT_REAGING_FREQUENCY_TYPE)//
                .startDate(DEFAULT_TRANSACTION_DATE)//
                .numberOfInstallments(DEFAULT_NUMBER_OF_INSTALLMENTS);//
    }

    public static PostLoansLoanIdTransactionsRequest defaultLoanReAmortizationRequest() {
        return new PostLoansLoanIdTransactionsRequest().dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }

    public static PostUpdateRescheduleLoansRequest defaultLoanRescheduleUpdateRequest() {
        return new PostUpdateRescheduleLoansRequest()//
                .locale(DEFAULT_LOCALE)//
                .dateFormat(DATE_FORMAT);
    }

    public static PostCreateRescheduleLoansRequest defaultLoanRescheduleCreateRequest(Long loanId, String fromDate, String toDate) {
        return new PostCreateRescheduleLoansRequest()//
                .submittedOnDate(DATE_SUBMIT_STRING)//
                .rescheduleFromDate(fromDate)//
                .adjustedDueDate(toDate)//
                .rescheduleReasonId(1L)//
                .loanId(loanId)//
                .locale(DEFAULT_LOCALE)//
                .dateFormat(DATE_FORMAT);
    }

    public static PostLoansLoanIdTransactionsRequest defaultWriteOffRequest() {
        return new PostLoansLoanIdTransactionsRequest().transactionDate(DEFAULT_TRANSACTION_DATE).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE).note("Write Off");
    }
}
