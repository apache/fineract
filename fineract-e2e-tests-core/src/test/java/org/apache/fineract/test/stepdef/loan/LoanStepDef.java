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
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.CHARGE_OFF_REASONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanChargePaidByDataV1;
import org.apache.fineract.avro.loan.v1.LoanStatusEnumDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetLoanProductsChargeOffReasonOptions;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargePaidByData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTermVariations;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTimeline;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.IsCatchUpRunningDTO;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostAddAndDeleteDisbursementDetailRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.services.LoanCobCatchUpApi;
import org.apache.fineract.client.services.LoanDisbursementDetailsApi;
import org.apache.fineract.client.services.LoanInterestPauseApi;
import org.apache.fineract.client.services.LoanProductsApi;
import org.apache.fineract.client.services.LoanTransactionsApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.AmortizationType;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.InterestCalculationPeriodTime;
import org.apache.fineract.test.data.InterestRateFrequencyType;
import org.apache.fineract.test.data.InterestType;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.LoanTermFrequencyType;
import org.apache.fineract.test.data.RepaymentFrequencyType;
import org.apache.fineract.test.data.TransactionProcessingStrategyCode;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.data.codevalue.CodeValue;
import org.apache.fineract.test.data.codevalue.CodeValueResolver;
import org.apache.fineract.test.data.codevalue.DefaultCodeValue;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.initializer.global.LoanProductGlobalInitializerStep;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.config.EventProperties;
import org.apache.fineract.test.messaging.config.JobPollingProperties;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.loan.LoanRescheduledDueAdjustScheduleEvent;
import org.apache.fineract.test.messaging.event.loan.LoanStatusChangedEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.BulkBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargeAdjustmentPostBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargeOffEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanChargeOffUndoEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionAccrualActivityPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionContractTerminationPostBusinessEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class LoanStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_FORMAT_EVENTS = "yyyy-MM-dd";
    public static final String DEFAULT_LOCALE = "en";
    public static final String LOAN_STATE_SUBMITTED_AND_PENDING = "Submitted and pending approval";
    public static final String LOAN_STATE_APPROVED = "Approved";
    public static final String LOAN_STATE_REJECTED = "Rejected";
    public static final String LOAN_STATE_WITHDRAWN = "Withdrawn by applicant";
    public static final String LOAN_STATE_ACTIVE = "Active";
    private static final Gson GSON = new JSON().getGson();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern(DATE_FORMAT_EVENTS);
    private static final String TRANSACTION_DATE_FORMAT = "dd MMMM yyyy";

    @Autowired
    private BusinessDateHelper businessDateHelper;

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

    @Autowired
    private LoanProductsCustomApi loanProductsCustomApi;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private CodeValueResolver codeValueResolver;

    @Autowired
    private CodeHelper codeHelper;

    @Autowired
    private LoanInterestPauseApi loanInterestPauseApi;

    @Autowired
    private EventProperties eventProperties;

    @Autowired
    private LoanDisbursementDetailsApi loanDisbursementDetailsApi;

    @Autowired
    private JobPollingProperties jobPollingProperties;

    @Autowired
    private BusinessDateManagementApi businessDateApi;

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
        eventStore.reset();
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
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount")
    public void createTransactionForRefund(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws IOException, InterruptedException {
        eventStore.reset();
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
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
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
        eventStore.reset();
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
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin makes Credit Balance Refund transaction on {string} with {double} EUR transaction amount")
    public void createCBR(String transactionDate, double transactionAmount) throws IOException {
        eventStore.reset();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String transactionTypeValue = "creditBalanceRefund";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        Response<PostLoansLoanIdTransactionsResponse> paymentTransactionResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, paymentTransactionRequest, transactionTypeValue).execute();
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, paymentTransactionResponse);
        ErrorHelper.checkSuccessfulApiCall(paymentTransactionResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    public void checkCBRerror(PostLoansLoanIdTransactionsRequest paymentTransactionRequest, int errorCodeExpected,
            String errorMessageExpected) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        String transactionTypeValue = "creditBalanceRefund";
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

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("Credit Balance Refund transaction on future date {string} with {double} EUR transaction amount will result an error")
    public void futureDateCBRError(String transactionDate, double transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        int errorCodeExpected = 403;
        String errorMessageExpected = String.format("Loan: %s, Credit Balance Refund transaction cannot be created for the future.",
                loanId);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        checkCBRerror(paymentTransactionRequest, errorCodeExpected, errorMessageExpected);
    }

    @Then("Credit Balance Refund transaction on active loan {string} with {double} EUR transaction amount will result an error")
    public void notOverpaidLoanCBRError(String transactionDate, double transactionAmount) throws IOException {
        int errorCodeExpected = 400;
        String errorMessageExpected = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);
        checkCBRerror(paymentTransactionRequest, errorCodeExpected, errorMessageExpected);
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

    @When("Admin creates a fully customized loan with interestRateFrequencyType and following data:")
    public void createFullyCustomizedLoanWithInterestRateFrequencyType(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithInterestRateFrequency(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and following data:")
    public void createFullyCustomizedLoanWithLoanCharges(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithCharges(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and disbursement details and following data:")
    public void createFullyCustomizedLoanWithChargesAndDisbursementDetails(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and disbursements details and following data:")
    public void createFullyCustomizedLoanWithChargesAndDisbursementsDetails(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementsDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with disbursement details and following data:")
    public void createFullyCustomizedLoanWithDisbursementDetails(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithExpectedTrancheDisbursementDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with disbursements details and following data:")
    public void createFullyCustomizedLoanWithDisbursementsDetails(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithExpectedTrancheDisbursementsDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with forced disabled downpayment with the following data:")
    public void createFullyCustomizedLoanWithForcedDisabledDownpayment(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .enableDownPayment(false)//
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
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

        ErrorHelper.checkSuccessfulApiCall(response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @Then("Admin fails to create a fully customized loan with forced enabled downpayment with the following data:")
    public void createFullyCustomizedLoanWithForcedEnabledDownpayment(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .enableDownPayment(true)//
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
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

        ErrorResponse errorDetails = ErrorResponse.from(response);
        Integer errorCode = errorDetails.getHttpStatusCode();
        String errorMessage = errorDetails.getSingleError().getDeveloperMessage();
        assertThat(errorCode).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(errorMessage).isEqualTo(ErrorMessageHelper.downpaymentDisabledOnProductErrorCodeMsg());

        log.debug("Error code: {}", errorCode);
        log.debug("Error message: {}}", errorMessage);
    }

    @When("Admin creates a fully customized loan with auto downpayment {double}% and with the following data:")
    public void createFullyCustomizedLoanWithAutoDownpayment15(double percentage, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .enableAutoRepaymentForDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(percentage))//
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
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

        ErrorHelper.checkSuccessfulApiCall(response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a fully customized loan with downpayment {double}%, NO auto downpayment, and with the following data:")
    public void createFullyCustomizedLoanWithDownpayment15(double percentage, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

        TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(principal))//
                .interestRatePerPeriod(interestRate)//
                .enableAutoRepaymentForDownPayment(false)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(percentage))//
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
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

        ErrorHelper.checkSuccessfulApiCall(response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a fully customized loan with fixed length {int} and with the following data:")
    public void createFullyCustomizedLoanFixedLength(int fixedLength, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .fixedLength(fixedLength);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Trying to create a fully customized loan with fixed length {int} and with the following data will result a {int} ERROR:")
    public void createFullyCustomizedLoanFixedLengthError(int fixedLength, int errorCodeExpected, DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .fixedLength(fixedLength);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        String errorToString = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();
        int errorCodeActual = response.code();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
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
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        int errorCodeActual = response.code();
        String errorBody = response.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        String errorMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @When("Admin creates a fully customized loan with installment level delinquency and with the following data:")
    public void createFullyCustomizedLoanWithInstallmentLvlDelinquency(DataTable table) throws IOException {
        List<List<String>> data = table.asLists();
        List<String> loanData = data.get(1);
        String loanProduct = loanData.get(0);
        String submitDate = loanData.get(1);
        String principal = loanData.get(2);
        BigDecimal interestRate = new BigDecimal(loanData.get(3));
        String interestTypeStr = loanData.get(4);
        String interestCalculationPeriodStr = loanData.get(5);
        String amortizationTypeStr = loanData.get(6);
        Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        String loanTermFrequencyType = loanData.get(8);
        Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        String repaymentFrequencyTypeStr = loanData.get(10);
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

        RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        InterestType interestType = InterestType.valueOf(interestTypeStr);
        Integer interestTypeValue = interestType.getValue();

        InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        Integer amortizationTypeValue = amortizationType.getValue();

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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .enableInstallmentLevelDelinquency(true);//

        Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
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
        String lastPaymentAmountActual = delinquent.getLastPaymentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getLastPaymentAmount().doubleValue()).format();
        String lastPaymentDateActual = FORMATTER.format(delinquent.getLastPaymentDate());
        String lastRepaymentAmountActual = delinquent.getLastRepaymentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getLastRepaymentAmount().doubleValue()).format();
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

                String amountActual = loanChargePaidByList.get(i).getAmount() == null ? null
                        : new Utils.DoubleFormatter(loanChargePaidByList.get(i).getAmount().doubleValue()).format();
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

    @And("Admin successfully rejects the loan on {string}")
    public void rejectLoan(String rejectDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest rejectRequest = LoanRequestFactory.defaultLoanRejectRequest().rejectedOnDate(rejectDate);

        Response<PostLoansLoanIdResponse> loanRejectResponse = loansApi.stateTransitions(loanId, rejectRequest, "reject").execute();
        testContext().set(TestContextKey.LOAN_REJECT_RESPONSE, loanRejectResponse);
        ErrorHelper.checkSuccessfulApiCall(loanRejectResponse);
        assertThat(loanRejectResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_REJECTED);
        assertThat(loanRejectResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_REJECTED);

        eventCheckHelper.loanRejectedEventCheck(loanRejectResponse);
    }

    @And("Admin successfully withdrawn the loan on {string}")
    public void withdrawnLoan(String withdrawnDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest withdawnRequest = LoanRequestFactory.defaultLoanWithdrawnRequest().withdrawnOnDate(withdrawnDate);

        Response<PostLoansLoanIdResponse> loanWithdrawnResponse = loansApi.stateTransitions(loanId, withdawnRequest, "withdrawnByApplicant")
                .execute();
        testContext().set(TestContextKey.LOAN_WITHDRAWN_RESPONSE, loanWithdrawnResponse);
        ErrorHelper.checkSuccessfulApiCall(loanWithdrawnResponse);
        assertThat(loanWithdrawnResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_WITHDRAWN);
        assertThat(loanWithdrawnResponse.body().getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_WITHDRAWN);

        eventCheckHelper.undoApproveLoanEventCheck(loanWithdrawnResponse);
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
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorHelper.checkSuccessfulApiCall(loanDisburseResponse);
        Long statusActual = loanDisburseResponse.body().getChanges().getStatus().getId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        Long statusExpected = Long.valueOf(loanDetails.body().getStatus().getId());

        assertThat(statusActual)//
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))//
                .isEqualTo(statusExpected);//
        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    @And("Admin successfully add disbursement detail to the loan on {string} with {double} EUR transaction amount")
    public void addDisbursementDetailToLoan(String expectedDisbursementDate, Double disbursementAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "all", "", "").execute();
        Set<GetLoansLoanIdDisbursementDetails> disbursementDetailsList = loanDetails.body().getDisbursementDetails();

        List<DisbursementDetail> disbursementData = new ArrayList<>();

        // get and add already existing entries - just do not delete them
        if (disbursementDetailsList != null) {
            disbursementDetailsList.stream().forEach(disbursementDetail -> {
                LocalDate expectedDisbursementDateExisting = disbursementDetail.getExpectedDisbursementDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                String formatted = expectedDisbursementDateExisting.format(formatter);

                DisbursementDetail disbursementDetailEntryExisting = new DisbursementDetail().id(disbursementDetail.getId())
                        .expectedDisbursementDate(formatted).principal(disbursementDetail.getPrincipal());
                disbursementData.add(disbursementDetailEntryExisting);
            });
        }

        // add new entry with expected disbursement detail
        DisbursementDetail disbursementDetailsEntryNew = new DisbursementDetail().principal(disbursementAmount)
                .expectedDisbursementDate(expectedDisbursementDate);
        disbursementData.add(disbursementDetailsEntryNew);

        PostAddAndDeleteDisbursementDetailRequest disbursementDetailRequest = LoanRequestFactory
                .defaultLoanDisbursementDetailRequest(disbursementData);
        Response<String> loanDisburseResponse = loanDisbursementDetailsApi.addAndDeleteDisbursementDetail(loanId, disbursementDetailRequest)
                .execute();
        testContext().set(TestContextKey.LOAN_DISBURSEMENT_DETAIL_RESPONSE, loanDisburseResponse);
        ErrorHelper.checkSuccessfulApiCall(loanDisburseResponse);
    }

    @Then("Loan Tranche Details tab has the following data:")
    public void loanTrancheDetailsTabCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "all", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        Set<GetLoansLoanIdDisbursementDetails> disbursementDetails = loanDetailsResponse.body().getDisbursementDetails();
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String expectedDisbursementDateExpected = expectedValues.get(0);

            Set<List<String>> actualValuesList = disbursementDetails.stream()//
                    .filter(t -> expectedDisbursementDateExpected.equals(FORMATTER.format(t.getExpectedDisbursementDate())))//
                    .map(t -> fetchValuesOfDisbursementDetails(table.row(0), t))//
                    .collect(Collectors.toSet());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInDisbursementDetailsTab(resourceId, i, actualValuesList, expectedValues))
                    .isTrue();
        }
        assertThat(disbursementDetails.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(resourceId, disbursementDetails.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    @And("Admin successfully disburse the loan without auto downpayment on {string} with {string} EUR transaction amount")
    public void disburseLoanWithoutAutoDownpayment(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi
                .stateTransitions(loanId, disburseRequest, "disburseWithoutAutoDownPayment").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorHelper.checkSuccessfulApiCall(loanDisburseResponse);
        Long statusActual = loanDisburseResponse.body().getChanges().getStatus().getId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        Long statusExpected = Long.valueOf(loanDetails.body().getStatus().getId());

        assertThat(statusActual)//
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))//
                .isEqualTo(statusExpected);//
        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
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

        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
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
        log.debug("Error message: {}", developerMessage);
    }

    @Then("Admin fails to disburse the loan on {string} with {string} amount")
    public void disburseLoanFailureIsNotAllowed(String disbursementDate, String disbursementAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest().actualDisbursementDate(disbursementDate)
                .transactionAmount(new BigDecimal(disbursementAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.disburseIsNotAllowedFailure());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of charge-off that was performed for the loan")
    public void disburseChargedOffLoanFailure(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        String developerMessage = errorDetails.getSingleError().getDeveloperMessage();

        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(developerMessage).matches(ErrorMessageHelper.disburseChargedOffLoanFailure());
        log.debug("Error message: {}", developerMessage);
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

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount due to exceed approved amount")
    public void disbursementForbiddenExceedApprovedAmount(String actualDisbursementDate, String transactionAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addDisbursementExceedApprovedAmountFailure()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addDisbursementExceedApprovedAmountFailure());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR trn amount with total disb amount {string} and max disb amount {string} due to exceed max applied amount")
    public void disbursementForbiddenExceedMaxAppliedAmount(String actualDisbursementDate, String transactionAmount,
            String totalDisbursalAmount, String maxDisbursalAmount) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        Response<PostLoansLoanIdResponse> loanDisburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse").execute();
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        ErrorResponse errorDetails = ErrorResponse.from(loanDisburseResponse);
        assertThat(errorDetails.getHttpStatusCode())
                .as(ErrorMessageHelper.addDisbursementExceedMaxAppliedAmountFailure(totalDisbursalAmount, maxDisbursalAmount))
                .isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addDisbursementExceedMaxAppliedAmountFailure(totalDisbursalAmount, maxDisbursalAmount));
    }

    @And("Admin does charge-off the loan on {string}")
    public void chargeOffLoan(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);
        ErrorHelper.checkSuccessfulApiCall(chargeOffResponse);

        Long transactionId = chargeOffResponse.body().getResourceId();
        eventAssertion.assertEvent(LoanChargeOffEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId).isEqualTo(chargeOffResponse.body().getResourceId());
    }

    @Then("Backdated charge-off on a date {string} is forbidden")
    public void chargeOffBackdatedForbidden(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);
        assertThat(chargeOffResponse.isSuccessful()).isFalse();

        String string = chargeOffResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(string, ErrorResponse.class);

        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        String developerMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        Integer httpStatusCodeExpected = 403;
        String developerMessageExpected = String.format(
                "Loan: %s charge-off cannot be executed. Loan has monetary activity after the charge-off transaction date!", loanId);

        assertThat(httpStatusCodeActual)
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(developerMessageActual)
                .as(ErrorMessageHelper.wrongErrorMessageInFailedChargeAdjustment(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);
    }

    @And("Admin does charge-off the loan with reason {string} on {string}")
    public void chargeOffLoan(String chargeOffReason, String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        final CodeValue chargeOffReasonCodeValue = DefaultCodeValue.valueOf(chargeOffReason);
        Long chargeOffReasonCodeId = codeHelper.retrieveCodeByName(CHARGE_OFF_REASONS).getId();
        long chargeOffReasonId = codeValueResolver.resolve(chargeOffReasonCodeId, chargeOffReasonCodeValue);

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest()
                .chargeOffReasonId(chargeOffReasonId).transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        ErrorHelper.checkSuccessfulApiCall(chargeOffResponse);

        Long transactionId = chargeOffResponse.body().getResourceId();

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        assert loanDetailsResponse.body() != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final Optional<GetLoansLoanIdTransactions> transactionsMatch = transactions.stream()
                .filter(t -> formatter.format(t.getDate()).equals(transactionDate) && t.getType().getCapitalizedIncomeAmortization())
                .reduce((one, two) -> two);
        if (transactionsMatch.isPresent()) {
            testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_AMORTIZATION_ID, transactionsMatch.get().getId());
        }
        eventAssertion.assertEvent(LoanChargeOffEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId).isEqualTo(chargeOffResponse.body().getResourceId());
    }

    @Then("Charge-off attempt on {string} results an error")
    public void chargeOffOnLoanWithInterestFails(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);
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
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.secondChargeOffFailure(loanId)).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.secondChargeOffFailure(loanId));
    }

    @And("Admin does a charge-off undo the loan")
    public void chargeOffUndo() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffUndoResponse = undoChargeOff(loanId);
        ErrorHelper.checkSuccessfulApiCall(chargeOffUndoResponse);

        Long transactionId = chargeOffUndoResponse.body().getResourceId();
        eventAssertion.assertEventRaised(LoanChargeOffUndoEvent.class, transactionId);
    }

    public Response<PostLoansLoanIdTransactionsResponse> undoChargeOff(Long loanId) throws IOException {
        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffUndoResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffUndoRequest, "undo-charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE, chargeOffUndoResponse);
        return chargeOffUndoResponse;
    }

    public Response<PostLoansLoanIdTransactionsResponse> makeChargeOffTransaction(Long loanId, String transactionDate) throws IOException {
        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, chargeOffRequest, "charge-off").execute();
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        return chargeOffResponse;
    }

    @Then("Charge-off transaction is not possible on {string}")
    public void chargeOffFailure(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);

        ErrorResponse errorDetails = ErrorResponse.from(chargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.chargeOffUndoFailure(loanId));
    }

    @Then("Charge-off transaction is not possible on {string} due to monetary activity before")
    public void chargeOffFailureDueToMonetaryActivityBefore(String transactionDate) throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);

        ErrorResponse errorDetails = ErrorResponse.from(chargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffFailureDueToMonetaryActivityBefore(loanId))
                .isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.chargeOffFailureDueToMonetaryActivityBefore(loanId));
    }

    @Then("Charge-off undo is not possible as the loan is not charged-off")
    public void chargeOffUndoNotPossibleFailure() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        Response<PostLoansLoanIdTransactionsResponse> undoChargeOffResponse = undoChargeOff(loanId);
        ErrorResponse errorDetails = ErrorResponse.from(undoChargeOffResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.notChargedOffFailure(loanId));
    }

    @Then("Loan has {double} outstanding amount")
    public void loanOutstanding(double totalOutstandingExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOutstandingActual = loanDetailsResponse.body().getSummary().getTotalOutstanding().doubleValue();
        assertThat(totalOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalOutstandingActual, totalOutstandingExpected))
                .isEqualTo(totalOutstandingExpected);
    }

    @Then("Loan has {double} interest outstanding amount")
    public void loanInterestOutstanding(double totalInterestOutstandingExpected) throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanCreateResponse.body() != null;
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assert loanDetailsResponse.body() != null;
        assert loanDetailsResponse.body().getSummary() != null;
        assert loanDetailsResponse.body().getSummary().getInterestOutstanding() != null;
        final double totalInterestOutstandingActual = loanDetailsResponse.body().getSummary().getInterestOutstanding().doubleValue();
        assertThat(totalInterestOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalInterestOutstandingActual, totalInterestOutstandingExpected))
                .isEqualTo(totalInterestOutstandingExpected);
    }

    @Then("Loan has {double} total unpaid payable due interest")
    public void loanTotalUnpaidPayableDueInterest(double totalUnpaidPayableDueInterestExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "repaymentSchedule", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalUnpaidPayableDueInterestActual = loanDetailsResponse.body().getSummary().getTotalUnpaidPayableDueInterest()
                .doubleValue();
        assertThat(totalUnpaidPayableDueInterestActual).as(ErrorMessageHelper
                .wrongAmountInTotalUnpaidPayableDueInterest(totalUnpaidPayableDueInterestActual, totalUnpaidPayableDueInterestExpected))
                .isEqualTo(totalUnpaidPayableDueInterestExpected);
    }

    @Then("Loan has {double} overpaid amount")
    public void loanOverpaid(double totalOverpaidExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOverpaidActual = loanDetailsResponse.body().getTotalOverpaid().doubleValue();
        Double totalOutstandingActual = loanDetailsResponse.body().getSummary().getTotalOutstanding().doubleValue();
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

        Double totalOverdueActual = loanDetailsResponse.body().getSummary().getTotalOverdue().doubleValue();
        assertThat(totalOverdueActual).as(ErrorMessageHelper.wrongAmountInTotalOverdue(totalOverdueActual, totalOverdueExpected))
                .isEqualTo(totalOverdueExpected);
    }

    @Then("Loan has {double} total interest overdue amount")
    public void loanInterestOverdue(final double totalInterestOverdueExpected) throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanCreateResponse.body() != null;
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assert Objects.requireNonNull(loanDetailsResponse.body()).getSummary() != null;
        assert loanDetailsResponse.body().getSummary().getInterestOverdue() != null;
        final double totalInterestOverdueActual = loanDetailsResponse.body().getSummary().getInterestOverdue().doubleValue();
        assertThat(totalInterestOverdueActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOverdue(totalInterestOverdueActual, totalInterestOverdueExpected))
                .isEqualTo(totalInterestOverdueExpected);
    }

    @Then("Loan has {double} last payment amount")
    public void loanLastPaymentAmount(double lastPaymentAmountExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double lastPaymentAmountActual = loanDetailsResponse.body().getDelinquent().getLastPaymentAmount().doubleValue();
        assertThat(lastPaymentAmountActual)
                .as(ErrorMessageHelper.wrongLastPaymentAmount(lastPaymentAmountActual, lastPaymentAmountExpected))
                .isEqualTo(lastPaymentAmountExpected);
    }

    @Then("Loan Repayment schedule has {int} periods, with the following data for periods:")
    public void loanRepaymentSchedulePeriodsCheck(int linesExpected, DataTable table) throws IOException {

        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

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
                    .as(ErrorMessageHelper.wrongValueInLineInRepaymentSchedule(resourceId, i, actualValuesList, expectedValues)).isTrue();

            assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInRepaymentSchedule(resourceId, linesActual, linesExpected))
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
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);

        List<List<String>> actualValuesList = transactions.stream().filter(t -> date.equals(FORMATTER.format(t.getDate())))
                .map(t -> fetchValuesOfTransaction(data.get(0), t)).collect(Collectors.toList());
        boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

        assertThat(containsExpectedValues)
                .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, 1, actualValuesList, expectedValues)).isTrue();
    }

    @Then("Loan Transactions tab has the following data:")
    public void loanTransactionsTabCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String transactionDateExpected = expectedValues.get(0);
            List<List<String>> actualValuesList = transactions.stream()//
                    .filter(t -> transactionDateExpected.equals(FORMATTER.format(t.getDate())))//
                    .map(t -> fetchValuesOfTransaction(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
        assertThat(transactions.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(resourceId, transactions.size(), data.size() - 1))
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

    @Then("In Loan Transactions the {string}th Transaction with type={string} and date {string} has non-null external-id")
    public void loanTransactionsNthTransactionHasNonNullExternalId(String nthTransactionStr, String transactionType, String transactionDate)
            throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        int nthItem = Integer.parseInt(nthTransactionStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .toList().get(nthItem);//

        assertThat(targetTransaction.getExternalId()).as(ErrorMessageHelper.transactionHasNullResourceValue(transactionType, "external-id"))
                .isNotNull();
        testContext().set(TestContextKey.LOAN_TRANSACTION_RESPONSE, targetTransaction);
    }

    @Then("In Loan Transactions all transactions have non-null external-id")
    public void loanTransactionsHaveNonNullExternalId() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        assertThat(transactions.stream().allMatch(transaction -> transaction.getExternalId() != null))
                .as(ErrorMessageHelper.transactionHasNullResourceValue("", "external-id")).isTrue();
    }

    @Then("Check required transaction for non-null eternal-id")
    public void loanTransactionHasNonNullExternalId() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        GetLoansLoanIdTransactions targetTransaction = testContext().get(TestContextKey.LOAN_TRANSACTION_RESPONSE);
        Long targetTransactionId = targetTransaction.getId();

        Response<GetLoansLoanIdTransactionsTransactionIdResponse> transactionResponse = loanTransactionsApi
                .retrieveTransaction(loanId, targetTransactionId, "").execute();

        GetLoansLoanIdTransactionsTransactionIdResponse transaction = transactionResponse.body();
        assertThat(transaction.getExternalId())
                .as(ErrorMessageHelper.transactionHasNullResourceValue(transaction.getType().getCode(), "external-id")).isNotNull();
    }

    @Then("Loan Transactions tab has none transaction")
    public void loanTransactionsTabNoneTransaction() throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        assertThat(transactions.size()).isZero();
    }

    @Then("Loan Charges tab has a given charge with the following data:")
    public void loanChargesGivenChargeDataCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "charges", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.body().getCharges();

        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String paymentDueAtExpected = expectedValues.get(2);
        String dueAsOfExpected = expectedValues.get(3);
        List<List<String>> actualValuesList = getActualValuesList(charges, paymentDueAtExpected, dueAsOfExpected);

        boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

        assertThat(containsExpectedValues)
                .as(ErrorMessageHelper.wrongValueInLineInChargesTab(resourceId, 1, actualValuesList, expectedValues)).isTrue();
    }

    @Then("Loan Charges tab has the following data:")
    public void loanChargesTabCheck(DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

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

            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInChargesTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
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

            actualValues.add(t.getAmount() == null ? null : new Utils.DoubleFormatter(t.getAmount().doubleValue()).format());

            actualValues.add(t.getAmountPaid() == null ? null : new Utils.DoubleFormatter(t.getAmountPaid().doubleValue()).format());
            actualValues.add(t.getAmountWaived() == null ? null : new Utils.DoubleFormatter(t.getAmountWaived().doubleValue()).format());

            actualValues.add(
                    t.getAmountOutstanding() == null ? null : new Utils.DoubleFormatter(t.getAmountOutstanding().doubleValue()).format());
            return actualValues;
        }).collect(Collectors.toList());
    }

    @Then("Loan status will be {string}")
    public void loanStatus(String statusExpected) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        Integer loanStatusActualValue = loanDetailsResponse.body().getStatus().getId();

        LoanStatus loanStatusExpected = LoanStatus.valueOf(statusExpected);
        Integer loanStatusExpectedValue = loanStatusExpected.getValue();

        assertThat(loanStatusActualValue).as(ErrorMessageHelper.wrongLoanStatus(resourceId, loanStatusActualValue, loanStatusExpectedValue))
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

    @Then("Loan closedon_date is {string}")
    public void loanClosedonDate(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        if ("null".equals(date)) {
            assertThat(loanDetailsResponse.body().getTimeline().getClosedOnDate()).isNull();
        } else {
            assertThat(FORMATTER.format(loanDetailsResponse.body().getTimeline().getClosedOnDate())).isEqualTo(date);
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
        await().atMost(Duration.ofMillis(eventProperties.getWaitTimeoutInMillis()))//
                .pollDelay(Duration.ofMillis(eventProperties.getDelayInMillis())) //
                .pollInterval(Duration.ofMillis(eventProperties.getIntervalInMillis()))//
                .untilAsserted(() -> {
                    eventAssertion.assertEvent(LoanStatusChangedEvent.class, loanId).extractingData(LoanAccountDataV1::getStatus)
                            .isEqualTo(expectedStatus);
                });
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
        log.debug("Loan ID: {}", loanId);
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
        await().atMost(Duration.ofMillis(jobPollingProperties.getTimeoutInMillis())) //
                .pollInterval(Duration.ofMillis(jobPollingProperties.getIntervalInMillis())) //
                .until(() -> {
                    Response<IsCatchUpRunningDTO> isCatchUpRunningResponse = loanCobCatchUpApi.isCatchUpRunning().execute();
                    ErrorHelper.checkSuccessfulApiCall(isCatchUpRunningResponse);
                    IsCatchUpRunningDTO isCatchUpRunning = isCatchUpRunningResponse.body();
                    return isCatchUpRunning.getCatchUpRunning();
                });
        // Then wait for catch-up to complete
        await().atMost(Duration.ofMinutes(4)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5)).until(() -> {
            // Check if catch-up is still running
            Response<IsCatchUpRunningDTO> statusResponse = loanCobCatchUpApi.isCatchUpRunning().execute();
            ErrorHelper.checkSuccessfulApiCall(statusResponse);

            // Only proceed with date check if catch-up is not running
            if (!statusResponse.body().getCatchUpRunning()) {
                // Get the current business date
                Response<BusinessDateResponse> businessDateResponse = businessDateApi.getBusinessDate(BusinessDateHelper.COB).execute();
                ErrorHelper.checkSuccessfulApiCall(businessDateResponse);
                LocalDate currentBusinessDate = businessDateResponse.body().getDate();

                // Get the last closed business date
                Response<OldestCOBProcessedLoanDTO> catchUpResponse = loanCobCatchUpApi.getOldestCOBProcessedLoan().execute();
                ErrorHelper.checkSuccessfulApiCall(catchUpResponse);
                LocalDate lastClosedDate = catchUpResponse.body().getCobBusinessDate();

                // Verify that the last closed date is not before the current business date
                return !lastClosedDate.isBefore(currentBusinessDate);
            }
            return false;
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
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual".equals(t.getType().getValue()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual transaction found on %s", date)));
        Long accrualTransactionId = accrualTransaction.getId();

        eventAssertion.assertEventRaised(LoanAccrualTransactionCreatedBusinessEvent.class, accrualTransactionId);
    }

    @Then("LoanAccrualAdjustmentTransactionBusinessEvent is raised on {string}")
    public void checkLoanAccrualAdjustmentTransactionBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions accrualAdjustmentTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Adjustment".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual Adjustment transaction found on %s", date)));
        Long accrualAdjustmentTransactionId = accrualAdjustmentTransaction.getId();

        eventAssertion.assertEventRaised(LoanAccrualAdjustmentTransactionBusinessEvent.class, accrualAdjustmentTransactionId);
    }

    @Then("LoanChargeAdjustmentPostBusinessEvent is raised on {string}")
    public void checkLoanChargeAdjustmentPostBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        GetLoansLoanIdTransactions loadTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Charge Adjustment".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Charge Adjustment transaction found on %s", date)));

        eventAssertion.assertEventRaised(LoanChargeAdjustmentPostBusinessEvent.class, loadTransaction.getId());
    }

    @Then("BulkBusinessEvent is not raised on {string}")
    public void checkLoanBulkBusinessEventNotCreatedBusinessEvent(String date) {
        eventAssertion.assertEventNotRaised(BulkBusinessEvent.class, em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    @Then("LoanAccrualTransactionCreatedBusinessEvent is not raised on {string}")
    public void checkLoanAccrualTransactionNotCreatedBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        assertThat(transactions).as("Unexpected Accrual activity transaction found on %s", date)
                .noneMatch(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Activity".equals(t.getType().getValue()));

        eventAssertion.assertEventNotRaised(LoanAccrualTransactionCreatedBusinessEvent.class,
                em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    @Then("{string} transaction on {string} got reverse-replayed on {string}")
    public void checkLoanAdjustTransactionBusinessEvent(String transactionType, String transactionDate, String submittedOnDate)
            throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        GetLoansLoanIdTransactions loadTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionType, transactionDate)));

        Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations = loadTransaction.getTransactionRelations();
        Long originalTransactionId = transactionRelations.stream().map(GetLoansLoanIdLoanTransactionRelation::getToLoanTransaction)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction was reversed, but not replayed!"));

        // Check whether reverse-replay event got occurred
        eventAssertion.assertEvent(LoanAdjustTransactionBusinessEvent.class, originalTransactionId).extractingData(
                e -> e.getNewTransactionDetail() != null && e.getNewTransactionDetail().getId().equals(loadTransaction.getId()));
        // Check whether there was just ONE event related to this transaction
        eventAssertion.assertEventNotRaised(LoanAdjustTransactionBusinessEvent.class, originalTransactionId);
        assertThat(FORMATTER.format(loadTransaction.getSubmittedOnDate()))
                .as("Loan got replayed on %s", loadTransaction.getSubmittedOnDate()).isEqualTo(submittedOnDate);
    }

    @When("Save external ID of {string} transaction made on {string} as {string}")
    public void saveExternalIdForTransaction(String transactionName, String transactionDate, String externalIdKey) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        GetLoansLoanIdTransactions loadTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionName.equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionName, transactionDate)));

        String externalId = loadTransaction.getExternalId();
        testContext().set(externalIdKey, externalId);
        log.debug("Transaction external ID: {} saved to testContext", externalId);
    }

    @Then("External ID of replayed {string} on {string} is matching with {string}")
    public void checkExternalIdForReplayedAccrualActivity(String transactionType, String transactionDate, String savedExternalIdKey)
            throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        GetLoansLoanIdTransactions transactionDetails = transactions.stream()
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionType, transactionDate)));

        Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations = transactionDetails.getTransactionRelations();
        Long originalTransactionId = transactionRelations.stream().map(GetLoansLoanIdLoanTransactionRelation::getToLoanTransaction)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction was reversed, but not replayed!"));

        String externalIdExpected = testContext().get(savedExternalIdKey).toString();
        String externalIdActual = transactionDetails.getExternalId();
        assertThat(externalIdActual).as(ErrorMessageHelper.wrongExternalID(externalIdActual, externalIdExpected))
                .isEqualTo(externalIdExpected);

        Response<GetLoansLoanIdTransactionsTransactionIdResponse> originalTransaction = loanTransactionsApi
                .retrieveTransaction(loanId, originalTransactionId, "").execute();
        assertNull(originalTransaction.body().getExternalId(),
                String.format("Original transaction external id is not null %n%s", originalTransaction.body()));
    }

    @Then("LoanTransactionAccrualActivityPostBusinessEvent is raised on {string}")
    public void checkLoanTransactionAccrualActivityPostBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions accrualTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Activity".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual activity transaction found on %s", date)));
        Long accrualTransactionId = accrualTransaction.getId();

        eventAssertion.assertEventRaised(LoanTransactionAccrualActivityPostEvent.class, accrualTransactionId);
    }

    @Then("LoanRescheduledDueAdjustScheduleBusinessEvent is raised on {string}")
    public void checkLoanRescheduledDueAdjustScheduleBusinessEvent(String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        eventAssertion.assertEventRaised(LoanRescheduledDueAdjustScheduleEvent.class, loanId);
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
        String lastPaymentAmountActual = delinquent.getLastPaymentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getLastPaymentAmount().doubleValue()).format();
        String lastPaymentDateActual = FORMATTER.format(delinquent.getLastPaymentDate());
        String lastRepaymentAmountActual = delinquent.getLastRepaymentAmount() == null ? null
                : new Utils.DoubleFormatter(delinquent.getLastRepaymentAmount().doubleValue()).format();
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
        log.debug("loanProductId: {}", loanProductId);

        Response<GetLoanProductsProductIdResponse> loanProductDetails = loanProductsApi.retrieveLoanProductDetails(loanProductId).execute();
        ErrorHelper.checkSuccessfulApiCall(loanProductDetails);
        List<AdvancedPaymentData> paymentAllocation = loanProductDetails.body().getPaymentAllocation();

        List<AdvancedPaymentData> newPaymentAllocation = new ArrayList<>();
        paymentAllocation.forEach(e -> {
            String transactionTypeOriginal = e.getTransactionType();
            String futureInstallmentAllocationRule = e.getFutureInstallmentAllocationRule();
            List<PaymentAllocationOrder> paymentAllocationOrder = e.getPaymentAllocationOrder();
            if (transactionTypeToChange.equals(transactionTypeOriginal)) {
                futureInstallmentAllocationRule = futureInstallmentAllocationRuleNew;
            }
            newPaymentAllocation.add(LoanProductGlobalInitializerStep.editPaymentAllocationFutureInstallment(transactionTypeOriginal,
                    futureInstallmentAllocationRule, paymentAllocationOrder));
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
        log.debug("loanProductId: {}", loanProductId);

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
        Double totalRepaymentTransaction = loanDetails.body().getSummary().getTotalRepaymentTransaction().doubleValue();

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

    @Then("In Loan Transactions the {string}th Transaction has relationship type={} with the {string}th Transaction")
    public void loanTransactionsRelationshipCheck(String nthTransactionFromStr, String relationshipType, String nthTransactionToStr)
            throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final int nthTransactionFrom = nthTransactionFromStr == null ? transactions.size() - 1
                : Integer.parseInt(nthTransactionFromStr) - 1;
        final int nthTransactionTo = nthTransactionToStr == null ? transactions.size() - 1 : Integer.parseInt(nthTransactionToStr) - 1;
        final GetLoansLoanIdTransactions transactionFrom = transactions.get(nthTransactionFrom);
        final GetLoansLoanIdTransactions transactionTo = transactions.get(nthTransactionTo);

        final Optional<GetLoansLoanIdLoanTransactionRelation> relationshipOptional = transactionFrom.getTransactionRelations().stream()
                .filter(r -> r.getRelationType().equals(relationshipType))
                .filter(r -> r.getToLoanTransaction().equals(transactionTo.getId())).findFirst();

        assertTrue(relationshipOptional.isPresent(), "Missed relationship between transactions");
    }

    @Then("Loan Product Charge-Off reasons options from loan product template have {int} options, with the following data:")
    public void loanProductTemplateChargeOffReasonOptionsCheck(final int linesExpected, final DataTable table) throws IOException {
        final Response<GetLoanProductsTemplateResponse> loanProductDetails = loanProductsApi.retrieveTemplate11(false).execute();
        ErrorHelper.checkSuccessfulApiCall(loanProductDetails);

        assertNotNull(loanProductDetails.body());
        final List<GetLoanProductsChargeOffReasonOptions> chargeOffReasonOptions = loanProductDetails.body().getChargeOffReasonOptions();
        assertNotNull(chargeOffReasonOptions);

        final List<List<String>> data = table.asLists();
        final int linesActual = chargeOffReasonOptions.size();
        data.stream().skip(1) // skip headers
                .forEach(expectedValues -> {
                    final List<List<String>> actualValuesList = chargeOffReasonOptions.stream()
                            .map(chargeOffReason -> fetchValuesOfLoanChargeOffReasonOptions(data.get(0), chargeOffReason))
                            .collect(Collectors.toList());

                    final boolean containsExpectedValues = actualValuesList.stream()
                            .anyMatch(actualValues -> actualValues.equals(expectedValues));
                    assertThat(containsExpectedValues).as(ErrorMessageHelper
                            .wrongValueInLineInChargeOffReasonOptions(data.indexOf(expectedValues), actualValuesList, expectedValues))
                            .isTrue();

                    assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInChargeOffReasonOptions(linesActual, linesExpected))
                            .isEqualTo(linesExpected);
                });
    }

    @Then("Loan Product {string} Charge-Off reasons options from specific loan product have {int} options, with the following data:")
    public void specificLoanProductChargeOffReasonOptionsCheck(final String loanProductName, final int linesExpected, final DataTable table)
            throws IOException {
        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProductName);
        final Long loanProductId = loanProductResolver.resolve(product);
        final Response<GetLoanProductsProductIdResponse> loanProductDetails = loanProductsCustomApi
                .retrieveLoanProductDetails(loanProductId, "true").execute();
        ErrorHelper.checkSuccessfulApiCall(loanProductDetails);

        assertNotNull(loanProductDetails.body());
        final List<GetLoanProductsChargeOffReasonOptions> chargeOffReasonOptions = loanProductDetails.body().getChargeOffReasonOptions();
        assertNotNull(chargeOffReasonOptions);

        final List<List<String>> data = table.asLists();
        final int linesActual = chargeOffReasonOptions.size();
        data.stream().skip(1) // skip headers
                .forEach(expectedValues -> {
                    final List<List<String>> actualValuesList = chargeOffReasonOptions.stream()
                            .map(chargeOffReason -> fetchValuesOfLoanChargeOffReasonOptions(data.get(0), chargeOffReason))
                            .collect(Collectors.toList());

                    final boolean containsExpectedValues = actualValuesList.stream()
                            .anyMatch(actualValues -> actualValues.equals(expectedValues));
                    assertThat(containsExpectedValues).as(ErrorMessageHelper
                            .wrongValueInLineInChargeOffReasonOptions(data.indexOf(expectedValues), actualValuesList, expectedValues))
                            .isTrue();

                    assertThat(linesActual).as(ErrorMessageHelper.wrongNumberOfLinesInChargeOffReasonOptions(linesActual, linesExpected))
                            .isEqualTo(linesExpected);
                });
    }

    private void createCustomizedLoan(final List<String> loanData, final boolean withEmi) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestTypeStr = loanData.get(4);
        final String interestCalculationPeriodStr = loanData.get(5);
        final String amortizationTypeStr = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyTypeStr = loanData.get(10);
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

        final RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        final InterestType interestType = InterestType.valueOf(interestTypeStr);
        final Integer interestTypeValue = interestType.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        final AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        final Integer amortizationTypeValue = amortizationType.getValue();

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

    public void createFullyCustomizedLoanWithInterestRateFrequency(final List<String> loanData) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestTypeStr = loanData.get(4);
        final String interestCalculationPeriodStr = loanData.get(5);
        final String amortizationTypeStr = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyTypeStr = loanData.get(10);
        final Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        final Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        final Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        final Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        final String transactionProcessingStrategyCode = loanData.get(15);
        final String interestRateFrequencyTypeStr = loanData.get(16);

        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        final Long loanProductId = loanProductResolver.resolve(product);

        final LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        final Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        final RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        final InterestType interestType = InterestType.valueOf(interestTypeStr);
        final Integer interestTypeValue = interestType.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        final AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        final Integer amortizationTypeValue = amortizationType.getValue();

        final TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        final String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        InterestRateFrequencyType interestRateFrequencyType = InterestRateFrequencyType.valueOf(interestRateFrequencyTypeStr);
        Integer interestRateFrequencyTypeValue = interestRateFrequencyType.value;

        final PostLoansRequest loansRequest = loanRequestFactory//
                .defaultLoansRequest(clientId)//
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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .interestRateFrequencyType(interestRateFrequencyTypeValue);//

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithCharges(final List<String> loanData) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestTypeStr = loanData.get(4);
        final String interestCalculationPeriodStr = loanData.get(5);
        final String amortizationTypeStr = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyTypeStr = loanData.get(10);
        final Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        final Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        final Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        final Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        final String transactionProcessingStrategyCode = loanData.get(15);
        final String chargesCalculationType = loanData.get(16);
        final BigDecimal chargesAmount = new BigDecimal(loanData.get(17));

        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        final Long loanProductId = loanProductResolver.resolve(product);

        final LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        final Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        final RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        final InterestType interestType = InterestType.valueOf(interestTypeStr);
        final Integer interestTypeValue = interestType.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        final AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        final Integer amortizationTypeValue = amortizationType.getValue();

        final TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        final String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargesCalculationType);
        Long chargeId = chargeProductType.getValue();

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        charges.add(new PostLoansRequestChargeData().chargeId(chargeId).amount(chargesAmount));

        final PostLoansRequest loansRequest = loanRequestFactory//
                .defaultLoansRequest(clientId)//
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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .charges(charges);//

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementDetails(final List<String> loanData) throws IOException {
        final String expectedDisbursementDate = loanData.get(18);
        final Double disbursementPrincipalAmount = Double.valueOf(loanData.get(19));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDate)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmount)));

        createFullyCustomizedLoanWithChargesExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementsDetails(final List<String> loanData) throws IOException {
        final String expectedDisbursementDateFirstDisbursal = loanData.get(18);
        final Double disbursementPrincipalAmountFirstDisbursal = Double.valueOf(loanData.get(19));

        final String expectedDisbursementDateSecondDisbursal = loanData.get(20);
        final Double disbursementPrincipalAmountSecondDisbursal = Double.valueOf(loanData.get(21));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateFirstDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountFirstDisbursal)));
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateSecondDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountSecondDisbursal)));

        createFullyCustomizedLoanWithChargesExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanWithChargesExpectsTrancheDisbursementDetails(final List<String> loanData,
            List<PostLoansDisbursementData> disbursementDetail) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestTypeStr = loanData.get(4);
        final String interestCalculationPeriodStr = loanData.get(5);
        final String amortizationTypeStr = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyTypeStr = loanData.get(10);
        final Integer numberOfRepayments = Integer.valueOf(loanData.get(11));
        final Integer graceOnPrincipalPayment = Integer.valueOf(loanData.get(12));
        final Integer graceOnInterestPayment = Integer.valueOf(loanData.get(13));
        final Integer graceOnInterestCharged = Integer.valueOf(loanData.get(14));
        final String transactionProcessingStrategyCode = loanData.get(15);
        final String chargesCalculationType = loanData.get(16);
        final BigDecimal chargesAmount = new BigDecimal(loanData.get(17));

        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        final Long loanProductId = loanProductResolver.resolve(product);

        final LoanTermFrequencyType termFrequencyType = LoanTermFrequencyType.valueOf(loanTermFrequencyType);
        final Integer loanTermFrequencyTypeValue = termFrequencyType.getValue();

        final RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        final InterestType interestType = InterestType.valueOf(interestTypeStr);
        final Integer interestTypeValue = interestType.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        final AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        final Integer amortizationTypeValue = amortizationType.getValue();

        final TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        final String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargesCalculationType);
        Long chargeId = chargeProductType.getValue();

        List<PostLoansRequestChargeData> charges = new ArrayList<>();
        charges.add(new PostLoansRequestChargeData().chargeId(chargeId).amount(chargesAmount));

        final PostLoansRequest loansRequest = loanRequestFactory//
                .defaultLoansRequest(clientId)//
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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .disbursementData(disbursementDetail)//
                .charges(charges);//

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithExpectedTrancheDisbursementDetails(final List<String> loanData) throws IOException {
        final String expectedDisbursementDate = loanData.get(16);
        final Double disbursementPrincipalAmount = Double.valueOf(loanData.get(17));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDate)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmount)));

        createFullyCustomizedLoanExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanWithExpectedTrancheDisbursementsDetails(final List<String> loanData) throws IOException {
        final String expectedDisbursementDateFirstDisbursal = loanData.get(16);
        final Double disbursementPrincipalAmountFirstDisbursal = Double.valueOf(loanData.get(17));

        final String expectedDisbursementDateSecondDisbursal = loanData.get(18);
        final Double disbursementPrincipalAmountSecondDisbursal = Double.valueOf(loanData.get(19));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateFirstDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountFirstDisbursal)));
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateSecondDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountSecondDisbursal)));

        createFullyCustomizedLoanExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanExpectsTrancheDisbursementDetails(final List<String> loanData,
            List<PostLoansDisbursementData> disbursementDetail) throws IOException {
        final String loanProduct = loanData.get(0);
        final String submitDate = loanData.get(1);
        final String principal = loanData.get(2);
        final BigDecimal interestRate = new BigDecimal(loanData.get(3));
        final String interestTypeStr = loanData.get(4);
        final String interestCalculationPeriodStr = loanData.get(5);
        final String amortizationTypeStr = loanData.get(6);
        final Integer loanTermFrequency = Integer.valueOf(loanData.get(7));
        final String loanTermFrequencyType = loanData.get(8);
        final Integer repaymentFrequency = Integer.valueOf(loanData.get(9));
        final String repaymentFrequencyTypeStr = loanData.get(10);
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

        final RepaymentFrequencyType repaymentFrequencyType = RepaymentFrequencyType.valueOf(repaymentFrequencyTypeStr);
        final Integer repaymentFrequencyTypeValue = repaymentFrequencyType.getValue();

        final InterestType interestType = InterestType.valueOf(interestTypeStr);
        final Integer interestTypeValue = interestType.getValue();

        final InterestCalculationPeriodTime interestCalculationPeriod = InterestCalculationPeriodTime.valueOf(interestCalculationPeriodStr);
        final Integer interestCalculationPeriodValue = interestCalculationPeriod.getValue();

        final AmortizationType amortizationType = AmortizationType.valueOf(amortizationTypeStr);
        final Integer amortizationTypeValue = amortizationType.getValue();

        final TransactionProcessingStrategyCode processingStrategyCode = TransactionProcessingStrategyCode
                .valueOf(transactionProcessingStrategyCode);
        final String transactionProcessingStrategyCodeValue = processingStrategyCode.getValue();

        final PostLoansRequest loansRequest = loanRequestFactory//
                .defaultLoansRequest(clientId)//
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
                .graceOnInterestPayment(graceOnInterestCharged)//
                .transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)//
                .disbursementData(disbursementDetail);//

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new zero charge-off Loan with interest recalculation and date: {string}")
    public void createLoanWithInterestRecalculationAndZeroChargeOffBehaviour(final String date) throws IOException {
        createLoanWithZeroChargeOffBehaviour(date, true);
    }

    @When("Admin creates a new zero charge-off Loan without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndZeroChargeOffBehaviour(final String date) throws IOException {
        createLoanWithZeroChargeOffBehaviour(date, false);
    }

    private void createLoanWithZeroChargeOffBehaviour(final String date, final boolean isInterestRecalculation) throws IOException {
        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final DefaultLoanProduct product = isInterestRecalculation
                ? DefaultLoanProduct
                        .valueOf(LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR.getName())
                : DefaultLoanProduct.valueOf(LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR.getName());

        final Long loanProductId = loanProductResolver.resolve(product);

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).productId(loanProductId)
                .principal(new BigDecimal(100)).numberOfRepayments(6).submittedOnDate(date).expectedDisbursementDate(date)
                .loanTermFrequency(6)//
                .loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value)//
                .interestRateFrequencyType(3)//
                .interestRatePerPeriod(new BigDecimal(7))//
                .interestType(InterestType.DECLINING_BALANCE.value)//
                .interestCalculationPeriodType(isInterestRecalculation ? InterestCalculationPeriodTime.DAILY.value
                        : InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.value);

        final Response<PostLoansResponse> response = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loansRequest, "").execute();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);

        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndAccelerateMaturityChargeOffBehaviour(final String date) throws IOException {
        createLoanWithLoanBehaviour(date, false,
                DefaultLoanProduct.valueOf(LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR.getName()));
    }

    @When("Admin creates a new accelerate maturity charge-off Loan with last installment strategy, without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndAccelerateMaturityChargeOffBehaviourLastInstallmentStrategy(final String date)
            throws IOException {
        createLoanWithLoanBehaviour(date, false,
                DefaultLoanProduct.valueOf(LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY.getName()));
    }

    private void createLoanWithLoanBehaviour(final String date, final boolean isInterestRecalculation, final DefaultLoanProduct product)
            throws IOException {
        final Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.body().getClientId();

        final Long loanProductId = loanProductResolver.resolve(product);

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).productId(loanProductId)
                .principal(new BigDecimal(100)).numberOfRepayments(6).submittedOnDate(date).expectedDisbursementDate(date)
                .loanTermFrequency(6)//
                .loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value)//
                .interestRateFrequencyType(3)//
                .interestRatePerPeriod(new BigDecimal(7))//
                .interestType(InterestType.DECLINING_BALANCE.value)//
                .interestCalculationPeriodType(isInterestRecalculation ? InterestCalculationPeriodTime.DAILY.value
                        : InterestCalculationPeriodTime.SAME_AS_REPAYMENT_PERIOD.value)//
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION.value);

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

        String resourceId = String.valueOf(loanId);
        assertThat(statusActual)
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))
                .isEqualTo(statusExpected);
        eventCheckHelper.disburseLoanEventCheck(loanId);
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

    private List<String> fetchValuesOfTransaction(List<String> header, GetLoansLoanIdTransactions t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Transaction date" -> actualValues.add(t.getDate() == null ? null : FORMATTER.format(t.getDate()));
                case "Transaction Type" -> actualValues.add(t.getType().getValue() == null ? null : t.getType().getValue());
                case "Amount" ->
                    actualValues.add(t.getAmount() == null ? null : new Utils.DoubleFormatter(t.getAmount().doubleValue()).format());
                case "Principal" -> actualValues.add(
                        t.getPrincipalPortion() == null ? null : new Utils.DoubleFormatter(t.getPrincipalPortion().doubleValue()).format());
                case "Interest" -> actualValues.add(
                        t.getInterestPortion() == null ? null : new Utils.DoubleFormatter(t.getInterestPortion().doubleValue()).format());
                case "Fees" -> actualValues.add(t.getFeeChargesPortion() == null ? null
                        : new Utils.DoubleFormatter(t.getFeeChargesPortion().doubleValue()).format());
                case "Penalties" -> actualValues.add(t.getPenaltyChargesPortion() == null ? null
                        : new Utils.DoubleFormatter(t.getPenaltyChargesPortion().doubleValue()).format());
                case "Loan Balance" -> actualValues.add(t.getOutstandingLoanBalance() == null ? null
                        : new Utils.DoubleFormatter(t.getOutstandingLoanBalance().doubleValue()).format());
                case "Overpayment" -> actualValues.add(t.getOverpaymentPortion() == null ? null
                        : new Utils.DoubleFormatter(t.getOverpaymentPortion().doubleValue()).format());
                case "Reverted" -> actualValues.add(t.getManuallyReversed() == null ? null : String.valueOf(t.getManuallyReversed()));
                case "Replayed" -> {
                    boolean hasReplayed = t.getTransactionRelations().stream().anyMatch(e -> "REPLAYED".equals(e.getRelationType()));
                    actualValues.add(hasReplayed ? "true" : "false");
                }
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private List<String> fetchValuesOfDisbursementDetails(List<String> header, GetLoansLoanIdDisbursementDetails t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Expected Disbursement On" ->
                    actualValues.add(t.getExpectedDisbursementDate() == null ? null : FORMATTER.format(t.getExpectedDisbursementDate()));
                case "Disbursed On" ->
                    actualValues.add(t.getActualDisbursementDate() == null ? null : FORMATTER.format(t.getActualDisbursementDate()));
                case "Principal" -> actualValues.add(t.getPrincipal() == null ? null : String.valueOf(t.getPrincipal()));
                case "Net Disbursal Amount" ->
                    actualValues.add(t.getNetDisbursalAmount() == null ? null : String.valueOf(t.getNetDisbursalAmount()));
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

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
                        : new Utils.DoubleFormatter(repaymentPeriod.getPrincipalLoanBalanceOutstanding().doubleValue()).format());
                case "Principal due" -> actualValues.add(repaymentPeriod.getPrincipalDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getPrincipalDue().doubleValue()).format());
                case "Interest" -> actualValues.add(repaymentPeriod.getInterestDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getInterestDue().doubleValue()).format());
                case "Fees" -> actualValues.add(repaymentPeriod.getFeeChargesDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getFeeChargesDue().doubleValue()).format());
                case "Penalties" -> actualValues.add(repaymentPeriod.getPenaltyChargesDue() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getPenaltyChargesDue().doubleValue()).format());
                case "Due" -> actualValues.add(repaymentPeriod.getTotalDueForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalDueForPeriod().doubleValue()).format());
                case "Paid" -> actualValues.add(repaymentPeriod.getTotalPaidForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidForPeriod().doubleValue()).format());
                case "In advance" -> actualValues.add(repaymentPeriod.getTotalPaidInAdvanceForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidInAdvanceForPeriod().doubleValue()).format());
                case "Late" -> actualValues.add(repaymentPeriod.getTotalPaidLateForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalPaidLateForPeriod().doubleValue()).format());
                case "Waived" -> actualValues.add(repaymentPeriod.getTotalWaivedForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalWaivedForPeriod().doubleValue()).format());
                case "Outstanding" -> actualValues.add(repaymentPeriod.getTotalOutstandingForPeriod() == null ? null
                        : new Utils.DoubleFormatter(repaymentPeriod.getTotalOutstandingForPeriod().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
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
                paidActual += period.getTotalPaidForPeriod().doubleValue();
            }
        }
        BigDecimal paidActualBd = new BigDecimal(paidActual).setScale(2, RoundingMode.HALF_DOWN);

        for (int i = 0; i < header.size(); i++) {
            String headerName = header.get(i);
            String expectedValue = expectedAmounts.get(i);
            switch (headerName) {
                case "Principal due" -> assertThat(repaymentSchedule.getTotalPrincipalExpected().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePrincipal(
                                repaymentSchedule.getTotalPrincipalExpected().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Interest" -> assertThat(repaymentSchedule.getTotalInterestCharged().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInterest(
                                repaymentSchedule.getTotalInterestCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Fees" -> assertThat(repaymentSchedule.getTotalFeeChargesCharged().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleFees(
                                repaymentSchedule.getTotalFeeChargesCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Penalties" -> assertThat(repaymentSchedule.getTotalPenaltyChargesCharged().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePenalties(
                                repaymentSchedule.getTotalPenaltyChargesCharged().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Due" -> assertThat(repaymentSchedule.getTotalRepaymentExpected().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleDue(
                                repaymentSchedule.getTotalRepaymentExpected().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Paid" -> assertThat(paidActualBd.doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentSchedulePaid(paidActualBd.doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "In advance" -> assertThat(repaymentSchedule.getTotalPaidInAdvance().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleInAdvance(
                                repaymentSchedule.getTotalPaidInAdvance().doubleValue(), Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Late" -> assertThat(repaymentSchedule.getTotalPaidLate().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleLate(repaymentSchedule.getTotalPaidLate().doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Waived" -> assertThat(repaymentSchedule.getTotalWaived().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleWaived(repaymentSchedule.getTotalWaived().doubleValue(),
                                Double.valueOf(expectedValue)))//
                        .isEqualTo(Double.valueOf(expectedValue));//
                case "Outstanding" -> assertThat(repaymentSchedule.getTotalOutstanding().doubleValue())//
                        .as(ErrorMessageHelper.wrongAmountInRepaymentScheduleOutstanding(
                                repaymentSchedule.getTotalOutstanding().doubleValue(), Double.valueOf(expectedValue)))//
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
                case "Decimal Value" -> actualValues.add(emiVariation.getDecimalValue() == null ? null
                        : new Utils.DoubleFormatter(emiVariation.getDecimalValue().doubleValue()).format());
                case "Date Value" ->
                    actualValues.add(emiVariation.getDateValue() == null ? null : FORMATTER.format(emiVariation.getDateValue()));
                case "Is Specific To Installment" -> actualValues.add(String.valueOf(emiVariation.getIsSpecificToInstallment()));
                case "Is Processed" ->
                    actualValues.add(emiVariation.getIsProcessed() == null ? null : String.valueOf(emiVariation.getIsProcessed()));
            }
        }
        return actualValues;
    }

    @SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
    private List<String> fetchValuesOfLoanChargeOffReasonOptions(final List<String> header,
            final GetLoanProductsChargeOffReasonOptions chargeOffReasonOption) {
        final List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Charge-Off Reason Name" ->
                    actualValues.add(chargeOffReasonOption.getName() == null ? null : chargeOffReasonOption.getName());
                case "Description" -> {
                    assertNotNull(chargeOffReasonOption.getDescription());
                    actualValues
                            .add(chargeOffReasonOption.getDescription().isEmpty() || chargeOffReasonOption.getDescription() == null ? null
                                    : chargeOffReasonOption.getDescription());
                }
                case "Position" -> actualValues
                        .add(chargeOffReasonOption.getPosition() == null ? null : String.valueOf(chargeOffReasonOption.getPosition()));
                case "Is Active" ->
                    actualValues.add(chargeOffReasonOption.getActive() == null ? null : String.valueOf(chargeOffReasonOption.getActive()));
                case "Is Mandatory" -> actualValues
                        .add(chargeOffReasonOption.getMandatory() == null ? null : String.valueOf(chargeOffReasonOption.getMandatory()));
            }
        }
        return actualValues;
    }

    @Then("Log out transaction list by loanId, filtered out the following transaction types: {string}")
    public void transactionsExcluded(String excludedTypes) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        Response<GetLoansLoanIdTransactionsResponse> transactionsByLoanIdFiltered = getTransactionsByLoanIdFiltered(loanId, excludedTypes);
        ErrorHelper.checkSuccessfulApiCall(transactionsByLoanIdFiltered);

        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanIdFiltered.body().getContent();
        log.debug("Filtered transactions: {}", transactions);

        List<String> excludedTypesList = Arrays.stream(excludedTypes.toLowerCase().split(",")).map(String::trim)
                .collect(Collectors.toList());

        // Verify no transaction with excluded types exists in the filtered list
        for (GetLoansLoanIdTransactionsTransactionIdResponse transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            assertThat(excludedTypesList.contains(transactionType))
                    .as(String.format("Transaction type '%s' should be excluded but was found in the filtered results", transactionType))
                    .isFalse();
        }
    }

    @Then("Log out transaction list by loanExternalId, filtered out the following transaction types: {string}")
    public void transactionsExcludedByExternalId(String excludedTypes) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.body().getResourceExternalId();
        Response<GetLoansLoanIdTransactionsResponse> transactionsByLoanExternalIdFiltered = getTransactionsByLoanIExternalIdFiltered(
                loanExternalId, excludedTypes);
        ErrorHelper.checkSuccessfulApiCall(transactionsByLoanExternalIdFiltered);

        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanExternalIdFiltered.body().getContent();
        log.debug("Filtered transactions: {}", transactions);

        List<String> excludedTypesList = Arrays.stream(excludedTypes.toLowerCase().split(",")).map(String::trim)
                .collect(Collectors.toList());

        // Verify no transaction with excluded types exists in the filtered list
        for (GetLoansLoanIdTransactionsTransactionIdResponse transaction : transactions) {
            String transactionType = transaction.getType().getCode();
            assertThat(excludedTypesList.contains(transactionType))
                    .as(String.format("Transaction type '%s' should be excluded but was found in the filtered results", transactionType))
                    .isFalse();
        }
    }

    @Then("Filtered out transactions list contains the the following entries when filtered out by loanId for transaction types: {string}")
    public void transactionsExcludedCheck(String excludedTypes, DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdTransactionsResponse> transactionsByLoanIdFiltered = getTransactionsByLoanIdFiltered(loanId, excludedTypes);
        ErrorHelper.checkSuccessfulApiCall(transactionsByLoanIdFiltered);
        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanIdFiltered.body().getContent();
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String transactionDateExpected = expectedValues.get(0);
            List<List<String>> actualValuesList = transactions.stream()//
                    .filter(t -> transactionDateExpected.equals(FORMATTER.format(t.getDate())))//
                    .map(t -> fetchValuesOfFilteredTransaction(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
        assertThat(transactions.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(resourceId, transactions.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    @Then("Filtered out transactions list contains the the following entries when filtered out by loanExternalId for transaction types: {string}")
    public void transactionsExcludedByLoanExternalIdCheck(String excludedTypes, DataTable table) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.body().getResourceExternalId();
        long loanId = loanCreateResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdTransactionsResponse> transactionsByLoanExternalIdFiltered = getTransactionsByLoanIExternalIdFiltered(
                loanExternalId, excludedTypes);
        ErrorHelper.checkSuccessfulApiCall(transactionsByLoanExternalIdFiltered);

        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanExternalIdFiltered.body().getContent();
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String transactionDateExpected = expectedValues.get(0);
            List<List<String>> actualValuesList = transactions.stream()//
                    .filter(t -> transactionDateExpected.equals(FORMATTER.format(t.getDate())))//
                    .map(t -> fetchValuesOfFilteredTransaction(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
        assertThat(transactions.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(resourceId, transactions.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    private Response<GetLoansLoanIdTransactionsResponse> getTransactionsByLoanIdFiltered(Long loanId, String excludedTypes)
            throws IOException {
        return loanTransactionsApi.retrieveTransactionsByLoanId(loanId, parseExcludedTypes(excludedTypes), null, null, null).execute();
    }

    private Response<GetLoansLoanIdTransactionsResponse> getTransactionsByLoanIExternalIdFiltered(String loanExternalId,
            String excludedTypes) throws IOException {

        return loanTransactionsApi.retrieveTransactionsByExternalLoanId(loanExternalId, parseExcludedTypes(excludedTypes), null, null, null)
                .execute();
    }

    public static List<org.apache.fineract.client.models.TransactionType> parseExcludedTypes(String excludedTypes) {
        if (excludedTypes == null || excludedTypes.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(excludedTypes.split(",")).map(String::trim).map(String::toUpperCase)
                    .map(org.apache.fineract.client.models.TransactionType::valueOf).collect(Collectors.toList());
        }
    }

    private List<String> fetchValuesOfFilteredTransaction(List<String> header, GetLoansLoanIdTransactionsTransactionIdResponse t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Transaction date" -> actualValues.add(t.getDate() == null ? null : FORMATTER.format(t.getDate()));
                case "Transaction Type" -> actualValues.add(t.getType().getCode() == null ? null : t.getType().getCode().substring(20));
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
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    @Then("Filtered out transactions list has {int} pages in case of size set to {int} and transactions are filtered out for transaction types: {string}")
    public void checkPagination(Integer totalPagesExpected, Integer size, String excludedTypes) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdTransactionsResponse> transactionsByLoanIdFiltered = loanTransactionsApi
                .retrieveTransactionsByLoanId(loanId, parseExcludedTypes(excludedTypes), null, size, null).execute();

        Integer totalPagesActual = transactionsByLoanIdFiltered.body().getTotalPages();

        assertThat(totalPagesActual).as(ErrorMessageHelper.wrongValueInTotalPages(totalPagesActual, totalPagesExpected))
                .isEqualTo(totalPagesExpected);
    }

    @Then("Loan Product response contains interestRecognitionOnDisbursementDate flag with value {string}")
    public void verifyInterestRecognitionOnDisbursementDateFlag(final String expectedValue) throws IOException {
        GetLoanProductsResponse targetProduct = getLoanProductResponse();

        assertNotNull(targetProduct.getInterestRecognitionOnDisbursementDate());
        assertThat(targetProduct.getInterestRecognitionOnDisbursementDate().toString()).isEqualTo(expectedValue);
    }

    public GetLoanProductsResponse getLoanProductResponse() throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse.body());
        final Long loanId = loanResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetails);
        assertNotNull(loanDetails.body());

        final Long targetLoanProductId = loanDetails.body().getLoanProductId();

        final Response<List<GetLoanProductsResponse>> allProductsResponse = loanProductsApi.retrieveAllLoanProducts().execute();
        ErrorHelper.checkSuccessfulApiCall(allProductsResponse);

        assertNotNull(allProductsResponse.body());
        final List<GetLoanProductsResponse> loanProducts = allProductsResponse.body();
        assertThat(loanProducts).isNotEmpty();

        final GetLoanProductsResponse targetProduct = loanProducts.stream().filter(product -> {
            assertNotNull(product.getId());
            return product.getId().equals(targetLoanProductId);
        }).findFirst().orElseThrow(() -> new AssertionError("Loan product with ID " + targetLoanProductId + " not found in response"));

        return targetProduct;
    }

    @Then("Loan Product response contains Buy Down Fees flag {string} with data:")
    public void verifyLoanProductWithBuyDownFeesData(String expectedValue, DataTable table) throws IOException {
        GetLoanProductsResponse targetProduct = getLoanProductResponse();

        assertNotNull(targetProduct.getEnableBuyDownFee());
        assertThat(targetProduct.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);

        List<String> data = table.asLists().get(1); // skip header
        String buyDownFeeCalculationType = data.get(0);
        String buyDownFeeStrategy = data.get(1);
        String buyDownFeeIncomeType = data.get(2);

        assertNotNull(targetProduct.getBuyDownFeeCalculationType());
        assertNotNull(targetProduct.getBuyDownFeeStrategy());
        assertNotNull(targetProduct.getBuyDownFeeIncomeType());

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(buyDownFeeCalculationType).isEqualTo(targetProduct.getBuyDownFeeCalculationType().getValue());
        assertions.assertThat(buyDownFeeStrategy).isEqualTo(targetProduct.getBuyDownFeeStrategy().getValue());
        assertions.assertThat(buyDownFeeIncomeType).isEqualTo(targetProduct.getBuyDownFeeIncomeType().getValue());
        assertions.assertAll();
    }

    @Then("Loan Product response contains Buy Down Fees flag {string}")
    public void verifyLoanProductWithBuyDownFeesFlag(String expectedValue) throws IOException {
        GetLoanProductsResponse targetProduct = getLoanProductResponse();

        assertNotNull(targetProduct.getEnableBuyDownFee());
        assertThat(targetProduct.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);
    }

    public Response<GetLoansLoanIdResponse> getLoanDetailsResponse() throws IOException {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);

        long loanId = loanResponse.body().getLoanId();

        Optional<Response<GetLoansLoanIdResponse>> loanDetailsResponseOptional = Optional
                .of(loansApi.retrieveLoan(loanId, false, "", "", "").execute());
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        return loanDetailsResponse;
    }

    @Then("Loan Details response contains Buy Down Fees flag {string} and data:")
    public void verifyBuyDownFeeDataInLoanResponse(final String expectedValue, DataTable table) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetailsResponse = getLoanDetailsResponse();

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        GetLoansLoanIdResponse loanDetails = loanDetailsResponse.body();

        assertNotNull(loanDetails.getEnableBuyDownFee());
        assertThat(loanDetails.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);

        List<String> data = table.asLists().get(1); // skip header
        String buyDownFeeCalculationType = data.get(0);
        String buyDownFeeStrategy = data.get(1);
        String buyDownFeeIncomeType = data.get(2);

        assertNotNull(loanDetails.getBuyDownFeeCalculationType());
        assertNotNull(loanDetails.getBuyDownFeeStrategy());
        assertNotNull(loanDetails.getBuyDownFeeIncomeType());

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(buyDownFeeCalculationType).isEqualTo(loanDetails.getBuyDownFeeCalculationType().getValue());
        assertions.assertThat(buyDownFeeStrategy).isEqualTo(loanDetails.getBuyDownFeeStrategy().getValue());
        assertions.assertThat(buyDownFeeIncomeType).isEqualTo(loanDetails.getBuyDownFeeIncomeType().getValue());
        assertions.assertAll();
    }

    @Then("Loan Details response contains Buy Down Fees flag {string}")
    public void verifyBuyDownFeeFlagInLoanResponse(final String expectedValue) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetailsResponse = getLoanDetailsResponse();

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        GetLoansLoanIdResponse loanDetails = loanDetailsResponse.body();

        assertNotNull(loanDetails.getEnableBuyDownFee());
        assertThat(loanDetails.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);
    }

    @Then("Loan Details response contains chargedOffOnDate set to {string}")
    public void verifyChargedOffOnDateFlagInLoanResponse(final String expectedValue) throws IOException {
        Response<PostLoansLoanIdTransactionsResponse> loanResponse = testContext().get(TestContextKey.LOAN_CHARGE_OFF_RESPONSE);

        long loanId = loanResponse.body().getLoanId();

        Optional<Response<GetLoansLoanIdResponse>> loanDetailsResponseOptional = Optional
                .of(loansApi.retrieveLoan(loanId, false, "", "", "").execute());
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.body().getTimeline().getChargedOffOnDate()).isEqualTo(LocalDate.parse(expectedValue, FORMATTER));
    }

    @Then("Loan Details response does not contain chargedOff flag and chargedOffOnDate field after repayment and reverted charge off")
    public void verifyChargedOffOnDateFlagIsNotPresentLoanResponse() throws IOException {
        Response<PostLoansLoanIdTransactionsResponse> loanResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        long loanId = loanResponse.body().getLoanId();

        Optional<Response<GetLoansLoanIdResponse>> loanDetailsResponseOptional = Optional
                .of(loansApi.retrieveLoan(loanId, false, "", "", "").execute());
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.body().getTimeline().getChargedOffOnDate()).isNull();
        assertThat(loanDetailsResponse.body().getChargedOff()).isFalse();
    }

    @Then("Loan Details response contains chargedOff flag set to {booleanValue}")
    public void verifyChargeOffFlagInLoanResponse(final Boolean expectedValue) throws IOException {
        Response<PostLoansLoanIdTransactionsResponse> loanResponse = expectedValue
                ? testContext().get(TestContextKey.LOAN_CHARGE_OFF_RESPONSE)
                : testContext().get(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE);

        long loanId = loanResponse.body().getLoanId();

        Optional<Response<GetLoansLoanIdResponse>> loanDetailsResponseOptional = Optional
                .of(loansApi.retrieveLoan(loanId, false, "", "", "").execute());
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));

        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.body().getChargedOff()).isEqualTo(expectedValue);
    }

    @ParameterType(value = "true|True|TRUE|false|False|FALSE")
    public Boolean booleanValue(String value) {
        return Boolean.valueOf(value);
    }

    public Response<PostLoansLoanIdTransactionsResponse> addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(
            final String transactionPaymentType, final String transactionDate, final String amount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID());

        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, capitalizedIncomeRequest, "capitalizedIncome").execute();
        return capitalizedIncomeResponse;
    }

    @And("Admin adds capitalized income with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType,
            final String transactionDate, final String amount) throws IOException {
        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeResponse = addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_RESPONSE, capitalizedIncomeResponse);
        ErrorHelper.checkSuccessfulApiCall(capitalizedIncomeResponse);
    }

    public Response<PostLoansLoanIdTransactionsResponse> adjustCapitalizedIncome(final String transactionPaymentType,
            final String transactionDate, final String amount, final Long transactionId) throws IOException {

        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest capitalizedIncomeRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en").transactionAmount(Double.valueOf(amount))
                .paymentTypeId(paymentTypeValue).externalId("EXT-CAP-INC-ADJ-" + UUID.randomUUID());

        // Use adjustLoanTransaction with the transaction ID and command
        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeResponse = loanTransactionsApi
                .adjustLoanTransaction(loanId, transactionId, capitalizedIncomeRequest, "capitalizedIncomeAdjustment").execute();

        return capitalizedIncomeResponse;
    }

    @Then("Capitalized income with payment type {string} on {string} is forbidden with amount {string} while exceed approved amount")
    public void capitalizedIncomeForbiddenExceedApprovedAmount(final String transactionPaymentType, final String transactionDate,
            final String amount) throws IOException {
        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeResponse = addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);

        ErrorResponse errorDetails = ErrorResponse.from(capitalizedIncomeResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addCapitalizedIncomeExceedApprovedAmountFailure())
                .isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addCapitalizedIncomeExceedApprovedAmountFailure());
    }

    @Then("Capitalized income with payment type {string} on {string} is forbidden with amount {string} due to future date")
    public void capitalizedIncomeForbiddenFutureDate(final String transactionPaymentType, final String transactionDate, final String amount)
            throws IOException {
        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeResponse = addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);

        ErrorResponse errorDetails = ErrorResponse.from(capitalizedIncomeResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure());
    }

    @Then("LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(final String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income Amortization".equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No Capitalized Income Amortization transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent.class,
                finalAmortizationTransactionId);
    }

    @Then("LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent(final String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate()))
                        && "Capitalized Income Amortization Adjustment".equals(t.getType().getValue()))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        String.format("No Capitalized Income Amortization Adjustment transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent.class,
                finalAmortizationTransactionId);
    }

    @Then("LoanCapitalizedIncomeTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeTransactionCreatedBusinessEvent(final String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Capitalized Income transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeTransactionCreatedBusinessEvent.class, finalAmortizationTransactionId);
    }

    @Then("LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent(final String date) throws IOException {
        Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.body().getLoanId();

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income Adjustment".equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No Capitalized Income Adjustment transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent.class,
                finalAmortizationTransactionId);
    }

    @And("Admin adds capitalized income adjustment with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsCapitalizedIncomeAdjustmentToTheLoan(final String transactionPaymentType, final String transactionDate,
            final String amount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        // Get current business date to ensure we're not creating backdated transactions
        String currentBusinessDate = businessDateHelper.getBusinessDate();
        log.debug("Current business date: {}, Transaction date: {}", currentBusinessDate, transactionDate);

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final Response<PostLoansLoanIdTransactionsResponse> adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType,
                transactionDate, amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        ErrorHelper.checkSuccessfulApiCall(adjustmentResponse);

        log.debug("Capitalized Income Adjustment created: Transaction ID {}", adjustmentResponse.body().getResourceId());
    }

    @And("Admin adds capitalized income adjustment with {string} payment type to the loan on {string} with {string} EUR trn amount with {string} date for capitalized income")
    public void adminAddsCapitalizedIncomeAdjustmentToTheLoanWithCapitalizedIncomeDate(final String transactionPaymentType,
            final String transactionDate, final String amount, final String capitalizedIncomeTrnsDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue()))
                .filter(t -> FORMATTER.format(t.getDate()).equals(capitalizedIncomeTrnsDate)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final Response<PostLoansLoanIdTransactionsResponse> adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType,
                transactionDate, amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        ErrorHelper.checkSuccessfulApiCall(adjustmentResponse);

        log.debug("Capitalized Income Adjustment created: Transaction ID {}", adjustmentResponse.body().getResourceId());
    }

    @And("Admin adds invalid capitalized income adjustment with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsArbitraryCapitalizedIncomeAdjustmentToTheLoan(final String transactionPaymentType, final String transactionDate,
            final String amount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        // Get current business date to ensure we're not creating backdated transactions
        String currentBusinessDate = businessDateHelper.getBusinessDate();
        log.debug("Current business date: {}, Transaction date: {}", currentBusinessDate, transactionDate);

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final Response<PostLoansLoanIdTransactionsResponse> adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType,
                transactionDate, amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        ErrorHelper.checkFailedApiCall(adjustmentResponse, 400);
    }

    @And("Admin successfully terminates loan contract")
    public void makeLoanContractTermination() throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final long loanId = loanResponse.body().getLoanId();

        final PostLoansLoanIdRequest contractTerminationRequest = LoanRequestFactory.defaultLoanContractTerminationRequest();

        final Response<PostLoansLoanIdResponse> loanContractTerminationResponse = loansApi
                .stateTransitions(loanId, contractTerminationRequest, "contractTermination").execute();
        testContext().set(TestContextKey.LOAN_CONTRACT_TERMINATION_RESPONSE, loanContractTerminationResponse);
        ErrorHelper.checkSuccessfulApiCall(loanContractTerminationResponse);

        assert loanContractTerminationResponse.body() != null;
        final Long transactionId = loanContractTerminationResponse.body().getResourceId();
        eventAssertion.assertEvent(LoanTransactionContractTerminationPostBusinessEvent.class, transactionId)
                .extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId)
                .isEqualTo(transactionId);
    }

    @And("Admin successfully undoes loan contract termination")
    public void undoLoanContractTermination() throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final Response<PostLoansLoanIdResponse> loanContractTerminationResponse = testContext()
                .get(TestContextKey.LOAN_CONTRACT_TERMINATION_RESPONSE);
        assert loanContractTerminationResponse.body() != null;
        final Long loanId = loanResponse.body().getLoanId();

        final List<GetLoansLoanIdTransactions> transactions = Objects
                .requireNonNull(loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute().body()).getTransactions();

        assert transactions != null;
        final GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> {
            assert t.getType() != null;
            return Boolean.TRUE.equals(t.getType().getContractTermination());
        }).findFirst().orElse(null);

        final PostLoansLoanIdRequest request = LoanRequestFactory.defaultContractTerminationUndoRequest();

        final Response<PostLoansLoanIdResponse> response = loansApi.stateTransitions(loanId, request, "undoContractTermination").execute();
        testContext().set(TestContextKey.LOAN_UNDO_CONTRACT_TERMINATION_RESPONSE, response);
        ErrorHelper.checkSuccessfulApiCall(response);
        assert targetTransaction != null;
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanUndoContractTerminationEventCheck(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @Then("LoanTransactionContractTerminationPostBusinessEvent is raised on {string}")
    public void checkLoanTransactionContractTerminationPostBusinessEvent(final String date) throws IOException {
        final Response<PostLoansResponse> loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.body().getLoanId();

        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final GetLoansLoanIdTransactions loanContractTerminationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Contract Termination".equals(t.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Contract Termination transaction found on %s", date)));
        final Long loanContractTerminationTransactionId = loanContractTerminationTransaction.getId();

        eventAssertion.assertEventRaised(LoanTransactionContractTerminationPostBusinessEvent.class, loanContractTerminationTransactionId);
    }

    @Then("Capitalized income adjustment with payment type {string} on {string} is forbidden with amount {string} due to future date")
    public void capitalizedIncomeAdjustmentForbiddenFutureDate(final String transactionPaymentType, final String transactionDate,
            final String amount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();
        final Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final Response<PostLoansLoanIdTransactionsResponse> capitalizedIncomeAdjustmentResponse = adjustCapitalizedIncome(
                transactionPaymentType, transactionDate, amount, capitalizedIncomeTransaction.getId());

        ErrorResponse errorDetails = ErrorResponse.from(capitalizedIncomeAdjustmentResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage())
                .isEqualTo(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure());
    }

    public Response<PostLoansLoanIdTransactionsResponse> addBuyDownFeeToTheLoanOnWithEURTransactionAmount(
            final String transactionPaymentType, final String transactionDate, final String amount) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.body().getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest buyDownFeeRequest = LoanRequestFactory.defaultBuyDownFeeIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-BUY-DOWN-FEES" + UUID.randomUUID());

        final Response<PostLoansLoanIdTransactionsResponse> buyDownFeeResponse = loanTransactionsApi
                .executeLoanTransaction(loanId, buyDownFeeRequest, "buyDownFee").execute();
        return buyDownFeeResponse;
    }

    @And("Admin adds buy down fee with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsBuyDownFeesToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType, final String transactionDate,
            final String amount) throws IOException {
        final Response<PostLoansLoanIdTransactionsResponse> buyDownFeesIncomeResponse = addBuyDownFeeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_RESPONSE, buyDownFeesIncomeResponse);
        ErrorHelper.checkSuccessfulApiCall(buyDownFeesIncomeResponse);
    }

}
