/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.onAccountClosureIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.toSavingsAccountIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferDTO;
import org.mifosplatform.portfolio.account.domain.AccountTransferType;
import org.mifosplatform.portfolio.account.service.AccountTransfersWritePlatformService;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.DepositAccountOnClosureType;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.DepositsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsTransactionBooleanValues;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositAccountDomainServiceJpa implements DepositAccountDomainService {

    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;

    @Autowired
    public DepositAccountDomainServiceJpa(final SavingsAccountRepositoryWrapper savingsAccountRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final AccountNumberGenerator accountNumberGenerator,
            final DepositAccountAssembler depositAccountAssembler, final SavingsAccountDomainService savingsAccountDomainService,
            final AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            final ConfigurationDomainService configurationDomainService,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository) {
        this.savingsAccountRepository = savingsAccountRepository;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.accountNumberGenerator = accountNumberGenerator;
        this.depositAccountAssembler = depositAccountAssembler;
        this.savingsAccountDomainService = savingsAccountDomainService;
        this.accountTransfersWritePlatformService = accountTransfersWritePlatformService;
        this.configurationDomainService = configurationDomainService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleWithdrawal(final SavingsAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
            final boolean applyWithdrawFee, final boolean isRegularTransaction) {
        boolean isAccountTransfer = false;
        boolean isInterestTransfer = false;
        boolean isWithdrawBalance = false;

        SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                isRegularTransaction, applyWithdrawFee, isInterestTransfer, isWithdrawBalance);
        return this.savingsAccountDomainService.handleWithdrawal(account, fmt, transactionDate, transactionAmount, paymentDetail,
                transactionBooleanValues);
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleFDDeposit(final FixedDepositAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail) {
        boolean isAccountTransfer = false;
        boolean isRegularTransaction = false;
        return this.savingsAccountDomainService.handleDeposit(account, fmt, transactionDate, transactionAmount, paymentDetail,
                isAccountTransfer, isRegularTransaction);
    }

    @Transactional
    @Override
    public SavingsAccountTransaction handleRDDeposit(final RecurringDepositAccount account, final DateTimeFormatter fmt,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final PaymentDetail paymentDetail,
            final boolean isRegularTransaction) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        boolean isAccountTransfer = false;
        final boolean isPreMatureClosure = false;
        final MathContext mc = MathContext.DECIMAL64;
        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(account, fmt, transactionDate,
                transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction);

        account.handleScheduleInstallments(deposit);
        account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        account.updateOverduePayments(DateUtils.getLocalDateOfTenant());
        return deposit;
    }

    @Transactional
    @Override
    public Long handleFDAccountClosure(final FixedDepositAccount account, final PaymentDetail paymentDetail, final AppUser user,
            final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        boolean isRegularTransaction = false;
        boolean isAccountTransfer = false;
        final boolean isPreMatureClosure = false;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        /*
         * <<<<<<< HEAD final SavingsAccountTransactionDTO transactionDTO = new
         * SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
         * paymentDetail, new Date()); final SavingsAccountTransaction deposit =
         * account.deposit(transactionDTO); boolean isInterestTransfer = false;
         * final MathContext mc = MathContext.DECIMAL64; if
         * (account.isBeforeLastPostingPeriod(transactionDate)) { final
         * LocalDate today = DateUtils.getLocalDateOfTenant();
         * account.postInterest(mc, today, isInterestTransfer); } else { final
         * LocalDate today = DateUtils.getLocalDateOfTenant();
         * account.calculateInterestUsing(mc, today, isInterestTransfer);
         * =======
         */
        final MathContext mc = MathContext.DECIMAL64;
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        Long savingsTransactionId = null;
        account.postMaturityInterest(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        if (onClosureType.isReinvest()) {
            FixedDepositAccount reinvestedDeposit = account.reInvest(account.getAccountBalance());
            this.depositAccountAssembler.assignSavingAccountHelpers(reinvestedDeposit);
            reinvestedDeposit.updateMaturityDateAndAmountBeforeAccountActivation(mc, isPreMatureClosure,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
            this.savingsAccountRepository.save(reinvestedDeposit);
            autoGenerateAccountNumber(reinvestedDeposit);
            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();
        } else if (onClosureType.isTransferToSavings()) {
            final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
            final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
            final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                    DepositAccountType.SAVINGS_DEPOSIT);
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(closedDate, account.getAccountBalance(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS, null, null, transferDescription, locale, fmt, null, null,
                    null, null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, toSavingsAccount, account,
                    isAccountTransfer, isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } else {
            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();
        }

        /***
         * Update account transactionIds for post journal entries.
         */
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.close(user, command, tenantsTodayDate, changes);
        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);

        return savingsTransactionId;
    }

    @Transactional
    @Override
    public Long handleRDAccountClosure(final RecurringDepositAccount account, final PaymentDetail paymentDetail, final AppUser user,
            final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        boolean isRegularTransaction = false;
        boolean isAccountTransfer = false;
        final boolean isPreMatureClosure = false;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final MathContext mc = MathContext.DECIMAL64;
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        Long savingsTransactionId = null;
        account.postMaturityInterest(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, closedDate);
        final BigDecimal transactionAmount = account.getAccountBalance();
        final Integer onAccountClosureId = command.integerValueOfParameterNamed(onAccountClosureIdParamName);
        final DepositAccountOnClosureType onClosureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        if (onClosureType.isReinvest()) {
            RecurringDepositAccount reinvestedDeposit = account.reInvest(transactionAmount);
            depositAccountAssembler.assignSavingAccountHelpers(reinvestedDeposit);
            reinvestedDeposit.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            reinvestedDeposit.processAccountUponActivation(fmt, user);
            reinvestedDeposit.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            this.savingsAccountRepository.save(reinvestedDeposit);

            Money amountForDeposit = reinvestedDeposit.activateWithBalance();
            if (amountForDeposit.isGreaterThanZero()) {
                handleRDDeposit(reinvestedDeposit, fmt, reinvestedDeposit.getActivationLocalDate(), amountForDeposit.getAmount(),
                        paymentDetail, isRegularTransaction);
            }
            reinvestedDeposit.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            this.savingsAccountRepository.save(reinvestedDeposit);
            autoGenerateAccountNumber(reinvestedDeposit);

            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();

        } else if (onClosureType.isTransferToSavings()) {
            final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
            final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
            final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                    DepositAccountType.SAVINGS_DEPOSIT);
            final boolean isExceptionForBalanceCheck = false;
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(closedDate, transactionAmount,
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS, null, null, transferDescription, locale, fmt, null, null,
                    null, null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, toSavingsAccount, account,
                    isRegularTransaction, isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } else {
            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();
        }

        /***
         * Update account transactionIds for post journal entries.
         */
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.close(user, command, tenantsTodayDate, changes);

        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);

        return savingsTransactionId;
    }

    private void autoGenerateAccountNumber(final SavingsAccount account) {
        if (account.isAccountNumberRequiresAutoGeneration()) {
            final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.SAVINGS);
            account.updateAccountNo(this.accountNumberGenerator.generate(account, accountNumberFormat));
            this.savingsAccountRepository.save(account);
        }
    }

    @Transactional
    @Override
    public Long handleFDAccountPreMatureClosure(final FixedDepositAccount account, final PaymentDetail paymentDetail, final AppUser user,
            final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        boolean isAccountTransfer = false;
        boolean isRegularTransaction = false;
        final boolean isPreMatureClosure = true;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        Long savingsTransactionId = null;
        // post interest
        account.postPreMaturityInterest(closedDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        final Integer closureTypeValue = command.integerValueOfParameterNamed(DepositsApiConstants.onAccountClosureIdParamName);
        DepositAccountOnClosureType closureType = DepositAccountOnClosureType.fromInt(closureTypeValue);

        if (closureType.isTransferToSavings()) {
            final boolean isExceptionForBalanceCheck = false;
            final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
            final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
            final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                    DepositAccountType.SAVINGS_DEPOSIT);
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(closedDate, account.getAccountBalance(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS, null, null, transferDescription, locale, fmt, null, null,
                    null, null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, toSavingsAccount, account,
                    isRegularTransaction, isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } else {
            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();
        }

        /***
         * Update account transactionIds for post journal entries.
         */
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.prematureClosure(user, command, tenantsTodayDate, changes);

        this.savingsAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        return savingsTransactionId;
    }

    @Transactional
    @Override
    public Long handleRDAccountPreMatureClosure(final RecurringDepositAccount account, final PaymentDetail paymentDetail,
            final AppUser user, final JsonCommand command, final LocalDate tenantsTodayDate, final Map<String, Object> changes) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        boolean isAccountTransfer = false;
        final boolean isPreMatureClosure = true;
        boolean isRegularTransaction = false;
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        Long savingsTransactionId = null;
        // post interest
        account.postPreMaturityInterest(closedDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth);

        final Integer closureTypeValue = command.integerValueOfParameterNamed(DepositsApiConstants.onAccountClosureIdParamName);
        DepositAccountOnClosureType closureType = DepositAccountOnClosureType.fromInt(closureTypeValue);

        if (closureType.isTransferToSavings()) {
            final boolean isExceptionForBalanceCheck = false;
            final Long toSavingsId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
            final String transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
            final SavingsAccount toSavingsAccount = this.depositAccountAssembler.assembleFrom(toSavingsId,
                    DepositAccountType.SAVINGS_DEPOSIT);
            final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(closedDate, account.getAccountBalance(),
                    PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS, null, null, transferDescription, locale, fmt, null, null,
                    null, null, null, AccountTransferType.ACCOUNT_TRANSFER.getValue(), null, null, null, null, toSavingsAccount, account,
                    isRegularTransaction, isExceptionForBalanceCheck);
            this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } else {
            final SavingsAccountTransaction withdrawal = this.handleWithdrawal(account, fmt, closedDate, account.getAccountBalance(),
                    paymentDetail, false, isRegularTransaction);
            savingsTransactionId = withdrawal.getId();
        }

        /***
         * Update account transactionIds for post journal entries.
         */
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.prematureClosure(user, command, tenantsTodayDate, changes);
        this.savingsAccountRepository.save(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        return savingsTransactionId;
    }

    private void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, boolean isAccountTransfer) {

        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }
}