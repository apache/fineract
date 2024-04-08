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

import static org.apache.fineract.test.data.paymenttype.DefaultPaymentType.AUTOPAY;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.test.api.ApiProperties;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.LoanBalanceChangedEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class LoanRepaymentStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final String DEFAULT_ACCOUNT_NB = "1234567890";
    public static final String DEFAULT_CHECK_NB = "1234567890";
    public static final String DEFAULT_RECEIPT_NB = "1234567890";
    public static final String DEFAULT_BANK_NB = "1234567890";
    @Autowired
    private LoanTransactionsApi loanTransactionsApi;

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private UsersApi usersApi;

    @Autowired
    private PaymentTypeResolver paymentTypeResolver;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @And("Customer makes {string} repayment on {string} with {double} EUR transaction amount")
    public void makeLoanRepayment(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        makeRepayment(repaymentType, transactionDate, transactionAmount, null);
    }

    @And("Customer makes {string} repayment on {string} with {double} EUR transaction amount and check external owner")
    public void makeLoanRepaymentAndCheckOwner(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        String transferExternalOwnerId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        makeRepayment(repaymentType, transactionDate, transactionAmount, transferExternalOwnerId);
    }

    private void makeRepayment(String repaymentType, String transactionDate, double transactionAmount, String transferExternalOwnerId)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, repaymentRequest, "repayment", headerMap).execute();
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        ErrorHelper.checkSuccessfulApiCall(repaymentResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEvent = eventCheckHelper
                .transactionEventCheck(repaymentResponse, TransactionType.REPAYMENT, transferExternalOwnerId);
        testContext().set(TestContextKey.TRANSACTION_EVENT, transactionEvent);
        eventAssertion.assertEventRaised(LoanBalanceChangedEvent.class, loanId);
    }

    @And("Created user makes {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentWithGivenUser(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + apiProperties.getPassword();
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, repaymentRequest, "repayment", headerMap).execute();
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        ErrorHelper.checkSuccessfulApiCall(repaymentResponse);
        eventAssertion.assertEventRaised(LoanBalanceChangedEvent.class, loanId);
    }

    @And("Customer makes externalID controlled {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentByExternalId(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceExternalId = loanResponse.body().getResourceExternalId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction1(resourceExternalId, repaymentRequest, "repayment", headerMap).execute();

        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        ErrorHelper.checkSuccessfulApiCall(repaymentResponse);
        eventAssertion.assertEventRaised(LoanBalanceChangedEvent.class, loanId);
    }

    @And("Created user makes externalID controlled {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentWithGivenUserByExternalId(String repaymentType, String transactionDate, double transactionAmount)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceExternalId = loanResponse.body().getResourceExternalId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + apiProperties.getPassword();
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction1(resourceExternalId, repaymentRequest, "repayment", headerMap).execute();
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        ErrorHelper.checkSuccessfulApiCall(repaymentResponse);
        eventAssertion.assertEventRaised(LoanBalanceChangedEvent.class, loanId);
    }

    @And("Customer not able to make {string} repayment on {string} with {double} EUR transaction amount")
    public void makeLoanRepaymentFails(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, repaymentRequest, "repayment").execute();
        ErrorResponse errorDetails = ErrorResponse.from(repaymentResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.loanRepaymentOnClosedLoanFailureMsg());
    }

    @Then("Customer not able to make a repayment undo on {string} due to charge off")
    public void makeLoanRepaymentUndoAfterChargeOff(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Response<PostLoansLoanIdTransactionsResponse> transactionResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();
        Long transactionId = transactionResponse.body().getResourceId();

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, repaymentResponse.body().getResourceId(), repaymentUndoRequest, "").execute();
        ErrorResponse errorDetails = ErrorResponse.from(repaymentUndoResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.repaymentUndoFailureDueToChargeOffCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.repaymentUndoFailureDueToChargeOff(transactionId));
    }

    @And("Customer makes {string} repayment on {string} with {double} EUR transaction amount \\(and transaction fails because of wrong date)")
    public void makeLoanRepaymentWithWrongDate(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, repaymentRequest, "repayment").execute();
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
    }

    @When("Refund happens on {string} with {double} EUR transaction amount")
    public void makeRefund(String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdTransactionsRequest refundRequest = LoanRequestFactory.defaultRefundRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeResolver.resolve(AUTOPAY)).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE).accountNumber(DEFAULT_ACCOUNT_NB).checkNumber(DEFAULT_CHECK_NB).receiptNumber(DEFAULT_RECEIPT_NB)
                .bankNumber(DEFAULT_BANK_NB);

        Response<PostLoansLoanIdTransactionsResponse> refundResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, refundRequest, "payoutRefund").execute();
        ErrorHelper.checkSuccessfulApiCall(refundResponse);
        testContext().set(TestContextKey.LOAN_REFUND_RESPONSE, refundResponse);
    }

    @When("Refund undo happens on {string}")
    public void makeRefundUndo(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<PostLoansLoanIdTransactionsResponse> refundResponse = testContext().get(TestContextKey.LOAN_REFUND_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest refundUndoRequest = LoanRequestFactory.defaultRefundUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> refundUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, refundResponse.body().getResourceId(), refundUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(refundUndoResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, refundUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, refundResponse.body().getResourceId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(refundResponse.body().getResourceId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    @When("Customer makes a repayment undo on {string}")
    public void makeLoanRepaymentUndo(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, repaymentResponse.body().getResourceId(), repaymentUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(repaymentUndoResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, repaymentResponse.body().getResourceId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(repaymentResponse.body().getResourceId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    @Then("Loan {string} transaction adjust amount {double} must return {int} code")
    public void makeLoanRepaymentAdjustFail(String transactionType, double transactionAmount, int codeExpected) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = testContext().get(transactionType);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionAmount(transactionAmount);

        Response<PostLoansLoanIdTransactionsResponse> repaymentUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, repaymentResponse.body().getResourceId(), repaymentUndoRequest, "").execute();
        assertThat(repaymentUndoResponse.code()).isEqualTo(codeExpected);
    }

    @When("Customer undo {string}th repayment on {string}")
    public void undoNthRepayment(String nthItemStr, String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        List<GetLoansLoanIdTransactions> transactions = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> Boolean.TRUE.equals(t.getType().getRepayment()))
                .toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, targetTransaction.getId(), repaymentUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(repaymentUndoResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    @When("Customer undo {string}th transaction made on {string}")
    public void undoNthTransaction(String nthItemStr, String transactionDate) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        List<GetLoansLoanIdTransactions> transactions = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))).toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        Response<PostLoansLoanIdTransactionsResponse> transactionUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(transactionUndoResponse);
        testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    @When("Customer undo {string}th {string} transaction made on {string}")
    public void undoNthTransactionType(String nthItemStr, String transactionType, String transactionDate) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        List<GetLoansLoanIdTransactions> transactions = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(formatter.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .toList()//
                .get(nthItem);//

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        Response<PostLoansLoanIdTransactionsResponse> transactionUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(transactionUndoResponse);
        testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    @Then("Repayment transaction is created with {double} amount and {string} type")
    public void loanRepaymentStatus(double repaymentAmount, String paymentType) throws IOException {
        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdTransactionsTransactionIdResponse> transactionResponse = loanTransactionsApi
                .retrieveTransaction(loanId, repaymentResponse.body().getResourceId(), "").execute();
        ErrorHelper.checkSuccessfulApiCall(transactionResponse);
        assertThat(transactionResponse.body().getAmount()).isEqualTo(repaymentAmount);
        assertThat(transactionResponse.body().getPaymentDetailData().getPaymentType().getName()).isEqualTo(paymentType);

    }

    @Then("Repayment failed because the repayment date is after the business date")
    public void repaymentDateFailure() {
        Response<PostLoansLoanIdTransactionsResponse> response = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        ErrorResponse errorDetails = ErrorResponse.from(response);

        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.transactionDateInFutureFailureMsg());
    }

    @Then("Amounts are distributed equally in loan repayment schedule in case of total amount {double}")
    public void amountsEquallyDistributedInSchedule(double totalAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId1 = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> getLoansLoanIdResponseCall = loansApi
                .retrieveLoan(loanId1, false, "all", "guarantors,futureSchedule", "").execute();
        ErrorHelper.checkSuccessfulApiCall(getLoansLoanIdResponseCall);

        List<GetLoansLoanIdRepaymentPeriod> periods = getLoansLoanIdResponseCall.body().getRepaymentSchedule().getPeriods();

        BigDecimal expectedAmount = new BigDecimal(totalAmount / (periods.size() - 1)).setScale(0, RoundingMode.HALF_DOWN);
        BigDecimal lastExpectedAmount = new BigDecimal(totalAmount).setScale(0, RoundingMode.HALF_DOWN);

        for (int i = 1; i < periods.size(); i++) {
            BigDecimal actualAmount = new BigDecimal(periods.get(i).getPrincipalOriginalDue()).setScale(0, RoundingMode.HALF_DOWN);

            if (i == periods.size() - 1) {
                assertThat(actualAmount.compareTo(lastExpectedAmount))
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedule(i, actualAmount, lastExpectedAmount)).isEqualTo(0);
            } else {
                assertThat(actualAmount.compareTo(expectedAmount))
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedule(i, actualAmount, expectedAmount)).isEqualTo(0);
                lastExpectedAmount = lastExpectedAmount.subtract(actualAmount);
            }
        }
    }

    @When("Customer adjust {string}th repayment on {string} with amount {string} and check external owner")
    public void adjustNthRepaymentWithExternalOwner(String nthItemStr, String transactionDate, String amount) throws IOException {
        String transferExternalOwnerId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        adjustNthRepaymentWithExternalOwnerCheck(nthItemStr, transactionDate, amount, transferExternalOwnerId);
    }

    @When("Customer adjust {string}th repayment on {string} with amount {string}")
    public void adjustNthRepayment(String nthItemStr, String transactionDate, String amount) throws IOException {
        adjustNthRepaymentWithExternalOwnerCheck(nthItemStr, transactionDate, amount, null);
    }

    private void adjustNthRepaymentWithExternalOwnerCheck(String nthItemStr, String transactionDate, String amount, String externalOwnerId)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        List<GetLoansLoanIdTransactions> transactions = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> Boolean.TRUE.equals(t.getType().getRepayment()))
                .toList().get(nthItem);
        double amountValue = Double.parseDouble(amount);
        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentAdjustRequest(amountValue)
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentAdjustmentResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, targetTransaction.getId(), repaymentUndoRequest, "").execute();
        ErrorHelper.checkSuccessfulApiCall(repaymentAdjustmentResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentAdjustmentResponse);

        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(targetTransaction.getId());
        eventAssertionBuilder.extractingData(
                loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getAmount().doubleValue())
                .isEqualTo(targetTransaction.getAmount());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getExternalOwnerId())
                .isEqualTo(externalOwnerId);
        if (amountValue > 0) {
            eventAssertionBuilder
                    .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getNewTransactionDetail().getId())
                    .isEqualTo(repaymentAdjustmentResponse.body().getResourceId());
            eventAssertionBuilder.extractingData(
                    loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getNewTransactionDetail().getAmount().doubleValue())
                    .isEqualTo(amountValue);
            eventAssertionBuilder.extractingData(
                    loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getNewTransactionDetail().getExternalOwnerId())
                    .isEqualTo(externalOwnerId);
        }

    }
}
