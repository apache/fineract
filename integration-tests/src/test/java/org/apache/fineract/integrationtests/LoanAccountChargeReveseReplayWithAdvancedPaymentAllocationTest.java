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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostPaymentTypesRequest;
import org.apache.fineract.client.models.PostPaymentTypesResponse;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.funds.FundsHelper;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanAccountChargeReveseReplayWithAdvancedPaymentAllocationTest extends BaseLoanIntegrationTest {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private LoanProductHelper loanProductHelper;
    private PaymentTypeHelper paymentTypeHelper;
    private final BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    // asset
    private Account loansReceivable;
    private Account interestFeeReceivable;
    private Account suspenseAccount;
    private Account fundReceivables;
    // liability
    private Account suspenseClearingAccount;
    private Account overpaymentAccount;
    // income
    private Account interestIncome;
    private Account feeIncome;
    private Account feeChargeOff;
    private Account recoveries;
    private Account interestIncomeChargeOff;
    // expense
    private Account creditLossBadDebt;
    private Account creditLossBadDebtFraud;
    private Account writtenOff;
    private Account goodwillExpenseAccount;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.loanProductHelper = new LoanProductHelper();
        this.paymentTypeHelper = new PaymentTypeHelper();

        // Asset
        this.loansReceivable = this.accountHelper.createAssetAccount();
        this.interestFeeReceivable = this.accountHelper.createAssetAccount();
        this.suspenseAccount = this.accountHelper.createAssetAccount();
        this.fundReceivables = this.accountHelper.createAssetAccount();

        // Liability
        this.suspenseClearingAccount = this.accountHelper.createLiabilityAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();

        // income
        this.interestIncome = this.accountHelper.createIncomeAccount();
        this.feeIncome = this.accountHelper.createIncomeAccount();
        this.feeChargeOff = this.accountHelper.createIncomeAccount();
        this.recoveries = this.accountHelper.createIncomeAccount();
        this.interestIncomeChargeOff = this.accountHelper.createIncomeAccount();

        // expense
        this.creditLossBadDebt = this.accountHelper.createExpenseAccount();
        this.creditLossBadDebtFraud = this.accountHelper.createExpenseAccount();
        this.writtenOff = this.accountHelper.createExpenseAccount();
        this.goodwillExpenseAccount = this.accountHelper.createExpenseAccount();

        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testLoanChargeReverseReplayWithAdvancedPaymentStrategy() {
        runAt("10 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(true);
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr, true, "02 September 2022",
                    "03 September 2022");

            // make an in advance repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                            .transactionAmount(100.0));

            // apply charges
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 9);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // apply penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "20"));

            GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(20.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(900.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(930.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod());
        });
    }

    @Test
    public void testLoanChargeReverseReplayWithStandardPaymentStrategy() {
        runAt("10 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(false);
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr, false, "02 September 2022",
                    "03 September 2022");

            // make an in advance repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                            .transactionAmount(100.0));

            // apply charges
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 9);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // apply penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "20"));

            GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(930.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(930.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod());
        });
    }

    @Test
    public void testRepaymentReverseReplayedOnBackdatedChargeWithAdvancedPaymentStrategy() {
        runAt("1 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(true);
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr, true, "1 September 2022",
                    "1 September 2022");

            // make a repayment on 3rd od Sept
            updateBusinessDate("3 September 2022");
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 September 2022").locale("en")
                            .transactionAmount(100.0));

            // apply charges on 4th of Sept backdated to 2nd of Sept 2022
            updateBusinessDate("4 September 2022");
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 2);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // apply penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "20"));

            GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(930.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(930.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod());
        });
    }

    @Test
    public void testObligationMetDateIsNotMetOnExtraInstallment() {
        runAt("1 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(true);
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr, true, "1 September 2022",
                    "1 September 2022");

            // make a repayment on 3rd od Sept
            updateBusinessDate("3 September 2022");
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 September 2022").locale("en")
                            .transactionAmount(100.0));

            // apply charges on 4th of Sept backdated to 2nd of Sept 2022
            updateBusinessDate("4 September 2022");
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 2);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // apply penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "20"));

            // make a full repayment of 10th of September
            updateBusinessDate("10 September 2022");
            PostLoansLoanIdTransactionsResponse fullRepayment = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                            .transactionAmount(930.0));

            GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod());

            // adding an extra charge after maturity
            updateBusinessDate("11 October 2022");
            Integer snoozeFee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "30.0", false));
            loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(snoozeFee), "11 October 2022", "30.0"));

            loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().size()); // extra instalment is created
            assertNull(loanDetails.getRepaymentSchedule().getPeriods().get(2).getObligationsMetOnDate()); // not repayed
        });
    }

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId,
            final boolean advancedPaymentStrategy, String approveDate, String disbursementDate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy(advancedPaymentStrategy ? "advanced-payment-allocation-strategy" : "mifos-standard-strategy")
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(approveDate, "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount(disbursementDate, loanId, "1000");
        return loanId;
    }

    private Integer createLoanProductWithPeriodicAccrualAccounting(boolean advancedPaymentStrategy) {

        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        String paymentTypeName = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = false;
        Integer position = 1;

        PostPaymentTypesResponse paymentTypesResponse = paymentTypeHelper.createPaymentType(new PostPaymentTypesRequest()
                .name(paymentTypeName).description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeIdOne = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeIdOne);

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(fundReceivables.getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeIdOne.longValue());
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        // fund
        FundsHelper fh = FundsHelper.create(Utils.uniqueRandomStringGenerator("", 10)).externalId(UUID.randomUUID().toString()).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(fundID);

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";

        PostLoanProductsRequest loanProductsRequest = new PostLoanProductsRequest().name(name)//
                .shortName(shortName)//
                .description("Loan Product Description")//
                .fundId(fundID)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(2)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(true)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalcualtion(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(3)//
                .fundSourceAccountId(suspenseClearingAccount.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivable.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncome.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncome.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncome.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveries.getAccountID().longValue())//
                .writeOffAccountId(writtenOff.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestFeeReceivable.getAccountID().longValue())//
                .receivableFeeAccountId(interestFeeReceivable.getAccountID().longValue())//
                .receivablePenaltyAccountId(interestFeeReceivable.getAccountID().longValue())//
                .dateFormat("dd MMMM yyyy")//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOff.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOff.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOff.getAccountID().longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOff.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOff.getAccountID().longValue())//
                .chargeOffExpenseAccountId(creditLossBadDebt.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(creditLossBadDebtFraud.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(feeChargeOff.getAccountID().longValue());//

        if (advancedPaymentStrategy) {
            AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

            loanProductsRequest //
                    .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")//
                    .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString())//
                    .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())//
                    .addPaymentAllocationItem(defaultAllocation);
        }

        PostLoanProductsResponse loanProductCreateResponse = loanProductHelper.createLoanProduct(loanProductsRequest);
        return loanProductCreateResponse.getResourceId().intValue();
    }

    private Long createFund(final String fundJSON, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        String fundId = String.valueOf(FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec));
        if (fundId.equals("null")) {
            // Invalid JSON data parameters
            return null;
        }

        return Long.valueOf(fundId);
    }
}
