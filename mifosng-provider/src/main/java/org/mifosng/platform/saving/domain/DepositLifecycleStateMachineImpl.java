package org.mifosng.platform.saving.domain;

import java.util.List;

public class DepositLifecycleStateMachineImpl implements
		DepositLifecycleStateMachine {
	
	private final List<DepositStatus> allowedDepositStatuses;
	
	public DepositLifecycleStateMachineImpl(List<DepositStatus> allowedDepositStatuses) {
		this.allowedDepositStatuses=allowedDepositStatuses;
	}

	@Override
	public DepositStatus transition(DepositEvent depositEvent, DepositStatus from) {
		
		DepositStatus newState = null;
		
		switch (depositEvent) {
		case DEPOSIT_CREATED:
			if (from == null) {
				newState = stateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL, allowedDepositStatuses);
			}
			break;
		case DEPOSIT_REJECTED:
			if (from.hasStateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL)) {
				newState = stateOf(DepositStatus.REJECTED, allowedDepositStatuses);
			}
			break;
		case DEPOSIT_APPROVED:
			if (from.hasStateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL)) {
				newState = stateOf(DepositStatus.APPROVED, allowedDepositStatuses);
			}
			break;
		case DEPOSIT_WITHDRAWN:
			if (this.anyOfAllowedWhenComingFrom(from, DepositStatus.SUBMITED_AND_PENDING_APPROVAL, DepositStatus.APPROVED)) {
				newState = stateOf(DepositStatus.WITHDRAWN_BY_CLIENT, allowedDepositStatuses);
			}
			break;
		case DEPOSIT_APPROVAL_UNDO:
			if (from.hasStateOf(DepositStatus.APPROVED)) {
				newState = stateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL, allowedDepositStatuses);
			}
			break;
		case DEPOSIT_MATURED:
			if (from.hasStateOf(DepositStatus.APPROVED) ){
				newState = stateOf(DepositStatus.MATURED, allowedDepositStatuses);
			}
			break;
		default:
			break;
		}
		
		return newState;
	}
	
	private DepositStatus stateOf(DepositStatus state, List<DepositStatus> allowedLoanStatuses) {
		DepositStatus match = null;
		for (DepositStatus depositStatus : allowedLoanStatuses) {
			if (depositStatus.hasStateOf(state)) {
				match = depositStatus;
				break;
			}
		}
		return match;
	}

	private boolean anyOfAllowedWhenComingFrom(final DepositStatus state, final DepositStatus... allowedStates) {
		boolean allowed = false;

		for (DepositStatus allowedState : allowedStates) {
			if (state.hasStateOf(allowedState)) {
				allowed = true;
				break;
			}
		}

		return allowed;
	}

}
