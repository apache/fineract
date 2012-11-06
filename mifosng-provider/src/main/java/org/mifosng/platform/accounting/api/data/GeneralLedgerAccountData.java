package org.mifosng.platform.accounting.api.data;

/**
 * Immutable object representing a general ledger accounts data as required by chart of accounts.
 * 
 * Note: no getter/setters required as google-gson will produce json from fields of object.
 */
public class GeneralLedgerAccountData {

	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String name;
	@SuppressWarnings("unused")
	private final Long parentId;
	@SuppressWarnings("unused")
	private final String glCode;
	@SuppressWarnings("unused")
	private final boolean disabled;
	@SuppressWarnings("unused")
	private final boolean manualEntriesAllowed;
	@SuppressWarnings("unused")
	private final String category;
	@SuppressWarnings("unused")
	private final String ledgerType;
	@SuppressWarnings("unused")
	private final String description;

	public GeneralLedgerAccountData(final Long id, final String name,
			final Long parentId, final String glCode, final boolean disabled,
			final boolean manualEntriesAllowed, final String category,
			final String ledgerType, final String description) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.glCode = glCode;
		this.disabled = disabled;
		this.manualEntriesAllowed = manualEntriesAllowed;
		this.category = category;
		this.ledgerType = ledgerType;
		this.description = description;
	}
}