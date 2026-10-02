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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.fineract.client.models.CalendarCreateRequest;
import org.apache.fineract.client.models.CalendarCreateResponse;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CalendarHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarHelper.class);

    private CalendarHelper() {}

    public static CalendarCreateResponse createMeetingCalendarForGroup(final Long groupId, final String startDate, final String frequency,
            final String interval, final String repeatsOnDay) {
        LOG.info("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().calendar().createCalendar("groups", groupId,
                buildCalendarRequest(frequency, interval, repeatsOnDay, startDate)));
    }

    public static CommandProcessingResult updateMeetingCalendarForGroup(final Long groupId, final String calendarID, final String startDate,
            final String frequency, final String interval, final String repeatsOnDay) {
        LOG.info("---------------------------------UPDATING A MEETING CALENDAR FOR THE GROUP------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().calendar().updateCalendar("groups", groupId,
                Long.parseLong(calendarID), buildCalendarJson(frequency, interval, repeatsOnDay, startDate)));
    }

    public static CalendarCreateResponse createMeetingForGroup(final Long groupId, final String startDate, final String frequency,
            final String interval, final String repeatsOnDay) {
        LOG.info("---------------------------------CREATING A MEETING CALENDAR FOR THE GROUP------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().calendar().createCalendar("centers", groupId,
                buildCalendarRequest(frequency, interval, repeatsOnDay, startDate)));
    }

    public static CommandProcessingResult updateMeetingCalendarForCenter(final Long centerId, final String calendarID, final String oldDate,
            final String startDate) {
        LOG.info("---------------------------------UPDATING A MEETING CALENDAR FOR THE CENTER------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().calendar().updateCalendar("centers", centerId,
                Long.parseLong(calendarID), buildRescheduleMeetingJson(oldDate, startDate)));
    }

    public static void verifyCalendarCreatedOnServer(final Long generatedGroupId, final Long generatedCalendarId) {
        LOG.info("------------------------------CHECK CALENDAR DETAILS------------------------------------\n");
        final Long id = ok(() -> FineractFeignClientHelper.getFineractFeignClient().calendar().retrieveCalendar(generatedCalendarId,
                "groups", generatedGroupId)).getId();
        assertEquals(generatedCalendarId, id, "ERROR IN CREATING THE CALENDAR");
    }

    private static CalendarCreateRequest buildCalendarRequest(final String frequency, final String interval, final String repeatsOnDay,
            final String startDate) {
        return new CalendarCreateRequest().dateFormat("dd MMMM yyyy").locale("en").frequency(frequency).interval(Integer.parseInt(interval))
                .repeating("true").repeatsOnDay(repeatsOnDay).title(Utils.randomStringGenerator("groups_CollectionMeeting", 4)).typeId(1)
                .startDate(startDate);
    }

    private static String buildCalendarJson(final String frequency, final String interval, final String repeatsOnDay,
            final String startDate) {
        return String.format(
                "{\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"frequency\":\"%s\",\"interval\":\"%s\","
                        + "\"repeating\":\"true\",\"repeatsOnDay\":\"%s\",\"title\":\"%s\",\"typeId\":\"1\",\"startDate\":\"%s\"}",
                frequency, interval, repeatsOnDay, Utils.randomStringGenerator("groups_CollectionMeeting", 4), startDate);
    }

    private static String buildRescheduleMeetingJson(final String oldDate, final String startDate) {
        return String.format("{\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"newMeetingDate\":\"%s\","
                + "\"presentMeetingDate\":\"%s\",\"reschedulebasedOnMeetingDates\":\"true\"}", startDate, oldDate);
    }
}
