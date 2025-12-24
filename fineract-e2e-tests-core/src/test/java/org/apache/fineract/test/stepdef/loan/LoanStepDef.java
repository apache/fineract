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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.apache.fineract.test.data.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_INTEREST_DAILY_INTEREST_RECALCULATION_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.data.loanproduct.DefaultLoanProduct.LP2_ADV_PYMNT_ZERO_INTEREST_CHARGE_OFF_BEHAVIOUR;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.CHARGE_OFF_REASONS;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.LOCALE_EN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AmortizationMappingData;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BuyDownFeeAmortizationDetails;
import org.apache.fineract.client.models.CapitalizedIncomeDetails;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.GetLoanProductsChargeOffReasonOptions;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargePaidByData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTermVariations;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionEnumData;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTimeline;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.IsCatchUpRunningDTO;
import org.apache.fineract.client.models.LoanAmortizationAllocationResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostAddAndDeleteDisbursementDetailRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
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
import org.apache.fineract.client.models.PutLoansApprovedAmountRequest;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
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
import org.apache.fineract.test.helper.ErrorMessageHelper;
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
import org.apache.fineract.test.messaging.event.loan.transaction.LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanBuyDownFeeTransactionCreatedBusinessEvent;
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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern(DATE_FORMAT_EVENTS);
    private static final String TRANSACTION_DATE_FORMAT = "dd MMMM yyyy";

    @Autowired
    private BusinessDateHelper businessDateHelper;

    @Autowired
    private FineractFeignClient fineractClient;

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

    private void storePaymentTransactionResponse(ApiResponse<PostLoansLoanIdTransactionsResponse> apiResponse) {
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE, apiResponse.getData());
        testContext().set(TestContextKey.LOAN_PAYMENT_TRANSACTION_HEADERS, apiResponse.getHeaders());
    }

    @Autowired
    private EventStore eventStore;

    @Autowired
    private CodeValueResolver codeValueResolver;

    @Autowired
    private CodeHelper codeHelper;

    @Autowired
    private EventProperties eventProperties;

    @Autowired
    private JobPollingProperties jobPollingProperties;

    @When("Admin creates a new Loan")
    public void createLoan() {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new default Loan with date: {string}")
    public void createLoanWithDate(String date) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new default Progressive Loan with date: {string}")
    public void createProgressiveLoanWithDate(final String date) {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();
        final PostLoansRequest loansRequest = loanRequestFactory.defaultProgressiveLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin crates a second default loan with date: {string}")
    public void createSecondLoanWithDate(String date) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin crates a second default loan for the second client with date: {string}")
    public void createSecondLoanForSecondClientWithDate(String date) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE);
        Long clientId = clientResponse.getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).submittedOnDate(date)
                .expectedDisbursementDate(date);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    /**
     * Use this where inline COB run needed - this way we don't have to run inline COB for all 30 days of loan term, but
     * only 1 day
     */
    @When("Admin creates a new Loan with date: {string} and with 1 day loan term and repayment")
    public void createLoanWithDateShortTerm(String date) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)//
                .submittedOnDate(date)//
                .expectedDisbursementDate(date)//
                .loanTermFrequency(1)//
                .repaymentEvery(1);//

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
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
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        String idempotencyKey = UUID.randomUUID().toString();
        testContext().set(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY, idempotencyKey);

        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest,
                        transactionTypeValue, Map.of("Idempotency-Key", idempotencyKey)));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        eventCheckHelper.transactionEventCheck(paymentTransactionApiResponse.getData(), transactionType, externalOwnerId);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount")
    public void createTransactionForRefund(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws InterruptedException, IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest, transactionTypeValue, Map.of()));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        eventCheckHelper.transactionEventCheck(paymentTransactionApiResponse.getData(), transactionType, null);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and self-generated external-id")
    public void createTransactionWithExternalId(String transactionTypeInput, String transactionPaymentType, String transactionDate,
            double transactionAmount) throws IOException, InterruptedException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String externalId = UUID.randomUUID().toString();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue)
                .externalId(externalId);

        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest, transactionTypeValue, Map.of()));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        assertThat(paymentTransactionApiResponse.getData().getResourceExternalId()).as("External id is not correct").isEqualTo(externalId);

        eventCheckHelper.transactionEventCheck(paymentTransactionApiResponse.getData(), transactionType, null);
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

    @When("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount and system-generated Idempotency key and interestRefundCalculation {booleanValue}")
    public void createTransactionWithAutoIdempotencyKeyAndWithInterestRefundCalculationFlagProvided(final String transactionTypeInput,
            final String transactionPaymentType, final String transactionDate, final double transactionAmount,
            final boolean interestRefundCalculation) throws IOException {
        eventStore.reset();
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        final String transactionTypeValue = transactionType.getValue();
        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue)
                .interestRefundCalculation(interestRefundCalculation);

        final ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest, transactionTypeValue, Map.of()));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, paymentTransactionApiResponse.getData());
        eventCheckHelper.transactionEventCheck(paymentTransactionApiResponse.getData(), transactionType, null);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin manually adds Interest Refund for {string} transaction made on {string} with {double} EUR interest refund amount")
    public void addInterestRefundTransactionManually(final String transactionTypeInput, final String transactionDate, final double amount)
            throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final GetLoansLoanIdTransactions refundTransaction = transactions.stream()
                .filter(t -> t.getType() != null
                        && (transactionType.equals(TransactionType.PAYOUT_REFUND) ? "Payout Refund" : "Merchant Issued Refund")
                                .equals(t.getType().getValue())
                        && t.getDate() != null && transactionDate.equals(FORMATTER.format(t.getDate())))
                .findFirst().orElseThrow(() -> new IllegalStateException("No refund transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = addInterestRefundTransaction(amount, refundTransaction.getId());
        testContext().set(TestContextKey.LOAN_INTEREST_REFUND_RESPONSE, adjustmentResponse);
        eventCheckHelper.transactionEventCheck(adjustmentResponse, TransactionType.INTEREST_REFUND, null);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin manually adds Interest Refund for {string} transaction made on invalid date {string} with {double} EUR interest refund amount")
    public void addInterestRefundTransactionManuallyWithInvalidDate(final String transactionTypeInput, final String transactionDate,
            final double amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final GetLoansLoanIdTransactions refundTransaction = transactions.stream()
                .filter(t -> t.getType() != null
                        && (transactionType.equals(TransactionType.PAYOUT_REFUND) ? "Payout Refund" : "Merchant Issued Refund")
                                .equals(t.getType().getValue()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No refund transaction found for loan " + loanId));

        failAddInterestRefundTransaction(amount, refundTransaction.getId(), transactionDate);
    }

    @When("Admin fails to add Interest Refund for {string} transaction made on {string} with {double} EUR interest refund amount")
    public void addInterestRefundTransactionManuallyFailsInNonPayout(final String transactionTypeInput, final String transactionDate,
            final double amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;

        final GetLoansLoanIdTransactions moneyTransaction = transactions.stream()
                .filter(t -> t.getType() != null && transactionType.equals(TransactionType.REPAYMENT) && t.getDate() != null
                        && transactionDate.equals(FORMATTER.format(t.getDate())))
                .findFirst().orElseThrow(() -> new IllegalStateException("No repayment transaction found"));

        final Long paymentTypeValue = paymentTypeResolver.resolve(DefaultPaymentType.AUTOPAY);
        final PostLoansLoanIdTransactionsTransactionIdRequest interestRefundRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(amount).paymentTypeId(paymentTypeValue)
                .externalId("EXT-INT-REF-" + UUID.randomUUID()).note("");

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                moneyTransaction.getId(), interestRefundRequest, Map.of("command", "interest-refund")));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Admin fails to add duplicate Interest Refund for {string} transaction made on {string} with {double} EUR interest refund amount")
    public void failToAddManualInterestRefundIfAlreadyExists(final String transactionTypeInput, final String transactionDate,
            final double amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final GetLoansLoanIdTransactions refundTransaction = transactions.stream()
                .filter(t -> t.getType() != null
                        && (transactionType.equals(TransactionType.PAYOUT_REFUND) ? "Payout Refund" : "Merchant Issued Refund")
                                .equals(t.getType().getValue())
                        && t.getDate() != null && transactionDate.equals(FORMATTER.format(t.getDate())))
                .findFirst().orElseThrow(() -> new IllegalStateException("No refund transaction found for loan " + loanId));

        final Long paymentTypeValue = paymentTypeResolver.resolve(DefaultPaymentType.AUTOPAY);
        final PostLoansLoanIdTransactionsTransactionIdRequest interestRefundRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(amount).paymentTypeId(paymentTypeValue)
                .externalId("EXT-INT-REF-" + UUID.randomUUID()).note("");

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                refundTransaction.getId(), interestRefundRequest, Map.of("command", "interest-refund")));
        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addManualInterestRefundIfAlreadyExistsFailure());
    }

    @Then("Admin fails to add Interest Refund {string} transaction after reverse made on {string} with {double} EUR interest refund amount")
    public void failToAddManualInterestRefundIfReversed(final String transactionTypeInput, final String transactionDate,
            final double amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final GetLoansLoanIdTransactions refundTransaction = transactions.stream()
                .filter(t -> t.getType() != null
                        && (transactionType.equals(TransactionType.PAYOUT_REFUND) ? "Payout Refund" : "Merchant Issued Refund")
                                .equals(t.getType().getValue())
                        && t.getDate() != null && transactionDate.equals(FORMATTER.format(t.getDate())))
                .findFirst().orElseThrow(() -> new IllegalStateException("No refund transaction found for loan " + loanId));

        final Long paymentTypeValue = paymentTypeResolver.resolve(DefaultPaymentType.AUTOPAY);
        final PostLoansLoanIdTransactionsTransactionIdRequest interestRefundRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(amount).paymentTypeId(paymentTypeValue)
                .externalId("EXT-INT-REF-" + UUID.randomUUID()).note("");

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                refundTransaction.getId(), interestRefundRequest, Map.of("command", "interest-refund")));
        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addManualInterestRefundIfReversedFailure());
    }

    private void createTransactionWithAutoIdempotencyKeyAndWithExternalOwner(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount, String externalOwnerId) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest, transactionTypeValue, Map.of()));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        testContext().set(TestContextKey.LOAN_REPAYMENT_RESPONSE, paymentTransactionApiResponse.getData());
        eventCheckHelper.transactionEventCheck(paymentTransactionApiResponse.getData(), transactionType, externalOwnerId);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @When("Admin makes Credit Balance Refund transaction on {string} with {double} EUR transaction amount")
    public void createCBR(String transactionDate, double transactionAmount) throws IOException {
        eventStore.reset();
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        String transactionTypeValue = "creditBalanceRefund";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest, transactionTypeValue, Map.of()));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    public void checkCBRerror(PostLoansLoanIdTransactionsRequest paymentTransactionRequest, int errorCodeExpected,
            String errorMessageExpected) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        String transactionTypeValue = "creditBalanceRefund";
        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                paymentTransactionRequest, Map.of("command", transactionTypeValue)));

        int errorCodeActual = exception.getStatus();
        String errorMessageActual = exception.getDeveloperMessage();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @Then("Credit Balance Refund transaction on future date {string} with {double} EUR transaction amount will result an error")
    public void futureDateCBRError(String transactionDate, double transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        int errorCodeExpected = 403;
        String errorMessageExpected = String.format("Loan: %s, Credit Balance Refund transaction cannot be created for the future.",
                loanId);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);

        checkCBRerror(paymentTransactionRequest, errorCodeExpected, errorMessageExpected);
    }

    @Then("Credit Balance Refund transaction on active loan {string} with {double} EUR transaction amount will result an error")
    public void notOverpaidLoanCBRError(String transactionDate, double transactionAmount) {
        int errorCodeExpected = 400;
        String errorMessageExpected = "Loan Credit Balance Refund is not allowed. Loan Account is not Overpaid.";

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount);
        checkCBRerror(paymentTransactionRequest, errorCodeExpected, errorMessageExpected);
    }

    @When("Admin creates a fully customized loan with the following data:")
    public void createFullyCustomizedLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createCustomizedLoan(data.get(1), false);
    }

    @When("Admin creates a fully customized loan with loan product`s charges and following data:")
    public void createFullyCustomizedLoanWithProductCharges(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createCustomizedLoanWithProductCharges(data.get(1));
    }

    @When("Admin creates a fully customized loan with emi and the following data:")
    public void createFullyCustomizedLoanWithEmi(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createCustomizedLoan(data.get(1), true);
    }

    @When("Admin creates a fully customized loan with interestRateFrequencyType and following data:")
    public void createFullyCustomizedLoanWithInterestRateFrequencyType(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithInterestRateFrequency(data.get(1));
    }

    @When("Admin creates a fully customized loan with graceOnArrearsAgeing and following data:")
    public void createFullyCustomizedLoanWithGraceOnArrearsAgeing(final DataTable table) throws IOException {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithGraceOnArrearsAgeing(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and following data:")
    public void createFullyCustomizedLoanWithLoanCharges(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithCharges(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and disbursement details and following data:")
    public void createFullyCustomizedLoanWithChargesAndDisbursementDetails(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with charges and disbursements details and following data:")
    public void createFullyCustomizedLoanWithChargesAndDisbursementsDetails(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementsDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with disbursement details and following data:")
    public void createFullyCustomizedLoanWithDisbursementDetails(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithExpectedTrancheDisbursementDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with disbursements details and following data:")
    public void createFullyCustomizedLoanWithDisbursementsDetails(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithExpectedTrancheDisbursementsDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with three expected disbursements details and following data:")
    public void createFullyCustomizedLoanWithThreeDisbursementsDetails(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createFullyCustomizedLoanWithThreeExpectedTrancheDisbursementsDetails(data.get(1));
    }

    @When("Admin creates a fully customized loan with forced disabled downpayment with the following data:")
    public void createFullyCustomizedLoanWithForcedDisabledDownpayment(DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @Then("Admin fails to create a fully customized loan with forced enabled downpayment with the following data:")
    public void createFullyCustomizedLoanWithForcedEnabledDownpayment(DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains("downpayment");
    }

    @When("Admin creates a fully customized loan with auto downpayment {double}% and with the following data:")
    public void createFullyCustomizedLoanWithAutoDownpayment15(double percentage, DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a fully customized loan with downpayment {double}%, NO auto downpayment, and with the following data:")
    public void createFullyCustomizedLoanWithDownpayment15(double percentage, DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a fully customized loan with fixed length {int} and with the following data:")
    public void createFullyCustomizedLoanFixedLength(int fixedLength, DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Trying to create a fully customized loan with fixed length {int} and with the following data will result a {int} ERROR:")
    public void createFullyCustomizedLoanFixedLengthError(int fixedLength, int errorCodeExpected, DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), errorCodeExpected))
                .isEqualTo(errorCodeExpected);
        log.debug("ERROR CODE: {}", exception.getStatus());
        log.debug("ERROR MESSAGE: {}", exception.getDeveloperMessage());
    }

    @When("Admin creates a fully customized loan with Advanced payment allocation and with product no Advanced payment allocation set results an error:")
    public void createFullyCustomizedLoanNoAdvancedPaymentError(DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.wrongErrorCode(exception.getStatus(), errorCodeExpected))
                .isEqualTo(errorCodeExpected);
        assertThat(exception.getDeveloperMessage())
                .as(ErrorMessageHelper.wrongErrorMessage(exception.getDeveloperMessage(), errorMessageExpected))
                .contains(errorMessageExpected);

        log.debug("ERROR CODE: {}", exception.getStatus());
        log.debug("ERROR MESSAGE: {}", exception.getDeveloperMessage());
    }

    @When("Admin creates a fully customized loan with installment level delinquency and with the following data:")
    public void createFullyCustomizedLoanWithInstallmentLvlDelinquency(DataTable table) {
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

        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();

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

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @Then("Loan details has the following last payment related data:")
    public void checkLastPaymentData(DataTable table) {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String lastPaymentAmountExpected = expectedValues.get(0);
        String lastPaymentDateExpected = expectedValues.get(1);
        String lastRepaymentAmountExpected = expectedValues.get(2);
        String lastRepaymentDateExpected = expectedValues.get(3);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "collection")));
        GetLoansLoanIdDelinquencySummary delinquent = loanDetailsResponse.getDelinquent();
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
    public void checkLoanDetailsAndEventLoanChargePaidByListSection(DataTable table) {
        List<List<String>> data = table.asLists();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void createCustomizedLoan(String submitDate, String principal, Integer loanTermFrequency, Integer numberOfRepayments) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        Integer repaymentFrequency = loanTermFrequency / numberOfRepayments;

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).principal(new BigDecimal(principal))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)
                .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentFrequency)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value).submittedOnDate(submitDate)
                .expectedDisbursementDate(submitDate);

        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
    }

    @And("Customer makes {string} transaction with {string} payment type on {string} with {double} EUR transaction amount with the same Idempotency key as previous transaction")
    public void createTransactionWithIdempotencyKeyOfPreviousTransaction(String transactionTypeInput, String transactionPaymentType,
            String transactionDate, double transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        String idempotencyKey = testContext().get(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY);
        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest,
                        transactionTypeValue, Map.of("Idempotency-Key", idempotencyKey)));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
    }

    @And("Customer makes {string} transaction on the second loan with {string} payment type on {string} with {double} EUR transaction amount with the same Idempotency key as previous transaction")
    public void createTransactionOnSecondLoanWithIdempotencyKeyOfPreviousTransaction(String transactionTypeInput,
            String transactionPaymentType, String transactionDate, double transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();
        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest paymentTransactionRequest = LoanRequestFactory.defaultPaymentTransactionRequest()
                .transactionDate(transactionDate).transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue);

        String idempotencyKey = testContext().get(TestContextKey.TRANSACTION_IDEMPOTENCY_KEY);
        ApiResponse<PostLoansLoanIdTransactionsResponse> paymentTransactionApiResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransactionWithHttpInfo(loanId, paymentTransactionRequest,
                        transactionTypeValue, Map.of("Idempotency-Key", idempotencyKey)));
        storePaymentTransactionResponse(paymentTransactionApiResponse);
    }

    @Then("Admin can successfully modify the loan and changes the submitted on date to {string}")
    public void modifyLoanSubmittedOnDate(String newSubmittedOnDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId2 = loanResponse.getResourceId();
        Long clientId2 = loanResponse.getClientId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = loanRequestFactory.modifySubmittedOnDateOnLoan(clientId2, newSubmittedOnDate);

        PutLoansLoanIdResponse responseMod = ok(
                () -> fineractClient.loans().modifyLoanApplication(loanId2, putLoansLoanIdRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, responseMod);
    }

    @Then("Admin fails to create a new customised Loan submitted on date: {string}, with Principal: {string}, a loanTermFrequency: {int} months, and numberOfRepayments: {int}")
    public void createCustomizedLoanFailure(String submitDate, String principal, Integer loanTermFrequency, Integer numberOfRepayments) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientId = clientResponse.getClientId();
        Integer repaymentFrequency = loanTermFrequency / numberOfRepayments;

        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId).principal(new BigDecimal(principal))
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(LoanTermFrequencyType.MONTHS.value)
                .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentFrequency)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.value).submittedOnDate(submitDate)
                .expectedDisbursementDate(submitDate);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
    }

    @And("Admin successfully approves the loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveLoan(String approveDate, String approvedAmount, String expectedDisbursementDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        PostLoansLoanIdResponse loanApproveResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, approveRequest, Map.of("command", "approve")));
        testContext().set(TestContextKey.LOAN_APPROVAL_RESPONSE, loanApproveResponse);
        assertThat(loanApproveResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
        assertThat(loanApproveResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);

        eventCheckHelper.approveLoanEventCheck(loanApproveResponse);
    }

    @And("Admin successfully rejects the loan on {string}")
    public void rejectLoan(String rejectDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest rejectRequest = LoanRequestFactory.defaultLoanRejectRequest().rejectedOnDate(rejectDate);

        PostLoansLoanIdResponse loanRejectResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, rejectRequest, Map.of("command", "reject")));
        testContext().set(TestContextKey.LOAN_REJECT_RESPONSE, loanRejectResponse);
        assertThat(loanRejectResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_REJECTED);
        assertThat(loanRejectResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_REJECTED);

        eventCheckHelper.loanRejectedEventCheck(loanRejectResponse);
    }

    @And("Admin successfully withdrawn the loan on {string}")
    public void withdrawnLoan(String withdrawnDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest withdawnRequest = LoanRequestFactory.defaultLoanWithdrawnRequest().withdrawnOnDate(withdrawnDate);

        PostLoansLoanIdResponse loanWithdrawnResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, withdawnRequest, Map.of("command", "withdrawnByApplicant")));
        testContext().set(TestContextKey.LOAN_WITHDRAWN_RESPONSE, loanWithdrawnResponse);
        assertThat(loanWithdrawnResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_WITHDRAWN);
        assertThat(loanWithdrawnResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_WITHDRAWN);

        eventCheckHelper.undoApproveLoanEventCheck(loanWithdrawnResponse);
    }

    @And("Admin successfully approves the second loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveSecondLoan(String approveDate, String approvedAmount, String expectedDisbursementDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        PostLoansLoanIdResponse loanApproveResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, approveRequest, Map.of("command", "approve")));
        testContext().set(TestContextKey.LOAN_APPROVAL_SECOND_LOAN_RESPONSE, loanApproveResponse);
        assertThat(loanApproveResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
        assertThat(loanApproveResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
    }

    @Then("Admin can successfully undone the loan approval")
    public void undoLoanApproval() {
        PostLoansLoanIdResponse loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.getLoanId();
        PostLoansLoanIdRequest undoApprovalRequest = new PostLoansLoanIdRequest().note("");

        PostLoansLoanIdResponse undoApprovalResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, undoApprovalRequest, Map.of("command", "undoapproval")));
        testContext().set(TestContextKey.LOAN_UNDO_APPROVAL_RESPONSE, loanApproveResponse);
        assertThat(undoApprovalResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_SUBMITTED_AND_PENDING);
    }

    @Then("Admin fails to approve the loan on {string} with {string} amount and expected disbursement date on {string} because of wrong date")
    public void failedLoanApproveWithDate(String approveDate, String approvedAmount, String expectedDisbursementDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, approveRequest, Map.of("command", "approve")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.loanApproveDateInFutureFailureMsg());
    }

    @Then("Admin fails to approve the loan on {string} with {string} amount and expected disbursement date on {string} because of wrong amount")
    public void failedLoanApproveWithAmount(String approveDate, String approvedAmount, String expectedDisbursementDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest approveRequest = LoanRequestFactory.defaultLoanApproveRequest().approvedOnDate(approveDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount)).expectedDisbursementDate(expectedDisbursementDate);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, approveRequest, Map.of("command", "approve")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.loanApproveMaxAmountFailureMsg());
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount")
    public void disburseLoan(String actualDisbursementDate, String transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        PostLoansLoanIdResponse loanDisburseResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        Long statusActual = loanDisburseResponse.getChanges().getStatus().getId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        Long statusExpected = Long.valueOf(loanDetails.getStatus().getId());

        assertThat(statusActual)//
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))//
                .isEqualTo(statusExpected);//
        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    @And("Admin successfully add disbursement detail to the loan on {string} with {double} EUR transaction amount")
    public void addDisbursementDetailToLoan(String expectedDisbursementDate, Double disbursementAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "all")));
        Set<GetLoansLoanIdDisbursementDetails> disbursementDetailsList = loanDetails.getDisbursementDetails();

        List<DisbursementDetail> disbursementData = new ArrayList<>();

        // get and add already existing entries - just do not delete them
        if (disbursementDetailsList != null) {
            disbursementDetailsList.stream().sorted(Comparator.comparing(GetLoansLoanIdDisbursementDetails::getExpectedDisbursementDate))
                    .forEach(disbursementDetail -> {
                        String formatted = disbursementDetail.getExpectedDisbursementDate().format(FORMATTER);
                        DisbursementDetail disbursementDetailEntryExisting = new DisbursementDetail().id(disbursementDetail.getId())
                                .expectedDisbursementDate(formatted).principal(disbursementDetail.getPrincipal());
                        disbursementData.add(disbursementDetailEntryExisting);
                    });
        }

        // add new entry with expected disbursement detail
        DisbursementDetail disbursementDetailsEntryNew = new DisbursementDetail().principal(disbursementAmount)
                .expectedDisbursementDate(expectedDisbursementDate);
        disbursementData.add(disbursementDetailsEntryNew);

        DateTimeFormatter parsingFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        disbursementData.forEach(detail -> detail
                .expectedDisbursementDate(FORMATTER.format(LocalDate.parse(detail.getExpectedDisbursementDate(), parsingFormatter))));
        disbursementData.sort(Comparator.comparing(detail -> LocalDate.parse(detail.getExpectedDisbursementDate(), parsingFormatter)));

        PostAddAndDeleteDisbursementDetailRequest disbursementDetailRequest = LoanRequestFactory
                .defaultLoanDisbursementDetailRequest(disbursementData);
        CommandProcessingResult loanDisburseResponse = ok(
                () -> fineractClient.loanDisbursementDetails().addAndDeleteDisbursementDetail(loanId, disbursementDetailRequest));
        testContext().set(TestContextKey.LOAN_DISBURSEMENT_DETAIL_RESPONSE, loanDisburseResponse);
    }

    @Then("Loan Tranche Details tab has the following data:")
    public void loanTrancheDetailsTabCheck(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "all")));
        Set<GetLoansLoanIdDisbursementDetails> disbursementDetails = loanDetailsResponse.getDisbursementDetails();
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

    @And("Admin checks available disbursement amount {double} EUR")
    public void checkAvailableDisbursementAmountLoan(Double availableDisbursementAmountExpected) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "all")));
        BigDecimal availableDisbursementAmountActual = loanDetails.getDelinquent().getAvailableDisbursementAmount();
        assertThat(availableDisbursementAmountActual).isEqualByComparingTo(BigDecimal.valueOf(availableDisbursementAmountExpected));
    }

    @And("Admin successfully disburse the loan without auto downpayment on {string} with {string} EUR transaction amount")
    public void disburseLoanWithoutAutoDownpayment(String actualDisbursementDate, String transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        PostLoansLoanIdResponse loanDisburseResponse = ok(() -> fineractClient.loans().stateTransitions(loanId, disburseRequest,
                Map.of("command", "disburseWithoutAutoDownPayment")));
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        Long statusActual = loanDisburseResponse.getChanges().getStatus().getId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        Long statusExpected = Long.valueOf(loanDetails.getStatus().getId());

        assertThat(statusActual)//
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, Math.toIntExact(statusActual), Math.toIntExact(statusExpected)))//
                .isEqualTo(statusExpected);//
        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount and {string} fixed emi amount")
    public void disburseLoanWithFixedEmiAmount(final String actualDisbursementDate, final String transactionAmount,
            final String fixedEmiAmount) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final long loanId = loanResponse.getLoanId();
        final PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount))
                .fixedEmiAmount(new BigDecimal(fixedEmiAmount));
        performLoanDisbursementAndVerifyStatus(loanId, disburseRequest);
    }

    @And("Admin successfully disburse the loan on {string} with {string} EUR transaction amount, {string} EUR fixed emi amount and adjust repayment date on {string}")
    public void disburseLoanWithFixedEmiAmountAndAdjustRepaymentDate(final String actualDisbursementDate, final String transactionAmount,
            final String fixedEmiAmount, final String adjustRepaymentDate) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final long loanId = loanResponse.getLoanId();
        final PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount))
                .fixedEmiAmount(new BigDecimal(fixedEmiAmount)).adjustRepaymentDate(adjustRepaymentDate);
        performLoanDisbursementAndVerifyStatus(loanId, disburseRequest);
    }

    @And("Admin successfully disburse the second loan on {string} with {string} EUR transaction amount")
    public void disburseSecondLoan(String actualDisbursementDate, String transactionAmount) throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        PostLoansLoanIdResponse loanDisburseResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        testContext().set(TestContextKey.LOAN_DISBURSE_SECOND_LOAN_RESPONSE, loanDisburseResponse);
        assertThat(loanDisburseResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_ACTIVE);

        eventCheckHelper.disburseLoanEventCheck(loanId);
        eventCheckHelper.loanDisbursalTransactionEventCheck(loanDisburseResponse);
    }

    @When("Admin successfully undo disbursal")
    public void undoDisbursal() {
        PostLoansLoanIdResponse loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.getLoanId();

        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");
        ok(() -> fineractClient.loans().stateTransitions(loanId, undoDisbursalRequest, Map.of("command", "undodisbursal")));
    }

    @When("Admin successfully undo last disbursal")
    public void undoLastDisbursal() {
        PostLoansLoanIdResponse loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.getLoanId();

        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");
        ok(() -> fineractClient.loans().stateTransitions(loanId, undoDisbursalRequest, Map.of("command", "undolastdisbursal")));
    }

    @Then("Admin can successfully undone the loan disbursal")
    public void checkUndoLoanDisbursal() {
        PostLoansLoanIdResponse loanApproveResponse = testContext().get(TestContextKey.LOAN_APPROVAL_RESPONSE);
        long loanId = loanApproveResponse.getLoanId();
        PostLoansLoanIdRequest undoDisbursalRequest = new PostLoansLoanIdRequest().note("");

        PostLoansLoanIdResponse undoDisbursalResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, undoDisbursalRequest, Map.of("command", "undodisbursal")));
        testContext().set(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, undoDisbursalResponse);
        assertThat(undoDisbursalResponse.getChanges().getStatus().getValue()).isEqualTo(LOAN_STATE_APPROVED);
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of wrong date")
    public void disburseLoanFailureWithDate(String actualDisbursementDate, String transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.disburseDateFailure((int) loanId));
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of wrong amount")
    public void disburseLoanFailureWithAmount(String actualDisbursementDate, String transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).containsPattern(ErrorMessageHelper.disburseMaxAmountFailure());
        log.debug("Error message: {}", exception.getDeveloperMessage());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} amount")
    public void disburseLoanFailureIsNotAllowed(String disbursementDate, String disbursementAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest().actualDisbursementDate(disbursementDate)
                .transactionAmount(new BigDecimal(disbursementAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.disburseIsNotAllowedFailure());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because of charge-off that was performed for the loan")
    public void disburseChargedOffLoanFailure(String actualDisbursementDate, String transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).containsPattern(ErrorMessageHelper.disburseChargedOffLoanFailure());
        log.debug("Error message: {}", exception.getDeveloperMessage());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount because disbursement date is earlier than {string}")
    public void disburseLoanFailureWithPastDate(String actualDisbursementDate, String transactionAmount, String futureApproveDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        String futureApproveDateISO = FORMATTER_EVENTS.format(FORMATTER.parse(futureApproveDate));
        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.disbursePastDateFailure((int) loanId, futureApproveDateISO));
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR transaction amount due to exceed approved amount")
    public void disbursementForbiddenExceedApprovedAmount(String actualDisbursementDate, String transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.addDisbursementExceedApprovedAmountFailure()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addDisbursementExceedApprovedAmountFailure());
    }

    @Then("Admin fails to disburse the loan on {string} with {string} EUR trn amount with total disb amount {string} and max disb amount {string} due to exceed max applied amount")
    public void disbursementForbiddenExceedMaxAppliedAmount(String actualDisbursementDate, String transactionAmount,
            String totalDisbursalAmount, String maxDisbursalAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostLoansLoanIdRequest disburseRequest = LoanRequestFactory.defaultLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus())
                .as(ErrorMessageHelper.addDisbursementExceedMaxAppliedAmountFailure(totalDisbursalAmount, maxDisbursalAmount))
                .isEqualTo(403);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.addDisbursementExceedMaxAppliedAmountFailure(totalDisbursalAmount, maxDisbursalAmount));
    }

    @And("Admin does charge-off the loan on {string}")
    public void chargeOffLoan(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsResponse chargeOffResponse = makeChargeOffTransaction(loanId, transactionDate);
        Long transactionId = chargeOffResponse.getResourceId();
        eventAssertion.assertEvent(LoanChargeOffEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId).isEqualTo(chargeOffResponse.getResourceId());
    }

    @Then("Backdated charge-off on a date {string} is forbidden")
    public void chargeOffBackdatedForbidden(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));

        Integer httpStatusCodeExpected = 403;
        String developerMessageExpected = String.format(
                "Loan: %s charge-off cannot be executed. Loan has monetary activity after the charge-off transaction date!", loanId);

        assertThat(exception.getStatus())
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(exception.getStatus(), httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(exception.getDeveloperMessage())
                .as(ErrorMessageHelper.wrongErrorMessageInFailedChargeAdjustment(exception.getDeveloperMessage(), developerMessageExpected))
                .contains(developerMessageExpected);
    }

    @And("Admin does charge-off the loan with reason {string} on {string}")
    public void chargeOffLoan(String chargeOffReason, String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        final CodeValue chargeOffReasonCodeValue = DefaultCodeValue.valueOf(chargeOffReason);
        Long chargeOffReasonCodeId = codeHelper.retrieveCodeByName(CHARGE_OFF_REASONS).getId();
        long chargeOffReasonId = codeValueResolver.resolve(chargeOffReasonCodeId, chargeOffReasonCodeValue);

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest()
                .chargeOffReasonId(chargeOffReasonId).transactionDate(transactionDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse chargeOffResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        Long transactionId = chargeOffResponse.getResourceId();

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final Optional<GetLoansLoanIdTransactions> transactionsMatch = transactions.stream()
                .filter(t -> formatter.format(t.getDate()).equals(transactionDate) && t.getType().getCapitalizedIncomeAmortization())
                .reduce((one, two) -> two);
        if (transactionsMatch.isPresent()) {
            testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_AMORTIZATION_ID, transactionsMatch.get().getId());
        }
        eventAssertion.assertEvent(LoanChargeOffEvent.class, transactionId).extractingData(LoanTransactionDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId).isEqualTo(chargeOffResponse.getResourceId());
    }

    @Then("Charge-off attempt on {string} results an error")
    public void chargeOffOnLoanWithInterestFails(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        assertThat(exception.getDeveloperMessage())
                .isEqualTo(String.format("Loan: %s Charge-off is not allowed. Loan Account is interest bearing", loanId));
    }

    @Then("Second Charge-off is not possible on {string}")
    public void secondChargeOffLoan(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.secondChargeOffFailure(loanId)).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.secondChargeOffFailure(loanId));
    }

    @And("Admin does a charge-off undo the loan")
    public void chargeOffUndo() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsResponse chargeOffUndoResponse = undoChargeOff(loanId);
        Long transactionId = chargeOffUndoResponse.getResourceId();
        eventAssertion.assertEventRaised(LoanChargeOffUndoEvent.class, transactionId);
    }

    public PostLoansLoanIdTransactionsResponse undoChargeOff(Long loanId) {
        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest();

        PostLoansLoanIdTransactionsResponse chargeOffUndoResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction(loanId, chargeOffUndoRequest, Map.of("command", "undo-charge-off")));
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE, chargeOffUndoResponse);
        return chargeOffUndoResponse;
    }

    public PostLoansLoanIdTransactionsResponse makeChargeOffTransaction(Long loanId, String transactionDate) {
        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse chargeOffResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_RESPONSE, chargeOffResponse);
        return chargeOffResponse;
    }

    @Then("Charge-off transaction is not possible on {string}")
    public void chargeOffFailure(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.chargeOffUndoFailure(loanId));
    }

    @Then("Charge-off transaction is not possible on {string} due to monetary activity before")
    public void chargeOffFailureDueToMonetaryActivityBefore(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffRequest = LoanRequestFactory.defaultChargeOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, chargeOffRequest, Map.of("command", "charge-off")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.chargeOffFailureDueToMonetaryActivityBefore(loanId)).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.chargeOffFailureDueToMonetaryActivityBefore(loanId));
    }

    @Then("Charge-off undo is not possible as the loan is not charged-off")
    public void chargeOffUndoNotPossibleFailure() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest();

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                chargeOffUndoRequest, Map.of("command", "undo-charge-off")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.chargeOffUndoFailureCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.notChargedOffFailure(loanId));
    }

    @Then("Loan has {double} outstanding amount")
    public void loanOutstanding(double totalOutstandingExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOutstandingActual = loanDetailsResponse.getSummary().getTotalOutstanding().doubleValue();
        assertThat(totalOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalOutstandingActual, totalOutstandingExpected))
                .isEqualTo(totalOutstandingExpected);
    }

    @Then("Loan has {double} interest outstanding amount")
    public void loanInterestOutstanding(double totalInterestOutstandingExpected) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanCreateResponse != null;
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assert loanDetailsResponse != null;
        assert loanDetailsResponse.getSummary() != null;
        assert loanDetailsResponse.getSummary().getInterestOutstanding() != null;
        final double totalInterestOutstandingActual = loanDetailsResponse.getSummary().getInterestOutstanding().doubleValue();
        assertThat(totalInterestOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalInterestOutstandingActual, totalInterestOutstandingExpected))
                .isEqualTo(totalInterestOutstandingExpected);
    }

    @Then("Loan has {double} total unpaid payable due interest")
    public void loanTotalUnpaidPayableDueInterest(double totalUnpaidPayableDueInterestExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalUnpaidPayableDueInterestActual = loanDetailsResponse.getSummary().getTotalUnpaidPayableDueInterest().doubleValue();
        assertThat(totalUnpaidPayableDueInterestActual).as(ErrorMessageHelper
                .wrongAmountInTotalUnpaidPayableDueInterest(totalUnpaidPayableDueInterestActual, totalUnpaidPayableDueInterestExpected))
                .isEqualTo(totalUnpaidPayableDueInterestExpected);
    }

    @Then("Loan has {double} overpaid amount")
    public void loanOverpaid(double totalOverpaidExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOverpaidActual = loanDetailsResponse.getTotalOverpaid().doubleValue();
        Double totalOutstandingActual = loanDetailsResponse.getSummary().getTotalOutstanding().doubleValue();
        double totalOutstandingExpected = 0.0;
        assertThat(totalOutstandingActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOutstanding(totalOutstandingActual, totalOutstandingExpected))
                .isEqualTo(totalOutstandingExpected);
        assertThat(totalOverpaidActual)
                .as(ErrorMessageHelper.wrongAmountInTransactionsOverpayment(totalOverpaidActual, totalOverpaidExpected))
                .isEqualTo(totalOverpaidExpected);
    }

    @Then("Loan has {double} total overdue amount")
    public void loanOverdue(double totalOverdueExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalOverdueActual = loanDetailsResponse.getSummary().getTotalOverdue().doubleValue();
        assertThat(totalOverdueActual).as(ErrorMessageHelper.wrongAmountInTotalOverdue(totalOverdueActual, totalOverdueExpected))
                .isEqualTo(totalOverdueExpected);
    }

    @Then("Loan has {double} total interest overdue amount")
    public void loanInterestOverdue(final double totalInterestOverdueExpected) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanCreateResponse != null;
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assert Objects.requireNonNull(loanDetailsResponse).getSummary() != null;
        assert loanDetailsResponse.getSummary().getInterestOverdue() != null;
        final double totalInterestOverdueActual = loanDetailsResponse.getSummary().getInterestOverdue().doubleValue();
        assertThat(totalInterestOverdueActual)
                .as(ErrorMessageHelper.wrongAmountInTotalOverdue(totalInterestOverdueActual, totalInterestOverdueExpected))
                .isEqualTo(totalInterestOverdueExpected);
    }

    @Then("Loan has {double} last payment amount")
    public void loanLastPaymentAmount(double lastPaymentAmountExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "collection")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double lastPaymentAmountActual = loanDetailsResponse.getDelinquent().getLastPaymentAmount().doubleValue();
        assertThat(lastPaymentAmountActual)
                .as(ErrorMessageHelper.wrongLastPaymentAmount(lastPaymentAmountActual, lastPaymentAmountExpected))
                .isEqualTo(lastPaymentAmountExpected);
    }

    @Then("Loan Repayment schedule has {int} periods, with the following data for periods:")
    public void loanRepaymentSchedulePeriodsCheck(int linesExpected, DataTable table) {

        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods = loanDetailsResponse.getRepaymentSchedule().getPeriods();

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
    public void loanRepaymentScheduleAmountCheck(DataTable table) {
        List<List<String>> data = table.asLists();
        List<String> header = data.get(0);
        List<String> expectedValues = data.get(1);
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        GetLoansLoanIdRepaymentSchedule repaymentSchedule = loanDetailsResponse.getRepaymentSchedule();
        validateRepaymentScheduleTotal(header, repaymentSchedule, expectedValues);
    }

    @Then("Loan Transactions tab has a transaction with date: {string}, and with the following data:")
    public void loanTransactionsTransactionWithGivenDateDataCheck(String date, DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();

        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);

        List<List<String>> actualValuesList = transactions.stream().filter(t -> date.equals(FORMATTER.format(t.getDate())))
                .map(t -> fetchValuesOfTransaction(data.get(0), t)).collect(Collectors.toList());
        boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));

        assertThat(containsExpectedValues)
                .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, 1, actualValuesList, expectedValues)).isTrue();
    }

    @Then("Loan Transactions tab has the following data:")
    public void loanTransactionsTabCheck(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void loanTransactionsLatestTransactionReverted(String transactionType) {
        loanTransactionsLatestTransactionReverted(null, transactionType);
    }

    @Then("In Loan Transactions the {string}th Transaction has Transaction type={string} and is reverted")
    public void loanTransactionsLatestTransactionReverted(String nthTransactionStr, String transactionType) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void loanTransactionsGivenTransactionReverted(String transactionType, String transactionDate) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        List<GetLoansLoanIdTransactions> transactionsMatch = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .collect(Collectors.toList());//
        boolean isReverted = transactionsMatch.stream().anyMatch(t -> t.getManuallyReversed());

        assertThat(isReverted).as(ErrorMessageHelper.transactionIsNotReversedError(isReverted, true)).isEqualTo(true);
    }

    @Then("On Loan Transactions tab the {string} Transaction with date {string} is NOT reverted")
    public void loanTransactionsGivenTransactionNotReverted(String transactionType, String transactionDate) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        List<GetLoansLoanIdTransactions> transactionsMatch = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .collect(Collectors.toList());//
        boolean isReverted = transactionsMatch.stream().anyMatch(t -> t.getManuallyReversed());

        assertThat(isReverted).as(ErrorMessageHelper.transactionIsNotReversedError(isReverted, false)).isEqualTo(false);
    }

    @Then("In Loan Transactions the {string}th Transaction with type={string} and date {string} has non-null external-id")
    public void loanTransactionsNthTransactionHasNonNullExternalId(String nthTransactionStr, String transactionType,
            String transactionDate) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void loanTransactionsHaveNonNullExternalId() {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();

        assertThat(transactions.stream().allMatch(transaction -> transaction.getExternalId() != null))
                .as(ErrorMessageHelper.transactionHasNullResourceValue("", "external-id")).isTrue();
    }

    @Then("Check required {string}th transaction for non-null eternal-id")
    public void loanTransactionHasNonNullExternalId(String nThNumber) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdTransactions targetTransaction;
        if (nThNumber.equals("1")) {
            targetTransaction = testContext().get(TestContextKey.LOAN_TRANSACTION_RESPONSE);
        } else {
            targetTransaction = testContext().get(TestContextKey.LOAN_SECOND_TRANSACTION_RESPONSE);
        }
        Long targetTransactionId = targetTransaction.getId();

        GetLoansLoanIdTransactionsTransactionIdResponse transaction = ok(
                () -> fineractClient.loanTransactions().retrieveTransaction(loanId, targetTransactionId, Map.of()));
        assertThat(transaction.getExternalId())
                .as(ErrorMessageHelper.transactionHasNullResourceValue(transaction.getType().getCode(), "external-id")).isNotNull();
    }

    @Then("Loan Transactions tab has none transaction")
    public void loanTransactionsTabNoneTransaction() {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assertThat(transactions.size()).isZero();
    }

    @Then("Loan Charges tab has a given charge with the following data:")
    public void loanChargesGivenChargeDataCheck(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "charges")));
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.getCharges();

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
    public void loanChargesTabCheck(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "charges")));
        List<GetLoansLoanIdLoanChargeData> charges = loanDetailsResponse.getCharges();

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
    public void loanStatus(String statusExpected) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        Integer loanStatusActualValue = loanDetailsResponse.getStatus().getId();

        LoanStatus loanStatusExpected = LoanStatus.valueOf(statusExpected);
        Integer loanStatusExpectedValue = loanStatusExpected.getValue();

        assertThat(loanStatusActualValue).as(ErrorMessageHelper.wrongLoanStatus(resourceId, loanStatusActualValue, loanStatusExpectedValue))
                .isEqualTo(loanStatusExpectedValue);
    }

    @Then("Loan's all installments have obligations met")
    public void loanInstallmentsObligationsMet() {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        List<GetLoansLoanIdRepaymentPeriod> repaymentPeriods = loanDetailsResponse.getRepaymentSchedule().getPeriods();

        boolean allInstallmentsObligationsMet = repaymentPeriods.stream()
                .allMatch(t -> t.getDaysInPeriod() == null || t.getObligationsMetOnDate() != null);
        assertThat(allInstallmentsObligationsMet).isTrue();
    }

    @Then("Loan is closed with zero outstanding balance and it's all installments have obligations met")
    public void loanClosedAndInstallmentsObligationsMet() throws IOException {
        loanInstallmentsObligationsMet();
        loanOutstanding(0);
        loanStatus("CLOSED_OBLIGATIONS_MET");
    }

    @Then("Loan closedon_date is {string}")
    public void loanClosedonDate(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        if ("null".equals(date)) {
            assertThat(loanDetailsResponse.getTimeline().getClosedOnDate()).isNull();
        } else {
            assertThat(FORMATTER.format(loanDetailsResponse.getTimeline().getClosedOnDate())).isEqualTo(date);
        }
    }

    @Then("Admin can successfully set Fraud flag to the loan")
    public void setFraud() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.enableFraudFlag();

        PutLoansLoanIdResponse responseMod = ok(
                () -> fineractClient.loans().modifyLoanApplication(loanId, putLoansLoanIdRequest, Map.of("command", "markAsFraud")));
        testContext().set(TestContextKey.LOAN_FRAUD_MODIFY_RESPONSE, responseMod);
        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Boolean fraudFlagActual = loanDetailsResponse.getFraud();
        assertThat(fraudFlagActual).as(ErrorMessageHelper.wrongFraudFlag(fraudFlagActual, true)).isEqualTo(true);
    }

    @Then("Admin can successfully unset Fraud flag to the loan")
    public void unsetFraud() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.disableFraudFlag();

        PutLoansLoanIdResponse responseMod = ok(
                () -> fineractClient.loans().modifyLoanApplication(loanId, putLoansLoanIdRequest, Map.of("command", "markAsFraud")));
        testContext().set(TestContextKey.LOAN_FRAUD_MODIFY_RESPONSE, responseMod);
        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Boolean fraudFlagActual = loanDetailsResponse.getFraud();
        assertThat(fraudFlagActual).as(ErrorMessageHelper.wrongFraudFlag(fraudFlagActual, false)).isEqualTo(false);
    }

    @Then("Fraud flag modification fails")
    public void failedFraudModification() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getResourceId();

        PutLoansLoanIdRequest putLoansLoanIdRequest = LoanRequestFactory.disableFraudFlag();

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanApplication(loanId, putLoansLoanIdRequest, Map.of("command", "markAsFraud")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.loanFraudFlagModificationMsg(loanId.toString()));
    }

    @Then("Transaction response has boolean value in header {string}: {string}")
    public void transactionHeaderCheckBoolean(String headerKey, String headerValue) {
        Map<String, Collection<String>> headers = testContext().get(TestContextKey.LOAN_PAYMENT_TRANSACTION_HEADERS);
        String headerValueActual = null;
        if (headers != null && headers.containsKey(headerKey)) {
            Collection<String> values = headers.get(headerKey);
            headerValueActual = values != null && !values.isEmpty() ? values.iterator().next() : null;
        }
        assertThat(headerValueActual).as(ErrorMessageHelper.wrongValueInResponseHeader(headerKey, headerValueActual, headerValue))
                .isEqualTo(headerValue);
    }

    @Then("Transaction response has {double} EUR value for transaction amount")
    public void transactionAmountCheck(double amountExpected) {
        PostLoansLoanIdTransactionsResponse paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Double amountActual = Double.valueOf(paymentTransactionResponse.getChanges().getTransactionAmount());
        assertThat(amountActual).as(ErrorMessageHelper.wrongAmountInTransactionsResponse(amountActual, amountExpected))
                .isEqualTo(amountExpected);
    }

    @Then("Transaction response has the correct clientId and the loanId of the first transaction")
    public void transactionClientIdAndLoanIdCheck() {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        Long clientIdExpected = clientResponse.getClientId();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanIdExpected = Long.valueOf(loanResponse.getLoanId());

        PostLoansLoanIdTransactionsResponse paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Long clientIdActual = paymentTransactionResponse.getClientId();
        Long loanIdActual = paymentTransactionResponse.getLoanId();

        assertThat(clientIdActual).as(ErrorMessageHelper.wrongClientIdInTransactionResponse(clientIdActual, clientIdExpected))
                .isEqualTo(clientIdExpected);
        assertThat(loanIdActual).as(ErrorMessageHelper.wrongLoanIdInTransactionResponse(loanIdActual, loanIdExpected))
                .isEqualTo(loanIdExpected);
    }

    @Then("Transaction response has the clientId for the second client and the loanId of the second transaction")
    public void transactionSecondClientIdAndSecondLoanIdCheck() {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE);
        Long clientIdExpected = clientResponse.getClientId();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        Long loanIdExpected = Long.valueOf(loanResponse.getLoanId());

        PostLoansLoanIdTransactionsResponse paymentTransactionResponse = testContext()
                .get(TestContextKey.LOAN_PAYMENT_TRANSACTION_RESPONSE);
        Long clientIdActual = paymentTransactionResponse.getClientId();
        Long loanIdActual = paymentTransactionResponse.getLoanId();

        assertThat(clientIdActual).as(ErrorMessageHelper.wrongClientIdInTransactionResponse(clientIdActual, clientIdExpected))
                .isEqualTo(clientIdExpected);
        assertThat(loanIdActual).as(ErrorMessageHelper.wrongLoanIdInTransactionResponse(loanIdActual, loanIdExpected))
                .isEqualTo(loanIdExpected);
    }

    @Then("Loan has {int} {string} transactions on Transactions tab")
    public void checkNrOfTransactions(int nrOfTransactionsExpected, String transactionTypeInput) {
        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));

        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();
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
    public void checkNrOfTransactionsOnSecondLoan(int nrOfTransactionsExpected, String transactionTypeInput) {
        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));

        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();
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
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

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
    public void isLoanChargedOff(String chargeOffDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        LocalDate expectedChargeOffDate = LocalDate.parse(chargeOffDate, FORMATTER);

        assertThat(loanDetailsResponse.getChargedOff()).isEqualTo(true);
        assertThat(loanDetailsResponse.getTimeline().getChargedOffOnDate()).isEqualTo(expectedChargeOffDate);
    }

    @And("Admin checks that last closed business date of loan is {string}")
    public void getLoanLastCOBDate(String date) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        log.debug("Loan ID: {}", loanId);
        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        if ("null".equals(date)) {
            assertThat(loanDetails.getLastClosedBusinessDate()).isNull();
        } else {
            assertThat(FORMATTER.format(Objects.requireNonNull(loanDetails.getLastClosedBusinessDate()))).isEqualTo(date);
        }
    }

    @When("Admin runs COB catch up")
    public void runLoanCOBCatchUp() {
        try {
            executeVoid(() -> fineractClient.loanCobCatchUp().executeLoanCOBCatchUp());
        } catch (CallFailedRuntimeException e) {
            if (e.getStatus() == 400) {
                log.info("COB catch-up is already running (400 response), continuing with test");
            } else {
                throw e;
            }
        }
    }

    @When("Admin checks that Loan COB is running until the current business date")
    public void checkLoanCOBCatchUpRunningUntilCOBBusinessDate() {
        await().atMost(Duration.ofMillis(jobPollingProperties.getTimeoutInMillis())) //
                .pollInterval(Duration.ofMillis(jobPollingProperties.getIntervalInMillis())) //
                .until(() -> {
                    IsCatchUpRunningDTO isCatchUpRunningResponse = ok(() -> fineractClient.loanCobCatchUp().isCatchUpRunning());
                    IsCatchUpRunningDTO isCatchUpRunning = isCatchUpRunningResponse;
                    return isCatchUpRunning.getCatchUpRunning();
                });
        // Then wait for catch-up to complete
        await().atMost(Duration.ofMinutes(4)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5)).until(() -> {
            // Check if catch-up is still running
            IsCatchUpRunningDTO statusResponse = ok(() -> fineractClient.loanCobCatchUp().isCatchUpRunning());
            // Only proceed with date check if catch-up is not running
            if (!statusResponse.getCatchUpRunning()) {
                // Get the current business date
                BusinessDateResponse businessDateResponse = ok(
                        () -> fineractClient.businessDateManagement().getBusinessDate(BusinessDateHelper.COB, Map.of()));
                LocalDate currentBusinessDate = businessDateResponse.getDate();

                // Get the last closed business date
                OldestCOBProcessedLoanDTO catchUpResponse = ok(() -> fineractClient.loanCobCatchUp().getOldestCOBProcessedLoan());
                LocalDate lastClosedDate = catchUpResponse.getCobBusinessDate();

                // Verify that the last closed date is not before the current business date
                return !lastClosedDate.isBefore(currentBusinessDate);
            }
            return false;
        });
    }

    @Then("Loan's actualMaturityDate is {string}")
    public void checkActualMaturityDate(String actualMaturityDateExpected) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        LocalDate actualMaturityDate = loanDetailsResponse.getTimeline().getActualMaturityDate();
        String actualMaturityDateActual = FORMATTER.format(actualMaturityDate);

        assertThat(actualMaturityDateActual)
                .as(ErrorMessageHelper.wrongDataInActualMaturityDate(actualMaturityDateActual, actualMaturityDateExpected))
                .isEqualTo(actualMaturityDateExpected);
    }

    @Then("LoanAccrualTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanAccrualTransactionCreatedBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions accrualTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual".equals(t.getType().getValue()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual transaction found on %s", date)));
        Long accrualTransactionId = accrualTransaction.getId();

        eventAssertion.assertEventRaised(LoanAccrualTransactionCreatedBusinessEvent.class, accrualTransactionId);
    }

    @Then("LoanAccrualAdjustmentTransactionBusinessEvent is raised on {string}")
    public void checkLoanAccrualAdjustmentTransactionBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions accrualAdjustmentTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Adjustment".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual Adjustment transaction found on %s", date)));
        Long accrualAdjustmentTransactionId = accrualAdjustmentTransaction.getId();

        eventAssertion.assertEventRaised(LoanAccrualAdjustmentTransactionBusinessEvent.class, accrualAdjustmentTransactionId);
    }

    @Then("LoanChargeAdjustmentPostBusinessEvent is raised on {string}")
    public void checkLoanChargeAdjustmentPostBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();

        GetLoansLoanIdTransactions loanTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Charge Adjustment".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Charge Adjustment transaction found on %s", date)));

        eventAssertion.assertEventRaised(LoanChargeAdjustmentPostBusinessEvent.class, loanTransaction.getId());
    }

    @Then("BulkBusinessEvent is not raised on {string}")
    public void checkLoanBulkBusinessEventNotCreatedBusinessEvent(String date) {
        eventAssertion.assertEventNotRaised(BulkBusinessEvent.class, em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    @Then("LoanAccrualTransactionCreatedBusinessEvent is not raised on {string}")
    public void checkLoanAccrualTransactionNotCreatedBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();

        assertThat(transactions).as("Unexpected Accrual activity transaction found on %s", date)
                .noneMatch(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Activity".equals(t.getType().getValue()));

        eventAssertion.assertEventNotRaised(LoanAccrualTransactionCreatedBusinessEvent.class,
                em -> FORMATTER.format(em.getBusinessDate()).equals(date));
    }

    public GetLoansLoanIdTransactions getLoanTransactionIdByDate(String transactionType, String transactionDate) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();

        GetLoansLoanIdTransactions loanTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionType, transactionDate)));
        return loanTransaction;
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionIdById(Long transactionId) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        return ok(() -> fineractClient.loanTransactions().retrieveTransaction(loanId, transactionId, Map.of()));
    }

    @Then("{string} transaction on {string} got reverse-replayed on {string}")
    public void checkLoanAdjustTransactionBusinessEvent(String transactionType, String transactionDate, String submittedOnDate) {
        GetLoansLoanIdTransactions loanTransaction = getLoanTransactionIdByDate(transactionType, transactionDate);

        Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations = loanTransaction.getTransactionRelations();
        Long originalTransactionId = transactionRelations.stream().map(GetLoansLoanIdLoanTransactionRelation::getToLoanTransaction)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction was reversed, but not replayed!"));

        // Check whether reverse-replay event got occurred
        eventAssertion.assertEvent(LoanAdjustTransactionBusinessEvent.class, originalTransactionId)
                .extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isNotEqualTo(null)
                .extractingData(e -> e.getNewTransactionDetail().getId()).isEqualTo(loanTransaction.getId());
        // Check whether there was just ONE event related to this transaction
        eventAssertion.assertEventNotRaised(LoanAdjustTransactionBusinessEvent.class, originalTransactionId);
        assertThat(FORMATTER.format(loanTransaction.getSubmittedOnDate()))
                .as("Loan got replayed on %s", loanTransaction.getSubmittedOnDate()).isEqualTo(submittedOnDate);
    }

    @Then("Store {string} transaction created on {string} date as {string}th transaction")
    public void storeLoanTransactionId(String transactionType, String transactionDate, String nthTrnOrderNumber) {
        GetLoansLoanIdTransactions loanTransaction = getLoanTransactionIdByDate(transactionType, transactionDate);
        if (nthTrnOrderNumber.equals("1")) {
            testContext().set(TestContextKey.LOAN_TRANSACTION_RESPONSE, loanTransaction);
        } else {
            testContext().set(TestContextKey.LOAN_SECOND_TRANSACTION_RESPONSE, loanTransaction);
        }
    }

    @Then("LoanAdjustTransactionBusinessEvent is raised with transaction got reversed on {string}")
    public void checkLoanAdjustTransactionBusinessEventWithReversedTrn(String submittedOnDate) {
        GetLoansLoanIdTransactions originalTransaction = testContext().get(TestContextKey.LOAN_TRANSACTION_RESPONSE);
        Long originalTransactionId = originalTransaction.getId();
        GetLoansLoanIdTransactionsTransactionIdResponse loanTransaction = getLoanTransactionIdById(originalTransactionId);

        // Check whether reversed event got occurred
        eventAssertion.assertEvent(LoanAdjustTransactionBusinessEvent.class, originalTransactionId)
                .extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null)
                .extractingData(e -> e.getTransactionToAdjust().getId()).isEqualTo(originalTransactionId);
        // Check whether there was just ONE event related to this transaction
        eventAssertion.assertEventNotRaised(LoanAdjustTransactionBusinessEvent.class, originalTransactionId);
        assertThat(FORMATTER.format(loanTransaction.getReversedOnDate())).as("Loan got replayed on %s", loanTransaction.getReversedOnDate())
                .isEqualTo(submittedOnDate);
    }

    @Then("LoanAdjustTransactionBusinessEvent is raised with transaction on {string} got reversed on {string}")
    public void checkLoanAdjustTransactionBusinessEventWithReversedTrn(String transactionDate, String submittedOnDate) {
        Long originalTransactionId;
        GetLoansLoanIdTransactions loanTransaction1 = testContext().get(TestContextKey.LOAN_TRANSACTION_RESPONSE);
        GetLoansLoanIdTransactions loanTransaction2 = testContext().get(TestContextKey.LOAN_SECOND_TRANSACTION_RESPONSE);
        if (FORMATTER.format(loanTransaction1.getDate()).equals(transactionDate)) {
            originalTransactionId = loanTransaction1.getId();
        } else {
            originalTransactionId = loanTransaction2.getId();
        }
        GetLoansLoanIdTransactionsTransactionIdResponse loanTransaction = getLoanTransactionIdById(originalTransactionId);

        // Check whether reversedevent got occurred
        eventAssertion.assertEvent(LoanAdjustTransactionBusinessEvent.class, originalTransactionId)
                .extractingData(e -> e.getTransactionToAdjust().getId()).isEqualTo(originalTransactionId)
                .extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
        // Check whether there was just ONE event related to this transaction
        eventAssertion.assertEventNotRaised(LoanAdjustTransactionBusinessEvent.class, originalTransactionId);
        assertThat(FORMATTER.format(loanTransaction.getReversedOnDate())).as("Loan got replayed on %s", loanTransaction.getReversedOnDate())
                .isEqualTo(submittedOnDate);
    }

    @When("Save external ID of {string} transaction made on {string} as {string}")
    public void saveExternalIdForTransaction(String transactionName, String transactionDate, String externalIdKey) {
        GetLoansLoanIdTransactions loanTransaction = getLoanTransactionIdByDate(transactionName, transactionDate);

        String externalId = loanTransaction.getExternalId();
        testContext().set(externalIdKey, externalId);
        log.debug("Transaction external ID: {} saved to testContext", externalId);
    }

    @Then("External ID of replayed {string} on {string} is matching with {string}")
    public void checkExternalIdForReplayedAccrualActivity(String transactionType, String transactionDate, String savedExternalIdKey) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdTransactions transactionDetails = getLoanTransactionIdByDate(transactionType, transactionDate);

        Set<GetLoansLoanIdLoanTransactionRelation> transactionRelations = transactionDetails.getTransactionRelations();
        Long originalTransactionId = transactionRelations.stream().map(GetLoansLoanIdLoanTransactionRelation::getToLoanTransaction)
                .filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction was reversed, but not replayed!"));

        String externalIdExpected = testContext().get(savedExternalIdKey).toString();
        String externalIdActual = transactionDetails.getExternalId();
        assertThat(externalIdActual).as(ErrorMessageHelper.wrongExternalID(externalIdActual, externalIdExpected))
                .isEqualTo(externalIdExpected);

        GetLoansLoanIdTransactionsTransactionIdResponse originalTransaction = ok(
                () -> fineractClient.loanTransactions().retrieveTransaction(loanId, originalTransactionId, Map.of()));
        assertNull(originalTransaction.getExternalId(),
                String.format("Original transaction external id is not null %n%s", originalTransaction));
    }

    @Then("LoanTransactionAccrualActivityPostBusinessEvent is raised on {string}")
    public void checkLoanTransactionAccrualActivityPostBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions accrualTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Accrual Activity".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Accrual activity transaction found on %s", date)));
        Long accrualTransactionId = accrualTransaction.getId();

        eventAssertion.assertEventRaised(LoanTransactionAccrualActivityPostEvent.class, accrualTransactionId);
    }

    @Then("LoanRescheduledDueAdjustScheduleBusinessEvent is raised on {string}")
    public void checkLoanRescheduledDueAdjustScheduleBusinessEvent(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        eventAssertion.assertEventRaised(LoanRescheduledDueAdjustScheduleEvent.class, loanId);
    }

    @Then("Loan details and event has the following last repayment related data:")
    public void checkLastRepaymentData(DataTable table) {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String lastPaymentAmountExpected = expectedValues.get(0);
        String lastPaymentDateExpected = expectedValues.get(1);
        String lastRepaymentAmountExpected = expectedValues.get(2);
        String lastRepaymentDateExpected = expectedValues.get(3);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "collection")));
        GetLoansLoanIdDelinquencySummary delinquent = loanDetailsResponse.getDelinquent();
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
    public void chargeOffUndoWithReversalExternalId() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        String reversalExternalId = Utils.randomNameGenerator("reversalExtId_", 3);
        PostLoansLoanIdTransactionsRequest chargeOffUndoRequest = LoanRequestFactory.defaultUndoChargeOffRequest()
                .reversalExternalId(reversalExternalId);

        PostLoansLoanIdTransactionsResponse chargeOffUndoResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction(loanId, chargeOffUndoRequest, Map.of("command", "undo-charge-off")));
        testContext().set(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE, chargeOffUndoResponse);
        Long transactionId = chargeOffUndoResponse.getResourceId();

        GetLoansLoanIdTransactionsTransactionIdResponse transactionResponse = ok(
                () -> fineractClient.loanTransactions().retrieveTransaction(loanId, transactionId, Map.of()));
        assertThat(transactionResponse.getReversalExternalId()).isEqualTo(reversalExternalId);
    }

    @Then("Loan Charge-off undo event has reversed on date {string} for charge-off undo")
    public void reversedOnDateIsNotNullForEvent(String reversedDate) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void checkMaturity(DataTable table) {
        List<List<String>> data = table.asLists();
        List<String> expectedValues = data.get(1);
        String actualMaturityDateExpected = expectedValues.get(0);
        String expectedMaturityDateExpected = expectedValues.get(1);

        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        GetLoansLoanIdTimeline timeline = loanDetailsResponse.getTimeline();
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
    public void deleteLoanWithExternalId() {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanCreateResponse.getLoanId();
        String loanExternalId = loanCreateResponse.getResourceExternalId();
        DeleteLoansLoanIdResponse deleteLoanResponse = ok(() -> fineractClient.loans().deleteLoanApplication1(loanExternalId));
        assertThat(deleteLoanResponse.getLoanId()).isEqualTo(loanId);
        assertThat(deleteLoanResponse.getResourceExternalId()).isEqualTo(loanExternalId);
    }

    @Then("Admin fails to delete the loan with incorrect external id")
    public void failedDeleteLoanWithExternalId() {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.getResourceExternalId();
        CallFailedRuntimeException exception = fail(() -> fineractClient.loans().deleteLoanApplication1(loanExternalId.substring(5)));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(404);
    }

    @When("Admin set {string} loan product {string} transaction type to {string} future installment allocation rule")
    public void editFutureInstallmentAllocationTypeForLoanProduct(String loanProductName, String transactionTypeToChange,
            String futureInstallmentAllocationRuleNew) {
        DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProductName);
        Long loanProductId = loanProductResolver.resolve(product);
        log.debug("loanProductId: {}", loanProductId);

        GetLoanProductsProductIdResponse loanProductDetails = ok(
                () -> fineractClient.loanProducts().retrieveLoanProductDetails(loanProductId));
        List<AdvancedPaymentData> paymentAllocation = loanProductDetails.getPaymentAllocation();

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

        ok(() -> fineractClient.loanProducts().updateLoanProduct(loanProductId, putLoanProductsProductIdRequest));
    }

    @When("Admin sets repaymentStartDateType for {string} loan product to {string}")
    public void editRepaymentStartDateType(String loanProductName, String repaymentStartDateType) {
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

        ok(() -> fineractClient.loanProducts().updateLoanProduct(loanProductId, putLoanProductsProductIdRequest));
    }

    @And("Admin does write-off the loan on {string}")
    public void writeOffLoan(String transactionDate) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostLoansLoanIdTransactionsRequest writeOffRequest = LoanRequestFactory.defaultWriteOffRequest().transactionDate(transactionDate)
                .dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        PostLoansLoanIdTransactionsResponse writeOffResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, writeOffRequest, Map.of("command", "writeoff")));
        testContext().set(TestContextKey.LOAN_WRITE_OFF_RESPONSE, writeOffResponse);
    }

    @And("Admin does write-off the loan on {string} with write off reason: {string}")
    public void writeOffLoan(String transactionDate, String writeOffReason) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        final Long writeOffReasonCodeId = codeHelper.retrieveCodeByName("WriteOffReasons").getId();
        final CodeValue writeOffReasonCodeValueBadDebt = DefaultCodeValue.valueOf(writeOffReason);
        long writeOffReasonId = codeValueResolver.resolve(writeOffReasonCodeId, writeOffReasonCodeValueBadDebt);

        PostLoansLoanIdTransactionsRequest writeOffRequest = new PostLoansLoanIdTransactionsRequest()//
                .transactionDate(transactionDate)//
                .writeoffReasonId(writeOffReasonId)//
                .dateFormat(DATE_FORMAT)//
                .locale(DEFAULT_LOCALE)//
                .note("Write Off");//

        PostLoansLoanIdTransactionsResponse writeOffResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, writeOffRequest, Map.of("command", "writeoff")));
        testContext().set(TestContextKey.LOAN_WRITE_OFF_RESPONSE, writeOffResponse);
    }

    @Then("Admin fails to undo {string}th transaction made on {string}")
    public void undoTransaction(String nthTransaction, String transactionDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        List<GetLoansLoanIdTransactions> transactions = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions"))).getTransactions();

        int nthItem = Integer.parseInt(nthTransaction) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))).toList().get(nthItem);

        PostLoansLoanIdTransactionsTransactionIdRequest transactionUndoRequest = LoanRequestFactory.defaultTransactionUndoRequest()
                .transactionDate(transactionDate);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), transactionUndoRequest, Map.of()));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(503);
    }

    @Then("Loan {string} repayment transaction on {string} with {double} EUR transaction amount results in error")
    public void loanTransactionWithErrorCheck(String repaymentType, String transactionDate, double transactionAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        DefaultPaymentType paymentType = DefaultPaymentType.valueOf(repaymentType);
        long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        PostLoansLoanIdTransactionsRequest repaymentRequest = LoanRequestFactory.defaultRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(transactionAmount).paymentTypeId(paymentTypeValue).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, repaymentRequest, Map.of("command", "repayment")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
    }

    @Then("Loan details has the downpayment amount {string} in summary.totalRepaymentTransaction")
    public void totalRepaymentTransaction(String expectedAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        Double expectedAmountParsed = Double.parseDouble(expectedAmount);
        Double totalRepaymentTransaction = loanDetails.getSummary().getTotalRepaymentTransaction().doubleValue();

        assertThat(totalRepaymentTransaction)
                .as(ErrorMessageHelper.wrongAmountInTotalRepaymentTransaction(totalRepaymentTransaction, expectedAmountParsed))
                .isEqualTo(expectedAmountParsed);
    }

    @Then("LoanDetails has fixedLength field with int value: {int}")
    public void checkLoanDetailsFieldAndValueInt(int fieldValue) throws NoSuchMethodException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        Integer fixedLengthactual = loanDetails.getFixedLength();
        assertThat(fixedLengthactual).as(ErrorMessageHelper.wrongfixedLength(fixedLengthactual, fieldValue)).isEqualTo(fieldValue);
    }

    @Then("Loan has availableDisbursementAmountWithOverApplied field with value: {double}")
    public void checkLoanDetailsAvailableDisbursementAmountWithOverAppliedField(final double fieldValue) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "collection")));
        assert loanDetails != null;
        assert loanDetails.getDelinquent() != null;
        assert loanDetails.getDelinquent().getAvailableDisbursementAmountWithOverApplied() != null;
        final Double availableDisbursementAmountWithOverApplied = loanDetails.getDelinquent()
                .getAvailableDisbursementAmountWithOverApplied().doubleValue();
        assertThat(availableDisbursementAmountWithOverApplied).as(
                ErrorMessageHelper.wrongAvailableDisbursementAmountWithOverApplied(availableDisbursementAmountWithOverApplied, fieldValue))
                .isEqualTo(fieldValue);
    }

    @Then("Loan emi amount variations has {int} variation, with the following data:")
    public void loanEmiAmountVariationsCheck(final int linesExpected, final DataTable table) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanCreateResponse);
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "all")));
        final List<GetLoansLoanIdLoanTermVariations> emiAmountVariations = loanDetailsResponse.getEmiAmountVariations();

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
    public void loanTermVariationsCheck(final int linesExpected, final DataTable table) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanCreateResponse);
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "loanTermVariations")));
        final List<GetLoansLoanIdLoanTermVariations> loanTermVariations = loanDetailsResponse.getLoanTermVariations();
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
    public void loanTransactionsRelationshipCheck(String nthTransactionFromStr, String relationshipType, String nthTransactionToStr) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void loanProductTemplateChargeOffReasonOptionsCheck(final int linesExpected, final DataTable table) {
        final GetLoanProductsTemplateResponse loanProductDetails = ok(
                () -> fineractClient.loanProducts().retrieveTemplate11(Map.of("staffInSelectedOfficeOnly", "false")));
        assertNotNull(loanProductDetails);
        final List<GetLoanProductsChargeOffReasonOptions> chargeOffReasonOptions = loanProductDetails.getChargeOffReasonOptions();
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
    public void specificLoanProductChargeOffReasonOptionsCheck(final String loanProductName, final int linesExpected,
            final DataTable table) {
        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProductName);
        final Long loanProductId = loanProductResolver.resolve(product);
        final GetLoanProductsProductIdResponse loanProductDetails = ok(
                () -> fineractClient.loanProducts().retrieveLoanProductDetailsUniversal(loanProductId, Map.of("template", "true")));
        assertNotNull(loanProductDetails);
        final List<GetLoanProductsChargeOffReasonOptions> chargeOffReasonOptions = loanProductDetails.getChargeOffReasonOptions();
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

    private void createCustomizedLoan(final List<String> loanData, final boolean withEmi) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    private void createCustomizedLoanWithProductCharges(final List<String> loanData) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

        final DefaultLoanProduct product = DefaultLoanProduct.valueOf(loanProduct);
        final Long loanProductId = loanProductResolver.resolve(product);
        final GetLoanProductsProductIdResponse loanProductDetails = ok(
                () -> fineractClient.loanProducts().retrieveLoanProductDetails(loanProductId));

        final List<PostLoansRequestChargeData> loanCharges = new ArrayList<>();

        assert loanProductDetails != null;
        if (loanProductDetails.getCharges() != null) {
            for (final LoanProductChargeData chargeData : loanProductDetails.getCharges()) {
                loanCharges.add(new PostLoansRequestChargeData().chargeId(chargeData.getId()).amount(chargeData.getAmount()));
            }
        }

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
                .graceOnInterestPayment(graceOnInterestCharged).transactionProcessingStrategyCode(transactionProcessingStrategyCodeValue)
                .charges(loanCharges);

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithInterestRateFrequency(final List<String> loanData) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithGraceOnArrearsAgeing(final List<String> loanData) throws IOException {
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
        final String graceOnArrearsAgeingStr = loanData.get(16);

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        Integer graceOnArrearsAgeingValue = Integer.valueOf(graceOnArrearsAgeingStr);

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
                .graceOnArrearsAgeing(graceOnArrearsAgeingValue);//

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithCharges(final List<String> loanData) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementDetails(final List<String> loanData) {
        final String expectedDisbursementDate = loanData.get(18);
        final Double disbursementPrincipalAmount = Double.valueOf(loanData.get(19));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDate)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmount)));

        createFullyCustomizedLoanWithChargesExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanWithChargesAndExpectedTrancheDisbursementsDetails(final List<String> loanData) {
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
            List<PostLoansDisbursementData> disbursementDetail) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    public void createFullyCustomizedLoanWithExpectedTrancheDisbursementDetails(final List<String> loanData) {
        final String expectedDisbursementDate = loanData.get(16);
        final Double disbursementPrincipalAmount = Double.valueOf(loanData.get(17));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDate)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmount)));

        createFullyCustomizedLoanExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanWithExpectedTrancheDisbursementsDetails(final List<String> loanData) {
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

    public void createFullyCustomizedLoanWithThreeExpectedTrancheDisbursementsDetails(final List<String> loanData) {
        final String expectedDisbursementDateFirstDisbursal = loanData.get(16);
        final Double disbursementPrincipalAmountFirstDisbursal = Double.valueOf(loanData.get(17));

        final String expectedDisbursementDateSecondDisbursal = loanData.get(18);
        final Double disbursementPrincipalAmountSecondDisbursal = Double.valueOf(loanData.get(19));

        final String expectedDisbursementDateThirdDisbursal = loanData.get(20);
        final Double disbursementPrincipalAmountThirdDisbursal = Double.valueOf(loanData.get(21));

        List<PostLoansDisbursementData> disbursementDetail = new ArrayList<>();
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateFirstDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountFirstDisbursal)));
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateSecondDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountSecondDisbursal)));
        disbursementDetail.add(new PostLoansDisbursementData().expectedDisbursementDate(expectedDisbursementDateThirdDisbursal)
                .principal(BigDecimal.valueOf(disbursementPrincipalAmountThirdDisbursal)));

        createFullyCustomizedLoanExpectsTrancheDisbursementDetails(loanData, disbursementDetail);
    }

    public void createFullyCustomizedLoanExpectsTrancheDisbursementDetails(final List<String> loanData,
            List<PostLoansDisbursementData> disbursementDetail) {
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

        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new zero charge-off Loan with interest recalculation and date: {string}")
    public void createLoanWithInterestRecalculationAndZeroChargeOffBehaviour(final String date) {
        createLoanWithZeroChargeOffBehaviour(date, true);
    }

    @When("Admin creates a new zero charge-off Loan without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndZeroChargeOffBehaviour(final String date) {
        createLoanWithZeroChargeOffBehaviour(date, false);
    }

    private void createLoanWithZeroChargeOffBehaviour(final String date, final boolean isInterestRecalculation) {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    @When("Admin creates a new accelerate maturity charge-off Loan without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndAccelerateMaturityChargeOffBehaviour(final String date) {
        createLoanWithLoanBehaviour(date, false,
                DefaultLoanProduct.valueOf(LP2_ADV_PYMNT_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR.getName()));
    }

    @When("Admin creates a new accelerate maturity charge-off Loan with last installment strategy, without interest recalculation and with date: {string}")
    public void createLoanWithoutInterestRecalculationAndAccelerateMaturityChargeOffBehaviourLastInstallmentStrategy(final String date) {
        createLoanWithLoanBehaviour(date, false,
                DefaultLoanProduct.valueOf(LP2_ACCELERATE_MATURITY_CHARGE_OFF_BEHAVIOUR_LAST_INSTALLMENT_STRATEGY.getName()));
    }

    private void createLoanWithLoanBehaviour(final String date, final boolean isInterestRecalculation, final DefaultLoanProduct product) {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        final Long clientId = clientResponse.getClientId();

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

        final PostLoansResponse response = ok(
                () -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        eventCheckHelper.createLoanEventCheck(response);
    }

    private void performLoanDisbursementAndVerifyStatus(final long loanId, final PostLoansLoanIdRequest disburseRequest)
            throws IOException {
        final PostLoansLoanIdResponse loanDisburseResponse = ok(
                () -> fineractClient.loans().stateTransitions(loanId, disburseRequest, Map.of("command", "disburse")));
        testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, loanDisburseResponse);
        assertNotNull(loanDisburseResponse);
        assertNotNull(loanDisburseResponse.getChanges());
        assertNotNull(loanDisburseResponse.getChanges().getStatus());
        final Long statusActual = loanDisburseResponse.getChanges().getStatus().getId();
        assertNotNull(statusActual);

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getStatus());
        final Long statusExpected = Long.valueOf(loanDetails.getStatus().getId());

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

    private List<String> fetchValuesOfBuyDownFees(List<String> header, BuyDownFeeAmortizationDetails t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Date" -> actualValues.add(t.getBuyDownFeeDate() == null ? null : FORMATTER.format(t.getBuyDownFeeDate()));
                case "Fee Amount" -> actualValues
                        .add(t.getBuyDownFeeAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getBuyDownFeeAmount().doubleValue()).format());
                case "Amortized Amount" -> actualValues
                        .add(t.getAmortizedAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getAmortizedAmount().doubleValue()).format());
                case "Not Yet Amortized Amount" -> actualValues
                        .add(t.getNotYetAmortizedAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getNotYetAmortizedAmount().doubleValue()).format());
                case "Adjusted Amount" ->
                    actualValues.add(t.getAdjustedAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                            : new Utils.DoubleFormatter(t.getAdjustedAmount().doubleValue()).format());
                case "Charged Off Amount" -> actualValues
                        .add(t.getChargedOffAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getChargedOffAmount().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private List<String> fetchValuesOfCapitalizedIncome(List<String> header, CapitalizedIncomeDetails t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Amount" ->
                    actualValues.add(t.getAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                            : new Utils.DoubleFormatter(t.getAmount().doubleValue()).format());
                case "Amortized Amount" -> actualValues
                        .add(t.getAmortizedAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getAmortizedAmount().doubleValue()).format());
                case "Unrecognized Amount" -> actualValues
                        .add(t.getUnrecognizedAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getUnrecognizedAmount().doubleValue()).format());
                case "Adjusted Amount" -> actualValues
                        .add(t.getAmountAdjustment() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getAmountAdjustment().doubleValue()).format());
                case "Charged Off Amount" -> actualValues
                        .add(t.getChargedOffAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                                : new Utils.DoubleFormatter(t.getChargedOffAmount().doubleValue()).format());
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
    public void transactionsExcluded(String excludedTypes) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        GetLoansLoanIdTransactionsResponse transactionsByLoanIdFiltered = getTransactionsByLoanIdFiltered(loanId, excludedTypes);
        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanIdFiltered.getContent();
        log.debug("Filtered transactions: {}", transactions);

        List<String> excludedTypesList = Arrays.stream(excludedTypes.toLowerCase(Locale.ROOT).split(",")).map(String::trim)
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
    public void transactionsExcludedByExternalId(String excludedTypes) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.getResourceExternalId();
        GetLoansLoanIdTransactionsResponse transactionsByLoanExternalIdFiltered = getTransactionsByLoanIExternalIdFiltered(loanExternalId,
                excludedTypes);
        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanExternalIdFiltered.getContent();
        log.debug("Filtered transactions: {}", transactions);

        List<String> excludedTypesList = Arrays.stream(excludedTypes.toLowerCase(Locale.ROOT).split(",")).map(String::trim)
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
    public void transactionsExcludedCheck(String excludedTypes, DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdTransactionsResponse transactionsByLoanIdFiltered = getTransactionsByLoanIdFiltered(loanId, excludedTypes);
        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanIdFiltered.getContent();
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
    public void transactionsExcludedByLoanExternalIdCheck(String excludedTypes, DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.getResourceExternalId();
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetLoansLoanIdTransactionsResponse transactionsByLoanExternalIdFiltered = getTransactionsByLoanIExternalIdFiltered(loanExternalId,
                excludedTypes);
        List<GetLoansLoanIdTransactionsTransactionIdResponse> transactions = transactionsByLoanExternalIdFiltered.getContent();
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

    private GetLoansLoanIdTransactionsResponse getTransactionsByLoanIdFiltered(Long loanId, String excludedTypes) {
        Map<String, Object> queryParams = new HashMap<>();
        List<org.apache.fineract.client.models.TransactionType> excludedTypesList = parseExcludedTypes(excludedTypes);
        if (excludedTypesList != null && !excludedTypesList.isEmpty()) {
            queryParams.put("excludedTypes", excludedTypesList);
        }
        return ok(() -> fineractClient.loanTransactions().retrieveTransactionsByLoanId(loanId, queryParams));
    }

    private GetLoansLoanIdTransactionsResponse getTransactionsByLoanIExternalIdFiltered(String loanExternalId, String excludedTypes) {
        Map<String, Object> queryParams = new HashMap<>();
        List<org.apache.fineract.client.models.TransactionType> excludedTypesList = parseExcludedTypes(excludedTypes);
        if (excludedTypesList != null && !excludedTypesList.isEmpty()) {
            queryParams.put("excludedTypes", excludedTypesList);
        }
        return ok(() -> fineractClient.loanTransactions().retrieveTransactionsByExternalLoanId(loanExternalId, queryParams));
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
    public void checkPagination(Integer totalPagesExpected, Integer size, String excludedTypes) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        Map<String, Object> queryParams = new HashMap<>();
        List<org.apache.fineract.client.models.TransactionType> excludedTypesList = parseExcludedTypes(excludedTypes);
        if (excludedTypesList != null && !excludedTypesList.isEmpty()) {
            queryParams.put("excludedTypes", excludedTypesList);
        }
        if (size != null) {
            queryParams.put("size", size);
        }
        GetLoansLoanIdTransactionsResponse transactionsByLoanIdFiltered = ok(
                () -> fineractClient.loanTransactions().retrieveTransactionsByLoanId(loanId, queryParams));

        Integer totalPagesActual = transactionsByLoanIdFiltered.getTotalPages();

        assertThat(totalPagesActual).as(ErrorMessageHelper.wrongValueInTotalPages(totalPagesActual, totalPagesExpected))
                .isEqualTo(totalPagesExpected);
    }

    @Then("Loan Product response contains interestRecognitionOnDisbursementDate flag with value {string}")
    public void verifyInterestRecognitionOnDisbursementDateFlag(final String expectedValue) {
        GetLoanProductsResponse targetProduct = getLoanProductResponse();

        assertNotNull(targetProduct.getInterestRecognitionOnDisbursementDate());
        assertThat(targetProduct.getInterestRecognitionOnDisbursementDate().toString()).isEqualTo(expectedValue);
    }

    public GetLoanProductsResponse getLoanProductResponse() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final Long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        assertNotNull(loanDetails);

        final Long targetLoanProductId = loanDetails.getLoanProductId();

        final List<GetLoanProductsResponse> allProductsResponse = ok(() -> fineractClient.loanProducts().retrieveAllLoanProducts(Map.of()));
        assertNotNull(allProductsResponse);
        final List<GetLoanProductsResponse> loanProducts = allProductsResponse;
        assertThat(loanProducts).isNotEmpty();

        final GetLoanProductsResponse targetProduct = loanProducts.stream().filter(product -> {
            assertNotNull(product.getId());
            return product.getId().equals(targetLoanProductId);
        }).findFirst().orElseThrow(() -> new AssertionError("Loan product with ID " + targetLoanProductId + " not found in response"));

        return targetProduct;
    }

    @Then("Loan Product response contains Buy Down Fees flag {string} with data:")
    public void verifyLoanProductWithBuyDownFeesData(String expectedValue, DataTable table) {
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
    public void verifyLoanProductWithBuyDownFeesFlag(String expectedValue) {
        GetLoanProductsResponse targetProduct = getLoanProductResponse();

        assertNotNull(targetProduct.getEnableBuyDownFee());
        assertThat(targetProduct.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);
    }

    public GetLoansLoanIdResponse getLoanDetailsResponse() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);

        long loanId = loanResponse.getLoanId();

        Optional<GetLoansLoanIdResponse> loanDetailsResponseOptional = Optional
                .of(fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        GetLoansLoanIdResponse loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        return loanDetailsResponse;
    }

    @Then("Loan Details response contains Buy Down Fees flag {string} and data:")
    public void verifyBuyDownFeeDataInLoanResponse(final String expectedValue, DataTable table) {
        GetLoansLoanIdResponse loanDetailsResponse = getLoanDetailsResponse();
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        GetLoansLoanIdResponse loanDetails = loanDetailsResponse;

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
    public void verifyBuyDownFeeFlagInLoanResponse(final String expectedValue) {
        GetLoansLoanIdResponse loanDetailsResponse = getLoanDetailsResponse();
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        GetLoansLoanIdResponse loanDetails = loanDetailsResponse;

        assertNotNull(loanDetails.getEnableBuyDownFee());
        assertThat(loanDetails.getEnableBuyDownFee().toString()).isEqualTo(expectedValue);
    }

    @Then("Loan Details response contains chargedOffOnDate set to {string}")
    public void verifyChargedOffOnDateFlagInLoanResponse(final String expectedValue) {
        PostLoansLoanIdTransactionsResponse loanResponse = testContext().get(TestContextKey.LOAN_CHARGE_OFF_RESPONSE);

        long loanId = loanResponse.getLoanId();

        Optional<GetLoansLoanIdResponse> loanDetailsResponseOptional = Optional
                .of(fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        GetLoansLoanIdResponse loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.getTimeline().getChargedOffOnDate()).isEqualTo(LocalDate.parse(expectedValue, FORMATTER));
    }

    @Then("Loan Details response does not contain chargedOff flag and chargedOffOnDate field after repayment and reverted charge off")
    public void verifyChargedOffOnDateFlagIsNotPresentLoanResponse() {
        PostLoansLoanIdTransactionsResponse loanResponse = testContext().get(TestContextKey.LOAN_REPAYMENT_RESPONSE);

        long loanId = loanResponse.getLoanId();

        Optional<GetLoansLoanIdResponse> loanDetailsResponseOptional = Optional
                .of(fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        GetLoansLoanIdResponse loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.getTimeline().getChargedOffOnDate()).isNull();
        assertThat(loanDetailsResponse.getChargedOff()).isFalse();
    }

    @Then("Loan Details response contains chargedOff flag set to {booleanValue}")
    public void verifyChargeOffFlagInLoanResponse(final Boolean expectedValue) {
        PostLoansLoanIdTransactionsResponse loanResponse = expectedValue ? testContext().get(TestContextKey.LOAN_CHARGE_OFF_RESPONSE)
                : testContext().get(TestContextKey.LOAN_CHARGE_OFF_UNDO_RESPONSE);

        long loanId = loanResponse.getLoanId();

        Optional<GetLoansLoanIdResponse> loanDetailsResponseOptional = Optional
                .of(fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        GetLoansLoanIdResponse loanDetailsResponse = loanDetailsResponseOptional
                .orElseThrow(() -> new RuntimeException("Failed to retrieve loan details - response is null"));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        assertThat(loanDetailsResponse.getChargedOff()).isEqualTo(expectedValue);
    }

    @ParameterType(value = "true|True|TRUE|false|False|FALSE")
    public Boolean booleanValue(String value) {
        return Boolean.valueOf(value);
    }

    public PostLoansLoanIdTransactionsResponse addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID());

        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction(loanId, capitalizedIncomeRequest, Map.of("command", "capitalizedIncome")));
        return capitalizedIncomeResponse;
    }

    public PostLoansLoanIdTransactionsResponse addCapitalizedIncomeToTheLoanOnWithEURTransactionAmountWithClassificationScheduledPayment(
            final String transactionPaymentType, final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID()).classificationId(24L);

        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction(loanId, capitalizedIncomeRequest, Map.of("command", "capitalizedIncome")));
        return capitalizedIncomeResponse;
    }

    @And("Admin adds capitalized income with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = addCapitalizedIncomeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_RESPONSE, capitalizedIncomeResponse);
    }

    @And("Admin adds capitalized income with {string} payment type to the loan on {string} with {string} EUR transaction amount and classification: scheduled_payment")
    public void adminAddsCapitalizedIncomeToTheLoanOnWithEURTransactionAmountWithClassificationScheduledPayment(
            final String transactionPaymentType, final String transactionDate, final String amount) {
        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = addCapitalizedIncomeToTheLoanOnWithEURTransactionAmountWithClassificationScheduledPayment(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_RESPONSE, capitalizedIncomeResponse);
    }

    @And("Admin adds capitalized income with {string} payment type to the loan on {string} with {string} EUR transaction amount and {string} classification")
    public void adminAddsCapitalizedIncomeWithClassification(final String transactionPaymentType, final String transactionDate,
            final String amount, final String classificationCodeName) {
        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = addCapitalizedIncomeWithClassification(transactionPaymentType,
                transactionDate, amount, classificationCodeName);
        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_RESPONSE, capitalizedIncomeResponse);
    }

    public PostLoansLoanIdTransactionsResponse addCapitalizedIncomeWithClassification(final String transactionPaymentType,
            final String transactionDate, final String amount, final String classificationCodeName) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        // Get classification code value
        final Long classificationId = getClassificationCodeValueId(classificationCodeName);

        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID()).classificationId(classificationId);

        final PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = ok(() -> fineractClient.loanTransactions()
                .executeLoanTransaction(loanId, capitalizedIncomeRequest, Map.of("command", "capitalizedIncome")));
        return capitalizedIncomeResponse;
    }

    public PostLoansLoanIdTransactionsResponse adjustCapitalizedIncome(final String transactionPaymentType, final String transactionDate,
            final String amount, final Long transactionId) {

        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest capitalizedIncomeRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en").transactionAmount(Double.valueOf(amount))
                .paymentTypeId(paymentTypeValue).externalId("EXT-CAP-INC-ADJ-" + UUID.randomUUID());

        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, capitalizedIncomeRequest,
                Map.of("command", "capitalizedIncomeAdjustment")));
    }

    @Then("Capitalized income with payment type {string} on {string} is forbidden with amount {string} while exceed approved amount")
    public void capitalizedIncomeForbiddenExceedApprovedAmount(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);
        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID());

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                capitalizedIncomeRequest, Map.of("command", "capitalizedIncome")));

        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addCapitalizedIncomeExceedApprovedAmountFailure());
    }

    @Then("Capitalized income with payment type {string} on {string} is forbidden with amount {string} due to future date")
    public void capitalizedIncomeForbiddenFutureDate(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);
        final PostLoansLoanIdTransactionsRequest capitalizedIncomeRequest = LoanRequestFactory.defaultCapitalizedIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-CAP-INC-" + UUID.randomUUID());

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                capitalizedIncomeRequest, Map.of("command", "capitalizedIncome")));

        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure());
    }

    @Then("LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income Amortization".equals(t.getType().getValue()))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No Capitalized Income Amortization transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent.class,
                finalAmortizationTransactionId);
    }

    @Then("LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
    public void checkLoanCapitalizedIncomeTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions finalAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Capitalized Income transaction found on %s", date)));
        Long finalAmortizationTransactionId = finalAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeTransactionCreatedBusinessEvent.class, finalAmortizationTransactionId);
    }

    @Then("LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent is raised on {string}")
    public void checkLoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
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
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        // Get current business date to ensure we're not creating backdated transactions
        String currentBusinessDate = businessDateHelper.getBusinessDate();
        log.debug("Current business date: {}, Transaction date: {}", currentBusinessDate, transactionDate);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType, transactionDate,
                amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        log.debug("Capitalized Income Adjustment created: Transaction ID {}", adjustmentResponse.getResourceId());
    }

    @And("Admin adds capitalized income adjustment of capitalized income transaction made on {string} with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsCapitalizedIncomeAdjustmentToTheLoan(final String originalTransactionDate, final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        // Get current business date to ensure we're not creating backdated transactions
        String currentBusinessDate = businessDateHelper.getBusinessDate();
        log.debug("Current business date: {}, Transaction date: {}", currentBusinessDate, transactionDate);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream().filter(t -> {
            assert t.getType() != null;
            if (!"Capitalized Income".equals(t.getType().getValue())) {
                return false;
            }
            assert t.getDate() != null;
            return FORMATTER.format(t.getDate()).equals(originalTransactionDate);
        }).findFirst().orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType, transactionDate,
                amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        assert adjustmentResponse != null;
        log.debug("Capitalized Income Adjustment created: Transaction ID {}", adjustmentResponse.getResourceId());
    }

    @Then("Loan's available disbursement amount is {string}")
    public void verifyAvailableDisbursementAmount(String expectedAmount) {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "collection")));

        // Extract availableDisbursementAmount from collection data
        BigDecimal availableDisbursementAmount = loanDetailsResponse.getDelinquent().getAvailableDisbursementAmount();

        assertThat(availableDisbursementAmount).as("Available disbursement amount should be " + expectedAmount)
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }

    @And("Admin adds capitalized income adjustment with {string} payment type to the loan on {string} with {string} EUR trn amount with {string} date for capitalized income")
    public void adminAddsCapitalizedIncomeAdjustmentToTheLoanWithCapitalizedIncomeDate(final String transactionPaymentType,
            final String transactionDate, final String amount, final String capitalizedIncomeTrnsDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue()))
                .filter(t -> FORMATTER.format(t.getDate()).equals(capitalizedIncomeTrnsDate)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = adjustCapitalizedIncome(transactionPaymentType, transactionDate,
                amount, capitalizedIncomeTransaction.getId());

        testContext().set(TestContextKey.LOAN_CAPITALIZED_INCOME_ADJUSTMENT_RESPONSE, adjustmentResponse);
        log.debug("Capitalized Income Adjustment created: Transaction ID {}", adjustmentResponse.getResourceId());
    }

    @And("Admin adds invalid capitalized income adjustment with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsArbitraryCapitalizedIncomeAdjustmentToTheLoan(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        // Get current business date to ensure we're not creating backdated transactions
        String currentBusinessDate = businessDateHelper.getBusinessDate();
        log.debug("Current business date: {}, Transaction date: {}", currentBusinessDate, transactionDate);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest capitalizedIncomeRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en").transactionAmount(Double.valueOf(amount))
                .paymentTypeId(paymentTypeValue).externalId("EXT-CAP-INC-ADJ-" + UUID.randomUUID());

        // This step expects the call to fail with validation error
        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                capitalizedIncomeTransaction.getId(), capitalizedIncomeRequest, Map.of("command", "capitalizedIncomeAdjustment")));

        assertThat(exception.getStatus()).isEqualTo(400);
        // Validation error - just verify it's a 400 status code, the specific message varies
    }

    public void checkCapitalizedIncomeTransactionData(String resourceId, List<CapitalizedIncomeDetails> capitalizedIncomeTrn,
            DataTable table) {
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String capitalizedIncomeAmountExpected = expectedValues.get(0);
            List<List<String>> actualValuesList = capitalizedIncomeTrn.stream()//
                    .filter(t -> new BigDecimal(capitalizedIncomeAmountExpected).compareTo(t.getAmount()) == 0)//
                    .map(t -> fetchValuesOfCapitalizedIncome(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInDeferredIncomeTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
        assertThat(capitalizedIncomeTrn.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInDeferredIncomeTab(resourceId, capitalizedIncomeTrn.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    // TODO: Re-enable after loanCapitalizedIncomeApi is migrated to Feign
    // @And("Deferred Capitalized Income contains the following data:")
    // public void checkCapitalizedIncomeData(DataTable table) {
    // PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
    // long loanId = loanCreateResponse.getLoanId();
    // String resourceId = String.valueOf(loanId);
    //
    // final List<CapitalizedIncomeDetails> capitalizeIncomeDetails =
    // loanCapitalizedIncomeApi.fetchCapitalizedIncomeDetails(loanId);
    // checkCapitalizedIncomeTransactionData(resourceId, capitalizeIncomeDetails, table);
    // }

    // TODO: Re-enable after loanCapitalizedIncomeApi is migrated to Feign
    // @And("Deferred Capitalized Income by external-id contains the following data:")
    // public void checkCapitalizedIncomeByExternalIdData(DataTable table) {
    // PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
    // long loanId = loanCreateResponse.getLoanId();
    // String resourceId = String.valueOf(loanId);
    // String externalId = loanCreateResponse.getResourceExternalId();
    //
    // final List<CapitalizedIncomeDetails> capitalizeIncomeDetails = loanCapitalizedIncomeApi
    // .fetchCapitalizedIncomeDetailsByExternalId(externalId);
    // checkCapitalizedIncomeTransactionData(resourceId, capitalizeIncomeDetails, table);
    // }

    @And("Admin successfully terminates loan contract")
    public void makeLoanContractTermination() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final PostLoansLoanIdRequest contractTerminationRequest = LoanRequestFactory.defaultLoanContractTerminationRequest();

        final PostLoansLoanIdResponse loanContractTerminationResponse = ok(() -> fineractClient.loans().stateTransitions(loanId,
                contractTerminationRequest, Map.of("command", "contractTermination")));
        testContext().set(TestContextKey.LOAN_CONTRACT_TERMINATION_RESPONSE, loanContractTerminationResponse);
        assert loanContractTerminationResponse != null;
        final Long transactionId = loanContractTerminationResponse.getResourceId();
        eventAssertion.assertEvent(LoanTransactionContractTerminationPostBusinessEvent.class, transactionId)
                .extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(loanId).extractingData(LoanTransactionDataV1::getId)
                .isEqualTo(transactionId);
    }

    @And("Admin successfully terminates loan contract - no event check")
    public void makeLoanContractTerminationNoEventCheck() throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final PostLoansLoanIdRequest contractTerminationRequest = LoanRequestFactory.defaultLoanContractTerminationRequest();

        final PostLoansLoanIdResponse loanContractTerminationResponse = ok(() -> fineractClient.loans().stateTransitions(loanId,
                contractTerminationRequest, Map.of("command", "contractTermination")));
        testContext().set(TestContextKey.LOAN_CONTRACT_TERMINATION_RESPONSE, loanContractTerminationResponse);
    }

    @And("Admin successfully undoes loan contract termination")
    public void undoLoanContractTermination() throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final PostLoansLoanIdResponse loanContractTerminationResponse = testContext()
                .get(TestContextKey.LOAN_CONTRACT_TERMINATION_RESPONSE);
        assert loanContractTerminationResponse != null;
        final Long loanId = loanResponse.getLoanId();

        final List<GetLoansLoanIdTransactions> transactions = Objects.requireNonNull(
                fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")))
                .getTransactions();

        assert transactions != null;
        final GetLoansLoanIdTransactions targetTransaction = transactions.stream().filter(t -> {
            assert t.getType() != null;
            return Boolean.TRUE.equals(t.getType().getContractTermination());
        }).findFirst().orElse(null);

        final PostLoansLoanIdRequest request = LoanRequestFactory.defaultContractTerminationUndoRequest();

        final PostLoansLoanIdResponse response = ok(
                () -> fineractClient.loans().stateTransitions(loanId, request, Map.of("command", "undoContractTermination")));
        testContext().set(TestContextKey.LOAN_UNDO_CONTRACT_TERMINATION_RESPONSE, response);
        assert targetTransaction != null;
        eventCheckHelper.checkTransactionWithLoanTransactionAdjustmentBizEvent(targetTransaction);
        eventCheckHelper.loanUndoContractTerminationEventCheck(targetTransaction);
        eventCheckHelper.loanBalanceChangedEventCheck(loanId);
    }

    @Then("LoanTransactionContractTerminationPostBusinessEvent is raised on {string}")
    public void checkLoanTransactionContractTerminationPostBusinessEvent(final String date) {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions loanContractTerminationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Contract Termination".equals(t.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Contract Termination transaction found on %s", date)));
        final Long loanContractTerminationTransactionId = loanContractTerminationTransaction.getId();

        eventAssertion.assertEventRaised(LoanTransactionContractTerminationPostBusinessEvent.class, loanContractTerminationTransactionId);
    }

    @Then("Capitalized income adjustment with payment type {string} on {string} is forbidden with amount {string} due to future date")
    public void capitalizedIncomeAdjustmentForbiddenFutureDate(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions capitalizedIncomeTransaction = transactions.stream()
                .filter(t -> "Capitalized Income".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Capitalized Income transaction found for loan " + loanId));

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest capitalizedIncomeRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en").transactionAmount(Double.valueOf(amount))
                .paymentTypeId(paymentTypeValue).externalId("EXT-CAP-INC-ADJ-" + UUID.randomUUID());

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                capitalizedIncomeTransaction.getId(), capitalizedIncomeRequest, Map.of("command", "capitalizedIncomeAdjustment")));

        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.addCapitalizedIncomeFutureDateFailure());
    }

    public PostLoansLoanIdTransactionsResponse addBuyDownFeeToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest buyDownFeeRequest = LoanRequestFactory.defaultBuyDownFeeIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-BUY-DOWN-FEE" + UUID.randomUUID());

        final PostLoansLoanIdTransactionsResponse buyDownFeeResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, buyDownFeeRequest, Map.of("command", "buyDownFee")));
        return buyDownFeeResponse;
    }

    public PostLoansLoanIdTransactionsResponse addBuyDownFeeToTheLoanOnWithEURTransactionAmountWithClassification(
            final String transactionPaymentType, final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsRequest buyDownFeeRequest = LoanRequestFactory.defaultBuyDownFeeIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-BUY-DOWN-FEE" + UUID.randomUUID()).classificationId(25L);

        final PostLoansLoanIdTransactionsResponse buyDownFeeResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, buyDownFeeRequest, Map.of("command", "buyDownFee")));
        return buyDownFeeResponse;
    }

    public PostLoansLoanIdTransactionsResponse adjustBuyDownFee(final String transactionPaymentType, final String transactionDate,
            final String amount, final Long transactionId) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest buyDownFeeRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en").transactionAmount(Double.valueOf(amount))
                .paymentTypeId(paymentTypeValue).externalId("EXT-BUY-DOWN-FEE-ADJ-" + UUID.randomUUID());

        // Use adjustLoanTransaction with the transaction ID and command
        final PostLoansLoanIdTransactionsResponse buyDownFeeResponse = ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, transactionId, buyDownFeeRequest, Map.of("command", "buyDownFeeAdjustment")));

        return buyDownFeeResponse;
    }

    @And("Admin adds buy down fee with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsBuyDownFeesToTheLoanOnWithEURTransactionAmount(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansLoanIdTransactionsResponse buyDownFeesIncomeResponse = addBuyDownFeeToTheLoanOnWithEURTransactionAmount(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_RESPONSE, buyDownFeesIncomeResponse);
    }

    @And("Admin adds buy down fee with {string} payment type to the loan on {string} with {string} EUR transaction amount and classification: pending_bankruptcy")
    public void adminAddsBuyDownFeesToTheLoanOnWithEURTransactionAmountWithClassification(final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansLoanIdTransactionsResponse buyDownFeesIncomeResponse = addBuyDownFeeToTheLoanOnWithEURTransactionAmountWithClassification(
                transactionPaymentType, transactionDate, amount);
        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_RESPONSE, buyDownFeesIncomeResponse);
    }

    @When("Admin adds buy down fee with {string} payment type to the loan on {string} with {string} EUR transaction amount and {string} classification")
    public void adminAddsBuyDownFeeWithClassification(final String transactionPaymentType, final String transactionDate,
            final String amount, final String classificationCodeName) {
        final PostLoansLoanIdTransactionsResponse buyDownFeesIncomeResponse = addBuyDownFeeWithClassification(transactionPaymentType,
                transactionDate, amount, classificationCodeName);
        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_RESPONSE, buyDownFeesIncomeResponse);
    }

    public PostLoansLoanIdTransactionsResponse addBuyDownFeeWithClassification(final String transactionPaymentType,
            final String transactionDate, final String amount, final String classificationCodeName) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(transactionPaymentType);
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        // Get classification code value
        final Long classificationId = getClassificationCodeValueId(classificationCodeName);

        final PostLoansLoanIdTransactionsRequest buyDownFeeRequest = LoanRequestFactory.defaultBuyDownFeeIncomeRequest()
                .transactionDate(transactionDate).transactionAmount(Double.valueOf(amount)).paymentTypeId(paymentTypeValue)
                .externalId("EXT-BUY-DOWN-FEE" + UUID.randomUUID()).classificationId(classificationId);

        final PostLoansLoanIdTransactionsResponse buyDownFeeResponse = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, buyDownFeeRequest, Map.of("command", "buyDownFee")));
        return buyDownFeeResponse;
    }

    @And("Admin adds buy down fee adjustment with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsBuyDownFeesAdjustmentToTheLoan(final String transactionPaymentType, final String transactionDate,
            final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions buyDownFeeTransaction = transactions.stream()
                .filter(t -> "Buy Down Fee".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No Buy Down Fee transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = adjustBuyDownFee(transactionPaymentType, transactionDate, amount,
                buyDownFeeTransaction.getId());

        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_ADJUSTMENT_RESPONSE, adjustmentResponse);
        log.debug("BuyDown Fee Adjustment created: Transaction ID {}", adjustmentResponse.getResourceId());
    }

    @And("Admin adds buy down fee adjustment of buy down fee transaction made on {string} with {string} payment type to the loan on {string} with {string} EUR transaction amount")
    public void adminAddsBuyDownFeesAdjustmentToTheLoan(final String originalTransactionDate, final String transactionPaymentType,
            final String transactionDate, final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final GetLoansLoanIdTransactions buyDownFeeTransaction = transactions.stream().filter(t -> {
            assert t.getType() != null;
            if (!"Buy Down Fee".equals(t.getType().getValue())) {
                return false;
            }
            assert t.getDate() != null;
            return FORMATTER.format(t.getDate()).equals(originalTransactionDate);
        }).findFirst().orElseThrow(() -> new IllegalStateException("No Buy Down Fee transaction found for loan " + loanId));

        final PostLoansLoanIdTransactionsResponse adjustmentResponse = adjustBuyDownFee(transactionPaymentType, transactionDate, amount,
                buyDownFeeTransaction.getId());

        testContext().set(TestContextKey.LOAN_BUY_DOWN_FEE_ADJUSTMENT_RESPONSE, adjustmentResponse);
        log.debug("BuyDown Fee Adjustment created: Transaction ID {}", adjustmentResponse.getResourceId());
    }

    @And("Buy down fee contains the following data:")
    public void checkBuyDownFeeData(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        final List<BuyDownFeeAmortizationDetails> buyDownFees = ok(
                () -> fineractClient.loanBuyDownFees().retrieveLoanBuyDownFeeAmortizationDetails(loanId));
        checkBuyDownFeeTransactionData(resourceId, buyDownFees, table);
    }

    @And("Buy down fee by external-id contains the following data:")
    public void checkBuyDownFeeByExternalIdData(DataTable table) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);
        String externalId = loanCreateResponse.getResourceExternalId();

        final List<BuyDownFeeAmortizationDetails> buyDownFees = ok(
                () -> fineractClient.loanBuyDownFees().retrieveLoanBuyDownFeeAmortizationDetailsByExternalId(externalId));
        checkBuyDownFeeTransactionData(resourceId, buyDownFees, table);
    }

    public void checkBuyDownFeeTransactionData(String resourceId, List<BuyDownFeeAmortizationDetails> buyDownFees, DataTable table) {
        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String buyDownFeeDateExpected = expectedValues.get(0);
            List<List<String>> actualValuesList = buyDownFees.stream()//
                    .filter(t -> buyDownFeeDateExpected.equals(FORMATTER.format(t.getBuyDownFeeDate())))//
                    .map(t -> fetchValuesOfBuyDownFees(table.row(0), t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInBuyDownFeeTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
        assertThat(buyDownFees.size()).as(ErrorMessageHelper.nrOfLinesWrongInBuyDownFeeTab(resourceId, buyDownFees.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    @Then("Update loan approved amount with new amount {string} value")
    public void updateLoanApprovedAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final PutLoansApprovedAmountRequest modifyLoanApprovedAmountRequest = new PutLoansApprovedAmountRequest().locale(LOCALE_EN)
                .amount(new BigDecimal(amount));

        ok(() -> fineractClient.loans().modifyLoanApprovedAmount(loanId, modifyLoanApprovedAmountRequest));
    }

    @Then("Update loan approved amount is forbidden with amount {string} due to exceed applied amount")
    public void updateLoanApprovedAmountForbiddenExceedAppliedAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final PutLoansApprovedAmountRequest modifyLoanApprovedAmountRequest = new PutLoansApprovedAmountRequest().locale(LOCALE_EN)
                .amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanApprovedAmount(loanId, modifyLoanApprovedAmountRequest));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Update loan approved amount is forbidden with amount {string} due to higher principal amount on loan")
    public void updateLoanApprovedAmountForbiddenHigherPrincipalAmountOnLoan(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final PutLoansApprovedAmountRequest modifyLoanApprovedAmountRequest = new PutLoansApprovedAmountRequest().locale(LOCALE_EN)
                .amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanApprovedAmount(loanId, modifyLoanApprovedAmountRequest));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Update loan approved amount is forbidden with amount {string} due to min allowed amount")
    public void updateLoanApprovedAmountForbiddenMinAllowedAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final PutLoansApprovedAmountRequest modifyLoanApprovedAmountRequest = new PutLoansApprovedAmountRequest().locale(LOCALE_EN)
                .amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanApprovedAmount(loanId, modifyLoanApprovedAmountRequest));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    @Then("Update loan available disbursement amount with new amount {string} value")
    public void updateLoanAvailableDisbursementAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final PutLoansAvailableDisbursementAmountRequest modifyLoanAvailableDisbursementAmountRequest = new PutLoansAvailableDisbursementAmountRequest()
                .locale(LOCALE_EN).amount(new BigDecimal(amount));

        ok(() -> fineractClient.loans().modifyLoanAvailableDisbursementAmount(loanId, modifyLoanAvailableDisbursementAmountRequest));
    }

    @Then("Update loan available disbursement amount by external-id with new amount {string} value")
    public void updateLoanAvailableDisbursementAmountByExternalId(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final String externalId = loanResponse.getResourceExternalId();
        final PutLoansAvailableDisbursementAmountRequest modifyLoanAvailableDisbursementAmountRequest = new PutLoansAvailableDisbursementAmountRequest()
                .locale(LOCALE_EN).amount(new BigDecimal(amount));

        ok(() -> fineractClient.loans().modifyLoanAvailableDisbursementAmount1(externalId, modifyLoanAvailableDisbursementAmountRequest));
    }

    @Then("Update loan available disbursement amount is forbidden with amount {string} due to exceed applied amount")
    public void updateLoanAvailableDisbursementAmountForbiddenExceedAppliedAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final String externalId = loanResponse.getResourceExternalId();
        final PutLoansAvailableDisbursementAmountRequest modifyLoanAvailableDisbursementAmountRequest = new PutLoansAvailableDisbursementAmountRequest()
                .locale(LOCALE_EN).amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.loans().modifyLoanAvailableDisbursementAmount1(externalId,
                modifyLoanAvailableDisbursementAmountRequest));

        assertThat(exception.getStatus()).isEqualTo(403);
        // API returns generic validation error - ideally should contain specific message about exceeding amount
        assertThat(exception.getDeveloperMessage()).containsAnyOf("can't.be.greater.than.maximum.available.disbursement.amount.calculation",
                "Validation errors");
    }

    @Then("Update loan available disbursement amount is forbidden with amount {string} due to min allowed amount")
    public void updateLoanAvailableDisbursementAmountForbiddenMinAllowedAmount(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final PutLoansAvailableDisbursementAmountRequest modifyLoanAvailableDisbursementAmountRequest = new PutLoansAvailableDisbursementAmountRequest()
                .locale(LOCALE_EN).amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanAvailableDisbursementAmount(loanId, modifyLoanAvailableDisbursementAmountRequest));

        assertThat(exception.getStatus()).isEqualTo(403);
        // API returns generic validation error - ideally should contain specific message about min amount
        assertThat(exception.getDeveloperMessage()).containsAnyOf("must be greater than or equal to 0", "Validation errors");
    }

    @Then("Updating the loan's available disbursement amount to {string} is forbidden because cannot be zero as nothing was disbursed")
    public void updateLoanAvailableDisbursementAmountForbiddenCannotBeZeroAsNothingWasDisbursed(final String amount) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanResponse.getLoanId();
        final PutLoansAvailableDisbursementAmountRequest modifyLoanAvailableDisbursementAmountRequest = new PutLoansAvailableDisbursementAmountRequest()
                .locale(LOCALE_EN).amount(new BigDecimal(amount));

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.loans().modifyLoanAvailableDisbursementAmount(loanId, modifyLoanAvailableDisbursementAmountRequest));

        assertThat(exception.getStatus()).isEqualTo(403);
        // API returns generic validation error - ideally should contain specific message about zero amount
        assertThat(exception.getDeveloperMessage()).containsAnyOf("cannot.be.zero.as.nothing.was.disbursed.yet", "Validation errors");
    }

    private PostLoansLoanIdTransactionsResponse addInterestRefundTransaction(final double amount, final Long transactionId) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.AUTOPAY;
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest interestRefundRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(amount).paymentTypeId(paymentTypeValue)
                .externalId("EXT-INT-REF-" + UUID.randomUUID()).note("");

        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, interestRefundRequest,
                Map.of("command", "interest-refund")));
    }

    private CallFailedRuntimeException failAddInterestRefundTransaction(final double amount, final Long transactionId,
            final String transactionDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse != null;
        final long loanId = loanResponse.getLoanId();

        final DefaultPaymentType paymentType = DefaultPaymentType.AUTOPAY;
        final Long paymentTypeValue = paymentTypeResolver.resolve(paymentType);

        final PostLoansLoanIdTransactionsTransactionIdRequest interestRefundRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(amount).paymentTypeId(paymentTypeValue)
                .externalId("EXT-INT-REF-" + UUID.randomUUID()).note("");

        if (transactionDate != null) {
            interestRefundRequest.transactionDate(transactionDate);
        }

        return fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, interestRefundRequest,
                Map.of("command", "interest-refund")));
    }

    @Then("LoanBuyDownFeeTransactionCreatedBusinessEvent is created on {string}")
    public void checkLoanBuyDownFeeTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions buyDownFeeTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Buy Down Fee".equals(t.getType().getValue())).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Buy Down Fee transaction found on %s", date)));
        Long buyDownFeeTransactionId = buyDownFeeTransaction.getId();

        eventAssertion.assertEventRaised(LoanBuyDownFeeTransactionCreatedBusinessEvent.class, buyDownFeeTransactionId);
    }

    @Then("LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent is created on {string}")
    public void checkLoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions buyDownFeeAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Buy Down Fee Amortization".equals(t.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Buy Down Fee Amortization transaction found on %s", date)));
        Long buyDownFeeAmortizationTransactionId = buyDownFeeAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent.class,
                buyDownFeeAmortizationTransactionId);
    }

    @Then("LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent is created on {string}")
    public void checkLoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions buyDownFeeAdjustmentTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Buy Down Fee Adjustment".equals(t.getType().getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("No Buy Down Fee Adjustment transaction found on %s", date)));
        Long buyDownFeeAdjustmentTransactionId = buyDownFeeAdjustmentTransaction.getId();

        eventAssertion.assertEventRaised(LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent.class, buyDownFeeAdjustmentTransactionId);
    }

    @Then("LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent is created on {string}")
    public void checkLoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent(final String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions buyDownFeeAmortizationAdjustmentTransaction = transactions.stream().filter(
                t -> date.equals(FORMATTER.format(t.getDate())) && "Buy Down Fee Amortization Adjustment".equals(t.getType().getValue()))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        String.format("No Buy Down Fee Amortization Adjustment transaction found on %s", date)));
        Long buyDownFeeAmortizationAdjustmentTransactionId = buyDownFeeAmortizationAdjustmentTransaction.getId();

        eventAssertion.assertEventRaised(LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent.class,
                buyDownFeeAmortizationAdjustmentTransactionId);
    }

    @And("Loan Transactions tab has a {string} transaction with date {string} which has classification code value {string}")
    public void loanTransactionHasClassification(String transactionType, String expectedDate, String expectedClassification) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions transaction = transactions.stream()
                .filter(t -> transactionType.equals(t.getType().getValue()) && expectedDate.equals(FORMATTER.format(t.getDate())))
                .findFirst().orElseThrow(
                        () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionType, expectedDate)));

        // Get detailed transaction information including classification
        GetLoansLoanIdTransactionsTransactionIdResponse transactionDetailsResponse = ok(
                () -> fineractClient.loanTransactions().retrieveTransaction(loanId, transaction.getId(), (String) null));
        GetLoansLoanIdTransactionsTransactionIdResponse transactionDetails = transactionDetailsResponse;
        assertThat(transactionDetails.getClassification()).as(String.format("%s transaction should have classification", transactionType))
                .isNotNull();
        assertThat(transactionDetails.getClassification().getName()).as("Classification name should match expected value")
                .isEqualTo(expectedClassification);
    }

    private Long getClassificationCodeValueId(String classificationName) {
        final GetCodesResponse code = codeHelper.retrieveCodeByName(classificationName);

        // Check if code value already exists
        List<GetCodeValuesDataResponse> existingCodeValues = fineractClient.codeValues().retrieveAllCodeValues(code.getId());
        String codeValueName = classificationName + "_value";

        // Try to find existing code value with the same name
        for (GetCodeValuesDataResponse codeValue : existingCodeValues) {
            if (codeValueName.equals(codeValue.getName())) {
                log.info("Reusing existing code value: {}", codeValueName);
                return codeValue.getId();
            }
        }

        // If not found, create a new code value
        PostCodeValuesDataRequest codeValueRequest = new PostCodeValuesDataRequest().name(codeValueName).isActive(true).position(1);

        PostCodeValueDataResponse response = codeHelper.createCodeValue(code.getId(), codeValueRequest);

        return response.getSubResourceId();
    }

    @And("Loan Amortization Allocation Mapping for {string} transaction created on {string} contains the following data:")
    public void checkLoanAmortizationAllocationMapping(final String transactionType, final String transactionDate, DataTable table) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();
        final String resourceId = String.valueOf(loanId);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final TransactionType transactionType1 = TransactionType.valueOf(transactionType);
        final String transactionTypeExpected = transactionType1.getValue();

        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final List<GetLoansLoanIdTransactions> transactionsMatch = transactions.stream().filter(t -> {
            assert t.getDate() != null;
            if (!transactionDate.equals(formatter.format(t.getDate()))) {
                return false;
            }
            assert t.getType() != null;
            assert t.getType().getCode() != null;
            return transactionTypeExpected.equals(t.getType().getCode().substring(20));
        }).toList();

        final LoanAmortizationAllocationResponse loanAmortizationAllocationResponse = transactionsMatch.getFirst().getType().getCode()
                .substring(20).equals(GetLoansLoanIdLoanTransactionEnumData.SERIALIZED_NAME_CAPITALIZED_INCOME)
                        ? ok(() -> fineractClient.loanCapitalizedIncome().retrieveCapitalizedIncomeAllocationData(loanId,
                                transactionsMatch.getFirst().getId()))
                        : ok(() -> fineractClient.loanBuyDownFees().retrieveBuyDownFeesAllocationData(loanId,
                                transactionsMatch.getFirst().getId()));
        checkLoanAmortizationAllocationMappingData(resourceId, loanAmortizationAllocationResponse, table);
    }

    @And("Loan Amortization Allocation Mapping for the {string}th {string} transaction created on {string} contains the following data:")
    public void checkLoanAmortizationAllocationMapping(final String nthTransactionStr, final String transactionType,
            final String transactionDate, DataTable table) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();
        final String resourceId = String.valueOf(loanId);

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final TransactionType transactionType1 = TransactionType.valueOf(transactionType);
        final String transactionTypeExpected = transactionType1.getValue();

        assert loanDetailsResponse != null;
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        assert transactions != null;
        final int nthTransaction = Integer.parseInt(nthTransactionStr) - 1;
        final GetLoansLoanIdTransactions transactionMatch = transactions.stream().filter(t -> {
            assert t.getDate() != null;
            if (!transactionDate.equals(formatter.format(t.getDate()))) {
                return false;
            }
            assert t.getType() != null;
            assert t.getType().getCode() != null;
            return transactionTypeExpected.equals(t.getType().getCode().substring(20));
        }).toList().get(nthTransaction);

        final LoanAmortizationAllocationResponse loanAmortizationAllocationResponse = transactionMatch.getType().getCode().substring(20)
                .equals(GetLoansLoanIdLoanTransactionEnumData.SERIALIZED_NAME_CAPITALIZED_INCOME)
                        ? ok(() -> fineractClient.loanCapitalizedIncome().retrieveCapitalizedIncomeAllocationData(loanId,
                                transactionMatch.getId()))
                        : ok(() -> fineractClient.loanBuyDownFees().retrieveBuyDownFeesAllocationData(loanId, transactionMatch.getId()));
        checkLoanAmortizationAllocationMappingData(resourceId, loanAmortizationAllocationResponse, table);
    }

    @Then("Loan has {double} total unpaid payable not due interest")
    public void loanTotalUnpaidPayableNotDueInterest(double totalUnpaidPayableNotDueInterestExpected) throws IOException {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "repaymentSchedule")));
        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);

        Double totalUnpaidPayableNotDueInterestActual = loanDetailsResponse.getSummary().getTotalUnpaidPayableNotDueInterest()
                .doubleValue();
        assertThat(totalUnpaidPayableNotDueInterestActual)
                .as(ErrorMessageHelper.wrongAmountInTotalUnpaidPayableNotDueInterest(totalUnpaidPayableNotDueInterestActual,
                        totalUnpaidPayableNotDueInterestExpected))
                .isEqualTo(totalUnpaidPayableNotDueInterestExpected);
    }

    private void checkLoanAmortizationAllocationMappingData(final String resourceId,
            final LoanAmortizationAllocationResponse amortizationAllocationResponse, final DataTable table) {
        final List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            final List<String> expectedValues = data.get(i);
            assert amortizationAllocationResponse.getAmortizationMappings() != null;
            final boolean found = amortizationAllocationResponse.getAmortizationMappings().stream().anyMatch(t -> {
                final List<String> actualValues = fetchValuesOfAmortizationAllocationMapping(table.row(0), t);
                return actualValues.equals(expectedValues);
            });

            assertThat(found).as(ErrorMessageHelper.wrongValueInLineInDeferredIncomeTab(resourceId, i,
                    amortizationAllocationResponse.getAmortizationMappings().stream()
                            .map(t -> fetchValuesOfAmortizationAllocationMapping(table.row(0), t)).collect(Collectors.toList()),
                    expectedValues)).isTrue();
        }
        assertThat(amortizationAllocationResponse.getAmortizationMappings().size())
                .as(ErrorMessageHelper.nrOfLinesWrongInDeferredIncomeTab(resourceId,
                        amortizationAllocationResponse.getAmortizationMappings().size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    private List<String> fetchValuesOfAmortizationAllocationMapping(final List<String> header, final AmortizationMappingData t) {
        final List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Date" -> actualValues.add(t.getDate() == null ? null : FORMATTER.format(t.getDate()));
                case "Type" -> actualValues.add(t.getType() == null ? null : t.getType());
                case "Amount" ->
                    actualValues.add(t.getAmount() == null ? new Utils.DoubleFormatter(new BigDecimal("0.0").doubleValue()).format()
                            : new Utils.DoubleFormatter(t.getAmount().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    @Then("In Loan Transactions the {string}th Transaction of {string} on {string} has {string} relationship with type={string}")
    public void inLoanTransactionsTheThTransactionOfOnHasRelationshipWithTypeREPLAYED(String nthTransactionFromStr, String transactionType,
            String transactionDate, String numberOfRelations, String relationshipType) throws IOException {
        final PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final long loanId = loanCreateResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", "false", "associations", "transactions")));
        final List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        final int nthTransactionFrom = nthTransactionFromStr == null ? transactions.size() - 1
                : Integer.parseInt(nthTransactionFromStr) - 1;
        final GetLoansLoanIdTransactions transactionFrom = transactions.stream()
                .filter(t -> transactionType.equals(t.getType().getValue()) && transactionDate.equals(FORMATTER.format(t.getDate())))
                .toList().get(nthTransactionFrom);

        final List<GetLoansLoanIdLoanTransactionRelation> relationshipOptional = transactionFrom.getTransactionRelations().stream()
                .filter(r -> r.getRelationType().equals(relationshipType)).toList();

        assertEquals(Integer.valueOf(numberOfRelations), relationshipOptional.size(), "Missed relationship for transaction");
    }
}
