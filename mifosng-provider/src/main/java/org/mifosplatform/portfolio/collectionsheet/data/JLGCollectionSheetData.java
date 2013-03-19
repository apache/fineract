/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.util.Collection;

import org.joda.time.LocalDate;

/**
 * Immutable data object for join liability group's collection sheet.
 */
public class JLGCollectionSheetData {

    private final LocalDate dueDate;
    private final Collection<JLGClientsData> groups;

    public JLGCollectionSheetData(final LocalDate date, final Collection<JLGClientsData> groups) {
        this.dueDate = date;
        this.groups = groups;
    }

    public LocalDate getDate() {
        return this.dueDate;
    }

    public Collection<JLGClientsData> getGroups() {
        return this.groups;
    }

}