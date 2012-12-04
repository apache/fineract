package org.mifosplatform.portfolio.savingsdepositaccount.domain;

import java.util.List;

import org.mifosplatform.portfolio.savingsdepositaccount.exception.InvalidDepositStateTransitionException;

public class DepositLifecycleStateMachineImpl implements DepositLifecycleStateMachine {

    private final List<DepositAccountStatus> allowedDepositStatuses;

    public DepositLifecycleStateMachineImpl(List<DepositAccountStatus> allowedDepositStatuses) {
        this.allowedDepositStatuses = allowedDepositStatuses;
    }

    @Override
    public DepositAccountStatus transition(DepositAccountEvent depositEvent, DepositAccountStatus from) {

        DepositAccountStatus newState = null;

        switch (depositEvent) {
            case DEPOSIT_CREATED:
                if (from == null) {
                    newState = stateOf(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL, allowedDepositStatuses);
                }
            break;
            case DEPOSIT_REJECTED:
                if (from.hasStateOf(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(DepositAccountStatus.REJECTED, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be rejected from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("rejection", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_APPROVED:
                if (from.hasStateOf(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL)) {
                    newState = stateOf(DepositAccountStatus.ACTIVE, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be approved from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("approve", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_WITHDRAWN:
                if (this.anyOfAllowedWhenComingFrom(from, DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL, DepositAccountStatus.ACTIVE)) {
                    newState = stateOf(DepositAccountStatus.WITHDRAWN_BY_CLIENT, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be withdrawn from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("withdrawbyclient", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_APPROVAL_UNDO:
                if (from.hasStateOf(DepositAccountStatus.ACTIVE)) {
                    newState = stateOf(DepositAccountStatus.SUBMITED_AND_PENDING_APPROVAL, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be unapproved from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("unapprove", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_MATURED:
                if (from.hasStateOf(DepositAccountStatus.ACTIVE)) {
                    newState = stateOf(DepositAccountStatus.MATURED, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be matured from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("mature", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_CLOSED:
                if (from.hasStateOf(DepositAccountStatus.ACTIVE)) {
                    newState = stateOf(DepositAccountStatus.CLOSED, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be closed from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("closed", "failed", errorMessage, from);
                }
            break;
            case DEPOSIT_PRECLOSED:
                if (from.hasStateOf(DepositAccountStatus.ACTIVE)) {
                    newState = stateOf(DepositAccountStatus.PRECLOSED, allowedDepositStatuses);
                } else {
                    final String errorMessage = "The DepositApplication can not be matured from " + from.getCode();
                    throw new InvalidDepositStateTransitionException("preclosure", "failed", errorMessage, from);
                }
            break;
            default:
            break;

        }

        return newState;
    }

    private DepositAccountStatus stateOf(DepositAccountStatus state, List<DepositAccountStatus> allowedLoanStatuses) {
        DepositAccountStatus match = null;
        for (DepositAccountStatus depositStatus : allowedLoanStatuses) {
            if (depositStatus.hasStateOf(state)) {
                match = depositStatus;
                break;
            }
        }
        return match;
    }

    private boolean anyOfAllowedWhenComingFrom(final DepositAccountStatus state, final DepositAccountStatus... allowedStates) {
        boolean allowed = false;

        for (DepositAccountStatus allowedState : allowedStates) {
            if (state.hasStateOf(allowedState)) {
                allowed = true;
                break;
            }
        }

        return allowed;
    }

}
