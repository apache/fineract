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
package org.apache.fineract.organisation.workingdays.domain;

import java.util.List;

import org.apache.fineract.organisation.workingdays.exception.WorkingDaysNotFoundException;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysUtil;
import org.joda.time.LocalDate;
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

    public boolean isWorkingDay(LocalDate transactionDate) {
        final WorkingDays workingDays = findOne();
        return WorkingDaysUtil.isWorkingDay(workingDays, transactionDate);
    }
}