/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mifosplatform.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes" })
public class HolidayHelper {

    private static final String HOLIDAYS_URL = "/mifosng-provider/api/v1/holidays";
    private static final String CREATE_HOLIDAY_URL = HOLIDAYS_URL + "?" + Utils.TENANT_IDENTIFIER;

    private static final String OFFICE_ID = "1";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public HolidayHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static String getCreateHolidayDataAsJSON() {
        final HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, String>> offices = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> officeMap = new HashMap<>();
        officeMap.put("officeId", OFFICE_ID);
        offices.add(officeMap);

        map.put("offices", offices);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("name", Utils.randomNameGenerator("HOLIDAY_", 5));
        map.put("fromDate", "01 April 2013");
        map.put("toDate", "01 April 2013");
        map.put("repaymentsRescheduledTo", "08 April 2013");

        String HolidayCreateJson = new Gson().toJson(map);
        System.out.println(HolidayCreateJson);
        return HolidayCreateJson;
    }
    
    public static String getActivateHolidayDataAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String activateHoliday = new Gson().toJson(map);
        System.out.println(activateHoliday);
        return activateHoliday;
    }

    public static Integer createHolidays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_HOLIDAY_URL, getCreateHolidayDataAsJSON(), "resourceId");
    }
    
    public static Integer activateHolidays(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String holidayID) {
        final String ACTIVATE_HOLIDAY_URL = HOLIDAYS_URL + "/" + holidayID + "?command=activate&" + Utils.TENANT_IDENTIFIER; 
        return Utils.performServerPost(requestSpec, responseSpec, ACTIVATE_HOLIDAY_URL, getActivateHolidayDataAsJSON(), "resourceId");
    }
    
    public static HashMap getHolidayById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String holidayID) {
        final String GET_HOLIDAY_BY_ID_URL = HOLIDAYS_URL + "/" + holidayID + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING HOLIDAY BY ID -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_HOLIDAY_BY_ID_URL, "");
        return response;
    }

}