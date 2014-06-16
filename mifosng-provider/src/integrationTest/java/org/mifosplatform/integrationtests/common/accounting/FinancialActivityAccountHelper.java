package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class FinancialActivityAccountHelper {

    private static final String FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL = "/mifosng-provider/api/v1/financialactivityaccounts";
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public FinancialActivityAccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Object createFinancialActivityAccount(Integer financialActivityId, Integer glAccountId, String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?"
                + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    public Object updateFinancialActivityAccount(Integer financialActivityAccountId, Integer financialActivityId, Integer glAccountId,
            String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPut(this.requestSpec, this.responseSpec, FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/"
                + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    public HashMap getFinancialActivityAccount(final Integer financialActivityAccountId) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    public void getFinancialActivityAccountAndValidateResouceNotFound(final Integer financialActivityAccountId) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        Utils.performServerGetAndValidateResourceNotFound(url);
    }

    public HashMap getAllFinancialActivityAccounts() {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(this.requestSpec, this.responseSpec, url, "");
    }

    public Integer deleteFinancialActivityAccount(final Integer financialActivityAccountId, String jsonBack) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, url, jsonBack);
    }

}
