/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class PeriodicAccrualAccountingHelper {

    private static final String PERIODIC_ACCRUAL_URL = "/mifosng-provider/api/v1/runaccruals";
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public PeriodicAccrualAccountingHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Object runPeriodicAccrualAccounting(String date) {
        String json = getRunPeriodicAccrual(date);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, PERIODIC_ACCRUAL_URL + "?" + Utils.TENANT_IDENTIFIER, json, "");
    }

    private String getRunPeriodicAccrual(String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("tillDate", date);
        return new Gson().toJson(map);
    }

}
