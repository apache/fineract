package org.mifosplatform.integrationtests.common.xbrl;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class XBRLIntegrationTestHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String GET_TAXONOMY_LIST_URL = "/mifosng-provider/api/v1/mixtaxonomy?tenantIdentifier=default";
    private static final String TAXONOMY_MAPPING_URL = "/mifosng-provider/api/v1/mixmapping?tenantIdentifier=default";

    public XBRLIntegrationTestHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ArrayList getTaxonomyList() {
        final ArrayList response = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_TAXONOMY_LIST_URL, "");

        return response;
    }

    public HashMap getTaxonomyMapping() {
        final HashMap response = Utils.performServerGet(this.requestSpec, this.responseSpec, TAXONOMY_MAPPING_URL, "config");
        return response;
    }

}
