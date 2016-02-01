/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class JournalEntryHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public JournalEntryHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public void checkJournalEntryForExpenseAccount(final Account expenseAccount, final String date, final JournalEntry... accountEntries) {
        checkJournalEntry(null, expenseAccount, date, accountEntries);
    }

    public void checkJournalEntryForAssetAccount(final Account assetAccount, final String date, final JournalEntry... accountEntries) {
        checkJournalEntry(null, assetAccount, date, accountEntries);
    }

    public void checkJournalEntryForIncomeAccount(final Account incomeAccount, final String date, final JournalEntry... accountEntries) {
        checkJournalEntry(null, incomeAccount, date, accountEntries);
    }

    public void checkJournalEntryForLiabilityAccount(final Account liabilityAccount, final String date,
            final JournalEntry... accountEntries) {
        checkJournalEntry(null, liabilityAccount, date, accountEntries);
    }

    public void checkJournalEntryForLiabilityAccount(final Integer officeId, final Account liabilityAccount, final String date,
            final JournalEntry... accountEntries) {
        checkJournalEntry(officeId, liabilityAccount, date, accountEntries);
    }

    public void ensureNoAccountingTransactionsWithTransactionId(final String transactionId) {
        ArrayList<HashMap> transactions = getJournalEntriesByTransactionId(transactionId);
        assertTrue("Tranasactions are is not empty", transactions.isEmpty());

    }

    private String getEntryValueFromJournalEntry(final ArrayList<HashMap> entryResponse, final int entryNumber) {
        final HashMap map = (HashMap) entryResponse.get(entryNumber).get("entryType");
        return (String) map.get("value");
    }

    private Float getTransactionAmountFromJournalEntry(final ArrayList<HashMap> entryResponse, final int entryNumber) {
        return (Float) entryResponse.get(entryNumber).get("amount");
    }

    private void checkJournalEntry(final Integer officeId, final Account account, final String date, final JournalEntry... accountEntries) {
        final String url = createURLForGettingAccountEntries(account, date, officeId);
        final ArrayList<HashMap> response = Utils.performServerGet(this.requestSpec, this.responseSpec, url, "pageItems");
        for (int i = 0; i < accountEntries.length; i++) {
            assertThat(getEntryValueFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionType()));
            assertThat(getTransactionAmountFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionAmount()));
        }
    }

    private String createURLForGettingAccountEntries(final Account account, final String date, final Integer officeId) {
        String url = new String("/mifosng-provider/api/v1/journalentries?glAccountId=" + account.getAccountID() + "&type="
                + account.getAccountType() + "&fromDate=" + date + "&toDate=" + date + "&tenantIdentifier=default"
                + "&orderBy=id&sortOrder=desc&locale=en&dateFormat=dd MMMM yyyy");
        if (officeId != null) {
            url = url + "&officeId=" + officeId;
        }
        return url;
    }

    private ArrayList<HashMap> getJournalEntriesByTransactionId(final String transactionId) {
        final String url = createURLForGettingAccountEntriesByTransactionId(transactionId);
        final ArrayList<HashMap> response = Utils.performServerGet(this.requestSpec, this.responseSpec, url, "pageItems");
        return response;
    }

    private String createURLForGettingAccountEntriesByTransactionId(final String transactionId) {
        return new String("/mifosng-provider/api/v1/journalentries?transactionId=" + transactionId + "&tenantIdentifier=default"
                + "&orderBy=id&sortOrder=desc&locale=en&dateFormat=dd MMMM yyyy");
    }

}
