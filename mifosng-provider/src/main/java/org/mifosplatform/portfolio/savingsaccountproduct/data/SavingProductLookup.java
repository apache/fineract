package org.mifosplatform.portfolio.savingsaccountproduct.data;

import java.io.Serializable;

public class SavingProductLookup implements Serializable {

    private final Long id;
    private final String name;

    public SavingProductLookup() {
        this.id=null;
        this.name=null;
    }

    public SavingProductLookup(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasId(final Long matchingId) {
        return this.id.equals(matchingId);
    }

}
