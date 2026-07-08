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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanRescheduleRequestResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanTermVariationsData;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the creation, approval and rejection of a loan reschedule request
 **/
public class LoanRescheduleRequestTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleRequestTest.class);
    private Long clientId;
    private Long loanProductId;
    private Long loanId;
    private Long loanRescheduleRequestId;
    private final double loanPrincipalAmount = 100000.00;
    private final int numberOfRepayments = 12;
    private final double interestRatePerPeriod = 18;
    private final String dateString = "04 September 2014";

    /**
     * Creates the client, loan product, and loan entities
     **/
    private void createRequiredEntities() {
        this.createClientEntity();
        this.createLoanProductEntity();
        this.createLoanEntity();
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = createClient();
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        PostLoanProductsRequest product = twelveMonthInterestRecalculationProduct()//
                .principal(loanPrincipalAmount)//
                .interestRatePerPeriod(interestRatePerPeriod)//
                .isInterestRecalculationEnabled(false);

        this.loanProductId = createLoanProduct(product);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    /**
     * submit a new loan application, approve and disburse the loan
     **/
    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        PostLoansRequest applyRequest = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(BigDecimal.valueOf(loanPrincipalAmount))//
                .numberOfRepayments(numberOfRepayments)//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(interestRatePerPeriod))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .graceOnPrincipalPayment(2)//
                .graceOnInterestPayment(2)//
                .submittedOnDate(dateString)//
                .expectedDisbursementDate(dateString)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE)//
                .loanType("individual");

        this.loanId = applyForLoan(applyRequest);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        this.approveLoanApplication();
        this.disburseLoan();
    }

    /**
     * approve the loan application
     **/
    private void approveLoanApplication() {
        if (this.loanId != null) {
            approveLoan(loanId, LoanRequestBuilders.approveLoan(loanPrincipalAmount, dateString));
            LOG.info("Successfully approved loan (ID: {} )", this.loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan() {
        if (this.loanId != null) {
            disburseLoan(loanId,
                    new PostLoansLoanIdRequest().actualDisbursementDate(dateString)
                            .transactionAmount(getLoanDetails(loanId).getNetDisbursalAmount()).dateFormat(LoanTestData.DATETIME_PATTERN)
                            .locale(LoanTestData.LOCALE));
            LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
        }
    }

    /**
     * create new loan reschedule request
     **/
    private void createLoanRescheduleRequest() {
        LOG.info("---------------------------------CREATING LOAN RESCHEDULE REQUEST------------------------------------------");

        PostCreateRescheduleLoansRequest request = defaultRescheduleRequest(loanId);
        this.loanRescheduleRequestId = createRescheduleRequest(request);
        assertNotNull(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    private PostCreateRescheduleLoansRequest defaultRescheduleRequest(Long loanId) {
        return new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .submittedOnDate(dateString)//
                .rescheduleFromDate("04 December 2014")//
                .graceOnPrincipal(2)//
                .graceOnInterest(2)//
                .extraTerms(2)//
                .rescheduleReasonId(1L)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    @Test
    public void testCreateLoanRescheduleRequest() {
        this.createRequiredEntities();
        this.createLoanRescheduleRequest();
    }

    @Test
    public void testRejectLoanRescheduleRequest() {
        this.createRequiredEntities();
        this.createLoanRescheduleRequest();

        LOG.info("-----------------------------REJECTING LOAN RESCHEDULE REQUEST--------------------------");

        loanHelper.rejectRescheduleRequest(this.loanRescheduleRequestId, new PostUpdateRescheduleLoansRequest().rejectedOnDate(dateString)
                .locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

        assertTrue(loanHelper.readRescheduleRequest(loanRescheduleRequestId, "statusEnum").getStatusEnum().getRejected());

        LOG.info("Successfully rejected loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    @Test
    public void testApproveLoanRescheduleRequest() {
        this.createRequiredEntities();
        this.createLoanRescheduleRequest();

        LOG.info("-----------------------------APPROVING LOAN RESCHEDULE REQUEST--------------------------");

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));

        assertTrue(loanHelper.readRescheduleRequest(loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

        assertEquals(12, loanDetails.getNumberOfRepayments(), "NUMBER OF REPAYMENTS is NOK");
        assertEquals(0, BigDecimal.valueOf(118000).compareTo(loanDetails.getSummary().getTotalExpectedRepayment()),
                "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testInterestRateChangeForProgressiveLoan() {
        Long client = createClient();
        final Account assetAccount = accountHelper.createAssetAccount("asset");
        final Account incomeAccount = accountHelper.createIncomeAccount("income");
        final Account expenseAccount = accountHelper.createExpenseAccount("expense");
        final Account overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");

        Long commonLoanProductId = createProgressiveDownPaymentLoanProduct("500", 15, 4, true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<PostCreateRescheduleLoansResponse> rescheduleResponse = new AtomicReference<>();
        // Do not allow interest rate change on not active loan
        // Do not allow interest rate change twice on the same day
        runAt("15 February 2023", () -> {

            loanIdRef.set(applyForProgressiveLoan(client, commonLoanProductId, 500.0, 45, 15, 3, BigDecimal.TEN, "01 January 2023",
                    "01 January 2023"));

            approveLoan(loanIdRef.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(LoanTestData.DATETIME_PATTERN).approvedOnDate("01 January 2023").locale(LoanTestData.LOCALE));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> createRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                            .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("15 February 2023")
                            .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("16 February 2023")));
            assertEquals(400, exception.getStatus());
            assertTrue(exception.getMessage().contains("loan.is.not.active"));

            disburseLoan(loanIdRef.get(), new PostLoansLoanIdRequest().actualDisbursementDate("15 February 2023")
                    .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale(LoanTestData.LOCALE));

            rescheduleResponse.set(loanHelper.createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                    .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("15 February 2023")
                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("16 February 2023")));

            createRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get()).dateFormat(LoanTestData.DATETIME_PATTERN)
                    .locale(LoanTestData.LOCALE).submittedOnDate("15 February 2023").newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L)
                    .rescheduleFromDate("16 February 2023"));
        });
        // Do not allow approve an interest rate change if the reschedule from date is not in the future
        // Do not allow create interest rate change if a previous interest rate change got already approved for that
        // date
        runAt("16 February 2023", () -> {
            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanHelper
                    .createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                            .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("17 February 2023")
                            .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("17 February 2023"));

            approveRescheduleRequest(rescheduleLoansResponse.getResourceId(), new PostUpdateRescheduleLoansRequest()
                    .approvedOnDate("17 February 2024").locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            PostCreateRescheduleLoansResponse secondRescheduleLoansResponse = loanHelper
                    .createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                            .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("17 February 2023")
                            .newInterestRate(BigDecimal.TEN).rescheduleReasonId(1L).rescheduleFromDate("17 February 2023"));
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanIdRef.get());
            assertEquals(loanDetails.getSummary().getInterestCharged().stripTrailingZeros(), BigDecimal.valueOf(1.53).stripTrailingZeros());

            approveRescheduleRequest(secondRescheduleLoansResponse.getResourceId(), new PostUpdateRescheduleLoansRequest()
                    .approvedOnDate("17 February 2024").locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            loanDetails = getLoanDetails(loanIdRef.get());
            assertEquals(loanDetails.getSummary().getInterestCharged().stripTrailingZeros(), BigDecimal.valueOf(4.22).stripTrailingZeros());
        });

        // Allow new interest rate change if the previous got rejected
        runAt("17 February 2023", () -> {
            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanHelper
                    .createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                            .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("18 February 2023")
                            .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("18 February 2023"));

            loanHelper.rejectRescheduleRequest(rescheduleLoansResponse.getResourceId(), new PostUpdateRescheduleLoansRequest()
                    .rejectedOnDate("18 February 2024").locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            createRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get()).dateFormat(LoanTestData.DATETIME_PATTERN)
                    .locale(LoanTestData.LOCALE).submittedOnDate("18 February 2023").newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L)
                    .rescheduleFromDate("18 February 2023"));

        });
    }

    /**
     * create new loan reschedule request
     **/
    private void createLoanRescheduleChangeEMIRequest() {
        LOG.info("---------------------------------CREATING LOAN RESCHEDULE REQUEST CHANGE EMI------------------------------------------");

        PostCreateRescheduleLoansRequest request = new PostCreateRescheduleLoansRequest()//
                .loanId(loanId)//
                .submittedOnDate(dateString)//
                .rescheduleFromDate("04 January 2015")//
                .emi(BigDecimal.valueOf(5000))//
                .endDate("4 February 2015")//
                .rescheduleReasonId(1L)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);

        this.loanRescheduleRequestId = createRescheduleRequest(request);
        assertNotNull(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleChangeEMIRequest() {
        this.createRequiredEntities();
        this.createLoanRescheduleChangeEMIRequest();
    }

    @Test
    public void givenProgressiveLoanWithPaidInstallmentWhenInterestRateChangedThenDueAmountUpdated() {
        Long client = createClient();
        Long commonLoanProductId = createProgressiveLoanProduct();

        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("2 February 2024", () -> {
            loanIdRef.set(applyForProgressiveLoanWithRecalculation(client, commonLoanProductId, "01 January 2024", "01 January 2024"));

            approveAndDisburseLoan(loanIdRef.get(), "01 January 2024", BigDecimal.valueOf(100));
            makeRepayments(loanIdRef.get());

            GetLoansLoanIdResponse savedLoanResponse = getLoanDetails(loanIdRef.get());

            PostCreateRescheduleLoansResponse rescheduleLoansResponse = rescheduleLoanWithNewInterestRate(loanIdRef.get(),
                    "2 February 2024", BigDecimal.ONE, "3 February 2024");

            approveRescheduleRequest(rescheduleLoansResponse.getResourceId(), LoanRequestBuilders.approveReschedule("2 February 2024"));

            GetLoansLoanIdResponse actualLoanResponse = getLoanDetails(loanIdRef.get());

            verifyRepaymentSchedule(savedLoanResponse, actualLoanResponse, 7, 3);
        });
    }

    @Test
    public void testLoanTermVariationDeserializesProperly() {
        Long client = createClient();
        Long commonLoanProductId = createLoanProductPeriodicWithInterest();

        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("01 March 2024", () -> {
            Long loan = applyForLoanApplicationWithInterest(client, commonLoanProductId, BigDecimal.valueOf(4000), "1 March 2023",
                    "1 March 2024");
            loanIdRef.set(loan);
            approveLoan(loanIdRef.get(), LoanRequestBuilders.approveLoan(4000.0, "1 March 2024"));

            disburseLoan(loanIdRef.get(), LoanRequestBuilders.disburseLoan(400.0, "1 March 2024"));

            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanHelper
                    .createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanIdRef.get())
                            .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate("1 March 2024")
                            .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("1 April 2024"));

            GetLoanRescheduleRequestResponse getLoanRescheduleRequestResponse = Assertions
                    .assertDoesNotThrow(() -> loanHelper.readRescheduleRequest(rescheduleLoansResponse.getResourceId(), null));
            Assertions.assertNotNull(getLoanRescheduleRequestResponse);
        });
    }

    @Test
    public void testCreateLoanRescheduleChangeEMIWithExtraTermsUsesFutureScheduleForEndDate() {
        Long client = createClient();
        Long commonLoanProductId = createLoanProductPeriodicWithInterest();

        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("01 March 2024", () -> {
            Long loan = applyForLoanApplicationWithInterest(client, commonLoanProductId, BigDecimal.valueOf(4000), "1 March 2024",
                    "1 March 2024");
            loanIdRef.set(loan);
            approveLoan(loanIdRef.get(), LoanRequestBuilders.approveLoan(4000.0, "1 March 2024"));
            disburseLoan(loanIdRef.get(), LoanRequestBuilders.disburseLoan(4000.0, "1 March 2024"));

            PostCreateRescheduleLoansRequest request = new PostCreateRescheduleLoansRequest()//
                    .loanId(loanIdRef.get())//
                    .submittedOnDate("01 March 2024")//
                    .rescheduleFromDate("01 April 2024")//
                    .extraTerms(2)//
                    .emi(BigDecimal.valueOf(500))//
                    .endDate("01 September 2024")//
                    .rescheduleReasonId(1L)//
                    .locale(LoanTestData.LOCALE)//
                    .dateFormat(LoanTestData.DATETIME_PATTERN);

            Long rescheduleRequestId = createRescheduleRequest(request);
            Assertions.assertNotNull(rescheduleRequestId);

            GetLoanRescheduleRequestResponse createResponse = loanHelper.readRescheduleRequest(rescheduleRequestId, null);
            Assertions.assertNotNull(createResponse);
            Assertions.assertNotNull(createResponse.getLoanTermVariationsData());

            Set<LocalDate> emiTermVariationDates = createResponse.getLoanTermVariationsData().stream()
                    .filter(variation -> variation.getTermType() != null && variation.getTermType().getId() != null
                            && variation.getTermType().getId() == 1L)
                    .map(LoanTermVariationsData::getTermVariationApplicableFrom).collect(Collectors.toCollection(TreeSet::new));

            Set<LocalDate> expectedEMIVariationDates = Set.of(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 7, 1), LocalDate.of(2024, 8, 1), LocalDate.of(2024, 9, 1));
            assertEquals(expectedEMIVariationDates, emiTermVariationDates,
                    "EMI term variations should include installment dates created by extra terms");

            approveRescheduleRequest(rescheduleRequestId, LoanRequestBuilders.approveReschedule("01 March 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanIdRef.get());

            Set<LocalDate> repaymentDueDates = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(period -> period.getPeriod() != null && period.getPeriod() > 0).map(period -> period.getDueDate())
                    .collect(Collectors.toCollection(TreeSet::new));

            assertTrue(repaymentDueDates.containsAll(expectedEMIVariationDates),
                    "Repayment schedule should include all projected installment dates up to the EMI end date");
            assertEquals(LocalDate.of(2024, 9, 1), ((TreeSet<LocalDate>) repaymentDueDates).last(),
                    "Repayment schedule should end on the EMI change end date when extra terms are applied");
        });
    }

    private Long createProgressiveLoanProduct() {
        return createLoanProduct(twelveMonthInterestRecalculationProduct()//
                .numberOfRepayments(numberOfRepayments)//
                .interestRatePerPeriod(7.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                .maxTrancheCount(10)//
                .minPrincipal(1.0)//
                .principal(100.0)//
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .addPaymentAllocationItem(LoanRequestBuilders.defaultPaymentAllocation())//
                .isInterestRecalculationEnabled(true)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString())//
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()));
    }

    private Long applyForProgressiveLoanWithRecalculation(Long clientId, Long loanProductId, String expectedDisbursementDate,
            String submittedOnDate) {
        return applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .locale(LoanTestData.LOCALE).submittedOnDate(submittedOnDate).amortizationType(1)
                .interestRatePerPeriod(BigDecimal.valueOf(7)).interestCalculationPeriodType(0).interestType(0).repaymentFrequencyType(2)
                .repaymentEvery(1).numberOfRepayments(6).loanTermFrequency(6).loanTermFrequencyType(2).principal(BigDecimal.valueOf(100))
                .loanType("individual"));
    }

    private void approveAndDisburseLoan(Long loanId, String date, BigDecimal amount) {
        approveLoan(loanId, createLoanApprovalRequest(date, amount));
        disburseLoan(loanId, createDisbursementRequest(date, amount));
    }

    private PostLoansLoanIdRequest createLoanApprovalRequest(String date, BigDecimal amount) {
        return new PostLoansLoanIdRequest().approvedLoanAmount(amount).dateFormat(LoanTestData.DATETIME_PATTERN).approvedOnDate(date)
                .locale(LoanTestData.LOCALE);
    }

    private PostLoansLoanIdRequest createDisbursementRequest(String date, BigDecimal amount) {
        return new PostLoansLoanIdRequest().actualDisbursementDate(date).dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(amount)
                .locale(LoanTestData.LOCALE);
    }

    private void makeRepayments(Long loanId) {
        addRepaymentForLoan(loanId, 17.01, "01 February 2024");
        addRepaymentForLoan(loanId, 17.01, "02 February 2024");
    }

    private PostCreateRescheduleLoansResponse rescheduleLoanWithNewInterestRate(Long loanId, String submittedOnDate,
            BigDecimal newInterestRate, String rescheduleFromDate) {
        return loanHelper.createRescheduleRequestResponse(new PostCreateRescheduleLoansRequest().loanId(loanId)
                .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE).submittedOnDate(submittedOnDate)
                .newInterestRate(newInterestRate).rescheduleReasonId(1L).rescheduleFromDate(rescheduleFromDate));
    }

    private Long applyForProgressiveLoan(final Long clientId, final Long loanProductId, final double principal, final int loanTermFrequency,
            final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String transactionProcessorCode,
            String loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(
                new PostLoansRequest().clientId(clientId).productId(loanProductId).expectedDisbursementDate(expectedDisbursementDate)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).transactionProcessingStrategyCode(transactionProcessorCode)
                        .locale(LoanTestData.LOCALE).submittedOnDate(submittedOnDate).amortizationType(1)
                        .interestRatePerPeriod(interestRate).interestCalculationPeriodType(1).interestType(0).repaymentFrequencyType(0)
                        .repaymentEvery(repaymentAfterEvery).numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency)
                        .loanTermFrequencyType(0).principal(BigDecimal.valueOf(principal)).loanType("individual")
                        .loanScheduleProcessingType(loanScheduleProcessingType).maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }

    private Long applyForProgressiveLoan(final Long clientId, final Long loanProductId, final double principal, final int loanTermFrequency,
            final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        return applyForProgressiveLoan(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, LoanScheduleProcessingType.HORIZONTAL);
    }

    private Long applyForProgressiveLoan(final Long clientId, final Long loanProductId, final double principal, final int loanTermFrequency,
            final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, LoanScheduleProcessingType loanScheduleProcessingType) {
        return applyForProgressiveLoan(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate,
                AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY, loanScheduleProcessingType.name());
    }

    private Long createProgressiveDownPaymentLoanProduct(final String principal, final int repaymentAfterEvery,
            final int numberOfRepayments, boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment,
            LoanScheduleType loanScheduleType, LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()//
                .minPrincipal(Double.parseDouble(principal))//
                .principal(Double.parseDouble(principal))//
                .repaymentEvery(repaymentAfterEvery)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L)//
                .numberOfRepayments(numberOfRepayments)//
                .enableDownPayment(downPaymentEnabled)//
                .disbursedAmountPercentageForDownPayment(new BigDecimal(downPaymentPercentage))//
                .enableAutoRepaymentForDownPayment(autoPayForDownPayment)//
                .interestRatePerPeriod(0.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .addPaymentAllocationItem(LoanRequestBuilders.defaultPaymentAllocation())//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .loanScheduleType(loanScheduleType.toString())//
                .loanScheduleProcessingType(loanScheduleProcessingType.toString())//
                .daysInMonthType(30)//
                .daysInYearType(365)//
                .graceOnPrincipalPayment(0)//
                .graceOnInterestPayment(0);
        return createLoanProduct(withPeriodicAccrualAccounting(product, accounts));
    }

    private Long createLoanProductPeriodicWithInterest() {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        return createLoanProduct(new PostLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .multiDisburseLoan(true)//
                .maxTrancheCount(2)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .disallowExpectedDisbursements(true)//
                .description("Test loan description")//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .daysInYearType(LoanTestData.DaysInYearType.ACTUAL)//
                .daysInMonthType(LoanTestData.DaysInYearType.ACTUAL)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(1)//
                .rescheduleStrategyMethod(1)//
                .recalculationRestFrequencyInterval(0)//
                .isInterestRecalculationEnabled(false)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                .locale(LoanTestData.LOCALE)//
                .numberOfRepayments(4)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(2.0)//
                .repaymentEvery(1)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000000.0)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY)//
                .accountingRule(1));
    }

    private Long applyForLoanApplicationWithInterest(final Long clientId, final Long loanProductId, BigDecimal principal,
            String submittedOnDate, String expectedDisburmentDate) {
        final PostLoansRequest loanRequest = new PostLoansRequest()//
                .loanTermFrequency(4).locale(LoanTestData.LOCALE).loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(0)
                .interestCalculationPeriodType(0).dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY).loanType("individual").submittedOnDate(submittedOnDate)
                .expectedDisbursementDate(expectedDisburmentDate).clientId(clientId).productId(loanProductId);
        return applyForLoan(loanRequest);
    }

    private void verifyRepaymentSchedule(GetLoansLoanIdResponse savedLoanResponse, GetLoansLoanIdResponse actualLoanResponse,
            int totalPeriods, int identicalPeriods) {
        List<GetLoansLoanIdRepaymentPeriod> savedPeriods = savedLoanResponse.getRepaymentSchedule().getPeriods();
        List<GetLoansLoanIdRepaymentPeriod> actualPeriods = actualLoanResponse.getRepaymentSchedule().getPeriods();

        assertEquals(totalPeriods, savedPeriods.size(), "Unexpected number of periods in savedPeriods list.");
        assertEquals(totalPeriods, actualPeriods.size(), "Unexpected number of periods in actualPeriods list.");

        verifyPeriodsEquality(savedPeriods, actualPeriods, 0, identicalPeriods, true);
        verifyPeriodsEquality(savedPeriods, actualPeriods, identicalPeriods, totalPeriods, false);
    }

    private void verifyPeriodsEquality(List<GetLoansLoanIdRepaymentPeriod> savedPeriods, List<GetLoansLoanIdRepaymentPeriod> actualPeriods,
            int startIndex, int endIndex, boolean shouldEqual) {
        for (int i = startIndex; i < endIndex; i++) {
            Double savedTotalDue = Utils.getDoubleValue(savedPeriods.get(i).getTotalDueForPeriod());
            Double actualTotalDue = Utils.getDoubleValue(actualPeriods.get(i).getTotalDueForPeriod());

            if (shouldEqual) {
                assertEquals(savedTotalDue, actualTotalDue, String.format(
                        "Period %d should be identical in both responses. Expected: %s, Actual: %s", i + 1, savedTotalDue, actualTotalDue));
            } else {
                assertNotEquals(savedTotalDue, actualTotalDue, String
                        .format("Period %d should differ between responses. Saved: %s, Actual: %s", i + 1, savedTotalDue, actualTotalDue));
            }
        }
    }
}
