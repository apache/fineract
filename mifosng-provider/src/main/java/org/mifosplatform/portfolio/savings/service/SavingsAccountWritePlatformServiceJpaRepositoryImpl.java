/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsAccountWritePlatformServiceJpaRepositoryImpl implements SavingsAccountWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public SavingsAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsAccountRepositoryWrapper savingAccountRepository,
            final SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator,
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountTransactionDataValidator = savingsAccountTransactionDataValidator;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult activate(final Long savingsId, final JsonCommand command) {

        final AppUser user = this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validateActivation(command);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final Map<String, Object> changes = account.activate(user, command, DateUtils.getLocalDateOfTenant(),
                existingReversedTransactionIds, existingReversedTransactionIds);
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(account);
        }

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deposit(final Long savingsId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validate(command);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        PaymentDetail paymentDetail = paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final SavingsAccountTransaction deposit = account.deposit(fmt, transactionDate, transactionAmount, existingTransactionIds,
                existingReversedTransactionIds, paymentDetail);

        if (account.isBeforeLastPostingPeriod(transactionDate)) {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.postInterest(mc, today, existingTransactionIds, existingReversedTransactionIds);
        } else {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.calculateInterestUsing(mc, today);
        }

        final Long transactionId = saveTransactionToGenerateTransactionId(deposit);

        this.savingAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(transactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();
    }

    private Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.save(transaction);
        return transaction.getId();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validate(command);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        PaymentDetail paymentDetail = paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final SavingsAccountTransaction withdrawal = account.withdraw(fmt, transactionDate, transactionAmount, existingTransactionIds,
                existingReversedTransactionIds, paymentDetail);

        if (account.isBeforeLastPostingPeriod(transactionDate)) {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.postInterest(mc, today, existingTransactionIds, existingReversedTransactionIds);
        } else {
            final LocalDate today = DateUtils.getLocalDateOfTenant();
            account.calculateInterestUsing(mc, today);
        }

        final Long transactionId = saveTransactionToGenerateTransactionId(withdrawal);
        this.savingAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(transactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult applyAnnualFee(final Long savingsId, final LocalDate annualFeeTransactionDate) {

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MM yyyy");

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final MathContext mc = MathContext.DECIMAL64;
        final LocalDate today = DateUtils.getLocalDateOfTenant();

        final SavingsAccountTransaction annualFee = account.addAnnualFee(mc, fmt, annualFeeTransactionDate, today, existingTransactionIds,
                existingReversedTransactionIds);
        final Long transactionId = saveTransactionToGenerateTransactionId(annualFee);
        this.savingAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(transactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult calculateInterest(final Long savingsId) {

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(15, RoundingMode.HALF_EVEN);

        account.calculateInterestUsing(mc, today);

        this.savingAccountRepository.save(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult postInterest(final Long savingsId) {

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);

        account.postInterest(mc, today, existingTransactionIds, existingReversedTransactionIds);
        this.savingAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Override
    public CommandProcessingResult undoTransaction(final Long savingsId, final Long transactionId) {

        final List<Long> newTransactionIds = new ArrayList<Long>();
        final List<Long> reversedTransactionIds = new ArrayList<Long>();

        SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository.findOneByIdAndSavingsAccountId(
                transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            // throw new not found exception
        }

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(15, RoundingMode.HALF_EVEN);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        account.undoTransaction(transactionId, reversedTransactionIds, mc, today);
        checkClientOrGroupActive(account);

        postJournalEntries(account, newTransactionIds, reversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds);
        journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }

    private void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null) {
            if (client.isNotActive()) { throw new ClientNotActiveException(client.getId()); }
        }
        final Group group = account.group();
        if (group != null) {
            if (group.isNotActive()) { throw new GroupNotActiveException(group.getId()); }
        }
    }
}