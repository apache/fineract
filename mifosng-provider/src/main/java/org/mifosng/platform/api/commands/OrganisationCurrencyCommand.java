package org.mifosng.platform.api.commands;

/**
 * Immutable command for updating allowed currencies.
 */
public class OrganisationCurrencyCommand {

	private String[] currencies;

	public OrganisationCurrencyCommand(final String[] currencies) {
		this.currencies = currencies;
	}

	public String[] getCurrencies() {
		return currencies;
	}
}