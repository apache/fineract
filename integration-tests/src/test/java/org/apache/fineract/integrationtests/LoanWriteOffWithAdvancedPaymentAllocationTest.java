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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanWriteOffWithAdvancedPaymentAllocationTest {

    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;
    private static ClientHelper CLIENT_HELPER;
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
        CLIENT_HELPER = new ClientHelper(REQUEST_SPEC, RESPONSE_SPEC);
    }

    @Test
    public void loanWriteOffWithAdvancedPaymentAllocationTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccountAndDisbursePrincipalAmount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "200", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "200"));

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(100.0));

        // write off loan and verify amount
        final PostLoansLoanIdTransactionsResponse writeOffTransaction = LOAN_TRANSACTION_HELPER.writeOffLoanAccount(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .note("test WriteOff"));

        GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getClosedWrittenOff());

        // verify amounts for write-off transaction
        verifyTransaction(LocalDate.of(2022, 9, 10), 1100.0f, 1000.0f, 0.0f, 100.0f, 0.0f, loanId, "writeOff");

    }

    @Test
    public void loanUndoRepaymentAfterWriteOffShouldGiveErrorTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccountAndDisbursePrincipalAmount(clientId, loanProductId, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(250.0));

        // write off loan
        final PostLoansLoanIdTransactionsResponse writeOffTransaction = LOAN_TRANSACTION_HELPER.writeOffLoanAccount(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .note("test WriteOff"));

        GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getClosedWrittenOff());

        // reverse repayment
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                        new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("9 September 2022").locale("en")
                                .dateFormat("dd MMMM yyyy").transactionAmount(0.0)));

        assertEquals(503, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.written.off.update.not.allowed"));
    }

    @Test
    public void loanBackdatedRepaymentAfterWriteOffShouldGiveErrorTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccountAndDisbursePrincipalAmount(clientId, loanProductId, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(250.0));

        // write off loan
        final PostLoansLoanIdTransactionsResponse writeOffTransaction = LOAN_TRANSACTION_HELPER.writeOffLoanAccount(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .note("test WriteOff"));

        GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getClosedWrittenOff());

        // backdate repayment after write-off
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en").transactionAmount(50.0)));

        assertEquals(400, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.must.be.active.fully.paid.or.overpaid"));
    }

    @Test
    public void loanUndoWriteOffShouldGiveErrorTest() {
        // create loan product with Advanced Payment Allocation Strategy with default allocation with future installment
        // allocation as NEXT_INSTALLMENT
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

        Integer loanProductId = createLoanProduct(defaultAllocation);
        Assertions.assertNotNull(loanProductId);

        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccountAndDisbursePrincipalAmount(clientId, loanProductId, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(250.0));

        // write off loan
        final PostLoansLoanIdTransactionsResponse writeOffTransaction = LOAN_TRANSACTION_HELPER.writeOffLoanAccount(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .note("test WriteOff"));

        GetLoansLoanIdResponse loanDetails = LOAN_TRANSACTION_HELPER.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getClosedWrittenOff());

        // reverse write-off
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> LOAN_TRANSACTION_HELPER.reverseLoanTransaction(loanExternalIdStr, writeOffTransaction.getResourceId(),
                        new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("8 September 2022").locale("en")
                                .dateFormat("dd MMMM yyyy").transactionAmount(0.0)));

        assertEquals(503, exception.getResponse().code());
        assertTrue(exception.getMessage().contains("error.msg.loan.written.off.update.not.allowed"));
    }

    private Integer createLoanProduct(AdvancedPaymentData... advancedPaymentData) {
        String loanProductCreateJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).build();
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductCreateJSON);

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

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private Integer createLoanAccountAndDisbursePrincipalAmount(final Integer clientID, final Integer loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy("advanced-payment-allocation-strategy").build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
        LOAN_TRANSACTION_HELPER.approveLoan("02 September 2022", "1000", loanId, null);
        LOAN_TRANSACTION_HELPER.disburseLoanWithTransactionAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private void verifyTransaction(final LocalDate transactionDate, final Float transactionAmount, final Float principalPortion,
            final Float interestPortion, final Float feePortion, final Float penaltyPortion, final Integer loanID,
            final String transactionOfType) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) LOAN_TRANSACTION_HELPER.getLoanTransactions(REQUEST_SPEC, RESPONSE_SPEC,
                loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isTransaction = (Boolean) transactionType.get(transactionOfType);

            if (isTransaction) {
                ArrayList<Integer> transactionDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate transactionEntryDate = LocalDate.of(transactionDateAsArray.get(0), transactionDateAsArray.get(1),
                        transactionDateAsArray.get(2));

                if (transactionDate.isEqual(transactionEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(transactionAmount, Float.valueOf(String.valueOf(transactions.get(i).get("amount"))),
                            "Mismatch in transaction amounts");
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transactions.get(i).get("principalPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Transaction entries are posted");
    }

}
