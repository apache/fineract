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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionEnumDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansType;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargebackTransactionEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class LoanChargeBackStepDef extends AbstractStepDef {

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private PaymentTypeResolver paymentTypeResolver;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @Autowired
    EventStore eventStore;

    @When("Admin makes {string} chargeback with {double} EUR transaction amount")
    public void makeLoanChargeback(String repaymentType, double transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsResponse repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);
        Long transactionId = Long.valueOf(repaymentResponse.getResourceId());

        makeChargebackCall(loanId, transactionId, repaymentType, transactionAmount);
    }

    @When("Admin makes {string} chargeback with {double} EUR transaction amount for Payment nr. {double}")
    public void makeLoanChargebackForPayment(String repaymentType, double transactionAmount, double paymentNr) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();

        List<Long> transactionIdList = new ArrayList<>();
        for (GetLoansLoanIdTransactions f : transactions) {
            String code = f.getType().getCode();
            if (code.equals("loanTransactionType.repayment")) {
                transactionIdList.add(f.getId());
            }
        }
        Collections.sort(transactionIdList);
        Long transactionId = transactionIdList.get((int) paymentNr - 1);

        makeChargebackCall(loanId, transactionId, repaymentType, transactionAmount);
    }

    @When("Admin makes {string} chargeback with {double} EUR transaction amount for Downpayment nr. {double}")
    public void makeLoanChargebackForDownpaymentayment(String repaymentType, double transactionAmount, double paymentNr)
            throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();

        List<Long> transactionIdList = new ArrayList<>();
        for (GetLoansLoanIdTransactions f : transactions) {
            String code = f.getType().getCode();
            if (code.equals("loanTransactionType.downPayment")) {
                transactionIdList.add(f.getId());
            }
        }
        Collections.sort(transactionIdList);
        Long transactionId = transactionIdList.get((int) paymentNr - 1);

        makeChargebackCall(loanId, transactionId, repaymentType, transactionAmount);
    }

    @When("Admin makes {string} chargeback with {double} EUR transaction amount for MIR nr. {double}")
    public void makeLoanChargebackForMIR(String repaymentType, double transactionAmount, double paymentNr) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();

        List<Long> transactionIdList = new ArrayList<>();
        for (GetLoansLoanIdTransactions f : transactions) {
            String code = f.getType().getCode();
            if (code.equals("loanTransactionType.merchantIssuedRefund")) {
                transactionIdList.add(f.getId());
            }
        }
        Collections.sort(transactionIdList);
        Long transactionId = transactionIdList.get((int) paymentNr - 1);

        makeChargebackCall(loanId, transactionId, repaymentType, transactionAmount);
    }

    private void makeChargebackCall(Long loanId, Long transactionId, String repaymentType, double transactionAmount) throws IOException {
        eventStore.reset();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsTransactionIdRequest chargebackRequest = LoanRequestFactory.defaultChargebackRequest()
                .paymentTypeId(paymentTypeValue).transactionAmount(transactionAmount);

        PostLoansLoanIdTransactionsResponse chargebackResponse = ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                transactionId, chargebackRequest, Map.of("command", "chargeback")));
        testContext().set(TestContextKey.LOAN_CHARGEBACK_RESPONSE, chargebackResponse);
        checkEvents(chargebackResponse);
    }

    private void checkEvents(PostLoansLoanIdTransactionsResponse chargebackResponse) throws IOException {
        PostLoansLoanIdTransactionsResponse responseBody = chargebackResponse;
        Long loanId = responseBody.getLoanId();

        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
        checkLoanChargebackTransactionEvent(responseBody);
    }

    private void checkLoanChargebackTransactionEvent(PostLoansLoanIdTransactionsResponse chargebackResponse) throws IOException {

        // get loanId and transactionId
        Long loanId = chargebackResponse.getLoanId();
        long transactionId = Long.valueOf(chargebackResponse.getResourceId());

        // retrieve transaction details
        GetLoansLoanIdTransactionsTransactionIdResponse transactionResponseBody = ok(
                () -> fineractClient.loanTransactions().retrieveTransaction(loanId, transactionId, Map.of()));

        // Get transaction type from response
        GetLoansType transactionType = transactionResponseBody.getType();
        // Build expected avro transaction type from api response type
        LoanTransactionEnumDataV1 expectedEventTransactionType = LoanTransactionEnumDataV1.newBuilder()
                .setId(transactionType.getId().intValue()).setCode(transactionType.getCode()).setValue("Chargeback")
                .setDisbursement(transactionType.getDisbursement()).setRepaymentAtDisbursement(transactionType.getRepaymentAtDisbursement())
                .setRepayment(transactionType.getRepayment()).setMerchantIssuedRefund(false).setPayoutRefund(false).setGoodwillCredit(false)
                .setChargeRefund(false).setContra(transactionType.getContra()).setWaiveInterest(transactionType.getWaiveInterest())
                .setWaiveCharges(transactionType.getWaiveCharges()).setAccrual(false).setWriteOff(transactionType.getWriteOff())
                .setRecoveryRepayment(transactionType.getRecoveryRepayment()).setInitiateTransfer(false).setApproveTransfer(false)
                .setWithdrawTransfer(false).setRejectTransfer(false).setChargePayment(false).setRefund(false).setRefundForActiveLoans(false)
                .setCreditBalanceRefund(false).setChargeback(true).build();

        // verify payload for loanId, transactionId, transactionType, amount
        eventAssertion.assertEvent(LoanChargebackTransactionEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getType).isEqualTo(expectedEventTransactionType)
                .extractingBigDecimal(LoanTransactionDataV1::getAmount).isEqualTo(BigDecimal.valueOf(transactionResponseBody.getAmount()));

    }
}
