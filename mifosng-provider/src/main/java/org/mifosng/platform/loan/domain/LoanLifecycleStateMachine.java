package org.mifosng.platform.loan.domain;

public interface LoanLifecycleStateMachine {

	LoanStatus transition(LoanEvent loanEvent, LoanStatus from);

}
