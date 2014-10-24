/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import static com.jayway.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class CalendarHelper {

    private static final String BASE_URL = "/mifosng-provider/api/v1/";
    private static final String PARENT_ENTITY_NAME = "groups/";
    private static final String ENITY_NAME = "/calendars";

    public static Integer createMeetingCalendarForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, 
    		final Integer groupId, final String startDate, final String frequency, final String interval, final String repeatsOnDay) {
    	
        System.out.println("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");
        
        final String CALENDAR_RESOURCE_URL = BASE_URL + PARENT_ENTITY_NAME + groupId + ENITY_NAME + "?"  + Utils.TENANT_IDENTIFIER;
        
        System.out.println(CALENDAR_RESOURCE_URL);
        
        return Utils.performServerPost(requestSpec, responseSpec, CALENDAR_RESOURCE_URL, getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate),
                "resourceId");
    }

    public static String getTestCalendarAsJSON(final String frequency, final String interval,final String repeatsOnDay,
    		final String startDate) {
    	
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("frequency", frequency);
        map.put("interval", interval);
        map.put("repeating", "true");
        map.put("repeatsOnDay", repeatsOnDay );
        map.put("title", Utils.randomNameGenerator("groups_CollectionMeeting", 4));
        map.put("typeId", "1");
        map.put("startDate", startDate);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static void verifyCalendarCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupId, final Integer generatedCalendarId) {
        System.out.println("------------------------------CHECK CALENDAR DETAILS------------------------------------\n");
        final String CLIENT_URL = "/mifosng-provider/api/v1/groups/" + generatedGroupId + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final String responseCalendarDetailsinJSON = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL, "collectionMeetingCalendar");
        final Integer responseCalendarId = from(responseCalendarDetailsinJSON).get("id");
        assertEquals("ERROR IN CREATING THE CALENDAR", generatedCalendarId, responseCalendarId);
    }
}