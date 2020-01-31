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
package org.apache.fineract.integrationtests.common.savings;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;

public class SavingsApplicationTestBuilder {

    private static final String LOCALE = "en_GB";

    private String submittedOnDate = "";

    private String externalId;
    private boolean withdrawalFeeForTransfers;

    private HashMap<String, String> addParams = null;

    private List<HashMap<String, Object>> datatables = null;

    public String build(final String ID, final String savingsProductId, final String accountType) {

        final HashMap<String, Object> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        if (accountType.equals("GROUP")) {
            map.put("groupId", ID);
        } else {
            map.put("clientId", ID);
        }
        map.put("productId", savingsProductId);
        map.put("locale", LOCALE);
        map.put("submittedOnDate", this.submittedOnDate);
        map.put("externalId", this.externalId);
        map.put("withdrawalFeeForTransfers", this.withdrawalFeeForTransfers);
        if (addParams != null && addParams.size() > 0) {
            map.putAll(addParams);
        }
        if (datatables != null) {
            map.put("datatables", this.datatables);
        }
        String savingsApplicationJSON = new Gson().toJson(map);
        System.out.println(savingsApplicationJSON);
        return savingsApplicationJSON;
    }

    public SavingsApplicationTestBuilder withSubmittedOnDate(final String savingsApplicationSubmittedDate) {
        this.submittedOnDate = savingsApplicationSubmittedDate;
        return this;
    }

    public SavingsApplicationTestBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public SavingsApplicationTestBuilder withWithdrawalFeeForTransfers(boolean withdrawalFeeForTransfers) {
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        return this;
    }

    public SavingsApplicationTestBuilder withParams(HashMap<String, String> params) {
        this.addParams = params;
        return this;
    }

    public SavingsApplicationTestBuilder withDatatables(final List<HashMap<String, Object>> datatables) {
        this.datatables = datatables;
        return this;
    }
}