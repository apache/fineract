/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.funds;

import com.google.common.reflect.TypeToken;
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

    public static List<FundsHelper> retrieveAllFunds(final RequestSpecification requestSpec,
                                                                 final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;
        List<HashMap<String, Object>> list = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        final String jsonData = new Gson().toJson(list);
        return new Gson().fromJson(jsonData, new TypeToken<List<FundsHelper>>(){}.getType());
    }

    public static String retrieveFund(final Long fundID,
                                      final RequestSpecification requestSpec,
                                      final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "/" + fundID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return new Gson().toJson(response);
    }

    public static FundsHelper updateFund(final Long fundID,
                                         final String newName,
                                         final String newExternalId,
                                         final RequestSpecification requestSpec,
                                         final ResponseSpecification responseSpec) {
        FundsHelper fh = FundsHelper.create(newName).externalId(newExternalId).build();
        String updateJSON = new Gson().toJson(fh);

        final String URL = FUNDS_URL + "/" + fundID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap<String, String> response = Utils.performServerPut(requestSpec, responseSpec, URL, updateJSON, "changes");
        final String jsonData = new Gson().toJson(response);
        return new Gson().fromJson(jsonData, FundsHelper.class);
    }

}
