package org.mifosplatform.integrationtests.common.accounting;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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

    public void checkJournalEntryForExpenseAccount(final Account expenseAccount, final String date, final JournalEntry accountEntries) {
        checkJournalEntry(expenseAccount, date, accountEntries);
    }

    public void checkJournalEntryForAssetAccount(final Account assetAccount, final String date, final JournalEntry... accountEntries) {
        checkJournalEntry(assetAccount, date, accountEntries);
    }

    public void checkJournalEntryForIncomeAccount(final Account incomeAccount, final String date, final JournalEntry accountEntries) {
        checkJournalEntry(incomeAccount, date, accountEntries);
    }

    private String getEntryValueFromJournalEntry(final ArrayList<HashMap> entryResponse, final int entryNumber) {
        final HashMap map = (HashMap) entryResponse.get(entryNumber).get("entryType");
        return (String) map.get("value");
    }

    private Float getTransactionAmountFromJournalEntry(final ArrayList<HashMap> entryResponse, final int entryNumber) {
        return (Float) entryResponse.get(entryNumber).get("amount");
    }

    private void checkJournalEntry(final Account account, final String date, final JournalEntry... accountEntries) {
        final String url = createURLForGettingAccountEntries(account, date);
        final ArrayList<HashMap> response = Utils.performServerGet(this.requestSpec, this.responseSpec, url, "pageItems");
        for (int i = 0; i < accountEntries.length; i++) {
            assertThat(getEntryValueFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionType()));
            assertThat(getTransactionAmountFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionAmount()));
        }
    }

    private String createURLForGettingAccountEntries(final Account account, final String date) {
        return new String("/mifosng-provider/api/v1/journalentries?glAccountId=" + account.getAccountID() + "&type="
                + account.getAccountType() + "&fromDate=" + date + "&toDate=" + date + "&tenantIdentifier=default"
                + "&orderBy=id&sortOrder=desc&locale=en&dateFormat=dd MMMM yyyy");
    }

}
