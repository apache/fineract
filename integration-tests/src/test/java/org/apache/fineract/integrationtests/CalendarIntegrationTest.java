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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CalendarCreateRequest;
import org.apache.fineract.client.models.CalendarUpdateRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/** Creating and updating a calendar through the typed command pipeline. */
public class CalendarIntegrationTest {

    private static final String START_DATE = "04 March 2011";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final Long HEAD_OFFICE_ID = 1L;

    private final FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();

    @Test
    public void createKeepsEveryFieldAndUpdateChangesOnlyWhatIsSent() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();
        final Long calendarId = ok(() -> client.calendar().createCalendar("groups", groupId, weeklyMeeting()
                .description("Weekly collection").location("Village hall").duration(60).remindById(1).firstReminder(10).secondReminder(5)))
                .getResourceId();

        var calendar = ok(() -> client.calendar().retrieveCalendar(calendarId, "groups", groupId));
        assertEquals("Weekly collection", calendar.getDescription());
        assertEquals("Village hall", calendar.getLocation());
        assertEquals(60, calendar.getDuration());
        assertEquals(10, calendar.getFirstReminder());
        assertEquals(5, calendar.getSecondReminder());
        assertEquals("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO", calendar.getRecurrence());

        // only the interval is sent: the day and the other fields stay as they are
        ok(() -> client.calendar().updateCalendar("groups", groupId, calendarId, new CalendarUpdateRequest().interval(3)));

        calendar = ok(() -> client.calendar().retrieveCalendar(calendarId, "groups", groupId));
        assertEquals("FREQ=WEEKLY;INTERVAL=3;BYDAY=MO", calendar.getRecurrence());
        assertEquals("Weekly collection", calendar.getDescription());

        // an empty description clears it
        ok(() -> client.calendar().updateCalendar("groups", groupId, calendarId, new CalendarUpdateRequest().description("")));

        calendar = ok(() -> client.calendar().retrieveCalendar(calendarId, "groups", groupId));
        assertEquals(null, calendar.getDescription());
        assertEquals("Village hall", calendar.getLocation());
    }

    @Test
    public void createOfARepeatingCalendarWithoutFrequencyIsRejected() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();

        final CallFailedRuntimeException failure = fail(
                () -> client.calendar().createCalendar("groups", groupId, weeklyMeeting().frequency(null)));

        assertEquals(400, failure.getStatus());
        assertTrue(failure.getResponseBody().contains("frequency"), failure.getResponseBody());
    }

    @Test
    public void createWithoutTitleIsRejected() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();

        final CallFailedRuntimeException failure = fail(
                () -> client.calendar().createCalendar("groups", groupId, weeklyMeeting().title(null)));

        assertEquals(400, failure.getStatus());
    }

    @Test
    public void userWithoutCalendarPermissionsIsRejected() {
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();
        final Long calendarId = ok(() -> client.calendar().createCalendar("groups", groupId, weeklyMeeting())).getResourceId();
        final FineractFeignClient user = createUserWithPermissions(Map.of("READ_GROUP", true, "READ_CALENDAR", true));

        assertThrows(FeignException.Forbidden.class, () -> user.calendar().createCalendar("groups", groupId, weeklyMeeting()));
        assertThrows(FeignException.Forbidden.class,
                () -> user.calendar().updateCalendar("groups", groupId, calendarId, new CalendarUpdateRequest().interval(3)));
    }

    private static CalendarCreateRequest weeklyMeeting() {
        return new CalendarCreateRequest().title(Utils.randomStringGenerator("groups_CollectionMeeting", 4)).typeId(1).startDate(START_DATE)
                .repeating(true).frequency(2).interval(2).repeatsOnDay(1).dateFormat("dd MMMM yyyy").locale("en");
    }

    private static FineractFeignClient createUserWithPermissions(final Map<String, Boolean> permissions) {
        final String username = Utils.uniqueRandomStringGenerator("usr", 8);
        final Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(HEAD_OFFICE_ID).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
