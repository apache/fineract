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
package org.apache.fineract.portfolio.loanaccount.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApplyOverdueChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeUndoBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeAdjustmentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeAdjustmentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeRefundBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOverdueInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.exception.InstallmentNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeAdjustmentException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeRefundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanproduct.data.LoanOverdueDTO;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanChargeWritePlatformServiceImpl implements LoanChargeWritePlatformService {

    private static final String AMOUNT = "amount";
    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanChargeRepository loanChargeRepository;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanUtilService loanUtilService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanLifecycleStateMachine defaultLoanLifecycleStateMachine;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;

    private static boolean isPartOfThisInstallment(LoanCharge loanCharge, LoanRepaymentScheduleInstallment e) {
        return e.getFromDate().isBefore(loanCharge.getDueDate()) && !loanCharge.getDueDate().isAfter(e.getDueDate());
    }

    @Transactional
    @Override
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {

        this.loanChargeApiJsonValidator.validateAddLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        List<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        if (loan.isDisbursed() && chargeDefinition.isDisbursementCharge()) {
            // validates whether any pending disbursements are available to
            // apply this charge
            validateAddingNewChargeAllowed(loanDisburseDetails);
        }
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        boolean isAppliedOnBackDate = false;
        LoanCharge loanCharge = null;
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        if (chargeDefinition.isPercentageOfDisbursementAmount()) {
            LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;
            for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
                if (disbursementDetail.actualDisbursementDate() == null) {
                    loanCharge = LoanCharge.createNewWithoutLoan(chargeDefinition, disbursementDetail.principal(), null, null, null,
                            disbursementDetail.expectedDisbursementDateAsLocalDate(), null, null);
                    loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                    loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                    businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
                    validateAddLoanCharge(loan, chargeDefinition, loanCharge);
                    addCharge(loan, chargeDefinition, loanCharge);
                    isAppliedOnBackDate = true;
                    if (recalculateFrom.isAfter(disbursementDetail.expectedDisbursementDateAsLocalDate())) {
                        recalculateFrom = disbursementDetail.expectedDisbursementDateAsLocalDate();
                    }
                }
            }
            if (loanCharge == null) {
                final String errorMessage = "Charge with identifier " + chargeDefinition.getId()
                        + " cannot be applied: No valid loan disbursement available";
                throw new ChargeCannotBeAppliedToException("loan", errorMessage, chargeDefinition.getId());
            }
            loan.addTrancheLoanCharge(chargeDefinition);
        } else {
            loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));

            validateAddLoanCharge(loan, chargeDefinition, loanCharge);
            isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
            if (loanCharge.getDueLocalDate() == null || recalculateFrom.isAfter(loanCharge.getDueLocalDate())) {
                isAppliedOnBackDate = true;
                recalculateFrom = loanCharge.getDueLocalDate();
            }
        }

        boolean reprocessRequired = true;
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            if (isAppliedOnBackDate && loan.isFeeCompoundingEnabledForInterestRecalculation()) {

                runScheduleRecalculation(loan, recalculateFrom);
                reprocessRequired = false;
            }
            this.loanWritePlatformService.updateOriginalSchedule(loan);
        }
        if (reprocessRequired) {
            ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
            this.loanRepositoryWrapper.save(loan);
        }

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && isAppliedOnBackDate
                && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
            this.loanAccountDomainService.recalculateAccruals(loan);
        }
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanCharge.getId())
                .withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId()).withGroupId(loan.getGroupId()).withLoanId(loanId)
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult loanChargeRefund(final Long loanId, final JsonCommand command) {

        this.loanChargeApiJsonValidator.validateLoanChargeRefundTransaction(command.json());

        final Long loanChargeId = command.longValueOfParameterNamed("loanChargeId");
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");

        final LoanInstallmentCharge installmentChargeEntry = loanChargeRefundEntranceValidation(loanCharge, installmentNumber, dueDate);
        Integer installmentNumberIdentified = null;
        if (installmentChargeEntry != null) {
            installmentNumberIdentified = installmentChargeEntry.getRepaymentInstallment().getInstallmentNumber();
        }
        final BigDecimal fullRefundAbleAmount = loanChargeValidateRefundAmount(loanCharge, installmentChargeEntry, transactionAmount);

        JsonCommand repaymentJsonCommand = adaptLoanChargeRefundCommandForFurtherRepaymentProcessing(command, fullRefundAbleAmount);

        boolean isRecoveryRepayment = false;
        String chargeRefundChargeType = "F";
        if (loanCharge.isPenaltyCharge()) {
            chargeRefundChargeType = "P";
        }
        // chargeRefundChargeType only included as a parameter for accounting reason - in order to identify whether fee
        // or penalty GL account is relevant
        CommandProcessingResult result = loanWritePlatformService.makeLoanRepaymentWithChargeRefundChargeType(
                LoanTransactionType.CHARGE_REFUND, repaymentJsonCommand.getLoanId(), repaymentJsonCommand, isRecoveryRepayment,
                chargeRefundChargeType);

        Long loanChargeRefundTransactionId = result.getResourceId();
        LoanTransaction newChargeRefundTxn = null;
        for (LoanTransaction chargeRefundTxn : loanCharge.getLoan().getLoanTransactions()) {
            if (loanChargeRefundTransactionId.equals(chargeRefundTxn.getId())) {
                newChargeRefundTxn = chargeRefundTxn;
                final BigDecimal appliedRefundAmount = newChargeRefundTxn.getAmount(loanCharge.getLoan().getCurrency()).getAmount()
                        .multiply(BigDecimal.valueOf(-1));
                final LoanChargePaidBy loanChargePaidByForChargeRefund = new LoanChargePaidBy(newChargeRefundTxn, loanCharge,
                        appliedRefundAmount, installmentNumberIdentified);
                newChargeRefundTxn.getLoanChargesPaid().add(loanChargePaidByForChargeRefund);
                loanCharge.getLoanChargePaidBySet().add(loanChargePaidByForChargeRefund);
                break;
            }
        }
        if (newChargeRefundTxn != null) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(newChargeRefundTxn.getLoan()));
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeRefundBusinessEvent(newChargeRefundTxn));
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoWaiveLoanCharge(final JsonCommand command) {

        LoanTransaction loanTransaction = this.loanTransactionRepository.findByIdAndLoanId(command.entityId(), command.getLoanId())
                .orElseThrow(() -> new LoanTransactionNotFoundException(command.entityId(), command.getLoanId()));
        if (!loanTransaction.getTypeOf().getCode().equals(LoanTransactionType.WAIVE_CHARGES.getCode())) {
            throw new InvalidLoanTransactionTypeException("transaction", "undo.waive.charge", "Transaction is not a waive charge type.");
        }
        if (!loanTransaction.isNotReversed()) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.ALREADY_REVERSED, loanTransaction.getId());
        }

        Set<LoanChargePaidBy> loanChargePaidBySet = loanTransaction.getLoanChargesPaid();
        LoanChargePaidBy loanChargePaidBy = loanChargePaidBySet.stream().findFirst().orElseThrow(LoanChargeNotFoundException::new);
        final LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        // Validate loan charge is not already paid
        if (loanCharge.isPaid()) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.ALREADY_PAID, loanCharge.getId());
        }

        final Long loanId = loanTransaction.getLoan().getId();
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.LOAN_INACTIVE, loanCharge.getId());
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));

        undoWaivedCharge(changes, loan, loanTransaction, loanChargePaidBy);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));

        changes.put("principalPortion", loanTransaction.getPrincipalPortion());
        changes.put("interestPortion", loanTransaction.getInterestPortion(loan.getCurrency()));
        changes.put("feeChargesPortion", loanTransaction.getFeeChargesPortion(loan.getCurrency()));
        changes.put("penaltyChargesPortion", loanTransaction.getPenaltyChargesPortion(loan.getCurrency()));
        changes.put("outstandingLoanBalance", loanTransaction.getOutstandingLoanBalance());
        changes.put("id", loanTransaction.getId());
        changes.put("date", loanTransaction.getTransactionDate());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withLoanId(loanId) //
                .with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.loanChargeApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeUpdatedException(
                    LoanChargeCannotBeUpdatedException.LoanChargeCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));

        final Map<String, Object> changes = loan.updateLoanCharge(loanCharge, command);

        this.loanRepositoryWrapper.save(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        this.loanChargeApiJsonValidator.validateInstallmentChargeTransaction(command.json());
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedException.LoanChargeCannotBeWaivedReason.LOAN_INACTIVE,
                    loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedException.LoanChargeCannotBeWaivedReason.ALREADY_WAIVED,
                    loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBeWaivedException(LoanChargeCannotBeWaivedException.LoanChargeCannotBeWaivedReason.ALREADY_PAID,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveChargeBusinessEvent(loanCharge));
        Integer loanInstallmentNumber = null;
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            if (!StringUtils.isBlank(command.json())) {
                final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
                final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
                if (dueDate != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
                } else if (installmentNumber != null) {
                    chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
                }
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_WAIVED,
                        loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_PAID,
                        loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
        }

        final Map<String, Object> changes = new LinkedHashMap<>(3);

        final List<Long> existingTransactionIds = new ArrayList<>();
        final List<Long> existingReversedTransactionIds = new ArrayList<>();
        LocalDate recalculateFrom = null;
        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        Money accruedCharge = Money.zero(loan.getCurrency());
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Collection<LoanChargePaidByData> chargePaidByCollection = this.loanChargeReadPlatformService
                    .retrieveLoanChargesPaidBy(loanCharge.getId(), LoanTransactionType.ACCRUAL, loanInstallmentNumber);
            for (LoanChargePaidByData chargePaidByData : chargePaidByCollection) {
                accruedCharge = accruedCharge.plus(chargePaidByData.getAmount());
            }
        }

        final LoanTransaction waiveTransaction = loan.waiveLoanCharge(loanCharge, defaultLoanLifecycleStateMachine, changes,
                existingTransactionIds, existingReversedTransactionIds, loanInstallmentNumber, scheduleGeneratorDTO, accruedCharge);

        this.loanTransactionRepository.saveAndFlush(waiveTransaction);
        this.loanRepositoryWrapper.save(loan);

        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeBusinessEvent(loanCharge));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeDeletedException(
                    LoanChargeCannotBeDeletedException.LoanChargeCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));

        loan.removeLoanCharge(loanCharge);
        this.loanRepositoryWrapper.save(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult payLoanCharge(final Long loanId, Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {

        this.loanChargeApiJsonValidator.validateChargePaymentTransaction(command.json(), isChargeIdIncludedInJson);
        if (isChargeIdIncludedInJson) {
            loanChargeId = command.longValueOfParameterNamed("chargeId");
        }
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.LOAN_INACTIVE,
                    loanCharge.getId());
        }

        // validate loan charge is not already paid or waived
        if (loanCharge.isWaived()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_WAIVED,
                    loanCharge.getId());
        } else if (loanCharge.isPaid()) {
            throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_PAID,
                    loanCharge.getId());
        }

        if (!loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            throw new LoanChargeCannotBePayedException(
                    LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.CHARGE_NOT_ACCOUNT_TRANSFER, loanCharge.getId());
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        Integer loanInstallmentNumber = null;
        BigDecimal amount = loanCharge.amountOutstanding();
        if (loanCharge.isInstalmentFee()) {
            LoanInstallmentCharge chargePerInstallment = null;
            final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
            final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
            if (dueDate != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }
            if (chargePerInstallment == null) {
                chargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
            }
            if (chargePerInstallment.isWaived()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_WAIVED,
                        loanCharge.getId());
            } else if (chargePerInstallment.isPaid()) {
                throw new LoanChargeCannotBePayedException(LoanChargeCannotBePayedException.LoanChargeCannotBePayedReason.ALREADY_PAID,
                        loanCharge.getId());
            }
            loanInstallmentNumber = chargePerInstallment.getRepaymentInstallment().getInstallmentNumber();
            amount = chargePerInstallment.getAmountOutstanding();
        }

        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Charge with id:" + loanChargeId + " requires linked savings account for payment";
            throw new LinkedAccountRequiredException("loanCharge.pay", errorMessage, loanChargeId);
        }
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, amount, PortfolioAccountType.SAVINGS,
                PortfolioAccountType.LOAN, portfolioAccountData.getId(), loanId, "Loan Charge Payment", locale, fmt, null, null,
                LoanTransactionType.CHARGE_PAYMENT.getValue(), loanChargeId, loanInstallmentNumber,
                AccountTransferType.CHARGE_PAYMENT.getValue(), null, null, null, null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withSavingsId(portfolioAccountData.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustmentForLoanCharge(Long loanId, Long loanChargeId, JsonCommand command) {
        this.loanChargeApiJsonValidator.validateLoanAdjustmentRequest(loanId, loanChargeId, command.json());

        final LoanCharge loanCharge = retrieveLoanChargeBy(loanId, loanChargeId);
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("amount");
        final String txnExternalId = command.stringValueOfParameterNamedAllowingNull("externalId");
        final String locale = command.locale();

        loanChargeAdjustmentEntranceValidation(loanCharge, transactionAmount);
        final Loan loan = loanAssembler.assembleFrom(loanId);

        final CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder();

        LoanTransaction loanTransaction = applyChargeAdjustment(loan, loanCharge, transactionAmount, transactionDate, txnExternalId);

        // Update loan transaction on repayment.
        if (AccountType.fromInt(loan.getLoanType()).isIndividualAccount()) {
            Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
            for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
                loanCollateralManagement.setLoanTransactionData(loanTransaction);
                ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();

                if (loan.getStatus().isClosed()) {
                    loanCollateralManagement.setIsReleased(true);
                    BigDecimal quantity = loanCollateralManagement.getQuantity();
                    clientCollateralManagement.updateQuantity(clientCollateralManagement.getQuantity().add(quantity));
                    loanCollateralManagement.setClientCollateralManagement(clientCollateralManagement);
                }
            }
            this.loanAccountDomainService.updateLoanCollateralTransaction(loanCollateralManagements);
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanChargeAdjustmentPostBusinessEvent(loanTransaction));
        Map<String, Object> changes = new HashMap<>();
        changes.put("externalId", txnExternalId);
        changes.put("amount", transactionAmount);
        changes.put("transactionDate", transactionDate);
        changes.put("locale", locale);
        return commandProcessingResultBuilder.withCommandId(command.commandId()) //
                .withLoanId(loanId) //
                .withEntityId(loanTransaction.getId()).with(changes) //
                .build();
    }

    private LoanTransaction applyChargeAdjustment(final Loan loan, final LoanCharge loanCharge, final BigDecimal transactionAmount,
            final LocalDate transactionDate, final String txnExternalId) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanChargeAdjustmentPreBusinessEvent(loan));
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());

        LoanTransaction loanChargeAdjustmentTransaction = LoanTransaction.chargeAdjustment(loan, transactionAmount, transactionDate,
                txnExternalId);
        LoanTransactionRelation loanTransactionRelation = LoanTransactionRelation.linkToCharge(loanChargeAdjustmentTransaction, loanCharge,
                LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT);
        loanChargeAdjustmentTransaction.getLoanTransactionRelations().add(loanTransactionRelation);

        loanAccountDomainService.saveLoanTransactionWithDataIntegrityViolationChecks(loanChargeAdjustmentTransaction);

        defaultLoanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT_OR_WAIVER, loan);
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());
        loanRepaymentScheduleTransactionProcessor.handleTransaction(loanChargeAdjustmentTransaction, loan.getCurrency(),
                loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());

        loan.addLoanTransaction(loanChargeAdjustmentTransaction);
        loan.updateLoanSummaryAndStatus();

        loanAccountDomainService.saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(loan.getCurrency().getCode(),
                existingTransactionIds, existingReversedTransactionIds, false);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);

        loanAccountDomainService.setLoanDelinquencyTag(loan, transactionDate);

        return loanChargeAdjustmentTransaction;
    }

    @Transactional
    @Override
    public void applyOverdueChargesForLoan(final Long loanId, Collection<OverdueLoanScheduleData> overdueLoanScheduleDataList) {

        Loan loan = this.loanAssembler.assembleFrom(loanId);
        final List<Long> existingTransactionIds = loan.findExistingTransactionIds();
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();
        boolean runInterestRecalculation = false;
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();
        LocalDate lastChargeDate = null;
        for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduleDataList) {

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(overdueInstallment.toString());
            final JsonCommand command = JsonCommand.from(overdueInstallment.toString(), parsedCommand, this.fromApiJsonHelper, null, null,
                    null, null, null, loanId, null, null, null, null, null, null, null);
            LoanOverdueDTO overdueDTO = applyChargeToOverdueLoanInstallment(loan, overdueInstallment.getChargeId(),
                    overdueInstallment.getPeriodNumber(), command);
            loan = overdueDTO.getLoan();
            runInterestRecalculation = runInterestRecalculation || overdueDTO.isRunInterestRecalculation();
            if (recalculateFrom.isAfter(overdueDTO.getRecalculateFrom())) {
                recalculateFrom = overdueDTO.getRecalculateFrom();
            }
            if (lastChargeDate == null || overdueDTO.getLastChargeAppliedDate().isAfter(lastChargeDate)) {
                lastChargeDate = overdueDTO.getLastChargeAppliedDate();
            }
        }
        if (loan != null) {
            boolean reprocessRequired = true;
            LocalDate recalculatedTill = loan.fetchInterestRecalculateFromDate();
            if (recalculateFrom.isAfter(recalculatedTill)) {
                recalculateFrom = recalculatedTill;
            }

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
                if (runInterestRecalculation && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                    runScheduleRecalculation(loan, recalculateFrom);
                    reprocessRequired = false;
                }
                this.loanWritePlatformService.updateOriginalSchedule(loan);
            }

            if (reprocessRequired) {
                addInstallmentIfPenaltyAppliedAfterLastDueDate(loan, lastChargeDate);
                ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
                if (changedTransactionDetail != null) {
                    for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings()
                            .entrySet()) {
                        this.loanTransactionRepository.save(mapEntry.getValue());
                        // update loan with references to the newly created
                        // transactions
                        loan.addLoanTransaction(mapEntry.getValue());
                        this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            }

            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

            if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled() && runInterestRecalculation
                    && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                this.loanAccountDomainService.recalculateAccruals(loan);
            }
            this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
            businessEventNotifierService.notifyPostBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));
        }
    }

    private void undoWaivedCharge(final Map<String, Object> changes, final Loan loan, final LoanTransaction loanTransaction,
            final LoanChargePaidBy loanChargePaidBy) {
        switch (loanChargePaidBy.getLoanCharge().getChargeTimeType()) {
            case SPECIFIED_DUE_DATE -> undoSpecifiedDueDateCharge(changes, loan, loanTransaction, loanChargePaidBy);
            case INSTALMENT_FEE -> undoInstalmentFee(changes, loan, loanTransaction, loanChargePaidBy);
            default -> throw new UnsupportedOperationException(
                    "Undo waive charge is not support for this charge: " + loanChargePaidBy.getLoanCharge().getChargeTimeType());
        }
    }

    private void undoInstalmentFee(Map<String, Object> changes, Loan loan, LoanTransaction loanTransaction,
            LoanChargePaidBy loanChargePaidBy) {
        final List<Long> existingTransactionIds = loan.findExistingTransactionIds();
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        final Integer installmentNumber = loanChargePaidBy.getInstallmentNumber();
        LoanInstallmentCharge chargePerInstallment;
        // final Integer installmentNumber = command.integerValueOfParameterNamed("installmentNumber");
        if (installmentNumber != null) {
            // Get installment charge.
            chargePerInstallment = loanCharge.getInstallmentLoanCharge(installmentNumber);
            // Get installment amount waived.
            BigDecimal amountWaived = chargePerInstallment.getAmountWaived(loan.getCurrency()).getAmount();
            // Check whether the installment charge is not waived. If so throw new error
            if (!chargePerInstallment.isWaived() || amountWaived == null) {
                throw new LoanChargeWaiveCannotBeReversedException(
                        LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.NOT_WAIVED, loanCharge.getId());
            }
            // Reverse waived transaction
            loanTransaction.reverse();
            // Set manually adjusted value to `1`
            loanTransaction.setManuallyAdjustedOrReversed();
            // Get loan charge outstanding amount
            BigDecimal amountOutstanding = loanCharge.getAmountOutstanding(loan.getCurrency()).getAmount();
            // Add the amount waived to outstanding amount
            loanCharge.setOutstandingAmount(amountOutstanding.add(amountWaived));
            // Get loan charge total amount waived
            BigDecimal totalAmountWaved = loanCharge.getAmountWaived(loan.getCurrency()).getAmount();
            // Subtract the amount waived from the existing amount waived.
            loanCharge.setAmountWaived(totalAmountWaved.subtract(amountWaived));
            // Get installment outstanding amount
            BigDecimal amountOutstandingPerInstallment = chargePerInstallment.getAmountOutstanding();
            // Add the amount waived to the outstanding amount of the installment
            chargePerInstallment.setOutstandingAmount(amountOutstandingPerInstallment.add(amountWaived));
            // Set the amount waived value to ZERO
            chargePerInstallment.setAmountWaived(null);
            // Reset waived flag
            chargePerInstallment.undoWaiveFlag();
            // Update installment balances
            updateRepaymentInstalmentWithWaivedAmount(loanCharge, chargePerInstallment.getInstallment(), amountWaived);
            // Update loan charge.
            loanCharge.setInstallmentLoanCharge(chargePerInstallment, chargePerInstallment.getInstallment().getInstallmentNumber());
            if (loanCharge.amount().compareTo(loanCharge.amountOutstanding()) == 0 && loanCharge.isWaived()) {
                loanCharge.undoWaived();
            }
            loan.updateLoanSummaryForUndoWaiveCharge(amountWaived, loanCharge.isPenaltyCharge());
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            changes.put(AMOUNT, amountWaived);
        } else {
            throw new InstallmentNotFoundException(loanTransaction.getId());
        }
    }

    private void undoSpecifiedDueDateCharge(final Map<String, Object> changes, final Loan loan, final LoanTransaction loanTransaction,
            final LoanChargePaidBy loanChargePaidBy) {

        final List<Long> existingTransactionIds = loan.findExistingTransactionIds();
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        BigDecimal amountWaived = loanCharge.getAmountWaived(loan.getCurrency()).getAmount();
        if (!loanCharge.isWaived() || amountWaived == null) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.NOT_WAIVED, loanCharge.getId());
        }
        loanTransaction.reverse();
        loanTransaction.setManuallyAdjustedOrReversed();
        loanCharge.setOutstandingAmount(loanCharge.amountOutstanding().add(amountWaived));
        loanCharge.setAmountWaived(null);
        loanCharge.undoWaived();
        LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallments().stream()
                .filter(e -> isPartOfThisInstallment(loanCharge, e)).findFirst().orElseThrow();
        updateRepaymentInstalmentWithWaivedAmount(loanCharge, installment, amountWaived);
        loan.updateLoanSummaryForUndoWaiveCharge(amountWaived, loanCharge.isPenaltyCharge());
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        changes.put(AMOUNT, amountWaived);
    }

    private void updateRepaymentInstalmentWithWaivedAmount(final LoanCharge loanCharge, final LoanRepaymentScheduleInstallment installment,
            final BigDecimal amountWaived) {
        if (loanCharge.isPenaltyCharge()) {
            // Get the penalty charges waived amount per installment
            BigDecimal penaltyChargesWaivedAmount = installment.getPenaltyChargesWaived(loanCharge.getLoan().getCurrency()).getAmount();
            // Subtract the amount waived from the existing fee charges waived amount.
            installment.setPenaltyChargesWaived(penaltyChargesWaivedAmount.subtract(amountWaived));
        } else {
            // Get the fee charges waived amount per installment
            BigDecimal feeChargesWaivedAmount = installment.getFeeChargesWaived(loanCharge.getLoan().getCurrency()).getAmount();
            // Subtract the amount waived from the existing fee charges waived amount.
            installment.setFeeChargesWaived(feeChargesWaivedAmount.subtract(amountWaived));
        }
    }

    private void validateAddingNewChargeAllowed(List<LoanDisbursementDetails> loanDisburseDetails) {
        boolean pendingDisbursementAvailable = false;
        for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
            if (disbursementDetail.actualDisbursementDate() == null) {
                pendingDisbursementAvailable = true;
                break;
            }
        }
        if (!pendingDisbursementAvailable) {
            throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.no.pending.disbursements.in.loan",
                    "This charge cannot be added, No disbursement is pending");
        }
    }

    private void validateAddLoanCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {
        if (chargeDefinition.isOverdueInstallment()) {
            final String defaultUserMessage = "Installment charge cannot be added to the loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "overdue.charge", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loanCharge.getDueLocalDate() != null
                && loanCharge.getDueLocalDate().isBefore(loan.getLastUserTransactionForChargeCalc())) {
            final String defaultUserMessage = "charge with date before last transaction date can not be added to loan.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "date.is.before.last.transaction.date", defaultUserMessage, null,
                    chargeDefinition.getName());
        } else if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {

            if (loanCharge.isInstalmentFee() && loan.getStatus().isActive()) {
                final String defaultUserMessage = "installment charge addition not allowed after disbursement";
                throw new LoanChargeCannotBeAddedException("loanCharge", "installment.charge", defaultUserMessage, null,
                        chargeDefinition.getName());
            }
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final Set<LoanCharge> loanCharges = new HashSet<>(1);
            loanCharges.add(loanCharge);
            this.loanChargeApiJsonValidator.validateLoanCharges(loanCharges, dataValidationErrors);
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }

    private boolean addCharge(final Loan loan, final Charge chargeDefinition, final LoanCharge loanCharge) {

        if (!loan.hasCurrencyCodeOf(chargeDefinition.getCurrencyCode())) {
            final String errorMessage = "Charge and Loan must have the same currency.";
            throw new InvalidCurrencyException("loanCharge", "attach.to.loan", errorMessage);
        }

        if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
            final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                    .retriveLoanLinkedAssociation(loan.getId());
            if (portfolioAccountData == null) {
                final String errorMessage = loanCharge.name() + "Charge  requires linked savings account for payment";
                throw new LinkedAccountRequiredException("loanCharge.add", errorMessage, loanCharge.name());
            }
        }

        if (!loan.isInterestBearing() && loanCharge.isSpecifiedDueDate() && loanCharge.getDueDate().isAfter(loan.getMaturityDate())) {
            LoanRepaymentScheduleInstallment latestRepaymentScheduleInstalment = loan.getRepaymentScheduleInstallments()
                    .get(loan.getLoanRepaymentScheduleInstallmentsSize() - 1);
            if (loanCharge.getDueDate().isAfter(latestRepaymentScheduleInstalment.getDueDate())) {
                if (latestRepaymentScheduleInstalment.isAdditional()) {
                    latestRepaymentScheduleInstalment.updateDueDate(loanCharge.getDueDate());
                } else {
                    final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan,
                            (loan.getLoanRepaymentScheduleInstallmentsSize() + 1), latestRepaymentScheduleInstalment.getDueDate(),
                            loanCharge.getDueDate(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null);
                    installment.markAsAdditional();
                    loan.addLoanRepaymentScheduleInstallment(installment);
                }
            }
        }

        loan.addLoanCharge(loanCharge);

        this.loanChargeRepository.saveAndFlush(loanCharge);

        /**
         * we want to apply charge transactions only for those loans charges that are applied when a loan is active and
         * the loan product uses Upfront Accruals
         **/
        if (loan.getStatus().isActive() && loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
            final LoanTransaction applyLoanChargeTransaction = loan.handleChargeAppliedTransaction(loanCharge, null);
            this.loanTransactionRepository.saveAndFlush(applyLoanChargeTransaction);
        }
        return loanCharge.getDueLocalDate() == null || DateUtils.getBusinessLocalDate().isAfter(loanCharge.getDueLocalDate());
    }

    private LoanOverdueDTO applyChargeToOverdueLoanInstallment(final Loan loan, final Long loanChargeId, final Integer periodNumber,
            final JsonCommand command) {
        boolean runInterestRecalculation = false;
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(loanChargeId);

        Collection<Integer> frequencyNumbers = loanChargeReadPlatformService.retrieveOverdueInstallmentChargeFrequencyNumber(loan,
                chargeDefinition, periodNumber);

        Integer feeFrequency = chargeDefinition.feeFrequency();
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        Map<Integer, LocalDate> scheduleDates = new HashMap<>();
        final Long penaltyWaitPeriodValue = this.configurationDomainService.retrievePenaltyWaitPeriod();
        final Long penaltyPostingWaitPeriodValue = this.configurationDomainService.retrieveGraceOnPenaltyPostingPeriod();
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        long diff = penaltyWaitPeriodValue + 1 - penaltyPostingWaitPeriodValue;
        if (diff < 1) {
            diff = 1L;
        }
        LocalDate startDate = dueDate.plusDays(penaltyWaitPeriodValue + 1L);
        int frequencyNumber = 1;
        if (feeFrequency == null) {
            scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));
        } else {
            while (!startDate.isAfter(DateUtils.getBusinessLocalDate())) {
                scheduleDates.put(frequencyNumber++, startDate.minusDays(diff));

                startDate = scheduledDateGenerator.getRepaymentPeriodDate(PeriodFrequencyType.fromInt(feeFrequency),
                        chargeDefinition.feeInterval(), startDate);
            }
        }

        for (Integer frequency : frequencyNumbers) {
            scheduleDates.remove(frequency);
        }

        LoanRepaymentScheduleInstallment installment = null;
        LocalDate lastChargeAppliedDate = dueDate;
        if (!scheduleDates.isEmpty()) {
            installment = loan.fetchRepaymentScheduleInstallment(periodNumber);
            lastChargeAppliedDate = installment.getDueDate();
        }
        LocalDate recalculateFrom = DateUtils.getBusinessLocalDate();

        if (loan != null) {
            businessEventNotifierService.notifyPreBusinessEvent(new LoanApplyOverdueChargeBusinessEvent(loan));
            for (Map.Entry<Integer, LocalDate> entry : scheduleDates.entrySet()) {

                final LoanCharge loanCharge = LoanCharge.createNewFromJson(loan, chargeDefinition, command, entry.getValue());

                if (BigDecimal.ZERO.compareTo(loanCharge.amount()) == 0) {
                    continue;
                }
                LoanOverdueInstallmentCharge overdueInstallmentCharge = new LoanOverdueInstallmentCharge(loanCharge, installment,
                        entry.getKey());
                loanCharge.updateOverdueInstallmentCharge(overdueInstallmentCharge);

                boolean isAppliedOnBackDate = addCharge(loan, chargeDefinition, loanCharge);
                runInterestRecalculation = runInterestRecalculation || isAppliedOnBackDate;
                if (entry.getValue().isBefore(recalculateFrom)) {
                    recalculateFrom = entry.getValue();
                }
                if (entry.getValue().isAfter(lastChargeAppliedDate)) {
                    lastChargeAppliedDate = entry.getValue();
                }
            }
        }

        return new LoanOverdueDTO(loan, runInterestRecalculation, recalculateFrom, lastChargeAppliedDate);
    }

    private void addInstallmentIfPenaltyAppliedAfterLastDueDate(Loan loan, LocalDate lastChargeDate) {
        if (lastChargeDate != null) {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            LoanRepaymentScheduleInstallment lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
            if (lastChargeDate.isAfter(lastInstallment.getDueDate())) {
                if (lastInstallment.isRecalculatedInterestComponent()) {
                    installments.remove(lastInstallment);
                    lastInstallment = loan.fetchRepaymentScheduleInstallment(installments.size());
                }
                boolean recalculatedInterestComponent = true;
                BigDecimal principal = BigDecimal.ZERO;
                BigDecimal interest = BigDecimal.ZERO;
                BigDecimal feeCharges = BigDecimal.ZERO;
                BigDecimal penaltyCharges = BigDecimal.ONE;
                final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
                LoanRepaymentScheduleInstallment newEntry = new LoanRepaymentScheduleInstallment(loan, installments.size() + 1,
                        lastInstallment.getDueDate(), lastChargeDate, principal, interest, feeCharges, penaltyCharges,
                        recalculatedInterestComponent, compoundingDetails);
                loan.addLoanRepaymentScheduleInstallment(newEntry);
            }
        }
    }

    public void runScheduleRecalculation(final Loan loan, final LocalDate recalculateFrom) {
        if (loan.repaymentScheduleDetail().isInterestRecalculationEnabled()) {
            ScheduleGeneratorDTO generatorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
            ChangedTransactionDetail changedTransactionDetail = loan
                    .handleRegenerateRepaymentScheduleWithInterestRecalculation(generatorDTO);
            this.loanRepositoryWrapper.save(loan);
            if (changedTransactionDetail != null) {
                for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                    this.loanTransactionRepository.save(mapEntry.getValue());
                    // update loan with references to the newly created
                    // transactions
                    loan.addLoanTransaction(mapEntry.getValue());
                    this.accountTransfersWritePlatformService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
    }

    private JsonCommand adaptLoanChargeRefundCommandForFurtherRepaymentProcessing(JsonCommand command, BigDecimal fullRefundAbleAmount) {
        // creates JsonCommand for onward repayment processing
        JsonObject jsonObject = (JsonObject) this.fromApiJsonHelper.parse(command.json());

        String dateFormat;
        if (this.fromApiJsonHelper.parameterExists("dateFormat", jsonObject)) {
            dateFormat = this.fromApiJsonHelper.extractStringNamed("dateFormat", jsonObject);
        } else {
            dateFormat = "dd MMMM yyyy";
            jsonObject.addProperty("dateFormat", dateFormat);
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
        String transactionDateString = transactionDate.format(dateTimeFormatter);
        jsonObject.addProperty("transactionDate", transactionDateString);
        if (!this.fromApiJsonHelper.parameterExists("transactionAmount", jsonObject)) {
            jsonObject.addProperty("transactionAmount", fullRefundAbleAmount.toString());
        }
        jsonObject.remove("loanChargeId");
        jsonObject.remove("installmentNumber");
        jsonObject.remove("dueDate");

        return JsonCommand.fromExistingCommand(command, jsonObject);
    }

    private BigDecimal loanChargeValidateRefundAmount(LoanCharge loanCharge, LoanInstallmentCharge installmentChargeEntry,
            BigDecimal transactionAmount) {
        // if transactionAmount not provided return max refundable amount (amount paid minus previous refunds)
        BigDecimal chargeAmountPaid;
        BigDecimal chargeAmountRefunded = BigDecimal.ZERO;
        MonetaryCurrency loanCurrency = loanCharge.getLoan().getCurrency();
        if (loanCharge.isInstalmentFee() && installmentChargeEntry != null) {
            final Integer installmentNumber = installmentChargeEntry.getRepaymentInstallment().getInstallmentNumber();
            chargeAmountPaid = installmentChargeEntry.getAmountPaid(loanCurrency).getAmount();
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (installmentNumber.equals(loanChargePaidBy.getInstallmentNumber()) && isRefundElementOfChargeRefund(loanChargePaidBy)) {
                    chargeAmountRefunded = chargeAmountRefunded.add(loanChargePaidBy.getAmount());
                }
            }
        } else {
            chargeAmountPaid = loanCharge.getAmountPaid(loanCurrency).getAmount();
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (isRefundElementOfChargeRefund(loanChargePaidBy)) {
                    chargeAmountRefunded = chargeAmountRefunded.add(loanChargePaidBy.getAmount());
                }
            }
        }
        chargeAmountRefunded = chargeAmountRefunded.multiply(BigDecimal.valueOf(-1));

        if (chargeAmountRefunded.compareTo(chargeAmountPaid) > 0) {
            final String errorMessage = "loan.charge.more.refunded.than.paid.unexpected.system.error";
            final String details = "Paid: " + chargeAmountPaid.toString() + "  Refunded: " + chargeAmountPaid;
            throw new LoanChargeRefundException(errorMessage, details);
        }

        BigDecimal refundableAmount = chargeAmountPaid.subtract(chargeAmountRefunded);
        // refund amount was provided.
        if (transactionAmount != null && transactionAmount.compareTo(refundableAmount) > 0) {
            final String errorMessage = "loan.charge.transaction.amount.is.more.than.is.refundable";
            final String details = "transactionAmount: " + transactionAmount + "  Refundable: " + refundableAmount;
            throw new LoanChargeRefundException(errorMessage, details);
        }

        return refundableAmount;
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {

        final MonetaryCurrency currency = loan.getCurrency();
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private LoanCharge retrieveLoanChargeBy(final Long loanId, final Long loanChargeId) {
        final LoanCharge loanCharge = this.loanChargeRepository.findById(loanChargeId)
                .orElseThrow(() -> new LoanChargeNotFoundException(loanChargeId));

        if (loanCharge.hasNotLoanIdentifiedBy(loanId)) {
            throw new LoanChargeNotFoundException(loanChargeId, loanId);
        }
        return loanCharge;
    }

    private boolean isRefundElementOfChargeRefund(LoanChargePaidBy loanChargePaidBy) {
        // The Refund Element is always negative
        return (loanChargePaidBy.getLoanTransaction().isChargeRefund() && loanChargePaidBy.getAmount().compareTo(BigDecimal.ZERO) < 0);
    }

    private LoanInstallmentCharge loanChargeRefundEntranceValidation(LoanCharge loanCharge, Integer installmentNumber, LocalDate dueDate) {

        LoanInstallmentCharge installmentChargeEntry = null;

        Loan loan = loanCharge.getLoan();
        if (!(loan.isOpen() || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid())) {
            final String errorMessage = "loan.charge.refund.invalid.status";
            throw new LoanChargeRefundException(errorMessage, loan.getStatus().toString());
        }

        if (dueDate != null && installmentNumber != null) {
            throwLoanChargeRefundException("loan.charge.refund.dueDate.and.installmentNumber.provided.use.only.one", installmentNumber,
                    dueDate);
        }

        if (loanCharge.isInstalmentFee()) { // identify specific installment
            if (dueDate == null && installmentNumber == null) {
                throwLoanChargeRefundException(
                        "loan.charge.refund.neither.dueDate.nor.installmentNumber.provided.for.this.installment.charge", installmentNumber,
                        dueDate);
            }

            if (dueDate != null) {
                installmentChargeEntry = loanCharge.getInstallmentLoanCharge(dueDate);
            } else if (installmentNumber != null) {
                installmentChargeEntry = loanCharge.getInstallmentLoanCharge(installmentNumber);
            }

            if (installmentChargeEntry == null) {
                throwLoanChargeRefundException("loan.charge.refund.installment.not.found", installmentNumber, dueDate);
            }
        } else {

            if (dueDate != null || installmentNumber != null) {
                throwLoanChargeRefundException(
                        "loan.charge.refund.dueDate.or.installmentNumber.provided.but.this.is.not.an.installment.charge", installmentNumber,
                        dueDate);
            }
        }

        return installmentChargeEntry;
    }

    private void loanChargeAdjustmentEntranceValidation(final LoanCharge loanCharge, final BigDecimal transactionAmount) {
        final Loan loan = loanCharge.getLoan();
        if (!(loan.isOpen() || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid())) {
            final String errorCode = "loan.charge.adjustment.invalid.status";
            throw new LoanChargeAdjustmentException(errorCode,
                    "Adjustment is not supported for the status of " + loan.getStatus().toString());
        }

        if (transactionAmount.compareTo(loanCharge.amount()) > 0) {
            final String errorCode = "loan.charge.adjustment.invalid.amount";
            throw new LoanChargeAdjustmentException(errorCode,
                    "Transaction amount cannot be higher than the charge amount: " + loanCharge.amount());
        }

        BigDecimal availableAmountForAdjustment = calculateAvailableAmountForChargeAdjustment(loanCharge);
        if (transactionAmount.compareTo(availableAmountForAdjustment) > 0) {
            final String errorCode = "loan.charge.adjustment.invalid.amount";
            throw new LoanChargeAdjustmentException(errorCode,
                    "Transaction amount cannot be higher than the available charge amount for adjustment: " + availableAmountForAdjustment);
        }
        checkClientOrGroupActive(loan);
        loan.validateAccountStatus(LoanEvent.LOAN_CHARGE_ADJUSTMENT);
    }

    private BigDecimal calculateAvailableAmountForChargeAdjustment(final LoanCharge loanCharge) {
        BigDecimal availableAmountForAdjustment = loanCharge.amount();
        for (LoanTransaction loanTransaction : loanCharge.getLoan().getLoanTransactions()) {
            if (loanTransaction.isNotReversed() && loanTransaction.getTypeOf().isChargeAdjustment()) {
                LoanTransactionRelation loanTransactionRelation = loanTransaction.getLoanTransactionRelations().stream()
                        .filter(e -> e.getToCharge() != null).findFirst().orElseThrow();
                if (loanCharge.equals(loanTransactionRelation.getToCharge())) {
                    availableAmountForAdjustment = availableAmountForAdjustment.subtract(loanTransaction.getAmount());
                }
            }
        }
        return availableAmountForAdjustment;
    }

    private void throwLoanChargeRefundException(String errorMessage, Integer installmentNumber, LocalDate dueDate) {
        String dueDateValue = "";
        String installmentNumberValue = "";
        if (dueDate != null) {
            dueDateValue = dueDate.toString();
        }
        if (installmentNumber != null) {
            installmentNumberValue = installmentNumber.toString();
        }
        throw new LoanChargeRefundException(errorMessage, "dueDate: " + dueDateValue + "  installmentNumber: " + installmentNumberValue);
    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }
}
