package org.mifosplatform.integrationtests.common.accounting;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JournalEntryHelper {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    public JournalEntryHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public  void checkJournalEntryForExpenseAccount(Account expenseAccount, String date, JournalEntry accountEntries) {
       checkJournalEntry(expenseAccount, Utils.convertDateToURLFormat(date),accountEntries);
    }


    public  void checkJournalEntryForAssetAccount(Account assetAccount, String date, JournalEntry... accountEntries) {
         checkJournalEntry(assetAccount,Utils.convertDateToURLFormat(date),accountEntries);
    }

    public void checkJournalEntryForIncomeAccount(Account incomeAccount, String date, JournalEntry accountEntries) {
         checkJournalEntry(incomeAccount, Utils.convertDateToURLFormat(date), accountEntries);
    }

    private  String getEntryValueFromJournalEntry(ArrayList<HashMap> entryResponse, int entryNumber) {
        HashMap map = (HashMap) entryResponse.get(entryNumber).get("entryType");
        return (String) map.get("value");
    }

    private  Float getTransactionAmountFromJournalEntry(ArrayList<HashMap> entryResponse, int entryNumber) {
        return (Float) entryResponse.get(entryNumber).get("amount");
    }

    private void checkJournalEntry(Account account, String date, JournalEntry... accountEntries){
        String url = createURLForGettingAccountEntries(account,date);
        ArrayList<HashMap> response = Utils.performServerGet(requestSpec, responseSpec, url, "");
        for (int i = 0; i < accountEntries.length; i++) {
            assertThat(getEntryValueFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionType()));
            assertThat(getTransactionAmountFromJournalEntry(response, i), equalTo(accountEntries[i].getTransactionAmount()));
        }
    }

    private String createURLForGettingAccountEntries (Account account,String date){
        return new String ("/mifosng-provider/api/v1/journalentries?glAccountId=" + account.getAccountID() + "&type=" +account.getAccountType()
                            + "&fromDate=" + date + "&toDate=" + date + "&tenantIdentifier=default");
    }

}
