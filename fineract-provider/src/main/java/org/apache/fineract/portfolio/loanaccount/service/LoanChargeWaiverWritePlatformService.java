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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeUndoBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBePayedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeWaivedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWaiveCannotBeReversedException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InstallmentNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.springframework.transaction.annotation.Transactional;

/**
 * Waives loan charges and undoes waivers.
 */
@RequiredArgsConstructor
public class LoanChargeWaiverWritePlatformService {

    private static final String AMOUNT = "amount";
    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanAssembler loanAssembler;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanUtilService loanUtilService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final LoanChargeWriteHelper loanChargeWriteHelper;

    private static boolean isPartOfThisInstallment(LoanCharge loanCharge, LoanRepaymentScheduleInstallment e) {
        return DateUtils.isAfter(loanCharge.getDueDate(), e.getFromDate()) && !DateUtils.isAfter(loanCharge.getDueDate(), e.getDueDate());
    }

    @Transactional
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        this.loanChargeApiJsonValidator.validateInstallmentChargeTransaction(command.json());
        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);

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

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanApiConstants.externalIdParameterName, externalId);

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

        loanChargeValidator.validateLoanIsNotClosed(loan, loanCharge);
        final LoanTransaction waiveTransaction = waiveLoanCharge(loan, loanCharge, changes, loanInstallmentNumber, scheduleGeneratorDTO,
                accruedCharge, externalId);

        if (loan.isInterestBearingAndInterestRecalculationEnabled()
                && DateUtils.isBefore(loanCharge.getDueLocalDate(), DateUtils.getBusinessLocalDate())) {
            loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        this.loanTransactionRepository.saveAndFlush(waiveTransaction);
        this.loanRepositoryWrapper.save(loan);

        loanJournalEntryPoster.postJournalEntriesForLoanTransaction(waiveTransaction, false, false);

        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeBusinessEvent(loanCharge));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withSubEntityId(waiveTransaction.getId()) //
                .withSubEntityExternalId(waiveTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
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
        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        // Charges may be waived only when the loan associated with them are
        // active
        if (!loan.getStatus().isActive()) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.LOAN_INACTIVE, loanCharge.getId());
        }
        if (loan.isChargedOff() && !DateUtils.isAfter(loanTransaction.getTransactionDate(), loan.getChargedOffOnDate())) {
            throw new GeneralPlatformDomainRuleException("error.msg.transaction.date.cannot.be.earlier.than.charge.off.date",
                    "Undo Loan transaction: " + loanTransaction.getId()
                            + " is not allowed before or on the date when the loan got charged-off",
                    loanTransaction.getId());
        }
        final Map<String, Object> changes = new LinkedHashMap<>();

        businessEventNotifierService.notifyPreBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));

        undoWaivedCharge(changes, loan, loanTransaction, loanChargePaidBy);
        this.loanChargeWriteHelper.updateProgressiveInterestModel(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanWaiveChargeUndoBusinessEvent(loanCharge));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        changes.put("principalPortion", loanTransaction.getPrincipalPortion());
        changes.put("interestPortion", loanTransaction.getInterestPortion());
        changes.put("feeChargesPortion", loanTransaction.getFeeChargesPortion());
        changes.put("penaltyChargesPortion", loanTransaction.getPenaltyChargesPortion());
        changes.put("outstandingLoanBalance", loanTransaction.getOutstandingLoanBalance());
        changes.put("id", loanTransaction.getId());
        changes.put("externalId", loanTransaction.getExternalId());
        changes.put("date", loanTransaction.getTransactionDate());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withSubEntityId(loanTransaction.getId()) //
                .withSubEntityExternalId(loanTransaction.getExternalId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    public LoanTransaction waiveLoanCharge(final Loan loan, final LoanCharge loanCharge, final Map<String, Object> changes,
            final Integer loanInstallmentNumber, final ScheduleGeneratorDTO scheduleGeneratorDTO, final Money accruedCharge,
            final ExternalId externalId) {
        final Money amountWaived = loanCharge.waive(loan.getCurrency(), loanInstallmentNumber);
        changes.put("amount", amountWaived.getAmount());

        Money unrecognizedIncome = amountWaived.zero();
        Money chargeComponent = amountWaived;
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            Money receivableCharge;
            if (loanInstallmentNumber != null) {
                receivableCharge = accruedCharge
                        .minus(loanCharge.getInstallmentLoanCharge(loanInstallmentNumber).getAmountPaid(loan.getCurrency()));
            } else {
                receivableCharge = accruedCharge.minus(loanCharge.getAmountPaid(loan.getCurrency()));
            }
            if (receivableCharge.isLessThanZero()) {
                receivableCharge = amountWaived.zero();
            }
            if (amountWaived.isGreaterThan(receivableCharge)) {
                chargeComponent = receivableCharge;
                unrecognizedIncome = amountWaived.minus(receivableCharge);
            }
        }
        Money feeChargesWaived = chargeComponent;
        Money penaltyChargesWaived = Money.zero(loan.getCurrency());
        if (loanCharge.isPenaltyCharge()) {
            penaltyChargesWaived = chargeComponent;
            feeChargesWaived = Money.zero(loan.getCurrency());
        }

        LocalDate transactionDate = loan.getDisbursementDate();
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        if (loanCharge.isDueDateCharge()) {
            if (DateUtils.isAfter(loanCharge.getDueLocalDate(), businessDate)) {
                transactionDate = businessDate;
            } else {
                transactionDate = loanCharge.getDueLocalDate();
            }
        } else if (loanCharge.isInstalmentFee()) {
            LocalDate repaymentDueDate = loanCharge.getInstallmentLoanCharge(loanInstallmentNumber).getRepaymentInstallment().getDueDate();
            if (DateUtils.isAfter(repaymentDueDate, businessDate)) {
                transactionDate = businessDate;
            } else {
                transactionDate = repaymentDueDate;
            }
        }

        scheduleGeneratorDTO.setRecalculateFrom(transactionDate);

        loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());

        final LoanTransaction waiveLoanChargeTransaction = LoanTransaction.waiveLoanCharge(loan, loan.getOffice(), amountWaived,
                transactionDate, feeChargesWaived, penaltyChargesWaived, unrecognizedIncome, externalId);
        final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(waiveLoanChargeTransaction, loanCharge,
                waiveLoanChargeTransaction.getAmount(loan.getCurrency()).getAmount(), loanInstallmentNumber);
        waiveLoanChargeTransaction.getLoanChargesPaid().add(loanChargePaidBy);

        loan.addLoanTransaction(waiveLoanChargeTransaction);
        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()
                && DateUtils.isBefore(loanCharge.getDueLocalDate(), businessDate)) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }
        // Waive of charges whose due date falls after latest 'repayment' transaction don't require entire loan schedule
        // to be reprocessed.
        if (!loanCharge.isDueAtDisbursement() && loanCharge.isPaidOrPartiallyPaid(loan.getCurrency())) {
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(),
                    loan.getActiveCharges());
        }

        loanLifecycleStateMachine.determineAndTransition(loan, waiveLoanChargeTransaction.getTransactionDate());

        return waiveLoanChargeTransaction;
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
            loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loanTransaction.getLoan(), loanTransaction,
                    "reversed");
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
            loanJournalEntryPoster.postJournalEntriesForLoanTransaction(loanTransaction, false, false);
            changes.put(AMOUNT, amountWaived);
        } else {
            throw new InstallmentNotFoundException(loanTransaction.getId());
        }
    }

    private void undoSpecifiedDueDateCharge(final Map<String, Object> changes, final Loan loan, final LoanTransaction loanTransaction,
            final LoanChargePaidBy loanChargePaidBy) {
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        BigDecimal amountWaived = loanCharge.getAmountWaived(loan.getCurrency()).getAmount();
        if (!loanCharge.isWaived() || amountWaived == null) {
            throw new LoanChargeWaiveCannotBeReversedException(
                    LoanChargeWaiveCannotBeReversedException.LoanChargeWaiveCannotUndoReason.NOT_WAIVED, loanCharge.getId());
        }
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loanTransaction.getLoan(), loanTransaction, "reversed");
        loanTransaction.reverse();
        loanTransaction.setManuallyAdjustedOrReversed();
        loanCharge.setOutstandingAmount(loanCharge.amountOutstanding().add(amountWaived));
        loanCharge.setAmountWaived(null);
        loanCharge.undoWaived();
        LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallments().stream()
                .filter(e -> isPartOfThisInstallment(loanCharge, e)).findFirst().orElseThrow();
        updateRepaymentInstalmentWithWaivedAmount(loanCharge, installment, amountWaived);
        loan.updateLoanSummaryForUndoWaiveCharge(amountWaived, loanCharge.isPenaltyCharge());
        loanJournalEntryPoster.postJournalEntriesForLoanTransaction(loanTransaction, false, false);
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
}
