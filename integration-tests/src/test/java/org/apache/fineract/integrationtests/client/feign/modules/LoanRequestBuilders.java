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
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;

public final class LoanRequestBuilders {

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
                .transactionProcessingStrategyCode("DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY")//
                .loanType("individual")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansRequest applyProgressiveLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments, Double interestRate) {
        return applyCumulativeLoan(clientId, productId, submittedOnDate, principal, numberOfRepayments, interestRate);
    }

    public static PostLoansLoanIdRequest approveLoan(Double approvedAmount, String approvedOnDate) {
        return new PostLoansLoanIdRequest()//
                .approvedLoanAmount(BigDecimal.valueOf(approvedAmount))//
                .approvedOnDate(approvedOnDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    public static PostLoansLoanIdRequest disburseLoan(Double disbursedAmount, String disbursedOnDate) {
        return new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursedOnDate)//
                .transactionAmount(BigDecimal.valueOf(disbursedAmount))//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
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
}
