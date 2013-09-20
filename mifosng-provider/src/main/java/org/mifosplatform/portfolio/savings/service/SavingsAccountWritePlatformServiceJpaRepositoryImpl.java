/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargeIdParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.mifosplatform.portfolio.account.service.AccountTransfersReadPlatformService;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.exception.ClientNotActiveException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.exception.GroupNotActiveException;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountCharge;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountDomainService;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountRepository;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountStatusType;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransaction;
import org.mifosplatform.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountClosingNotAllowedException;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.mifosplatform.portfolio.savings.exception.TransactionUpdateNotAllowedException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingsAccountWritePlatformServiceJpaRepositoryImpl implements SavingsAccountWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepository savingAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator;
    private final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final NoteRepository noteRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final ChargeRepositoryWrapper chargeRepository;
    private final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository;

    @Autowired
    public SavingsAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsAccountRepository savingAccountRepository,
            final SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            final SavingsAccountAssembler savingAccountAssembler,
            final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator,
            final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            final PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final SavingsAccountDomainService savingsAccountDomainService, final NoteRepository noteRepository,
            final AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final ChargeRepositoryWrapper chargeRepository, final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.savingAccountAssembler = savingAccountAssembler;
        this.savingsAccountTransactionDataValidator = savingsAccountTransactionDataValidator;
        this.savingsAccountChargeDataValidator = savingsAccountChargeDataValidator;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.applicationCurrencyRepositoryWrapper = applicationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.savingsAccountDomainService = savingsAccountDomainService;
        this.noteRepository = noteRepository;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.chargeRepository = chargeRepository;
        this.savingsAccountChargeRepository = savingsAccountChargeRepository;
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

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(account, fmt, transactionDate,
                transactionAmount, paymentDetail);

        return new CommandProcessingResultBuilder() //
                .withEntityId(deposit.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    private Long saveTransactionToGenerateTransactionId(final SavingsAccountTransaction transaction) {
        this.savingsAccountTransactionRepository.save(transaction);
        return transaction.getId();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command) {

        this.savingsAccountTransactionDataValidator.validate(command);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);

        final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(account, fmt, transactionDate,
                transactionAmount, paymentDetail, true);

        return new CommandProcessingResultBuilder() //
                .withEntityId(withdrawal.getId()) //
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

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        checkClientOrGroupActive(account);
        postInterest(account);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    private void postInterest(final SavingsAccount account) {
        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);

        account.postInterest(mc, today, existingTransactionIds, existingReversedTransactionIds);
        this.savingAccountRepository.save(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    @CronTarget(jobName = JobName.POST_INTEREST_FOR_SAVINGS)
    @Override
    public void postInterestForAccounts() {
        final List<SavingsAccount> savingsAccounts = this.savingAccountRepository.findSavingAccountByStatus(SavingsAccountStatusType.ACTIVE
                .getValue());
        for (final SavingsAccount savingsAccount : savingsAccounts) {
            this.savingAccountAssembler.assignSavingAccountHelpers(savingsAccount);
            postInterest(savingsAccount);
        }
    }

    @Override
    public CommandProcessingResult undoTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {

        final List<Long> newTransactionIds = new ArrayList<Long>();
        final List<Long> reversedTransactionIds = new ArrayList<Long>();

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) { throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId); }

        if (!allowAccountTransferModification
                && this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) { throw new PlatformServiceUnavailableException(
                "error.msg.saving.account.transfer.transaction.update.not.allowed", "Savings account transaction:" + transactionId
                        + " update not allowed as it involves in account transfer", transactionId); }

        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final MathContext mc = new MathContext(15, RoundingMode.HALF_EVEN);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId, reversedTransactionIds);
        checkClientOrGroupActive(account);
        if ((savingsAccountTransaction.isDeposit() || savingsAccountTransaction.isWithdrawal())
                && account.isBeforeLastPostingPeriod(savingsAccountTransaction.transactionLocalDate())) {
            account.postInterest(mc, today, newTransactionIds, reversedTransactionIds);
        } else {
            account.calculateInterestUsing(mc, today);
        }
        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.undoTransactionAction);
        account.activateAccountBasedOnBalance();
        postJournalEntries(account, newTransactionIds, reversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Override
    public CommandProcessingResult adjustSavingsTransaction(final Long savingsId, final Long transactionId, final JsonCommand command) {

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) { throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId); }

        if (!(savingsAccountTransaction.isDeposit() || savingsAccountTransaction.isWithdrawal()) || savingsAccountTransaction.isReversed()) { throw new TransactionUpdateNotAllowedException(
                savingsId, transactionId); }

        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) { throw new PlatformServiceUnavailableException(
                "error.msg.saving.account.transfer.transaction.update.not.allowed", "Savings account transaction:" + transactionId
                        + " update not allowed as it involves in account transfer", transactionId); }

        this.savingsAccountTransactionDataValidator.validate(command);

        final LocalDate today = DateUtils.getLocalDateOfTenant();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.adjustTransactionAction);
        }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(SavingsApiConstants.transactionDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.transactionAmountParamName);
        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();
        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        account.undoTransaction(transactionId, existingReversedTransactionIds);

        // for undo withdrawal fee
        final SavingsAccountTransaction nextSavingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId + 1, savingsId);
        if (nextSavingsAccountTransaction != null && nextSavingsAccountTransaction.isWithdrawalFeeAndNotReversed()) {
            account.undoTransaction(transactionId + 1, existingReversedTransactionIds);
        }

        SavingsAccountTransaction transaction = null;
        if (savingsAccountTransaction.isDeposit()) {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                    existingTransactionIds, existingReversedTransactionIds, paymentDetail);
            transaction = account.deposit(transactionDTO);
        } else {
            final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                    existingTransactionIds, existingReversedTransactionIds, paymentDetail);
            transaction = account.withdraw(transactionDTO, true);
        }
        final Long newtransactionId = saveTransactionToGenerateTransactionId(transaction);

        if (account.isBeforeLastPostingPeriod(transactionDate)
                || account.isBeforeLastPostingPeriod(savingsAccountTransaction.transactionLocalDate())) {
            account.postInterest(mc, today, existingTransactionIds, existingReversedTransactionIds);
        } else {
            account.calculateInterestUsing(mc, today);
        }
        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.adjustTransactionAction);
        account.activateAccountBasedOnBalance();
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
        return new CommandProcessingResultBuilder() //
                .withEntityId(newtransactionId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)//
                .build();
    }

    /**
     *
     */
    private void throwValidationForActiveStatus(final String actionName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + actionName);
        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.active");
        throw new PlatformApiDataValidationException(dataValidationErrors);
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = savingsAccount.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency);

        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
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

    @Override
    public CommandProcessingResult close(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validateClosing(command);
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId);
        final boolean isLinkedWithAnyActiveLoan = this.accountAssociationsReadPlatformService.isLinkedWithAnyActiveLoan(savingsId);

        if (isLinkedWithAnyActiveLoan) {
            final String defaultUserMessage = "Closing savings account with id:" + savingsId
                    + " is not allowed, since it is linked with one of the active loans";
            throw new SavingsAccountClosingNotAllowedException("linked", defaultUserMessage, savingsId);
        }

        final Map<String, Object> changes = account.close(user, command, DateUtils.getLocalDateOfTenant());
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(account);
            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(account, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }

        }
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Override
    public SavingsAccountTransaction initiateSavingsTransfer(final Long accountId, final LocalDate transferDate) {
        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(accountId);

        final List<Long> existingTransactionIds = new ArrayList<Long>(savingsAccount.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(savingsAccount.findExistingReversedTransactionIds());

        final SavingsAccountTransaction newTransferTransaction = SavingsAccountTransaction.initiateTransfer(savingsAccount,
                savingsAccount.office(), transferDate);
        savingsAccount.getTransactions().add(newTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue());

        this.savingsAccountTransactionRepository.save(newTransferTransaction);
        this.savingAccountRepository.save(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return newTransferTransaction;
    }

    @Override
    public SavingsAccountTransaction withdrawSavingsTransfer(final Long accountId, final LocalDate transferDate) {
        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(accountId);

        final List<Long> existingTransactionIds = new ArrayList<Long>(savingsAccount.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(savingsAccount.findExistingReversedTransactionIds());

        final SavingsAccountTransaction withdrawtransferTransaction = SavingsAccountTransaction.withdrawTransfer(savingsAccount,
                savingsAccount.office(), transferDate);
        savingsAccount.getTransactions().add(withdrawtransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());

        this.savingsAccountTransactionRepository.save(withdrawtransferTransaction);
        this.savingAccountRepository.save(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return withdrawtransferTransaction;
    }

    @Override
    public void rejectSavingsTransfer(final Long accountId) {
        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(accountId);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue());
        this.savingAccountRepository.save(savingsAccount);
    }

    @Override
    public SavingsAccountTransaction acceptSavingsTransfer(final Long accountId, final LocalDate transferDate,
            final Office acceptedInOffice, final Staff fieldOfficer) {
        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(accountId);

        final List<Long> existingTransactionIds = new ArrayList<Long>(savingsAccount.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>(savingsAccount.findExistingReversedTransactionIds());

        final SavingsAccountTransaction acceptTransferTransaction = SavingsAccountTransaction.approveTransfer(savingsAccount,
                acceptedInOffice, transferDate);
        savingsAccount.getTransactions().add(acceptTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        if (fieldOfficer != null) {
            savingsAccount.update(fieldOfficer);
        }

        this.savingsAccountTransactionRepository.save(acceptTransferTransaction);
        this.savingAccountRepository.save(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        return acceptTransferTransaction;
    }

    @Transactional
    @Override
    public CommandProcessingResult addSavingsAccountCharge(final JsonCommand command) {

        this.context.authenticatedUser();
        final Long savingsAccountId = command.getSavingsId();
        this.savingsAccountChargeDataValidator.validateAdd(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId);
        checkClientOrGroupActive(savingsAccount);

        final DateTimeFormatter fmt = DateTimeFormat.forPattern("dd MM yyyy");

        final Long chargeDefinitionId = command.longValueOfParameterNamed(chargeIdParamName);
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewFromJson(savingsAccount, chargeDefinition, command);

        savingsAccount.addCharge(fmt, savingsAccountCharge, chargeDefinition);

        this.savingAccountRepository.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withLoanId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingsAccountCharge(final JsonCommand command) {
        this.context.authenticatedUser();

        this.savingsAccountChargeDataValidator.validateUpdate(command.json());
        final Long savingsAccountId = command.getSavingsId();
        // SavingsAccount Charge entity
        final Long savingsChargeId = command.entityId();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId);
        checkClientOrGroupActive(savingsAccount);

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository.findOneWithNotFoundDetection(savingsChargeId,
                savingsAccountId);

        final Map<String, Object> changes = savingsAccountCharge.update(command);
        this.savingsAccountChargeRepository.saveAndFlush(savingsAccountCharge);
        // TODO AA: revisit the code
        /*
         * // Charges may be edited only when the loan associated with them are
         * // yet to be approved (are in submitted and pending status) if
         * (!loan.status().isSubmittedAndPendingApproval()) { throw new
         * LoanChargeCannotBeUpdatedException(
         * LOAN_CHARGE_CANNOT_BE_UPDATED_REASON
         * .LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
         * loanCharge.getId()); }
         */

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsChargeId) //
                // .withOfficeId(savings) //
                // .withClientId(loan.getClientId()) //
                // .withGroupId(loan.getGroupId()) //
                // .withLoanId(savingsAccountId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command) {

        this.context.authenticatedUser();

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId);
        checkClientOrGroupActive(savingsAccount);

        savingsAccount.waiveCharge(savingsAccountChargeId, existingTransactionIds, existingReversedTransactionIds);

        this.savingAccountRepository.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            @SuppressWarnings("unused") final JsonCommand command) {
        this.context.authenticatedUser();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId);
        checkClientOrGroupActive(savingsAccount);
        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository.findOneWithNotFoundDetection(
                savingsAccountChargeId, savingsAccountId);

        // TODO AA: validate before deleting a charge

        savingsAccount.removeCharge(savingsAccountCharge);
        this.savingAccountRepository.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult payCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command) {
        this.context.authenticatedUser();

        final List<Long> existingTransactionIds = new ArrayList<Long>();
        final List<Long> existingReversedTransactionIds = new ArrayList<Long>();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId);
        checkClientOrGroupActive(savingsAccount);

        final BigDecimal chargeAmount = command.bigDecimalValueOfParameterNamed(amountParamName);
        savingsAccount.payCharge(savingsAccountChargeId, chargeAmount, existingTransactionIds, existingReversedTransactionIds);

        this.savingAccountRepository.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();

    }

}