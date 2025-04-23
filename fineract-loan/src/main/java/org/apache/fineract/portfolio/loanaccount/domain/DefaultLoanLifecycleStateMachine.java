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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.springframework.stereotype.Component;

// TODO: introduce tests for the state machine
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

    private static final List<LoanStatus> ALLOWED_LOAN_STATUSES = List.of(LoanStatus.values());
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanBalanceService loanBalanceService;

    @Override
    public LoanStatus dryTransition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus newStatus = getNextStatus(loanEvent, loan);
        return newStatus != null ? newStatus : loan.getStatus();
    }

    @Override
    public void transition(final LoanEvent loanEvent, final Loan loan) {
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        internalTransition(loanEvent, loan);
    }

    @Override
    public void determineAndTransition(final Loan loan, final LocalDate transactionDate) {
        if (loan.getStatus() == null) {
            return;
        }

        loanBalanceService.updateLoanSummaryDerivedFields(loan);

        final LoanStatusTransition transition = determineTransition(loan, loan.getStatus(), transactionDate);

        if (MathUtil.isEmpty(loan.getTotalOverpaid())) {
            loan.setOverpaidOnDate(null);
        }

        if (transition.transitionNeeded()) {
            internalTransition(transition.event(), loan);
        }
    }

    private void internalTransition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus oldStatus = loan.getStatus();
        LoanStatus newStatus = getNextStatus(loanEvent, loan);
        if (newStatus != null) {
            loan.setLoanStatus(newStatus);

            if (isNotLoanCreation(loanEvent)) {
                // in case of Loan creation, a LoanCreatedBusinessEvent is also raised, no need to send a status change
                businessEventNotifierService.notifyPostBusinessEvent(new LoanStatusChangedBusinessEvent(loan, oldStatus));
            }

            // set mandatory field states based on new status after the transition
            log.debug("Transitioning loan {} status from {} to {}", loan.getId(), oldStatus, newStatus);
            switch (newStatus) {
                case SUBMITTED_AND_PENDING_APPROVAL -> {
                    loan.setApprovedOnDate(null);
                    loan.setApprovedBy(null);
                }
                case APPROVED -> {
                    loan.setDisbursedBy(null);
                    loan.setActualDisbursementDate(null);
                }
                case ACTIVE -> {
                    loan.setClosedBy(null);
                    loan.setClosedOnDate(null);
                    loan.setOverpaidOnDate(null);
                }
                default -> { // no fields need to get cleared
                }
            }
        }
    }

    private boolean isNotLoanCreation(LoanEvent loanEvent) {
        return !LoanEvent.LOAN_CREATED.equals(loanEvent);
    }

    private LoanStatus getNextStatus(LoanEvent loanEvent, Loan loan) {
        LoanStatus from = loan.getStatus();
        if (loanEvent.equals(LoanEvent.LOAN_CREATED) && from == null) {
            return submittedTransition();
        }

        LoanStatus newState = null;

        switch (loanEvent) {
            case LOAN_REJECTED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = rejectedTransition();
                }
            break;
            case LOAN_APPROVED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = approvedTransition();
                }
            break;
            case LOAN_WITHDRAWN:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = withdrawnByClientTransition();
                }
            break;
            case LOAN_DISBURSED:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.APPROVED, LoanStatus.CLOSED_OBLIGATIONS_MET)) {
                    newState = activeTransition();
                } else if (from.isOverpaid() && loan.getTotalOverpaidAsMoney().isZero()) {
                    if (loan.getSummary().getTotalOutstanding(loan.getCurrency()).isZero()) {
                        newState = closeObligationsMetTransition();
                    } else {
                        newState = activeTransition();
                    }
                }
            break;
            case LOAN_APPROVAL_UNDO:
                if (from.hasStateOf(LoanStatus.APPROVED)) {
                    newState = submittedTransition();
                }
            break;
            case LOAN_DISBURSAL_UNDO:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = approvedTransition();
                }
            break;
            case LOAN_CHARGE_PAYMENT:
            case LOAN_REPAYMENT_OR_WAIVER:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID)) {
                    newState = activeTransition();
                }
            break;
            case REPAID_IN_FULL:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE, LoanStatus.OVERPAID)) {
                    newState = closeObligationsMetTransition();
                }
            break;
            case WRITE_OFF_OUTSTANDING:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = closedWrittenOffTransition();
                }
            break;
            case LOAN_RESCHEDULE:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = closedRescheduleOutstandingAmountTransition();
                }
            break;
            case LOAN_OVERPAYMENT:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.ACTIVE)) {
                    newState = overpaidTransition();
                }
            break;
            case LOAN_ADJUST_TRANSACTION:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.CLOSED_WRITTEN_OFF,
                        LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT)) {
                    boolean isOverpaid = loan.getTotalOverpaid() != null && loan.getTotalOverpaid().compareTo(BigDecimal.ZERO) > 0;
                    if (isOverpaid) {
                        newState = overpaidTransition();
                    } else {
                        newState = activeTransition();
                    }
                }
            break;
            case LOAN_INITIATE_TRANSFER:
                newState = transferInProgress();
            break;
            case LOAN_REJECT_TRANSFER:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.TRANSFER_IN_PROGRESS)) {
                    newState = transferOnHold();
                }
            break;
            case LOAN_WITHDRAW_TRANSFER:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.TRANSFER_IN_PROGRESS)) {
                    newState = activeTransition();
                }
            break;
            case WRITE_OFF_OUTSTANDING_UNDO:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_WRITTEN_OFF)) {
                    newState = activeTransition();
                }
            break;
            case LOAN_CREDIT_BALANCE_REFUND:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.OVERPAID)) {
                    newState = closeObligationsMetTransition();
                }
            break;
            case LOAN_CHARGE_ADDED:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET)) {
                    newState = activeTransition();
                }
            break;
            case LOAN_CHARGEBACK:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID)) {
                    newState = activeTransition();
                }
            break;
            case LOAN_CHARGE_ADJUSTMENT:
                if (from.hasStateOf(LoanStatus.CLOSED_OBLIGATIONS_MET)) {
                    newState = overpaidTransition();
                }
            break;
            default:
            break;
        }
        return newState;
    }

    private LoanStatus transferOnHold() {
        return stateOf(LoanStatus.TRANSFER_ON_HOLD, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus transferInProgress() {
        return stateOf(LoanStatus.TRANSFER_IN_PROGRESS, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus overpaidTransition() {
        return stateOf(LoanStatus.OVERPAID, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus closedRescheduleOutstandingAmountTransition() {
        return stateOf(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus closedWrittenOffTransition() {
        return stateOf(LoanStatus.CLOSED_WRITTEN_OFF, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus closeObligationsMetTransition() {
        return stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus activeTransition() {
        return stateOf(LoanStatus.ACTIVE, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus withdrawnByClientTransition() {
        return stateOf(LoanStatus.WITHDRAWN_BY_CLIENT, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus approvedTransition() {
        return stateOf(LoanStatus.APPROVED, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus rejectedTransition() {
        return stateOf(LoanStatus.REJECTED, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus submittedTransition() {
        return stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, ALLOWED_LOAN_STATUSES);
    }

    private LoanStatus stateOf(final LoanStatus state, final List<LoanStatus> allowedLoanStatuses) {
        LoanStatus match = null;
        for (final LoanStatus loanStatus : allowedLoanStatuses) {
            if (loanStatus.hasStateOf(state)) {
                match = loanStatus;
                break;
            }
        }
        return match;
    }

    private boolean anyOfAllowedWhenComingFrom(final LoanStatus state, final LoanStatus... allowedStates) {
        boolean allowed = false;

        for (final LoanStatus allowedState : allowedStates) {
            if (state.hasStateOf(allowedState)) {
                allowed = true;
                break;
            }
        }

        return allowed;
    }

    private LoanStatusTransition determineTransition(final Loan loan, final LoanStatus currentStatus, final LocalDate transactionDate) {
        final boolean hasOutstanding = loan.getSummary().getTotalOutstanding(loan.getCurrency()).isGreaterThanZero();
        final boolean isRepaidInFull = loan.getSummary().isRepaidInFull(loan.getCurrency());
        final boolean isOverpaid = MathUtil.isGreaterThanZero(loan.getTotalOverpaid());
        final boolean isAllChargesPaid = loan.getLoanCharges().stream().allMatch(
                charge -> !charge.isActive() || charge.amount().compareTo(BigDecimal.ZERO) <= 0 || charge.isPaid() || charge.isWaived());

        if (currentStatus.isOverpaid()) {
            return determineTransitionFromOverpaid(loan, isOverpaid, isRepaidInFull, isAllChargesPaid, hasOutstanding, transactionDate);
        } else if (currentStatus.isClosedObligationsMet()) {
            return determineTransitionFromClosedObligationsMet(loan, isOverpaid, hasOutstanding, transactionDate);
        } else if (currentStatus.isActive()) {
            return determineTransitionFromActive(loan, isOverpaid, isRepaidInFull, isAllChargesPaid, transactionDate);
        } else if (currentStatus.isClosedWrittenOff() || currentStatus.isClosedWithOutsandingAmountMarkedForReschedule()) {
            return determineTransitionFromClosedWrittenOffOrRescheduled(loan, hasOutstanding);
        }

        return LoanStatusTransition.noTransition(currentStatus);
    }

    private LoanStatusTransition determineTransitionFromOverpaid(final Loan loan, final boolean isOverpaid, final boolean isRepaidInFull,
            final boolean isAllChargesPaid, final boolean hasOutstanding, final LocalDate transactionDate) {
        if (!isOverpaid) {
            if (isRepaidInFull && isAllChargesPaid) {
                loan.setClosedOnDate(transactionDate);
                loan.setActualMaturityDate(transactionDate);
                return LoanStatusTransition.to(LoanStatus.CLOSED_OBLIGATIONS_MET, LoanEvent.LOAN_CREDIT_BALANCE_REFUND);
            } else if (hasOutstanding) {
                loan.handleMaturityDateActivate();
                return LoanStatusTransition.to(LoanStatus.ACTIVE, LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
            }
        }
        return LoanStatusTransition.noTransition(LoanStatus.OVERPAID);
    }

    private LoanStatusTransition determineTransitionFromClosedObligationsMet(final Loan loan, final boolean isOverpaid,
            final boolean hasOutstanding, LocalDate transactionDate) {
        if (isOverpaid) {
            loan.setOverpaidOnDate(transactionDate);
            loan.setClosedOnDate(null);
            loan.setActualMaturityDate(null);
            return LoanStatusTransition.to(LoanStatus.OVERPAID, LoanEvent.LOAN_OVERPAYMENT);
        } else if (hasOutstanding) {
            loan.setClosedOnDate(null);
            loan.setActualMaturityDate(null);
            loan.handleMaturityDateActivate();
            return LoanStatusTransition.to(LoanStatus.ACTIVE, LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
        }
        return LoanStatusTransition.noTransition(LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    private LoanStatusTransition determineTransitionFromActive(final Loan loan, final boolean isOverpaid, final boolean isRepaidInFull,
            final boolean isAllChargesPaid, final LocalDate transactionDate) {
        if (isOverpaid) {
            loan.setOverpaidOnDate(transactionDate);
            loan.setActualMaturityDate(null);
            return LoanStatusTransition.to(LoanStatus.OVERPAID, LoanEvent.LOAN_OVERPAYMENT);
        } else if (isRepaidInFull && isAllChargesPaid) {
            loan.setClosedOnDate(transactionDate);
            loan.setActualMaturityDate(transactionDate);
            return LoanStatusTransition.to(LoanStatus.CLOSED_OBLIGATIONS_MET, LoanEvent.REPAID_IN_FULL);
        }
        return LoanStatusTransition.noTransition(LoanStatus.ACTIVE);
    }

    private LoanStatusTransition determineTransitionFromClosedWrittenOffOrRescheduled(final Loan loan, final boolean hasOutstanding) {
        if (hasOutstanding) {
            return LoanStatusTransition.to(LoanStatus.ACTIVE, LoanEvent.LOAN_ADJUST_TRANSACTION);
        }
        return LoanStatusTransition.noTransition(loan.getStatus());
    }

    private record LoanStatusTransition(LoanStatus targetStatus, LoanEvent event, boolean transitionNeeded) {

        public static LoanStatusTransition to(final LoanStatus targetStatus, final LoanEvent event) {
            return new LoanStatusTransition(targetStatus, event, true);
        }

        public static LoanStatusTransition noTransition(final LoanStatus currentStatus) {
            return new LoanStatusTransition(currentStatus, null, false);
        }

    }

}
