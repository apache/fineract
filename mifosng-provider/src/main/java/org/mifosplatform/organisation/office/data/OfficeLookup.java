/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.data;

/**
 * Immutable data object used for office lookups (will remove eventually and just use {@link OfficeData}.
 */
public class OfficeLookup {

    private Long id;
    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private String nameDecorated;

    public OfficeLookup(final Long id, final String name, final String nameDecorated) {
        this.id = id;
        this.name = name;
        this.nameDecorated = nameDecorated;
    }

    public boolean hasIdentifyOf(final Long officeId) {
        return this.id.equals(officeId);
    }
}