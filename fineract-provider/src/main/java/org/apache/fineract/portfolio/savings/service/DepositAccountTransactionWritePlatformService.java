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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransaction;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operations shared by every deposit account type: withdrawals, interest calculation and posting, charges and deposits
 * from collection sheets.
 */
@RequiredArgsConstructor
public class DepositAccountTransactionWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountPostInterestService savingsAccountPostInterestService;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final SavingsAccountChargeDataValidator savingsAccountChargeDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final DepositAccountDomainService depositAccountDomainService;
    private final SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final HolidayRepositoryWrapper holidayRepository;
    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    private final DepositAccountWriteHelper depositAccountWriteHelper;

    @Transactional
    public CommandProcessingResult withdrawal(final Long savingsId, final JsonCommand command,
            final DepositAccountType depositAccountType) {

        boolean isRegularTransaction = true;

        this.depositAccountTransactionDataValidator.validate(command, depositAccountType);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final SavingsAccount account = this.depositAccountAssembler.assembleFromInUserScope(savingsId, depositAccountType);

        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final SavingsAccountTransaction withdrawal = this.depositAccountDomainService.handleWithdrawal(account, fmt, transactionDate,
                transactionAmount, paymentDetail, true, isRegularTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(withdrawal.getId()) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    public CommandProcessingResult calculateInterest(final Long savingsId, final DepositAccountType depositAccountType) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(savingsId, depositAccountType);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        final LocalDate today = DateUtils.getBusinessLocalDate();
        final boolean postReversals = false;
        final MathContext mc = new MathContext(15, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);

        this.savingAccountRepositoryWrapper.save(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    public CommandProcessingResult postInterest(final Long savingsId, final DepositAccountType depositAccountType) {

        final SavingsAccount account = this.depositAccountAssembler.assembleFromInUserScope(savingsId, depositAccountType);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);
        postInterest(account);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    private void postInterest(final SavingsAccount account) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        final LocalDate today = DateUtils.getBusinessLocalDate();
        final MathContext mc = new MathContext(10, MoneyHelper.getRoundingMode());
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final boolean postReversals = false;
        savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    @Transactional
    public CommandProcessingResult waiveCharge(final Long savingsAccountId, final Long savingsAccountChargeId,
            @SuppressWarnings("unused") final DepositAccountType depositAccountType) {
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        final SavingsAccountCharge savingsAccountCharge = this.savingsAccountChargeRepository
                .findOneWithNotFoundDetection(savingsAccountChargeId, savingsAccountId);

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.depositAccountAssembler.assignSavingAccountHelpers(account);

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        account.waiveCharge(savingsAccountChargeId, false);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(savingsAccountCharge.getDueDate(), false)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        } else {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceConstraints(SavingsApiConstants.waiveChargeTransactionAction, depositAccountOnHoldTransactions,
                false);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountChargeId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsAccountId) //
                .build();
    }

    public CommandProcessingResult payCharge(final Long savingsAccountId, final Long savingsAccountChargeId, final JsonCommand command,
            @SuppressWarnings("unused") final DepositAccountType depositAccountType) {

        this.context.authenticatedUser();

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

        this.payCharge(savingsAccountCharge, transactionDate, amountPaid, fmt);
        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsAccountCharge.getId()) //
                .withOfficeId(savingsAccountCharge.savingsAccount().officeId()) //
                .withClientId(savingsAccountCharge.savingsAccount().clientId()) //
                .withGroupId(savingsAccountCharge.savingsAccount().groupId()) //
                .withSavingsId(savingsAccountCharge.savingsAccount().getId()) //
                .build();

    }

    private void payCharge(final SavingsAccountCharge savingsAccountCharge, final LocalDate transactionDate, final BigDecimal amountPaid,
            final DateTimeFormatter formatter) {
        context.authenticatedUser();

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        // Get Savings account from savings charge
        final SavingsAccount account = savingsAccountCharge.savingsAccount();
        this.depositAccountAssembler.assignSavingAccountHelpers(account);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);
        account.payCharge(savingsAccountCharge, amountPaid, transactionDate, formatter, false, null);
        boolean isInterestTransfer = false;
        LocalDate postInterestOnDate = null;
        final MathContext mc = MathContext.DECIMAL64;
        final boolean postReversals = false;
        if (account.isBeforeLastPostingPeriod(transactionDate, false)) {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                    isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        } else {
            final LocalDate today = DateUtils.getBusinessLocalDate();
            account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postInterestOnDate, false, postReversals);
        }
        List<DepositAccountOnHoldTransaction> depositAccountOnHoldTransactions = null;
        if (account.getOnHoldFunds().compareTo(BigDecimal.ZERO) > 0) {
            depositAccountOnHoldTransactions = this.depositAccountOnHoldTransactionRepository
                    .findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(account);
        }

        account.validateAccountBalanceConstraints("." + SavingsAccountTransactionType.PAY_CHARGE.getCode(),
                depositAccountOnHoldTransactions, false);

        this.savingAccountRepositoryWrapper.saveAndFlush(account);

        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }

    public SavingsAccountTransaction mandatorySavingsAccountDeposit(final SavingsAccountTransactionDTO accountTransactionDTO) {
        boolean isRegularTransaction = false;
        final PaymentDetail paymentDetail = accountTransactionDTO.getPaymentDetail();
        if (paymentDetail != null && paymentDetail.getId() == null) {
            this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
        }
        if (accountTransactionDTO.getAccountType().equals(DepositAccountType.RECURRING_DEPOSIT.getValue())) {
            RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler
                    .assembleFromInUserScope(accountTransactionDTO.getSavingsAccountId(), DepositAccountType.RECURRING_DEPOSIT);
            return this.depositAccountDomainService.handleRDDeposit(account, accountTransactionDTO.getFormatter(),
                    accountTransactionDTO.getTransactionDate(), accountTransactionDTO.getTransactionAmount(), paymentDetail,
                    isRegularTransaction);
        }
        SavingsAccount account = null;
        if (accountTransactionDTO.getAccountType().equals(DepositAccountType.SAVINGS_DEPOSIT.getValue())) {
            account = this.depositAccountAssembler.assembleFromInUserScope(accountTransactionDTO.getSavingsAccountId(),
                    DepositAccountType.SAVINGS_DEPOSIT);
        } else {
            account = this.depositAccountAssembler.assembleFromInUserScope(accountTransactionDTO.getSavingsAccountId(),
                    DepositAccountType.CURRENT_DEPOSIT);
        }
        return this.depositAccountDomainService.handleSavingDeposit(account, accountTransactionDTO.getFormatter(),
                accountTransactionDTO.getTransactionDate(), accountTransactionDTO.getTransactionAmount(), paymentDetail,
                isRegularTransaction);

    }
}
