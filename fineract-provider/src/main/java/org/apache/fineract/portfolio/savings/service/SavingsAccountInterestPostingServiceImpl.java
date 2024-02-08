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

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargesPaidByData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionDataComparator;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.service.TaxUtils;

@RequiredArgsConstructor
public class SavingsAccountInterestPostingServiceImpl implements SavingsAccountInterestPostingService {

    private final SavingsHelper savingsHelper;

    @Override
    public SavingsAccountData postInterest(final MathContext mc, final LocalDate interestPostingUpToDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final SavingsAccountData savingsAccountData) {
        Money interestPostedToDate = Money.zero(savingsAccountData.getCurrency());
        LocalDate startInterestDate = getStartInterestCalculationDate(savingsAccountData);

        if (backdatedTxnsAllowedTill && savingsAccountData.getSummary().getInterestPostedTillDate() != null) {
            interestPostedToDate = Money.of(savingsAccountData.getCurrency(), savingsAccountData.getSummary().getTotalInterestPosted());
            savingsAccountData.setStartInterestCalculationDate(savingsAccountData.getSummary().getInterestPostedTillDate());
        } else {
            savingsAccountData.setStartInterestCalculationDate(startInterestDate);
        }

        final List<PostingPeriod> postingPeriods = calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                savingsAccountData);

        boolean recalucateDailyBalanceDetails = false;
        boolean applyWithHoldTax = isWithHoldTaxApplicableForInterestPosting(savingsAccountData);
        final List<SavingsAccountTransactionData> withholdTransactions = new ArrayList<>();

        withholdTransactions.addAll(findWithHoldSavingsTransactionsWithPivotConfig(savingsAccountData));

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            if (!DateUtils.isAfter(interestPostingTransactionDate, interestPostingUpToDate)) {
                interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);
                final SavingsAccountTransactionData postingTransaction = findInterestPostingTransactionFor(interestPostingTransactionDate,
                        savingsAccountData);

