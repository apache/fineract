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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccrualData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.CompoundInterestValues;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingsAccrualWritePlatformServiceImpl implements SavingsAccrualWritePlatformService {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsHelper savingsHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final SavingsAccountDomainService savingsAccountDomainService;

    @Transactional
    @Override
    public void addAccrualEntries(LocalDate tillDate) throws JobExecutionException {
        final List<SavingsAccrualData> savingsAccrualData = savingsAccountReadPlatformService.retrievePeriodicAccrualData(tillDate, null);
        final Integer financialYearBeginningMonth = configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final MathContext mc = MoneyHelper.getMathContext();

        List<Throwable> errors = new ArrayList<>();
        for (SavingsAccrualData savingsAccrual : savingsAccrualData) {
            try {
                if (savingsAccrual.getDepositType().isSavingsDeposit() && savingsAccrual.getIsAllowOverdraft()) {
                    if (!savingsAccrual.getIsTypeInterestReceivable()) {
                        continue;
                    }
                }
                SavingsAccount savingsAccount = savingsAccountAssembler.assembleFrom(savingsAccrual.getId(), false);
                LocalDate fromDate = savingsAccrual.getAccruedTill();
                if (fromDate == null) {
                    fromDate = savingsAccount.getActivationDate();
                }
                log.debug("Processing savings account {} from date {} till date {}", savingsAccrual.getAccountNo(), fromDate, tillDate);
                addAccrualTransactions(savingsAccount, fromDate, tillDate, financialYearBeginningMonth,
                        isSavingsInterestPostingAtCurrentPeriodEnd, mc, null);
            } catch (Exception e) {
                log.error("Failed to add accrual transaction for savings {} : {}", savingsAccrual.getAccountNo(), e.getMessage());
                errors.add(e.getCause());
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    private void addAccrualTransactions(SavingsAccount savingsAccount, final LocalDate fromDate, final LocalDate tillDate,
            final Integer financialYearBeginningMonth, final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final MathContext mc,
            final Function<LocalDate, String> refNoProvider) {
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        existingTransactionIds.addAll(savingsAccount.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(savingsAccount.findExistingReversedTransactionIds());

        List<LocalDate> postedAsOnTransactionDates = savingsAccount.getManualPostingDates();
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(savingsAccount.getInterestCalculationType());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(savingsAccount.getInterestPostingPeriodType());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(savingsAccount.getInterestCalculationDaysInYearType());

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(fromDate, tillDate,
                postingPeriodType, financialYearBeginningMonth, postedAsOnTransactionDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        final MonetaryCurrency currency = savingsAccount.getCurrency();
        Money periodStartingBalance = Money.zero(currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(savingsAccount.getInterestCalculationType());
        final BigDecimal interestRateAsFraction = savingsAccount.getEffectiveInterestRateAsFractionAccrual(mc, tillDate);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(savingsAccount.getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(currency, savingsAccount.getMinBalanceForInterestCalculation());
        List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriodList = savingsAccount
                .toSavingsAccountTransactionDetailsForPostingPeriodList();
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            if (DateUtils.isDateInTheFuture(periodInterval.endDate())) {
                continue;
            }
            final boolean isUserPosting = postedAsOnTransactionDates.contains(periodInterval.endDate());

            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriodList, currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue(), tillDate, interestPostTransactions, isInterestTransfer,
                    minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting,
                    financialYearBeginningMonth);

            postingPeriod.setOverdraftInterestRateAsFraction(
                    savingsAccount.getNominalAnnualInterestRateOverdraft().divide(BigDecimal.valueOf(100), mc));
            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }
        BigDecimal compoundedInterest = BigDecimal.ZERO;
        BigDecimal unCompoundedInterest = BigDecimal.ZERO;
        final CompoundInterestValues compoundInterestValues = new CompoundInterestValues(compoundedInterest, unCompoundedInterest);

        final List<LocalDate> accrualTransactionDates = savingsAccount.retrieveOrderedAccrualTransactions().stream()
                .map(transaction -> transaction.getTransactionDate()).toList();
        LocalDate accruedTillDate = fromDate;

        for (PostingPeriod period : allPostingPeriods) {
            period.calculateInterest(compoundInterestValues);
            final LocalDate endDate = period.getPeriodInterval().endDate();
            if (!accrualTransactionDates.contains(period.getPeriodInterval().endDate())
                    && !MathUtil.isZero(period.closingBalance().getAmount())) {
                String refNo = (refNoProvider != null) ? refNoProvider.apply(endDate) : null;
                SavingsAccountTransaction savingsAccountTransaction = SavingsAccountTransaction.accrual(savingsAccount,
                        savingsAccount.office(), period.getPeriodInterval().endDate(), period.getInterestEarned().abs(), false, refNo);
                savingsAccountTransaction.setRunningBalance(period.getClosingBalance());
                savingsAccountTransaction.setOverdraftAmount(period.getInterestEarned());
                savingsAccount.addTransaction(savingsAccountTransaction);
            }
        }

        savingsAccount.setAccruedTillDate(accruedTillDate);
        savingsAccountRepository.saveAndFlush(savingsAccount);
        savingsAccountDomainService.postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds, false);
    }

}
