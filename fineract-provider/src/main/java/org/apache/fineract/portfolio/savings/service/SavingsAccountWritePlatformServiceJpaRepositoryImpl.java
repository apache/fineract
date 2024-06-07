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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.chargeIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lienAllowedParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionAmountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.transactionDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawBalanceParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsPostInterestBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.GroupSavingsIndividualMonitoring;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.PostInterestAsOnDateException;
import org.apache.fineract.portfolio.savings.exception.PostInterestAsOnDateException.PostInterestAsOnExceptionType;
import org.apache.fineract.portfolio.savings.exception.PostInterestClosingDateException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountClosingNotAllowedException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsOfficerAssignmentException;
import org.apache.fineract.portfolio.savings.exception.SavingsOfficerUnassignmentException;
import org.apache.fineract.portfolio.savings.exception.TransactionUpdateNotAllowedException;
import org.apache.fineract.portfolio.transfer.api.TransferApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class SavingsAccountWritePlatformServiceJpaRepositoryImpl implements SavingsAccountWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountDataValidator fromApiJsonDeserializer;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator;
    private final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final NoteRepository noteRepository;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final ChargeRepositoryWrapper chargeRepository;
    private final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final AppUserRepositoryWrapper appuserRepository;
    private final StandingInstructionRepository standingInstructionRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final GSIMRepositoy gsimRepository;
    private final SavingsAccountInterestPostingService savingsAccountInterestPostingService;
    private final ErrorHandler errorHandler;

    @Transactional
    @Override
    public CommandProcessingResult gsimActivate(final Long gsimId, final JsonCommand command) {

        Long parentSavingId = gsimId;
        GroupSavingsIndividualMonitoring parentSavings = gsimRepository.findById(parentSavingId).orElseThrow();
        List<SavingsAccount> childSavings = this.savingAccountRepositoryWrapper.findByGsimId(gsimId);

        CommandProcessingResult result = null;
        int count = 0;
        for (SavingsAccount account : childSavings) {
            result = activate(account.getId(), command);
            if (result != null) {
                count++;
                if (count == parentSavings.getChildAccountsCount()) {
                    parentSavings.setSavingsStatus(SavingsAccountStatusType.ACTIVE.getValue());
                    gsimRepository.save(parentSavings);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult activate(final Long savingsId, final JsonCommand command) {

        final AppUser user = this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validateActivation(command);

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);

        checkClientOrGroupActive(account);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Map<String, Object> changes = account.activate(user, command);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                StatusEnum.ACTIVATE.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(), account.productId());

        if (!changes.isEmpty()) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            processPostActiveActions(account, fmt, existingTransactionIds, existingReversedTransactionIds);
            this.savingAccountRepositoryWrapper.saveAndFlush(account);
        }

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);
        businessEventNotifierService.notifyPostBusinessEvent(new SavingsActivateBusinessEvent(account));

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
    public void processPostActiveActions(final SavingsAccount account, final DateTimeFormatter fmt, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds) {
        getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        Money amountForDeposit = account.activateWithBalance();
        boolean isRegularTransaction = false;
        if (amountForDeposit.isGreaterThanZero()) {
            boolean isAccountTransfer = false;
            this.savingsAccountDomainService.handleDeposit(account, fmt, account.getActivationDate(), amountForDeposit.getAmount(), null,
                    isAccountTransfer, isRegularTransaction, false);

            updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        }

        account.processAccountUponActivation(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsAccountTransactionType.PAY_CHARGE.name(),
                depositAccountOnHoldTransactions, false);
    }

    @Transactional
    @Override
    public CommandProcessingResult gsimDeposit(final Long gsimId, final JsonCommand command) {
        JsonArray savingsArray = command.arrayOfParameterNamed("savingsArray");

        CommandProcessingResult result = null;
        for (JsonElement element : savingsArray) {
            result = deposit(element.getAsJsonObject().get("childAccountId").getAsLong(),
                    JsonCommand.fromExistingCommand(command, element));
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult deposit(final Long savingsId, final JsonCommand command) {
        this.context.authenticatedUser();

        this.savingsAccountTransactionDataValidator.validate(command);
        boolean isGsim = false;

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);

        if (account.getGsim() != null) {
            isGsim = true;
            log.debug("is gsim");
        }
        checkClientOrGroupActive(account);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(transactionDate, account);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        boolean isAccountTransfer = false;
        boolean isRegularTransaction = true;
        final SavingsAccountTransaction deposit = this.savingsAccountDomainService.handleDeposit(account, fmt, transactionDate,
                transactionAmount, paymentDetail, isAccountTransfer, isRegularTransaction, backdatedTxnsAllowedTill);

        if (isGsim && (deposit.getId() != null)) {

            log.debug("Deposit account has been created: {} ", deposit);

            GroupSavingsIndividualMonitoring gsim = gsimRepository.findById(account.getGsim().getId()).orElseThrow();
            log.debug("parent deposit : {} ", gsim.getParentDeposit());
            log.debug("child account : {} ", savingsId);
            BigDecimal currentBalance = gsim.getParentDeposit();
            BigDecimal newBalance = currentBalance.add(transactionAmount);
            gsim.setParentDeposit(newBalance);
            gsimRepository.save(gsim);
            log.debug("balance after making deposit : {} ",
                    gsimRepository.findById(account.getGsim().getId()).orElseThrow().getParentDeposit());

        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingsTransactionNote(account, deposit, noteText);
            this.noteRepository.save(note);
        }

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
        this.savingsAccountTransactionRepository.saveAndFlush(transaction);
        return transaction.getId();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command) {

        this.savingsAccountTransactionDataValidator.validate(command);

        boolean isGsim = false;

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);

        if (account.getGsim() != null) {
            isGsim = true;
        }
        checkClientOrGroupActive(account);

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(transactionDate, account);

        final boolean isAccountTransfer = false;
        final boolean isRegularTransaction = true;
        final boolean isApplyWithdrawFee = true;
        final boolean isInterestTransfer = false;
        final boolean isWithdrawBalance = false;
        final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                isRegularTransaction, isApplyWithdrawFee, isInterestTransfer, isWithdrawBalance);
        final SavingsAccountTransaction withdrawal = this.savingsAccountDomainService.handleWithdrawal(account, fmt, transactionDate,
                transactionAmount, paymentDetail, transactionBooleanValues, backdatedTxnsAllowedTill);

        if (isGsim && (withdrawal.getId() != null)) {
            GroupSavingsIndividualMonitoring gsim = gsimRepository.findById(account.getGsim().getId()).orElseThrow();
            BigDecimal currentBalance = gsim.getParentDeposit().subtract(transactionAmount);
            gsim.setParentDeposit(currentBalance);
            gsimRepository.save(gsim);

        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingsTransactionNote(account, withdrawal, noteText);
            this.noteRepository.save(note);
        }

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
    public CommandProcessingResult applyAnnualFee(final Long savingsAccountChargeId, final Long accountId) {
        getAppUserIfPresent();

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, accountId);

        final LocalDate currentDate = DateUtils.getBusinessLocalDate();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MM yyyy").withZone(DateUtils.getDateTimeZoneOfTenant());

        while (DateUtils.isBefore(savingsAccountCharge.getDueDate(), currentDate)) {
            this.payCharge(savingsAccountCharge, savingsAccountCharge.getDueDate(), savingsAccountCharge.amount(), fmt, false);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult calculateInterest(final Long savingsId) {
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);
        checkClientOrGroupActive(account);

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        final LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);

        if (backdatedTxnsAllowedTill) {
            this.savingsAccountTransactionRepository.saveAll(account.getSavingsAccountTransactionsWithPivotConfig());
        }

        this.savingAccountRepositoryWrapper.save(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult postInterest(final JsonCommand command) {
        Long savingsId = command.getSavingsId();
        final boolean postInterestAs = command.booleanPrimitiveValueOfParameterNamed("isPostInterestAsOn");
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);
        checkClientOrGroupActive(account);

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(transactionDate, account);

        if (postInterestAs) {
            if (transactionDate == null) {
                throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.VALID_DATE);
            }
            if (DateUtils.isBefore(transactionDate, account.accountSubmittedOrActivationDate())) {
                throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.ACTIVATION_DATE);
            }

            List<SavingsAccountTransaction> savingTransactions = null;
            if (backdatedTxnsAllowedTill) {
                savingTransactions = account.getSavingsAccountTransactionsWithPivotConfig();
            } else {
                savingTransactions = account.getTransactions();
            }

            for (SavingsAccountTransaction savingTransaction : savingTransactions) {
                if (DateUtils.isBefore(transactionDate, savingTransaction.getDateOf())) {
                    throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.LAST_TRANSACTION_DATE);
                }
            }

            if (DateUtils.isDateInTheFuture(transactionDate)) {
                throw new PostInterestAsOnDateException(PostInterestAsOnExceptionType.FUTURE_DATE);
            }
        }
        postInterest(account, postInterestAs, transactionDate, backdatedTxnsAllowedTill);

        businessEventNotifierService.notifyPostBusinessEvent(new SavingsPostInterestBusinessEvent(account));
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
    public void postInterest(final SavingsAccount account, final boolean postInterestAs, final LocalDate transactionDate,
            final boolean backdatedTxnsAllowedTill) {
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        if (account.getNominalAnnualInterestRate().compareTo(BigDecimal.ZERO) > 0
                || (account.allowOverdraft() && account.getNominalAnnualInterestRateOverdraft().compareTo(BigDecimal.ZERO) > 0)) {
            final Set<Long> existingTransactionIds = new HashSet<>();
            final Set<Long> existingReversedTransactionIds = new HashSet<>();

            if (backdatedTxnsAllowedTill) {
                updateSavingsTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
            } else {
                updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
            }

            final LocalDate today = DateUtils.getBusinessLocalDate();
            final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
            boolean isInterestTransfer = false;
            LocalDate postInterestOnDate = null;
            if (postInterestAs) {
                postInterestOnDate = transactionDate;
            }
            boolean postReversals = false;
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, backdatedTxnsAllowedTill, postReversals);

            if (!backdatedTxnsAllowedTill) {
                List<SavingsAccountTransaction> transactions = account.getTransactions();
                for (SavingsAccountTransaction accountTransaction : transactions) {
                    if (accountTransaction.getId() == null) {
                        this.savingsAccountTransactionRepository.save(accountTransaction);
                    }
                }
            }

            if (backdatedTxnsAllowedTill) {
                this.savingsAccountTransactionRepository.saveAll(account.getSavingsAccountTransactionsWithPivotConfig());
            }

            this.savingAccountRepositoryWrapper.saveAndFlush(account);

            postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, backdatedTxnsAllowedTill);
        }
    }

    @Transactional
    @Override
    @Retry(name = "postInterest", fallbackMethod = "fallbackPostInterest")
    public SavingsAccountData postInterest(SavingsAccountData savingsAccountData, final boolean postInterestAs,
            final LocalDate transactionDate, final boolean backdatedTxnsAllowedTill) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        if (MathUtil.isGreaterThanZero(savingsAccountData.getNominalAnnualInterestRate()) || (savingsAccountData.isAllowOverdraft()
                && MathUtil.isGreaterThanZero(savingsAccountData.getNominalAnnualInterestRateOverdraft()))) {
            final Set<Long> existingTransactionIds = new HashSet<>();
            final Set<Long> existingReversedTransactionIds = new HashSet<>();
            updateExistingTransactionsDetails(savingsAccountData, existingTransactionIds, existingReversedTransactionIds);

            final LocalDate today = DateUtils.getBusinessLocalDate();
            final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
            boolean isInterestTransfer = false;
            LocalDate postInterestOnDate = null;
            if (postInterestAs) {
                postInterestOnDate = transactionDate;
            }

            savingsAccountData = this.savingsAccountInterestPostingService.postInterest(mc, today, isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                    savingsAccountData);

            if (!backdatedTxnsAllowedTill) {
                List<SavingsAccountTransactionData> transactions = savingsAccountData.getSavingsAccountTransactionData();
                for (SavingsAccountTransactionData accountTransaction : transactions) {
                    if (accountTransaction.getId() == null) {
                        savingsAccountData.setNewSavingsAccountTransactionData(accountTransaction);
                    }
                }
            }
            savingsAccountData.setExistingTransactionIds(existingTransactionIds);
            savingsAccountData.setExistingReversedTransactionIds(existingReversedTransactionIds);
        }
        return savingsAccountData;
    }

    @Override
    public CommandProcessingResult reverseTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification, final JsonCommand command) {

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();
        final boolean isBulk = command.booleanPrimitiveValueOfParameterNamed("isBulk");
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId);
        }

        if (!allowAccountTransferModification
                && this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transfer.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        if (!account.allowModify()) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed for this savings type", transactionId);
        }
        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        SavingsAccountTransaction reversal = null;
        Long reversalId = null;
        checkClientOrGroupActive(account);
        List<SavingsAccountTransaction> savingsAccountTransactions = null;
        if (isBulk) {
            String transactionRefNo = savingsAccountTransaction.getRefNo();
            savingsAccountTransactions = this.savingsAccountTransactionRepository.findByRefNo(transactionRefNo);
            reversal = this.savingsAccountDomainService.handleReversal(account, savingsAccountTransactions, backdatedTxnsAllowedTill);
        } else {
            reversal = this.savingsAccountDomainService.handleReversal(account, Collections.singletonList(savingsAccountTransaction),
                    backdatedTxnsAllowedTill);
        }
        reversalId = reversal.getId();
        return new CommandProcessingResultBuilder() //
                .withEntityId(reversalId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Override
    public CommandProcessingResult undoTransaction(final Long savingsId, final Long transactionId,
            final boolean allowAccountTransferModification) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId);
        }

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(savingsAccountTransaction.getTransactionDate(),
                account);

        if (!allowAccountTransferModification
                && this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transfer.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        if (!account.allowModify()) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed for this savings type", transactionId);
        }

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());

        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.undoTransactionAction);
        }
        account.undoTransaction(transactionId);

        // undoing transaction is withdrawal then undo withdrawal fee
        // transaction if any
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
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
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
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);
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
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final Long relaxingDaysConfigForPivotDate = this.configurationDomainService.retrieveRelaxingDaysConfigForPivotDate();
        final SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId, savingsId);
        if (savingsAccountTransaction == null) {
            throw new SavingsAccountTransactionNotFoundException(savingsId, transactionId);
        }

        if (!(savingsAccountTransaction.isDeposit() || savingsAccountTransaction.isWithdrawal())
                || savingsAccountTransaction.isReversed()) {
            throw new TransactionUpdateNotAllowedException(savingsId, transactionId);
        }

        if (this.accountTransfersReadPlatformService.isAccountTransfer(transactionId, PortfolioAccountType.SAVINGS)) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transfer.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed as it involves in account transfer",
                    transactionId);
        }

        this.savingsAccountTransactionDataValidator.validate(command);

        final LocalDate today = DateUtils.getBusinessLocalDate();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);

        if (account.isNotActive()) {
            throwValidationForActiveStatus(SavingsApiConstants.adjustTransactionAction);
        }
        if (!account.allowModify()) {
            throw new PlatformServiceUnavailableException("error.msg.saving.account.transaction.update.not.allowed",
                    "Savings account transaction:" + transactionId + " update not allowed for this savings type", transactionId);
        }
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(SavingsApiConstants.transactionDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(SavingsApiConstants.transactionAmountParamName);
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
        account.undoTransaction(transactionId);

        // for undo withdrawal fee
        final SavingsAccountTransaction nextSavingsAccountTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(transactionId + 1, savingsId);
        if (nextSavingsAccountTransaction != null && nextSavingsAccountTransaction.isWithdrawalFeeAndNotReversed()) {
            account.undoTransaction(transactionId + 1);
        }

        SavingsAccountTransaction transaction = null;
        boolean isInterestTransfer = false;
        Integer accountType = null;
        final SavingsAccountTransactionDTO transactionDTO = new SavingsAccountTransactionDTO(fmt, transactionDate, transactionAmount,
                paymentDetail, null, accountType);
        UUID refNo = UUID.randomUUID();
        if (savingsAccountTransaction.isDeposit()) {
            transaction = account.deposit(transactionDTO, false, relaxingDaysConfigForPivotDate, refNo.toString());
        } else {
            transaction = account.withdraw(transactionDTO, true, false, relaxingDaysConfigForPivotDate, refNo.toString());
        }
        final Long newtransactionId = saveTransactionToGenerateTransactionId(transaction);
        final LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, false)
                || account.isBeforeLastPostingPeriod(savingsAccountTransaction.getTransactionDate(), false)) {
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
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

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.adjustTransactionAction, depositAccountOnHoldTransactions,
                false);
        account.activateAccountBasedOnBalance();

        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);
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
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + actionName);
        baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("account.is.not.active");
        throw new PlatformApiDataValidationException(dataValidationErrors);
    }

    private void checkClientOrGroupActive(final SavingsAccount account) {
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

    @Override
    public CommandProcessingResult bulkGSIMClose(final Long gsimId, final JsonCommand command) {

        final Long parentSavingId = gsimId;
        GroupSavingsIndividualMonitoring parentSavings = gsimRepository.findById(parentSavingId).orElseThrow();
        List<SavingsAccount> childSavings = this.savingAccountRepositoryWrapper.findByGsimId(gsimId);

        CommandProcessingResult result = null;
        int count = 0;
        for (SavingsAccount account : childSavings) {
            result = close(account.getId(), command);

            if (result != null) {
                count++;
                if (count == parentSavings.getChildAccountsCount()) {
                    parentSavings.setSavingsStatus(SavingsAccountStatusType.CLOSED.getValue());
                    gsimRepository.save(parentSavings);
                }
            }
        }
        return result;
    }

    @Override
    public CommandProcessingResult close(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        this.savingsAccountTransactionDataValidator.validateClosing(command, account);

        final boolean isLinkedWithAnyActiveLoan = this.accountAssociationsReadPlatformService.isLinkedWithAnyActiveAccount(savingsId);

        if (isLinkedWithAnyActiveLoan) {
            final String defaultUserMessage = "Closing savings account with id:" + savingsId
                    + " is not allowed, since it is linked with one of the active accounts";
            throw new SavingsAccountClosingNotAllowedException("linked", defaultUserMessage, savingsId);
        }

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                StatusEnum.CLOSE.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(), account.productId());

        final boolean isWithdrawBalance = command.booleanPrimitiveValueOfParameterNamed(withdrawBalanceParamName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        final boolean isPostInterest = command.booleanPrimitiveValueOfParameterNamed(SavingsApiConstants.postInterestValidationOnClosure);
        // postInterest(account,closedDate,flag);
        if (isPostInterest) {
            boolean postInterestOnClosingDate = false;
            List<SavingsAccountTransaction> savingTransactions = account.getTransactions();
            for (SavingsAccountTransaction savingTransaction : savingTransactions) {
                if (savingTransaction.isInterestPosting() && savingTransaction.isNotReversed()
                        && DateUtils.isEqual(closedDate, savingTransaction.getTransactionDate())) {
                    postInterestOnClosingDate = true;
                    break;
                }
            }
            if (!postInterestOnClosingDate) {
                throw new PostInterestClosingDateException();
            }
        }

        final Map<String, Object> changes = new LinkedHashMap<>();

        if (isWithdrawBalance && account.getSummary().getAccountBalance(account.getCurrency()).isGreaterThanZero()) {

            final BigDecimal transactionAmount = account.getSummary().getAccountBalance();

            final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

            final boolean isAccountTransfer = false;
            final boolean isRegularTransaction = true;
            final boolean isApplyWithdrawFee = false;
            final boolean isInterestTransfer = false;
            final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(isAccountTransfer,
                    isRegularTransaction, isApplyWithdrawFee, isInterestTransfer, isWithdrawBalance);

            this.savingsAccountDomainService.handleWithdrawal(account, fmt, closedDate, transactionAmount, paymentDetail,
                    transactionBooleanValues, false);

        }

        final Map<String, Object> accountChanges = account.close(user, command);
        changes.putAll(accountChanges);
        if (!changes.isEmpty()) {
            this.savingAccountRepositoryWrapper.save(account);
            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(account, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }

        }

        businessEventNotifierService.notifyPostBusinessEvent(new SavingsCloseBusinessEvent(account));
        // disable all standing orders linked to the savings account
        disableStandingInstructionsLinkedToClosedSavings(account);
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
    public SavingsAccountTransaction initiateSavingsTransfer(final SavingsAccount savingsAccount, final LocalDate transferDate) {
        getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();

        validateTransactionsForTransfer(savingsAccount, transferDate);

        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        this.savingAccountAssembler.setHelpers(savingsAccount);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction newTransferTransaction = SavingsAccountTransaction.initiateTransfer(savingsAccount,
                savingsAccount.office(), transferDate);
        savingsAccount.addTransaction(newTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_IN_PROGRESS.getValue());
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(newTransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds, false);

        return newTransferTransaction;
    }

    @Override
    public SavingsAccountTransaction withdrawSavingsTransfer(final SavingsAccount savingsAccount, final LocalDate transferDate) {
        getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        this.savingAccountAssembler.setHelpers(savingsAccount);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction withdrawtransferTransaction = SavingsAccountTransaction.withdrawTransfer(savingsAccount,
                savingsAccount.office(), transferDate);
        savingsAccount.addTransaction(withdrawtransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        final MathContext mc = MathContext.DECIMAL64;
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(withdrawtransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds, false);

        return withdrawtransferTransaction;
    }

    @Override
    public void rejectSavingsTransfer(final SavingsAccount savingsAccount) {
        this.savingAccountAssembler.setHelpers(savingsAccount);
        savingsAccount.setStatus(SavingsAccountStatusType.TRANSFER_ON_HOLD.getValue());
        this.savingAccountRepositoryWrapper.save(savingsAccount);
    }

    @Override
    public SavingsAccountTransaction acceptSavingsTransfer(final SavingsAccount savingsAccount, final LocalDate transferDate,
            final Office acceptedInOffice, final Staff fieldOfficer) {
        getAppUserIfPresent();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        this.savingAccountAssembler.setHelpers(savingsAccount);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(savingsAccount, existingTransactionIds, existingReversedTransactionIds);

        final SavingsAccountTransaction acceptTransferTransaction = SavingsAccountTransaction.approveTransfer(savingsAccount,
                acceptedInOffice, transferDate);
        savingsAccount.addTransaction(acceptTransferTransaction);
        savingsAccount.setStatus(SavingsAccountStatusType.ACTIVE.getValue());
        if (fieldOfficer != null) {
            savingsAccount.reassignSavingsOfficer(fieldOfficer, transferDate);
        }
        boolean isInterestTransfer = false;
        final MathContext mc = MathContext.DECIMAL64;
        LocalDate postInterestOnDate = null;
        boolean postReversals = false;
        savingsAccount.calculateInterestUsing(mc, transferDate, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingsAccountTransactionRepository.save(acceptTransferTransaction);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        postJournalEntries(savingsAccount, existingTransactionIds, existingReversedTransactionIds, false);

        return acceptTransferTransaction;
    }

    @Transactional
    @Override
    public CommandProcessingResult addSavingsAccountCharge(final JsonCommand command) {
        this.context.authenticatedUser();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        final Long savingsAccountId = command.getSavingsId();
        this.savingsAccountChargeDataValidator.validateAdd(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId, false);
        checkClientOrGroupActive(savingsAccount);

        final Locale locale = command.extractLocale();
        final String format = command.dateFormat();
        final DateTimeFormatter fmt = StringUtils.isNotBlank(format) ? DateTimeFormatter.ofPattern(format).withLocale(locale)
                : DateTimeFormatter.ofPattern("dd MM yyyy");

        final Long chargeDefinitionId = command.longValueOfParameterNamed(chargeIdParamName);
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        Integer chargeTimeType = chargeDefinition.getChargeTimeType();
        LocalDate dueAsOfDateParam = command.localDateValueOfParameterNamed(dueAsOfDateParamName);
        if ((chargeTimeType.equals(ChargeTimeType.WITHDRAWAL_FEE.getValue())
                || chargeTimeType.equals(ChargeTimeType.SAVINGS_NOACTIVITY_FEE.getValue())) && dueAsOfDateParam != null) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(dueAsOfDateParam.format(fmt))
                    .failWithCodeNoParameterAddedToErrorCode(
                            "charge.due.date.is.invalid.for." + ChargeTimeType.fromInt(chargeTimeType).getCode());
        }
        final SavingsAccountCharge savingsAccountCharge = SavingsAccountCharge.createNewFromJson(savingsAccount, chargeDefinition, command);

        if (chargeDefinition.isEnableFreeWithdrawal()) {
            savingsAccountCharge.setFreeWithdrawalCount(0);
        }

        if (savingsAccountCharge.getDueDate() != null) {
            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                    && this.holidayRepository.isHoliday(savingsAccount.officeId(), savingsAccountCharge.getDueDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.on.holiday");
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(savingsAccountCharge.getDueDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.a.nonworking.day");
            }
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        savingsAccount.addCharge(fmt, savingsAccountCharge, chargeDefinition);
        this.savingsAccountChargeRepository.save(savingsAccountCharge);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingsAccountCharge(final JsonCommand command) {

        this.context.authenticatedUser();
        this.savingsAccountChargeDataValidator.validateUpdate(command.json());
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        final Long savingsAccountId = command.getSavingsId();
        // SavingsAccount Charge entity
        final Long savingsChargeId = command.entityId();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId, false);
        checkClientOrGroupActive(savingsAccount);

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository.findOneWithNotFoundDetection(savingsChargeId,
                savingsAccountId);

        final Map<String, Object> changes = savingsAccountCharge.update(command);

        if (savingsAccountCharge.getDueDate() != null) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

            // transaction date should not be on a holiday or non working day
            if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                    && this.holidayRepository.isHoliday(savingsAccount.officeId(), savingsAccountCharge.getDueDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.on.holiday");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }

            if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                    && !this.workingDaysRepository.isWorkingDay(savingsAccountCharge.getDueDate())) {
                baseDataValidator.reset().parameter(dueAsOfDateParamName).value(savingsAccountCharge.getDueDate().format(fmt))
                        .failWithCodeNoParameterAddedToErrorCode("charge.due.date.is.a.nonworking.day");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }
        }

        this.savingsAccountChargeRepository.saveAndFlush(savingsAccountCharge);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();

        this.savingAccountAssembler.loadTransactionsToSavingsAccount(account, backdatedTxnsAllowedTill);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        if (backdatedTxnsAllowedTill) {
            updateSavingsTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        } else {
            updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        }

        account.waiveCharge(savingsAccountChargeId, backdatedTxnsAllowedTill);

        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(savingsAccountCharge.getDueDate(), backdatedTxnsAllowedTill)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        } else {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        }

        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative(SavingsApiConstants.waiveChargeTransactionAction,
                depositAccountOnHoldTransactions, backdatedTxnsAllowedTill);

        if (backdatedTxnsAllowedTill) {
            this.savingsAccountTransactionRepository.saveAll(account.getSavingsAccountTransactionsWithPivotConfig());
        }

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, backdatedTxnsAllowedTill);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            @SuppressWarnings("unused") final JsonCommand command) {
        this.context.authenticatedUser();

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsAccountId, false);
        checkClientOrGroupActive(savingsAccount);
        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        savingsAccount.removeCharge(savingsAccountCharge);
        this.savingAccountRepositoryWrapper.saveAndFlush(savingsAccount);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    @Override
    public CommandProcessingResult payCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command) {
        context.authenticatedUser();

        this.savingsAccountChargeDataValidator.validatePayCharge(command.json());
        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final BigDecimal amountPaid = command.bigDecimalValueOfParameterNamed(amountParamName);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(dueAsOfDateParamName);

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_RESOURCE_NAME);

        // transaction date should not be on a holiday or non working day
        if (!this.configurationDomainService.allowTransactionsOnHolidayEnabled()
                && this.holidayRepository.isHoliday(savingsAccountCharge.savingsAccount().officeId(), transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(fmt))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.allowed.transaction.date.is.on.holiday");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        if (!this.configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()
                && !this.workingDaysRepository.isWorkingDay(transactionDate)) {
            baseDataValidator.reset().parameter(dueAsOfDateParamName).value(transactionDate.format(fmt))
                    .failWithCodeNoParameterAddedToErrorCode("transaction.not.allowed.transaction.date.is.a.nonworking.day");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final boolean backdatedTxnsAllowedTill = false;

        SavingsAccountTransaction chargeTransaction = this.payCharge(savingsAccountCharge, transactionDate, amountPaid, fmt,
                backdatedTxnsAllowedTill);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingsTransactionNote(savingsAccountCharge.savingsAccount(), chargeTransaction, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .build();

    }

    @Transactional
    @Override
    public void applyChargeDue(final Long savingsAccountChargeId, final Long accountId) {
        // always use current date as transaction date for batch job
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, accountId);

        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MM yyyy").withZone(DateUtils.getDateTimeZoneOfTenant());

        while (savingsAccountCharge.isNotFullyPaid() && DateUtils.isBefore(savingsAccountCharge.getDueDate(), transactionDate)) {
            payCharge(savingsAccountCharge, transactionDate, savingsAccountCharge.amoutOutstanding(), fmt, false);
        }
    }

    @SuppressWarnings("unused")
    public SavingsAccountData fallbackPostInterest(SavingsAccountData savingsAccountData, boolean postInterestAs, LocalDate transactionDate,
            boolean backdatedTxnsAllowedTill, Throwable t) {
        // NOTE: allow caller to catch the exceptions
        // NOTE: wrap throwable only if really necessary
        throw errorHandler.getMappable(t, null, null, "savings.postinterest");
    }

    @Transactional
    private SavingsAccountTransaction payCharge(final SavingsAccountCharge savingsAccountCharge, final LocalDate transactionDate,
            final BigDecimal amountPaid, final DateTimeFormatter formatter, final boolean backdatedTxnsAllowedTill) {
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.savingAccountAssembler.assignSavingAccountHelpers(account);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        Pageable sortedByDateAndIdDesc = PageRequest.of(0, 1, Sort.by("dateOf", "id").descending());

        List<SavingsAccountTransaction> savingsAccountTransaction = this.savingsAccountTransactionRepository
                .findBySavingsAccountIdAndLessThanDateOfAndReversedIsFalse(account.getId(), transactionDate, sortedByDateAndIdDesc);

        account.validateAccountBalanceDoesNotViolateOverdraft(savingsAccountTransaction, amountPaid);

        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        SavingsAccountTransaction chargeTransaction = account.payCharge(savingsAccountCharge, amountPaid, transactionDate, formatter,
                backdatedTxnsAllowedTill, null);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, backdatedTxnsAllowedTill)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.postInterest(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth,
                    postInterestOnDate, isInterestTransfer, postReversals);
        } else {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;

        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceDoesNotBecomeNegative("." + SavingsAccountTransactionType.PAY_CHARGE.getCode(),
                depositAccountOnHoldTransactions, backdatedTxnsAllowedTill);

        saveTransactionToGenerateTransactionId(chargeTransaction);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, backdatedTxnsAllowedTill);

        return chargeTransaction;
    }

    private void updateExistingTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findExistingTransactionIds());
        existingReversedTransactionIds.addAll(account.findExistingReversedTransactionIds());
    }

    private void updateExistingTransactionsDetails(SavingsAccountData account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findCurrentTransactionIdsWithPivotDateConfig());
        existingReversedTransactionIds.addAll(account.findCurrentReversedTransactionIdsWithPivotDateConfig());
    }

    private void updateSavingsTransactionsDetails(SavingsAccount account, Set<Long> existingTransactionIds,
            Set<Long> existingReversedTransactionIds) {
        existingTransactionIds.addAll(account.findCurrentTransactionIdsWithPivotDateConfig());
        existingReversedTransactionIds.addAll(account.findCurrentReversedTransactionIdsWithPivotDateConfig());
    }

    private void postJournalEntries(final SavingsAccount savingsAccount, final Set<Long> existingTransactionIds,
            final Set<Long> existingReversedTransactionIds, final boolean backdatedTxnsAllowedTill) {

        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = savingsAccount.deriveAccountingBridgeData(savingsAccount.getCurrency().getCode(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer, backdatedTxnsAllowedTill);
        this.journalEntryWritePlatformService.createJournalEntriesForSavings(accountingBridgeData);
    }

    @Override
    public CommandProcessingResult inactivateCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {

        this.context.authenticatedUser();

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.savingAccountAssembler.assignSavingAccountHelpers(account);

        final LocalDate inactivationOnDate = DateUtils.getBusinessLocalDate();

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        // Only recurring fees are allowed to inactivate
        if (!savingsAccountCharge.isRecurringFee()) {
            baseDataValidator.reset().parameter(null).value(savingsAccountCharge.getId())
                    .failWithCodeNoParameterAddedToErrorCode("charge.inactivation.allowed.only.for.recurring.charges");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

        } else {
            final LocalDate nextDueDate = savingsAccountCharge.getNextDueDateFrom(inactivationOnDate);

            if (savingsAccountCharge.isChargeIsDue(nextDueDate)) {
                baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("inactivation.of.charge.not.allowed.when.charge.is.due");
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            } else if (savingsAccountCharge.isChargeIsOverPaid(nextDueDate)) {

                final List<SavingsAccountTransaction> chargePayments = new ArrayList<>();
                SavingsAccountCharge updatedCharge = savingsAccountCharge;
                do {
                    chargePayments.clear();
                    for (SavingsAccountTransaction transaction : account.getTransactions()) {
                        if (transaction.isPayCharge() && transaction.isNotReversed()
                                && transaction.isPaymentForCurrentCharge(savingsAccountCharge)) {
                            chargePayments.add(transaction);
                        }
                    }
                    // Reverse the excess payments of charge transactions
                    SavingsAccountTransaction lastChargePayment = getLastChargePayment(chargePayments);
                    this.undoTransaction(savingsAccountCharge.savingsAccount().getId(), lastChargePayment.getId(), false);
                    updatedCharge = account.getUpdatedChargeDetails(savingsAccountCharge);
                } while (updatedCharge.isChargeIsOverPaid(nextDueDate));
            }
            account.inactivateCharge(savingsAccountCharge, inactivationOnDate);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .build();
    }

    private SavingsAccountTransaction getLastChargePayment(final List<SavingsAccountTransaction> chargePayments) {
        if (!CollectionUtils.isEmpty(chargePayments)) {
            return chargePayments.get(chargePayments.size() - 1);
        }
        return null;
    }

    @Transactional
    @Override
    public CommandProcessingResult assignFieldOfficer(Long savingsAccountId, JsonCommand command) {
        this.context.authenticatedUser();
        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        Staff fromSavingsOfficer = null;
        Staff toSavingsOfficer = null;
        this.fromApiJsonDeserializer.validateForAssignSavingsOfficer(command.json());

        final SavingsAccount savingsForUpdate = this.savingAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
        final Long fromSavingsOfficerId = command.longValueOfParameterNamed("fromSavingsOfficerId");
        final Long toSavingsOfficerId = command.longValueOfParameterNamed("toSavingsOfficerId");
        final LocalDate dateOfSavingsOfficerAssignment = command.localDateValueOfParameterNamed("assignmentDate");

        if (fromSavingsOfficerId != null) {
            fromSavingsOfficer = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(fromSavingsOfficerId,
                    savingsForUpdate.office().getHierarchy());
        }
        if (toSavingsOfficerId != null) {
            toSavingsOfficer = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(toSavingsOfficerId,
                    savingsForUpdate.office().getHierarchy());
            actualChanges.put("toSavingsOfficerId", toSavingsOfficer.getId());
        }
        if (!savingsForUpdate.hasSavingsOfficer(fromSavingsOfficer)) {
            throw new SavingsOfficerAssignmentException(savingsAccountId, fromSavingsOfficerId);
        }

        savingsForUpdate.reassignSavingsOfficer(toSavingsOfficer, dateOfSavingsOfficerAssignment);

        this.savingAccountRepositoryWrapper.saveAndFlush(savingsForUpdate);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(savingsForUpdate.officeId()) //
                .withEntityId(savingsForUpdate.getId()) //
                .withSavingsId(savingsAccountId) //
                .with(actualChanges) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult unassignFieldOfficer(Long savingsAccountId, JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);
        this.fromApiJsonDeserializer.validateForUnAssignSavingsOfficer(command.json());

        final SavingsAccount savingsForUpdate = this.savingAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
        if (savingsForUpdate.getSavingsOfficer() == null) {
            throw new SavingsOfficerUnassignmentException(savingsAccountId);
        }

        final LocalDate dateOfSavingsOfficerUnassigned = command.localDateValueOfParameterNamed("unassignedDate");

        savingsForUpdate.removeSavingsOfficer(dateOfSavingsOfficerUnassigned);

        this.savingAccountRepositoryWrapper.saveAndFlush(savingsForUpdate);

        actualChanges.put("toSavingsOfficerId", null);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(savingsForUpdate.officeId()) //
                .withEntityId(savingsForUpdate.getId()) //
                .withSavingsId(savingsAccountId) //
                .with(actualChanges) //
                .build();
    }

    @Override
    public CommandProcessingResult modifyWithHoldTax(Long savingsAccountId, JsonCommand command) {
        final Map<String, Object> actualChanges = new HashMap<>(1);
        final SavingsAccount savingsForUpdate = this.savingAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsAccountId);
        if (command.isChangeInBooleanParameterNamed(withHoldTaxParamName, savingsForUpdate.withHoldTax())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(withHoldTaxParamName);
            actualChanges.put(withHoldTaxParamName, newValue);
            savingsForUpdate.setWithHoldTax(newValue);
            if (savingsForUpdate.getTaxGroup() == null) {
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("account");
                baseDataValidator.reset().parameter(withHoldTaxParamName).failWithCode("not.supported");
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsAccountId) //
                .withSavingsId(savingsAccountId) //
                .with(actualChanges) //
                .build();
    }

    @Override
    public void setSubStatusInactive(Long savingsId) {
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.setSubStatusInactive(false);
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);
    }

    @Override
    public void setSubStatusDormant(Long savingsId) {
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        account.setSubStatusDormant();
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
    }

    @Override
    public void escheat(Long savingsId) {
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.escheat(appuserRepository.fetchSystemUser());
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    /**
     * Disable all standing instructions linked to the savings account if the status is "closed"
     *
     * @param savingsAccount
     *            -- the savings account object
     *
     **/
    @Transactional
    private void disableStandingInstructionsLinkedToClosedSavings(final SavingsAccount savingsAccount) {
        if (savingsAccount != null && savingsAccount.isClosed()) {
            final Integer standingInstructionStatus = StandingInstructionStatus.ACTIVE.getValue();
            final Collection<AccountTransferStandingInstruction> accountTransferStandingInstructions = this.standingInstructionRepository
                    .findBySavingsAccountAndStatus(savingsAccount, standingInstructionStatus);

            if (!accountTransferStandingInstructions.isEmpty()) {
                for (AccountTransferStandingInstruction accountTransferStandingInstruction : accountTransferStandingInstructions) {
                    accountTransferStandingInstruction.updateStatus(StandingInstructionStatus.DISABLED.getValue());
                    this.standingInstructionRepository.save(accountTransferStandingInstruction);
                }
            }
        }
    }

    @Override
    public CommandProcessingResult blockAccount(final Long savingsId, final JsonCommand command) {

        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        final Map<String, Object> changes = account.block();

        final String reasonForBlock = command.stringValueOfParameterNamed(SavingsApiConstants.reasonForBlockParamName);
        validateReasonForHold(reasonForBlock);
        account.updateReason(reasonForBlock);

        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    @Override
    public CommandProcessingResult unblockAccount(final Long savingsId) {
        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        final Map<String, Object> changes = account.unblock();

        account.updateReason(null);

        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult holdAmount(final Long savingsId, final JsonCommand command) {

        final AppUser submittedBy = this.context.authenticatedUser();
        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transactionDateParamName);
        final boolean lienAllowed = command.booleanPrimitiveValueOfParameterNamed(lienAllowedParamName);

        checkClientOrGroupActive(account);

        final BigDecimal amount = command.bigDecimalValueOfParameterNamed(transactionAmountParamName);

        Money runningBalance = Money.of(account.getCurrency(), account.getAccountBalance());
        if (account.getSavingsHoldAmount() != null) {
            runningBalance = runningBalance.minus(account.getSavingsHoldAmount()).minus(amount);
        } else {
            runningBalance = runningBalance.minus(amount);
        }

        this.savingsAccountTransactionDataValidator.validateHoldAndAssembleForm(command.json(), account, submittedBy,
                backdatedTxnsAllowedTill);
        SavingsAccountTransaction transaction = this.savingsAccountDomainService.handleHold(account, amount, transactionDate, lienAllowed);
        account.holdAmount(amount);
        transaction.setRunningBalance(runningBalance);

        final String reasonForBlock = command.stringValueOfParameterNamed(SavingsApiConstants.reasonForBlockParamName);
        transaction.updateReason(reasonForBlock);

        account.getAccountBalance();
        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(transaction.getTransactionDate(), account);

        this.savingsAccountTransactionRepository.saveAndFlush(transaction);

        if (backdatedTxnsAllowedTill) {
            // Check again whether transactions are modified
            this.savingsAccountTransactionRepository.saveAll(account.getSavingsAccountTransactionsWithPivotConfig());
        }

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        return new CommandProcessingResultBuilder().withEntityId(transaction.getId()).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult releaseAmount(final Long savingsId, final Long savingsTransactionId) {
        context.authenticatedUser();
        SavingsAccountTransaction holdTransaction = this.savingsAccountTransactionRepository
                .findOneByIdAndSavingsAccountId(savingsTransactionId, savingsId);

        holdTransaction.updateReason(null);

        final SavingsAccountTransaction transaction = this.savingsAccountTransactionDataValidator
                .validateReleaseAmountAndAssembleForm(holdTransaction);

        final boolean backdatedTxnsAllowedTill = this.savingAccountAssembler.getPivotConfigStatus();
        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, backdatedTxnsAllowedTill);
        checkClientOrGroupActive(account);

        Money runningBalance = Money.of(account.getCurrency(), account.getAccountBalance());

        Money savingsOnHold = Money.of(account.getCurrency(), account.getSavingsHoldAmount());

        runningBalance = runningBalance.minus(savingsOnHold);

        runningBalance = runningBalance.plus(transaction.getAmount());
        transaction.setRunningBalance(runningBalance);

        this.savingsAccountTransactionDataValidator.validateTransactionWithPivotDate(transaction.getTransactionDate(), account);
        account.releaseOnHoldAmount(transaction.getAmount());

        this.savingsAccountTransactionRepository.saveAndFlush(transaction);
        holdTransaction.updateReleaseId(transaction.getId());

        if (backdatedTxnsAllowedTill) {
            this.savingsAccountTransactionRepository.saveAll(account.getSavingsAccountTransactionsWithPivotConfig());
        } else {
            account.addTransaction(transaction);
        }

        this.savingAccountRepositoryWrapper.save(account);

        return new CommandProcessingResultBuilder().withEntityId(transaction.getId()).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(account.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult blockCredits(final Long savingsId, final JsonCommand command) {
        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        final String reasonForBlock = command.stringValueOfParameterNamed(SavingsApiConstants.reasonForBlockParamName);
        validateReasonForHold(reasonForBlock);
        account.updateReason(reasonForBlock);

        final Map<String, Object> changes = account.blockCredits(account.getSubStatus());
        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult unblockCredits(final Long savingsId) {
        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);
        account.updateReason(null);
        final Map<String, Object> changes = account.unblockCredits();
        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult blockDebits(final Long savingsId, final JsonCommand command) {
        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        final String reasonForBlock = command.stringValueOfParameterNamed(SavingsApiConstants.reasonForBlockParamName);
        validateReasonForHold(reasonForBlock);
        account.updateReason(reasonForBlock);

        final Map<String, Object> changes = account.blockDebits(account.getSubStatus());
        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult unblockDebits(final Long savingsId) {
        this.context.authenticatedUser();

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        account.updateReason(null);

        final Map<String, Object> changes = account.unblockDebits();
        if (!changes.isEmpty()) {

            this.savingAccountRepositoryWrapper.save(account);
        }
        return new CommandProcessingResultBuilder().withEntityId(savingsId).withOfficeId(account.officeId())
                .withClientId(account.clientId()).withGroupId(account.groupId()).withSavingsId(savingsId).with(changes).build();
    }

    private void validateTransactionsForTransfer(final SavingsAccount savingsAccount, final LocalDate transferDate) {
        for (SavingsAccountTransaction transaction : savingsAccount.getTransactions()) {
            if ((DateUtils.isEqual(transferDate, transaction.getTransactionDate())
                    && DateUtils.isEqual(transferDate, transaction.getSubmittedOnDate()))
                    || DateUtils.isBefore(transferDate, transaction.getTransactionDate())) {
                throw new GeneralPlatformDomainRuleException(TransferApiConstants.transferClientSavingsException,
                        TransferApiConstants.transferClientSavingsException, transaction.getCreatedDateTime(), transferDate);
            }
        }
    }

    private void validateReasonForHold(String reasonForBlock) {
        if (StringUtils.isBlank(reasonForBlock)) {
            throw new PlatformDataIntegrityException("Reason For Block is Mandatory", "error.msg.reason.for.block.mandatory");
        }
    }
}
