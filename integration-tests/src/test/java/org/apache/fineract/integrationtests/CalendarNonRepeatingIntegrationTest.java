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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/** A non-repeating calendar has no recurrence rule; reading it must not fail, also once it has history rows. */
public class CalendarNonRepeatingIntegrationTest {

    private static final String START_DATE = "04 March 2011";

    @Test
    public void readsNonRepeatingCalendarBeforeAndAfterAnEdit() {
        final FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        final Long groupId = new FeignGroupHelper(client).createActiveGroup().getResourceId();
        final Long calendarId = CalendarHelper.createNonRepeatingCalendarForGroup(groupId, START_DATE).getResourceId();

        CalendarHelper.verifyCalendarCreatedOnServer(groupId, calendarId);

        // any update writes an m_calendar_history row, which the read then walks
        final String title = Utils.uniqueRandomStringGenerator("General_", 5);
        CalendarHelper.updateCalendarTitleForGroup(groupId, calendarId, title);

        assertEquals(title, ok(() -> client.calendar().retrieveCalendar(calendarId, "groups", groupId)).getTitle());
    }
}
