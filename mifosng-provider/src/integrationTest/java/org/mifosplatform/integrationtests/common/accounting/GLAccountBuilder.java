package org.mifosplatform.integrationtests.common.accounting;

import com.google.gson.Gson;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.Utils;

import java.util.Calendar;
import java.util.HashMap;

public class GLAccountBuilder {

    public static final String ASSET_ACCOUNT="1";
    public static final String LIABILITY_ACCOUNT="2";
    public static final String EQUITY_ACCOUNT="3";
    public static final String INCOME_ACCOUNT="4";
    public static final String EXPENSE_ACCOUNT="5";

    private static final String ACCOUNT_USAGE_DETAIL="1";
    private static final String ACCOUNT_USAGE_HEADER="2";
    private  static final String MANUAL_ENTRIES_ALLOW="true";
    private  static final String MANUAL_ENTRIES_NOT_ALLOW="false";

    private static String name = Utils.randomStringGenerator("ACCOUNT_NAME_", 5);

    private static String GLCode="";
    private static String accountType="";
    private static String accountUsage=ACCOUNT_USAGE_DETAIL;
    private static String manualEntriesAllowed=MANUAL_ENTRIES_ALLOW;
    private static String description = "DEFAULT_DESCRIPTION";

    public String build(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name",this.name);
        map.put("glCode",this.GLCode);
        map.put("manualEntriesAllowed",this.manualEntriesAllowed);
        map.put("type",this.accountType);
        map.put("usage",this.accountUsage);
        map.put("description",this.description);
        return new Gson().toJson(map);
    }

    public GLAccountBuilder withAccountTypeAsAsset(){
        this.accountType= ASSET_ACCOUNT;
        this.GLCode= Utils.randomStringGenerator("ASSET_",2);
        this.GLCode+= Calendar.getInstance().getTimeInMillis()+""; //Added unique timestamp for avoiding random collisions
        return this;
    }
    public GLAccountBuilder withAccountTypeAsLiability(){
        this.accountType=LIABILITY_ACCOUNT;
        this.GLCode=Utils.randomStringGenerator("LIABILITY_",2);
        this.GLCode+= Calendar.getInstance().getTimeInMillis()+"";
        return this;
    }
    public GLAccountBuilder withAccountTypeAsAsEquity(){
        this.accountType=EQUITY_ACCOUNT;
        this.GLCode=Utils.randomStringGenerator("EQUITY_",2);
        this.GLCode+= Calendar.getInstance().getTimeInMillis()+"";
        return this;
    }
    public GLAccountBuilder withAccountTypeAsIncome(){
        this.accountType=INCOME_ACCOUNT;
        this.GLCode=Utils.randomStringGenerator("INCOME_",2);
        this.GLCode+= Calendar.getInstance().getTimeInMillis()+"";
        return this;
    }
    public GLAccountBuilder withAccountTypeAsExpense(){
        this.accountType=EXPENSE_ACCOUNT;
        this.GLCode=Utils.randomStringGenerator("EXPENSE_",2);
        this.GLCode+= Calendar.getInstance().getTimeInMillis()+"";
        return this;
    }
    public GLAccountBuilder withAccountUsageAsHeader(){
        this.accountUsage = ACCOUNT_USAGE_HEADER;
        return this;
    }

    public GLAccountBuilder withMaualEntriesNotAllowed(){
        this.manualEntriesAllowed = MANUAL_ENTRIES_NOT_ALLOW;
        return this;
    }
}
