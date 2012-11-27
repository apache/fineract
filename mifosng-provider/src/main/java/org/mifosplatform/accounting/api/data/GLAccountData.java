package org.mifosplatform.accounting.api.data;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class GLAccountData {

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
    private final String classification;
    @SuppressWarnings("unused")
    private final Boolean headerAccount;
    @SuppressWarnings("unused")
    private final String description;

    public GLAccountData(final Long id, final String name, final Long parentId, final String glCode, final boolean disabled,
            final boolean manualEntriesAllowed, final String classification, final boolean headerAccount, final String description) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.disabled = disabled;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.classification = classification;
        this.headerAccount = headerAccount;
        this.description = description;
    }
}