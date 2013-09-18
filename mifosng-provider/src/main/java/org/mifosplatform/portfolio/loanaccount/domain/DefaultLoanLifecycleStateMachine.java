/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;

public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

    private final List<LoanStatus> allowedLoanStatuses;

    public DefaultLoanLifecycleStateMachine(List<LoanStatus> allowedLoanStatuses) {
        this.allowedLoanStatuses = allowedLoanStatuses;
    }

    @Override
    public LoanStatus transition(LoanEvent loanEvent, LoanStatus from) {

        LoanStatus newState = from;

        switch (loanEvent) {
            case LOAN_CREATED:
                if (from == null) {
                    newState = stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, allowedLoanStatuses);
                }
            break;
            case LOAN_REJECTED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.REJECTED, allowedLoanStatuses);
                }
            break;
            case LOAN_APPROVED:
                if (from.hasStateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.APPROVED, allowedLoanStatuses);
                }
            break;
            case LOAN_WITHDRAWN:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(LoanStatus.WITHDRAWN_BY_CLIENT, allowedLoanStatuses);
                }
            break;
            case LOAN_DISBURSED:
                if (from.hasStateOf(LoanStatus.APPROVED)) {
                    newState = stateOf(LoanStatus.ACTIVE, allowedLoanStatuses);
                }
            break;
            case LOAN_APPROVAL_UNDO:
                if (from.hasStateOf(LoanStatus.APPROVED)) {
                    newState = stateOf(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, allowedLoanStatuses);
                }
            break;
            case LOAN_DISBURSAL_UNDO:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.APPROVED, allowedLoanStatuses);
                }
            break;
            case LOAN_CHARGE_PAYMENT:
            case LOAN_REPAYMENT_OR_WAIVER:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID)) {
                    newState = stateOf(LoanStatus.ACTIVE, allowedLoanStatuses);
                } else {
                    newState = from;
                }
            break;
            case REPAID_IN_FULL:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE, LoanStatus.OVERPAID)) {
                    newState = stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, allowedLoanStatuses);
                }
            break;
            case WRITE_OFF_OUTSTANDING:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.CLOSED_WRITTEN_OFF, allowedLoanStatuses);
                }
            break;
            case LOAN_RESCHEDULE:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT, allowedLoanStatuses);
                }
            break;
            case INTERST_REBATE_OWED:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET)) {
                    newState = stateOf(LoanStatus.CLOSED_OBLIGATIONS_MET, allowedLoanStatuses);
                }
            break;
            case LOAN_OVERPAYMENT:
                if (this.anyOfAllowedWhenComingFrom(from, LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.ACTIVE)) {
                    newState = stateOf(LoanStatus.OVERPAID, allowedLoanStatuses);
                }
            break;
        }

        return newState;
    }

    private LoanStatus stateOf(LoanStatus state, List<LoanStatus> allowedLoanStatuses) {
        LoanStatus match = null;
        for (LoanStatus loanStatus : allowedLoanStatuses) {
            if (loanStatus.hasStateOf(state)) {
                match = loanStatus;
                break;
            }
        }
        return match;
    }

    private boolean anyOfAllowedWhenComingFrom(final LoanStatus state, final LoanStatus... allowedStates) {
        boolean allowed = false;

        for (LoanStatus allowedState : allowedStates) {
            if (state.hasStateOf(allowedState)) {
                allowed = true;
                break;
            }
        }

        return allowed;
    }
}