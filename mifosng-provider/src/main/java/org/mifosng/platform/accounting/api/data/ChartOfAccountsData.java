package org.mifosng.platform.accounting.api.data;

import java.util.Collection;

/**
 * Immutable object representing chart of accounts data.
 * 
 * Note: no getter/setters required as google-gson will produce json from fields of object.
 */
public class ChartOfAccountsData {

	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String name;
	@SuppressWarnings("unused")
	private final Collection<GeneralLedgerAccountData> accounts;

	public ChartOfAccountsData(final Long id, final String name, final Collection<GeneralLedgerAccountData> accounts) {
		this.id = id;
		this.name = name;
		this.accounts = accounts;
	}
}