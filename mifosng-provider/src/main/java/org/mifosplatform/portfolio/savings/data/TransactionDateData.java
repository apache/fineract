/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing date
 */
@SuppressWarnings("unused")
public class TransactionDateData {

    private final LocalDate date;

    public TransactionDateData(final LocalDate date) {
        this.date = date;
    }
}