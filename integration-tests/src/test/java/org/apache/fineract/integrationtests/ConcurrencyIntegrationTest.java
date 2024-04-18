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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class ConcurrencyIntegrationTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyIntegrationTest.class);
    private static final String NO_ACCOUNTING = "1";

    private static final int MYTHREADS = 30;

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static BatchHelper batchHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        commonLoanProductId = createLoanProduct("500", "15", "4", true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, LoanScheduleType loanScheduleType,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");
        AdvancedPaymentData goodwillCreditAllocation = createDefaultPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT");
        AdvancedPaymentData merchantIssuedRefundAllocation = createDefaultPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION");
        AdvancedPaymentData payoutRefundAllocation = createDefaultPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT");
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(downPaymentEnabled, downPaymentPercentage, autoPayForDownPayment).withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation, goodwillCreditAllocation, merchantIssuedRefundAllocation,
                        payoutRefundAllocation)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance().withMultiDisburse()
                .withDisallowExpectedDisbursements(true).withLoanScheduleType(loanScheduleType)
                .withLoanScheduleProcessingType(loanScheduleProcessingType).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String transactionProcessorCode,
            String loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanTransactionHelper.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(transactionProcessorCode).locale("en").submittedOnDate(submittedOnDate)
                .amortizationType(1).interestRatePerPeriod(interestRate).interestCalculationPeriodType(1).interestType(0)
                .repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery).repaymentFrequencyType(0)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency).loanTermFrequencyType(0).principal(principal)
                .loanType("individual").loanScheduleProcessingType(loanScheduleProcessingType).externalId(UUID.randomUUID().toString())
                .maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }

    private static Stream<Arguments> enclosingTransaction() {
        return Stream.of(Arguments.of(Named.of("withEnclosingTransaction", true)), //
                Arguments.of(Named.of("withoutEnclosingTransaction", false)));
    }

    // UC1: Reverse-replay parallel
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. Pay installments on due dates
    @ParameterizedTest
    @MethodSource("enclosingTransaction")
    public void uc1(boolean enclosingTransaction) {
        runAt("15 February 2023", () -> {
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023",
                    AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY,
                    LoanScheduleProcessingType.HORIZONTAL.name());

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(500.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Down Payment", "01 January 2023") //
            );
            assertTrue(loanDetails.getStatus().getActive());

            addCharge(loanResponse.getLoanId(), false, 50, "10 January 2023");

            String firstRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("11 January 2023").locale("en")
                            .transactionAmount(10.0).externalId(firstRepaymentExternalId));

            String secondRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("12 January 2023").locale("en")
                            .transactionAmount(41.0).externalId(secondRepaymentExternalId));

            String thirdRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("13 January 2023").locale("en")
                            .transactionAmount(16.0).externalId(thirdRepaymentExternalId));

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(500.0, "Disbursement", "01 January 2023", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(125.0, "Down Payment", "01 January 2023", 375.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Repayment", "11 January 2023", 375.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0), //
                    transaction(41.0, "Repayment", "12 January 2023", 374.0, 1.0, 0.0, 40.0, 0.0, 0.0, 0.0), //
                    transaction(16.0, "Repayment", "13 January 2023", 358.0, 16.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
            Callable<BatchResponse> worker1 = new BatchLoanReversalExecutor(1L, loanResponse.getResourceExternalId(),
                    firstRepaymentExternalId, LocalDate.of(2023, 1, 14), enclosingTransaction);
            Callable<BatchResponse> worker2 = new BatchLoanReversalExecutor(2L, loanResponse.getResourceExternalId(),
                    secondRepaymentExternalId, LocalDate.of(2023, 1, 14), enclosingTransaction);
            Callable<BatchResponse> worker3 = new BatchLoanReversalExecutor(3L, loanResponse.getResourceExternalId(),
                    thirdRepaymentExternalId, LocalDate.of(2023, 1, 14), enclosingTransaction);

            try {
                List<Future<BatchResponse>> futures = executor.invokeAll(List.of(worker1, worker2, worker3));
                for (Future<BatchResponse> future : futures) {
                    Assertions.assertEquals(HttpStatus.SC_OK, (long) future.get().getStatusCode(), "Verify Status Code 200");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void uc2() {
        runAt("15 February 2023", () -> {
            Long loanId;
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023",
                    AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY,
                    LoanScheduleProcessingType.HORIZONTAL.name());

            loanId = loanResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(500.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Down Payment", "01 January 2023") //
            );
            assertTrue(loanDetails.getStatus().getActive());

            addCharge(loanResponse.getLoanId(), false, 50, "10 January 2023");

            String firstRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("11 January 2023").locale("en")
                            .transactionAmount(10.0).externalId(firstRepaymentExternalId));

            String secondRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("12 January 2023").locale("en")
                            .transactionAmount(41.0).externalId(secondRepaymentExternalId));

            String thirdRepaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("13 January 2023").locale("en")
                            .transactionAmount(16.0).externalId(thirdRepaymentExternalId));

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(500.0, "Disbursement", "01 January 2023", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(125.0, "Down Payment", "01 January 2023", 375.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Repayment", "11 January 2023", 375.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0), //
                    transaction(41.0, "Repayment", "12 January 2023", 374.0, 1.0, 0.0, 40.0, 0.0, 0.0, 0.0), //
                    transaction(16.0, "Repayment", "13 January 2023", 358.0, 16.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
            Callable<PostLoansLoanIdTransactionsResponse> worker1 = new LoanReversalExecutor(loanTransactionHelper,
                    loanResponse.getResourceExternalId(), firstRepaymentExternalId, LocalDate.of(2023, 1, 14));
            Callable<PostLoansLoanIdTransactionsResponse> worker2 = new LoanReversalExecutor(loanTransactionHelper,
                    loanResponse.getResourceExternalId(), secondRepaymentExternalId, LocalDate.of(2023, 1, 14));
            Callable<PostLoansLoanIdTransactionsResponse> worker3 = new LoanReversalExecutor(loanTransactionHelper,
                    loanResponse.getResourceExternalId(), thirdRepaymentExternalId, LocalDate.of(2023, 1, 14));

            try {
                List<Future<PostLoansLoanIdTransactionsResponse>> futures = executor.invokeAll(List.of(worker1, worker2, worker3));
                for (Future<PostLoansLoanIdTransactionsResponse> future : futures) {
                    Assertions.assertEquals(loanId, future.get().getLoanId(), "Verify it was successful");
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void verifyConcurrentLoanRepayments() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NO_ACCOUNTING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "12,000.00");
        this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID, "12,000.00",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
        Calendar date = Calendar.getInstance();
        date.set(2011, 9, 20);
        Double repaymentAmount = 100.0;
        for (int i = 0; i < 3; i++) {
            LOG.info("Starting concurrent transaction number {}", i);
            date.add(Calendar.DAY_OF_MONTH, 1);
            repaymentAmount = repaymentAmount + 100;
            Callable<PostLoansLoanIdTransactionsResponse> worker = new LoanRepaymentExecutor(loanTransactionHelper, loanID, repaymentAmount,
                    date);
            Future<PostLoansLoanIdTransactionsResponse> future = executor.submit(worker);
            try {
                Assertions.assertEquals((long) loanID, future.get().getLoanId(), "Verify it was successful");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        LOG.info("\nFinished all threads");

    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);

        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withCollaterals(collaterals).build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    public static class LoanRepaymentExecutor implements Callable<PostLoansLoanIdTransactionsResponse> {

        private final Integer loanId;
        private final Double repaymentAmount;
        private final String repaymentDate;
        private final LoanTransactionHelper loanTransactionHelper;

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        LoanRepaymentExecutor(LoanTransactionHelper loanTransactionHelper, Integer loanId, Double repaymentAmount, Calendar repaymentDate) {
            this.loanId = loanId;
            this.repaymentAmount = repaymentAmount;
            this.repaymentDate = dateFormat.format(repaymentDate.getTime());
            this.loanTransactionHelper = loanTransactionHelper;
        }

        @Override
        public PostLoansLoanIdTransactionsResponse call() {
            return this.loanTransactionHelper.makeLoanRepayment((long) loanId, new PostLoansLoanIdTransactionsRequest()
                    .transactionDate(repaymentDate).dateFormat(DATETIME_PATTERN).locale("en").transactionAmount(repaymentAmount));
        }
    }

    public static class LoanReversalExecutor implements Callable<PostLoansLoanIdTransactionsResponse> {

        private final String loanExternalId;
        private final String transactionExternalId;
        private final LocalDate reversalDate;
        private final LoanTransactionHelper loanTransactionHelper;

        LoanReversalExecutor(LoanTransactionHelper loanTransactionHelper, String loanExternalId, String transactionExternalId,
                LocalDate reversalDate) {
            this.loanExternalId = loanExternalId;
            this.transactionExternalId = transactionExternalId;
            this.reversalDate = reversalDate;
            this.loanTransactionHelper = loanTransactionHelper;
        }

        @Override
        public PostLoansLoanIdTransactionsResponse call() {
            return this.loanTransactionHelper.reverseLoanTransaction(loanExternalId, transactionExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                            .transactionDate(dateTimeFormatter.format(reversalDate)).locale("en").transactionAmount(0.0));
        }
    }

    public static class BatchLoanReversalExecutor implements Callable<BatchResponse> {

        private final Long requestNumber;
        private final String loanExternalId;
        private final String transactionExternalId;
        private final LocalDate reversalDate;
        private final boolean enclosingTransaction;

        BatchLoanReversalExecutor(Long requestNumber, String loanExternalId, String transactionExternalId, LocalDate reversalDate,
                boolean enclosingTransaction) {
            this.requestNumber = requestNumber;
            this.loanExternalId = loanExternalId;
            this.transactionExternalId = transactionExternalId;
            this.reversalDate = reversalDate;
            this.enclosingTransaction = enclosingTransaction;
        }

        @Override
        public BatchResponse call() {
            BatchRequest repaymentReversalRequest = BatchHelper.createAdjustTransactionByExternalIdRequest(requestNumber, null,
                    loanExternalId, transactionExternalId, "0", reversalDate);
            List<BatchResponse> response;
            if (enclosingTransaction) {
                response = BatchHelper.postBatchRequestsWithEnclosingTransaction(requestSpec, responseSpec,
                        BatchHelper.toJsonString(List.of(repaymentReversalRequest)));
            } else {
                response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec, responseSpec,
                        BatchHelper.toJsonString(List.of(repaymentReversalRequest)));
            }

            return response.get(0);
        }
    }
}
