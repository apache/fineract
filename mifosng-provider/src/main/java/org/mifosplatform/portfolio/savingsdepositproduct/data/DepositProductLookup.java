package org.mifosplatform.portfolio.savingsdepositproduct.data;

public class DepositProductLookup {

    private final Long id;
    @SuppressWarnings("unused")
    private final String name;

    public DepositProductLookup(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public boolean hasId(final Long matchingId) {
        return this.id.equals(matchingId);
    }

    public Long id() {
        return this.id;
    }
}