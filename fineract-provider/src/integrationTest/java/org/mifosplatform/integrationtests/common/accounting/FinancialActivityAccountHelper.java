/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class FinancialActivityAccountHelper {

    private static final String FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL = "/mifosng-provider/api/v1/financialactivityaccounts";
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
