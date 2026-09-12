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
package org.apache.fineract.portfolio.loanproduct.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanInterestScheduleModelParserServiceGsonImpl;
import org.apache.fineract.portfolio.loanproduct.calc.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReAgedChargebackPayoffTest {

    private static final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator(new DefaultScheduledDateGenerator());
    private static MockedStatic<ThreadLocalContextUtil> threadLocalContextUtil;
    private static MockedStatic<MoneyHelper> moneyHelper;
    private static final MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final CurrencyData currency = new CurrencyData("EUR", "EUR", 2, 1, "E", "EUR");
    private static final ILoanConfigurationDetails detail = Mockito.mock(ILoanConfigurationDetails.class);
    private final ProgressiveLoanInterestScheduleModelParserServiceGsonImpl interestScheduleModelService = new ProgressiveLoanInterestScheduleModelParserServiceGsonImpl();

    @BeforeAll
    static void init() {
        threadLocalContextUtil = Mockito.mockStatic(ThreadLocalContextUtil.class);
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void tearDown() {
        threadLocalContextUtil.close();
        moneyHelper.close();
    }

    @BeforeEach
    void setupDefaults() {
        Mockito.lenient().when(detail.isInterestRecognitionOnDisbursementDate()).thenReturn(false);
        Mockito.lenient().when(detail.getDaysInYearCustomStrategy()).thenReturn(null);
        Mockito.lenient().when(detail.getCurrencyData()).thenReturn(currency);
        Mockito.lenient().when(detail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        Mockito.lenient().when(detail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
        Mockito.lenient().when(detail.isAllowPartialPeriodInterestCalculation()).thenReturn(false);
        Mockito.lenient().when(detail.getGraceOnPrincipalPayment()).thenReturn(0);
        Mockito.lenient().when(detail.getGraceOnInterestPayment()).thenReturn(0);
        Mockito.lenient().when(detail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(9.99));
        Mockito.lenient().when(detail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.lenient().when(detail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.lenient().when(detail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.lenient().when(detail.getRepayEvery()).thenReturn(1);
        Mockito.lenient().when(detail.isInterestRecalculationEnabled()).thenReturn(true);
    }

    /**
     * Persisted interest schedule model of a loan that was re-aged with EQUAL_AMORTIZATION_PAYABLE_INTEREST, then
     * received an additional disbursement and a chargeback of 150 on the same day. The chargeback lives as
     * creditedPrincipal on the first re-aged period (2026-06-10 - 2026-07-10), whose EMI is 30.33.
     */
    private static final String PRE_PAYOFF_MODEL = """
                {"repaymentPeriods":[{"fromDate":"2026-04-21","dueDate":"2026-05-21","interestPeriods":[{"fromDate":"2026-04-21","dueDate":"2026-04-21","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.008325,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":300.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":0.0,"capitalizedIncomePrincipal":0.0,"isPaused":false},
                {"fromDate":"2026-04-21","dueDate":"2026-05-21","rateFactor":0.008325,"rateFactorTillPeriodDueDate":0.008325,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":-147.5,"outstandingLoanBalance":300.0,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":101.67,"originalEmi":101.67,"paidPrincipal":99.17,"paidInterest":2.5,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":300.0,"creditedPrincipalMovedDueReAge":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":true,"reAged":false,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.0},
                {"fromDate":"2026-05-21","dueDate":"2026-06-10","interestPeriods":[{"fromDate":"2026-05-21","dueDate":"2026-06-10","rateFactor":0.008325,"rateFactorTillPeriodDueDate":0.005370967741935484,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":150.0,"balanceCorrectionAmount":150.0,"outstandingLoanBalance":152.5,"capitalizedIncomePrincipal":0.0,"isPaused":false},
                {"fromDate":"2026-06-10","dueDate":"2026-06-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":452.5,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":48.33,"originalEmi":101.67,"paidPrincipal":48.33,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":300.0,"creditedPrincipalMovedDueReAge":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":true,"reAged":true,"reAgedEarlyRepaymentHolder":true,"fixedInterest":0.0},
                {"fromDate":"2026-06-10","dueDate":"2026-07-10","interestPeriods":[{"fromDate":"2026-06-10","dueDate":"2026-07-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":150.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":452.5,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-07-10","dueDate":"2026-08-10","interestPeriods":[{"fromDate":"2026-07-10","dueDate":"2026-08-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":272.25,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-08-10","dueDate":"2026-09-10","interestPeriods":[{"fromDate":"2026-08-10","dueDate":"2026-09-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":242.0,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-09-10","dueDate":"2026-10-10","interestPeriods":[{"fromDate":"2026-09-10","dueDate":"2026-10-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":211.75,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-10-10","dueDate":"2026-11-10","interestPeriods":[{"fromDate":"2026-10-10","dueDate":"2026-11-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":181.5,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-11-10","dueDate":"2026-12-10","interestPeriods":[{"fromDate":"2026-11-10","dueDate":"2026-12-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":151.25,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2026-12-10","dueDate":"2027-01-10","interestPeriods":[{"fromDate":"2026-12-10","dueDate":"2027-01-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":121.0,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2027-01-10","dueDate":"2027-02-10","interestPeriods":[{"fromDate":"2027-01-10","dueDate":"2027-02-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":90.75,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2027-02-10","dueDate":"2027-03-10","interestPeriods":[{"fromDate":"2027-02-10","dueDate":"2027-03-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":60.5,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.33,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.08},
                {"fromDate":"2027-03-10","dueDate":"2027-04-10","interestPeriods":[{"fromDate":"2027-03-10","dueDate":"2027-04-10","rateFactor":0.0,"rateFactorTillPeriodDueDate":0.0,"creditedPrincipal":0.0,"creditedInterest":0.0,"disbursementAmount":0.0,"balanceCorrectionAmount":0.0,"outstandingLoanBalance":30.25,"capitalizedIncomePrincipal":0.0,"isPaused":false}],"emi":30.35,"originalEmi":30.33,"paidPrincipal":0.0,"paidInterest":0.0,"futureUnrecognizedInterest":0.0,"isInterestMovedUpward":false,"interestPaymentGrace":false,"totalDisbursedAmount":450.0,"totalCapitalizedIncomeAmount":0.0,"creditedInterestMovedDueReAge":0.0,"isInterestMovedDownward":false,"reAged":true,"reAgedEarlyRepaymentHolder":false,"fixedInterest":0.1}],"interestRates":[{"effectiveFrom":"2026-06-10","interestRate":0}],"modifiers":{"COPY":false,"EMI_RECALCULATION":true,"INTEREST_PAUSE_FOR_EMI_CALCULATION":false,"INTEREST_RECALCULATION_ENABLED":true},"lastOverdueBalanceChange":"2026-06-10","overdueCorrections":[{"correctionDate":"2026-05-21","amount":0.0,"affectedRpDueDate":"2026-05-21"},
                {"correctionDate":"2026-06-10","amount":0.0,"affectedRpDueDate":"2026-06-21"}]}
            """;

    @Test
    void payOffOfReAgedScheduleMustNotCountTheChargebackTwice() {
        final ProgressiveLoanInterestScheduleModel model = interestScheduleModelService.fromJson(PRE_PAYOFF_MODEL, detail, mc, null);
        Assertions.assertNotNull(model);
        final LocalDate payOffDate = LocalDate.of(2026, 6, 10);

        // processLatestTransaction recalculates the overdue amounts for the transaction date before allocating,
        // flagging the transaction as a pre-payment attempt
        emiCalculator.recalculateModelOverdueAmountsTillDate(model, payOffDate, true);

        // pay-off of the whole outstanding amount, allocated like the processor does with NEXT_INSTALLMENT
        Money unprocessed = Money.of(currency, BigDecimal.valueOf(453.32), mc);
        int guard = 0;
        while (unprocessed.isGreaterThanZero() && guard++ < 40) {
            // the loan keeps the LAST_INSTALLMENT rule it copied at creation time, so the allocation walks the
            // schedule backwards and the charged back period is the last one to be paid
            RepaymentPeriod target = null;
            for (RepaymentPeriod rp : model.repaymentPeriods()) {
                if (rp.getDueDate().isAfter(payOffDate) && !rp.isFullyPaid()) {
                    target = rp;
                }
            }
            if (target == null) {
                break;
            }
            // restFrequency DAILY + preCloseInterestCalculationStrategy TILL_PRE_CLOSURE_DATE: the processor pays
            // every installment with the transaction date, not with the installment due date
            final LocalDate payDate = payOffDate;
            final PeriodDueDetails due = emiCalculator.getDueAmounts(model, target.getFromDate(), target.getDueDate(), payDate);

            final Money principalToPay = min(due.getDuePrincipal().minus(target.getPaidPrincipal()), unprocessed);
            if (principalToPay.isGreaterThanZero()) {
                emiCalculator.payPrincipal(model, target.getFromDate(), target.getDueDate(), payDate, principalToPay);
                unprocessed = unprocessed.minus(principalToPay);
            }
            final Money interestToPay = min(due.getDueInterest().minus(target.getPaidInterest()), unprocessed);
            if (interestToPay.isGreaterThanZero()) {
                emiCalculator.payInterest(model, target.getFromDate(), target.getDueDate(), payDate, interestToPay);
                unprocessed = unprocessed.minus(interestToPay);
            }
            if (!principalToPay.isGreaterThanZero() && !interestToPay.isGreaterThanZero()) {
                break;
            }
        }

        final RepaymentPeriod chargedBackPeriod = model
                .findRepaymentPeriodByFromAndDueDate(LocalDate.of(2026, 6, 10), LocalDate.of(2026, 7, 10)).orElseThrow();
        // The period carries EMI 30.33 plus the 150 chargeback: 180.25 principal + 0.08 interest. Setting its EMI to
        // the total paid amount without removing the credited amounts counts the chargeback twice (330.25) and leaves
        // the installment 150 short after a full pay-off.
        Assertions.assertEquals(0, chargedBackPeriod.getEmi().getAmount().compareTo(BigDecimal.valueOf(30.33)),
                "EMI of the re-aged period must not absorb the paid chargeback, was " + chargedBackPeriod.getEmi());
        Assertions.assertEquals(0, chargedBackPeriod.getDuePrincipal().getAmount().compareTo(BigDecimal.valueOf(180.25)),
                "due principal must stay EMI + credited principal - due interest, was " + chargedBackPeriod.getDuePrincipal());
        Assertions.assertTrue(chargedBackPeriod.isFullyPaid(), "the period must be fully paid after the pay-off");
    }

    private static Money min(Money a, Money b) {
        return a.isGreaterThan(b) ? b : a;
    }
}
