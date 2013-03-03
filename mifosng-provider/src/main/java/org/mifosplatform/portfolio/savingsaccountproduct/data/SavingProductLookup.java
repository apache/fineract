/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccountproduct.data;

import java.io.Serializable;

public class SavingProductLookup implements Serializable {

    private final Long id;
    private final String name;

    public SavingProductLookup() {
        this.id = null;
        this.name = null;
    }

    public SavingProductLookup(final Long id, final String name) {
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
