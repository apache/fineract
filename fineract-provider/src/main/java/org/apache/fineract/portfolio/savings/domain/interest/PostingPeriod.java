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
package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class PostingPeriod {
    
    private final LocalDateInterval periodInterval;
    private final MonetaryCurrency currency;
    private final SavingsCompoundingInterestPeriodType interestCompoundingType;
    private final BigDecimal interestRateAsFraction;
    private final long daysInYear;
    private final List<CompoundingPeriod> compoundingPeriods;

    // interest posting details
    private final LocalDate dateOfPostingTransaction;
    private BigDecimal interestEarnedUnrounded;
    private Money interestEarnedRounded;

    // opening/closing details
    private final Money openingBalance;
    private final Money closingBalance;
    private final SavingsInterestCalculationType interestCalculationType;

    // include in compounding interest
    private boolean interestTransfered = false;
    private boolean isUserPosting = false;

    // minimum balance for interest calculation
    private final Money minBalanceForInterestCalculation;
	private BigDecimal overdraftInterestRateAsFraction;
	private Money minOverdraftForInterestCalculation;
 
    private Integer financialYearBeginningMonth;

    public static PostingPeriod createFrom(final LocalDateInterval periodInterval, final Money periodStartingBalance,
            final List<SavingsAccountTransaction> orderedListOfTransactions, final MonetaryCurrency currency,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final LocalDate upToInterestCalculationDate, Collection<Long> interestPostTransactions, boolean isInterestTransfer,
            final Money minBalanceForInterestCalculation, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,final boolean isUserPosting, Integer financialYearBeginningMonth) {

    	final BigDecimal overdraftInterestRateAsFraction = BigDecimal.ZERO;
    	final Money minOverdraftForInterestCalculation = Money.zero(currency);
    	
    	return createFrom(periodInterval, periodStartingBalance, orderedListOfTransactions, currency, 
    			interestCompoundingPeriodType, interestCalculationType, interestRateAsFraction, daysInYear, 
    			upToInterestCalculationDate, interestPostTransactions, isInterestTransfer, 
    			minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, 
    			overdraftInterestRateAsFraction, minOverdraftForInterestCalculation, isUserPosting, financialYearBeginningMonth);
    }

    // isInterestTransfer boolean is to identify newly created transaction is
    // interest transfer
    public static PostingPeriod createFrom(final LocalDateInterval periodInterval, final Money periodStartingBalance,
            final List<SavingsAccountTransaction> orderedListOfTransactions, final MonetaryCurrency currency,
            final SavingsCompoundingInterestPeriodType interestCompoundingPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final LocalDate upToInterestCalculationDate, Collection<Long> interestPostTransactions, boolean isInterestTransfer,
            final Money minBalanceForInterestCalculation, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final BigDecimal overdraftInterestRateAsFraction, final Money minOverdraftForInterestCalculation, boolean isUserPosting, int financialYearBeginningMonth) {

        final List<EndOfDayBalance> accountEndOfDayBalances = new ArrayList<>();
        boolean interestTransfered = false;
        Money openingDayBalance = periodStartingBalance;
        Money closeOfDayBalance = openingDayBalance;
        for (final SavingsAccountTransaction transaction : orderedListOfTransactions) {

            if (transaction.fallsWithin(periodInterval)) {
                // the balance of the transaction falls entirely within this
                // period so no need to do any cropping/bounding
                final EndOfDayBalance endOfDayBalance = transaction.toEndOfDayBalance(openingDayBalance);
                accountEndOfDayBalances.add(endOfDayBalance);

                openingDayBalance = endOfDayBalance.closingBalance();

            } else if (transaction.spansAnyPortionOf(periodInterval)) {
                final EndOfDayBalance endOfDayBalance = transaction.toEndOfDayBalanceBoundedBy(openingDayBalance, periodInterval);
                accountEndOfDayBalances.add(endOfDayBalance);

                closeOfDayBalance = endOfDayBalance.closingBalance();
                openingDayBalance = closeOfDayBalance;
            }

            // this check is to make sure to add interest if withdrawal is
            // happened for already
            if (transaction.occursOn(periodInterval.endDate().plusDays(1))) {
                if (transaction.getId() == null) {
                    interestTransfered = isInterestTransfer;
                } else if (interestPostTransactions.contains(transaction.getId())) {
                    interestTransfered = true;
                }
            }

        }

        if (accountEndOfDayBalances.isEmpty()) {
            LocalDate balanceStartDate = periodInterval.startDate();
            LocalDate balanceEndDate = periodInterval.endDate();
            Integer numberOfDaysOfBalance = periodInterval.daysInPeriodInclusiveOfEndDate();

            if (balanceEndDate.isAfter(upToInterestCalculationDate)) {
                balanceEndDate = upToInterestCalculationDate;
                final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
                numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
            }

            final EndOfDayBalance endOfDayBalance = EndOfDayBalance.from(balanceStartDate, openingDayBalance, closeOfDayBalance,
                    numberOfDaysOfBalance);

            accountEndOfDayBalances.add(endOfDayBalance);

            closeOfDayBalance = endOfDayBalance.closingBalance();
            openingDayBalance = closeOfDayBalance;
        }

        final List<CompoundingPeriod> compoundingPeriods = compoundingPeriodsInPostingPeriod(periodInterval, interestCompoundingPeriodType,
                accountEndOfDayBalances, upToInterestCalculationDate, financialYearBeginningMonth);

        return new PostingPeriod(periodInterval, currency, periodStartingBalance, openingDayBalance, interestCompoundingPeriodType,
                interestCalculationType, interestRateAsFraction, daysInYear, compoundingPeriods, interestTransfered,
                minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd,
                overdraftInterestRateAsFraction, minOverdraftForInterestCalculation, isUserPosting, financialYearBeginningMonth);
    }

    private PostingPeriod(final LocalDateInterval periodInterval, final MonetaryCurrency currency, final Money openingBalance,
            final Money closingBalance, final SavingsCompoundingInterestPeriodType interestCompoundingType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestRateAsFraction, final long daysInYear,
            final List<CompoundingPeriod> compoundingPeriods, boolean interestTransfered, final Money minBalanceForInterestCalculation,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final BigDecimal overdraftInterestRateAsFraction, 
            final Money minOverdraftForInterestCalculation, boolean isUserPosting, Integer financialYearBeginningMonth) {
        this.periodInterval = periodInterval;
        this.currency = currency;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.interestCompoundingType = interestCompoundingType;
        this.interestCalculationType = interestCalculationType;
        this.interestRateAsFraction = interestRateAsFraction;
        this.daysInYear = daysInYear;
        this.compoundingPeriods = compoundingPeriods;

        if (isSavingsInterestPostingAtCurrentPeriodEnd)
            this.dateOfPostingTransaction = periodInterval.endDate();
        else
            this.dateOfPostingTransaction = periodInterval.endDate().plusDays(1);
        this.interestTransfered = interestTransfered;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
        this.overdraftInterestRateAsFraction = overdraftInterestRateAsFraction;
        this.minOverdraftForInterestCalculation = minOverdraftForInterestCalculation;
        this.isUserPosting = isUserPosting;
        this.financialYearBeginningMonth = financialYearBeginningMonth;
    }

    public Money interest() {
        return this.interestEarnedRounded;
    }

    public LocalDate dateOfPostingTransaction() {
        return this.dateOfPostingTransaction;
    }

    public Money closingBalance() {
        return this.closingBalance;
    }

    public Money openingBalance() {
        return this.openingBalance;
    }

    public BigDecimal calculateInterest(final CompoundInterestValues compoundInterestValues) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for each compounding period accumulate the amount of interest
        // to be applied to the balanced for interest calculation
        for (final CompoundingPeriod compoundingPeriod : this.compoundingPeriods) {

            final BigDecimal interestUnrounded = compoundingPeriod.calculateInterest(this.interestCompoundingType,
                    this.interestCalculationType, compoundInterestValues.getcompoundedInterest(), this.interestRateAsFraction, this.daysInYear,
                    this.minBalanceForInterestCalculation.getAmount(), 	this.overdraftInterestRateAsFraction,
                    this.minOverdraftForInterestCalculation.getAmount());
			BigDecimal unCompoundedInterest = compoundInterestValues.getuncompoundedInterest().add(interestUnrounded);
			compoundInterestValues.setuncompoundedInterest(unCompoundedInterest);
			LocalDate compoundingPeriodEndDate = compoundingPeriod.getPeriodInterval().endDate();
			if (!SavingsCompoundingInterestPeriodType.DAILY.equals(this.interestCompoundingType)) {
				compoundingPeriodEndDate = determineInterestPeriodEndDateFrom(
						compoundingPeriod.getPeriodInterval().startDate(), this.interestCompoundingType,
						compoundingPeriod.getPeriodInterval().endDate(), this.getFinancialYearBeginningMonth());
			}

			if (compoundingPeriodEndDate.equals(compoundingPeriod.getPeriodInterval().endDate())) {
				BigDecimal interestCompounded = compoundInterestValues.getcompoundedInterest()
						.add(unCompoundedInterest);
				compoundInterestValues.setcompoundedInterest(interestCompounded);
				compoundInterestValues.setZeroForInterestToBeUncompounded();
			}
            interestEarned = interestEarned.add(interestUnrounded);
        }

        this.interestEarnedUnrounded = interestEarned;
        this.interestEarnedRounded = Money.of(this.currency, this.interestEarnedUnrounded);

        return interestEarned;
    }

    public Money getInterestEarned() {
        return this.interestEarnedRounded;
    }

    private static List<CompoundingPeriod> compoundingPeriodsInPostingPeriod(final LocalDateInterval postingPeriodInterval,
            final SavingsCompoundingInterestPeriodType interestPeriodType, final List<EndOfDayBalance> allEndOfDayBalances,
            final LocalDate upToInterestCalculationDate, int financialYearBeginningMonth) {

        final List<CompoundingPeriod> compoundingPeriods = new ArrayList<>();

        CompoundingPeriod compoundingPeriod = null;
        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                compoundingPeriod = DailyCompoundingPeriod.create(postingPeriodInterval, allEndOfDayBalances, upToInterestCalculationDate);
                compoundingPeriods.add(compoundingPeriod);
            break;
            case MONTHLY:

                final LocalDate postingPeriodEndDate = postingPeriodInterval.endDate();

                LocalDate periodStartDate = postingPeriodInterval.startDate();
                LocalDate periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(postingPeriodEndDate) && !periodEndDate.isAfter(postingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate, financialYearBeginningMonth);
                    if (periodEndDate.isAfter(postingPeriodEndDate)) {
                        periodEndDate = postingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = MonthlyCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            // case WEEKLY:
            // break;
            // case BIWEEKLY:
            // break;
            case QUATERLY:
                final LocalDate qPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(qPostingPeriodEndDate) && !periodEndDate.isAfter(qPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate, financialYearBeginningMonth);
                    if (periodEndDate.isAfter(qPostingPeriodEndDate)) {
                        periodEndDate = qPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = QuarterlyCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            case BI_ANNUAL:
                final LocalDate bPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(bPostingPeriodEndDate) && !periodEndDate.isAfter(bPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate, financialYearBeginningMonth);
                    if (periodEndDate.isAfter(bPostingPeriodEndDate)) {
                        periodEndDate = bPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = BiAnnualCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
            case ANNUAL:
                final LocalDate aPostingPeriodEndDate = postingPeriodInterval.endDate();

                periodStartDate = postingPeriodInterval.startDate();
                periodEndDate = periodStartDate;

                while (!periodStartDate.isAfter(aPostingPeriodEndDate) && !periodEndDate.isAfter(aPostingPeriodEndDate)) {

                    periodEndDate = determineInterestPeriodEndDateFrom(periodStartDate, interestPeriodType, upToInterestCalculationDate, financialYearBeginningMonth);
                    if (periodEndDate.isAfter(aPostingPeriodEndDate)) {
                        periodEndDate = aPostingPeriodEndDate;
                    }

                    final LocalDateInterval compoundingPeriodInterval = LocalDateInterval.create(periodStartDate, periodEndDate);
                    if (postingPeriodInterval.contains(compoundingPeriodInterval)) {

                        compoundingPeriod = AnnualCompoundingPeriod.create(compoundingPeriodInterval, allEndOfDayBalances,
                                upToInterestCalculationDate);
                        compoundingPeriods.add(compoundingPeriod);
                    }

                    // move periodStartDate forward to day after this period
                    periodStartDate = periodEndDate.plusDays(1);
                }
            break;
        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // break;
        }

        return compoundingPeriods;
    }

    private static LocalDate determineInterestPeriodEndDateFrom(final LocalDate periodStartDate,
            final SavingsCompoundingInterestPeriodType interestPeriodType, final LocalDate upToInterestCalculationDate, int financialYearBeginningMonth) {

        LocalDate periodEndDate = upToInterestCalculationDate;
        int previousMonth = financialYearBeginningMonth-1;
        if(previousMonth==0){
            previousMonth = 12;
        }
        int periodsInMonth = 1;
        switch (interestPeriodType) {
            case INVALID:
            break;
            case DAILY:
                periodEndDate = periodStartDate;
            break;
            // case WEEKLY:
            // periodEndDate = periodStartDate.dayOfWeek().withMaximumValue();
            // break;
            // case BIWEEKLY:
            // final LocalDate closestEndOfWeek =
            // periodStartDate.dayOfWeek().withMaximumValue();
            // periodEndDate = closestEndOfWeek.plusWeeks(1);
            // break;
            case MONTHLY:
                // produce period end date on last day of current month
                periodEndDate = periodStartDate.dayOfMonth().withMaximumValue();
            break;
            case QUATERLY:
                periodsInMonth = 4;
                periodEndDate = getPeriodEndDate(periodEndDate, previousMonth, periodsInMonth, periodStartDate);
            break;
            case BI_ANNUAL:                
                periodsInMonth = 2;
                periodEndDate = getPeriodEndDate(periodEndDate, previousMonth, periodsInMonth, periodStartDate);
                
            break;
            case ANNUAL:
                periodEndDate = periodStartDate.withMonthOfYear(previousMonth);
                periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
                if(periodEndDate.isBefore(periodStartDate)){
                    periodEndDate = periodEndDate.plusYears(1);
                }
            break;

        // case NO_COMPOUNDING_SIMPLE_INTEREST:
        // periodEndDate = periodStartDate.monthOfYear().withMaximumValue();
        // periodEndDate = periodEndDate.dayOfMonth().withMaximumValue();
        // break;
        }

        return periodEndDate;
    }

    private static LocalDate getPeriodEndDate(LocalDate periodEndDate, int previousMonth, int periodsInMonth, LocalDate periodStartDate) {
        int year = periodStartDate.getYearOfEra();
        int monthofYear = periodStartDate.getMonthOfYear();
        LocalDate date = DateUtils.getLocalDateOfTenant();
        TreeSet<Integer> monthSet = new TreeSet<>();
        date = date.withMonthOfYear(previousMonth);
        monthSet.add(date.getMonthOfYear());
        int count =0;
        while(count<(periodsInMonth-1)){
            date = date.plusMonths((12/periodsInMonth));
            monthSet.add(date.getMonthOfYear());
            count++;
        }
        boolean notInRange = true;
        /*
         * if strt date is 2016-10-05
         * if financial year set to 4 then applicable month will be march and september that is (3,9)
         * if start date fall in month of oct,nov or dec then month will be 10, 11 or 12
         * so period end date should be taken from next year for march month
         */
        
        for (Integer month : monthSet) {
            if(monthofYear<=month.intValue()){
                periodEndDate = new DateTime().withDate(year, month, DateUtils.getLocalDateOfTenant().withMonthOfYear(month).dayOfMonth().withMaximumValue().getDayOfMonth()).toLocalDate();
                notInRange = false;
                break;
            }
        }
        if(notInRange){
            periodEndDate = new DateTime().withDate(year+1, monthSet.first(), DateUtils.getLocalDateOfTenant().withMonthOfYear(monthSet.first()).dayOfMonth().withMaximumValue().getDayOfMonth()).toLocalDate();
        }
        return periodEndDate;
    }

    public boolean isInterestTransfered() {
        return this.interestTransfered;
    }

    public LocalDateInterval getPeriodInterval() {
        return this.periodInterval;
    }
    
    public boolean isUserPosting() {
        return this.isUserPosting;
    }

    
    public Integer getFinancialYearBeginningMonth() {
        return this.financialYearBeginningMonth;
    }
    
    
}