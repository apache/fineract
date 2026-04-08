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
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.Utils;

public class AccountingRuleBuilder {

    private Long officeId;
    private Integer accountToCreditId;
    private Integer accountToDebitId;
    private String name;
    private String description;

    public AccountingRuleBuilder() {
        name = Utils.uniqueRandomStringGenerator("ACCOUNTRULE_NAME_", 5);
        description = name;
    }

    public String build() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("officeId", officeId);
        map.put("accountToCredit", accountToCreditId);
        map.put("accountToDebit", accountToDebitId);
        map.put("description", description);
        return new Gson().toJson(map);
    }

    public AccountingRuleBuilder withGLAccounts(final Integer accountToCreditId, final Integer accountToDebitId) {
        this.accountToCreditId = accountToCreditId;
        this.accountToDebitId = accountToDebitId;
        return this;
    }

    public AccountingRuleBuilder withOffice(final Long officeId) {
        this.officeId = officeId;
        return this;
    }
}
