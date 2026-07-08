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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.DeleteFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanChargePaymentWithAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern(DATETIME_PATTERN).toFormatter();

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignSavingsTransactionHelper savingsTransactionHelper;
    private static FinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupSavingsAndFinancialActivityHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        financialActivityAccountHelper = new FinancialActivityAccountHelper(null);
    }

    @AfterAll
    public static void tearDownFinancialActivityAccounts() {
        List<GetFinancialActivityAccountsResponse> financialActivities = financialActivityAccountHelper.getAllFinancialActivityAccounts();
        for (GetFinancialActivityAccountsResponse financialActivity : financialActivities) {
            DeleteFinancialActivityAccountsResponse deletedFinancialActivityAccount = financialActivityAccountHelper
                    .deleteFinancialActivityAccount(financialActivity.getId());
            Assertions.assertNotNull(deletedFinancialActivityAccount);
            Assertions.assertEquals(financialActivity.getId(), deletedFinancialActivityAccount.getResourceId());
        }
    }

    @Test
    public void feeAndPenaltyChargePaymentWithDefaultAllocationRuleTest() {
        runAt("15 February 2023", () -> {
            final String jobName = "Transfer Fee For Loans From Savings";
            final String startDate = "10 April 2022";

            Long clientId = createClient();
            Long loanProductId = createLoanProduct("500", "15", "4");

            Long savingsId = createSavingsAccountDailyPosting(clientId, startDate);
            savingsTransactionHelper.deposit(savingsId, "10000", startDate);

            mapLiabilityTransferFinancialActivity();

            Long loanId = applyForLoanApplication(clientId, loanProductId, savingsId, 1000L, 45, 15, 3, BigDecimal.ZERO, "01 January 2023",
                    "01 January 2023");

            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en"));

            final double feePortion = 50.0d;
            final double penaltyPortion = 100.0d;

            Long fee = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(feePortion, false))
                    .getResourceId();
            Long penalty = chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateAccountTransferFee(penaltyPortion, true))
                    .getResourceId();

            LocalDate targetDate = LocalDate.of(2023, 1, 3);
            final String penaltyChargeAddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, fee, penaltyChargeAddedDate, feePortion);
            addLoanCharge(loanId, penalty, penaltyChargeAddedDate, penaltyPortion);

            verifyNoAccrualTransactionForRepayment(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(feePortion,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(penaltyPortion,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(penaltyPortion,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(400.0d, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalDueForPeriod()));
            assertEquals(400.0d,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 16), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());

            schedulerHelper.executeAndAwaitJob(jobName);

            loanDetails = getLoanDetails(loanId);
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue()));
            assertEquals(0.0d, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding()));
            assertEquals(penaltyPortion,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue()));
            assertEquals(0.0d, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding()));
            assertEquals(400.0d, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalDueForPeriod()));
            assertEquals(250.0d,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 16), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
        });
    }

    private Long createSavingsAccountDailyPosting(final Long clientId, String startDate) {
        final Long savingsProductId = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductId);
        final Long savingsId = savingsHelper.createApproveActivateSavings(clientId, savingsProductId, startDate);
        Assertions.assertNotNull(savingsId);
        return savingsId;
    }

    private Long createSavingsProductDailyPosting() {
        return savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(new BigDecimal("10000.0")))
                .getResourceId();
    }

    private void mapLiabilityTransferFinancialActivity() {
        financialActivityAccountHelper.getAllFinancialActivityAccounts()
                .forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));
        Long liabilityAccountId = getAccounts().getOverpaymentAccount().getAccountID().longValue();
        PostFinancialActivityAccountsResponse response = financialActivityAccountHelper
                .createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId((long) LIABILITY_TRANSFER.getValue()).glAccountId(liabilityAccountId));
        Assertions.assertNotNull(response.getResourceId());
    }

    private Long createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments) {
        Account assetAccount = getAccounts().getLoansReceivableAccount();
        Account incomeAccount = getAccounts().getInterestIncomeAccount();
        Account expenseAccount = getAccounts().getChargeOffExpenseAccount();
        Account overpaymentAccount = getAccounts().getOverpaymentAccount();

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
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .addAdvancedPaymentAllocation(defaultAllocation, goodwillCreditAllocation, merchantIssuedRefundAllocation,
                        payoutRefundAllocation)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return createLoanProductFromJson(loanProductJSON);
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final Long savingsId, final Long principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000.00").withLoanTermFrequency("45")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("3").withRepaymentEveryAfter("15").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0")
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()).withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(expectedDisbursementDate).withSubmittedOnDate(submittedOnDate)
                .build(clientId.toString(), loanProductId.toString(), savingsId.toString());
        return applyForLoanFromJson(loanApplicationJSON);
    }

    private void verifyNoAccrualTransactionForRepayment(Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        loanDetails.getTransactions().forEach(
                transaction -> assertFalse(Boolean.TRUE.equals(transaction.getType().getAccrual()), "Accrual entries are posted!"));
    }
}
