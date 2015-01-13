/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;

public class OfficeOpeningBalancesData {

    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final LocalDate transactionDate;
    @SuppressWarnings("unused")
    private final GLAccountData contraAccount;
    @SuppressWarnings("unused")
    private final List<JournalEntryData> assetAccountOpeningBalances;
    @SuppressWarnings("unused")
    private final List<JournalEntryData> liabityAccountOpeningBalances;
    @SuppressWarnings("unused")
    private final List<JournalEntryData> incomeAccountOpeningBalances;
    @SuppressWarnings("unused")
    private final List<JournalEntryData> equityAccountOpeningBalances;
    @SuppressWarnings("unused")
    private final List<JournalEntryData> expenseAccountOpeningBalances;

    private OfficeOpeningBalancesData(final Long officeId, final String officeName, final LocalDate transactionDate,
            final GLAccountData contraAccount, final List<JournalEntryData> assetAccountOpeningBalances,
            final List<JournalEntryData> liabityAccountOpeningBalances, final List<JournalEntryData> incomeAccountOpeningBalances,
            final List<JournalEntryData> equityAccountOpeningBalances, final List<JournalEntryData> expenseAccountOpeningBalances) {
        this.officeId = officeId;
        this.officeName = officeName;
        this.transactionDate = transactionDate;
        this.contraAccount = contraAccount;
        this.assetAccountOpeningBalances = assetAccountOpeningBalances;
        this.liabityAccountOpeningBalances = liabityAccountOpeningBalances;
        this.incomeAccountOpeningBalances = incomeAccountOpeningBalances;
        this.equityAccountOpeningBalances = equityAccountOpeningBalances;
        this.expenseAccountOpeningBalances = expenseAccountOpeningBalances;
    }

    public static OfficeOpeningBalancesData createNew(final Long officeId, final String officeName, final LocalDate transactionDate,
            final GLAccountData contraAccount, final List<JournalEntryData> assetAccountOpeningBalances,
            final List<JournalEntryData> liabityAccountOpeningBalances, final List<JournalEntryData> incomeAccountOpeningBalances,
            final List<JournalEntryData> equityAccountOpeningBalances, final List<JournalEntryData> expenseAccountOpeningBalances) {
        return new OfficeOpeningBalancesData(officeId, officeName, transactionDate, contraAccount, assetAccountOpeningBalances,
                liabityAccountOpeningBalances, incomeAccountOpeningBalances, equityAccountOpeningBalances, expenseAccountOpeningBalances);
    }
}
