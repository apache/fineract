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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanWriteOffWithAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    @Test
    public void loanWriteOffWithAdvancedPaymentAllocationTest() {
        runAt("03 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            Long clientId = createClient("01 September 2022");
            Long loanProductId = createApaLoanProduct();
            Long loanId = createAndDisburseLoan(clientId, loanProductId, loanExternalIdStr);

            runAt("05 September 2022", () -> addCharge(loanId, false, 200, "05 September 2022"));

            runAt("09 September 2022", () -> addRepaymentForLoan(loanId, 100.0, "9 September 2022"));

            runAt("10 September 2022", () -> {
                writeOffLoan(loanExternalIdStr, LoanRequestBuilders.writeOff("10 September 2022").note("test WriteOff"));

                GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                assertTrue(loanDetails.getStatus().getClosedWrittenOff());

                var writeOffTx = loanDetails.getTransactions().stream().filter(tx -> Boolean.TRUE.equals(tx.getType().getWriteOff()))
                        .findFirst().orElseThrow();
                assertEquals(1100.0, Utils.getDoubleValue(writeOffTx.getAmount()));
                assertEquals(1000.0, Utils.getDoubleValue(writeOffTx.getPrincipalPortion()));
                assertEquals(0.0, Utils.getDoubleValue(writeOffTx.getInterestPortion()));
                assertEquals(100.0, Utils.getDoubleValue(writeOffTx.getFeeChargesPortion()));
                assertEquals(0.0, Utils.getDoubleValue(writeOffTx.getPenaltyChargesPortion()));
            });
        });
    }

    @Test
    public void loanUndoRepaymentAfterWriteOffShouldGiveErrorTest() {
        runAt("03 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            Long clientId = createClient("01 September 2022");
            Long loanProductId = createApaLoanProduct();
            Long loanId = createAndDisburseLoan(clientId, loanProductId, loanExternalIdStr);

            AtomicReference<Long> repaymentTransactionId = new AtomicReference<>();
            runAt("09 September 2022", () -> repaymentTransactionId.set(addRepaymentForLoan(loanId, 250.0, "9 September 2022")));

            runAt("10 September 2022", () -> {
                writeOffLoan(loanExternalIdStr, LoanRequestBuilders.writeOff("10 September 2022").note("test WriteOff"));
                assertTrue(getLoanDetails(loanId).getStatus().getClosedWrittenOff());

                CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                        () -> reverseLoanTransaction(loanExternalIdStr, repaymentTransactionId.get(),
                                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("9 September 2022").locale("en")
                                        .dateFormat("dd MMMM yyyy").transactionAmount(0.0)));

                assertEquals(403, exception.getStatus());
                assertTrue(exception.getMessage().contains("error.msg.loan.written.off.update.not.allowed"));
            });
        });
    }

    @Test
    public void loanBackdatedRepaymentAfterWriteOffShouldGiveErrorTest() {
        runAt("03 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            Long clientId = createClient("01 September 2022");
            Long loanProductId = createApaLoanProduct();
            Long loanId = createAndDisburseLoan(clientId, loanProductId, loanExternalIdStr);

            runAt("09 September 2022", () -> addRepaymentForLoan(loanId, 250.0, "9 September 2022"));

            runAt("10 September 2022", () -> {
                writeOffLoan(loanExternalIdStr, LoanRequestBuilders.writeOff("10 September 2022").note("test WriteOff"));
                assertTrue(getLoanDetails(loanId).getStatus().getClosedWrittenOff());

                CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                        () -> addRepayment(loanId, LoanRequestBuilders.repayLoan(50.0, "8 September 2022")));

                assertEquals(400, exception.getStatus());
                assertTrue(exception.getMessage().contains("error.msg.loan.must.be.active.fully.paid.or.overpaid"));
            });
        });
    }

    @Test
    public void loanUndoWriteOffShouldGiveErrorTest() {
        runAt("03 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            Long clientId = createClient("01 September 2022");
            Long loanProductId = createApaLoanProduct();
            Long loanId = createAndDisburseLoan(clientId, loanProductId, loanExternalIdStr);

            runAt("09 September 2022", () -> addRepaymentForLoan(loanId, 250.0, "9 September 2022"));

            AtomicReference<PostLoansLoanIdTransactionsResponse> writeOffTransaction = new AtomicReference<>();
            runAt("10 September 2022", () -> {
                writeOffTransaction
                        .set(writeOffLoan(loanExternalIdStr, LoanRequestBuilders.writeOff("10 September 2022").note("test WriteOff")));
                assertTrue(getLoanDetails(loanId).getStatus().getClosedWrittenOff());
            });

            runAt("10 September 2022", () -> {
                CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                        () -> reverseLoanTransaction(loanExternalIdStr, writeOffTransaction.get().getResourceId(),
                                new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("8 September 2022").locale("en")
                                        .dateFormat("dd MMMM yyyy").transactionAmount(0.0)));

                assertEquals(403, exception.getStatus());
                assertTrue(exception.getMessage().contains("error.msg.loan.written.off.update.not.allowed"));
            });
        });
    }

    private Long createApaLoanProduct() {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");
        PostLoanProductsRequest loanProductCreateRequest = new LoanProductTestBuilder().withPrincipal("15,000.00")
                .withNumberOfRepayments("4").withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).buildRequest();
        return createLoanProduct(loanProductCreateRequest);
    }

    private Long createAndDisburseLoan(Long clientId, Long loanProductId, String externalId) {
        PostLoansRequest loanApplication = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal("1000"))//
                .loanTermFrequency(30)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .numberOfRepayments(1)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .expectedDisbursementDate("03 September 2022")//
                .submittedOnDate("01 September 2022")//
                .loanType("individual")//
                .externalId(externalId)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN);

        Long loanId = applyForLoan(loanApplication);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoan(loanId, BigDecimal.valueOf(1000), "03 September 2022");
        return loanId;
    }
}
