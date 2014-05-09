/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.toSavingsAccountIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetailRepository;
import org.mifosplatform.portfolio.account.domain.AccountTransferDetails;
import org.mifosplatform.portfolio.account.domain.AccountTransferTransaction;
import org.mifosplatform.portfolio.account.domain.AccountTransferType;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositAccountDomainServiceJpa implements DepositAccountDomainService {

    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;

    @Autowired
    public DepositAccountDomainServiceJpa(final SavingsAccountRepositoryWrapper savingsAccountRepository,
            final SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,
            final AccountTransferDetailRepository accountTransferDetailRepository, final DepositAccountAssembler depositAccountAssembler,
            final SavingsAccountDomainService savingsAccountDomainService) {
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.accountTransferDetailRepository = accountTransferDetailRepository;
        this.depositAccountAssembler = depositAccountAssembler;
        this.savingsAccountDomainService = savingsAccountDomainService;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleWithdrawal(final SavingsAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
            final boolean applyWithdrawFee) {

        final Set<Long> existingTransactionIds = new HashSet<Long>();
        final Set<Long> existingReversedTransactionIds = new HashSet<Long>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                paymentDetail, new Date());
        final SavingsAccountTransaction withdrawal = account.withdraw(transactionDTO, applyWithdrawFee);
        boolean isInterestTransfer = false;
        final MathContext mc = MathContext.DECIMAL64;
        if (account.isBeforeLastPostingPeriod(transactionDate)) {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.postInterest(mc, today, isInterestTransfer);
        } else {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.calculateInterestUsing(mc, today, isInterestTransfer);
        }
        account.validateAccountBalanceDoesNotBecomeNegative(transactionAmount);
        saveTransactionToGenerateTransactionId(withdrawal);
        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return withdrawal;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleDeposit(final SavingsAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail) {

        final Set<Long> existingTransactionIds = new HashSet<Long>();
        final Set<Long> existingReversedTransactionIds = new HashSet<Long>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                paymentDetail, new Date());
        final SavingsAccountTransaction deposit = account.deposit(transactionDTO);
        boolean isInterestTransfer = false;
        final MathContext mc = MathContext.DECIMAL64;
        if (account.isBeforeLastPostingPeriod(transactionDate)) {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.postInterest(mc, today, isInterestTransfer);
        } else {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.calculateInterestUsing(mc, today, isInterestTransfer);
        }

        saveTransactionToGenerateTransactionId(deposit);

        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return deposit;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleAccountClosure(final SavingsAccount account, final PaymentDetail paymentDetail,
            final AppUser user, final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final Set<Long> existingTransactionIds = new HashSet<Long>();
        final Set<Long> existingReversedTransactionIds = new HashSet<Long>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final DepositAccountType depositAccountType = account.depositAccountType();
        SavingsAccountTransaction withdrawal = null;
        final MathContext mc = MathContext.DECIMAL64;
        if (depositAccountType.isFixedDeposit()) {
            final FixedDepositAccount fdAccount = (FixedDepositAccount) account;
            withdrawal = fdAccount.close(user, command, tenantsTodayDate, paymentDetail, changes);
            final Money transactionAmount = withdrawal.getAmount(fdAccount.getCurrency());
            final LocalDate transactionDate = withdrawal.transactionLocalDate();
            if (fdAccount.isReinvestOnClosure()) {
                FixedDepositAccount reinvestedDeposit = fdAccount.reInvest(transactionAmount);
                fdAccount.updateMaturityDateAndAmountBeforeAccountActivation(mc);
                this.savingsAccountRepository.save(reinvestedDeposit);
                autoGenerateAccountNumber(reinvestedDeposit);
            } else if (fdAccount.isTransferToSavingsOnClosure()) {
                final Locale locale = command.extractLocale();
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
                final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
                final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
                final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                        DepositAccountType.SAVINGS_DEPOSIT);
                final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                        withdrawal.transactionLocalDate(), transactionAmount.getAmount(), null);

                final AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToSavingsTransfer(fdAccount.office(),
                        fdAccount.getClient(), fdAccount, toSavingsAccount.office(), toSavingsAccount.getClient(), toSavingsAccount,
                        AccountTransferType.ACCOUNT_TRANSFER.getValue());
                AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(
                        accountTransferDetails, withdrawal, deposit, transactionDate, transactionAmount, transferDescription);

                accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        } else if (depositAccountType.isRecurringDeposit()) {
            final RecurringDepositAccount rdAccount = (RecurringDepositAccount) account;
            withdrawal = rdAccount.close(user, command, tenantsTodayDate, paymentDetail, changes);
            final Money transactionAmount = withdrawal.getAmount(rdAccount.getCurrency());
            final LocalDate transactionDate = withdrawal.transactionLocalDate();
            if (rdAccount.isReinvestOnClosure()) {
                RecurringDepositAccount reinvestedDeposit = rdAccount.reInvest(transactionAmount);
                depositAccountAssembler.assignSavingAccountHelpers(reinvestedDeposit);
                final Locale locale = command.extractLocale();
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
                final LocalDate transactionStartDate = reinvestedDeposit.getActivationLocalDate();
                final LocalDate nextTransactionStartDate = reinvestedDeposit.nextDepositDate(transactionStartDate);
                reinvestedDeposit.updateMaturityDateAndAmount(mc, nextTransactionStartDate);
                this.savingsAccountRepository.save(reinvestedDeposit);
                Money amountForDeposit = reinvestedDeposit.activateWithBalance();
                if (amountForDeposit.isGreaterThanZero()) {
                    handleDeposit(reinvestedDeposit, fmt, reinvestedDeposit.getActivationLocalDate(), amountForDeposit.getAmount(), null);
                }
                reinvestedDeposit.updateMaturityDateAndAmount(mc, nextTransactionStartDate);

                this.savingsAccountRepository.save(reinvestedDeposit);
                autoGenerateAccountNumber(reinvestedDeposit);
            } else if (rdAccount.isTransferToSavingsOnClosure()) {
                final Locale locale = command.extractLocale();
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
                final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
                final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
                final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                        DepositAccountType.SAVINGS_DEPOSIT);
                final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                        withdrawal.transactionLocalDate(), transactionAmount.getAmount(), null);

                final AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToSavingsTransfer(rdAccount.office(),
                        rdAccount.getClient(), rdAccount, toSavingsAccount.office(), toSavingsAccount.getClient(), toSavingsAccount,
                        AccountTransferType.ACCOUNT_TRANSFER.getValue());
                AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(
                        accountTransferDetails, withdrawal, deposit, transactionDate, transactionAmount, transferDescription);

                accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }

        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return withdrawal;
    }

    private void autoGenerateAccountNumber(final SavingsAccount account) {
        if (account.isAccountNumberRequiresAutoGeneration()) {
            final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                    .determineSavingsAccountNoGenerator(account.getId());
            account.updateAccountNo(accountNoGenerator.generate());
            this.savingsAccountRepository.save(account);
        }
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleAccountPreMatureClosure(final SavingsAccount account, final PaymentDetail paymentDetail,
            final AppUser user, final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final Set<Long> existingTransactionIds = new HashSet<Long>();
        final Set<Long> existingReversedTransactionIds = new HashSet<Long>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final DepositAccountType depositAccountType = account.depositAccountType();
        SavingsAccountTransaction withdrawal = null;
        if (depositAccountType.isFixedDeposit()) {
            final FixedDepositAccount fdAccount = (FixedDepositAccount) account;
            withdrawal = fdAccount.prematureClosure(user, command, tenantsTodayDate, paymentDetail, changes);
            final Money transactionAmount = withdrawal.getAmount(fdAccount.getCurrency());
            final LocalDate transactionDate = withdrawal.transactionLocalDate();
            if (fdAccount.isTransferToSavingsOnClosure()) {
                final Locale locale = command.extractLocale();
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
                final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
                final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
                final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                        DepositAccountType.SAVINGS_DEPOSIT);
                final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                        withdrawal.transactionLocalDate(), transactionAmount.getAmount(), null);

                final AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToSavingsTransfer(fdAccount.office(),
                        fdAccount.getClient(), fdAccount, toSavingsAccount.office(), toSavingsAccount.getClient(), toSavingsAccount,
                        AccountTransferType.ACCOUNT_TRANSFER.getValue());
                AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(
                        accountTransferDetails, withdrawal, deposit, transactionDate, transactionAmount, transferDescription);

                accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        } else if (depositAccountType.isRecurringDeposit()) {
            final RecurringDepositAccount rdAccount = (RecurringDepositAccount) account;
            withdrawal = rdAccount.prematureClosure(user, command, tenantsTodayDate, paymentDetail, changes);
            final Money transactionAmount = withdrawal.getAmount(rdAccount.getCurrency());
            final LocalDate transactionDate = withdrawal.transactionLocalDate();
            if (rdAccount.isTransferToSavingsOnClosure()) {
                final Locale locale = command.extractLocale();
                final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
                final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
                final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
                final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                        DepositAccountType.SAVINGS_DEPOSIT);
                final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(toSavingsAccount, fmt,
                        transactionDate, transactionAmount.getAmount(), null);

                final AccountTransferDetails accountTransferDetails = AccountTransferDetails.savingsToSavingsTransfer(rdAccount.office(),
                        rdAccount.getClient(), rdAccount, toSavingsAccount.office(), toSavingsAccount.getClient(), toSavingsAccount,
                        AccountTransferType.ACCOUNT_TRANSFER.getValue());
                AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(
                        accountTransferDetails, withdrawal, deposit, transactionDate, transactionAmount, transferDescription);

                accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);

                this.accountTransferDetailRepository.save(accountTransferDetails);
            }
        }
        if (withdrawal != null) {
            saveTransactionToGenerateTransactionId(withdrawal);
        }

        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return withdrawal;
    }

    private Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.save(transaction);
        return transaction.getId();
    }

    private void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }
}