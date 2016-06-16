package org.apache.fineract.portfolio.loanaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class EachClientShareMustBeGreaterThanZeroException  extends AbstractPlatformResourceNotFoundException {

	public EachClientShareMustBeGreaterThanZeroException() {
		super("error.msg.glim.each.client.must.have.more.than.zero.amount", "Each client must have more than 0 amount.");
		// TODO Auto-generated constructor stub
	}

}
