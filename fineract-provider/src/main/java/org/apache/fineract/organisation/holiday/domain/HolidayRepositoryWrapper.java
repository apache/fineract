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
package org.apache.fineract.organisation.holiday.domain;

import java.util.Date;
import java.util.List;

import org.apache.fineract.organisation.holiday.exception.HolidayNotFoundException;
import org.apache.fineract.organisation.holiday.service.HolidayUtil;
import org.joda.time.LocalDate;
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

    public boolean isHoliday(Long officeId, LocalDate transactionDate) {
        final List<Holiday> holidays = findByOfficeIdAndGreaterThanDate(officeId, transactionDate.toDate());
        return HolidayUtil.isHoliday(transactionDate, holidays);
    }
}