                if (postingTransaction == null) {
                    SavingsAccountTransactionData newPostingTransaction;
                    if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(savingsAccountData.getCurrency()))) {
                        newPostingTransaction = SavingsAccountTransactionData.interestPosting(savingsAccountData,
                                interestPostingTransactionDate, interestEarnedToBePostedForPeriod, interestPostingPeriod.isUserPosting());
                    } else {
                        newPostingTransaction = SavingsAccountTransactionData.overdraftInterest(savingsAccountData,
                                interestPostingTransactionDate, interestEarnedToBePostedForPeriod.negated(),
                                interestPostingPeriod.isUserPosting());
                    }

                    savingsAccountData.updateTransactions(newPostingTransaction);

                    if (applyWithHoldTax) {
                        createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                savingsAccountData);
                    }
                    recalucateDailyBalanceDetails = true;
                } else {
                    boolean correctionRequired = false;
                    if (postingTransaction.isInterestPostingAndNotReversed()) {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                    } else {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod.negated());
                    }
                    if (correctionRequired) {
                        boolean applyWithHoldTaxForOldTransaction = false;
                        postingTransaction.reverse();

                        final SavingsAccountTransactionData withholdTransaction = findTransactionFor(interestPostingTransactionDate,
                                withholdTransactions);

                        if (withholdTransaction != null) {
                            withholdTransaction.reverse();
                            applyWithHoldTaxForOldTransaction = true;
                        }
                        SavingsAccountTransactionData newPostingTransaction;
                        if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(savingsAccountData.getCurrency()))) {
                            newPostingTransaction = SavingsAccountTransactionData.interestPosting(savingsAccountData,
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                    interestPostingPeriod.isUserPosting());
                        } else {
                            newPostingTransaction = SavingsAccountTransactionData.overdraftInterest(savingsAccountData,
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod.negated(),
                                    interestPostingPeriod.isUserPosting());
                        }

                        savingsAccountData.updateTransactions(newPostingTransaction);

                        if (applyWithHoldTaxForOldTransaction) {
                            createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                    savingsAccountData);
                        }
                        recalucateDailyBalanceDetails = true;
                    }
                }
            }
        }

        if (recalucateDailyBalanceDetails) {
            // no openingBalance concept supported yet but probably will to
            // allow
            // for migrations.
            Money openingAccountBalance = Money.zero(savingsAccountData.getCurrency());

            if (backdatedTxnsAllowedTill) {
                if (savingsAccountData.getSummary().getLastInterestCalculationDate() == null) {
                    openingAccountBalance = Money.zero(savingsAccountData.getCurrency());
                } else {
                    openingAccountBalance = Money.of(savingsAccountData.getCurrency(),
                            savingsAccountData.getSummary().getRunningBalanceOnPivotDate());
                }
            }

            // update existing transactions so derived balance fields are
            // correct.
            recalculateDailyBalances(openingAccountBalance, interestPostingUpToDate, backdatedTxnsAllowedTill, savingsAccountData);
        }

        if (!backdatedTxnsAllowedTill) {
            savingsAccountData.getSummary().updateSummary(savingsAccountData.getCurrency(),
                    savingsAccountData.getSavingsAccountTransactionSummaryWrapper(), savingsAccountData.getSavingsAccountTransactionData());
        } else {
            savingsAccountData.getSummary().updateSummaryWithPivotConfig(savingsAccountData.getCurrency(),
                    savingsAccountData.getSavingsAccountTransactionSummaryWrapper(), null,
                    savingsAccountData.getSavingsAccountTransactionData());
        }

        return savingsAccountData;
    }

    protected SavingsAccountTransactionData findTransactionFor(final LocalDate postingDate,
            final List<SavingsAccountTransactionData> transactions) {
        SavingsAccountTransactionData transaction = null;
        for (final SavingsAccountTransactionData savingsAccountTransaction : transactions) {
            if (savingsAccountTransaction.occursOn(postingDate)) {
                transaction = savingsAccountTransaction;
                break;
            }
        }
        return transaction;
    }

    public List<PostingPeriod> calculateInterestUsing(final MathContext mc, final LocalDate upToInterestCalculationDate,
            boolean isInterestTransfer, final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final SavingsAccountData savingsAccountData) {

        // no openingBalance concept supported yet but probably will to allow
        // for migrations.
        Money openingAccountBalance = null;

        // Check global configurations and 'pivot' date is null
        if (backdatedTxnsAllowedTill) {
            openingAccountBalance = Money.of(savingsAccountData.getCurrency(),
                    savingsAccountData.getSummary().getRunningBalanceOnPivotDate());
        } else {
            openingAccountBalance = Money.zero(savingsAccountData.getCurrency());
        }

        // update existing transactions so derived balance fields are
        // correct.
        recalculateDailyBalances(openingAccountBalance, upToInterestCalculationDate, backdatedTxnsAllowedTill, savingsAccountData);

        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to
        // that period

        // A generate list of EndOfDayBalances (not including interest postings)

        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(savingsAccountData.getInterestPostingPeriodTypeId());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(savingsAccountData.getInterestCompoundingPeriodTypeId());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(savingsAccountData.getInterestCalculationDaysInYearTypeId());

        List<LocalDate> postedAsOnDates = getManualPostingDates(savingsAccountData);
        if (postInterestOnDate != null) {
            postedAsOnDates.add(postInterestOnDate);
        }
        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(
                savingsAccountData.getStartInterestCalculationDate(), upToInterestCalculationDate, postingPeriodType,
                financialYearBeginningMonth, postedAsOnDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();

        Money periodStartingBalance;
        if (savingsAccountData.getStartInterestCalculationDate() != null
                && !savingsAccountData.getStartInterestCalculationDate().equals(savingsAccountData.getActivationLocalDate())) {
            final SavingsAccountTransactionData transaction = retrieveLastTransaction(savingsAccountData);

            if (transaction == null) {
                periodStartingBalance = Money.zero(savingsAccountData.getCurrency());
            } else {
                periodStartingBalance = Money.of(savingsAccountData.getCurrency(),
                        savingsAccountData.getSummary().getRunningBalanceOnPivotDate());
            }

        } else {
            periodStartingBalance = Money.zero(savingsAccountData.getCurrency());
        }

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(savingsAccountData.getInterestCalculationTypeId());
        final BigDecimal interestRateAsFraction = getEffectiveInterestRateAsFraction(mc, upToInterestCalculationDate, savingsAccountData);
        final BigDecimal overdraftInterestRateAsFraction = getEffectiveOverdraftInterestRateAsFraction(mc, savingsAccountData);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(savingsAccountData.getId());
        final Money minBalanceForInterestCalculation = Money.of(savingsAccountData.getCurrency(),
                minBalanceForInterestCalculation(savingsAccountData));
        final Money minOverdraftForInterestCalculation = Money.of(savingsAccountData.getCurrency(),
                savingsAccountData.getMinOverdraftForInterestCalculation());
        final MonetaryCurrency monetaryCurrency = MonetaryCurrency.fromCurrencyData(savingsAccountData.getCurrency());

        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {

            boolean isUserPosting = false;
            if (postedAsOnDates.contains(periodInterval.endDate().plusDays(1))) {
                isUserPosting = true;
            }
            final PostingPeriod postingPeriod = PostingPeriod.createFromDTO(periodInterval, periodStartingBalance,
                    retreiveOrderedNonInterestPostingTransactions(savingsAccountData), monetaryCurrency, compoundingPeriodType,
                    interestCalculationType, interestRateAsFraction, daysInYearType.getValue(), upToInterestCalculationDate,
                    interestPostTransactions, isInterestTransfer, minBalanceForInterestCalculation,
                    isSavingsInterestPostingAtCurrentPeriodEnd, overdraftInterestRateAsFraction, minOverdraftForInterestCalculation,
                    isUserPosting, financialYearBeginningMonth, savingsAccountData.isAllowOverdraft());

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        this.savingsHelper.calculateInterestForAllPostingPeriods(monetaryCurrency, allPostingPeriods,
                getLockedInUntilLocalDate(savingsAccountData), false);

        savingsAccountData.getSummary().updateFromInterestPeriodSummaries(monetaryCurrency, allPostingPeriods);

        if (backdatedTxnsAllowedTill) {
            savingsAccountData.getSummary().updateSummaryWithPivotConfig(savingsAccountData.getCurrency(),
                    savingsAccountData.getSavingsAccountTransactionSummaryWrapper(), null,
                    savingsAccountData.getSavingsAccountTransactionData());
        } else {
            savingsAccountData.getSummary().updateSummary(savingsAccountData.getCurrency(),
                    savingsAccountData.getSavingsAccountTransactionSummaryWrapper(), savingsAccountData.getSavingsAccountTransactionData());
        }

        return allPostingPeriods;
    }

    private List<SavingsAccountTransactionData> retreiveOrderedNonInterestPostingTransactions(final SavingsAccountData savingsAccountData) {
        final List<SavingsAccountTransactionData> listOfTransactionsSorted = retrieveListOfTransactions(savingsAccountData);

        final List<SavingsAccountTransactionData> orderedNonInterestPostingTransactions = new ArrayList<>();

        for (final SavingsAccountTransactionData transaction : listOfTransactionsSorted) {
            if (!(transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                orderedNonInterestPostingTransactions.add(transaction);
            }
        }
        orderedNonInterestPostingTransactions.sort(new SavingsAccountTransactionDataComparator());
        return orderedNonInterestPostingTransactions;
    }

    private List<SavingsAccountTransactionData> retrieveListOfTransactions(final SavingsAccountData savingsAccountData) {
        final List<SavingsAccountTransactionData> listOfTransactionsSorted = new ArrayList<>();
        listOfTransactionsSorted.addAll(savingsAccountData.getSavingsAccountTransactionData());

        final SavingsAccountTransactionDataComparator transactionComparator = new SavingsAccountTransactionDataComparator();
        Collections.sort(listOfTransactionsSorted, transactionComparator);
        return listOfTransactionsSorted;
    }

    protected LocalDate getLockedInUntilLocalDate(final SavingsAccountData savingsAccount) {
        LocalDate lockedInUntilLocalDate = savingsAccount.getLockedInUntilDate();
        return lockedInUntilLocalDate == null ? savingsAccount.getActivationLocalDate() : lockedInUntilLocalDate;
    }

    private BigDecimal minBalanceForInterestCalculation(final SavingsAccountData savingsAccountData) {
        return savingsAccountData.getMinBalanceForInterestCalculation();
    }

    private BigDecimal getEffectiveOverdraftInterestRateAsFraction(MathContext mc, final SavingsAccountData savingsAccountData) {
        return savingsAccountData.getNominalAnnualInterestRateOverdraft() != null
                ? savingsAccountData.getNominalAnnualInterestRateOverdraft().divide(BigDecimal.valueOf(100L), mc)
                : BigDecimal.ZERO;
    }

    @SuppressWarnings("unused")
    private BigDecimal getEffectiveInterestRateAsFraction(final MathContext mc, final LocalDate upToInterestCalculationDate,
            final SavingsAccountData savingsAccountData) {
        return savingsAccountData.getNominalAnnualInterestRate().divide(BigDecimal.valueOf(100L), mc);
    }

    public List<SavingsAccountTransactionData> getTransactions(final SavingsAccountData savingsAccountData) {
        return savingsAccountData.getSavingsAccountTransactionData();
    }

    private SavingsAccountTransactionData retrieveLastTransaction(@NotNull SavingsAccountData savingsAccountData) {
        List<SavingsAccountTransactionData> transactions = savingsAccountData.getSavingsAccountTransactionData();
        if (transactions == null || transactions.isEmpty()) {
            return savingsAccountData.getLastSavingsAccountTransaction(); // what is this?
        }
        if (transactions.size() == 1) {
            return transactions.get(0);
        }
        final List<SavingsAccountTransactionData> listOfTransactionsSorted = new ArrayList<>(transactions);
        listOfTransactionsSorted.sort(new SavingsAccountTransactionDataComparator());
        return listOfTransactionsSorted.get(0); // this is the first transaction, not the last
    }

    public LocalDate getStartInterestCalculationDate(final SavingsAccountData savingsAccountData) {
        LocalDate startInterestCalculationLocalDate = null;
        if (savingsAccountData.getStartInterestCalculationDate() != null) {
            startInterestCalculationLocalDate = savingsAccountData.getStartInterestCalculationDate();
        } else {
            startInterestCalculationLocalDate = getActivationLocalDate(savingsAccountData);
        }
        return startInterestCalculationLocalDate;
    }

    public LocalDate getActivationLocalDate(final SavingsAccountData savingsAccountData) {
        LocalDate activationLocalDate = null;
        if (savingsAccountData.getActivationLocalDate() != null) {
            activationLocalDate = savingsAccountData.getActivationLocalDate();
        }
        return activationLocalDate;
    }

    public List<LocalDate> getManualPostingDates(final SavingsAccountData savingsAccountData) {
        List<LocalDate> transactions = new ArrayList<>();
        for (SavingsAccountTransactionData trans : savingsAccountData.getSavingsAccountTransactionData()) {
            if (trans.isInterestPosting() && trans.isNotReversed() && !trans.isReversalTransaction() && trans.isManualTransaction()) {
                transactions.add(trans.getTransactionDate());
            }
        }
        return transactions;
    }

    protected void recalculateDailyBalances(final Money openingAccountBalance, final LocalDate interestPostingUpToDate,
            final boolean backdatedTxnsAllowedTill, final SavingsAccountData savingsAccountData) {

        Money runningBalance = openingAccountBalance.copy();

        List<SavingsAccountTransactionData> accountTransactionsSorted = retrieveListOfTransactions(savingsAccountData);
        boolean isTransactionsModified = false;

        for (final SavingsAccountTransactionData transaction : accountTransactionsSorted) {
            if (transaction.isReversed() || transaction.isReversalTransaction()) {
                transaction.zeroBalanceFields();
            } else {
                Money overdraftAmount = Money.zero(savingsAccountData.getCurrency());
                Money transactionAmount = Money.zero(savingsAccountData.getCurrency());
                if (transaction.isCredit() || transaction.isAmountRelease()) {
                    if (runningBalance.isLessThanZero()) {
                        Money diffAmount = Money.of(savingsAccountData.getCurrency(), transaction.getAmount()).plus(runningBalance);
                        if (diffAmount.isGreaterThanZero()) {
                            overdraftAmount = Money.of(savingsAccountData.getCurrency(), transaction.getAmount()).minus(diffAmount);
                        } else {
                            overdraftAmount = Money.of(savingsAccountData.getCurrency(), transaction.getAmount());
                        }
                    }
                    transactionAmount = transactionAmount.plus(transaction.getAmount());
                } else if (transaction.isDebit() || transaction.isAmountOnHold()) {
                    if (runningBalance.isLessThanZero()) {
                        overdraftAmount = Money.of(savingsAccountData.getCurrency(), transaction.getAmount());
                    }
                    transactionAmount = transactionAmount.minus(transaction.getAmount());
                }

                runningBalance = runningBalance.plus(transactionAmount);
                if (!transaction.getRunningBalance(transactionAmount.getCurrency()).isEqualTo(runningBalance)) {
                    transaction.updateRunningBalance(runningBalance);
                }
                // transaction.updateRunningBalance(runningBalance);
                if (overdraftAmount.isZero() && runningBalance.isLessThanZero()) {
                    overdraftAmount = overdraftAmount.plus(runningBalance.getAmount().negate());
                }
                if (transaction.getId() == null && overdraftAmount.isGreaterThanZero()) {
                    transaction.updateOverdraftAmount(overdraftAmount.getAmount());
                } else if (overdraftAmount.isNotEqualTo(Money.of(savingsAccountData.getCurrency(), transaction.getOverdraftAmount()))) {
                    SavingsAccountTransactionData accountTransaction = SavingsAccountTransactionData.copyTransaction(transaction);
                    if (transaction.isChargeTransaction()) {
                        Set<SavingsAccountChargesPaidByData> chargesPaidBy = transaction.getSavingsAccountChargesPaid();
                        final Set<SavingsAccountChargesPaidByData> newChargePaidBy = new HashSet<>();
                        chargesPaidBy.forEach(
                                x -> newChargePaidBy.add(SavingsAccountChargesPaidByData.instance(x.getChargeId(), x.getAmount())));
                        accountTransaction.getSavingsAccountChargesPaid().addAll(newChargePaidBy);
                    }
                    transaction.reverse();
                    if (overdraftAmount.isGreaterThanZero()) {
                        accountTransaction.updateOverdraftAmount(overdraftAmount.getAmount());
                    }
                    accountTransaction.updateRunningBalance(runningBalance);
                    addTransactionToExisting(accountTransaction, savingsAccountData);

                    isTransactionsModified = true;
                }

            }
        }

        if (isTransactionsModified) {
            accountTransactionsSorted = retrieveListOfTransactions(savingsAccountData);
        }
        resetAccountTransactionsEndOfDayBalances(accountTransactionsSorted, interestPostingUpToDate, savingsAccountData);
    }

    public void addTransactionToExisting(final SavingsAccountTransactionData transaction, final SavingsAccountData savingsAccountData) {
        savingsAccountData.updateTransactions(transaction);
    }

    private List<SavingsAccountTransactionData> findWithHoldSavingsTransactionsWithPivotConfig(
            final SavingsAccountData savingsAccountData) {
        final List<SavingsAccountTransactionData> withholdTransactions = new ArrayList<>();
        List<SavingsAccountTransactionData> trans = savingsAccountData.getSavingsAccountTransactionData();
        for (final SavingsAccountTransactionData transaction : trans) {
            if (transaction.isWithHoldTaxAndNotReversed()) {
                withholdTransactions.add(transaction);
            }
        }
        return withholdTransactions;
    }

    private boolean createWithHoldTransaction(final BigDecimal amount, final LocalDate date, final SavingsAccountData savingsAccountData) {
        boolean isTaxAdded = false;
        if (savingsAccountData.getTaxGroup() != null && savingsAccountData.getTaxGroup().getTaxAssociations() != null
                && amount.compareTo(BigDecimal.ZERO) > 0) {
            Map<TaxComponentData, BigDecimal> taxSplit = TaxUtils.splitTaxData(amount, date,
                    savingsAccountData.getTaxGroup().getTaxAssociations().stream().collect(Collectors.toSet()), amount.scale());
            BigDecimal totalTax = TaxUtils.totalTaxDataAmount(taxSplit);
            if (totalTax.compareTo(BigDecimal.ZERO) > 0) {
                SavingsAccountTransactionData withholdTransaction = SavingsAccountTransactionData.withHoldTax(savingsAccountData, date,
                        Money.of(savingsAccountData.getCurrency(), totalTax), taxSplit);

                savingsAccountData.getSavingsAccountTransactionData().add(withholdTransaction);
                // if (backdatedTxnsAllowedTill) {
                // addTransactionToExisting(withholdTransaction);
                // } else {
                // addTransaction(withholdTransaction);
                // }
                isTaxAdded = true;
            }
        }
        return isTaxAdded;
    }

    protected SavingsAccountTransactionData findInterestPostingTransactionFor(final LocalDate postingDate,
            final SavingsAccountData savingsAccountData) {
        SavingsAccountTransactionData postingTransation = null;
        List<SavingsAccountTransactionData> trans = savingsAccountData.getSavingsAccountTransactionData();
        for (final SavingsAccountTransactionData transaction : trans) {
            if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && transaction.occursOn(postingDate) && !transaction.isReversalTransaction()) {
                postingTransation = transaction;
                break;
            }
        }
        return postingTransation;
    }

    protected void resetAccountTransactionsEndOfDayBalances(final List<SavingsAccountTransactionData> accountTransactionsSorted,
            final LocalDate interestPostingUpToDate, final SavingsAccountData savingsAccountData) {
        // loop over transactions in reverse
        LocalDate endOfBalanceDate = interestPostingUpToDate;
        for (int i = accountTransactionsSorted.size() - 1; i >= 0; i--) {
            final SavingsAccountTransactionData transaction = accountTransactionsSorted.get(i);
            if (transaction.isNotReversed() && !transaction.isReversalTransaction()
                    && !(transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())) {
                transaction.updateCumulativeBalanceAndDates(MonetaryCurrency.fromCurrencyData(savingsAccountData.getCurrency()),
                        endOfBalanceDate);
                // this transactions transaction date is end of balance date for
                // previous transaction.
                endOfBalanceDate = transaction.getTransactionDate().minusDays(1);
            }
        }
    }

    private boolean isWithHoldTaxApplicableForInterestPosting(final SavingsAccountData savingsAccountData) {
        return this.withHoldTax(savingsAccountData) && this.depositAccountType(savingsAccountData).isSavingsDeposit();
    }

    private boolean withHoldTax(final SavingsAccountData savingsAccountData) {
        return savingsAccountData.isWithHoldTax();
    }

    public DepositAccountType depositAccountType(final SavingsAccountData savingsAccountData) {
        return savingsAccountData.depositAccountType();
    }

}
