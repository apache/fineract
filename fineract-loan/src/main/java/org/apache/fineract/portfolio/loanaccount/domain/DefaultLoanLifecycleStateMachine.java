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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.stereotype.Component;

// TODO: introduce tests for the state machine
@Component
@RequiredArgsConstructor
public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

    private static final List<LoanStatus> ALLOWED_LOAN_STATUSES = List.of(LoanStatus.values());
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public LoanStatus dryTransition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus newStatus = getNextStatus(loanEvent, loan);
        return newStatus != null ? newStatus : loan.getStatus();
    }

    @Override
    public void transition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus newStatus = getNextStatus(loanEvent, loan);
        if (newStatus != null) {
            Integer newPlainStatus = newStatus.getValue();
            loan.setLoanStatus(newPlainStatus);

            if (isNotLoanCreation(loanEvent)) {
                // in case of Loan creation, a LoanCreatedBusinessEvent is also raised, no need to send a status change
                businessEventNotifierService.notifyPostBusinessEvent(new LoanStatusChangedBusinessEvent(loan));
            }
        }
    }

    private boolean isNotLoanCreation(LoanEvent loanEvent) {
        return !LoanEvent.LOAN_CREATED.equals(loanEvent);
    }

    private LoanStatus getNextStatus(LoanEvent loanEvent, Loan loan) {
        Integer plainFrom = loan.getPlainStatus();
        if (loanEvent.equals(LoanEvent.LOAN_CREATED) && plainFrom == null) {
            return submittedTransition();
        }

        LoanStatus from = loan.getStatus();
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
                    if (loan.getLoanSummary().getTotalOutstanding(loan.getCurrency()).isZero()) {
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
                    newState = activeTransition();
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
}
