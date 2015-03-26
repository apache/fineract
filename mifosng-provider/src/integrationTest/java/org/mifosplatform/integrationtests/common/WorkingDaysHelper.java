/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;


import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WorkingDaysHelper {
    private static final String WORKINGDAYS_URL = "/mifosng-provider/api/v1/workingdays";
    private static final String CREATE_HOLIDAY_URL = WORKINGDAYS_URL  + "?" + Utils.TENANT_IDENTIFIER;


    public static Object updateWorkingDays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String UPDATE_WORKINGDAYS_URL =  WORKINGDAYS_URL +"/" +workingDaysId(requestSpec,responseSpec) + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE WORKINGDAY---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec,UPDATE_WORKINGDAYS_URL, updateWorkingDaysAsJson(), "");
    }


    public static Object updateWorkingDaysWithWrongRecurrence(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,String jsonAttributeToGetback){
        final String UPDATE_WORKINGDAYS_URL =  WORKINGDAYS_URL +"/" +workingDaysId(requestSpec,responseSpec) + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE WORKINGDAY WITH WRONG RECURRENCE-----------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec,UPDATE_WORKINGDAYS_URL, updateWorkingDayWithWrongRecur(), jsonAttributeToGetback);
    }


    public static String updateWorkingDaysAsJson(){
        final HashMap<String, Object> map = new HashMap<>();
        map.put("recurrence", "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH");
        map.put("locale", "en");
        map.put("repaymentRescheduleType", randomInt(1,4));
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }


    public static String updateWorkingDayWithWrongRecur(){
        final HashMap<String, Object> map = new HashMap<>();
        map.put("recurrence", "FREQ=WEEKLY;INTERVAL=1;BYDAY=MP,TI,TE,TH");
        map.put("locale", "en");
        map.put("repaymentRescheduleType", randomInt(1,4));
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }


    public static int randomInt(int low, int high){
        int i = new Random().nextInt(high) + low;
        return i;
    }

    public static int workingDaysId(final RequestSpecification requestSpec,final ResponseSpecification responseSpec){
        List<HashMap<String, Object>> workingDaysId = getAllWorkingDays(requestSpec,responseSpec);
        final HashMap<String, Object> workingDay = workingDaysId.get(0);
        return (int) workingDay.get("id");
    }


    public static ArrayList<HashMap<String, Object>> getAllWorkingDays(
            final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {

        return Utils.performServerGet(requestSpec, responseSpec, WORKINGDAYS_URL + "?"
                + Utils.TENANT_IDENTIFIER, "");

    }

}
