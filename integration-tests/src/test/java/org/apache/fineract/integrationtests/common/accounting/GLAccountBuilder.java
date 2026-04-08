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
package org.apache.fineract.integrationtests.common.accounting;

import com.google.gson.Gson;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.integrationtests.common.Utils;

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

    private static final String DESCRIPTION = "DEFAULT_DESCRIPTION";

    private String name = Utils.uniqueRandomStringGenerator("ACCOUNT_NAME_", 5);

    private String glCode = "";
    private String accountType = "";
    private String accountUsage = ACCOUNT_USAGE_DETAIL;
    private String manualEntriesAllowed = MANUAL_ENTRIES_ALLOW;

    public String build() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("glCode", glCode);
        map.put("manualEntriesAllowed", manualEntriesAllowed);
        map.put("type", accountType);
        map.put("usage", accountUsage);
        map.put("description", DESCRIPTION);
        return new Gson().toJson(map);
    }

    public GLAccountBuilder withAccountTypeAsAsset() {
        accountType = ASSET_ACCOUNT;
        glCode = Utils.uniqueRandomStringGenerator("ASSET_" + Calendar.getInstance().getTimeInMillis(), 2);
        return this;
    }

    public GLAccountBuilder withAccountTypeAsLiability() {
        accountType = LIABILITY_ACCOUNT;
        glCode = Utils.uniqueRandomStringGenerator("LIABILITY_" + Calendar.getInstance().getTimeInMillis(), 2);
        return this;
    }

    public GLAccountBuilder withAccountTypeAsAsEquity() {
        accountType = EQUITY_ACCOUNT;
        glCode = Utils.uniqueRandomStringGenerator("EQUITY_" + Calendar.getInstance().getTimeInMillis(), 2);
        return this;
    }

    public GLAccountBuilder withAccountTypeAsIncome() {
        accountType = INCOME_ACCOUNT;
        glCode = Utils.uniqueRandomStringGenerator("INCOME_" + Calendar.getInstance().getTimeInMillis(), 2);
        return this;
    }

    public GLAccountBuilder withAccountTypeAsExpense() {
        accountType = EXPENSE_ACCOUNT;
        glCode = Utils.uniqueRandomStringGenerator("EXPENSE_" + Calendar.getInstance().getTimeInMillis(), 2);
        return this;
    }

    public GLAccountBuilder withName(String name) {
        if (StringUtils.isNotBlank(name)) {
            this.name = Utils.uniqueRandomStringGenerator(name + "_", 5);
        }
        return this;
    }

    public GLAccountBuilder withAccountUsageAsHeader() {
        accountUsage = ACCOUNT_USAGE_HEADER;
        return this;
    }

    public GLAccountBuilder withMaualEntriesNotAllowed() {
        manualEntriesAllowed = MANUAL_ENTRIES_NOT_ALLOW;
        return this;
    }
}
