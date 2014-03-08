package org.mifosplatform.integrationtests.common.savings;

import java.util.HashMap;

import org.mifosplatform.portfolio.savings.SavingsApiConstants;

import com.google.gson.Gson;

public class SavingsApplicationTestBuilder {

    private static final String LOCALE = "en_GB";

    private String submittedOnDate = "";

    public String build(final String ID, final String savingsProductId) {

        final HashMap<String, String> map = new HashMap<String, String>();
        map.put(SavingsApiConstants.dateFormatParamName, "dd MMMM yyyy");
        map.put(SavingsApiConstants.localeParamName, "en_GB");
        map.put(SavingsApiConstants.clientIdParamName, ID);
        map.put(SavingsApiConstants.productIdParamName, savingsProductId);
        map.put(SavingsApiConstants.localeParamName, LOCALE);
        map.put(SavingsApiConstants.submittedOnDateParamName, this.submittedOnDate);
        String savingsApplicationJSON = new Gson().toJson(map);
        System.out.println(savingsApplicationJSON);
        return savingsApplicationJSON;
    }

    public SavingsApplicationTestBuilder withSubmittedOnDate(final String savingsApplicationSubmittedDate) {
        this.submittedOnDate = savingsApplicationSubmittedDate;
        return this;
    }
}