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

import static org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity.LIABILITY_TRANSFER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
@Slf4j
public class LoanChargePaymentWithAdvancedPaymentAllocationTest {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern(DATETIME_PATTERN).toFormatter();
    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;
    private static BusinessDateHelper businessDateHelper;
    private static SchedulerJobHelper scheduleJobHelper;
    private static SavingsAccountHelper savingsAccountHelper;
    private static SavingsProductHelper savingsProductHelper;
    private static FinancialActivityAccountHelper financialActivityAccountHelper;
    private static Integer financialActivityAccountId;
    private static Account liabilityTransferAccount;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        businessDateHelper = new BusinessDateHelper();
        scheduleJobHelper = new SchedulerJobHelper(requestSpec);
        savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        savingsProductHelper = new SavingsProductHelper();
        commonLoanProductId = createLoanProduct("500", "15", "4", assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);

        List<HashMap> financialActivities = financialActivityAccountHelper.getAllFinancialActivityAccounts(responseSpec);
        if (financialActivities.isEmpty()) {
            /** Setup liability transfer account **/
            /** Create a Liability and an Asset Transfer Account **/
            liabilityTransferAccount = accountHelper.createLiabilityAccount();
            Assertions.assertNotNull(liabilityTransferAccount);

            /*** Create A Financial Activity to Account Mapping **/
            financialActivityAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                    LIABILITY_TRANSFER.getValue(), liabilityTransferAccount.getAccountID(), responseSpec,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            Assertions.assertNotNull(financialActivityAccountId);
        } else {
            for (HashMap financialActivity : financialActivities) {
                HashMap financialActivityData = (HashMap) financialActivity.get("financialActivityData");
                if (financialActivityData.get("id").equals(FinancialActivityAccountsTest.LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID)) {
                    HashMap glAccountData = (HashMap) financialActivity.get("glAccountData");
                    liabilityTransferAccount = new Account((Integer) glAccountData.get("id"), Account.AccountType.LIABILITY);
                    financialActivityAccountId = (Integer) financialActivity.get("id");
                    break;
                }
            }
        }
    }

    @AfterAll
    public static void tearDown() {
        Integer deletedFinancialActivityAccountId = financialActivityAccountHelper
                .deleteFinancialActivityAccount(financialActivityAccountId, responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(deletedFinancialActivityAccountId);
        Assertions.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
    }

    @Test
    public void feeAndPenaltyChargePaymentWithDefaultAllocationRuleTest() {
        try {

            final String jobName = "Transfer Fee For Loans From Savings";
            final String startDate = "10 April 2022";

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.15").dateFormat("yyyy.MM.dd").locale("en"));

            final Integer savingsId = createSavingsAccountDailyPosting(client.getClientId().intValue(), startDate);

            savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId, 1000L, 45, 15, 3,
                    BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            int loanId = loanResponse.getLoanId().intValue();

            loanTransactionHelper.updateLoan(loanId,
                    updateLoanJson(client.getClientId().intValue(), commonLoanProductId, savingsId.toString()));

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en"));

            final float feePortion = 50.0f;
            final float penaltyPortion = 100.0f;

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT,
                            String.valueOf(feePortion), false));

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT,
                            String.valueOf(penaltyPortion), true));

            LocalDate targetDate = LocalDate.of(2023, 1, 3);
            final String penaltyChargeAddedDate = DATE_FORMATTER.format(targetDate);
            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), penaltyChargeAddedDate, String.valueOf(feePortion)));

            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), penaltyChargeAddedDate, String.valueOf(penaltyPortion)));

            loanTransactionHelper.noAccrualTransactionForRepayment(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(feePortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(penaltyPortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(penaltyPortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(400.0f, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalDueForPeriod());
            assertEquals(400.0f, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 16), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());

            scheduleJobHelper.executeAndAwaitJob(jobName);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(penaltyPortion, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(400, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalDueForPeriod());
            assertEquals(250, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 16), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID, String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL,
                startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                .withMinimumOpenningBalance("10000.0").build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData goodwillCreditAllocation = createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT");
        AdvancedPaymentData merchantIssuedRefundAllocation = createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION");
        AdvancedPaymentData payoutRefundAllocation = createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT");
        log.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(true, "25", true).withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation, goodwillCreditAllocation, merchantIssuedRefundAllocation,
                        payoutRefundAllocation)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final Long principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanTransactionHelper.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString())
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()).locale("en").submittedOnDate(submittedOnDate)
                .amortizationType(1).interestRatePerPeriod(interestRate).interestCalculationPeriodType(1).interestType(0)
                .repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery).repaymentFrequencyType(0)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency).loanTermFrequencyType(0)
                .principal(BigDecimal.valueOf(principal)).loanType("individual"));
    }

    private String updateLoanJson(final Integer clientID, final Integer loanProductID, String savingsId) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("1,000.00") //
                .withLoanTermFrequency("45") //
                .withLoanTermFrequencyAsDays() //
                .withNumberOfRepayments("3") //
                .withRepaymentEveryAfter("15") //
                .withRepaymentFrequencyTypeAsDays() //
                .withInterestRatePerPeriod("0") //
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()) //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("01 January 2023") //
                .withSubmittedOnDate("01 January 2023") //
                .withCollaterals(collaterals) //
                .build(clientID.toString(), loanProductID.toString(), savingsId);
        return loanApplicationJSON;
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    private static AdvancedPaymentData createPaymentAllocation(String transactionType, String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }
}
