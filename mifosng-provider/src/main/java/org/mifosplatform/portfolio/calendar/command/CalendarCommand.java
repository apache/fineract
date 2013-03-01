/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.command;

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
    private final String recurrence;
    @SuppressWarnings("unused")
    private final Integer remindById;
    @SuppressWarnings("unused")
    private final Integer firstReminder;
    @SuppressWarnings("unused")
    private final Integer secondReminder;

    public CalendarCommand(final String title, final String description, final String location, final LocalDate startDate,
            final LocalDate endDate, final LocalDate createdDate, final Integer duration, final Integer typeId, final boolean repeating,
            final String recurrence, final Integer remindById, final Integer firstReminder, final Integer secondReminder) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.duration = duration;
        this.typeId = typeId;
        this.repeating = repeating;
        this.recurrence = recurrence;
        this.remindById = remindById;
        this.firstReminder = firstReminder;
        this.secondReminder = secondReminder;
    }

}
