package org.mifosng.platform.saving.domain;


public interface DepositLifecycleStateMachine {
	
	DepositStatus transition(DepositEvent loanEvent, DepositStatus from);

}
