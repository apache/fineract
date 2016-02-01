/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import org.mifosplatform.portfolio.calendar.exception.CalendarInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CalendarInstanceRepository} that is responsible for
 * checking if {@link CalendarInstance} is returned when using
 * <code>findOne</code> repository method and throwing an appropriate not found
 * exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link CalendarInstanceRepository} is required.
 * </p>
 */
@Service
public class CalendarInstanceRepositoryWrapper {

    private final CalendarInstanceRepository repository;

    @Autowired
    public CalendarInstanceRepositoryWrapper(final CalendarInstanceRepository repository) {
        this.repository = repository;
    }

    public CalendarInstance findOneWithNotFoundDetection(final Long CalendarInstanceId) {
        final CalendarInstance calendatInstance = this.repository.findOne(CalendarInstanceId);
        if (calendatInstance == null) { throw new CalendarInstanceNotFoundException(CalendarInstanceId); }
        return calendatInstance;
    }

    public void save(final CalendarInstance calendatInstance) {
        this.repository.save(calendatInstance);
    }

    public void delete(final CalendarInstance calendatInstance) {
        this.repository.delete(calendatInstance);
    }

    public void saveAndFlush(final CalendarInstance calendatInstance) {
        this.repository.saveAndFlush(calendatInstance);
    }
}