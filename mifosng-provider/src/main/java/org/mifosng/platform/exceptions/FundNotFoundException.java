package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when fund resources are not found.
 */
public class FundNotFoundException extends AbstractPlatformResourceNotFoundException {

	public FundNotFoundException(final Long id) {
		super("error.msg.fund.id.invalid", "Fund with identifier " + id + " does not exist", id);
	}
}