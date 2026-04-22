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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalendarHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarHelper.class);
    private static final String BASE_URL = "/fineract-provider/api/v1/";
    private static final String PARENT_ENTITY_NAME = "groups/";
    private static final String ENITY_NAME = "/calendars";
    private static final String CENTER_ENTITY = "centers/";
    private static final String EDIT_CALENDAR = "editcalendarbasedonmeetingdates/";

    private CalendarHelper() {

    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createMeetingCalendarForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupId, final String startDate, final String frequency, final String interval, final String repeatsOnDay) {

        LOG.info("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + PARENT_ENTITY_NAME + groupId + ENITY_NAME + "?" + Utils.TENANT_IDENTIFIER;

        LOG.info("{}", CALENDAR_RESOURCE_URL);

        return Utils.performServerPost(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
                getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateMeetingCalendarForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupId, String calendarID, final String startDate, final String frequency, final String interval,
            final String repeatsOnDay) {

        LOG.info("---------------------------------UPDATING A MEETING CALENDAR FOR THE GROUP------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + PARENT_ENTITY_NAME + groupId + ENITY_NAME + "/" + calendarID;

        LOG.info("{}", CALENDAR_RESOURCE_URL);
        // TODO: check that resource id indeed exists in calendar update put.
        return Utils.performServerPut(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
                getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestCalendarAsJSON(final String frequency, final String interval, final String repeatsOnDay,
            final String startDate) {

        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("frequency", frequency);
        map.put("interval", interval);
        map.put("repeating", "true");
        map.put("repeatsOnDay", repeatsOnDay);
        map.put("title", Utils.randomStringGenerator("groups_CollectionMeeting", 4));
        map.put("typeId", "1");
        map.put("startDate", startDate);
        LOG.info("map : {}", map);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static void verifyCalendarCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedGroupId, final Integer generatedCalendarId) {
        LOG.info("------------------------------CHECK CALENDAR DETAILS------------------------------------\n");
        final String CLIENT_URL = "/fineract-provider/api/v1/groups/" + generatedGroupId + "?associations=all&" + Utils.TENANT_IDENTIFIER;
        final String responseCalendarDetailsinJSON = Utils.performServerGet(requestSpec, responseSpec, CLIENT_URL,
                "collectionMeetingCalendar");
        final Integer responseCalendarId = JsonPath.from(responseCalendarDetailsinJSON).get("id");
        assertEquals(generatedCalendarId, responseCalendarId, "ERROR IN CREATING THE CALENDAR");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createMeetingForGroup(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer groupId, final String startDate, final String frequency, final String interval, final String repeatsOnDay) {

        LOG.info("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + CENTER_ENTITY + groupId + ENITY_NAME + "?" + Utils.TENANT_IDENTIFIER;

        LOG.info("{}", CALENDAR_RESOURCE_URL);

        return Utils.performServerPost(requestSpec, responseSpec, CALENDAR_RESOURCE_URL,
                getTestCalendarAsJSON(frequency, interval, repeatsOnDay, startDate), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateMeetingCalendarForCenter(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Integer centerId, String calendarID, String oldDate, String startDate) {

        LOG.info("---------------------------------UPADATING A MEETING CALENDAR FOR THE CENTER------------------------------");

        final String CALENDAR_RESOURCE_URL = BASE_URL + CENTER_ENTITY + centerId + ENITY_NAME + "/" + calendarID + "?"
                + Utils.TENANT_IDENTIFIER;

        LOG.info("{}", CALENDAR_RESOURCE_URL);

        return Utils.performServerPut(requestSpec, responseSpec, CALENDAR_RESOURCE_URL, getTestCalendarMeetingAsJSON(oldDate, startDate),
                "resourceId");

    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String getTestCalendarMeetingAsJSON(String oldDate, String startDate) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("newMeetingDate", startDate);
        map.put("presentMeetingDate", oldDate);
        map.put("reschedulebasedOnMeetingDates", "true");
        LOG.info("map : {}", map);
        return new Gson().toJson(map);
    }
}
