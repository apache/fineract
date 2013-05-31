package org.mifosplatform.accounting.glaccount.data;

public class GLAccountDataForLookup {

    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String glCode;

    public GLAccountDataForLookup(final Long id, final String name, final String glCode) {
        this.id = id;
        this.name = name;
        this.glCode = glCode;
    }

    public Long getId() {
        return this.id;
    }

}
