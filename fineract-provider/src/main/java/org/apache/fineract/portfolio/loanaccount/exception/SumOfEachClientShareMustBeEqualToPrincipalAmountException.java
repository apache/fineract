package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class SumOfEachClientShareMustBeEqualToPrincipalAmountException   extends AbstractPlatformResourceNotFoundException {
	public SumOfEachClientShareMustBeEqualToPrincipalAmountException() {
		super("error.msg.glim.sum.of.each.clients.share.must.be.equal.to.principal.amount", "Sum of each clients share must be equal to principal amount.");
		// TODO Auto-generated constructor stub
	}
}
