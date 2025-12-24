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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Collections;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class FeignAccountHelper {

    private final FineractFeignClient fineractClient;

    public FeignAccountHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Account createAssetAccount(String name) {
        return createAccount(name, "1", "ASSET");
    }

    public Account createLiabilityAccount(String name) {
        return createAccount(name, "2", "LIABILITY");
    }

    public Account createIncomeAccount(String name) {
        return createAccount(name, "4", "INCOME");
    }

    public Account createExpenseAccount(String name) {
        return createAccount(name, "5", "EXPENSE");
    }

    private Account createAccount(String name, String glCode, String type) {
        String uniqueName = Utils.uniqueRandomStringGenerator(name + "_", 4);
        String accountCode = Utils.uniqueRandomStringGenerator("GL_" + glCode, 6);

        PostGLAccountsRequest request = new PostGLAccountsRequest()//
                .name(uniqueName)//
                .glCode(accountCode)//
                .manualEntriesAllowed(true)//
                .type(getAccountTypeId(type))//
                .usage(1);

        PostGLAccountsResponse response = ok(() -> fineractClient.generalLedgerAccount().createGLAccount1(request));

        GetGLAccountsResponse account = ok(
                () -> fineractClient.generalLedgerAccount().retreiveAccount(response.getResourceId(), Collections.emptyMap()));

        return new Account(account.getId().intValue(), getAccountType(type));
    }

    private Integer getAccountTypeId(String type) {
        return switch (type) {
            case "ASSET" -> 1;
            case "LIABILITY" -> 2;
            case "EQUITY" -> 3;
            case "INCOME" -> 4;
            case "EXPENSE" -> 5;
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    private Account.AccountType getAccountType(String type) {
        return switch (type) {
            case "ASSET" -> Account.AccountType.ASSET;
            case "LIABILITY" -> Account.AccountType.LIABILITY;
            case "EQUITY" -> Account.AccountType.EQUITY;
            case "INCOME" -> Account.AccountType.INCOME;
            case "EXPENSE" -> Account.AccountType.EXPENSE;
            default -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }
}
