/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;

/**
 * Immutable data object for join liability group's collection sheet.
 */
public class JLGCollectionSheetData {

    private final LocalDate dueDate;
    private final Collection<LoanProductData> loanProducts;
    private final Collection<JLGClientsData> groups;

    public JLGCollectionSheetData(final LocalDate date, final Collection<LoanProductData> loanProducts, final Collection<JLGClientsData> groups) {
        this.dueDate = date;
        this.loanProducts = loanProducts;
        this.groups = groups;
    }

    public LocalDate getDate() {
        return this.dueDate;
    }

    public Collection<JLGClientsData> getGroups() {
        return this.groups;
    }

    public Collection<LoanProductData> getLoanProducts() {
        return this.loanProducts;
    }
}