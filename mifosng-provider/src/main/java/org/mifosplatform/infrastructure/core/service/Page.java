/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import java.util.List;

public class Page<E> {

    private final int totalFilteredRecords;
    private final List<E> pageItems;

    public Page(final List<E> pageItems, final int totalFilteredRecords) {
        this.pageItems = pageItems;
        this.totalFilteredRecords = totalFilteredRecords;
    }

    public int getTotalFilteredRecords() {
        return this.totalFilteredRecords;
    }

    public List<E> getPageItems() {
        return pageItems;
    }
}