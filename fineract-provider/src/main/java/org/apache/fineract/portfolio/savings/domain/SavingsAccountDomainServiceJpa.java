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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.savings.transaction.SavingsDepositBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.transaction.SavingsWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.exception.DepositAccountTransactionNotAllowedException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingsAccountDomainServiceJpa implements SavingsAccountDomainService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    private final SavingsHelper savingsHelper;

    @Transactional
    @Override
    public SavingsAccountTransaction handleWithdrawal(final SavingsAccount account, final DateTimeFormatter fmt,
                                                      final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
                                                      final SavingsTransactionBooleanValues transactionBooleanValues, final boolean backdatedTxnsAllowedTill) {
        context.authenticatedUser();
        account.validateForAccountBlock();
        account.validateForDebitBlock();
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        final boolean postReversals = this.configurationDomainService.isReversalTransactionAllowed();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        if (transactionBooleanValues.isRegularTransaction() && !account.allowWithdrawal()) {
            throw new DepositAccountTransactionNotAllowedException(account.getId(), "withdraw", account.depositAccountType());
        }
        final Set<Long> existingTransactionIds = new HashSet<>();
        final LocalDate postInterestOnDate = null;
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        if (backdatedTxnsAllowedTill) {
            updateTransactionDetailsWithPivotConfig(account, existingTransactionIds, existingReversedTransactionIds);
        } else {
            updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        }

        Integer accountType = null;
        final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                paymentDetail, null, accountType);
        UUID refNo = UUID.randomUUID();
        final SavingsAccountTransaction withdrawal = account.withdraw(transactionDTO, transactionBooleanValues.isApplyWithdrawFee(),
                backdatedTxnsAllowedTill, relaxingDaysConfigForPivotDate, refNo.toString());
        final MathContext mc = MathContext.DECIMAL64;

        final LocalDate today = DateUtils.getBusinessLocalDate();

        if (account.isBeforeLastPostingPeriod(transactionDate, backdatedTxnsAllowedTill)) {
            account.postInterest(mc, today, transactionBooleanValues.isInterestTransfer(), isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, transactionBooleanValues.isInterestTransfer(),
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                    postReversals);
        }

        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative(transactionAmount, transactionBooleanValues.isExceptionForBalanceCheck(),
                depositAccountOnHoldTransactions, backdatedTxnsAllowedTill);

        saveTransactionToGenerateTransactionId(withdrawal);
        if (backdatedTxnsAllowedTill) {
            // Update transactions separately
            saveUpdatedTransactionsOfSavingsAccount(account.getSavingsAccountTransactionsWithPivotConfig());
        }
        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, transactionBooleanValues.isAccountTransfer(),
                backdatedTxnsAllowedTill);

        businessEventNotifierService.notifyPostBusinessEvent(new SavingsWithdrawalBusinessEvent(withdrawal));
        return withdrawal;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleDeposit(final SavingsAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
            final boolean isAccountTransfer, final boolean isRegularTransaction, final boolean backdatedTxnsAllowedTill) {
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.DEPOSIT;
        return handleDeposit(account, fmt, transactionDate, transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction,
                savingsAccountTransactionType, backdatedTxnsAllowedTill);
    }

    private SavingsAccountTransaction handleDeposit(final SavingsAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
            final boolean isAccountTransfer, final boolean isRegularTransaction,
            final SavingsAccountTransactionType savingsAccountTransactionType, final boolean backdatedTxnsAllowedTill) {
        context.authenticatedUser();
        account.validateForAccountBlock();
        account.validateForCreditBlock();

        // Global configurations
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        if (isRegularTransaction && !account.allowDeposit()) {
            throw new DepositAccountTransactionNotAllowedException(account.getId(), "deposit", account.depositAccountType());
        }
        boolean isInterestTransfer = false;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        if (backdatedTxnsAllowedTill) {
            updateTransactionDetailsWithPivotConfig(account, existingTransactionIds, existingReversedTransactionIds);
        } else {
            updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        }

        Integer accountType = null;
        final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                paymentDetail, null, accountType);
        final String refNo = ExternalId.generate().getValue();
        final SavingsAccountTransaction deposit = account.deposit(transactionDTO, savingsAccountTransactionType, backdatedTxnsAllowedTill,
                relaxingDaysConfigForPivotDate, refNo);
        final LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;

        final LocalDate today = DateUtils.getBusinessLocalDate();
        boolean postReversals = this.configurationDomainService.isReversalTransactionAllowed();
        if (account.isBeforeLastPostingPeriod(transactionDate, backdatedTxnsAllowedTill)) {
            postInterest(account, mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        }

        saveTransactionToGenerateTransactionId(deposit);

        if (backdatedTxnsAllowedTill) {
            // Update transactions separately
            saveUpdatedTransactionsOfSavingsAccount(account.getSavingsAccountTransactionsWithPivotConfig());
        }

        this.savingsAccountRepository.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, backdatedTxnsAllowedTill);
        businessEventNotifierService.notifyPostBusinessEvent(new SavingsDepositBusinessEvent(deposit));
        return deposit;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleHold(final SavingsAccount account, BigDecimal amount, LocalDate transactionDate,
            Boolean lienAllowed) {
        return SavingsAccountTransaction.holdAmount(account, account.office(), null, transactionDate,
                Money.of(account.getCurrency(), amount), lienAllowed);
    }

    @Override
    public SavingsAccountTransaction handleDividendPayout(final SavingsAccount account, final LocalDate transactionDate,
            final BigDecimal transactionAmount, final boolean backdatedTxnsAllowedTill) {
        final DateTimeFormatter fmt = null;
        final PaymentDetail paymentDetail = null;
        final boolean isAccountTransfer = false;
        final boolean isRegularTransaction = true;
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.DIVIDEND_PAYOUT;
        return handleDeposit(account, fmt, transactionDate, transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction,
                savingsAccountTransactionType, backdatedTxnsAllowedTill);
    }

    private void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    private Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.saveAndFlush(transaction);
        return transaction.getId();
    }

    private void saveUpdatedTransactionsOfSavingsAccount(final List<SavingsAccountTransaction> savingsAccountTransactions) {
        this.savingsAccountTransactionRepository.saveAll(savingsAccountTransactions);
    }

    private void updateTransactionDetailsWithPivotConfig(final SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findCurrentTransactionIdsWithPivotDateConfig());
        existingReversedTransactionIds.addAll(account.findCurrentReversedTransactionIdsWithPivotDateConfig());
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, boolean isAccountTransfer, final boolean backdatedTxnsAllowedTill,
            final boolean isNegativeBalance) {

        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(savingsAccount.getCurrency().getCode(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, backdatedTxnsAllowedTill);
        accountingBridgeData.put("isNegativeBalance", isNegativeBalance);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }

    @Transactional
    @Override
    public void postJournalEntries(final SavingsAccount account, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, final boolean backdatedTxnsAllowedTill, boolean isNegativeBalance) {

        final boolean isAccountTransfer = false;
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, backdatedTxnsAllowedTill,
                isNegativeBalance);
    }

    @Override
    public SavingsAccountTransaction handleReversal(SavingsAccount account, List<SavingsAccountTransaction> savingsAccountTransactions,
            boolean backdatedTxnsAllowedTill) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        final boolean postReversals = true;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        if (backdatedTxnsAllowedTill) {
            updateTransactionDetailsWithPivotConfig(account, existingTransactionIds, existingReversedTransactionIds);
        } else {
            updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        }
        List<SavingsAccountTransaction> newTransactions = new ArrayList<>();
        SavingsAccountTransaction reversal = null;

        Set<SavingsAccountChargePaidBy> chargePaidBySet = null;
        for (SavingsAccountTransaction savingsAccountTransaction : savingsAccountTransactions) {
            reversal = SavingsAccountTransaction.reversal(savingsAccountTransaction);
            chargePaidBySet = savingsAccountTransaction.getSavingsAccountChargesPaid();
            reversal.getSavingsAccountChargesPaid().addAll(chargePaidBySet);
            account.undoTransaction(savingsAccountTransaction);
            if (postReversals) {
                newTransactions.add(reversal);
            }
        }

        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());
        for (SavingsAccountTransaction savingsAccountTransaction : savingsAccountTransactions) {
            if (savingsAccountTransaction.isPostInterestCalculationRequired()
                    && account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), backdatedTxnsAllowedTill)) {

                postInterest(account, mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
            } else {
                account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
            }
            account.validatePivotDateTransaction(savingsAccountTransaction.getTransactionDate(), backdatedTxnsAllowedTill,
                    relaxingDaysConfigForPivotDate, "savingsaccount");
            account.validateAccountBalanceDoesNotBecomeNegativeMinimal(savingsAccountTransaction.getAmount(), false);
            account.activateAccountBasedOnBalance();
        }
        this.savingsAccountRepository.save(account);
        newTransactions.addAll(account.getSavingsAccountTransactionsWithPivotConfig());
        this.savingsAccountTransactionRepository.saveAll(newTransactions);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false, backdatedTxnsAllowedTill);

        return reversal;
    }

    @Override
    public void postInterest(SavingsAccount account, final MathContext mc, final LocalDate interestPostingUpToDate,
            final boolean isInterestTransfer, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill,
            final boolean postReversals) {
        final List<PostingPeriod> postingPeriods = account.calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                postReversals);
        log.debug("postInterest {}", postingPeriods.size());

        MonetaryCurrency currency = account.getCurrency();
        Money interestPostedToDate = Money.zero(currency);

        if (backdatedTxnsAllowedTill) {
            interestPostedToDate = Money.of(currency, account.getSummary().getTotalInterestPosted());
        }

        boolean recalucateDailyBalanceDetails = false;
        boolean applyWithHoldTax = account.isWithHoldTaxApplicableForInterestPosting();
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();

        if (backdatedTxnsAllowedTill) {
            withholdTransactions.addAll(account.findWithHoldSavingsTransactionsWithPivotConfig());
        } else {
            withholdTransactions.addAll(account.findWithHoldTransactions());
        }

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            log.debug("  period: {}", interestPostingPeriod.dateOfPostingTransaction());

            final LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();
            log.debug("  interestEarnedToBePostedForPeriod: {}", interestEarnedToBePostedForPeriod.toString());

            if (!interestPostingTransactionDate.isAfter(interestPostingUpToDate)) {
                interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

                SavingsAccountTransaction postingTransaction = null;
                if (backdatedTxnsAllowedTill) {
                    postingTransaction = account.findInterestPostingSavingsTransactionWithPivotConfig(interestPostingTransactionDate);
                } else {
                    postingTransaction = account.findInterestPostingTransactionFor(interestPostingTransactionDate);
                }
                if (postingTransaction == null) {
                    SavingsAccountTransaction newPostingTransaction = null;
                    if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(currency))) {
                        if (interestEarnedToBePostedForPeriod.isGreaterThan(Money.zero(currency))) {
                            newPostingTransaction = SavingsAccountTransaction.interestPosting(account, account.office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                    interestPostingPeriod.isUserPosting());
                        }
                    } else {
                        newPostingTransaction = SavingsAccountTransaction.overdraftInterest(account, account.office(),
                                interestPostingTransactionDate, interestEarnedToBePostedForPeriod.negated(),
                                interestPostingPeriod.isUserPosting());
                    }
                    if (newPostingTransaction != null) {
                        if (backdatedTxnsAllowedTill) {
                            account.addTransactionToExisting(newPostingTransaction);
                        } else {
                            account.addTransaction(newPostingTransaction);
                        }
                        if (account.savingsProduct().isAccrualBasedAccountingEnabled()) {
                            if (MathUtil.isGreaterThanZero(interestEarnedToBePostedForPeriod)) {
                                SavingsAccountTransaction accrualTransaction = SavingsAccountTransaction.accrual(account, account.office(),
                                        interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                        interestPostingPeriod.isUserPosting(), false);
                                if (backdatedTxnsAllowedTill) {
                                    account.addTransactionToExisting(accrualTransaction);
                                } else {
                                    account.addTransaction(accrualTransaction);
                                }
                            } else {
                                log.info("Accrual for Overdraft interest");
                            }
                        }
                        if (applyWithHoldTax) {
                            account.createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                    backdatedTxnsAllowedTill);
                        }
                    }
                    recalucateDailyBalanceDetails = true;
                } else {
                    boolean correctionRequired = false;
                    if (postingTransaction.isInterestPostingAndNotReversed()) {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                    } else {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod.negated());
                    }
                    log.debug("  correctionRequired {}", correctionRequired);
                    if (correctionRequired) {
                        boolean applyWithHoldTaxForOldTransaction = false;
                        postingTransaction.reverse();
                        SavingsAccountTransaction reversal = null;
                        if (postReversals) {
                            reversal = SavingsAccountTransaction.reversal(postingTransaction);
                        }
                        final SavingsAccountTransaction withholdTransaction = account.findTransactionFor(interestPostingTransactionDate,
                                withholdTransactions);
                        if (withholdTransaction != null) {
                            withholdTransaction.reverse();
                            applyWithHoldTaxForOldTransaction = true;
                        }
                        SavingsAccountTransaction newPostingTransaction;
                        if (interestEarnedToBePostedForPeriod.isGreaterThanOrEqualTo(Money.zero(currency))) {
                            newPostingTransaction = SavingsAccountTransaction.interestPosting(account, account.office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                    interestPostingPeriod.isUserPosting());
                        } else {
                            newPostingTransaction = SavingsAccountTransaction.overdraftInterest(account, account.office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod.negated(),
                                    interestPostingPeriod.isUserPosting());
                        }
                        if (backdatedTxnsAllowedTill) {
                            account.addTransactionToExisting(newPostingTransaction);
                            if (reversal != null) {
                                account.addTransactionToExisting(reversal);
                            }
                        } else {
                            account.addTransaction(newPostingTransaction);
                            if (reversal != null) {
                                account.addTransaction(reversal);
                            }
                        }
                        if (account.savingsProduct().isAccrualBasedAccountingEnabled()
                                && MathUtil.isGreaterThanZero(interestEarnedToBePostedForPeriod)) {
                            log.info("TX2: {}", interestEarnedToBePostedForPeriod.getAmount());
                            SavingsAccountTransaction accrualTransaction = SavingsAccountTransaction.accrual(account, account.office(),
                                    interestPostingTransactionDate, interestEarnedToBePostedForPeriod,
                                    interestPostingPeriod.isUserPosting(), false);
                            if (backdatedTxnsAllowedTill) {
                                account.addTransactionToExisting(accrualTransaction);
                            } else {
                                account.addTransaction(accrualTransaction);
                            }
                        } else {
                            log.info("Accrual for Overdraft2 interest");
                        }
                        if (applyWithHoldTaxForOldTransaction) {
                            account.createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                    backdatedTxnsAllowedTill);
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
            Money openingAccountBalance = Money.zero(currency);

            if (backdatedTxnsAllowedTill) {
                if (account.getSummary().getLastInterestCalculationDate() == null) {
                    openingAccountBalance = Money.zero(currency);
                } else {
                    openingAccountBalance = Money.of(currency, account.getSummary().getRunningBalanceOnPivotDate());
                }
            }

            // update existing transactions so derived balance fields are
            // correct.
            account.recalculateDailyBalances(openingAccountBalance, interestPostingUpToDate, backdatedTxnsAllowedTill, postReversals);
        }

        if (!backdatedTxnsAllowedTill) {
            account.getSummary().updateSummary(currency, account.savingsAccountTransactionSummaryWrapper, account.getTransactions());
        } else {
            account.getSummary().updateSummaryWithPivotConfig(currency, account.savingsAccountTransactionSummaryWrapper, null,
                    account.savingsAccountTransactions);
        }
    }

    @Override
    public void reverseTransfer(SavingsAccountTransaction savingsAccountTransaction, boolean backdatedTxnsAllowedTill) {
        final SavingsAccount account = savingsAccountTransaction.getSavingsAccount();
        account.setHelpers(savingsAccountTransactionSummaryWrapper, savingsHelper);

        undoTransaction(account, savingsAccountTransaction);
    }

    @Override
    public void undoTransaction(SavingsAccount account, SavingsAccountTransaction savingsAccountTransaction) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Long savingsId = account.getId();
        final Long transactionId = savingsAccountTransaction.getId();

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(savingsAccountTransaction.getTransactionDate(),
                account);

        if (!account.allowModify()) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed for this savings type", transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());

        if (account.isNotActive()) {
            throwValidationExceptionForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId);

        // undoing transaction is withdrawal then undo withdrawal fee transaction if any
        if (savingsAccountTransaction.isWithdrawal()) {
            final SavingsAccountTransaction nextSavingsAccountTransaction = this.savingsAccountTransactionRepository
                    .findOneByIdAndSavingsAccountId(transactionId + 1, savingsId);
            if (nextSavingsAccountTransaction != null && nextSavingsAccountTransaction.isWithdrawalFeeAndNotReversed()) {
                account.undoTransaction(transactionId + 1);
            }
        }
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        checkClientOrGroupActive(account);
        if (savingsAccountTransaction.isPostInterestCalculationRequired()
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            postInterest(account, mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, false, postReversals);
        } else {
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }
        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.undoTransactionAction, depositAccountOnHoldTransactions,
                false);
        account.activateAccountBasedOnBalance();
        savingsAccountRepository.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false, false);
    }

    private void throwValidationExceptionForActiveStatus(final String actionName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + actionName);
        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.active");
        throw new PlatformApiDataValidationException(dataValidationErrors);
    }

    @Override
    public void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = account.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }
}
