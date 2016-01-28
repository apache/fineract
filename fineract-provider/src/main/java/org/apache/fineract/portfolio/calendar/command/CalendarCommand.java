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
package org.apache.fineract.portfolio.calendar.command;

import org.joda.time.LocalDate;

public class CalendarCommand {

    @SuppressWarnings("unused")
    private final String title;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final String location;
    @SuppressWarnings("unused")
    private final LocalDate startDate;
    @SuppressWarnings("unused")
    private final LocalDate endDate;
    @SuppressWarnings("unused")
    private final LocalDate createdDate;
    @SuppressWarnings("unused")
    private final Integer duration;
    @SuppressWarnings("unused")
    private final Integer typeId;
    @SuppressWarnings("unused")
    private final boolean repeating;
    @SuppressWarnings("unused")
    private final Integer remindById;
    @SuppressWarnings("unused")
    private final Integer firstReminder;
    @SuppressWarnings("unused")
    private final Integer secondReminder;

    public CalendarCommand(final String title, final String description, final String location, final LocalDate startDate,
            final LocalDate endDate, final LocalDate createdDate, final Integer duration, final Integer typeId, final boolean repeating,
            final Integer remindById, final Integer firstReminder, final Integer secondReminder) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.duration = duration;
        this.typeId = typeId;
        this.repeating = repeating;
        this.remindById = remindById;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
    }

}
