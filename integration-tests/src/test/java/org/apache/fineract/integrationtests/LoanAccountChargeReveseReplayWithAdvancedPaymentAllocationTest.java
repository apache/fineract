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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanAccountChargeReveseReplayWithAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void testLoanChargeReverseReplayWithAdvancedPaymentStrategy() {
        runAt("10 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(true);
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, true, "02 September 2022",
                    "03 September 2022");

            // make an in advance repayment
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("8 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

            // apply charges
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 9);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 20.0, penaltyCharge1AddedDate);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(20.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(10.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(900.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(930.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod()));
        });
    }

    @Test
    public void testLoanChargeReverseReplayWithStandardPaymentStrategy() {
        runAt("10 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(false);
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, false, "02 September 2022",
                    "03 September 2022");

            // make an in advance repayment
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("8 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

            // apply charges
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 9);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 20.0, penaltyCharge1AddedDate);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(930.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(930.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod()));
        });
    }

    @Test
    public void testRepaymentReverseReplayedOnBackdatedChargeWithAdvancedPaymentStrategy() {
        runAt("1 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(true);
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, true, "1 September 2022", "1 September 2022");

            // make a repayment on 3rd od Sept
            updateBusinessDate("3 September 2022");
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("3 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

            // apply charges on 4th of Sept backdated to 2nd of Sept 2022
            updateBusinessDate("4 September 2022");
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 2);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 20.0, penaltyCharge1AddedDate);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(930.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(930.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod()));
        });
    }

    @Test
    public void testObligationMetDateIsNotMetOnExtraInstallment() {
        runAt("1 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(true);
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr, true, "1 September 2022", "1 September 2022");

            // make a repayment on 3rd od Sept
            updateBusinessDate("3 September 2022");
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("3 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

            // apply charges on 4th of Sept backdated to 2nd of Sept 2022
            updateBusinessDate("4 September 2022");
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 2);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 20.0, penaltyCharge1AddedDate);

            // make a full repayment of 10th of September
            updateBusinessDate("10 September 2022");
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("10 September 2022").locale(LoanTestData.LOCALE).transactionAmount(930.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding()));
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalOutstandingForPeriod()));

            // adding an extra charge after maturity
            updateBusinessDate("11 October 2022");
            Long snoozeFee = createLoanSpecifiedDueDateCharge(30.0);
            addLoanCharge(loanId, snoozeFee, "11 October 2022", 30.0);

            loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());
            assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().size()); // extra instalment is created
            assertNull(loanDetails.getRepaymentSchedule().getPeriods().get(2).getObligationsMetOnDate()); // not repayed
        });
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId,
            final boolean advancedPaymentStrategy, String approveDate, String disbursementDate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy(advancedPaymentStrategy ? "advanced-payment-allocation-strategy" : "mifos-standard-strategy")
                .build(clientId.toString(), loanProductId.toString(), null);

        Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, approveDate));
        disburseLoanWithAmount(loanId, disbursementDate, 1000.0);
        return loanId;
    }

    private Long createLoanProductWithPeriodicAccrualAccounting(boolean advancedPaymentStrategy) {

        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<LoanProductChargeData> charges = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> feeToIncomeAccountMappings = new ArrayList<>();

        String paymentTypeName = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = false;
        Long position = 1L;

        var paymentTypesResponse = PaymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest().name(paymentTypeName)
                .description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeIdOne = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeIdOne);

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeIdOne.longValue());
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Long fundId = FundsResourceHandler.createFund().getResourceId();
        Assertions.assertNotNull(fundId);

        // Delinquency Bucket
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";

        PostLoanProductsRequest loanProductsRequest = new PostLoanProductsRequest().name(name)//
                .shortName(shortName)//
                .description("Loan Product Description")//
                .fundId(fundId)//
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
                .allowPartialPeriodInterestCalculation(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(3)//
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())//
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())//
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount().getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount().getAccountID().longValue())//
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())//
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())//
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())//
                .receivableInterestAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivablePenaltyAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .dateFormat("dd MMMM yyyy")//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .goodwillCreditAccountId(getAccounts().getGoodwillExpenseAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue());//

        if (advancedPaymentStrategy) {
            AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

            loanProductsRequest //
                    .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")//
                    .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString())//
                    .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())//
                    .addPaymentAllocationItem(defaultAllocation);
        }

        return createLoanProduct(loanProductsRequest);
    }
}
