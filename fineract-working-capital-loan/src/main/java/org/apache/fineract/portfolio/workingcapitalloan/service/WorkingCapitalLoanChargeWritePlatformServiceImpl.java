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

package org.apache.fineract.portfolio.workingcapitalloan.service;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanChargeAdjustmentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanChargeAdjustmentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationPlan;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAllocationRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanChargePaidBy;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanChargeAdjustmentException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanChargeNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargePaidByRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeConstants;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanChargeDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeWritePlatformServiceImpl implements WorkingCapitalLoanChargeWritePlatformService {

    private final WorkingCapitalLoanChargeDataValidator loanChargeDataValidator;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    private final ChargeRepositoryWrapper chargeRepository;
    private final WorkingCapitalLoanChargeRepository loanChargeRepository;
    private final ExternalIdFactory externalIdFactory;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionRelationRepository relationRepository;
    private final PaymentDetailWritePlatformService paymentDetailService;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanTransactionProcessor transactionProcessor;
    private final WorkingCapitalLoanChargePaymentHandler chargePaymentHandler;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanChargePaidByRepository chargePaidByRepository;
    private final WorkingCapitalLoanBalanceUpdater balanceUpdater;
    private final WorkingCapitalLoanLifecycleStateMachine stateMachine;
    private final WorkingCapitalLoanAllocationRequestFactory allocationRequestFactory;
    private final WorkingCapitalLoanPaymentAllocationProcessor allocationProcessor;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final WorkingCapitalLoanChargeAccrualService chargeAccrualService;

    @Transactional
    @Override
    public CommandProcessingResult createLoanCharge(Long loanId, JsonCommand command) {
        loanChargeDataValidator.validateCreateLoanCharge(command.json());
        WorkingCapitalLoan loan = workingCapitalLoanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final LoanStatus statusBeforeCharge = loan.getLoanStatus();
        // New charges cannot be added once the loan is charged off (modify/waive/pay of existing charges stay allowed).
        if (loan.isChargedOff()) {
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.is.charged.off",
                    "Adding a charge to Working Capital Loan " + loanId + " is not allowed. The loan is charged off.", loanId);
        }

        WorkingCapitalLoanCharge loanCharge = assemblyChargeFromCommand(loan, command);

        loanCharge = loanChargeRepository.saveAndFlush(loanCharge);

        final WorkingCapitalLoanBalance balance = balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        addChargeToBalance(balance, loanCharge);

        // A specified-due-date charge added after the loan matured (closed / overpaid / active-past-maturity) creates a
        // new outstanding obligation: consume any overpayment against it, then let the resulting balance drive the
        // status (reopen / stay overpaid / close), the new maturity date and the delinquency / breach schedules.
        final LocalDate chargeDueDate = loanCharge.getDueDate();
        if (requiresChargeDrivenLifecycle(loan, statusBeforeCharge, chargeDueDate)) {
            if (statusBeforeCharge.isOverpaid()) {
                consumeOverpaymentAgainstCharges(loan, balance);
            }
            applyChargeDrivenLifecycle(loan, balance, statusBeforeCharge, chargeDueDate);
        }

        balanceRepository.saveAndFlush(balance);
        workingCapitalLoanRepository.saveAndFlush(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        if (loan.getLoanStatus() != statusBeforeCharge) {
            changes.put("status", loan.getLoanStatus());
        }

        chargeAccrualService.processOnChargeAdded(loan, loanCharge);

        notifyBalanceChanged(loan);
        notifyStatusChanged(loan, statusBeforeCharge);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanCharge.getId()) //
                .withEntityExternalId(loanCharge.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private boolean requiresChargeDrivenLifecycle(final WorkingCapitalLoan loan, final LoanStatus statusBeforeCharge,
            final LocalDate chargeDueDate) {
        if (chargeDueDate == null) {
            return false;
        }
        if (statusBeforeCharge.isClosedObligationsMet() || statusBeforeCharge.isOverpaid()) {
            return true;
        }
        if (statusBeforeCharge.isActive()) {
            final LocalDate scheduledMaturityDate = resolveScheduledMaturityDate(loan);
            return scheduledMaturityDate != null && chargeDueDate.isAfter(scheduledMaturityDate);
        }
        return false;
    }

    private LocalDate resolveScheduledMaturityDate(final WorkingCapitalLoan loan) {
        final MathContext mc = MoneyHelper.getMathContext();
        return scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .map(ProjectedAmortizationScheduleModel::scheduledMaturityDate).orElse(null);
    }

    /**
     * Settles newly-outstanding charges on an overpaid loan out of its overpayment.
     *
     * <p>
     * Relies on the invariant that the overpayment balance equals the sum of the unallocated remainders of the loan's
     * non-reversed repayments - the overpayment is not money of its own, only the part of those repayments that has not
     * been attributed yet. The plan is capped at the overpayment, so the funders always cover it; the guard below
     * enforces that rather than trusting it, because a shortfall would settle the charges only partially while the
     * balance booked the whole plan.
     */
    private void consumeOverpaymentAgainstCharges(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance) {
        final BigDecimal availableOverpayment = MathUtil.nullToZero(balance.getOverpaymentAmount());
        if (!MathUtil.isGreaterThanZero(availableOverpayment)) {
            return;
        }
        final List<WorkingCapitalLoanCharge> charges = loanChargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loan.getId());
        final LocalDate businessDate = ThreadLocalContextUtil.getBusinessDate();
        final WorkingCapitalLoanAllocationRequest request = allocationRequestFactory.build(loan, balance, charges, businessDate,
                availableOverpayment, LoanTransactionType.REPAYMENT);
        final WorkingCapitalLoanAllocationPlan plan = allocationProcessor.plan(request);

        final Map<Long, WorkingCapitalLoanCharge> chargesById = charges.stream()
                .collect(Collectors.toMap(WorkingCapitalLoanCharge::getId, Function.identity()));

        // An overpayment is not money in its own right - it is the still-unallocated remainder of earlier repayments.
        // So the charge is settled out of those repayments, taken in chronological order, which gives every settlement
        // a real transaction to attribute it to: the paid-by row points at the repayment that funded it, and that
        // repayment's allocation grows by the same amount, keeping the two consistent with each other.
        final List<OverpaymentFunder> funders = loadOverpaymentFunders(loan);
        final BigDecimal fundableTotal = funders.stream().map(OverpaymentFunder::getAvailable).reduce(BigDecimal.ZERO, BigDecimal::add);
        final List<WorkingCapitalLoanChargePaidBy> paidByRows = new ArrayList<>();
        final List<WorkingCapitalLoanTransactionAllocation> touchedAllocations = new ArrayList<>();
        int funderIndex = 0;

        BigDecimal totalUnfunded = BigDecimal.ZERO;
        for (final WorkingCapitalLoanAllocationPlan.ChargeAllocation chargeAllocation : plan.chargeAllocations()) {
            final WorkingCapitalLoanCharge charge = chargesById.get(chargeAllocation.chargeId());
            BigDecimal unfunded = chargeAllocation.amount();
            while (MathUtil.isGreaterThanZero(unfunded) && funderIndex < funders.size()) {
                final OverpaymentFunder funder = funders.get(funderIndex);
                final BigDecimal take = unfunded.min(funder.getAvailable());
                if (MathUtil.isGreaterThanZero(take)) {
                    final WorkingCapitalLoanChargePaidBy paidBy = chargePaymentHandler.applyChargePayment(funder.getTransaction(), charge,
                            take);
                    if (paidBy != null) {
                        paidByRows.add(paidBy);
                    }
                    funder.fund(take, chargeAllocation.penalty());
                    touchedAllocations.add(funder.getAllocation());
                    unfunded = unfunded.subtract(take);
                }
                if (!MathUtil.isGreaterThanZero(funder.getAvailable())) {
                    funderIndex++;
                }
            }
            totalUnfunded = totalUnfunded.add(unfunded);
        }

        // The plan is capped at the overpayment, and the overpayment is by construction the sum of the funders'
        // unallocated remainders, so the funders always cover it. Should that ever stop holding, the charges and
        // paid-by rows would record only what was funded while the balance below booked the whole plan - a silent
        // split between the two. Fail the transaction instead of committing that inconsistency.
        if (MathUtil.isGreaterThanZero(totalUnfunded)) {
            throw new IllegalStateException("Overpayment settlement for WC loan " + loan.getId() + " is short by " + totalUnfunded
                    + ": the overpayment balance of " + availableOverpayment
                    + " exceeds the unallocated repayment remainders funding it, which total " + fundableTotal);
        }

        loanChargeRepository.saveAll(charges);
        allocationRepository.saveAll(touchedAllocations);
        chargePaidByRepository.saveAll(paidByRows);

        balance.setOverpaymentAmount(BigDecimal.ZERO);
        balanceUpdater.apply(balance, plan);
    }

    private List<OverpaymentFunder> loadOverpaymentFunders(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> transactions = transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId()).stream()
                .filter(txn -> !txn.isReversed() && txn.getTypeOf().isRepaymentType()).toList();
        if (transactions.isEmpty()) {
            return List.of();
        }
        final List<OverpaymentFunder> funders = new ArrayList<>();
        for (final WorkingCapitalLoanTransaction txn : transactions) {
            final WorkingCapitalLoanTransactionAllocation allocation = txn.getAllocation();
            if (allocation == null) {
                // A repayment with no allocation row never reached the balance, so it contributed nothing to the
                // overpayment and cannot fund anything back out of it.
                continue;
            }
            if (MathUtil.isGreaterThanZero(allocation.getOverpaymentPortion())) {
                funders.add(new OverpaymentFunder(txn, allocation));
            }
        }
        return funders;
    }

    private void applyChargeDrivenLifecycle(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final LoanStatus statusBeforeCharge, final LocalDate chargeDueDate) {
        final BigDecimal overpayment = MathUtil.nullToZero(balance.getOverpaymentAmount());
        if (MathUtil.isGreaterThanZero(overpayment)) {
            // The overpayment still exceeds the charges: the loan stays overpaid, nothing becomes outstanding.
            return;
        }

        if (MathUtil.isGreaterThanZero(balance.getTotalOutstanding())) {
            // An outstanding obligation appeared: a closed / overpaid loan reopens; an active loan stays active.
            if (statusBeforeCharge.isClosedObligationsMet() || statusBeforeCharge.isOverpaid()) {
                stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REOPENED, loan);
            }
            loan.setMaturedOnDate(chargeDueDate);
            generateDelinquencyAndBreachPeriods(loan, chargeDueDate);
        } else if (statusBeforeCharge.isOverpaid()) {
            // The overpayment exactly settled the charge: the loan closes with obligations met.
            stateMachine.transition(WorkingCapitalLoanEvent.LOAN_CREDIT_BALANCE_REFUND_IN_FULL, loan);
            loan.setMaturedOnDate(chargeDueDate);
        }
    }

    private void generateDelinquencyAndBreachPeriods(final WorkingCapitalLoan loan, final LocalDate chargeDueDate) {
        if (delinquencyRangeScheduleService.hasSchedule(loan.getId())) {
            delinquencyRangeScheduleService.generateNextPeriodIfNeeded(loan, chargeDueDate);
        } else {
            delinquencyRangeScheduleService.generateInitialPeriod(loan);
        }
        if (breachScheduleService.hasSchedule(loan.getId())) {
            breachScheduleService.generateNextPeriodIfNeeded(loan, chargeDueDate);
        } else {
            breachScheduleService.generateInitialPeriod(loan);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult adjustmentForLoanCharge(final Long loanId, final Long wcLoanChargeId, final JsonCommand command) {
        loanChargeDataValidator.validateChargeAdjustmentRequest(command.json());

        final WorkingCapitalLoan loan = workingCapitalLoanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        final WorkingCapitalLoanCharge wcCharge = loanChargeRepository.findById(wcLoanChargeId)
                .orElseThrow(() -> new WorkingCapitalLoanChargeNotFoundException(wcLoanChargeId));

        if (wcCharge.getLoan() == null || !loanId.equals(wcCharge.getLoan().getId())) {
            throw new WorkingCapitalLoanChargeAdjustmentException("wc.loan.charge.adjustment.charge.not.belongs.to.loan",
                    "Working capital loan charge " + wcLoanChargeId + " does not belong to loan " + loanId);
        }

        final BigDecimal amount = command.bigDecimalValueOfParameterNamed(WorkingCapitalLoanChargeConstants.amountParamName);
        final LocalDate transactionDate = ThreadLocalContextUtil.getBusinessDate();
        final ExternalId externalId = externalIdFactory.createFromCommand(command, WorkingCapitalLoanChargeConstants.externalIdParamName);

        chargeAdjustmentEntranceValidation(loan, wcCharge, amount);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanChargeConstants.amountParamName, amount);
        changes.put(WorkingCapitalLoanChargeConstants.transactionDateParamName, transactionDate);
        changes.put(WorkingCapitalLoanChargeConstants.externalIdParamName, externalId);

        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);

        final WorkingCapitalLoanTransaction adjustmentTx = WorkingCapitalLoanTransaction.chargeAdjustment(loan, externalId, amount,
                transactionDate, paymentDetail);

        businessEventNotifierService
                .notifyPreBusinessEvent(new WorkingCapitalLoanChargeAdjustmentPreBusinessEvent(adjustmentTx, loan.getId()));

        final WorkingCapitalLoanTransactionRelation relation = WorkingCapitalLoanTransactionRelation.linkToCharge(adjustmentTx, wcCharge,
                LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT);
        adjustmentTx.getLoanTransactionRelations().add(relation);
        transactionRepository.saveAndFlush(adjustmentTx);

        final LoanStatus oldStatus = loan.getLoanStatus();
        // The processor owns the whole tail of a repayment-like transaction, journal entries included, so there is no
        // posting to do here.
        transactionProcessor.processRepaymentLikeTransaction(loan, adjustmentTx, transactionDate, amount);

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanChargeConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            noteRepository.save(WorkingCapitalLoanNote.create(loan, noteText));
            changes.put(WorkingCapitalLoanChargeConstants.noteParamName, noteText);
        }

        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanChargeAdjustmentPostBusinessEvent(adjustmentTx, loan.getId()));

        workingCapitalLoanRepository.saveAndFlush(loan);
        notifyBalanceChanged(loan);
        notifyStatusChanged(loan, oldStatus);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(wcLoanChargeId) //
                .withEntityExternalId(wcCharge.getExternalId()) //
                .withSubEntityId(adjustmentTx.getId()) //
                .withSubEntityExternalId(adjustmentTx.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void chargeAdjustmentEntranceValidation(final WorkingCapitalLoan loan, final WorkingCapitalLoanCharge wcCharge,
            final BigDecimal amount) {
        if (loan.getLoanStatus() != LoanStatus.ACTIVE && loan.getLoanStatus() != LoanStatus.CLOSED_OBLIGATIONS_MET
                && loan.getLoanStatus() != LoanStatus.OVERPAID) {
            throw new WorkingCapitalLoanChargeAdjustmentException("wc.loan.charge.adjustment.invalid.status",
                    "Adjustment is not supported for the status of " + loan.getLoanStatus());
        }

        if (!wcCharge.isActive()) {
            throw new WorkingCapitalLoanChargeAdjustmentException("wc.loan.charge.adjustment.inactive.charge",
                    "Adjustment is not supported for inactive charges");
        }

        if (amount.compareTo(wcCharge.getAmount()) > 0) {
            throw new WorkingCapitalLoanChargeAdjustmentException("wc.loan.charge.adjustment.invalid.amount",
                    "Transaction amount cannot be higher than the charge amount: " + wcCharge.getAmount());
        }

        final BigDecimal available = calculateAvailableAmountForChargeAdjustment(wcCharge);
        if (amount.compareTo(available) > 0) {
            throw new WorkingCapitalLoanChargeAdjustmentException("wc.loan.charge.adjustment.invalid.amount",
                    "Transaction amount cannot be higher than the available charge amount for adjustment: " + available);
        }

        checkClientActive(loan);
    }

    private BigDecimal calculateAvailableAmountForChargeAdjustment(final WorkingCapitalLoanCharge wcCharge) {
        final BigDecimal previouslyAdjusted = relationRepository
                .findAllByToChargeAndFromTransactionReversedAndFromTransactionTransactionType(wcCharge, false,
                        LoanTransactionType.CHARGE_ADJUSTMENT)
                .stream().map(rel -> rel.getFromTransaction().getTransactionAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return wcCharge.getAmount().subtract(previouslyAdjusted);
    }

    private void checkClientActive(final WorkingCapitalLoan loan) {
        if (loan.getClient() != null && loan.getClient().isNotActive()) {
            throw new ClientNotActiveException(loan.getClient().getId());
        }
    }

    private PaymentDetail createAndPersistPaymentDetailFromCommand(final JsonCommand command, final Map<String, Object> changes) {
        final JsonElement paymentDetailsElement = command.jsonElement(WorkingCapitalLoanConstants.paymentDetailsParamName);
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonNull()) {
            return null;
        }
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonObject()) {
            final JsonCommand paymentDetailsCommand = JsonCommand.fromExistingCommand(command, paymentDetailsElement);
            return paymentDetailService.createPaymentDetail(paymentDetailsCommand, changes);
        }
        return paymentDetailService.createPaymentDetail(command, changes);
    }

    private WorkingCapitalLoanCharge assemblyChargeFromCommand(WorkingCapitalLoan loan, JsonCommand command) {
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");
        final LocalDate dueDate = command.dateValueOfParameterNamed("dueDate");
        final Long chargeId = command.longValueOfParameterNamed("chargeId");
        final ExternalId externalId = externalIdFactory.createFromCommand(command, WorkingCapitalLoanConstants.externalIdParameterName);

        final Charge chargeDefinition = chargeRepository.findOneWithNotFoundDetection(chargeId);
        loanChargeDataValidator.validateCreateLoanChargeAgainstLoan(loan.getLoanStatus(),
                ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType()), dueDate, ThreadLocalContextUtil.getBusinessDate());
        return WorkingCapitalLoanCharge.build(loan, externalId, chargeDefinition, amount, dueDate,
                ThreadLocalContextUtil.getBusinessDate());
    }

    private void addChargeToBalance(final WorkingCapitalLoanBalance balance, final WorkingCapitalLoanCharge loanCharge) {
        if (loanCharge.isPenaltyCharge()) {
            balance.setPenalty(balance.getPenalty().add(loanCharge.getAmount()));
        } else {
            balance.setFee(balance.getFee().add(loanCharge.getAmount()));
        }
    }

    private void notifyStatusChanged(final WorkingCapitalLoan loan, final LoanStatus oldStatus) {
        if (oldStatus != loan.getLoanStatus()) {
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanStatusChangedBusinessEvent(loan));
        }
    }

    private void notifyBalanceChanged(final WorkingCapitalLoan loan) {
        businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanBalanceChangedBusinessEvent(loan));
    }
}
