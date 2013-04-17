package org.mifosplatform.integrationtests.common.accounting;

import java.util.Calendar;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;

public class GLAccountBuilder {

    public static final String ASSET_ACCOUNT = "1";
    public static final String LIABILITY_ACCOUNT = "2";
    public static final String EQUITY_ACCOUNT = "3";
    public static final String INCOME_ACCOUNT = "4";
    public static final String EXPENSE_ACCOUNT = "5";

    private static final String ACCOUNT_USAGE_DETAIL = "1";
    private static final String ACCOUNT_USAGE_HEADER = "2";
    private static final String MANUAL_ENTRIES_ALLOW = "true";
    private static final String MANUAL_ENTRIES_NOT_ALLOW = "false";

    private static String name = Utils.randomStringGenerator("ACCOUNT_NAME_", 5);

    private static String GLCode = "";
    private static String accountType = "";
    private static String accountUsage = ACCOUNT_USAGE_DETAIL;
    private static String manualEntriesAllowed = MANUAL_ENTRIES_ALLOW;
    private static String description = "DEFAULT_DESCRIPTION";

    public String build() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", GLAccountBuilder.name);
        map.put("glCode", GLAccountBuilder.GLCode);
        map.put("manualEntriesAllowed", GLAccountBuilder.manualEntriesAllowed);
        map.put("type", GLAccountBuilder.accountType);
        map.put("usage", GLAccountBuilder.accountUsage);
        map.put("description", GLAccountBuilder.description);
        return new Gson().toJson(map);
    }

    public GLAccountBuilder withAccountTypeAsAsset() {
        GLAccountBuilder.accountType = ASSET_ACCOUNT;
        GLAccountBuilder.GLCode = Utils.randomStringGenerator("ASSET_", 2);
        GLAccountBuilder.GLCode += Calendar.getInstance().getTimeInMillis() + ""; // Added
                                                                      // unique
                                                                      // timestamp
                                                                      // for
                                                                      // avoiding
                                                                      // random
                                                                      // collisions
        return this;
    }

    public GLAccountBuilder withAccountTypeAsLiability() {
        GLAccountBuilder.accountType = LIABILITY_ACCOUNT;
        GLAccountBuilder.GLCode = Utils.randomStringGenerator("LIABILITY_", 2);
        GLAccountBuilder.GLCode += Calendar.getInstance().getTimeInMillis() + "";
        return this;
    }

    public GLAccountBuilder withAccountTypeAsAsEquity() {
        GLAccountBuilder.accountType = EQUITY_ACCOUNT;
        GLAccountBuilder.GLCode = Utils.randomStringGenerator("EQUITY_", 2);
        GLAccountBuilder.GLCode += Calendar.getInstance().getTimeInMillis() + "";
        return this;
    }

    public GLAccountBuilder withAccountTypeAsIncome() {
        GLAccountBuilder.accountType = INCOME_ACCOUNT;
        GLAccountBuilder.GLCode = Utils.randomStringGenerator("INCOME_", 2);
        GLAccountBuilder.GLCode += Calendar.getInstance().getTimeInMillis() + "";
        return this;
    }

    public GLAccountBuilder withAccountTypeAsExpense() {
        GLAccountBuilder.accountType = EXPENSE_ACCOUNT;
        GLAccountBuilder.GLCode = Utils.randomStringGenerator("EXPENSE_", 2);
        GLAccountBuilder.GLCode += Calendar.getInstance().getTimeInMillis() + "";
        return this;
    }

    public GLAccountBuilder withAccountUsageAsHeader() {
        GLAccountBuilder.accountUsage = ACCOUNT_USAGE_HEADER;
        return this;
    }

    public GLAccountBuilder withMaualEntriesNotAllowed() {
        GLAccountBuilder.manualEntriesAllowed = MANUAL_ENTRIES_NOT_ALLOW;
        return this;
    }
}
