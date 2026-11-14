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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Closes fixed and recurring deposit accounts, on or before maturity, and applies maturity instructions.
 */
@Slf4j
@RequiredArgsConstructor
public class DepositAccountClosureWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    private final DepositAccountAssembler depositAccountAssembler;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final DepositAccountDomainService depositAccountDomainService;
    private final NoteRepository noteRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final DepositAccountWriteHelper depositAccountWriteHelper;

    public CommandProcessingResult closeFDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();
        final boolean isPreMatureClose = false;
        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.FIXED_DEPOSIT, isPreMatureClose);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleFDAccountClosure(account, paymentDetail, user, command, changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
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

    public CommandProcessingResult closeRDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.RECURRING_DEPOSIT, false);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleRDAccountClosure(account, paymentDetail, user, command, changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
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

    public CommandProcessingResult prematureCloseFDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.FIXED_DEPOSIT, true);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.FIXED_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);

        this.depositAccountDomainService.handleFDAccountPreMatureClosure(account, paymentDetail, user, command, changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
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

    public CommandProcessingResult prematureCloseRDAccount(final Long savingsId, final JsonCommand command) {
        final AppUser user = this.context.authenticatedUser();

        this.depositAccountTransactionDataValidator.validateClosing(command, DepositAccountType.RECURRING_DEPOSIT, true);

        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFromInUserScope(savingsId,
                DepositAccountType.RECURRING_DEPOSIT);
        this.depositAccountWriteHelper.checkClientOrGroupActive(account);
        if (account.maturityDate() == null) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME + DepositsApiConstants.preMatureCloseAction);
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("can.not.close.as.premature");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        this.depositAccountDomainService.handleRDAccountPreMatureClosure(account, paymentDetail, user, command, changes);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            changes.put("note", noteText);
            this.noteRepository.save(note);
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

    public void updateMaturityDetails(Long depositAccountId, DepositAccountType depositAccountType) {
        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();
        final boolean postReversals = false;
        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(depositAccountId, depositAccountType);
        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();
        this.depositAccountWriteHelper.updateExistingTransactionsDetails(account, existingTransactionIds, existingReversedTransactionIds);

        if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
            ((FixedDepositAccount) account).updateMaturityStatus(isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth);
            FixedDepositAccount fdAccount = ((FixedDepositAccount) account);
            // handle maturity instructions

            final boolean skipReinvestmentForArchivedProduct = fdAccount.isMatured() && fdAccount.isReinvestOnClosure()
                    && !this.depositAccountAssembler.isProductOpenForApplication(fdAccount.maturityDate(), fdAccount.savingsProduct());
            if (skipReinvestmentForArchivedProduct) {
                log.warn("Skipping reinvestment at maturity for FD account {}: product is archived", fdAccount.getId());
            }
            if (fdAccount.isMatured() && !skipReinvestmentForArchivedProduct
                    && (fdAccount.isReinvestOnClosure() || fdAccount.isTransferToSavingsOnClosure())) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                Map<String, Object> changes = new HashMap<>();
                final AppUser user = context.authenticatedUser();
                Long toSavingsId = fdAccount.getTransferToSavingsAccountId();
                this.depositAccountDomainService.handleFDAccountMaturityClosure(fdAccount, null, user, fmt, fdAccount.maturityDate(),
                        fdAccount.getOnAccountClosureId(), toSavingsId, "Apply maturity instructions", changes);

                if (changes.get("reinvestedDepositId") != null) {
                    Long reinvestedDepositId = (Long) changes.get("reinvestedDepositId");
                    final FixedDepositAccount reinvestAccount = (FixedDepositAccount) this.depositAccountAssembler
                            .assembleFrom(reinvestedDepositId, DepositAccountType.FIXED_DEPOSIT);
                    Money amountForDeposit = reinvestAccount.activateWithBalance();
                    Money activationChargeAmount = this.depositAccountWriteHelper.getActivationCharge(reinvestAccount);
                    if (activationChargeAmount.isGreaterThanZero()) {
                        this.depositAccountWriteHelper.payActivationCharge(reinvestAccount);
                        amountForDeposit = amountForDeposit.plus(activationChargeAmount);
                    }
                    this.depositAccountDomainService.handleFDDeposit(reinvestAccount, fmt, fdAccount.maturityDate(),
                            amountForDeposit.getAmount(), null);
                }
            }
        } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
            ((RecurringDepositAccount) account).updateMaturityStatus(isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth, postReversals);
        }
        this.savingAccountRepositoryWrapper.saveAndFlush(account);
        this.depositAccountWriteHelper.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds);
    }
}
