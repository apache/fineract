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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.MeetingCreateRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.Test;

public class CalendarDeleteIntegrationTest {

    private static final String START_DATE = "04 March 2011";

    private final FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void deletesCalendarOfGroup() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();
        final Long calendarId = CalendarHelper.createNonRepeatingCalendarForGroup(groupId, START_DATE).getResourceId();
        CalendarHelper.verifyCalendarCreatedOnServer(groupId, calendarId);

        assertEquals(calendarId, ok(() -> client.calendar().deleteCalendar("groups", groupId, calendarId)).getResourceId());

        final CallFailedRuntimeException exception = fail(() -> client.calendar().retrieveCalendar(calendarId, "groups", groupId));
        assertEquals(404, exception.getStatus());
    }

    @Test
    public void rejectsDeletingCalendarWithRecordedMeetings() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();
        // weekly on Fridays; START_DATE is a Friday, so it is a meeting date
        final Long calendarId = CalendarHelper.createMeetingCalendarForGroup(groupId, START_DATE, "2", "1", "5").getResourceId();
        ok(() -> client.meetings().createMeeting("groups", groupId,
                new MeetingCreateRequest().calendarId(calendarId).meetingDate(START_DATE).dateFormat("dd MMMM yyyy").locale("en")));

        final CallFailedRuntimeException exception = fail(() -> client.calendar().deleteCalendar("groups", groupId, calendarId));
        assertEquals(403, exception.getStatus());
        assertTrue(exception.getResponseBody().contains("error.msg.calendar.delete.not.allowed.has.meetings"), exception.getResponseBody());
        CalendarHelper.verifyCalendarCreatedOnServer(groupId, calendarId);
    }

    @Test
    public void deletingUnknownCalendarReturnsNotFound() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();

        final CallFailedRuntimeException exception = fail(() -> client.calendar().deleteCalendar("groups", groupId, Long.MAX_VALUE));
        assertEquals(404, exception.getStatus());
    }
}
