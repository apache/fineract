package org.mifosplatform.organisation.office.data;

import java.math.BigDecimal;
import java.util.List;

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
    private CurrencyData currency;
    @SuppressWarnings("unused")
    private final BigDecimal transactionAmount;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final List<CurrencyData> currencyOptions;
    @SuppressWarnings("unused")
    private final List<OfficeLookup> allowedOffices;

    public OfficeTransactionData(final LocalDate transactionDate, final List<OfficeLookup> allowedOffices,
            final List<CurrencyData> currencyOptions) {
        this.transactionDate = transactionDate;
        this.allowedOffices = allowedOffices;
        this.currencyOptions = currencyOptions;
        this.id = null;
        this.fromOfficeId = null;
        this.fromOfficeName = null;
        this.toOfficeId = null;
        this.toOfficeName = null;
        this.currency = null;
        this.transactionAmount = null;
        this.description = null;
    }

    public OfficeTransactionData(final Long id, final LocalDate transactionDate, final Long fromOfficeId, final String fromOfficeName,
            final Long toOfficeId, final String toOfficeName, final CurrencyData currency, final BigDecimal transactionAmount,
            final String description) {
        this.id = id;
        this.fromOfficeId = fromOfficeId;
        this.fromOfficeName = fromOfficeName;
        this.toOfficeId = toOfficeId;
        this.toOfficeName = toOfficeName;
        this.currency = currency;
        this.transactionAmount = transactionAmount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.currencyOptions = null;
        this.allowedOffices = null;

    }
}