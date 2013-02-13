/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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