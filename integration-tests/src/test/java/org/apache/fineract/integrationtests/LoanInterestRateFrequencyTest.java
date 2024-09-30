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

import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith({ LoanTestLifecycleExtension.class })
public class LoanInterestRateFrequencyTest extends BaseLoanIntegrationTest {

    @Test
    public void testProgressiveInterestRateTypeWholeTerm() {
        runAt("15 April 2024", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest loanProductsRequest = createLoanProductWithInterestCalculation();
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoanApplication(clientId, loanProductResponse.getResourceId(), "15 April 2024", 1000.0, 6);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000), "15 April 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(loanDetails.getInterestRateFrequencyType().getCode(),
                    "interestRateFrequency.periodFrequencyType.whole_term");
            Assertions.assertEquals(loanDetails.getAnnualInterestRate(), new BigDecimal("20.000000"));
            Assertions.assertEquals(loanDetails.getInterestRatePerPeriod(), new BigDecimal("10.000000"));
        });
    }

    private Long applyAndApproveLoanApplication(Long clientId, Long productId, String disbursementDate, double amount,
            int numberOfRepayments) {
        PostLoansRequest postLoansRequest = new PostLoansRequest().clientId(clientId).productId(productId)
                .expectedDisbursementDate(disbursementDate).dateFormat(DATETIME_PATTERN) //
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY) //
                .locale("en") //
                .submittedOnDate(disbursementDate) //
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS) //
                .interestRatePerPeriod(new BigDecimal(10.0)) //
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD) //
                .interestType(InterestType.DECLINING_BALANCE) //
                .repaymentEvery(1) //
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS) //
                .numberOfRepayments(numberOfRepayments) //
                .loanTermFrequency(numberOfRepayments) //
                .loanTermFrequencyType(2) //
                .maxOutstandingLoanBalance(BigDecimal.valueOf(amount)) //
                .principal(BigDecimal.valueOf(amount)) //
                .loanType("individual");
        PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(postLoansRequest);
        PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                approveLoanRequest(amount, disbursementDate));
        return approvedLoanResult.getLoanId();
    }

    private PostLoanProductsRequest createLoanProductWithInterestCalculation() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY) //
                .paymentAllocation(List.of(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation())) //
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL") //
                .principal(1000.0)//
                .numberOfRepayments(6)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue())//
                .interestType(BaseLoanIntegrationTest.InterestType.DECLINING_BALANCE)//
                .amortizationType(BaseLoanIntegrationTest.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestCalculationPeriodType(BaseLoanIntegrationTest.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .interestRatePerPeriod(10.0) //
                .interestRateFrequencyType(InterestRateFrequencyType.WHOLE_TERM)//
                .isInterestRecalculationEnabled(false);
    }

    private AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
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
}
