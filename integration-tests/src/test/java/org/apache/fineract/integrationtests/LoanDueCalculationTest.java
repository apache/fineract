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

import static org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoanDueCalculationTest extends BaseLoanIntegrationTest {

    private static Stream<Arguments> processingStrategy() {
        return Stream.of(
                Arguments.of(Named.of("originalStrategy",
                        DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY)), //
                Arguments.of(Named.of("advancedStrategy", AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)) //
        );
    }

    // Repayment dates are calculated from the provided date (2024-02-29). As repayment starting date was provided, it
    // overrules `repayment start date type` configuration
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnFirstRepaymentDate(String repaymentProcessor) {
        runAt("2 February 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-01-31", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).dateFormat("yyyy-MM-dd")
                                .repaymentsStartingFromDate(LocalDate.of(2024, 2, 29));
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "29 March 2024"), //
                    installment(250.0, false, "29 April 2024"), //
                    installment(250.0, false, "29 May 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(1000.0, "31 January 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "29 March 2024"), //
                    installment(250.0, false, "29 April 2024"), //
                    installment(250.0, false, "29 May 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "31 January 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "29 March 2024"), //
                    installment(250.0, false, "29 April 2024"), //
                    installment(250.0, false, "29 May 2024")) //
            ;

        });
    }

    // Repayment dates are calculated based on `repayment start date type` configuration(=Expected disbursement date).
    // Expected disbursement date `2024-01-30`,
    // which is used to generate repayment due date when loan got submitted and approved, however the loan got disbursed
    // on `2024-01-31`,
    // the repayment schedule reflects the "new date" after it got disbursed
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnExpectedDisbursementDate(String repaymentProcessor) {
        runAt("31 March 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor)
                    .repaymentStartDateType(RepaymentStartDateType.DISBURSEMENT_DATE.getValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-01-30", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).dateFormat("yyyy-MM-dd");
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "30 January 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "30 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "30 May 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(1000.0, "30 January 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "30 January 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "30 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "30 May 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "31 March 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024"), //
                    installment(250.0, false, "30 June 2024"), //
                    installment(250.0, false, "31 July 2024")) //
            ;
        });
    }

    // Repayment dates are calculated based on `repayment start date type` configuration(=Submitted on date). Submitted
    // on date is `2024-01-31`,
    // and even the expected disbursement date is `2024-02-01`, the generated repayment schedule honors the submitted on
    // date
    // when it got disbursed on `2024-02-03`, the repayment schedule due dates got no changed.
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnSubmittedOnDate(String repaymentProcessor) {
        runAt("03 February 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor)
                    .repaymentStartDateType(RepaymentStartDateType.SUBMITTED_ON_DATE.getValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-02-01", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).submittedOnDate("2024-01-31").dateFormat("yyyy-MM-dd");
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "01 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(1000.0, "31 January 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "01 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "03 February 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "03 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;
        });
    }

    // Repayment dates are calculated based on `repayment start date type` configuration(=Submitted on date). Submitted
    // on date is `2024-01-31 the expected disbursement date is `2024-02-26`, the minimum days between disbursement and
    // first repayment is 10 days
    // so the repayment schedule got amended accordingly
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnSubmittedOnDateButThereShallBeMinimumDaysBetweenDisbursementAndFirstRepayment(String repaymentProcessor) {
        runAt("31 January 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor)
                    .repaymentStartDateType(RepaymentStartDateType.SUBMITTED_ON_DATE.getValue())
                    .minimumDaysBetweenDisbursalAndFirstRepayment(10);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-02-26", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).submittedOnDate("2024-01-31").dateFormat("yyyy-MM-dd");
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "26 February 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(1000.0, "31 January 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "26 February 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "31 January 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;
        });
    }

    // Repayment dates are calculated based on `repayment start date type` configuration(=Disbursement date). Submitted
    // on date is `2024-01-31 the expected disbursement date is `2024-02-26`, the minimum days between disbursement and
    // first repayment is 36 days
    // so the repayment schedule got amended accordingly
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnExpectedDisbursalDateButThereShallBeMinimumDaysBetweenDisbursementAndFirstRepayment(
            String repaymentProcessor) {
        runAt("31 January 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor)
                    .repaymentStartDateType(RepaymentStartDateType.DISBURSEMENT_DATE.getValue())
                    .minimumDaysBetweenDisbursalAndFirstRepayment(36);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-01-31", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).submittedOnDate("2024-01-31").dateFormat("yyyy-MM-dd");
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(1000.0, "31 January 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "31 January 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), installment(1000.0, null, "31 January 2024"), //
                    installment(250.0, false, "07 March 2024"), //
                    installment(250.0, false, "07 April 2024"), //
                    installment(250.0, false, "07 May 2024"), //
                    installment(250.0, false, "07 June 2024")) //
            ;
        });
    }

    // Repayment dates are calculated based on `repayment start date type` configuration(=Submitted on date). Submitted
    // on date is `2024-01-31`, and even the expected disbursement date is `2024-02-01`, the generated repayment
    // schedule honors the submitted on date
    // when it got approved and new expected disbursement date is `2024-02-02`, the repayment schedule due dates got no
    // changed
    // when it got disbursed on `2024-02-03`, the repayment schedule due dates got no changed.
    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void dueDateBasedOnSubmittedOnDateButChangingExpectedDisbursementAtApproval(String repaymentProcessor) {
        runAt("03 February 2024", () -> {
            // Client and Loan account creation
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest loanProductsRequest = create4Period1MonthLongWithoutInterestProduct(repaymentProcessor)
                    .repaymentStartDateType(RepaymentStartDateType.SUBMITTED_ON_DATE.getValue());
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            PostLoansRequest loanRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "2024-02-01", 1000.0, 4,
                    (postLoansRequest) -> {
                        postLoansRequest.transactionProcessingStrategyCode(repaymentProcessor).repaymentEvery(1).repaymentFrequencyType(2)
                                .loanTermFrequency(4).loanTermFrequencyType(2).submittedOnDate("2024-01-31").dateFormat("yyyy-MM-dd");
                    });
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(loanRequest);
            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "01 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(1000.0, "31 January 2024", "02 February 2024"));

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "02 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;

            disburseLoan(postLoansResponse.getLoanId(), BigDecimal.valueOf(1000.00), "03 February 2024");

            verifyRepaymentSchedule(postLoansResponse.getLoanId(), //
                    installment(1000.0, null, "03 February 2024"), //
                    installment(250.0, false, "29 February 2024"), //
                    installment(250.0, false, "31 March 2024"), //
                    installment(250.0, false, "30 April 2024"), //
                    installment(250.0, false, "31 May 2024")) //
            ;
        });
    }
}
