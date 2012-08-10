package org.mifosng.platform.exceptions;

import java.math.BigDecimal;

/**
 * Exception thrown when an attempt is made update the parent of a root office.
 */
public class InterestRateOutsideRangeException extends AbstractPlatformDomainRuleException {

	public InterestRateOutsideRangeException(final BigDecimal interestRate, final BigDecimal rangeMin, final BigDecimal rangeMax) {
		super("error.msg.deposit.account.maturityInterestRate.outside.of.allowed.range", "Interest rate of " + interestRate.toPlainString() + " is outside of allowed range of ["
				 + rangeMin.toPlainString() + ", " + rangeMax.toPlainString() + "].", interestRate, rangeMin, rangeMax);
	}
}