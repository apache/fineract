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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the creation, approval and rejection of a loan reschedule request
 **/
@SuppressWarnings({ "rawtypes" })
public class LoanRescheduleRequestTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleRequestTest.class);
    private ResponseSpecification responseSpec;
    private ResponseSpecification generalResponseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private Integer clientId;
    private Integer loanProductId;
    private Integer loanId;
    private Integer loanRescheduleRequestId;
    private final String loanPrincipalAmount = "100000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";
    private final String dateString = "04 September 2014";

    @BeforeEach
    public void initialize() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);

        this.generalResponseSpec = new ResponseSpecBuilder().build();

        // create all required entities
        this.createRequiredEntities();
    }

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
        this.clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, this.clientId);
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount)
                .withNumberOfRepayments(numberOfRepayments).withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().build(null);

        this.loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    /**
     * submit a new loan application, approve and disburse the loan
     **/
    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                this.clientId.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeAsDays().withInterestRatePerPeriod(interestRatePerPeriod).withLoanTermFrequencyAsMonths()
                .withSubmittedOnDate(dateString).withExpectedDisbursementDate(dateString).withPrincipalGrace("2").withInterestGrace("2")
                .withCollaterals(collaterals).build(this.clientId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        this.approveLoanApplication();
        this.disburseLoan();
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

    /**
     * approve the loan application
     **/
    private void approveLoanApplication() {

        if (this.loanId != null) {
            this.loanTransactionHelper.approveLoan(this.dateString, this.loanId);
            LOG.info("Successfully approved loan (ID: {} )", this.loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan() {

        if (this.loanId != null) {
            String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, this.loanId);
            this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(this.dateString, this.loanId,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
        }
    }

    /**
     * create new loan reschedule request
     **/
    private void createLoanRescheduleRequest() {
        LOG.info("---------------------------------CREATING LOAN RESCHEDULE REQUEST------------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleRequest() {
        this.createLoanRescheduleRequest();
    }

    @Test
    public void testRejectLoanRescheduleRequest() {
        this.createLoanRescheduleRequest();

        LOG.info("-----------------------------REJECTING LOAN RESCHEDULE REQUEST--------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().getRejectLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.rejectLoanRescheduleRequest(this.loanRescheduleRequestId, requestJSON);

        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("rejected"));

        LOG.info("Successfully rejected loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    @Test
    public void testApproveLoanRescheduleRequest() {
        this.createLoanRescheduleRequest();

        LOG.info("-----------------------------APPROVING LOAN RESCHEDULE REQUEST--------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, requestJSON);

        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        final Integer numberOfRepayments = (Integer) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec, loanId,
                "numberOfRepayments");
        final HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(requestSpec, generalResponseSpec, loanId);
        final Float totalExpectedRepayment = (Float) loanSummary.get("totalExpectedRepayment");

        assertEquals(12, numberOfRepayments, "NUMBER OF REPAYMENTS is NOK");
        assertEquals(118000, totalExpectedRepayment, "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testInterestRateChangeForProgressiveLoan() {
        PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        Integer commonLoanProductId = createLoanProduct("500", "15", "4", true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        AtomicReference<PostCreateRescheduleLoansResponse> rescheduleResponse = new AtomicReference<>();
        AtomicReference<PostLoansResponse> loanResponse = new AtomicReference<>();
        // Do not allow interest rate change on not active loan
        // Do not allow interest rate change twice on the same day
        runAt("15 February 2023", () -> {

            loanResponse.set(applyForLoanApplication(client.getClientId(), commonLoanProductId, BigDecimal.valueOf(500.0), 45, 15, 3,
                    BigDecimal.ZERO, "01 January 2023", "01 January 2023"));

            loanTransactionHelper.approveLoan(loanResponse.get().getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanRescheduleRequestHelper
                            .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanResponse.get().getLoanId())
                                    .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("15 February 2023")
                                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("16 February 2023")));
            assertEquals(400, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("loan.is.not.active"));

            loanTransactionHelper.disburseLoan(loanResponse.get().getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("15 February 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanRescheduleRequestHelper
                            .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanResponse.get().getLoanId())
                                    .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("15 February 2023")
                                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("15 February 2023")));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("loan.reschedule.interest.rate.change.reschedule.from.date.should.be.in.future"));

            rescheduleResponse.set(loanRescheduleRequestHelper.createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest()
                    .loanId(loanResponse.get().getLoanId()).dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("15 February 2023")
                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("16 February 2023")));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanRescheduleRequestHelper
                            .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanResponse.get().getLoanId())
                                    .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("15 February 2023")
                                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("16 February 2023")));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("loan.reschedule.interest.rate.change.already.exists"));
        });
        // Do not allow approve an interest rate change if the reschedule from date is not in the future
        // Do not allow create interest rate change if a previous interest rate change got already approved for that
        // date
        runAt("16 February 2023", () -> {
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanRescheduleRequestHelper.approveLoanRescheduleRequest(rescheduleResponse.get().getResourceId(),
                            new PostUpdateRescheduleLoansRequest().approvedOnDate("16 February 2024").locale("en")
                                    .dateFormat(DATETIME_PATTERN)));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("loan.reschedule.interest.rate.change.reschedule.from.date.should.be.in.future"));

            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanRescheduleRequestHelper
                    .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanResponse.get().getLoanId())
                            .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("17 February 2023").newInterestRate(BigDecimal.ONE)
                            .rescheduleReasonId(1L).rescheduleFromDate("17 February 2023"));

            loanRescheduleRequestHelper.approveLoanRescheduleRequest(rescheduleLoansResponse.getResourceId(),
                    new PostUpdateRescheduleLoansRequest().approvedOnDate("17 February 2024").locale("en").dateFormat(DATETIME_PATTERN));

            exception = assertThrows(CallFailedRuntimeException.class,
                    () -> loanRescheduleRequestHelper
                            .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(rescheduleLoansResponse.getLoanId())
                                    .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("17 February 2023")
                                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("17 February 2023")));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage().contains("loan.reschedule.interest.rate.change.already.exists"));

        });

        // Allow new interest rate change if the previous got rejected
        runAt("17 February 2023", () -> {
            PostCreateRescheduleLoansResponse rescheduleLoansResponse = loanRescheduleRequestHelper
                    .createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest().loanId(loanResponse.get().getLoanId())
                            .dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("18 February 2023").newInterestRate(BigDecimal.ONE)
                            .rescheduleReasonId(1L).rescheduleFromDate("18 February 2023"));

            loanRescheduleRequestHelper.rejectLoanRescheduleRequest(rescheduleLoansResponse.getResourceId(),
                    new PostUpdateRescheduleLoansRequest().rejectedOnDate("18 February 2024").locale("en").dateFormat(DATETIME_PATTERN));

            loanRescheduleRequestHelper.createLoanRescheduleRequest(new PostCreateRescheduleLoansRequest()
                    .loanId(loanResponse.get().getLoanId()).dateFormat(DATETIME_PATTERN).locale("en").submittedOnDate("18 February 2023")
                    .newInterestRate(BigDecimal.ONE).rescheduleReasonId(1L).rescheduleFromDate("18 February 2023"));

        });
    }

    /**
     * create new loan reschedule request
     **/
    private void createLoanRescheduleChangeEMIRequest() {
        LOG.info("---------------------------------CREATING LOAN RESCHEDULE REQUEST CHANGE EMI------------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateEMI("5000").updateEmiChangeEndDate("4 February 2015")
                .updateRescheduleFromDate("04 January 2015").updateRecalculateInterest(true).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleChangeEMIRequest() {
        this.createLoanRescheduleChangeEMIRequest();
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
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
                .loanType("individual").loanScheduleProcessingType(loanScheduleProcessingType)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, LoanScheduleProcessingType.HORIZONTAL);
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, LoanScheduleProcessingType loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate,
                AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY, loanScheduleProcessingType.name());
    }

    private Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, LoanScheduleType loanScheduleType,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(downPaymentEnabled, downPaymentPercentage, autoPayForDownPayment).withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance().withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withLoanScheduleType(loanScheduleType).withLoanScheduleProcessingType(loanScheduleProcessingType).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }
}
