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
package org.apache.fineract.portfolio.calendar;

import java.util.HashSet;
import java.util.Set;

public class CalendarConstants {

    public static final String CALENDAR_RESOURCE_NAME = "calendar";

    public static enum CALENDAR_SUPPORTED_PARAMETERS {
        CALENDAR_ID("id"), ENTITY_TYPE("entityType"), ENTITY_ID("entityId"), TITLE("title"), DESCRIPTION("description"), LOCATION(
                "location"), START_DATE("startDate"), END_DATE("endDate"), CREATED_DATE("createdDate"), DURATION("duration"), TYPE_ID(
                "typeId"), REPEATING("repeating"), REMIND_BY_ID("remindById"), FIRST_REMINDER("firstReminder"), SECOND_REMINDER(
                "secondReminder"), LOCALE("locale"), DATE_FORMAT("dateFormat"), FREQUENCY("frequency"), INTERVAL("interval"), REPEATS_ON_DAY(
                "repeatsOnDay"), RESCHEDULE_BASED_ON_MEETING_DATES("reschedulebasedOnMeetingDates"), PRESENT_MEETING_DATE(
                "presentMeetingDate"), NEW_MEETING_DATE("newMeetingDate"),MEETING_TIME("meetingtime"),Time_Format("timeFormat"), REPEATS_ON_NTH_DAY_OF_MONTH("repeatsOnNthDayOfMonth"),
                REPEATS_ON_LAST_WEEKDAY_OF_MONTH("repeatsOnLastWeekdayOfMonth"), REPEATS_ON_DAY_OF_MONTH("repeatsOnDayOfMonth");

        private final String value;

        private CALENDAR_SUPPORTED_PARAMETERS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final CALENDAR_SUPPORTED_PARAMETERS param : CALENDAR_SUPPORTED_PARAMETERS.values()) {
                values.add(param.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().toString().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }

}
