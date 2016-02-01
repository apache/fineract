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
package org.apache.fineract.integrationtests.common;

import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class WorkingDaysHelper {

    private static final String WORKINGDAYS_URL = "/fineract-provider/api/v1/workingdays";

    public static Object updateWorkingDays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String UPDATE_WORKINGDAYS_URL = WORKINGDAYS_URL + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE WORKINGDAY---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_WORKINGDAYS_URL, updateWorkingDaysAsJson(), "");
    }

    public static Object updateWorkingDaysWithWrongRecurrence(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, String jsonAttributeToGetback) {
        final String UPDATE_WORKINGDAYS_URL = WORKINGDAYS_URL + "?" + Utils.TENANT_IDENTIFIER;
        System.out
                .println("---------------------------------UPDATE WORKINGDAY WITH WRONG RECURRENCE-----------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_WORKINGDAYS_URL, updateWorkingDayWithWrongRecur(),
                jsonAttributeToGetback);
    }

    public static String updateWorkingDaysAsJson() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("recurrence", "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU");
        map.put("locale", "en");
        map.put("repaymentRescheduleType", randomInt(1, 4));
        map.put("extendTermForDailyRepayments", false);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String updateWorkingDayWithWrongRecur() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("recurrence", "FREQ=WEEKLY;INTERVAL=1;BYDAY=MP,TI,TE,TH");
        map.put("locale", "en");
        map.put("repaymentRescheduleType", randomInt(1, 4));
        map.put("extendTermForDailyRepayments", false);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static int randomInt(int low, int high) {
        int i = new Random().nextInt(high) + low;
        return i;
    }

    public static int workingDaysId(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        HashMap<String, Object> workingDays = getAllWorkingDays(requestSpec, responseSpec);
        return (int) workingDays.get("id");
    }

    public static HashMap<String, Object> getAllWorkingDays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {

        return Utils.performServerGet(requestSpec, responseSpec, WORKINGDAYS_URL + "?" + Utils.TENANT_IDENTIFIER, "");

    }

}
