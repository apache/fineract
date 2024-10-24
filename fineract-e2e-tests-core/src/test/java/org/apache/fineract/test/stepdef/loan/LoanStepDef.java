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

import static org.apache.fineract.test.data.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanChargePaidByDataV1;
import org.apache.fineract.avro.loan.v1.LoanStatusEnumDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargePaidByData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTermVariations;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTimeline;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.IsCatchUpRunningResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.client.services.LoanCobCatchUpApi;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.AmortizationType;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.InterestType;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.LoanTermFrequencyType;
import org.apache.fineract.test.data.RepaymentFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.initializer.global.LoanProductGlobalInitializerStep;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.LoanStatusChangedEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargeOffEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargeOffUndoEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_FORMAT_EVENTS = "yyyy-MM-dd";
    public static final String DEFAULT_LOCALE = "en";
    public static final String LOAN_STATE_SUBMITTED_AND_PENDING = "Submitted and pending approval";
    public static final String LOAN_STATE_APPROVED = "Approved";
    public static final String LOAN_STATE_ACTIVE = "Active";
    private static final Gson GSON = new JSON().getGson();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern(DATE_FORMAT_EVENTS);

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private LoanCobCatchUpApi loanCobCatchUpApi;

    @Autowired
    private LoanTransactionsApi loanTransactionsApi;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private PaymentTypeResolver paymentTypeResolver;

    @Autowired
    private LoanProductResolver loanProductResolver;

    @Autowired
    private LoanRequestFactory loanRequestFactory;

    @Autowired
    private EventCheckHelper eventCheckHelper;

    @Autowired
    private LoanProductsApi loanProductsApi;

    @When("Admin creates a new Loan")
    public void createLoan() throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new default Loan with date: {string}")
    public void createLoanWithDate(String date) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin crates a second default loan with date: {string}")
    public void createSecondLoanWithDate(String date) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin crates a second default loan for the second client with date: {string}")
    public void createSecondLoanForSecondClientWithDate(String date) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    /**
     * Use this where inline COB run needed - this way we don't have to run inline COB for all 30 days of loan term, but
     * only 1 day
     */
    @When("Admin creates a new Loan with date: {string} and with 1 day loan term and repayment")
    public void createLoanWithDateShortTerm(String date) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .submittedOnDate(date)//
                .expectedDisbursementDate(date)//
                .loanTermFrequency(1)//
                .repaymentEvery(1);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @When("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and self-generated Idempotency key")
    public void createTransactionWithIdempotencyKey(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws IOException {
        createTransactionWithIdempotencyKeyAndExternalOwnerCheck(transactionTypeInput, transactionPaymentType, transactionDate,
                transactionAmount, null);
    }

    @When("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and self-generated Idempotency key and check external owner")
    public void createTransactionWithIdempotencyKeyAndWithExternalOwner(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount) throws IOException {
        String transferExternalOwnerId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        createTransactionWithIdempotencyKeyAndExternalOwnerCheck(transactionTypeInput, transactionPaymentType, transactionDate,
                transactionAmount, transferExternalOwnerId);
    }

    private void createTransactionWithIdempotencyKeyAndExternalOwnerCheck(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount, String externalOwnerId) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue, headerMap).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);

        eventCheckHelper.transactionEventCheck(paymentTransactionResponse, transactionType, externalOwnerId);
    }

    @When("Admin makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount")
    public void createTransactionForRefund(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws IOException, InterruptedException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);

        eventCheckHelper.transactionEventCheck(paymentTransactionResponse, transactionType, null);
    }

    @When("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and system-generated Idempotency key")
    public void createTransactionWithAutoIdempotencyKey(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws IOException {
        createTransactionWithAutoIdempotencyKeyAndWithExternalOwner(transactionTypeInput, transactionPaymentType, transactionDate,
                transactionAmount, null);
    }

    @When("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and system-generated Idempotency key and check external owner")
    public void createTransactionWithAutoIdempotencyKeyWithExternalOwner(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount) throws IOException {
        String transferExternalOwnerId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        createTransactionWithAutoIdempotencyKeyAndWithExternalOwner(transactionTypeInput, transactionPaymentType, transactionDate,
                transactionAmount, transferExternalOwnerId);
    }

    private void createTransactionWithAutoIdempotencyKeyAndWithExternalOwner(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount, String externalOwnerId) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);

        eventCheckHelper.transactionEventCheck(paymentTransactionResponse, transactionType, externalOwnerId);
    }

    @When("Admin makes Credit Balance Refund transaction on {string} with {double} EUR transaction amount")
    public void createCBR(String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String transactionTypeValue = "creditBalanceRefund";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);
    }

    @Then("Credit Balance Refund transaction on future date {string} with {double} EUR transaction amount will result an error")
    public void futureDateCBRError(String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        int errorCodeExpected = 403;
        String errorMessageExpected = String.format("Loan: %s, Credit Balance Refund transaction cannot be created for the future.",
                loanId);

        String transactionTypeValue = "creditBalanceRefund";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);

        int errorCodeActual = paymentTransactionResponse.code();
        String errorBody = paymentTransactionResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.info("ERROR CODE: {}", errorCodeActual);
        log.info("ERROR MESSAGE: {}", errorMessageActual);
    }

    @When("Admin creates a fully customized loan with the following data:")
    public void createFullyCustomizedLoan(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createCustomizedLoan(data.get(1), false);
    }

    @When("Admin creates a fully customized loan with emi and the following data:")
    public void createFullyCustomizedLoanWithEmi(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createCustomizedLoan(data.get(1), true);
    }

    @When("Admin creates a fully customized loan with fixed length {int} and with the following data:")
    public void createFullyCustomizedLoanFixedLength(int fixedLength, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestType = loanData.get(4);
        String interestCalculationPeriod = loanData.get(5);
        String amortizationType = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyType = loanData.get(10);
        Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        String transactionProcessingStrategyCode = loanData.get(15);

        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();

        DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        Long loanProductId = loanProductResolver.resolve(product);

        LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        RepaymentFrequencyType repaymentFrequencyType1 = RepaymentFrequencyType.valueOf(repaymentFrequencyType);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType1.getValue();

        InterestType interestType1 = InterestType.valueOf(interestType);
        Integer interestTypeValue = interestType1.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod1 = InterestCalculationPeriodTime.valueOf(interestCalculationPeriod);
        Integer interestCalculationPeriodValue = interestCalculationPeriod1.getValue();

        AmortizationType amortizationType1 = AmortizationType.valueOf(amortizationType);
        Integer amortizationTypeValue = amortizationType1.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .interestType(interestTypeValue)//
                .interestCalculationPeriodType(interestCalculationPeriodValue)//
                .amortizationType(amortizationTypeValue)//
                .loanTermFrequency(loanTermFrequency)//
                .loanTermFrequencyType(loanTermFrequencyTypeValue)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(repaymentFrequency)//
                .repaymentFrequencyType(repaymentFrequencyTypeValue)//
                .submittedOnDate(submitDate)//
                .expectedDisbursementDate(submitDate)//
                .graceOnPrincipalPayment(graceOnPrincipalPayment)//
                .graceOnInterestPayment(graceOnInterestPayment)//
                .graceOnInterestPayment(graceOnInterestCharged).transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .fixedLength(fixedLength);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a fully customized loan with Advanced payment allocation and with product no Advanced payment allocation set results an error:")
    public void createFullyCustomizedLoanNoAdvancedPaymentError(DataTable table) throws IOException {
        int errorCodeExpected = 403;
        String errorMessageExpected = "Loan transaction processing strategy cannot be Advanced Payment Allocation Strategy if it's not configured on loan product";

        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestType = loanData.get(4);
        String interestCalculationPeriod = loanData.get(5);
        String amortizationType = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyType = loanData.get(10);
        Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        String transactionProcessingStrategyCode = loanData.get(15);

        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();

        DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        Long loanProductId = loanProductResolver.resolve(product);

        LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        RepaymentFrequencyType repaymentFrequencyType1 = RepaymentFrequencyType.valueOf(repaymentFrequencyType);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType1.getValue();

        InterestType interestType1 = InterestType.valueOf(interestType);
        Integer interestTypeValue = interestType1.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod1 = InterestCalculationPeriodTime.valueOf(interestCalculationPeriod);
        Integer interestCalculationPeriodValue = interestCalculationPeriod1.getValue();

        AmortizationType amortizationType1 = AmortizationType.valueOf(amortizationType);
        Integer amortizationTypeValue = amortizationType1.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .interestType(interestTypeValue)//
                .interestCalculationPeriodType(interestCalculationPeriodValue)//
                .amortizationType(amortizationTypeValue)//
                .loanTermFrequency(loanTermFrequency)//
                .loanTermFrequencyType(loanTermFrequencyTypeValue)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(repaymentFrequency)//
                .repaymentFrequencyType(repaymentFrequencyTypeValue)//
                .submittedOnDate(submitDate)//
                .expectedDisbursementDate(submitDate)//
                .graceOnPrincipalPayment(graceOnPrincipalPayment)//
                .graceOnInterestPayment(graceOnInterestPayment)//
                .graceOnInterestPayment(graceOnInterestCharged).transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        int errorCodeActual = response.code();
        String errorBody = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.info("ERROR CODE: {}", errorCodeActual);
        log.info("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("Loan details has the following last payment related data:")
    public void checkLastPaymentData(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String lastPaymentAmountExpected = expectedValues.get(0);
        String lastPaymentDateExpected = expectedValues.get(1);
        String lastRepaymentAmountExpected = expectedValues.get(2);
        String lastRepaymentDateExpected = expectedValues.get(3);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "collection", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        GetLoansLoanIdDelinquencySummary delinquent = loanDetailsResponse.body().getDelinquent();
        String lastPaymentAmountActual = String.valueOf(delinquent.getLastPaymentAmount());
        String lastPaymentDateActual = FORMATTER.format(delinquent.getLastPaymentDate());
        String lastRepaymentAmountActual = String.valueOf(delinquent.getLastRepaymentAmount());
        String lastRepaymentDateActual = FORMATTER.format(delinquent.getLastRepaymentDate());

        assertThat(lastPaymentAmountActual)
                .as(ErrorMessageHelper.wrongDataInLastPaymentAmount(lastPaymentAmountActual, lastPaymentAmountExpected))
                .isEqualTo(lastPaymentAmountExpected);
        assertThat(lastPaymentDateActual).as(ErrorMessageHelper.wrongDataInLastPaymentDate(lastPaymentDateActual, lastPaymentDateExpected))
                .isEqualTo(lastPaymentDateExpected);
        assertThat(lastRepaymentAmountActual)
                .as(ErrorMessageHelper.wrongDataInLastRepaymentAmount(lastRepaymentAmountActual, lastRepaymentAmountExpected))
                .isEqualTo(lastRepaymentAmountExpected);
        assertThat(lastRepaymentDateActual)
                .as(ErrorMessageHelper.wrongDataInLastRepaymentDate(lastRepaymentDateActual, lastRepaymentDateExpected))
                .isEqualTo(lastRepaymentDateExpected);
    }

    @Then("Loan details and LoanTransactionMakeRepaymentPostBusinessEvent has the following data in loanChargePaidByList section:")
    public void checkLoanDetailsAndEventLoanChargePaidByListSection(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions lastRepaymentData = transactions.stream()
                .filter(t -> "loanTransactionType.repayment".equals(t.getType().getCode())).reduce((first, second) -> second).orElse(null);
        List<GetLoansLoanIdLoanChargePaidByData> loanChargePaidByList = lastRepaymentData.getLoanChargePaidByList();
        loanChargePaidByList.sort(Comparator.comparing(GetLoansLoanIdLoanChargePaidByData::getChargeId));

        EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEvent = testContext().get(TestContextKey.TRANSACTION_EVENT);
        transactionEvent.extractingData(loanTransactionDataV1 -> {
            for (int i = 0; i < loanChargePaidByList.size(); i++) {
                List<LoanChargePaidByDataV1> loanChargePaidByListEvent = loanTransactionDataV1.getLoanChargePaidByList();
                loanChargePaidByListEvent.sort(Comparator.comparing(LoanChargePaidByDataV1::getChargeId));
                String amountEventActual = loanChargePaidByListEvent.get(i).getAmount().setScale(1, RoundingMode.HALF_DOWN).toString();
                String nameEventActual = loanChargePaidByListEvent.get(i).getName();

                String amountActual = String.valueOf(loanChargePaidByList.get(i).getAmount());
                String nameActual = loanChargePaidByList.get(i).getName();

                String amountExpected = data.get(i + 1).get(0);
                String nameExpected = data.get(i + 1).get(1);

                assertThat(amountActual)
                        .as(ErrorMessageHelper.wrongDataInLoanDetailsLoanChargePaidByListAmount(amountActual, amountExpected))
                        .isEqualTo(amountExpected);
                assertThat(nameActual).as(ErrorMessageHelper.wrongDataInLoanDetailsLoanChargePaidByListName(nameActual, nameExpected))
                        .isEqualTo(nameExpected);

                assertThat(amountEventActual).as(ErrorMessageHelper
                        .wrongDataInLoanTransactionMakeRepaymentPostEventLoanChargePaidByListAmount(amountEventActual, amountExpected))
                        .isEqualTo(amountExpected);
                assertThat(nameEventActual).as(ErrorMessageHelper
                        .wrongDataInLoanTransactionMakeRepaymentPostEventLoanChargePaidByListName(nameEventActual, nameExpected))
                        .isEqualTo(nameExpected);
            }
            return null;
        });
    }

    @And("Admin successfully creates a new customised Loan submitted on date: {string}, with Principal: {string}, a loanTermFrequency: {int} months, and numberOfRepayments: {int}")
    public void createCustomizedLoan(String submitDate, String principal, Integer loanTermFrequency, Integer numberOfRepayments)
            throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        Integer repaymentFrequency = loanTermFrequency / numberOfRepayments;

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).principal(new BigDecimal(principal))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)
                .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentFrequency)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value).submittedOnDate(submitDate)
                .expectedDisbursementDate(submitDate);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @And("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount with the same Idempotency key as previous transaction")
    public void createTransactionWithIdempotencyKeyOfPreviousTransaction(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = testContext().get(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue, headerMap).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);
    }

    @And("Customer makes {string} transaction on the second loan with {string} payment type on {string} with {double} EUR transaction amount with the same Idempotency key as previous transaction")
    public void createTransactionOnSecondLoanWithIdempotencyKeyOfPreviousTransaction(String transactionTypeInput,
            String transactionPaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        Map<String, String> headerMap = new HashMap<>();
        String idempotencyKey = testContext().get(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY);
        headerMap.put("Idempotency-Key", idempotencyKey);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue, headerMap).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);
    }

    @Then("Admin can successfully modify the loan and changes the submitted on date to {string}")
    public void modifyLoanSubmittedOnDate(String newSubmittedOnDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId2 = loanResponse.body().getResourceId();
        Long clientId2 = loanResponse.body().getClientId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = loanRequestFactory.modifySubmittedOnDateOnLoan(clientId2, newSubmittedOnDate);

        Response<PutLoansLoanIdResponse> responseMod = loansApi.modifyLoanApplication(loanId2, putLoansLoanIdRequest, "").execute();
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, responseMod);
        ErrorHelper.checkSuccessfulApiCall(responseMod);
    }

    @Then("Admin fails to create a new customised Loan submitted on date: {string}, with Principal: {string}, a loanTermFrequency: {int} months, and numberOfRepayments: {int}")
    public void createCustomizedLoanFailure(String submitDate, String principal, Integer loanTermFrequency, Integer numberOfRepayments)
            throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.body().getClientId();
        Integer repaymentFrequency = loanTermFrequency / numberOfRepayments;

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).principal(new BigDecimal(principal))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)
                .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentFrequency)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value).submittedOnDate(submitDate)
                .expectedDisbursementDate(submitDate);

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorResponse errorDetails = ErrorResponse.from(response);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.loanSubmitDateInFutureFailureMsg());
    }

    @And("Admin successfully approves the loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveLoan(String approveDate, String approvedAmount, String expectedDisbursementDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        Response<PostLoansLoanIdResponse> loanApproveResponse = loansApi.stateTransitions(loanId, approveRequest, "approve").execute();
        testContext().set(TestContextKey.LOAN_APPROVAL_RESPONSE, loanApproveResponse);
        ErrorHelper.checkSuccessfulApiCall(loanApproveResponse);
        assertThat(loanApproveResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
        assertThat(loanApproveResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);

        eventCheckHelper.approveLoanEventCheck(loanApproveResponse);
    }

    @And("Admin successfully approves the second loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveSecondLoan(String approveDate, String approvedAmount, String expectedDisbursementDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        Response<PostLoansLoanIdResponse> loanApproveResponse = loansApi.stateTransitions(loanId, approveRequest, "approve").execute();
        testContext().set(TestContextKey.LOAN_APPROVAL_SECOND_LOAN_RESPONSE, loanApproveResponse);
        ErrorHelper.checkSuccessfulApiCall(loanApproveResponse);
        assertThat(loanApproveResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
        assertThat(loanApproveResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
    }

    @Then("Admin can successfully undone the loan approval")
    public void undoLoanApproval() throws IOException {
        Response<PostLoansLoanIdResponse> loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.body().getLoanId();
        PostLoansLoanIdRequest undoApprovalRequest = new PostLoansLoanIdRequest().note("");

        Response<PostLoansLoanIdResponse> undoApprovalResponse = loansApi.stateTransitions(loanId, undoApprovalRequest, "undoapproval")
                .execute();
        testContext().set(TestContextKey.LOAN_UNDO_APPROVAL_RESPONSE, loanApproveResponse);
        ErrorHelper.checkSuccessfulApiCall(undoApprovalResponse);
        assertThat(undoApprovalResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_SUBMITTED_AND_PENDING);
    }

    @Then("Admin fails to approve the loan on {string} with {string} amount and expected disbursement date on {string} because of wrong date")
    public void failedLoanApproveWithDate(String approveDate, String approvedAmount, String expectedDisbursementDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        Response<PostLoansLoanIdResponse> loanApproveResponse = loansApi.stateTransitions(loanId, approveRequest, "approve").execute();
        ErrorResponse errorDetails = ErrorResponse.from(loanApproveResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.loanApproveDateInFutureFailureMsg());
    }

    @Then("Admin fails to approve the loan on {string} with {string} amount and expected disbursement date on {string} because of wrong amount")
    public void failedLoanApproveWithAmount(String approveDate, String approvedAmount, String expectedDisbursementDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        Response<PostLoansLoanIdResponse> loanApproveResponse = loansApi.stateTransitions(loanId, approveRequest, "approve").execute();
        ErrorResponse errorDetails = ErrorResponse.from(loanApproveResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.loanApproveMaxAmountFailureMsg());
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount")
    public void disburseLoan(String actualDisbursementDate, String transactionAmount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse.body());
        final long loanId = loanResponse.body().getLoanId();
        final PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));
        performLoanDisbursementAndVerifyStatus(loanId, disburseRequest);
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount and {string} fixed emi amount")
    public void disburseLoanWithFixedEmiAmount(final String actualDisbursementDate, final String transactionAmount,
            final String fixedEmiAmount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse.body());
        final long loanId = loanResponse.body().getLoanId();
        final PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount))
                .fixedEmiAmount(new BigDecimal(fixedEmiAmount));
        performLoanDisbursementAndVerifyStatus(loanId, disburseRequest);
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount, {string} EUR fixed emi amount and adjust repayment date on {string}")
    public void disburseLoanWithFixedEmiAmountAndAdjustRepaymentDate(final String actualDisbursementDate, final String transactionAmount,
            final String fixedEmiAmount, final String adjustRepaymentDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse.body());
        final long loanId = loanResponse.body().getLoanId();
        final PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount))
                .fixedEmiAmount(new BigDecimal(fixedEmiAmount)).adjustRepaymentDate(adjustRepaymentDate);
        performLoanDisbursementAndVerifyStatus(loanId, disburseRequest);
    }

    @And("Admin successfully disburse the second loan on {string} with {string} EUR transaction amount")
    public void disburseSecondLoan(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_SECOND_LOAN_RESPONSE, loanDisburseResponse);
        ErrorHelper.checkSuccessfulApiCall(loanDisburseResponse);
        assertThat(loanDisburseResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_ACTIVE);

        eventCheckHelper.disburseLoanEventCheck(loanDisburseResponse);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    @And("Admin does charge-off the loan on {string}")
    public void chargeOffLoan(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        ErrorHelper.checkSuccessfulApiCall(chargeOffResponse);

        Long transactionId = chargeOffResponse.body().getResourceId();
        eventAssertion.assertEvent(LoanChargeOffEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId).isEqualTo(chargeOffResponse.body().getResourceId());
    }

    @Then("Charge-off attempt on {string} results an error")
    public void chargeOffOnLoanWithInterestFails(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);

        assertThat(chargeOffResponse.isSuccessful()).isFalse();

        String string = chargeOffResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(string, ErrorResponse.class);
        String developerMessage = errorResponse.getErrors().get(0).getDeveloperMessage();
        assertThat(developerMessage)
                .isEqualTo(String.format("Loan: %s Charge-off is not allowed. Loan Account is interest bearing", loanId));
    }

    @Then("Second Charge-off is not possible on {string}")
    public void secondChargeOffLoan(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> secondChargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, secondChargeOffResponse);
        ErrorResponse errorDetails = ErrorResponse.from(secondChargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.secondChargeOffFailure(loanId));
    }

    @And("Admin does a charge-off undo the loan")
    public void chargeOffUndo() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffUndoResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffUndoRequest, "undo-charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE, chargeOffUndoResponse);
        ErrorHelper.checkSuccessfulApiCall(chargeOffUndoResponse);

        Long transactionId = chargeOffUndoResponse.body().getResourceId();
        eventAssertion.assertEventRaised(LoanChargeOffUndoEvent.class, transactionId);
    }

    @Then("Charge-off undo is not possible on {string}")
    public void chargeOffUndoFailure(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        ErrorResponse errorDetails = ErrorResponse.from(chargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.chargeOffUndoFailure(loanId));
    }

    @Then("Charge-off undo is not possible as the loan is not charged-off")
    public void chargeOffNotPossibleFailure() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultUndoChargeOffRequest();

        Response<PostLoansLoanIdTransactionsResponse> undoChargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "undo-charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, undoChargeOffResponse);
        ErrorResponse errorDetails = ErrorResponse.from(undoChargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.notChargedOffFailure(loanId));
    }

    @When("Admin successfully undo disbursal")
    public void undoDisbursal() throws IOException {
        Response<PostLoansLoanIdResponse> loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.body().getLoanId();

        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");
        Response<PostLoansLoanIdResponse> undoLastDisbursalResponse = loansApi
                .stateTransitions(loanId, undoDisbursalRequest, "undodisbursal").execute();
        ErrorHelper.checkSuccessfulApiCall(undoLastDisbursalResponse);
    }

    @When("Admin successfully undo last disbursal")
    public void undoLastDisbursal() throws IOException {
        Response<PostLoansLoanIdResponse> loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.body().getLoanId();

        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");
        Response<PostLoansLoanIdResponse> undoLastDisbursalResponse = loansApi
                .stateTransitions(loanId, undoDisbursalRequest, "undolastdisbursal").execute();
        ErrorHelper.checkSuccessfulApiCall(undoLastDisbursalResponse);
    }

    @Then("Admin can successfully undone the loan disbursal")
    public void checkUndoLoanDisbursal() throws IOException {
        Response<PostLoansLoanIdResponse> loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.body().getLoanId();
        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");

        Response<PostLoansLoanIdResponse> undoDisbursalResponse = loansApi.stateTransitions(loanId, undoDisbursalRequest, "undodisbursal")
                .execute();
        testContext().set(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, undoDisbursalResponse);
        ErrorHelper.checkSuccessfulApiCall(undoDisbursalResponse);
        assertThat(undoDisbursalResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of wrong date")
    public void disburseLoanFailureWithDate(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.disburseDateFailure((int) loanId));
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of wrong amount")
    public void disburseLoanFailureWithAmount(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        String developerMessage = errorDetails.getSingleError().getDeveloperMessage();

        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(developerMessage).matches(ErrorMessageHelper.disburseMaxAmountFailure());
        log.info("Error message: {}", developerMessage);
    }

    @Then("Loan has {double} outstanding amount")
    public void loanOutstanding(double totalOutstandingExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOutstandingActual = loanDetailsResponse.body().getSummary().getTotalOutstanding();
        assertThat(totalOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalOutstandingActual, totalOutstandingExpected))
                .isEqualTo(totalOutstandingExpected);
    }

    @Then("Loan has {double} overpaid amount")
    public void loanOverpaid(double totalOverpaidExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOverpaidActual = loanDetailsResponse.body().getTotalOverpaid();
        Double totalOutstandingActual = loanDetailsResponse.body().getSummary().getTotalOutstanding();
        double totalOutstandingExpected = 0.0;
        assertThat(totalOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalOutstandingActual, totalOutstandingExpected))
                .isEqualTo(totalOutstandingExpected);
        assertThat(totalOverpaidActual)
                .as(ErrorMessageHelper.wrongAmountInTransactionsOverpayment(totalOverpaidActual, totalOverpaidExpected))
                .isEqualTo(totalOverpaidExpected);
    }

    @Then("Loan has {double} total overdue amount")
    public void loanOverdue(double totalOverdueExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOverdueActual = loanDetailsResponse.body().getSummary().getTotalOverdue();
        assertThat(totalOverdueActual).as(ErrorMessageHelper.wrongAmountInTotalOverdue(totalOverdueActual, totalOverdueExpected))
                .isEqualTo(totalOverdueExpected);
    }

    @Then("Loan has {double} last payment amount")
    public void loanLastPaymentAmount(double lastPaymentAmountExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double lastPaymentAmountActual = loanDetailsResponse.body().getDelinquent().getLastPaymentAmount();
        assertThat(lastPaymentAmountActual)
                .as(ErrorMessageHelper.wrongLastPaymentAmount(lastPaymentAmountActual, lastPaymentAmountExpected))
                .isEqualTo(lastPaymentAmountExpected);
    }

    @Then("Loan Repayment schedule has {int} periods, with the following data for periods:")
    public void loanRepaymentSchedulePeriodsCheck(int linesExpected, DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "repaymentSchedule", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods = loanDetailsResponse.body().getRepaymentSchedule().getPeriods();

        List<List<String>> data = table.asLists();
        int nrLines = data.size();
        int linesActual = (int) repaymentPeriods.stream().filter(r -> r.getPeriod() != null).count();
        for (int i = 1; i < nrLines; i++) {
            List<String> expectedValues = data.get(i);
            String dueDateExpected = expectedValues.get(2);

            List<List<String>> actualValuesList = repaymentPeriods.stream()
                    .filter(r -> dueDateExpected.equals(FORMATTER.format(r.getDueDate())))
                    .map(r -> fetchValuesOfRepaymentSchedule(data.get(0), r)).collect(Collectors.toList());

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInRepaymentSchedule(i, actualValuesList, expectedValues)).isTrue();

            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInRepaymentSchedule(linesActual, linesExpected))
                    .isEqualTo(linesExpected);
        }
    }

    @Then("Loan Repayment schedule has the following data in Total row:")
    public void loanRepaymentScheduleAmountCheck(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> header = data.get(0);
        List<String> expectedValues = data.get(1);
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "repaymentSchedule", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        GetLoansLoanIdRepaymentSchedule repaymentSchedule = loanDetailsResponse.body().getRepaymentSchedule();
        validateRepaymentScheduleTotal(header, repaymentSchedule, expectedValues);
    }

    @Then("Loan Transactions tab has a transaction with date: {string}, and with the following data:")
    public void loanTransactionsTransactionWithGivenDateDataCheck(String date, DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);

        List<List<String>> actualValuesList = transactions.stream().filter(t -> date.equals(FORMATTER.format(t.getDate())))
                .map(t -> fetchValuesOfTransaction(data.get(0), t)).collect(Collectors.toList());
        boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

        assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(1, actualValuesList, expectedValues))
                .isTrue();
    }

    @Then("Loan Transactions tab has the following data:")
    public void loanTransactionsTabCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            List<List<String>> actualValuesList = transactions.stream()//
                    .map(t -> fetchValuesOfTransaction(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.get(i - 1).equals(expectedValues);//
            assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(i, actualValuesList, expectedValues))
                    .isTrue();
        }
        assertThat(transactions.size()).as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(transactions.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    @Then("In Loan Transactions the latest Transaction has Transaction type={string} and is reverted")
    public void loanTransactionsLatestTransactionReverted(String transactionType) throws IOException {
        loanTransactionsLatestTransactionReverted(null, transactionType);
    }

    @Then("In Loan Transactions the {string}th Transaction has Transaction type={string} and is reverted")
    public void loanTransactionsLatestTransactionReverted(String nthTransactionStr, String transactionType) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        int nthTransaction = nthTransactionStr == null ? transactions.size() - 1 : Integer.parseInt(nthTransactionStr) - 1;
        GetLoansLoanIdTransactions latestTransaction = transactions.get(nthTransaction);

        String transactionTypeActual = latestTransaction.getType().getValue();
        Boolean isReversedActual = latestTransaction.getManuallyReversed();

        assertThat(transactionTypeActual)
                .as(ErrorMessageHelper.wrongDataInTransactionsTransactionType(transactionTypeActual, transactionType))
                .isEqualTo(transactionType);
        assertThat(isReversedActual).as(ErrorMessageHelper.transactionIsNotReversedError(isReversedActual, true)).isEqualTo(true);
    }

    @Then("On Loan Transactions tab the {string} Transaction with date {string} is reverted")
    public void loanTransactionsGivenTransactionReverted(String transactionType, String transactionDate) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        List<GetLoansLoanIdTransactions> transactionsMatch = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .collect(Collectors.toList());//
        boolean isReverted = transactionsMatch.stream().anyMatch(t -> t.getManuallyReversed());

        assertThat(isReverted).as(ErrorMessageHelper.transactionIsNotReversedError(isReverted, true)).isEqualTo(true);
    }

    @Then("On Loan Transactions tab the {string} Transaction with date {string} is NOT reverted")
    public void loanTransactionsGivenTransactionNotReverted(String transactionType, String transactionDate) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        List<GetLoansLoanIdTransactions> transactionsMatch = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .collect(Collectors.toList());//
        boolean isReverted = transactionsMatch.stream().anyMatch(t -> t.getManuallyReversed());

        assertThat(isReverted).as(ErrorMessageHelper.transactionIsNotReversedError(isReverted, false)).isEqualTo(false);
    }

    @Then("Loan Charges tab has a given charge with the following data:")
    public void loanChargesGivenChargeDataCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "charges", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.body().getCharges();

        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String paymentDueAtExpected = expectedValues.get(2);
        String dueAsOfExpected = expectedValues.get(3);
        List<List<String>> actualValuesList = getActualValuesList(charges, paymentDueAtExpected, dueAsOfExpected);

        boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

        assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInLineInChargesTab(1, actualValuesList, expectedValues))
                .isTrue();
    }

    @Then("Loan Charges tab has the following data:")
    public void loanChargesTabCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "charges", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.body().getCharges();

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String paymentDueAtExpected = expectedValues.get(2);
            String dueAsOfExpected = expectedValues.get(3);
            List<List<String>> actualValuesList = getActualValuesList(charges, paymentDueAtExpected, dueAsOfExpected);

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

            assertThat(containsExpectedValues).as(ErrorMessageHelper.wrongValueInLineInChargesTab(i, actualValuesList, expectedValues))
                    .isTrue();
        }
    }

    private List<List<String>> getActualValuesList(List<GetLoansLoanIdLoanChargeData> charges, String paymentDueAtExpected,
            String dueAsOfExpected) {
        List<GetLoansLoanIdLoanChargeData> result;

        if (dueAsOfExpected != null) {
            result = charges.stream().filter(t -> {
                LocalDate dueDate = t.getDueDate();
                return dueDate != null && dueAsOfExpected.equals(FORMATTER.format(dueDate));
            }).collect(Collectors.toList());
        } else {
            result = charges.stream().filter(t -> paymentDueAtExpected.equals(t.getChargeTimeType().getValue()))
                    .collect(Collectors.toList());
        }

        return result.stream().map(t -> {
            List<String> actualValues = new ArrayList<>();
            actualValues.add(t.getName() == null ? null : t.getName());
            actualValues.add(String.valueOf(t.getPenalty() == null ? null : t.getPenalty()));
            actualValues.add(t.getChargeTimeType().getValue() == null ? null : t.getChargeTimeType().getValue());
            actualValues.add(t.getDueDate() == null ? null : FORMATTER.format(t.getDueDate()));
            actualValues.add(t.getChargeCalculationType().getValue() == null ? null : t.getChargeCalculationType().getValue());
            actualValues.add(t.getAmount() == null ? null : String.valueOf(t.getAmount()));
            actualValues.add(t.getAmountPaid() == null ? null : String.valueOf(t.getAmountPaid()));
            actualValues.add(t.getAmountWaived() == null ? null : String.valueOf(t.getAmountWaived()));
            actualValues.add(t.getAmountOutstanding() == null ? null : String.valueOf(t.getAmountOutstanding()));
            return actualValues;
        }).collect(Collectors.toList());
    }

    @Then("Loan status will be {string}")
    public void loanStatus(String statusExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        Integer loanStatusActualValue = loanDetailsResponse.body().getStatus().getId();

        LoanStatus loanStatusExpected = LoanStatus.valueOf(statusExpected);
        Integer loanStatusExpectedValue = loanStatusExpected.getValue();

        assertThat(loanStatusActualValue).as(ErrorMessageHelper.wrongLoanStatus(loanStatusActualValue, loanStatusExpectedValue))
                .isEqualTo(loanStatusExpectedValue);
    }

    @Then("Loan's all installments have obligations met")
    public void loanInstallmentsObligationsMet() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "repaymentSchedule", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods = loanDetailsResponse.body().getRepaymentSchedule().getPeriods();

        boolean allInstallmentsObligationsMet = repaymentPeriods.stream()
                .allMatch(t -> t.getDaysInPeriod() == null || t.getObligationsMetOnDate() != null);
        assertThat(allInstallmentsObligationsMet).isTrue();
    }

    @Then("Loan closedon_date is {}")
    public void loanClosedonDate(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        if (date == null || "null".equals(date)) {
            assertThat(loanDetailsResponse.body().getTimeline().getClosedOnDate()).isNull();
        } else {
            assertThat(loanDetailsResponse.body().getTimeline().getClosedOnDate()).isEqualTo(date);
        }
    }

    @Then("Admin can successfully set Fraud flag to the loan")
    public void setFraud() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.enableFraudFlag();

        Response<PutLoansLoanIdResponse> responseMod = loansApi.modifyLoanApplication(loanId, putLoansLoanIdRequest, "markAsFraud")
                .execute();
        testContext().set(TestContextKey.LOAN_FRAUD_MODIFY_RESPONSE, responseMod);

        ErrorHelper.checkSuccessfulApiCall(responseMod);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Boolean fraudFlagActual = loanDetailsResponse.body().getFraud();
        assertThat(fraudFlagActual).as(ErrorMessageHelper.wrongFraudFlag(fraudFlagActual, true)).isEqualTo(true);
    }

    @Then("Admin can successfully unset Fraud flag to the loan")
    public void unsetFraud() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.disableFraudFlag();

        Response<PutLoansLoanIdResponse> responseMod = loansApi.modifyLoanApplication(loanId, putLoansLoanIdRequest, "markAsFraud")
                .execute();
        testContext().set(TestContextKey.LOAN_FRAUD_MODIFY_RESPONSE, responseMod);
        ErrorHelper.checkSuccessfulApiCall(responseMod);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Boolean fraudFlagActual = loanDetailsResponse.body().getFraud();
        assertThat(fraudFlagActual).as(ErrorMessageHelper.wrongFraudFlag(fraudFlagActual, false)).isEqualTo(false);
    }

    @Then("Fraud flag modification fails")
    public void failedFraudModification() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.disableFraudFlag();

        Response<PutLoansLoanIdResponse> responseMod = loansApi.modifyLoanApplication(loanId, putLoansLoanIdRequest, "markAsFraud")
                .execute();
        testContext().set(TestContextKey.LOAN_FRAUD_MODIFY_RESPONSE, responseMod);

        ErrorResponse errorDetails = ErrorResponse.from(responseMod);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.loanFraudFlagModificationMsg(loanId.toString()));
    }

    @Then("Transaction response has boolean value in header {string}: {string}")
    public void transactionHeaderCheckBoolean(String headerKey, String headerValue) {
        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        String headerValueActual = paymentTransactionResponse.headers().get(headerKey);
        assertThat(headerValueActual).as(ErrorMessageHelper.wrongValueInResponseHeader(headerKey, headerValueActual, headerValue))
                .isEqualTo(headerValue);
    }

    @Then("Transaction response has {double} EUR value for transaction amount")
    public void transactionAmountCheck(double amountExpected) {
        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Double amountActual = Double.valueOf(paymentTransactionResponse.body().getChanges().getTransactionAmount());
        assertThat(amountActual).as(ErrorMessageHelper.wrongAmountInTransactionsResponse(amountActual, amountExpected))
                .isEqualTo(amountExpected);
    }

    @Then("Transaction response has the correct clientId and the loanId of the first transaction")
    public void transactionClientIdAndLoanIdCheck() {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientIdExpected = clientResponse.body().getClientId();

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanIdExpected = Long.valueOf(loanResponse.body().getLoanId());

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Long clientIdActual = paymentTransactionResponse.body().getClientId();
        Long loanIdActual = paymentTransactionResponse.body().getLoanId();

        assertThat(clientIdActual).as(ErrorMessageHelper.wrongClientIdInTransactionResponse(clientIdActual, clientIdExpected))
                .isEqualTo(clientIdExpected);
        assertThat(loanIdActual).as(ErrorMessageHelper.wrongLoanIdInTransactionResponse(loanIdActual, loanIdExpected))
                .isEqualTo(loanIdExpected);
    }

    @Then("Transaction response has the clientId for the second client and the loanId of the second transaction")
    public void transactionSecondClientIdAndSecondLoanIdCheck() {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE);
        Long clientIdExpected = clientResponse.body().getClientId();

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        Long loanIdExpected = Long.valueOf(loanResponse.body().getLoanId());

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Long clientIdActual = paymentTransactionResponse.body().getClientId();
        Long loanIdActual = paymentTransactionResponse.body().getLoanId();

        assertThat(clientIdActual).as(ErrorMessageHelper.wrongClientIdInTransactionResponse(clientIdActual, clientIdExpected))
                .isEqualTo(clientIdExpected);
        assertThat(loanIdActual).as(ErrorMessageHelper.wrongLoanIdInTransactionResponse(loanIdActual, loanIdExpected))
                .isEqualTo(loanIdExpected);
    }

    @Then("Loan has {int} {string} transactions on Transactions tab")
    public void checkNrOfTransactions(int nrOfTransactionsExpected, String transactionTypeInput) throws IOException {
        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();

        List<GetLoansLoanIdTransactions> transactions = loanDetails.body().getTransactions();
        List<String> transactionsMatched = new ArrayList<>();

        transactions.forEach(t -> {
            String transactionTypeValueActual = t.getType().getCode();
            String transactionTypeValueExpected = "loanTransactionType." + transactionTypeValue;

            if (transactionTypeValueActual.equals(transactionTypeValueExpected)) {
                transactionsMatched.add(transactionTypeValueActual);
            }
        });

        int nrOfTransactionsActual = transactionsMatched.size();
        assertThat(nrOfTransactionsActual)
                .as(ErrorMessageHelper.wrongNrOfTransactions(transactionTypeInput, nrOfTransactionsActual, nrOfTransactionsExpected))
                .isEqualTo(nrOfTransactionsExpected);
    }

    @Then("Second loan has {int} {string} transactions on Transactions tab")
    public void checkNrOfTransactionsOnSecondLoan(int nrOfTransactionsExpected, String transactionTypeInput) throws IOException {
        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();

        List<GetLoansLoanIdTransactions> transactions = loanDetails.body().getTransactions();
        List<String> transactionsMatched = new ArrayList<>();

        transactions.forEach(t -> {
            String transactionTypeValueActual = t.getType().getCode();
            String transactionTypeValueExpected = "loanTransactionType." + transactionTypeValue;

            if (transactionTypeValueActual.equals(transactionTypeValueExpected)) {
                transactionsMatched.add(transactionTypeValueActual);
            }
        });

        int nrOfTransactionsActual = transactionsMatched.size();
        assertThat(nrOfTransactionsActual)
                .as(ErrorMessageHelper.wrongNrOfTransactions(transactionTypeInput, nrOfTransactionsActual, nrOfTransactionsExpected))
                .isEqualTo(nrOfTransactionsExpected);
    }

    @Then("Loan status has changed to {string}")
    public void loanStatusHasChangedTo(String loanStatus) {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        LoanStatusEnumDataV1 expectedStatus = getExpectedStatus(loanStatus);
        eventAssertion.assertEvent(LoanStatusChangedEvent.class, loanId).extractingData(LoanAccountDataV1::getStatus)
                .isEqualTo(expectedStatus);
    }

    @Then("Loan marked as charged-off on {string}")
    public void isLoanChargedOff(String chargeOffDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        LocalDate expectedChargeOffDate = LocalDate.parse(chargeOffDate, FORMATTER);

        assertThat(loanDetailsResponse.body().getChargedOff()).isEqualTo(true);
        assertThat(loanDetailsResponse.body().getTimeline().getChargedOffOnDate()).isEqualTo(expectedChargeOffDate);
    }

    @And("Admin checks that last closed business date of loan is {string}")
    public void getLoanLastCOBDate(String date) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        if ("null".equals(date)) {
            assertThat(loanDetails.body().getLastClosedBusinessDate()).isNull();
        } else {
            assertThat(FORMATTER.format(Objects.requireNonNull(loanDetails.body().getLastClosedBusinessDate()))).isEqualTo(date);
        }
    }

    @When("Admin runs COB catch up")
    public void runLoanCOBCatchUp() throws IOException {
        Response<Void> catchUpResponse = loanCobCatchUpApi.executeLoanCOBCatchUp().execute();
        ErrorHelper.checkSuccessfulApiCall(catchUpResponse);
    }

    @When("Admin checks that Loan COB is running until the current business date")
    public void checkLoanCOBCatchUpRunningUntilCOBBusinessDate() {
        await().pollInterval(2, TimeUnit.SECONDS).atMost(Duration.ofSeconds(20)).until(() -> {
            Response<IsCatchUpRunningResponse> isCatchUpRunningResponse = loanCobCatchUpApi.isCatchUpRunning().execute();
            ErrorHelper.checkSuccessfulApiCall(isCatchUpRunningResponse);
            IsCatchUpRunningResponse isCatchUpRunning = isCatchUpRunningResponse.body();
            return isCatchUpRunning.getIsCatchUpRunning();
        });
        await().pollInterval(2, TimeUnit.SECONDS).atMost(Duration.ofSeconds(240)).until(() -> {
            Response<IsCatchUpRunningResponse> isCatchUpRunningResponse = loanCobCatchUpApi.isCatchUpRunning().execute();
            ErrorHelper.checkSuccessfulApiCall(isCatchUpRunningResponse);
            IsCatchUpRunningResponse isCatchUpRunning = isCatchUpRunningResponse.body();
            return !isCatchUpRunning.getIsCatchUpRunning();
        });
    }

    @Then("Loan's actualMaturityDate is {string}")
    public void checkActualMaturityDate(String actualMaturityDateExpected) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        LocalDate actualMaturityDate = loanDetailsResponse.body().getTimeline().getActualMaturityDate();
        String actualMaturityDateActual = FORMATTER.format(actualMaturityDate);

        assertThat(actualMaturityDateActual)
                .as(ErrorMessageHelper.wrongDataInActualMaturityDate(actualMaturityDateActual, actualMaturityDateExpected))
                .isEqualTo(actualMaturityDateExpected);
    }

    @Then("LoanAccrualTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanAccrualTransactionCreatedBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions accrualTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual transaction found on %s", date)));
        Long accrualTransactionId = accrualTransaction.getId();

        eventAssertion.assertEventRaised(LoanAccrualTransactionCreatedBusinessEvent.class, accrualTransactionId);
    }

    @Then("Loan details and event has the following last repayment related data:")
    public void checkLastRepaymentData(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String lastPaymentAmountExpected = expectedValues.get(0);
        String lastPaymentDateExpected = expectedValues.get(1);
        String lastRepaymentAmountExpected = expectedValues.get(2);
        String lastRepaymentDateExpected = expectedValues.get(3);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "collection", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        GetLoansLoanIdDelinquencySummary delinquent = loanDetailsResponse.body().getDelinquent();
        String lastPaymentAmountActual = String.valueOf(delinquent.getLastPaymentAmount());
        String lastPaymentDateActual = FORMATTER.format(delinquent.getLastPaymentDate());
        String lastRepaymentAmountActual = String.valueOf(delinquent.getLastRepaymentAmount());
        String lastRepaymentDateActual = FORMATTER.format(delinquent.getLastRepaymentDate());

        assertThat(lastPaymentAmountActual)
                .as(ErrorMessageHelper.wrongDataInLastPaymentAmount(lastPaymentAmountActual, lastPaymentAmountExpected))
                .isEqualTo(lastPaymentAmountExpected);
        assertThat(lastPaymentDateActual).as(ErrorMessageHelper.wrongDataInLastPaymentDate(lastPaymentDateActual, lastPaymentDateExpected))
                .isEqualTo(lastPaymentDateExpected);
        assertThat(lastRepaymentAmountActual)
                .as(ErrorMessageHelper.wrongDataInLastRepaymentAmount(lastRepaymentAmountActual, lastRepaymentAmountExpected))
                .isEqualTo(lastRepaymentAmountExpected);
        assertThat(lastRepaymentDateActual)
                .as(ErrorMessageHelper.wrongDataInLastRepaymentDate(lastRepaymentDateActual, lastRepaymentDateExpected))
                .isEqualTo(lastRepaymentDateExpected);

        eventAssertion.assertEvent(LoanStatusChangedEvent.class, loanId).extractingData(loanAccountDataV1 -> {
            String lastPaymentAmountEvent = String.valueOf(loanAccountDataV1.getDelinquent().getLastPaymentAmount().doubleValue());
            String lastPaymentDateEvent = FORMATTER.format(LocalDate.parse(loanAccountDataV1.getDelinquent().getLastPaymentDate()));
            String lastRepaymentAmountEvent = String.valueOf(loanAccountDataV1.getDelinquent().getLastRepaymentAmount().doubleValue());
            String lastRepaymentDateEvent = FORMATTER.format(LocalDate.parse(loanAccountDataV1.getDelinquent().getLastRepaymentDate()));

            assertThat(lastPaymentAmountEvent)
                    .as(ErrorMessageHelper.wrongDataInLastPaymentAmount(lastPaymentAmountEvent, lastPaymentAmountExpected))
                    .isEqualTo(lastPaymentAmountExpected);
            assertThat(lastPaymentDateEvent)
                    .as(ErrorMessageHelper.wrongDataInLastPaymentDate(lastPaymentDateEvent, lastPaymentDateExpected))
                    .isEqualTo(lastPaymentDateExpected);
            assertThat(lastRepaymentAmountEvent)
                    .as(ErrorMessageHelper.wrongDataInLastRepaymentAmount(lastRepaymentAmountEvent, lastRepaymentAmountExpected))
                    .isEqualTo(lastRepaymentAmountExpected);
            assertThat(lastRepaymentDateEvent)
                    .as(ErrorMessageHelper.wrongDataInLastRepaymentDate(lastRepaymentDateEvent, lastRepaymentDateExpected))
                    .isEqualTo(lastRepaymentDateExpected);

            return null;
        });

    }

    @And("Admin does a charge-off undo the loan with reversal external Id")
    public void chargeOffUndoWithReversalExternalId() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String reversalExternalId = Utils.randomNameGenerator("reversalExtId_", 3);
        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest()
                .reversalExternalId(reversalExternalId);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffUndoResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffUndoRequest, "undo-charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE, chargeOffUndoResponse);
        ErrorHelper.checkSuccessfulApiCall(chargeOffUndoResponse);

        Long transactionId = chargeOffUndoResponse.body().getResourceId();

        Response<GetLoansLoanIdTransactionsTransactionIdResponse> transactionResponse = loanTransactionsApi
                .retrieveTransaction(loanId, transactionId, "").execute();
        ErrorHelper.checkSuccessfulApiCall(transactionResponse);
        assertThat(transactionResponse.body().getReversalExternalId()).isEqualTo(reversalExternalId);
    }

    @Then("Loan Charge-off undo event has reversed on date {string} for charge-off undo")
    public void reversedOnDateIsNotNullForEvent(String reversedDate) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions chargeOffTransaction = transactions.stream().filter(t -> "Charge-off".equals(t.getType().getValue()))
                .findFirst().orElseThrow(() -> new IllegalStateException(String.format("No transaction found")));
        Long chargeOffTransactionId = chargeOffTransaction.getId();

        eventAssertion.assertEvent(LoanChargeOffUndoEvent.class, chargeOffTransactionId).extractingData(loanTransactionDataV1 -> {
            String reversedOnDate = FORMATTER.format(LocalDate.parse(loanTransactionDataV1.getReversedOnDate()));
            assertThat(reversedOnDate).isEqualTo(reversedDate);
            return null;
        });
    }

    @Then("Loan has the following maturity data:")
    public void checkMaturity(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String actualMaturityDateExpected = expectedValues.get(0);
        String expectedMaturityDateExpected = expectedValues.get(1);

        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        GetLoansLoanIdTimeline timeline = loanDetailsResponse.body().getTimeline();
        String actualMaturityDateActual = FORMATTER.format(timeline.getActualMaturityDate());
        String expectedMaturityDateActual = FORMATTER.format(timeline.getExpectedMaturityDate());

        assertThat(actualMaturityDateActual)
                .as(ErrorMessageHelper.wrongDataInActualMaturityDate(actualMaturityDateActual, actualMaturityDateExpected))
                .isEqualTo(actualMaturityDateExpected);
        assertThat(expectedMaturityDateActual)
                .as(ErrorMessageHelper.wrongDataInExpectedMaturityDate(expectedMaturityDateActual, expectedMaturityDateExpected))
                .isEqualTo(expectedMaturityDateExpected);
    }

    @Then("Admin successfully deletes the loan with external id")
    public void deleteLoanWithExternalId() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanCreateResponse.body().getLoanId();
        String loanExternalId = loanCreateResponse.body().getResourceExternalId();
        Response<DeleteLoansLoanIdResponse> deleteLoanResponse = loansApi.deleteLoanApplication1(loanExternalId).execute();
        assertThat(deleteLoanResponse.body().getLoanId()).isEqualTo(loanId);
        assertThat(deleteLoanResponse.body().getResourceExternalId()).isEqualTo(loanExternalId);
    }

    @Then("Admin fails to delete the loan with incorrect external id")
    public void failedDeleteLoanWithExternalId() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.body().getResourceExternalId();
        Response<DeleteLoansLoanIdResponse> deleteLoanResponse = loansApi.deleteLoanApplication1(loanExternalId.substring(5)).execute();
        ErrorResponse errorDetails = ErrorResponse.from(deleteLoanResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
    }

    @When("Admin set {string} loan product {string} transaction type to {string} future installment allocation rule")
    public void editFutureInstallmentAllocationTypeForLoanProduct(String loanProductName, String transactionTypeToChange,
            String futureInstallmentAllocationRuleNew) throws IOException {
        DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProductName);
        Long loanProductId = loanProductResolver.resolve(product);
        log.info("loanProductId {}", loanProductId);

        Response<GetLoanProductsProductIdResponse> loanProductDetails = loanProductsApi.retrieveLoanProductDetails(loanProductId).execute();
        ErrorHelper.checkSuccessfulApiCall(loanProductDetails);
        List<AdvancedPaymentData> paymentAllocation = loanProductDetails.body().getPaymentAllocation();

        List<AdvancedPaymentData> newPaymentAllocation = new ArrayList<>();
        paymentAllocation.forEach(e -> {
            String transactionTypeOriginal = e.getTransactionType();
            String futureInstallmentAllocationRule = e.getFutureInstallmentAllocationRule();
            if (transactionTypeToChange.equals(transactionTypeOriginal)) {
                futureInstallmentAllocationRule = futureInstallmentAllocationRuleNew;
            }
            newPaymentAllocation.add(
                    LoanProductGlobalInitializerStep.createPaymentAllocation(transactionTypeOriginal, futureInstallmentAllocationRule));
        });

        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest()
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.getValue()).paymentAllocation(newPaymentAllocation);

        Response<PutLoanProductsProductIdResponse> response = loanProductsApi
                .updateLoanProduct(loanProductId, putLoanProductsProductIdRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @When("Admin sets repaymentStartDateType for {string} loan product to {string}")
    public void editRepaymentStartDateType(String loanProductName, String repaymentStartDateType) throws IOException {
        DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProductName);
        Long loanProductId = loanProductResolver.resolve(product);
        log.info("loanProductId {}", loanProductId);

        Map<String, Integer> repaymentStartDateTypeMap = Map.of("DISBURSEMENT_DATE", 1, "SUBMITTED_ON_DATE", 2);

        if (!repaymentStartDateTypeMap.containsKey(repaymentStartDateType)) {
            throw new IllegalArgumentException(String
                    .format("Invalid repaymentStartDateType: %s. Must be DISBURSEMENT_DATE or SUBMITTED_ON_DATE.", repaymentStartDateType));
        }

        int repaymentStartDateTypeValue = repaymentStartDateTypeMap.get(repaymentStartDateType);
        PutLoanProductsProductIdRequest putLoanProductsProductIdRequest = new PutLoanProductsProductIdRequest()//
                .repaymentStartDateType(repaymentStartDateTypeValue)//
                .locale(DEFAULT_LOCALE);//

        Response<PutLoanProductsProductIdResponse> response = loanProductsApi
                .updateLoanProduct(loanProductId, putLoanProductsProductIdRequest).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
    }

    @And("Admin does write-off the loan on {string}")
    public void writeOffLoan(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        PostLoansLoanIdTransactionsRequest writeOffRequest = LoanRequestFactory.defaultWriteOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> writeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, writeOffRequest, "writeoff").execute();
        testContext().set(TestContextKey.LOAN_WRITE_OFF_RESPONSE, writeOffResponse);
        ErrorHelper.checkSuccessfulApiCall(writeOffResponse);
    }

    @Then("Admin fails to undo {string}th transaction made on {string}")
    public void undoTransaction(String nthTransaction, String transactionDate) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        List<GetLoansLoanIdTransactions> transactions = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()
                .getTransactions();

        int nthItem = Integer.parseInt(nthTransaction) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))).toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        Response<PostLoansLoanIdTransactionsResponse> transactionUndoResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, targetTransaction.getId(), transactionUndoRequest, "").execute();
        ErrorResponse errorDetails = ErrorResponse.from(transactionUndoResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(503);

    }

    @Then("Loan {string} repayment transaction on {string} with {double} EUR transaction amount results in error")
    public void loanTransactionWithErrorCheck(String repaymentType, String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        Map<String, String> headerMap = new HashMap<>();

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> repaymentResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, repaymentRequest, "repayment", headerMap).execute();

        ErrorResponse errorDetails = ErrorResponse.from(repaymentResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);

    }

    @Then("Loan details has the downpayment amount {string} in summary.totalRepaymentTransaction")
    public void totalRepaymentTransaction(String expectedAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);

        Double expectedAmountParsed = Double.parseDouble(expectedAmount);
        Double totalRepaymentTransaction = loanDetails.body().getSummary().getTotalRepaymentTransaction();

        assertThat(totalRepaymentTransaction)
                .as(ErrorMessageHelper.wrongAmountInTotalRepaymentTransaction(totalRepaymentTransaction, expectedAmountParsed))
                .isEqualTo(expectedAmountParsed);
    }

    @Then("LoanDetails has fixedLength field with int value: {int}")
    public void checkLoanDetailsFieldAndValueInt(int fieldValue) throws IOException, NoSuchMethodException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);

        Integer fixedLengthactual = loanDetails.body().getFixedLength();
        assertThat(fixedLengthactual).as(ErrorMessageHelper.wrongfixedLength(fixedLengthactual, fieldValue)).isEqualTo(fieldValue);
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because disbursement date is earlier than {string}")
    public void disburseLoanFailureWithPastDate(String actualDisbursementDate, String transactionAmount, String futureApproveDate)
            throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        String futureApproveDateISO = FORMATTER_EVENTS.format(FORMATTER.parse(futureApproveDate));
        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.disbursePastDateFailure((int) loanId, futureApproveDateISO));
    }

    @Then("Loan emi amount variations has {int} variation, with the following data:")
    public void loanEmiAmountVariationsCheck(final int linesExpected, final DataTable table) throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanCreateResponse.body());
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "all", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdLoanTermVariations> emiAmountVariations = loanDetailsResponse.body().getEmiAmountVariations();

        final List<List<String>> data = table.asLists();
        assertNotNull(emiAmountVariations);
        final int linesActual = emiAmountVariations.size();
        data.stream().skip(1) // skip headers
                .forEach(expectedValues -> {
                    final List<List<String>> actualValuesList = emiAmountVariations.stream()
                            .map(emi -> fetchValuesOfLoanTermVariations(data.get(0), emi)).collect(Collectors.toList());

                    final boolean containsExpectedValues = actualValuesList.stream()
                            .anyMatch(actualValues -> actualValues.equals(expectedValues));
                    assertThat(containsExpectedValues).as(ErrorMessageHelper
                            .wrongValueInLineInLoanTermVariations(data.indexOf(expectedValues), actualValuesList, expectedValues)).isTrue();

                    assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInLoanTermVariations(linesActual, linesExpected))
                            .isEqualTo(linesExpected);
                });
    }

    @Then("Loan term variations has {int} variation, with the following data:")
    public void loanTermVariationsCheck(final int linesExpected, final DataTable table) throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanCreateResponse.body());
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "loanTermVariations", "", "")
                .execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdLoanTermVariations> loanTermVariations = loanDetailsResponse.body().getLoanTermVariations();
        assertNotNull(loanTermVariations);

        final List<List<String>> data = table.asLists();
        final int linesActual = loanTermVariations.size();
        data.stream().skip(1) // skip headers
                .forEach(expectedValues -> {
                    final String expectedTermTypeId = expectedValues.get(0);

                    final List<List<String>> actualValuesList = loanTermVariations.stream().filter(loanTerm -> {
                        assertNotNull(loanTerm.getTermType());
                        return expectedTermTypeId.equals(String.valueOf(loanTerm.getTermType().getId()));
                    }).map(loanTerm -> fetchValuesOfLoanTermVariations(data.get(0), loanTerm)).collect(Collectors.toList());

                    final boolean containsExpectedValues = actualValuesList.stream()
                            .anyMatch(actualValues -> actualValues.equals(expectedValues));
                    assertThat(containsExpectedValues).as(ErrorMessageHelper
                            .wrongValueInLineInLoanTermVariations(data.indexOf(expectedValues), actualValuesList, expectedValues)).isTrue();

                    assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInLoanTermVariations(linesActual, linesExpected))
                            .isEqualTo(linesExpected);
                });
    }

    private void createCustomizedLoan(final List<String> loanData, final boolean withEmi) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestType = loanData.get(4);
        final String interestCalculationPeriod = loanData.get(5);
        final String amortizationType = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyType = loanData.get(10);
        final Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        final Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        final Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        final Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        final String transactionProcessingStrategyCode = loanData.get(15);

        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        final Long loanProductId = loanProductResolver.resolve(product);

        final LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        final Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        final RepaymentFrequencyType repaymentFrequencyType1 = RepaymentFrequencyType.valueOf(repaymentFrequencyType);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType1.getValue();

        final InterestType interestType1 = InterestType.valueOf(interestType);
        final Integer interestTypeValue = interestType1.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod1 = InterestCalculationPeriodTime.valueOf(interestCalculationPeriod);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod1.getValue();

        final AmortizationType amortizationType1 = AmortizationType.valueOf(amortizationType);
        final Integer amortizationTypeValue = amortizationType1.getValue();

        final TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        final String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).productId(loanProductId)
                .principal(new BigDecimal(principal)).interestRatePerPeriod(interestRate).interestType(interestTypeValue)
                .interestCalculationPeriodType(interestCalculationPeriodValue).amortizationType(amortizationTypeValue)
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(loanTermFrequencyTypeValue)
                .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentFrequency)
                .repaymentFrequencyType(repaymentFrequencyTypeValue).submittedOnDate(submitDate).expectedDisbursementDate(submitDate)
                .graceOnPrincipalPayment(graceOnPrincipalPayment).graceOnInterestPayment(graceOnInterestPayment)
                .graceOnInterestPayment(graceOnInterestCharged).transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue);

        if (withEmi) {
            loansRequest.fixedEmiAmount(new BigDecimal(555));
        }

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    private void performLoanDisbursementAndVerifyStatus(final long loanId, final PostLoansLoanIdRequest disburseRequest)
            throws IOException {
        final Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse")
                .execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorHelper.checkSuccessfulApiCall(loanDisburseResponse);
        assertNotNull(loanDisburseResponse.body());
        assertNotNull(loanDisburseResponse.body().getChanges());
        assertNotNull(loanDisburseResponse.body().getChanges().getStatus());
        final Long statusActual = loanDisburseResponse.body().getChanges().getStatus().getId();
        assertNotNull(statusActual);

        final Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        assertNotNull(loanDetails.body());
        assertNotNull(loanDetails.body().getStatus());
        final Long statusExpected = Long.valueOf(loanDetails.body().getStatus().getId());

        assertThat(statusActual).as(ErrorMessageHelper.wrongLoanStatus(Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))
                .isEqualTo(statusExpected);
        eventCheckHelper.disburseLoanEventCheck(loanDisburseResponse);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    private LoanStatusEnumDataV1 getExpectedStatus(String loanStatus) {
        LoanStatusEnumDataV1 result = new LoanStatusEnumDataV1();
        switch (loanStatus) {
            case "Submitted and pending approval" -> {
                result.setId(100);
                result.setCode("loanStatusType.submitted.and.pending.approval");
                result.setValue("Submitted and pending approval");
                result.setPendingApproval(true);
                result.setWaitingForDisbursal(false);
                result.setActive(false);
                result.setClosedObligationsMet(false);
                result.setClosedWrittenOff(false);
                result.setClosedRescheduled(false);
                result.setClosed(false);
                result.setOverpaid(false);
            }
            case "Approved" -> {
                result.setId(200);
                result.setCode("loanStatusType.approved");
                result.setValue("Approved");
                result.setPendingApproval(false);
                result.setWaitingForDisbursal(true);
                result.setActive(false);
                result.setClosedObligationsMet(false);
                result.setClosedWrittenOff(false);
                result.setClosedRescheduled(false);
                result.setClosed(false);
                result.setOverpaid(false);
            }
            case "Active" -> {
                result.setId(300);
                result.setCode("loanStatusType.active");
                result.setValue("Active");
                result.setPendingApproval(false);
                result.setWaitingForDisbursal(false);
                result.setActive(true);
                result.setClosedObligationsMet(false);
                result.setClosedWrittenOff(false);
                result.setClosedRescheduled(false);
                result.setClosed(false);
                result.setOverpaid(false);
            }
            case "Closed (obligations met)" -> {
                result.setId(600);
                result.setCode("loanStatusType.closed.obligations.met");
                result.setValue("Closed (obligations met)");
                result.setPendingApproval(false);
                result.setWaitingForDisbursal(false);
                result.setActive(false);
                result.setClosedObligationsMet(true);
                result.setClosedWrittenOff(false);
                result.setClosedRescheduled(false);
                result.setClosed(true);
                result.setOverpaid(false);
            }
            case "Overpaid" -> {
                result.setId(700);
                result.setCode("loanStatusType.overpaid");
                result.setValue("Overpaid");
                result.setPendingApproval(false);
                result.setWaitingForDisbursal(false);
                result.setActive(false);
                result.setClosedObligationsMet(false);
                result.setClosedWrittenOff(false);
                result.setClosedRescheduled(false);
                result.setClosed(false);
                result.setOverpaid(true);

            }
            default -> throw new UnsupportedOperationException("Not yet covered loan status: " + loanStatus);
        }
        return result;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> fetchValuesOfTransaction(List<String> header, GetLoansLoanIdTransactions t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Transaction date" -> actualValues.add(t.getDate() == null ? null : FORMATTER.format(t.getDate()));
                case "Transaction Type" -> actualValues.add(t.getType().getValue() == null ? null : t.getType().getValue());
                case "Amount" -> actualValues.add(t.getAmount() == null ? null : String.valueOf(t.getAmount()));
                case "Principal" -> actualValues.add(t.getPrincipalPortion() == null ? null : String.valueOf(t.getPrincipalPortion()));
                case "Interest" -> actualValues.add(t.getInterestPortion() == null ? null : String.valueOf(t.getInterestPortion()));
                case "Fees" -> actualValues.add(t.getFeeChargesPortion() == null ? null : String.valueOf(t.getFeeChargesPortion()));
                case "Penalties" ->
                    actualValues.add(t.getPenaltyChargesPortion() == null ? null : String.valueOf(t.getPenaltyChargesPortion()));
                case "Loan Balance" ->
                    actualValues.add(t.getOutstandingLoanBalance() == null ? null : String.valueOf(t.getOutstandingLoanBalance()));
                case "Overpayment" ->
                    actualValues.add(t.getOverpaymentPortion() == null ? null : String.valueOf(t.getOverpaymentPortion()));
                case "Reverted" -> actualValues.add(t.getManuallyReversed() == null ? null : String.valueOf(t.getManuallyReversed()));
            }
        }
        return actualValues;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> fetchValuesOfRepaymentSchedule(List<String> header, GetLoansLoanIdRepaymentPeriod repaymentPeriod) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Nr" -> actualValues.add(repaymentPeriod.getPeriod() == null ? null : String.valueOf(repaymentPeriod.getPeriod()));
                case "Days" ->
                    actualValues.add(repaymentPeriod.getDaysInPeriod() == null ? null : String.valueOf(repaymentPeriod.getDaysInPeriod()));
                case "Date" ->
                    actualValues.add(repaymentPeriod.getDueDate() == null ? null : FORMATTER.format(repaymentPeriod.getDueDate()));
                case "Paid date" -> actualValues.add(repaymentPeriod.getObligationsMetOnDate() == null ? null
                        : FORMATTER.format(repaymentPeriod.getObligationsMetOnDate()));
                case "Balance of loan" -> actualValues.add(repaymentPeriod.getPrincipalLoanBalanceOutstanding() == null ? null
                        : String.valueOf(repaymentPeriod.getPrincipalLoanBalanceOutstanding()));
                case "Principal due" ->
                    actualValues.add(repaymentPeriod.getPrincipalDue() == null ? null : String.valueOf(repaymentPeriod.getPrincipalDue()));
                case "Interest" ->
                    actualValues.add(repaymentPeriod.getInterestDue() == null ? null : String.valueOf(repaymentPeriod.getInterestDue()));
                case "Fees" -> actualValues
                        .add(repaymentPeriod.getFeeChargesDue() == null ? null : String.valueOf(repaymentPeriod.getFeeChargesDue()));
                case "Penalties" -> actualValues.add(
                        repaymentPeriod.getPenaltyChargesDue() == null ? null : String.valueOf(repaymentPeriod.getPenaltyChargesDue()));
                case "Due" -> actualValues.add(
                        repaymentPeriod.getTotalDueForPeriod() == null ? null : String.valueOf(repaymentPeriod.getTotalDueForPeriod()));
                case "Paid" -> actualValues.add(
                        repaymentPeriod.getTotalPaidForPeriod() == null ? null : String.valueOf(repaymentPeriod.getTotalPaidForPeriod()));
                case "In advance" -> actualValues.add(repaymentPeriod.getTotalPaidInAdvanceForPeriod() == null ? null
                        : String.valueOf(repaymentPeriod.getTotalPaidInAdvanceForPeriod()));
                case "Late" -> actualValues.add(repaymentPeriod.getTotalPaidLateForPeriod() == null ? null
                        : String.valueOf(repaymentPeriod.getTotalPaidLateForPeriod()));
                case "Waived" -> actualValues.add(repaymentPeriod.getTotalWaivedForPeriod() == null ? null
                        : String.valueOf(repaymentPeriod.getTotalWaivedForPeriod()));
                case "Outstanding" -> actualValues.add(repaymentPeriod.getTotalOutstandingForPeriod() == null ? null
                        : String.valueOf(repaymentPeriod.getTotalOutstandingForPeriod()));
            }
        }
        return actualValues;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> validateRepaymentScheduleTotal(List<String> header, GetLoansLoanIdRepaymentSchedule repaymentSchedule,
            List<String> expectedAmounts) {
        List<String> actualValues = new ArrayList<>();
        // total paid for all periods
        Double paidActual = 0.0;
        List<GetLoansLoanIdRepaymentPeriod> periods = repaymentSchedule.getPeriods();
        for (GetLoansLoanIdRepaymentPeriod period : periods) {
            if (null != period.getTotalPaidForPeriod()) {
                paidActual += period.getTotalPaidForPeriod();
            }
        }
        BigDecimal paidActualBd = new BigDecimal(paidActual).setScale(2, RoundingMode.HALF_DOWN);

        for (int i = 0; i < header.size(); i++) {
            String headerName = header.get(i);
            String expectedValue = expectedAmounts.get(i);
            switch (headerName) {
                case "Principal due" -> assertThat(repaymentSchedule.getTotalPrincipalExpected())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePrincipal(repaymentSchedule.getTotalPrincipalExpected(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Interest" -> assertThat(repaymentSchedule.getTotalInterestCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInterest(repaymentSchedule.getTotalInterestCharged(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Fees" -> assertThat(repaymentSchedule.getTotalFeeChargesCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleFees(repaymentSchedule.getTotalFeeChargesCharged(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Penalties" -> assertThat(repaymentSchedule.getTotalPenaltyChargesCharged())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePenalties(repaymentSchedule.getTotalPenaltyChargesCharged(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Due" -> assertThat(repaymentSchedule.getTotalRepaymentExpected())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleDue(repaymentSchedule.getTotalRepaymentExpected(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Paid" -> assertThat(paidActualBd.doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePaid(paidActualBd.doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "In advance" -> assertThat(repaymentSchedule.getTotalPaidInAdvance())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInAdvance(repaymentSchedule.getTotalPaidInAdvance(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Late" -> assertThat(repaymentSchedule.getTotalPaidLate())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleLate(repaymentSchedule.getTotalPaidLate(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Waived" -> assertThat(repaymentSchedule.getTotalWaived())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleWaived(repaymentSchedule.getTotalWaived(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Outstanding" -> assertThat(repaymentSchedule.getTotalOutstanding())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleOutstanding(repaymentSchedule.getTotalOutstanding(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
            }
        }
        return actualValues;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> fetchValuesOfLoanTermVariations(final List<String> header, final GetLoansLoanIdLoanTermVariations emiVariation) {
        final List<String> actualValues = new ArrayList<>();
        assertNotNull(emiVariation.getTermType());
        for (String headerName : header) {
            switch (headerName) {
                case "Term Type Id" -> actualValues
                        .add(emiVariation.getTermType().getId() == null ? null : String.valueOf(emiVariation.getTermType().getId()));
                case "Term Type Code" ->
                    actualValues.add(emiVariation.getTermType().getCode() == null ? null : emiVariation.getTermType().getCode());
                case "Term Type Value" ->
                    actualValues.add(emiVariation.getTermType().getValue() == null ? null : emiVariation.getTermType().getValue());
                case "Applicable From" -> actualValues.add(emiVariation.getTermVariationApplicableFrom() == null ? null
                        : FORMATTER.format(emiVariation.getTermVariationApplicableFrom()));
                case "Decimal Value" ->
                    actualValues.add(emiVariation.getDecimalValue() == null ? null : String.valueOf(emiVariation.getDecimalValue()));
                case "Date Value" ->
                    actualValues.add(emiVariation.getDateValue() == null ? null : FORMATTER.format(emiVariation.getDateValue()));
                case "Is Specific To Installment" -> actualValues.add(String.valueOf(emiVariation.getIsSpecificToInstallment()));
                case "Is Processed" ->
                    actualValues.add(emiVariation.getIsProcessed() == null ? null : String.valueOf(emiVariation.getIsProcessed()));
            }
        }
        return actualValues;
    }
}
