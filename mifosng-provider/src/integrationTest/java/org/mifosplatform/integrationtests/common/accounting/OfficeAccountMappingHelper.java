package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class OfficeAccountMappingHelper {

    private static final String ACCOUNT_MAPPING_URL = "/mifosng-provider/api/v1/officeglaccounts";
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public OfficeAccountMappingHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Object createOfficeAccountMapping(Integer officeId, Integer glAccountId, String jsonBack) {
        String json = OfficeAccountMappingBuilder.build(officeId, glAccountId);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER, json,
                jsonBack);
    }

    public Object updateOfficeAccountMapping(Integer mappingId, Integer officeId, Integer glAccountId, String jsonBack) {
        String json = OfficeAccountMappingBuilder.build(officeId, glAccountId);
        return Utils.performServerPut(this.requestSpec, this.responseSpec, ACCOUNT_MAPPING_URL + "/" + mappingId + "?"
                + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    public HashMap getOfficeAccountMapping(final Integer mappingId) {
        final String URL = ACCOUNT_MAPPING_URL + "/" + mappingId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public HashMap getOfficeAccountMappings() {
        final String URL = ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

}
