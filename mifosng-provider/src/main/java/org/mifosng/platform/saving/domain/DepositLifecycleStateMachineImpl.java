package org.mifosng.platform.saving.domain;

import java.util.List;

import org.mifosng.platform.exceptions.InvalidDepositStateTransitionException;

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
			else{
				final String errorMessage = "The DepositApplication can not be rejected from "+from.getCode() ;
				throw new InvalidDepositStateTransitionException("rejection", "failed", errorMessage,from);
			}
			break;
		case DEPOSIT_APPROVED:
			if (from.hasStateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL)) {
				newState = stateOf(DepositStatus.APPROVED, allowedDepositStatuses);
			}
			else{
				final String errorMessage = "The DepositApplication can not be approved from "+from.getCode() ;
				throw new InvalidDepositStateTransitionException("approve", "failed", errorMessage,from);
			}
			break;
		case DEPOSIT_WITHDRAWN:
			if (this.anyOfAllowedWhenComingFrom(from, DepositStatus.SUBMITED_AND_PENDING_APPROVAL, DepositStatus.APPROVED)) {
				newState = stateOf(DepositStatus.WITHDRAWN_BY_CLIENT, allowedDepositStatuses);
			}
			else{
				final String errorMessage = "The DepositApplication can not be withdrawn from "+from.getCode() ;
				throw new InvalidDepositStateTransitionException("withdrawbyclient", "failed", errorMessage,from);
			}
			break;
		case DEPOSIT_APPROVAL_UNDO:
			if (from.hasStateOf(DepositStatus.APPROVED)) {
				newState = stateOf(DepositStatus.SUBMITED_AND_PENDING_APPROVAL, allowedDepositStatuses);
			}
			else{
				final String errorMessage = "The DepositApplication can not be unapproved from "+from.getCode() ;
				throw new InvalidDepositStateTransitionException("unapprove", "failed", errorMessage,from);
			}
			break;
		case DEPOSIT_MATURED:
			if (from.hasStateOf(DepositStatus.APPROVED) ){
				newState = stateOf(DepositStatus.MATURED, allowedDepositStatuses);
			}
			else{
				final String errorMessage = "The DepositApplication can not be matured from "+from.getCode() ;
				throw new InvalidDepositStateTransitionException("mature", "failed", errorMessage,from);
			}
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
