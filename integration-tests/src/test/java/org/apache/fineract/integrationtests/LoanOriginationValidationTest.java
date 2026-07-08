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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanOriginationValidationTest extends FeignLoanTestBase {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static Long clientId;

    @BeforeAll
    public static void createCommonClient() {
        clientId = clientHelper.createClient();
    }

    // uc1: Negative Test: Loan approval transaction without approvedOnDate parameter
    // uc1: Negative Test: Loan approval transaction without approvedOnDate parameter
    // 1. Create a Loan product
    // 2. Submit a Loan application
    // 3. Try to Approve a Loan account without approvedOnDate parameter to catch The request was invalid error due
    // missed required attribute
    @Test
    public void uc1() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 5).numberOfRepayments(6)
                    .loanTermFrequency(6).loanTermFrequencyType(2)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).repaymentEvery(1)
                    .repaymentFrequencyType(2).maxOutstandingLoanBalance(BigDecimal.valueOf(10000.0));

            Long loanId = applyForLoan(applicationRequest);

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(100))
                            .dateFormat(DATETIME_PATTERN).approvedOnDate(null).locale("en")));

            assertTrue(exception.getMessage().contains("The parameter `approvedOnDate` is mandatory."));
        });
    }

    // uc2: Negative Test: Loan disbursement transaction without actualDisbursementDate parameter
    // uc2: Negative Test: Loan disbursement transaction without actualDisbursementDate parameter
    // 1. Create a Loan product
    // 2. Submit and Approve Loan application
    // 3. Try to Disburse a Loan account without actualDisbursementDate parameter to catch The request was invalid error
    // due missed required attribute
    @Test
    public void uc2() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 5).numberOfRepayments(6)
                    .loanTermFrequency(6).loanTermFrequencyType(2)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).repaymentEvery(1)
                    .repaymentFrequencyType(2).maxOutstandingLoanBalance(BigDecimal.valueOf(10000.0));

            Long loanId = applyForLoan(applicationRequest);

            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(100)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate(operationDate).locale("en"));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(null).dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(100.0)).locale("en")));

            assertTrue(exception.getMessage().contains("The parameter `actualDisbursementDate` is mandatory."));
        });
    }

    // uc3: Negative Test: Loan application without required parameters
    // uc3: Negative Test: Loan application without required parameters
    // 1. Create a Loan product
    // 2. Submit Loan application without required parameters
    @Test
    public void uc3() {
        String operationDate = "15 August 2024";
        runAt(operationDate, () -> {
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            // Product Id null
            final PostLoansRequest applicationRequest01 = applyLoanRequest(clientId, null, operationDate, 100.0, 5).numberOfRepayments(6)
                    .loanTermFrequency(6).loanTermFrequencyType(2).repaymentEvery(1).repaymentFrequencyType(2);
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(applicationRequest01));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `productId` is mandatory."));

            // Transaction Processing Strategy Code null
            final PostLoansRequest applicationRequest02 = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 5)
                    .numberOfRepayments(6).loanTermFrequency(6).loanTermFrequencyType(2).transactionProcessingStrategyCode(null)
                    .repaymentEvery(1).repaymentFrequencyType(2);
            exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(applicationRequest02));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `transactionProcessingStrategyCode` is mandatory."));

            // Client Id null
            final PostLoansRequest applicationRequest03 = applyLoanRequest(null, loanProductId, operationDate, 100.0, 5)
                    .numberOfRepayments(6).loanTermFrequency(6).loanTermFrequencyType(2)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).repaymentEvery(1)
                    .repaymentFrequencyType(2);
            exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(applicationRequest03));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `clientId` is mandatory."));

            // Submitted Date null
            final PostLoansRequest applicationRequest04 = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 5)
                    .numberOfRepayments(6).submittedOnDate(null).loanTermFrequency(6).loanTermFrequencyType(2)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).repaymentEvery(1)
                    .repaymentFrequencyType(2);
            exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(applicationRequest04));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `submittedOnDate` is mandatory."));

            // Expected disbursement Date null
            final PostLoansRequest applicationRequest05 = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 5)
                    .numberOfRepayments(6).expectedDisbursementDate(null).loanTermFrequency(6).loanTermFrequencyType(2)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).repaymentEvery(1)
                    .repaymentFrequencyType(2);
            exception = assertThrows(CallFailedRuntimeException.class, () -> applyForLoan(applicationRequest05));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("The parameter `expectedDisbursementDate` is mandatory."));
        });
    }
}
