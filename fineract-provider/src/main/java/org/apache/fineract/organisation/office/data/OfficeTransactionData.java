/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for office transactions.
 */
public class OfficeTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final LocalDate transactionDate;
    @SuppressWarnings("unused")
    private final Long fromOfficeId;
    @SuppressWarnings("unused")
    private final String fromOfficeName;
    @SuppressWarnings("unused")
    private final Long toOfficeId;
    @SuppressWarnings("unused")
    private final String toOfficeName;
    @SuppressWarnings("unused")
    private final CurrencyData currency;
    @SuppressWarnings("unused")
    private final BigDecimal transactionAmount;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;
    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;

    public static OfficeTransactionData instance(final Long id, final LocalDate transactionDate, final Long fromOfficeId,
            final String fromOfficeName, final Long toOfficeId, final String toOfficeName, final CurrencyData currency,
            final BigDecimal transactionAmount, final String description) {
        return new OfficeTransactionData(id, transactionDate, fromOfficeId, fromOfficeName, toOfficeId, toOfficeName, currency,
                transactionAmount, description, null, null);
    }

    public static OfficeTransactionData template(final LocalDate transactionDate, final Collection<OfficeData> parentLookups,
            final Collection<CurrencyData> currencyOptions) {
        return new OfficeTransactionData(null, transactionDate, null, null, null, null, null, null, null, parentLookups, currencyOptions);
    }

    private OfficeTransactionData(final Long id, final LocalDate transactionDate, final Long fromOfficeId, final String fromOfficeName,
            final Long toOfficeId, final String toOfficeName, final CurrencyData currency, final BigDecimal transactionAmount,
            final String description, final Collection<OfficeData> allowedOffices, final Collection<CurrencyData> currencyOptions) {
        this.id = id;
        this.fromOfficeId = fromOfficeId;
        this.fromOfficeName = fromOfficeName;
        this.toOfficeId = toOfficeId;
        this.toOfficeName = toOfficeName;
        this.currency = currency;
        this.transactionAmount = transactionAmount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.allowedOffices = allowedOffices;
        this.currencyOptions = currencyOptions;
    }
}