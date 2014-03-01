/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.domain;

import java.util.Date;
import java.util.List;

import org.mifosplatform.organisation.holiday.exception.HolidayNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link HolidayRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class HolidayRepositoryWrapper {

    private final HolidayRepository repository;

    @Autowired
    public HolidayRepositoryWrapper(final HolidayRepository repository) {
        this.repository = repository;
    }

    public Holiday findOneWithNotFoundDetection(final Long id) {
        final Holiday holiday = this.repository.findOne(id);
        if (holiday == null) { throw new HolidayNotFoundException(id); }
        return holiday;
    }

    public void save(final Holiday holiday) {
        this.repository.save(holiday);
    }

    public void save(final Iterable<Holiday> holidays) {
        this.repository.save(holidays);
    }

    public void saveAndFlush(final Holiday holiday) {
        this.repository.saveAndFlush(holiday);
    }

    public void delete(final Holiday holiday) {
        this.repository.delete(holiday);
    }

    public List<Holiday> findByOfficeIdAndGreaterThanDate(final Long officeId, final Date date) {
        return this.repository.findByOfficeIdAndGreaterThanDate(officeId, date, HolidayStatusType.ACTIVE.getValue());
    }

    public List<Holiday> findUnprocessed() {
        return this.repository.findUnprocessed(HolidayStatusType.ACTIVE.getValue());
    }
}