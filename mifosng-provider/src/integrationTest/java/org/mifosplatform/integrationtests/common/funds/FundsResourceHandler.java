/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.funds;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;

import java.util.HashMap;
import java.util.List;

public class FundsResourceHandler {

    private static final String FUNDS_URL = "/mifosng-provider/api/v1/funds";
    private static final String CREATE_FUNDS_URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;

    public static Integer createFund(final String fundJSON,
                                     final RequestSpecification requestSpec,
                                     final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_FUNDS_URL, fundJSON, "resourceId");
    }

    public static List<HashMap> retrieveAllFunds(final RequestSpecification requestSpec,
                                                 final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, URL, "");
    }

    public static String retrieveFund(final Integer fundID,
                                      final RequestSpecification requestSpec,
                                      final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "/" + fundID + "?" + Utils.TENANT_IDENTIFIER;
        HashMap map = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return new Gson().toJson(map);
    }

}
