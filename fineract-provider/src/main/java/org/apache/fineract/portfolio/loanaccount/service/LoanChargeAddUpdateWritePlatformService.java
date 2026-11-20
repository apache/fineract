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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTrancheDisbursementCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds loan charges, and updates or deletes them while the loan is pending approval.
 */
@RequiredArgsConstructor
public class LoanChargeAddUpdateWritePlatformService {

    private final LoanChargeApiJsonValidator loanChargeApiJsonValidator;
    private final LoanAssembler loanAssembler;
    private final ChargeRepositoryWrapper chargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanWritePlatformService loanWritePlatformService;
    private final LoanUtilService loanUtilService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final ExternalIdFactory externalIdFactory;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanScheduleService loanScheduleService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanChargeService loanChargeService;
    private final LoanChargeWriteHelper loanChargeWriteHelper;

    @Transactional
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {
        this.loanChargeApiJsonValidator.validateAddLoanCharge(command.json());

        Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.is.charged.off",
                    "Adding charge to Loan: " + loanId + " is not allowed. Loan Account is Charged-off", loanId);
        }
        List<LoanDisbursementDetails> loanDisburseDetails = loan.getDisbursementDetails();
        final Long chargeDefinitionId = command.longValueOfParameterNamed("chargeId");
        final Charge chargeDefinition = this.chargeRepository.findOneWithNotFoundDetection(chargeDefinitionId);

        if (loan.isDisbursed() && chargeDefinition.isDisbursementCharge()) {
            // validates whether any pending disbursements are available to
            // apply this charge
            validateAddingNewChargeAllowed(loanDisburseDetails);
        }

        boolean isAppliedOnBackDate = false;
        LoanCharge loanCharge = null;
        final List<LoanCharge> addedCharges = new ArrayList<>();
        LocalDate recalculateFrom = loan.fetchInterestRecalculateFromDate();
        LocalDate transactionDate = null;
        if (chargeDefinition.isPercentageOfDisbursementAmount()) {
            LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;
            ExternalId externalId = externalIdFactory.createFromCommand(command, "externalId");
            boolean isFirst = true;
            for (LoanDisbursementDetails disbursementDetail : loanDisburseDetails) {
                if (disbursementDetail.actualDisbursementDate() == null) {
                    // If multiple charges to be applied, only the first one will get the provided externalId, for the
                    // rest we generate new ones (if needed)
                    if (!isFirst) {
                        externalId = externalIdFactory.create();
                    }
                    LocalDate dueDate = disbursementDetail.expectedDisbursementDateAsLocalDate();
                    loanCharge = loanChargeAssembler.createNewWithoutLoan(chargeDefinition, disbursementDetail.getPrincipal(), null, null,
                            null, dueDate, null, null, externalId);
                    loanTrancheDisbursementCharge = new LoanTrancheDisbursementCharge(loanCharge, disbursementDetail);
                    loanCharge.updateLoanTrancheDisbursementCharge(loanTrancheDisbursementCharge);
                    businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
                    validateAddLoanCharge(loan, chargeDefinition, loanCharge);
                    this.loanChargeWriteHelper.addCharge(loan, chargeDefinition, loanCharge);
                    addedCharges.add(loanCharge);
                    isAppliedOnBackDate = true;
                    if (DateUtils.isAfter(recalculateFrom, dueDate)) {
                        recalculateFrom = dueDate;
                    }
                    if (isFirst) {
                        transactionDate = loanCharge.getEffectiveDueDate();
                    }
                    isFirst = false;
                }
            }
            if (loanCharge == null) {
                final String errorMessage = "Charge with identifier " + chargeDefinition.getId()
                        + " cannot be applied: No valid loan disbursement available";
                throw new ChargeCannotBeAppliedToException("loan", errorMessage, chargeDefinition.getId());
            }
            loan.addTrancheLoanCharge(chargeDefinition);
        } else {
            loanCharge = loanChargeAssembler.createNewFromJson(loan, chargeDefinition, command);
            businessEventNotifierService.notifyPreBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));

            validateAddLoanCharge(loan, chargeDefinition, loanCharge);
            isAppliedOnBackDate = this.loanChargeWriteHelper.addCharge(loan, chargeDefinition, loanCharge);
            addedCharges.add(loanCharge);
            if (DateUtils.isAfter(recalculateFrom, loanCharge.getDueLocalDate())) {
                isAppliedOnBackDate = true;
                recalculateFrom = loanCharge.getDueLocalDate();
            }
            transactionDate = loanCharge.getEffectiveDueDate();
        }

        boolean reprocessRequired = true;
        // overpaid transactions will be reprocessed and pay this charge
        boolean overpaidReprocess = !loanCharge.isDueAtDisbursement() && !loanCharge.isPaid() && loan.getStatus().isOverpaid();
        if (!overpaidReprocess && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            if (isAppliedOnBackDate && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
                loan = this.loanChargeWriteHelper.runScheduleRecalculation(loan, recalculateFrom);
                reprocessRequired = false;
            }
            this.loanWritePlatformService.updateOriginalSchedule(loan);
        }
        if (!overpaidReprocess && AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY
                .equals(loan.transactionProcessingStrategy())) {
            // [For Adv payment allocation strategy] check if charge due date is earlier than last transaction
            // date, if yes trigger reprocess else no reprocessing
            final Optional<LocalDate> lastPaymentTransactionDate = loanTransactionRepository.findLastTransactionDateForReprocessing(loan);
            if (lastPaymentTransactionDate.isPresent()
                    && DateUtils.isAfter(loanCharge.getEffectiveDueDate(), lastPaymentTransactionDate.get())) {
                reprocessRequired = false;
            }
        }
        // Ensure reverse-replay so an existing auto down payment transaction can cover a newly added DP fee
        if (loanCharge.isDueAtDownPayment() && loan.isAutoRepaymentForDownPaymentEnabled()) {
            reprocessRequired = true;
        }

        if (reprocessRequired) {
            if (loan.isProgressiveSchedule()) {
                final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
                loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
            }
            reprocessLoanTransactionsService.reprocessTransactions(loan);
            loanLifecycleStateMachine.determineAndTransition(loan, transactionDate);
        } else {
            // reprocess skipped: bring stored installment interest up to the business date
            this.loanChargeWriteHelper.updateProgressiveInterestModel(loan);
        }
        loanAccrualsProcessingService.processRealTimeChargeAccruals(loan, addedCharges);

        if (loan.isInterestBearingAndInterestRecalculationEnabled() && isAppliedOnBackDate
                && loan.isFeeCompoundingEnabledForInterestRecalculation()) {
            loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, true, true);
        }
        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(loanCharge));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    @Transactional
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        this.loanChargeApiJsonValidator.validateUpdateOfLoanCharge(command.json());

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be edited only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeUpdatedException(
                    LoanChargeCannotBeUpdatedException.LoanChargeCannotBeUpdatedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));

        loanChargeValidator.validateLoanIsNotClosed(loan, loanCharge);

        final Map<String, Object> changes = new LinkedHashMap<>(3);
        if (loan.getActiveCharges().contains(loanCharge)) {
            final BigDecimal amount = loanChargeService.calculateAmountPercentageAppliedTo(loan, loanCharge);
            final Map<String, Object> loanChargeChanges = loanChargeService.update(command, amount, loanCharge);
            changes.putAll(loanChargeChanges);
            loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(loan.deriveSumTotalOfChargesDueAtDisbursement());
        }

        if (!loanCharge.isDueAtDisbursement()) {
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            // reprocess loan schedule based on charge been waived.
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(loan.getCurrency(), loan.getDisbursementDate(), loan.getRepaymentScheduleInstallments(),
                    loan.getActiveCharges());
        }

        this.loanRepositoryWrapper.save(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {

        final Loan loan = this.loanAssembler.assembleFromInUserScope(loanId);
        this.loanChargeWriteHelper.checkClientOrGroupActive(loan);
        final LoanCharge loanCharge = this.loanChargeWriteHelper.retrieveLoanChargeBy(loanId, loanChargeId);

        // Charges may be deleted only when the loan associated with them are
        // yet to be approved (are in submitted and pending status)
        if (!loan.getStatus().isSubmittedAndPendingApproval()) {
            throw new LoanChargeCannotBeDeletedException(
                    LoanChargeCannotBeDeletedException.LoanChargeCannotBeDeletedReason.LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE,
                    loanCharge.getId());
        }
        businessEventNotifierService.notifyPreBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));

        loanChargeValidator.validateLoanIsNotClosed(loan, loanCharge);
        loanChargeValidator.validateLoanChargeIsNotWaived(loan, loanCharge);
        loanChargeService.removeLoanCharge(loan, loanCharge);
        this.loanRepositoryWrapper.save(loan);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanDeleteChargeBusinessEvent(loanCharge));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanChargeId) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
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
        } else if (chargeDefinition.isDownPaymentCharge()) {
            final LocalDate dueDate = loanCharge.getDueLocalDate();
            final boolean hasMatchingDownPaymentPeriod = loan.getRepaymentScheduleInstallments().stream()
                    .anyMatch(installment -> installment.isDownPayment() && DateUtils.isEqual(dueDate, installment.getDueDate()));
            if (!hasMatchingDownPaymentPeriod) {
                final String defaultUserMessage = "Down payment charge due date must match an existing down payment installment.";
                throw new LoanChargeCannotBeAddedException("loanCharge", "down.payment.due.date.no.matching.period", defaultUserMessage,
                        dueDate, chargeDefinition.getName());
            }
        } else if (loanCharge.getDueLocalDate() != null) {
            // TODO: Review, error message seems not valid if interest recalculation is not enabled.
            boolean isCumulative = loan.getLoanRepaymentScheduleDetail().getLoanScheduleType().equals(LoanScheduleType.CUMULATIVE);
            LocalDate validationDate = loan.isInterestBearingAndInterestRecalculationEnabled() && isCumulative
                    ? loan.getLastUserTransactionDate()
                    : loan.getDisbursementDate();
            if (DateUtils.isBefore(loanCharge.getDueLocalDate(), validationDate)) {
                final String defaultUserMessage = "charge with date before last transaction date can not be added to loan.";
                throw new LoanChargeCannotBeAddedException("loanCharge", "date.is.before.last.transaction.date", defaultUserMessage, null,
                        chargeDefinition.getName());
            }
        } else if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final Set<LoanCharge> loanCharges = new HashSet<>(1);
            loanCharges.add(loanCharge);
            this.loanChargeApiJsonValidator.validateLoanCharges(loanCharges, dataValidationErrors);
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
    }
}
