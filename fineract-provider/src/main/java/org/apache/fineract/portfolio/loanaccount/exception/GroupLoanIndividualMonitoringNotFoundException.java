package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

@SuppressWarnings("serial")
public class GroupLoanIndividualMonitoringNotFoundException extends AbstractPlatformResourceNotFoundException{

	public GroupLoanIndividualMonitoringNotFoundException(final Long id) {
        super("error.msg.glim.id.invalid", "Group Loan individual record with identifier " + id + " does not exist", id);
    }
	
}
