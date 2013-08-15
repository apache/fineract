/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.workingdays.domain;

import java.util.List;

import org.mifosplatform.organisation.workingdays.exception.WorkingDaysNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link WorkingDaysRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class WorkingDaysRepositoryWrapper {

    private final WorkingDaysRepository repository;

    @Autowired
    public WorkingDaysRepositoryWrapper(final WorkingDaysRepository repository) {
        this.repository = repository;
    }

    public WorkingDays findOne() {
        final List<WorkingDays> workingDaysList = this.repository.findAll();

        if (workingDaysList == null || workingDaysList.isEmpty()) { throw new WorkingDaysNotFoundException(); }
        return workingDaysList.get(0);
    }

    public void save(final WorkingDays workingDays) {
        this.repository.save(workingDays);
    }

    public void saveAndFlush(final WorkingDays workingDays) {
        this.repository.saveAndFlush(workingDays);
    }

    public void delete(final WorkingDays workingDays) {
        this.repository.delete(workingDays);
    }
}