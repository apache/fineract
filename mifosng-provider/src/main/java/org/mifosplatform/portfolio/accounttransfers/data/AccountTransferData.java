/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounttransfers.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;

/**
 * Immutable data object representing a savings account.
 */
@SuppressWarnings("unused")
public class AccountTransferData {

    private final Long id;
    private final Boolean reversed;
    private final CurrencyData currency;
    private final BigDecimal transferAmount;
    private final LocalDate transferDate;
    private final String transferDescription;
    private final OfficeData fromOffice;
    private final ClientData fromClient;
    private final SavingsAccountData fromAccount;
    private final OfficeData toOffice;
    private final ClientData toClient;
    private final SavingsAccountData toAccount;

    // template
    private final Collection<OfficeData> fromOfficeOptions;
    private final Collection<ClientData> fromClientOptions;
    private final Collection<SavingsAccountData> fromAccountOptions;
    private final Collection<OfficeData> toOfficeOptions;
    private final Collection<ClientData> toClientOptions;
    private final Collection<SavingsAccountData> toAccountOptions;

    public static AccountTransferData template(final OfficeData fromOffice, final ClientData fromClient,
            final SavingsAccountData fromAccount, final LocalDate transferDate, final OfficeData toOffice, final ClientData toClient,
            final SavingsAccountData toAccount, final Collection<OfficeData> fromOfficeOptions,
            final Collection<ClientData> fromClientOptions, final Collection<SavingsAccountData> fromAccountOptions,
            final Collection<OfficeData> toOfficeOptions, final Collection<ClientData> toClientOptions,
            final Collection<SavingsAccountData> toAccountOptions) {
        final Long id = null;
        CurrencyData currency = null;
        if (fromAccount != null) {
            currency = fromAccount.currency();
        }

        final BigDecimal transferAmount = BigDecimal.ZERO;
        final String transferDescription = null;
        final Boolean reversed = null;
        return new AccountTransferData(id, reversed, fromOffice, fromClient, fromAccount, currency, transferAmount, transferDate,
                transferDescription, toOffice, toClient, toAccount, fromOfficeOptions, fromClientOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountOptions);
    }

    public static AccountTransferData instance(final Long id, final Boolean reversed, final LocalDate transferDate,
            final CurrencyData currency, final BigDecimal transferAmount, final String transferDescription, final OfficeData fromOffice,
            final OfficeData toOffice, final ClientData fromClient, final ClientData toClient, final SavingsAccountData fromSavingsAccount,
            final SavingsAccountData toSavingsAccount) {

        return new AccountTransferData(id, reversed, fromOffice, fromClient, fromSavingsAccount, currency, transferAmount, transferDate,
                transferDescription, toOffice, toClient, toSavingsAccount, null, null, null, null, null, null);
    }

    public static AccountTransferData transferBasicDetails(final Long id, final CurrencyData currency, final BigDecimal transferAmount,
            final LocalDate transferDate, final String description, final Boolean reversed) {

        return new AccountTransferData(id, reversed, null, null, null, currency, transferAmount, transferDate, description, null, null,
                null, null, null, null, null, null, null);
    }

    private AccountTransferData(final Long id, final Boolean reversed, final OfficeData fromOffice, final ClientData fromClient,
            final SavingsAccountData fromAccount, final CurrencyData currency, final BigDecimal transferAmount,
            final LocalDate transferDate, final String transferDescription, final OfficeData toOffice, final ClientData toClient,
            final SavingsAccountData toAccount, final Collection<OfficeData> fromOfficeOptions,
            final Collection<ClientData> fromClientOptions, final Collection<SavingsAccountData> fromAccountOptions,
            final Collection<OfficeData> toOfficeOptions, final Collection<ClientData> toClientOptions,
            final Collection<SavingsAccountData> toAccountOptions) {
        this.id = id;
        this.reversed = reversed;
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromAccount = fromAccount;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.toAccount = toAccount;

        this.currency = currency;
        this.transferAmount = transferAmount;
        this.transferDate = transferDate;
        this.transferDescription = transferDescription;

        this.fromOfficeOptions = fromOfficeOptions;
        this.fromClientOptions = fromClientOptions;
        this.fromAccountOptions = fromAccountOptions;
        this.toOfficeOptions = toOfficeOptions;
        this.toClientOptions = toClientOptions;
        this.toAccountOptions = toAccountOptions;
    }
}