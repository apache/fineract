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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.test.data.GLAType;
import org.apache.fineract.test.data.GLAUsage;
import org.apache.fineract.test.factory.GLAccountRequestFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GLGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final Integer GLA_USAGE_DETAIL = GLAUsage.DETAIL.value;
    public static final Integer GLA_TYPE_ASSET = GLAType.ASSET.value;
    public static final Integer GLA_TYPE_LIABILITY = GLAType.LIABILITY.value;
    public static final Integer GLA_TYPE_INCOME = GLAType.INCOME.value;
    public static final Integer GLA_TYPE_EXPENSE = GLAType.EXPENSE.value;
    public static final String GLA_NAME_1 = "Loans Receivable";
    public static final String GLA_NAME_2 = "Interest/Fee Receivable";
    public static final String GLA_NAME_3 = "Other Receivables";
    public static final String GLA_NAME_4 = "UNC Receivable";
    public static final String GLA_NAME_5 = "AA Suspense Balance";
    public static final String GLA_NAME_6 = "Suspense/Clearing account";
    public static final String GLA_NAME_7 = "Deferred Interest Revenue";
    public static final String GLA_NAME_8 = "Retained Earnings Prior Year";
    public static final String GLA_NAME_9 = "Interest Income";
    public static final String GLA_NAME_10 = "Fee Income";
    public static final String GLA_NAME_11 = "Fee Charge Off";
    public static final String GLA_NAME_12 = "Credit Loss/Bad Debt";
    public static final String GLA_NAME_13 = "Credit Loss/Bad Debt-Fraud";
    public static final String GLA_NAME_14 = "Transfer in suspense account";
    public static final String GLA_NAME_15 = "Recoveries";
    public static final String GLA_NAME_16 = "Written off";
    public static final String GLA_NAME_17 = "Overpayment account";
    public static final String GLA_NAME_18 = "Fund Receivables";
    public static final String GLA_NAME_19 = "Goodwill Expense Account";
    public static final String GLA_NAME_20 = "Interest Income Charge Off";
    public static final String GLA_NAME_21 = "Asset transfer";
    public static final String GLA_NAME_22 = "Deferred Capitalized Income";
    public static final String GLA_NAME_23 = "Buy Down Expense";
    public static final String GLA_NAME_24 = "Income From Buy Down";
    public static final String GLA_GL_CODE_1 = "112601";
    public static final String GLA_GL_CODE_2 = "112603";
    public static final String GLA_GL_CODE_3 = "145800";
    public static final String GLA_GL_CODE_4 = "245000";
    public static final String GLA_GL_CODE_5 = "999999";
    public static final String GLA_GL_CODE_6 = "145023";
    public static final String GLA_GL_CODE_7 = "240005";
    public static final String GLA_GL_CODE_8 = "320000";
    public static final String GLA_GL_CODE_9 = "404000";
    public static final String GLA_GL_CODE_10 = "404007";
    public static final String GLA_GL_CODE_11 = "404008";
    public static final String GLA_GL_CODE_12 = "744007";
    public static final String GLA_GL_CODE_13 = "744037";
    public static final String GLA_GL_CODE_14 = "A5";
    public static final String GLA_GL_CODE_15 = "744008";
    public static final String GLA_GL_CODE_16 = "e4";
    public static final String GLA_GL_CODE_17 = "l1";
    public static final String GLA_GL_CODE_18 = "987654";
    public static final String GLA_GL_CODE_19 = "744003";
    public static final String GLA_GL_CODE_20 = "404001";
    public static final String GLA_GL_CODE_21 = "146000";
    public static final String GLA_GL_CODE_22 = "145024";
    public static final String GLA_GL_CODE_23 = "450280";
    public static final String GLA_GL_CODE_24 = "450281";

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        List<GetGLAccountsResponse> existingAccounts = new ArrayList<>();
        try {
            existingAccounts = fineractClient.generalLedgerAccount().retrieveAllAccountsUniversal(Map.of());
        } catch (Exception e) {
            log.debug("Could not retrieve existing GL accounts, will create them", e);
        }

        final List<GetGLAccountsResponse> accounts = existingAccounts;

        createGLAccountIfNotExists(accounts, GLA_NAME_1, GLA_GL_CODE_1, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_2, GLA_GL_CODE_2, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_3, GLA_GL_CODE_3, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_4, GLA_GL_CODE_4, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_5, GLA_GL_CODE_5, GLA_TYPE_LIABILITY);
        createGLAccountIfNotExists(accounts, GLA_NAME_6, GLA_GL_CODE_6, GLA_TYPE_LIABILITY);
        createGLAccountIfNotExists(accounts, GLA_NAME_7, GLA_GL_CODE_7, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_8, GLA_GL_CODE_8, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_9, GLA_GL_CODE_9, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_10, GLA_GL_CODE_10, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_11, GLA_GL_CODE_11, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_12, GLA_GL_CODE_12, GLA_TYPE_EXPENSE);
        createGLAccountIfNotExists(accounts, GLA_NAME_13, GLA_GL_CODE_13, GLA_TYPE_EXPENSE);
        createGLAccountIfNotExists(accounts, GLA_NAME_14, GLA_GL_CODE_14, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_15, GLA_GL_CODE_15, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_16, GLA_GL_CODE_16, GLA_TYPE_EXPENSE);
        createGLAccountIfNotExists(accounts, GLA_NAME_17, GLA_GL_CODE_17, GLA_TYPE_LIABILITY);
        createGLAccountIfNotExists(accounts, GLA_NAME_18, GLA_GL_CODE_18, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_19, GLA_GL_CODE_19, GLA_TYPE_EXPENSE);
        createGLAccountIfNotExists(accounts, GLA_NAME_20, GLA_GL_CODE_20, GLA_TYPE_INCOME);
        createGLAccountIfNotExists(accounts, GLA_NAME_21, GLA_GL_CODE_21, GLA_TYPE_ASSET);
        createGLAccountIfNotExists(accounts, GLA_NAME_22, GLA_GL_CODE_22, GLA_TYPE_LIABILITY);
        createGLAccountIfNotExists(accounts, GLA_NAME_23, GLA_GL_CODE_23, GLA_TYPE_EXPENSE);
        createGLAccountIfNotExists(accounts, GLA_NAME_24, GLA_GL_CODE_24, GLA_TYPE_INCOME);
    }

    private void createGLAccountIfNotExists(List<GetGLAccountsResponse> existingAccounts, String name, String glCode, Integer type) {
        boolean accountExists = existingAccounts.stream().anyMatch(a -> glCode.equals(a.getGlCode()));
        if (accountExists) {
            return;
        }

        PostGLAccountsRequest request = GLAccountRequestFactory.defaultGLAccountRequest(name, glCode, type, GLA_USAGE_DETAIL, true);
        executeVoid(() -> fineractClient.generalLedgerAccount().createGLAccount1(request, Map.of()));
    }
}
