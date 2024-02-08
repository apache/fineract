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

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;

@RequiredArgsConstructor
@Getter
public final class OfficeOpeningBalancesData {

    private final Long officeId;
    private final String officeName;
    private final LocalDate transactionDate;
    private final GLAccountData contraAccount;
    private final List<JournalEntryData> assetAccountOpeningBalances;
    private final List<JournalEntryData> liabityAccountOpeningBalances;
    private final List<JournalEntryData> incomeAccountOpeningBalances;
    private final List<JournalEntryData> equityAccountOpeningBalances;
    private final List<JournalEntryData> expenseAccountOpeningBalances;

    public static OfficeOpeningBalancesData createNew(final Long officeId, final String officeName, final LocalDate transactionDate,
            final GLAccountData contraAccount, final List<JournalEntryData> assetAccountOpeningBalances,
            final List<JournalEntryData> liabityAccountOpeningBalances, final List<JournalEntryData> incomeAccountOpeningBalances,
            final List<JournalEntryData> equityAccountOpeningBalances, final List<JournalEntryData> expenseAccountOpeningBalances) {
        return new OfficeOpeningBalancesData(officeId, officeName, transactionDate, contraAccount, assetAccountOpeningBalances,
                liabityAccountOpeningBalances, incomeAccountOpeningBalances, equityAccountOpeningBalances, expenseAccountOpeningBalances);
    }
}
