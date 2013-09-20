/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import org.mifosplatform.portfolio.calendar.exception.CalendarNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CalendarRepository} that is responsible for checking if
 * {@link Calendar} is returned when using <code>findOne</code> repository
 * method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link CalendarRepository} is required.
 * </p>
 */
@Service
public class CalendarRepositoryWrapper {

    private final CalendarRepository repository;

    @Autowired
    public CalendarRepositoryWrapper(final CalendarRepository repository) {
        this.repository = repository;
    }

    public Calendar findOneWithNotFoundDetection(final Long calendarId) {
        final Calendar calendar = this.repository.findOne(calendarId);
        if (calendar == null) { throw new CalendarNotFoundException(calendarId); }
        return calendar;
    }

    public void save(final Calendar calendar) {
        this.repository.save(calendar);
    }

    public void delete(final Calendar calendar) {
        this.repository.delete(calendar);
    }

    public void saveAndFlush(final Calendar calendar) {
        this.repository.saveAndFlush(calendar);
    }
}