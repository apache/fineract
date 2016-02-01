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