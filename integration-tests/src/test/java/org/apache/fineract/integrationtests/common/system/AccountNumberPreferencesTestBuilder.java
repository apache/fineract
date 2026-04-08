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
package org.apache.fineract.integrationtests.common.system;

import com.google.gson.Gson;
import java.util.HashMap;

public class AccountNumberPreferencesTestBuilder {

    private static final String ACCOUNT_TYPE = "accountType";
    private static final String PREFIX_TYPE = "prefixType";

    private String clientAccountType = "1";
    private String clientPrefixType = "101";
    private String loanAccountType = "2";
    private String loanPrefixType = "1";
    private String savingsAccountType = "3";
    private String savingsPrefixType = "1";
    private String centerAccountType = "4";
    private String centerPrefixType = "1";
    private String groupsAccountType = "5";
    private String groupsPrefixType = "1";

    public String clientBuild() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, clientAccountType);
        map.put(PREFIX_TYPE, clientPrefixType);

        return new Gson().toJson(map);
    }

    public String loanBuild() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, loanAccountType);
        map.put(PREFIX_TYPE, loanPrefixType);

        return new Gson().toJson(map);
    }

    public String savingsBuild() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, savingsAccountType);
        map.put(PREFIX_TYPE, savingsPrefixType);

        return new Gson().toJson(map);
    }

    public String groupsBuild() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, groupsAccountType);
        map.put(PREFIX_TYPE, groupsPrefixType);

        return new Gson().toJson(map);
    }

    public String centerBuild() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, centerAccountType);
        map.put(PREFIX_TYPE, centerPrefixType);

        return new Gson().toJson(map);
    }

    public String invalidDataBuild(String accountType, String prefixType) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ACCOUNT_TYPE, accountType);
        map.put(PREFIX_TYPE, prefixType);

        return new Gson().toJson(map);
    }

    public String updatePrefixType(final String prefixType) {

        final HashMap<String, Object> map = new HashMap<>();
        map.put(PREFIX_TYPE, prefixType);
        return new Gson().toJson(map);
    }
}
