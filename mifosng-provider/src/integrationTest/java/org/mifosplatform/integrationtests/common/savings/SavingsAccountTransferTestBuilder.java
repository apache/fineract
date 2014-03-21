package org.mifosplatform.integrationtests.common.savings;

import java.util.HashMap;

import com.google.gson.Gson;

public class SavingsAccountTransferTestBuilder {

    private static final String LOCALE = "en_GB";
    private static final String OFFICE_ID = "1";
    private static final String FROM_ACCOUNT_TYPE_SAVINGS = "2";
    private static final String TRANSFER_DESCRIPTION = "Transfer";

    private String transferDate = "";
    private String transferAmount = "";
    private String fromAccountType = FROM_ACCOUNT_TYPE_SAVINGS;
    private String officeId = OFFICE_ID;
    private String transferDescription = TRANSFER_DESCRIPTION;

    public String build(final String fromAccountId, final String fromClientId, final String toAccountId, final String toClientId, final String toAccountType) {

        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", LOCALE);
        map.put("fromClientId", fromClientId);
        map.put("fromAccountId", fromAccountId);
        map.put("fromAccountType", this.fromAccountType);
        map.put("fromOfficeId", this.officeId);
        map.put("toClientId", toClientId);
        map.put("toAccountId", toAccountId);
        map.put("toAccountType", toAccountType);
        map.put("toOfficeId", this.officeId);
        map.put("transferDate", this.transferDate);
        map.put("transferAmount", this.transferAmount);
        map.put("transferDescription", this.transferDescription);
        String savingsApplicationJSON = new Gson().toJson(map);
        System.out.println(savingsApplicationJSON);
        return savingsApplicationJSON;
    }

    public SavingsAccountTransferTestBuilder withTransferOnDate(final String savingsAccountTransferDate) {
        this.transferDate = savingsAccountTransferDate;
        return this;
    }

    public SavingsAccountTransferTestBuilder withTransferAmount(final String savingsAccountTransferAmount) {
        this.transferAmount = savingsAccountTransferAmount;
        return this;
    }
}