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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.services.GeneralLedgerAccountApi;
import org.apache.fineract.test.data.GLAType;
import org.apache.fineract.test.data.GLAUsage;
import org.apache.fineract.test.factory.GLAccountRequestFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

    private final GeneralLedgerAccountApi glaApi;

    @Override
    public void initialize() throws Exception {

        PostGLAccountsRequest postGLAccountsRequest1 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_1, GLA_GL_CODE_1,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest1).execute();
        PostGLAccountsRequest postGLAccountsRequest2 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_2, GLA_GL_CODE_2,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest2).execute();
        PostGLAccountsRequest postGLAccountsRequest3 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_3, GLA_GL_CODE_3,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest3).execute();
        PostGLAccountsRequest postGLAccountsRequest4 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_4, GLA_GL_CODE_4,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest4).execute();
        PostGLAccountsRequest postGLAccountsRequest5 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_5, GLA_GL_CODE_5,
                GLA_TYPE_LIABILITY, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest5).execute();
        PostGLAccountsRequest postGLAccountsRequest6 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_6, GLA_GL_CODE_6,
                GLA_TYPE_LIABILITY, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest6).execute();
        PostGLAccountsRequest postGLAccountsRequest7 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_7, GLA_GL_CODE_7,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest7).execute();
        PostGLAccountsRequest postGLAccountsRequest8 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_8, GLA_GL_CODE_8,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest8).execute();
        PostGLAccountsRequest postGLAccountsRequest9 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_9, GLA_GL_CODE_9,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest9).execute();
        PostGLAccountsRequest postGLAccountsRequest10 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_10, GLA_GL_CODE_10,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest10).execute();
        PostGLAccountsRequest postGLAccountsRequest11 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_11, GLA_GL_CODE_11,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest11).execute();
        PostGLAccountsRequest postGLAccountsRequest12 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_12, GLA_GL_CODE_12,
                GLA_TYPE_EXPENSE, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest12).execute();
        PostGLAccountsRequest postGLAccountsRequest13 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_13, GLA_GL_CODE_13,
                GLA_TYPE_EXPENSE, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest13).execute();
        PostGLAccountsRequest postGLAccountsRequest14 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_14, GLA_GL_CODE_14,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest14).execute();
        PostGLAccountsRequest postGLAccountsRequest15 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_15, GLA_GL_CODE_15,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest15).execute();
        PostGLAccountsRequest postGLAccountsRequest16 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_16, GLA_GL_CODE_16,
                GLA_TYPE_EXPENSE, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest16).execute();
        PostGLAccountsRequest postGLAccountsRequest17 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_17, GLA_GL_CODE_17,
                GLA_TYPE_LIABILITY, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest17).execute();
        PostGLAccountsRequest postGLAccountsRequest18 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_18, GLA_GL_CODE_18,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest18).execute();
        PostGLAccountsRequest postGLAccountsRequest19 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_19, GLA_GL_CODE_19,
                GLA_TYPE_EXPENSE, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest19).execute();
        PostGLAccountsRequest postGLAccountsRequest20 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_20, GLA_GL_CODE_20,
                GLA_TYPE_INCOME, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest20).execute();
        PostGLAccountsRequest postGLAccountsRequest21 = GLAccountRequestFactory.defaultGLAccountRequest(GLA_NAME_21, GLA_GL_CODE_21,
                GLA_TYPE_ASSET, GLA_USAGE_DETAIL, true);
        glaApi.createGLAccount1(postGLAccountsRequest21).execute();
    }
}
