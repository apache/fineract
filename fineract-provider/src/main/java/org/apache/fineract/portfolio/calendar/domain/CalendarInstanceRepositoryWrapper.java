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
package org.apache.fineract.portfolio.calendar.domain;

import org.apache.fineract.portfolio.calendar.exception.CalendarInstanceNotFoundException;
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