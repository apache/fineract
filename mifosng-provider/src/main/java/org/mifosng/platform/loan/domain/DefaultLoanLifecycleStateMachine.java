package org.mifosng.platform.loan.domain;

import java.util.List;

public class DefaultLoanLifecycleStateMachine implements LoanLifecycleStateMachine {

	private final List<LoanStatusEnum> allowedLoanStatuses;

	public DefaultLoanLifecycleStateMachine(List<LoanStatusEnum> allowedLoanStatuses) {
		this.allowedLoanStatuses = allowedLoanStatuses;
	}

	@Override
	public LoanStatusEnum transition(LoanEvent loanEvent, LoanStatusEnum from) {

		LoanStatusEnum newState = null;

		switch (loanEvent) {
		case LOAN_CREATED:
			if (from == null) {
				newState = stateOf(LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL,
						allowedLoanStatuses);
			}
			break;
		case LOAN_REJECTED:
			if (from.hasStateOf(LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL)) {
				newState = stateOf(LoanStatusEnum.REJECTED, allowedLoanStatuses);
			}
			break;
		case LOAN_APPROVED:
			if (from.hasStateOf(LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL)) {
				newState = stateOf(LoanStatusEnum.APPROVED, allowedLoanStatuses);
			}
			break;
		case LOAN_WITHDRAWN:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL, LoanStatusEnum.APPROVED)) {
				newState = stateOf(LoanStatusEnum.WITHDRAWN_BY_CLIENT, allowedLoanStatuses);
			}
			break;
		case LOAN_DISBURSED:
			if (from.hasStateOf(LoanStatusEnum.APPROVED)) {
				newState = stateOf(LoanStatusEnum.ACTIVE, allowedLoanStatuses);
			}
			break;
		case LOAN_APPROVAL_UNDO:
			if (from.hasStateOf(LoanStatusEnum.APPROVED)) {
				newState = stateOf(LoanStatusEnum.SUBMITED_AND_PENDING_APPROVAL, allowedLoanStatuses);
			}
			break;
		case LOAN_DISBURSAL_UNDO:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.ACTIVE)) {
				newState = stateOf(LoanStatusEnum.APPROVED, allowedLoanStatuses);
			}
			break;
		case LOAN_REPAYMENT:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.ACTIVE, LoanStatusEnum.CLOSED, LoanStatusEnum.OVERPAID)) {
				newState = stateOf(LoanStatusEnum.ACTIVE, allowedLoanStatuses);
			} else {
				newState = from;
			}
			break;
		case REPAID_IN_FULL:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.ACTIVE)) {
				newState = stateOf(LoanStatusEnum.CLOSED, allowedLoanStatuses);
			}
			break;
		case LOAN_WRITE_OFF:
			if (this.anyOfAllowedWhenComingFrom(from,LoanStatusEnum.ACTIVE)) {
				newState = stateOf(LoanStatusEnum.CLOSED, allowedLoanStatuses);
			}
			break;
		case LOAN_RESCHEDULE:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.ACTIVE)) {
				newState = stateOf(LoanStatusEnum.CLOSED, allowedLoanStatuses);
			}
			break;
		case INTERST_REBATE_OWED:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.CLOSED)) {
				newState = stateOf(LoanStatusEnum.CLOSED, allowedLoanStatuses);
			}
			break;
		case LOAN_OVERPAYMENT:
			if (this.anyOfAllowedWhenComingFrom(from, LoanStatusEnum.CLOSED, LoanStatusEnum.ACTIVE)) {
				newState = stateOf(LoanStatusEnum.OVERPAID, allowedLoanStatuses);
			}
			break;
		}

		return newState;
	}

	private LoanStatusEnum stateOf(LoanStatusEnum state, List<LoanStatusEnum> allowedLoanStatuses) {
		LoanStatusEnum match = null;
		for (LoanStatusEnum loanStatus : allowedLoanStatuses) {
			if (loanStatus.hasStateOf(state)) {
				match = loanStatus;
				break;
			}
		}
		return match;
	}

	private boolean anyOfAllowedWhenComingFrom(final LoanStatusEnum state,
			final LoanStatusEnum... allowedStates) {
		boolean allowed = false;

		for (LoanStatusEnum allowedState : allowedStates) {
			if (state.hasStateOf(allowedState)) {
				allowed = true;
				break;
			}
		}

		return allowed;
	}
}