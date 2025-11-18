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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.apache.fineract.test.data.paymenttype.DefaultPaymentType.AUTOPAY;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanRepaymentStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final String DEFAULT_ACCOUNT_NB = "1234567890";
    public static final String DEFAULT_CHECK_NB = "1234567890";
    public static final String DEFAULT_RECEIPT_NB = "1234567890";
    public static final String DEFAULT_BANK_NB = "1234567890";
    public static final String DEFAULT_REPAYMENT_TYPE = "AUTOPAY";
    private static final String PWD_USER_WITH_ROLE = "1234567890Aa!";

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private PaymentTypeResolver paymentTypeResolver;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private org.apache.fineract.test.api.ApiProperties apiProperties;

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
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);

        PostLoansLoanIdTransactionsResponse repaymentResponse = ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                repaymentRequest, Map.<String, Object>of("command", "repayment")));
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEvent = eventCheckHelper
                .transactionEventCheck(repaymentResponse, TransactionType.REPAYMENT, transferExternalOwnerId);
        testContext().set(TestContextKey.TRANSACTION_EVENT, transactionEvent);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Created user makes {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentWithGivenUser(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);

        PostUsersResponse createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.getResourceId();
        GetUsersUserIdResponse user = ok(() -> fineractClient.users().retrieveOne31(createdUserId));

        String apiBaseUrl = apiProperties.getBaseUrl() + "/fineract-provider/api/";
        FineractFeignClient userClient = FineractFeignClient.builder().baseUrl(apiBaseUrl)
                .credentials(user.getUsername(), PWD_USER_WITH_ROLE).tenantId(apiProperties.getTenantId()).disableSslVerification(true)
                .readTimeout((int) apiProperties.getReadTimeout(), java.util.concurrent.TimeUnit.SECONDS).build();

        PostLoansLoanIdTransactionsResponse repaymentResponse = ok(() -> userClient.loanTransactions().executeLoanTransaction(loanId,
                repaymentRequest, Map.<String, Object>of("command", "repayment")));
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Customer makes externalID controlled {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentByExternalId(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String resourceExternalId = loanResponse.getResourceExternalId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);

        PostLoansLoanIdTransactionsResponse repaymentResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction1(resourceExternalId, repaymentRequest, Map.<String, Object>of("command", "repayment")));

        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Created user makes externalID controlled {string} repayment on {string} with {double} EUR transaction amount")
    public void makeRepaymentWithGivenUserByExternalId(String repaymentType, String transactionDate, double transactionAmount)
            throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String resourceExternalId = loanResponse.getResourceExternalId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);

        PostUsersResponse createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.getResourceId();
        GetUsersUserIdResponse user = ok(() -> fineractClient.users().retrieveOne31(createdUserId));

        String apiBaseUrl = apiProperties.getBaseUrl() + "/fineract-provider/api/";
        FineractFeignClient userClient = FineractFeignClient.builder().baseUrl(apiBaseUrl)
                .credentials(user.getUsername(), PWD_USER_WITH_ROLE).tenantId(apiProperties.getTenantId()).disableSslVerification(true)
                .readTimeout((int) apiProperties.getReadTimeout(), java.util.concurrent.TimeUnit.SECONDS).build();

        PostLoansLoanIdTransactionsResponse repaymentResponse = ok(() -> userClient.loanTransactions()
                .executeLoanTransaction1(resourceExternalId, repaymentRequest, Map.<String, Object>of("command", "repayment")));
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, repaymentResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @And("Customer not able to make {string} repayment on {string} with {double} EUR transaction amount")
    public void makeLoanRepaymentFails(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        try {
            ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, repaymentRequest,
                    Map.<String, Object>of("command", "repayment")));
            throw new IllegalStateException("Expected FeignException but call succeeded");
        } catch (feign.FeignException e) {
            ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
            assertThat(errorDetails.getSingleError().getDeveloperMessage())
                    .isEqualTo(ErrorMessageHelper.loanRepaymentOnClosedLoanFailureMsg());
        }
    }

    @Then("Customer not able to make a repayment undo on {string} due to charge off")
    public void makeLoanRepaymentUndoAfterChargeOff(String transactionDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        PostLoansLoanIdTransactionsResponse transactionResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);
        Long loanId = loanResponse.getLoanId();
        Long transactionId = transactionResponse.getResourceId();

        PostLoansLoanIdTransactionsResponse repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        try {
            ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, repaymentResponse.getResourceId(),
                    repaymentUndoRequest, Map.<String, Object>of()));
            throw new IllegalStateException("Expected FeignException but call succeeded");
        } catch (feign.FeignException e) {
            ErrorResponse errorDetails = ErrorResponse.fromFeignException(e);
            assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.repaymentUndoFailureDueToChargeOffCodeMsg()).isEqualTo(403);
            assertThat(errorDetails.getSingleError().getDeveloperMessage())
                    .isEqualTo(ErrorMessageHelper.repaymentUndoFailureDueToChargeOff(transactionId));
        }
    }

    @And("Customer makes {string} repayment on {string} with {double} EUR transaction amount \\(and transaction fails because of wrong date)")
    public void makeLoanRepaymentWithWrongDate(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, repaymentRequest,
                Map.<String, Object>of("command", "repayment")));
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, null);
        testContext().set(TestContextKey.ERROR_RESPONSE, exception);
    }

    @When("Refund happens on {string} with {double} EUR transaction amount")
    public void makeRefund(String transactionDate, double transactionAmount) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdTransactionsRequest refundRequest = LoanRequestFactory.defaultRefundRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeResolver.resolve(AUTOPAY)).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE).accountNumber(DEFAULT_ACCOUNT_NB).checkNumber(DEFAULT_CHECK_NB).receiptNumber(DEFAULT_RECEIPT_NB)
                .bankNumber(DEFAULT_BANK_NB);

        PostLoansLoanIdTransactionsResponse refundResponse = ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                refundRequest, Map.<String, Object>of("command", "payoutRefund")));
        testContext().set(TestContextKey.LOAN_REFUND_RESPONSE, refundResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Refund undo happens on {string}")
    public void makeRefundUndo(String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdTransactionsResponse refundResponse = testContext().get(TestContextKey.LOAN_REFUND_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest refundUndoRequest = LoanRequestFactory.defaultRefundUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse refundUndoResponse = ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                refundResponse.getResourceId(), refundUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, refundUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, refundResponse.getResourceId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(refundResponse.getResourceId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Customer makes a repayment undo on {string}")
    public void makeLoanRepaymentUndo(String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdTransactionsResponse repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse repaymentUndoResponse = ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                repaymentResponse.getResourceId(), repaymentUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentUndoResponse);
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, repaymentResponse.getResourceId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(repaymentResponse.getResourceId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @Then("Loan {string} transaction adjust amount {double} must return {int} code")
    public void makeLoanRepaymentAdjustFail(String transactionType, double transactionAmount, int codeExpected) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdTransactionsResponse repaymentResponse = testContext().get(transactionType);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionAmount(transactionAmount);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                repaymentResponse.getResourceId(), repaymentUndoRequest, Map.<String, Object>of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(codeExpected);
        assertThat(exception.getDeveloperMessage()).isNotEmpty();
    }

    @When("Customer undo {string}th repayment on {string}")
    public void undoNthRepayment(String nthItemStr, String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> Boolean.TRUE.equals(t.getType().getRepayment()))
                .toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentUndoRequest()
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse repaymentUndoResponse = ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), repaymentUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentUndoResponse);
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Customer undo {string}th capitalized income adjustment on {string}")
    public void undoNthCapitalizedIncomeAdjustment(String nthItemStr, String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream()
                .filter(t -> Boolean.TRUE.equals(t.getType().getCapitalizedIncomeAdjustment())).toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest capitalizedIncomeUndoRequest = LoanRequestFactory
                .defaultCapitalizedIncomeAdjustmentUndoRequest().transactionDate(transactionDate);

        PostLoansLoanIdTransactionsResponse capitalizedIncomeUndoResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, targetTransaction.getId(), capitalizedIncomeUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_UNDO_RESPONSE, capitalizedIncomeUndoResponse);
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Customer undo {string}th transaction made on {string}")
    public void undoNthTransaction(String nthItemStr, String transactionDate) throws IOException {
        eventStore.reset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))).toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        PostLoansLoanIdTransactionsResponse transactionUndoResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);

        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Customer undo {string}th {string} transaction made on {string}")
    public void undoNthTransactionType(String nthItemStr, String transactionType, String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.getNthTransactionType(nthItemStr, transactionType, transactionDate,
                transactions);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        PostLoansLoanIdTransactionsResponse transactionUndoResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @Then("Customer is forbidden to undo {string}th {string} transaction made on {string}")
    public void makeTransactionUndoForbidden(String nthItemStr, String transactionType, String transactionDate) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.findNthTransaction(nthItemStr, transactionType, transactionDate,
                loanId);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));

        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains("Interest refund transaction")
                .contains("cannot be reversed or adjusted directly");
    }

    public void checkMakeTransactionForbidden(feign.FeignException e, Integer httpStatusCodeExpected, String developerMessageExpected)
            throws IOException {
        ErrorResponse errorResponse = ErrorResponse.fromFeignException(e);
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        String developerMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(httpStatusCodeActual)
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(developerMessageActual)
                .as(ErrorMessageHelper.wrongErrorMessageInFailedChargeAdjustment(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);

        log.debug("Error code: {}", httpStatusCodeActual);
        log.debug("Error message: {}", developerMessageActual);
    }

    @Then("Customer is forbidden to undo {string}th {string} transaction made on {string} due to transaction type is non-reversal")
    public void makeTransactionUndoForbiddenNonReversal(String nthItemStr, String transactionType, String transactionDate)
            throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.findNthTransaction(nthItemStr, transactionType, transactionDate,
                loanId);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));

        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.addCapitalizedIncomeUndoFailureTransactionTypeNonReversal());
    }

    @Then("Customer is forbidden to undo {string}th {string} transaction made on {string} due to adjustment exists")
    public void makeTransactionUndoForbiddenAdjustmentExiists(String nthItemStr, String transactionType, String transactionDate)
            throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.findNthTransaction(nthItemStr, transactionType, transactionDate,
                loanId);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));

        assertThat(exception.getStatus()).isEqualTo(403);
        if (transactionType.equals("Buy Down Fee")) {
            assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.buyDownFeeUndoFailureAdjustmentExists());
        } else if (transactionType.equals("Capitalized Income")) {
            assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addCapitalizedIncomeUndoFailureAdjustmentExists());
        }
    }

    @When("Customer undo {string}th {string} transaction made on {string} with linked {string} transaction")
    public void checkNthTransactionType(String nthItemStr, String transactionType, String transactionDate, String linkedTransactionType)
            throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        // check that here are 2 transactions - target and linked
        assertThat(transactions.size()).isGreaterThanOrEqualTo(2);

        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.getNthTransactionType(nthItemStr, transactionType, transactionDate,
                transactions);
        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);
        PostLoansLoanIdTransactionsResponse transactionUndoResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);

        // linked transaction
        GetLoansLoanIdTransactions linkedTargetTransaction = eventCheckHelper.getNthTransactionType(nthItemStr, linkedTransactionType,
                transactionDate, transactions);
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(linkedTargetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @Then("Repayment transaction is created with {double} amount and {string} type")
    public void loanRepaymentStatus(double repaymentAmount, String paymentType) throws IOException {
        PostLoansLoanIdTransactionsResponse repaymentResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdTransactionsTransactionIdResponse transactionResponse = ok(() -> fineractClient.loanTransactions()
                .retrieveTransaction(loanId, repaymentResponse.getResourceId(), Map.<String, Object>of()));
        assertThat(transactionResponse.getAmount()).isEqualTo(repaymentAmount);
        assertThat(transactionResponse.getPaymentDetailData().getPaymentType().getName()).isEqualTo(paymentType);
    }

    @Then("Repayment failed because the repayment date is after the business date")
    public void repaymentDateFailure() {
        CallFailedRuntimeException exception = testContext().get(TestContextKey.ERROR_RESPONSE);
        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains("transaction date cannot be in the future");
    }

    @Then("Amounts are distributed equally in loan repayment schedule in case of total amount {double}")
    public void amountsEquallyDistributedInSchedule(double totalAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId1 = loanResponse.getLoanId();

        GetLoansLoanIdResponse getLoansLoanIdResponseCall = ok(() -> fineractClient.loans().retrieveLoan(loanId1,
                Map.<String, Object>of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "guarantors,futureSchedule")));

        List<GetLoansLoanIdRepaymentPeriod> periods = getLoansLoanIdResponseCall.getRepaymentSchedule().getPeriods();

        BigDecimal expectedAmount = new BigDecimal(totalAmount / (periods.size() - 1)).setScale(0, RoundingMode.HALF_DOWN);
        BigDecimal lastExpectedAmount = new BigDecimal(totalAmount).setScale(0, RoundingMode.HALF_DOWN);

        for (int i = 1; i < periods.size(); i++) {
            BigDecimal actualAmount = periods.get(i).getPrincipalOriginalDue().setScale(0, RoundingMode.HALF_DOWN);

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

    @When("Loan Pay-off is made on {string}")
    public void makeLoanPayOff(String transactionDate) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId1 = loanResponse.getLoanId();
        GetLoansLoanIdTransactionsTemplateResponse response = ok(
                () -> fineractClient.loanTransactions().retrieveTransactionTemplate(loanId1, Map.<String, Object>of("command", "prepayLoan",
                        "dateFormat", DATE_FORMAT, "transactionDate", transactionDate, "locale", DEFAULT_LOCALE)));
        Double transactionAmount = response.getAmount();

        log.debug("%n--- Loan Pay-off with amount: {} ---", transactionAmount);
        makeRepayment(DEFAULT_REPAYMENT_TYPE, transactionDate, transactionAmount, null);
    }

    private void adjustNthRepaymentWithExternalOwnerCheck(String nthItemStr, String transactionDate, String amount, String externalOwnerId)
            throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.<String, Object>of("associations", "transactions")))
                .getTransactions();

        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> Boolean.TRUE.equals(t.getType().getRepayment()))
                .toList().get(nthItem);
        double amountValue = Double.parseDouble(amount);
        PostLoansLoanIdTransactionsTransactionIdRequest repaymentUndoRequest = LoanRequestFactory.defaultRepaymentAdjustRequest(amountValue)
                .transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse repaymentAdjustmentResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, targetTransaction.getId(), repaymentUndoRequest, Map.<String, Object>of()));
        testContext().set(TestContextKey.LOAN_REPAYMENT_UNDO_RESPONSE, repaymentAdjustmentResponse);

        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, targetTransaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(targetTransaction.getId());
        eventAssertionBuilder
                .extractingBigDecimal(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getAmount())
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
                    .isEqualTo(repaymentAdjustmentResponse.getResourceId());
            eventAssertionBuilder
                    .extractingBigDecimal(
                            loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getNewTransactionDetail().getAmount())
                    .isEqualTo(BigDecimal.valueOf(amountValue));
            eventAssertionBuilder.extractingData(
                    loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getNewTransactionDetail().getExternalOwnerId())
                    .isEqualTo(externalOwnerId);
        }

    }
}
