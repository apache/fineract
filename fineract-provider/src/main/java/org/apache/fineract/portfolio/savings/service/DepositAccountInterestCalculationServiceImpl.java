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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsInterestCalculationUtil;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;

/**
 * Default implementation of {@link DepositAccountInterestCalculationService}. The method bodies were extracted from
 * {@link FixedDepositAccount} and {@link RecurringDepositAccount}; behaviour is intentionally unchanged, including the
 * differences between both deposit types (start date of the posting periods, user-posting detection and when the
 * interest period summaries are stored on the account).
 */
@RequiredArgsConstructor
public class DepositAccountInterestCalculationServiceImpl implements DepositAccountInterestCalculationService {

    private final SavingsHelper savingsHelper;
    private final SavingsAccountInterestCalculationService savingsAccountInterestCalculationService;

    @Override
    public void updateMaturityDateAndAmountBeforeAccountActivation(final FixedDepositAccount account, final MathContext mc,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        List<SavingsAccountTransaction> allTransactions = new ArrayList<>();
        String refNo = null;
        final Money transactionAmountMoney = Money.of(account.getCurrency(), account.getDepositAmount());
        final SavingsAccountTransaction transaction = SavingsAccountTransaction.deposit(null, account.office(), null,
                account.accountSubmittedOrActivationDate(), transactionAmountMoney, refNo);
        transaction.setRunningBalance(transactionAmountMoney);
        transaction.updateCumulativeBalanceAndDates(account.getCurrency(), account.interestCalculatedUpto());
        allTransactions.add(transaction);
        updateMaturityDateAndAmount(account, mc, allTransactions, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
    }

    @Override
    public void updateMaturityDateAndAmount(final FixedDepositAccount account, final MathContext mc, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        updateMaturityDateAndAmount(account, mc, account.retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
    }

    private void updateMaturityDateAndAmount(final FixedDepositAccount account, final MathContext mc,
            final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final LocalDate maturityDate = account.calculateMaturityDate();
        final LocalDate interestCalculationUpto = maturityDate.minusDays(1);

        // set end of day balance to maturity date for maturity interest
        // calculation
        account.resetAccountTransactionsEndOfDayBalances(transactions, maturityDate);

        final List<PostingPeriod> postingPeriods = calculateInterestPayable(account, mc, interestCalculationUpto, transactions,
                isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        // reset end of day balance back to today's date
        account.resetAccountTransactionsEndOfDayBalances(transactions, DateUtils.getBusinessLocalDate());

        Money totalInterestPayable = Money.zero(account.getCurrency());
        for (PostingPeriod postingPeriod : postingPeriods) {
            totalInterestPayable = totalInterestPayable.plus(postingPeriod.getInterestEarned());
        }
        final Money depositAmount = Money.of(account.getCurrency(), account.getDepositAmount());
        final Money maturityAmount = depositAmount.plus(totalInterestPayable);

        account.updateMaturityDetails(maturityAmount.getAmount(), maturityDate);
    }

    @Override
    public void updateMaturityDateAndAmount(final RecurringDepositAccount account, final MathContext mc, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final LocalDate maturityDate = account.calculateMaturityDate();
        LocalDate interestCalculationUpto = null;
        List<SavingsAccountTransaction> allTransactions = null;
        if (maturityDate == null) {
            interestCalculationUpto = DateUtils.getBusinessLocalDate();
            allTransactions = account.getTransactions(interestCalculationUpto, false);
        } else {
            interestCalculationUpto = maturityDate.minusDays(1);
            allTransactions = account.getTransactions(interestCalculationUpto, true);

        }

        final List<PostingPeriod> postingPeriods = calculateInterestPayable(account, mc, interestCalculationUpto, allTransactions,
                isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        Money totalInterestPayable = Money.zero(account.getCurrency());
        Money totalDepositAmount = Money.zero(account.getCurrency());
        for (PostingPeriod postingPeriod : postingPeriods) {
            totalInterestPayable = totalInterestPayable.plus(postingPeriod.getInterestEarned());
            totalDepositAmount = totalDepositAmount.plus(postingPeriod.closingBalance()).minus(postingPeriod.openingBalance());
        }
        account.updateMaturityDetails(maturityDate, totalDepositAmount.getAmount(), totalInterestPayable.getAmount());
    }

    @Override
    public void updateMaturityStatus(final FixedDepositAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        if (account.updateMaturityStatus()) {
            postMaturityInterest(account, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        }
    }

    @Override
    public void updateMaturityStatus(final RecurringDepositAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final boolean postReversals) {
        final LocalDate todayDate = DateUtils.getBusinessLocalDate();
        if (account.updateMaturityStatus(todayDate)) {
            postMaturityInterest(account, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, todayDate,
                    postReversals);
        }
    }

    @Override
    public void postMaturityInterest(final FixedDepositAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        final LocalDate interestPostingUpToDate = account.maturityDate();
        final MathContext mc = MathContext.DECIMAL64;
        final boolean isInterestTransfer = false;
        final LocalDate postInterestOnDate = null;
        final boolean backdatedTxnsAllowedTill = false;
        boolean postReversals = false;
        final List<PostingPeriod> postingPeriods = this.savingsAccountInterestCalculationService.calculateInterestUsing(account, mc,
                interestPostingUpToDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        account.postMaturityInterest(postingPeriods);
    }

    @Override
    public void postMaturityInterest(final RecurringDepositAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final LocalDate closeDate, final boolean postReversals) {
        final LocalDate interestPostingUpToDate = account.maturityInterestPostingUpToDate(closeDate);
        account.setClosedOnDate(closeDate);
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final boolean backdatedTxnsAllowedTill = false;
        final List<PostingPeriod> postingPeriods = this.savingsAccountInterestCalculationService.calculateInterestUsing(account, mc,
                interestPostingUpToDate.minusDays(1), isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        account.postMaturityInterest(postingPeriods, closeDate, postReversals);
    }

    @Override
    public void postPreMaturityInterest(final FixedDepositAccount account, final LocalDate accountCloseDate,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = accountCloseDate.minusDays(1);
        final Money interestOnMaturity = calculatePreMatureInterest(account, interestCalculatedToDate,
                account.retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
        account.postPreMaturityInterest(accountCloseDate, interestOnMaturity);
    }

    @Override
    public void postPreMaturityInterest(final RecurringDepositAccount account, final LocalDate accountCloseDate,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final boolean postReversals) {
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = accountCloseDate.minusDays(1);
        final Money interestOnMaturity = calculatePreMatureInterest(account, interestCalculatedToDate,
                account.retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);
        account.postPreMaturityInterest(accountCloseDate, interestOnMaturity, postReversals);
    }

    @Override
    public BigDecimal calculatePreMatureAmount(final FixedDepositAccount account, final LocalDate preMatureDate,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {

        final Money interestPostedToDate = account.totalInterestPosted().copy();

        final Money interestEarnedTillDate = calculatePreMatureInterest(account, preMatureDate,
                account.retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        final Money accountBalance = Money.of(account.getCurrency(), account.getAccountBalance());
        final Money maturityAmount = accountBalance.minus(interestPostedToDate).plus(interestEarnedTillDate);

        return maturityAmount.getAmount();
    }

    @Override
    public BigDecimal calculatePreMatureAmount(final RecurringDepositAccount account, final LocalDate preMatureDate,
            final boolean isPreMatureClosure, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {

        final Money interestPostedToDate = account.totalInterestPosted().copy();

        final Money interestEarnedTillDate = calculatePreMatureInterest(account, preMatureDate,
                account.retreiveOrderedNonInterestPostingTransactions(), isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        final Money accountBalance = Money.of(account.getCurrency(), account.getAccountBalance());
        final Money maturityAmount = accountBalance.minus(interestPostedToDate).plus(interestEarnedTillDate);

        return maturityAmount.getAmount();
    }

    private Money calculatePreMatureInterest(final FixedDepositAccount account, final LocalDate preMatureDate,
            final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final MathContext mc = MathContext.DECIMAL64;
        final List<PostingPeriod> postingPeriods = calculateInterestPayable(account, mc, preMatureDate, transactions, isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        Money interestOnMaturity = Money.zero(account.getCurrency());

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final Money interestEarnedForPeriod = interestPostingPeriod.getInterestEarned();
            interestOnMaturity = interestOnMaturity.plus(interestEarnedForPeriod);
        }

        return interestOnMaturity;
    }

    private Money calculatePreMatureInterest(final RecurringDepositAccount account, final LocalDate preMatureDate,
            final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {
        final MathContext mc = MathContext.DECIMAL64;
        final List<PostingPeriod> postingPeriods = calculateInterestPayable(account, mc, preMatureDate, transactions, isPreMatureClosure,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);

        Money interestOnMaturity = Money.zero(account.getCurrency());

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final Money interestEarnedForPeriod = interestPostingPeriod.getInterestEarned();
            interestOnMaturity = interestOnMaturity.plus(interestEarnedForPeriod);
        }
        account.getSummary().updateFromInterestPeriodSummaries(account.getCurrency(), postingPeriods);
        return interestOnMaturity;
    }

    private List<PostingPeriod> calculateInterestPayable(final FixedDepositAccount account, final MathContext mc,
            final LocalDate maturityDate, final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(account.getInterestPostingPeriodType());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(account.getInterestCompoundingPeriodType());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(account.getInterestCalculationDaysInYearType());
        List<LocalDate> postedAsOnTransactionDates = account.getManualPostingDates();

        final List<LocalDateInterval> postingPeriodIntervals = SavingsInterestCalculationUtil.determineInterestPostingPeriods(
                account.accountSubmittedOrActivationDate(), maturityDate, postingPeriodType, financialYearBeginningMonth,
                postedAsOnTransactionDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance = Money.zero(account.getCurrency());

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(account.getInterestCalculationType());
        final BigDecimal interestRateAsFraction = account.getEffectiveInterestRateAsFraction(mc, maturityDate, isPreMatureClosure);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(account.getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(account.getCurrency(), account.minBalanceForInterestCalculation());
        List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriodList = account
                .toSavingsAccountTransactionDetailsForPostingPeriodList(transactions);
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            boolean isUserPosting = postedAsOnTransactionDates.contains(periodInterval.endDate());
            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriodList, account.getCurrency(), compoundingPeriodType,
                    interestCalculationType, interestRateAsFraction, daysInYearType.getValue(), maturityDate, interestPostTransactions,
                    isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting,
                    financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        account.getSummary().updateFromInterestPeriodSummaries(account.getCurrency(), allPostingPeriods);
        SavingsInterestCalculationUtil.calculateInterestForAllPostingPeriods(account.getCurrency(), allPostingPeriods,
                account.getLockedInUntilDate(), account.isTransferInterestToOtherAccount());
        return allPostingPeriods;
    }

    private List<PostingPeriod> calculateInterestPayable(final RecurringDepositAccount account, final MathContext mc,
            final LocalDate maturityDate, final List<SavingsAccountTransaction> transactions, final boolean isPreMatureClosure,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth) {

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(account.getInterestPostingPeriodType());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(account.getInterestCompoundingPeriodType());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(account.getInterestCalculationDaysInYearType());
        List<LocalDate> postedAsOnDates = account.getManualPostingDates();
        final List<LocalDateInterval> postingPeriodIntervals = SavingsInterestCalculationUtil.determineInterestPostingPeriods(
                account.depositStartDate(), maturityDate, postingPeriodType, financialYearBeginningMonth, postedAsOnDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance = Money.zero(account.getCurrency());

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(account.getInterestCalculationType());
        final BigDecimal interestRateAsFraction = account.getEffectiveInterestRateAsFraction(mc, maturityDate, isPreMatureClosure);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(account.getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(account.getCurrency(), account.minBalanceForInterestCalculation());
        List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriodList = account
                .toSavingsAccountTransactionDetailsForPostingPeriodList(transactions);
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            boolean isUserPosting = false;
            if (postedAsOnDates.contains(periodInterval.endDate())) {
                isUserPosting = true;
            }
            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriodList, account.getCurrency(), compoundingPeriodType,
                    interestCalculationType, interestRateAsFraction, daysInYearType.getValue(), maturityDate, interestPostTransactions,
                    isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting,
                    financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        SavingsInterestCalculationUtil.calculateInterestForAllPostingPeriods(account.getCurrency(), allPostingPeriods,
                account.getLockedInUntilDate(), account.isTransferInterestToOtherAccount());
        // the recurring deposit stores the interest period summaries only for premature closures, see
        // calculatePreMatureInterest
        return allPostingPeriods;
    }
}
