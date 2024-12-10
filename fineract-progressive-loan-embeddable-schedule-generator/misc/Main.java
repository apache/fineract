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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanDisbursementPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.EmbeddableProgressiveLoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleModelData;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

/*
 * NOTE: This file used by a CI job to test Progressive Loan Embedded Jar compiled successfully and runs smoothly.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        EmbeddableProgressiveLoanScheduleGenerator calculator = new EmbeddableProgressiveLoanScheduleGenerator();

        final CurrencyData currency = new CurrencyData("usd", "US Dollar", 2, null, "usd", "$");
        final LocalDate startDate = LocalDate.of(2024, 1, 1);
        final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);
        final BigDecimal disbursedAmount = BigDecimal.valueOf(100);

        final int noRepayments = 6;
        final int repaymentFrequency = 1;
        final String repaymentFrequencyType = "MONTHS";
        final BigDecimal downPaymentPercentage = args.length > 0 ? new BigDecimal(args[0]) : BigDecimal.ZERO;
        final boolean isDownPaymentEnabled = BigDecimal.ZERO.compareTo(downPaymentPercentage) != 0;
        final BigDecimal annualNominalInterestRate = BigDecimal.valueOf(7.0);
        final DaysInMonthType daysInMonthType = DaysInMonthType.DAYS_30;
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_360;
        final Integer installmentAmountInMultiplesOf = null;
        final Integer fixedLength = null;

        var config = new LoanRepaymentScheduleModelData(startDate, currency, disbursedAmount, disbursementDate, noRepayments, repaymentFrequency, repaymentFrequencyType, annualNominalInterestRate, isDownPaymentEnabled, daysInMonthType, daysInYearType, downPaymentPercentage, installmentAmountInMultiplesOf, fixedLength);

        final LoanSchedulePlan plan = calculator.generate(mc, config);
        printPlan(plan);
    }

    static void printPlan(final LoanSchedulePlan plan) throws InterruptedException {
        System.out.println("#------ Loan Schedule -----------------#");
        System.out.printf("  Number of Periods: %d%n", plan.getPeriods().stream().filter(period -> !(period instanceof LoanSchedulePlanDisbursementPeriod)).count());
        System.out.printf("  Loan Term in Days: %d%n", plan.getLoanTermInDays());
        System.out.printf("  Total Disbursed Amount: %s%n", plan.getTotalDisbursedAmount());
        System.out.printf("  Total Interest Amount: %s%n", plan.getTotalInterestAmount());
        System.out.printf("  Total Repayment Amount: %s%n", plan.getTotalRepaymentAmount());
        System.out.println("#------ Repayment Schedule ------------#");

        for (LoanSchedulePlanPeriod period : plan.getPeriods()) {
            if (period instanceof LoanSchedulePlanDisbursementPeriod dp) {
                System.out.printf("  Disbursement - Date: %s, Amount: %s%n", dp.periodDueDate(), dp.getPrincipalAmount());
            } if (period instanceof LoanSchedulePlanDownPaymentPeriod rp) {
                System.out.printf("  Down payment Period: #%d, Due Date: %s, Balance: %s, Principal: %s, Total: %s%n", rp.periodNumber(), rp.periodDueDate(), rp.getOutstandingLoanBalance(), rp.getPrincipalAmount(), rp.getTotalDueAmount());
            } if (period instanceof LoanSchedulePlanRepaymentPeriod rp) {
                System.out.printf("  Repayment Period: #%d, Due Date: %s, Balance: %s, Principal: %s, Interest: %s, Total: %s%n", rp.periodNumber(), rp.periodDueDate(), rp.getOutstandingLoanBalance(), rp.getPrincipalAmount(), rp.getInterestAmount(), rp.getTotalDueAmount());
            }
        }
    }
}
