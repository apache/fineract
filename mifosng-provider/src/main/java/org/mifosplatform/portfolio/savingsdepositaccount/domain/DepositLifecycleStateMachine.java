package org.mifosplatform.portfolio.savingsdepositaccount.domain;

public interface DepositLifecycleStateMachine {

    DepositAccountStatus transition(DepositAccountEvent loanEvent, DepositAccountStatus from);
}