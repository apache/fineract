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

import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class FinancialActivityAccountHelper {

    private static final String FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL = "/fineract-provider/api/v1/financialactivityaccounts";
    private final RequestSpecification requestSpec;

    public FinancialActivityAccountHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    public Object createFinancialActivityAccount(Integer financialActivityId, Integer glAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPost(this.requestSpec, responseSpecification, FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?"
                + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    public Object updateFinancialActivityAccount(Integer financialActivityAccountId, Integer financialActivityId, Integer glAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPut(this.requestSpec, responseSpecification, FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/"
                + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    public HashMap getFinancialActivityAccount(final Integer financialActivityAccountId, final ResponseSpecification responseSpecification) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpecification, url, "");
    }

    public List<HashMap> getAllFinancialActivityAccounts(final ResponseSpecification responseSpecification) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(this.requestSpec, responseSpecification, url, "");
    }

    public Integer deleteFinancialActivityAccount(final Integer financialActivityAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, responseSpecification, url, jsonBack);
    }

}
