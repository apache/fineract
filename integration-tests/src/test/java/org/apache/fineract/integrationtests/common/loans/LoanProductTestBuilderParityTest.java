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
package org.apache.fineract.integrationtests.common.loans;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Pins {@link LoanProductTestBuilder#buildRequest(String, Long)} to {@link LoanProductTestBuilder#build(String, Long)}:
 * the map is serialised and deserialised into the request model the way the retired JSON path did, and the result must
 * equal the typed request. Any drift between the two builders fails here rather than in a loan product test.
 */
public class LoanProductTestBuilderParityTest {

    private static final Account ASSET = new Account(11, Account.AccountType.ASSET);
    private static final Account INCOME = new Account(22, Account.AccountType.INCOME);
    private static final Account EXPENSE = new Account(33, Account.AccountType.EXPENSE);
    private static final Account LIABILITY = new Account(44, Account.AccountType.LIABILITY);
    private static final Account[] ACCOUNTS = { ASSET, INCOME, EXPENSE, LIABILITY };

    private static AdvancedPaymentData defaultAllocation() {
        AdvancedPaymentData data = new AdvancedPaymentData();
        data.setTransactionType("DEFAULT");
        data.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");
        List<PaymentAllocationOrder> orders = new ArrayList<>();
        PaymentAllocationOrder order = new PaymentAllocationOrder();
        order.setPaymentAllocationRule("PAST_DUE_PENALTY");
        order.setOrder(1);
        orders.add(order);
        data.setPaymentAllocationOrder(orders);
        return data;
    }

    private static CreditAllocationData chargebackAllocation() {
        CreditAllocationData data = new CreditAllocationData();
        data.setTransactionType("CHARGEBACK");
        return data;
    }

    private record Case(String name, Supplier<LoanProductTestBuilder> builder, String chargeId, Long delinquencyBucketId) {
    }

    private static List<Case> cases() {
        List<Case> cases = new ArrayList<>();
        cases.add(new Case("defaults", LoanProductTestBuilder::new, null, null));
        cases.add(new Case("charge + bucket", LoanProductTestBuilder::new, "7", 9L));
        cases.add(new Case("thousands separators",
                () -> new LoanProductTestBuilder().withPrincipal("15,000.00").withMinPrincipal("1,000.00").withMaxPrincipal("100,000.00"),
                null, null));
        cases.add(new Case("empty moratorium", () -> new LoanProductTestBuilder().withMoratorium("", ""), null, null));
        cases.add(new Case("zero moratorium", () -> new LoanProductTestBuilder().withMoratorium("0", "0"), null, null));
        cases.add(new Case("declining balance monthly",
                () -> new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                        .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1").withInterestRateFrequencyTypeAsMonths()
                        .withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                        .withInterestCalculationPeriodTypeAsRepaymentPeriod(true),
                null, null));
        cases.add(new Case("flat interest days", () -> new LoanProductTestBuilder().withInterestTypeAsFlat().withRepaymentTypeAsDays()
                .withInterestCalculationPeriodTypeAsDays().withInterestRateFrequencyTypeAsYear(), null, null));
        cases.add(new Case("weekly", () -> new LoanProductTestBuilder().withRepaymentTypeAsWeek(), null, null));
        cases.add(
                new Case("equal principal", () -> new LoanProductTestBuilder().withAmortizationTypeAsEqualPrincipalPayment(), null, null));
        cases.add(new Case("multi disburse", () -> new LoanProductTestBuilder().withMultiDisburse().withMaxTrancheCount("5"), null, null));
        cases.add(new Case("multi disburse disallow expected",
                () -> new LoanProductTestBuilder().withMultiDisburse().withDisallowExpectedDisbursements(true), null, null));
        cases.add(new Case("tranches", () -> new LoanProductTestBuilder().withTranches(true), null, null));
        cases.add(new Case("periodic accrual accounting", () -> new LoanProductTestBuilder().withAccountingRulePeriodicAccrual(ACCOUNTS),
                null, null));
        cases.add(new Case("periodic accrual with fee/penalty asset account", () -> new LoanProductTestBuilder()
                .withAccountingRulePeriodicAccrual(ACCOUNTS).withFeeAndPenaltyAssetAccount(new Account(55, Account.AccountType.ASSET)),
                null, null));
        cases.add(new Case("cash based accounting",
                () -> new LoanProductTestBuilder().withAccounting(LoanProductTestBuilder.CASH_BASED, ACCOUNTS), null, null));
        cases.add(new Case("full accounting config", () -> new LoanProductTestBuilder().withFullAccountingConfig(
                LoanProductTestBuilder.ACCRUAL_PERIODIC,
                LoanProductTestBuilder.FullAccountingConfig.builder().fundSourceAccountId(1L).loanPortfolioAccountId(2L)
                        .transfersInSuspenseAccountId(3L).interestOnLoanAccountId(4L).incomeFromFeeAccountId(5L)
                        .incomeFromPenaltyAccountId(6L).incomeFromRecoveryAccountId(7L).writeOffAccountId(8L)
                        .overpaymentLiabilityAccountId(9L).receivableInterestAccountId(10L).receivableFeeAccountId(11L)
                        .receivablePenaltyAccountId(12L).goodwillCreditAccountId(13L).incomeFromGoodwillCreditInterestAccountId(14L)
                        .incomeFromGoodwillCreditFeesAccountId(15L).incomeFromGoodwillCreditPenaltyAccountId(16L)
                        .incomeFromChargeOffInterestAccountId(17L).incomeFromChargeOffFeesAccountId(18L).chargeOffExpenseAccountId(19L)
                        .chargeOffFraudExpenseAccountId(20L).incomeFromChargeOffPenaltyAccountId(21L).build()),
                null, null));
        cases.add(new Case("advanced payment allocation",
                () -> new LoanProductTestBuilder().withRepaymentStrategy(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                        .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                        .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                        .addAdvancedPaymentAllocation(defaultAllocation()),
                null, null));
        cases.add(new Case("credit allocations",
                () -> new LoanProductTestBuilder().withRepaymentStrategy(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                        .addAdvancedPaymentAllocation(defaultAllocation()).addCreditAllocations(chargebackAllocation()),
                null, null));
        cases.add(new Case("down payment", () -> new LoanProductTestBuilder().withEnableDownPayment(true, "25", true), null, null));
        cases.add(new Case("down payment no auto repayment", () -> new LoanProductTestBuilder().withEnableDownPayment(true, "12.5", false),
                null, null));
        cases.add(new Case("interest recalculation", () -> new LoanProductTestBuilder()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_INTEREST,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_REST_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_MONTHLY, "1",
                        null, null),
                null, null));
        cases.add(
                new Case("days in month/year", () -> new LoanProductTestBuilder().withDaysInMonth("30").withDaysInYear("365"), null, null));
        cases.add(new Case("supported interest refund types",
                () -> new LoanProductTestBuilder().withSupportedInterestRefundTypes("PAYOUT_REFUND", "MERCHANT_ISSUED_REFUND"), null,
                null));
        cases.add(new Case("short name", () -> new LoanProductTestBuilder().withShortName("ABCD"), null, null));
        cases.add(new Case("null number of repayments", () -> new LoanProductTestBuilder().withNumberOfRepayments(null), null, null));
        cases.add(new Case("null interest rate", () -> new LoanProductTestBuilder().withinterestRatePerPeriod(null), null, null));
        cases.add(new Case("down payment with null percentage", () -> new LoanProductTestBuilder().withEnableDownPayment(true, null, false),
                null, null));
        cases.add(new Case("down payment percentage with six decimals",
                () -> new LoanProductTestBuilder().withEnableDownPayment(true, "12.55555555", false), null, null));
        cases.add(new Case("auto repayment without down payment",
                () -> new LoanProductTestBuilder().withEnableDownPayment(false, null, true), null, null));
        cases.add(new Case("in arrears tolerance", () -> new LoanProductTestBuilder().withInArrearsTolerance("1001"), null, null));
        cases.add(new Case("no accounting accounts", () -> new LoanProductTestBuilder().withAccounting("1", null), null, null));
        return cases;
    }

    @TestFactory
    List<DynamicTest> mapAndTypedRequestAgree() {
        return cases().stream().map(testCase -> DynamicTest.dynamicTest(testCase.name(), () -> {
            LoanProductTestBuilder builder = testCase.builder().get();
            String json = new com.google.gson.Gson().toJson(builder.build(testCase.chargeId(), testCase.delinquencyBucketId()));
            String sanitized = json.replaceAll("(?<=\\d),(?=\\d{3}(?!\\d))", "");
            PostLoanProductsRequest fromJson = ObjectMapperFactory.getShared().readValue(sanitized, PostLoanProductsRequest.class);
            PostLoanProductsRequest typed = builder.buildRequest(testCase.chargeId(), testCase.delinquencyBucketId());
            assertEquals(ObjectMapperFactory.getShared().writeValueAsString(fromJson),
                    ObjectMapperFactory.getShared().writeValueAsString(typed), testCase.name());
        })).toList();
    }
}
