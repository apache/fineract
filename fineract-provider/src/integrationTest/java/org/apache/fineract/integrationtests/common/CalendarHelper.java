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

import static com.jayway.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class CalendarHelper {

    private static final String BASE_URL = "/fineract-provider/api/v1/";
    private static final String PARENT_ENTITY_NAME = "groups/";
    private static final String ENITY_NAME = "/calendars";
    private static final String Center_Entity = "centers/";
    private static final String Edit_Calendar = "editcalendarbasedonmeetingdates/";

    public static Integer createMeetingCalendarForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupId, final String startDate, final String frequency, final String interval, final String repeatsOnDay) {

        System.out.println("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + PARENT_ENTITY_NAME + groupId + ENITY_NAME + "?" + Utils.TENANT_IDENTIFIER;

        System.out.println(CALENDAR_RESOURCE_URL);

        return Utils.performServerPost(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
                getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    public static Integer updateMeetingCalendarForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupId, String calendarID, final String startDate, final String frequency, final String interval,
            final String repeatsOnDay) {

        System.out.println("---------------------------------UPDATING A MEETING CALENDAR FOR THE GROUP------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + PARENT_ENTITY_NAME + groupId + ENITY_NAME + "/" + calendarID;

        System.out.println(CALENDAR_RESOURCE_URL);
        // TODO: check that resource id indeed exists in calendar update put.
        return Utils.performServerPut(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
                getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    public static String getTestCalendarAsJSON(final String frequency, final String interval, final String repeatsOnDay,
            final String startDate) {

        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("frequency", frequency);
        map.put("interval", interval);
        map.put("repeating", "true");
        map.put("repeatsOnDay", repeatsOnDay);
        map.put("title", Utils.randomNameGenerator("groups_CollectionMeeting", 4));
        map.put("typeId", "1");
        map.put("startDate", startDate);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static void verifyCalendarCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupId, final Integer generatedCalendarId) {
        System.out.println("------------------------------CHECK CALENDAR DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/groups/" + generatedGroupId + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final String responseCalendarDetailsinJSON = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL,
                "collectionMeetingCalendar");
        final Integer responseCalendarId = from(responseCalendarDetailsinJSON).get("id");
        assertEquals("ERROR IN CREATING THE CALENDAR", generatedCalendarId, responseCalendarId);
    }
    
    public static Integer createMeetingForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
    final Integer groupId, final String startDate, final String frequency, final String interval, final String repeatsOnDay) {

    	System.out.println("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");

    	final String CALENDAR_RESOURCE_URL = BASE_URL + Center_Entity + groupId + ENITY_NAME + "?" + Utils.TENANT_IDENTIFIER;

    	System.out.println(CALENDAR_RESOURCE_URL);

    	return Utils.performServerPost(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
    			getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    public static Integer updateMeetingCalendarForCenter(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer centerId, String calendarID, String oldDate, String startDate) {

        System.out.println("---------------------------------UPADATING A MEETING CALENDAR FOR THE CENTER------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + Center_Entity + centerId + ENITY_NAME + "/" + calendarID + "?"
                + Utils.TENANT_IDENTIFIER;

        System.out.println(CALENDAR_RESOURCE_URL);

        return Utils.performServerPut(requestSpec, responseSpec, CALENDAR_RESOURCE_URL, getTestCalendarMeetingAsJSON(oldDate, startDate),
                "resourceId");

    }

    private static String getTestCalendarMeetingAsJSON(String oldDate, String startDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("newMeetingDate", startDate);
        map.put("presentMeetingDate", oldDate);
        map.put("reschedulebasedOnMeetingDates", "true");
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
}