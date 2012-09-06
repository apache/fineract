package org.mifosng.platform.saving.domain;


public interface DepositLifecycleStateMachine {
	
	DepositAccountStatus transition(DepositAccountEvent loanEvent, DepositAccountStatus from);

}
