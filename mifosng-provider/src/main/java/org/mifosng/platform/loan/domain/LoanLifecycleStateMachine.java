package org.mifosng.platform.loan.domain;

public interface LoanLifecycleStateMachine {

	LoanStatusEnum transition(LoanEvent loanEvent, LoanStatusEnum from);

}
