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
package org.apache.fineract.portfolio.savings.service.accrual;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
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
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargePaidBy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.CompoundInterestValues;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SavingsAccrualDomainServiceImpl {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsHelper savingsHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final SavingsAccountDomainService savingsAccountDomainService;

    public void addAccrualEntries(LocalDate tillDate) throws JobExecutionException {
        final Collection<SavingsAccrualData> savingsAccrualData = savingsAccountReadPlatformService.retrievePeriodicAccrualData(tillDate,
                null);
        log.debug("Savings Accrual for date {} : {}", tillDate, savingsAccrualData.size());
        final Integer financialYearBeginningMonth = configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final MathContext mc = MoneyHelper.getMathContext();

        List<Throwable> errors = new ArrayList<>();
        for (SavingsAccrualData savingsAccrual : savingsAccrualData) {
            try {
                SavingsAccount savingsAccount = savingsAccountAssembler.assembleFrom(savingsAccrual.getId(), false);
                LocalDate fromDate = savingsAccrual.getAccruedTill();
                if (fromDate == null) {
                    fromDate = savingsAccount.getActivationDate();
                }
                log.debug("Processing savings account {} from date {} till date {}", savingsAccrual.getAccountNo(), fromDate, tillDate);
                addAccrualTransactions(savingsAccount, fromDate, tillDate, financialYearBeginningMonth,
                        isSavingsInterestPostingAtCurrentPeriodEnd, mc);
            } catch (Exception e) {
                log.error("Failed to add accrual transaction for savings {} : {}", savingsAccrual.getAccountNo(), e.getMessage());
                errors.add(e.getCause());
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    public boolean isChargeToBeRecognizedAsAccrual(final Collection<Long> chargeIds, final SavingsAccountCharge savingsAccountCharge) {
        if (chargeIds.isEmpty()) {
            return false;
        }
        return chargeIds.contains(savingsAccountCharge.getCharge().getId());
    }

    public SavingsAccountTransaction addSavingsChargeAccrualTransaction(SavingsAccount savingsAccount,
            SavingsAccountCharge savingsAccountCharge, LocalDate transactionDate) {
        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final Money chargeAmount = savingsAccountCharge.getAmount(currency);
        SavingsAccountTransaction savingsAccountTransaction = SavingsAccountTransaction.accrual(savingsAccount, savingsAccount.office(),
                transactionDate, chargeAmount, false);
        final SavingsAccountChargePaidBy chargePaidBy = SavingsAccountChargePaidBy.instance(savingsAccountTransaction, savingsAccountCharge,
                savingsAccountTransaction.getAmount(currency).getAmount());
        savingsAccountTransaction.getSavingsAccountChargesPaid().add(chargePaidBy);

        savingsAccount.addTransaction(savingsAccountTransaction);
        return savingsAccountTransaction;
    }

    private void addAccrualTransactions(SavingsAccount savingsAccount, final LocalDate fromDate, final LocalDate tillDate,
            final Integer financialYearBeginningMonth, final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final MathContext mc) {

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        existingTransactionIds.addAll(savingsAccount.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(savingsAccount.findExistingReversedTransactionIds());

        List<LocalDate> postedAsOnTransactionDates = savingsAccount.getManualPostingDates();
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(savingsAccount.getInterestCompoundingPeriodType());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(savingsAccount.getInterestCompoundingPeriodType());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(savingsAccount.getInterestCalculationDaysInYearType());

        final List<LocalDateInterval> postingPeriodIntervals = this.savingsHelper.determineInterestPostingPeriods(fromDate, tillDate,
                postingPeriodType, financialYearBeginningMonth, postedAsOnTransactionDates);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        final MonetaryCurrency currency = savingsAccount.getCurrency();
        Money periodStartingBalance = Money.zero(currency);

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(savingsAccount.getInterestCalculationType());
        final BigDecimal interestRateAsFraction = savingsAccount.getEffectiveInterestRateAsFraction(mc, tillDate);
        final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(savingsAccount.getId());
        boolean isInterestTransfer = false;
        final Money minBalanceForInterestCalculation = Money.of(currency, savingsAccount.getMinBalanceForInterestCalculation());
        List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriodList = savingsAccount
                .toSavingsAccountTransactionDetailsForPostingPeriodList();
        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {
            final boolean isUserPosting = (postedAsOnTransactionDates.contains(periodInterval.endDate()));

            final PostingPeriod postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriodList, currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue(), tillDate, interestPostTransactions, isInterestTransfer,
                    minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd, isUserPosting,
                    financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }
        BigDecimal compoundedInterest = BigDecimal.ZERO;
        BigDecimal unCompoundedInterest = BigDecimal.ZERO;
        final CompoundInterestValues compoundInterestValues = new CompoundInterestValues(compoundedInterest, unCompoundedInterest);

        final List<LocalDate> accrualTransactionDates = savingsAccount.retreiveOrderedAccrualTransactions().stream()
                .map(transaction -> transaction.getTransactionDate()).toList();

        LocalDate accruedTillDate = fromDate;
        for (PostingPeriod period : allPostingPeriods) {
            if (MathUtil.isGreaterThanZero(period.closingBalance())) {
                period.calculateInterest(compoundInterestValues);
                log.debug("  period {} {} : {}", period.getPeriodInterval().startDate(), period.getPeriodInterval().endDate(),
                        period.getInterestEarned());
                if (!accrualTransactionDates.contains(period.getPeriodInterval().endDate())) {
                    SavingsAccountTransaction savingsAccountTransaction = SavingsAccountTransaction.accrual(savingsAccount,
                            savingsAccount.office(), period.getPeriodInterval().endDate(), period.getInterestEarned(), false);
                    savingsAccount.addTransaction(savingsAccountTransaction);
                }
            }
            accruedTillDate = period.getPeriodInterval().endDate();
        }

        savingsAccount.getSummary().setAccruedTillDate(accruedTillDate);
        savingsAccountRepository.saveAndFlush(savingsAccount);

        savingsAccountDomainService.postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds, false);
    }

}
