/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object for collection sheet.
 */
public class IndividualCollectionSheetData {

    @SuppressWarnings("unused")
    private final LocalDate dueDate;
    @SuppressWarnings("unused")
    private final Collection<IndividualClientData> clients;

    @SuppressWarnings("unused")
    private final Collection<CodeValueData> paymentTypeOptions;

    public static IndividualCollectionSheetData instance(final LocalDate date, final Collection<IndividualClientData> clients,
            final Collection<CodeValueData> paymentTypeOptions) {
        return new IndividualCollectionSheetData(date, clients, paymentTypeOptions);
    }

    private IndividualCollectionSheetData(final LocalDate dueDate, final Collection<IndividualClientData> clients,
            final Collection<CodeValueData> paymentTypeOptions) {
        this.dueDate = dueDate;
        this.clients = clients;
        this.paymentTypeOptions = paymentTypeOptions;
    }

}