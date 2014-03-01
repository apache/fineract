/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;

public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

    private final List<LoanStatus> allowedLoanStatuses;

    public DefaultLoanLifecycleStateMachine(final List<LoanStatus> allowedLoanStatuses) {
        this.allowedLoanStatuses = allowedLoanStatuses;
    }

    @Override
    public LoanStatus transition(final LoanEvent loanEvent, final LoanStatus from) {

        LoanStatus newState = from;

        switch (loanEvent) {
            case LOAN_CREATED:
                if (from == null) {
                    newState = stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, this.allowedLoanStatuses);
                }
            break;
            case LOAN_REJECTED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.REJECTED, this.allowedLoanStatuses);
                }
            break;
            case LOAN_APPROVED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.APPROVED, this.allowedLoanStatuses);
                }
            break;
            case LOAN_WITHDRAWN:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.WITHDRAWN_BY_CLIENT, this.allowedLoanStatuses);
                }
            break;
            case LOAN_DISBURSED:
                if (from.hasStateOf(LoanStatus.APPROVED)) {
                    newState = stateOf(LoanStatus.ACTIVE, this.allowedLoanStatuses);
                }
            break;
            case LOAN_APPROVAL_UNDO:
                if (from.hasStateOf(LoanStatus.APPROVED)) {
                    newState = stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, this.allowedLoanStatuses);
                }
            break;
            case LOAN_DISBURSAL_UNDO:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.APPROVED, this.allowedLoanStatuses);
                }
            break;
            case LOAN_CHARGE_PAYMENT:
            case LOAN_REPAYMENT_OR_WAIVER:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID)) {
                    newState = stateOf(LoanStatus.ACTIVE, this.allowedLoanStatuses);
                } else {
                    newState = from;
                }
            break;
            case REPAID_IN_FULL:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE, LoanStatus.OVERPAID)) {
                    newState = stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, this.allowedLoanStatuses);
                }
            break;
            case WRITE_OFF_OUTSTANDING:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.CLOSED_WRITTEN_OFF, this.allowedLoanStatuses);
                }
            break;
            case LOAN_RESCHEDULE:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT, this.allowedLoanStatuses);
                }
            break;
            case INTERST_REBATE_OWED:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET)) {
                    newState = stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, this.allowedLoanStatuses);
                }
            break;
            case LOAN_OVERPAYMENT:
                if (anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.OVERPAID, this.allowedLoanStatuses);
                }
            break;
            case LOAN_CLOSED:
            break;
            case WRITE_OFF_OUTSTANDING_UNDO:
            break;
            default:
            break;
        }

        return newState;
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