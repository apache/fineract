/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.journalentry.data;

import java.util.List;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.joda.time.LocalDate;

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
