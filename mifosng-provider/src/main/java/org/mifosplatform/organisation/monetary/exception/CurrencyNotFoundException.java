package org.mifosplatform.organisation.monetary.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class CurrencyNotFoundException extends AbstractPlatformResourceNotFoundException {

	public CurrencyNotFoundException(final String currencyCode) {
		super("error.msg.currency.currencyCode.invalid", "Currency with identifier " + currencyCode + " does not exist", currencyCode);
	}
}