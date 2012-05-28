package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class CurrencyNotFoundException extends PlatformResourceNotFoundException {

	public CurrencyNotFoundException(final String currencyCode) {
		super("error.msg.currency.currencyCode.invalid", "Currency with identifier " + currencyCode + " does not exist", currencyCode);
	}
}