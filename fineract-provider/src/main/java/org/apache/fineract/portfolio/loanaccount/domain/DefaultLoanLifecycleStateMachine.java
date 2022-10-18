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

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;

// TODO: introduce tests for the state machine
@RequiredArgsConstructor
public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

    private final List<LoanStatus> allowedLoanStatuses;
    private final BusinessEventNotifierService businessEventNotifierService;

    public DefaultLoanLifecycleStateMachine(LoanStatus[] allowedLoanStatuses, BusinessEventNotifierService businessEventNotifierService) {
        this(Arrays.asList(allowedLoanStatuses), businessEventNotifierService);
    }

    @Override
    public LoanStatus dryTransition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus nextStatus = getNextState(loanEvent, loan);
        return nextStatus != null ? nextStatus : loan.getStatus();
    }

    @Override
    public void transition(final LoanEvent loanEvent, final Loan loan) {
        LoanStatus newState = getNextState(loanEvent, loan);
        if (newState != null) {
            loan.setLoanStatus(newState.getValue());

            if (isNotLoanCreation(loanEvent)) {
                // in case of Loan creation, a LoanCreatedBusinessEvent is also raised, no need to send a status change
                businessEventNotifierService.notifyPostBusinessEvent(new LoanStatusChangedBusinessEvent(loan));
            }
        }
    }

    private boolean isNotLoanCreation(LoanEvent loanEvent) {
        return !LoanEvent.LOAN_CREATED.equals(loanEvent);
    }

    private LoanStatus getNextState(LoanEvent loanEvent, Loan loan) {
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
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.APPROVED, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID)) {
                    newState = activeTransition();
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
                newState = transferOnHold();
            break;
            case LOAN_WITHDRAW_TRANSFER:
                newState = activeTransition();
            break;
            case WRITE_OFF_OUTSTANDING_UNDO:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_WRITTEN_OFF)) {
                    newState = activeTransition();
                }
            break;
            case LOAN_CREDIT_BALANCE_REFUND:
                newState = closeObligationsMetTransition();
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
            default:
            break;
        }
        return newState;
    }

    private LoanStatus transferOnHold() {
        return stateOf(LoanStatus.TRANSFER_ON_HOLD, this.allowedLoanStatuses);
    }

    private LoanStatus transferInProgress() {
        return stateOf(LoanStatus.TRANSFER_IN_PROGRESS, this.allowedLoanStatuses);
    }

    private LoanStatus overpaidTransition() {
        return stateOf(LoanStatus.OVERPAID, this.allowedLoanStatuses);
    }

    private LoanStatus closedRescheduleOutstandingAmountTransition() {
        return stateOf(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT, this.allowedLoanStatuses);
    }

    private LoanStatus closedWrittenOffTransition() {
        return stateOf(LoanStatus.CLOSED_WRITTEN_OFF, this.allowedLoanStatuses);
    }

    private LoanStatus closeObligationsMetTransition() {
        return stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, this.allowedLoanStatuses);
    }

    private LoanStatus activeTransition() {
        return stateOf(LoanStatus.ACTIVE, this.allowedLoanStatuses);
    }

    private LoanStatus withdrawnByClientTransition() {
        return stateOf(LoanStatus.WITHDRAWN_BY_CLIENT, this.allowedLoanStatuses);
    }

    private LoanStatus approvedTransition() {
        return stateOf(LoanStatus.APPROVED, this.allowedLoanStatuses);
    }

    private LoanStatus rejectedTransition() {
        return stateOf(LoanStatus.REJECTED, this.allowedLoanStatuses);
    }

    private LoanStatus submittedTransition() {
        return stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, this.allowedLoanStatuses);
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
