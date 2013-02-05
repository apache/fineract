package org.mifosplatform.portfolio.loanaccount.domain;

public interface LoanLifecycleStateMachine {

    LoanStatus transition(LoanEvent loanEvent, LoanStatus from);
}
