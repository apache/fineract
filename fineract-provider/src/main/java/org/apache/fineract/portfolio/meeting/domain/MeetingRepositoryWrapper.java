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
package org.apache.fineract.portfolio.meeting.domain;

import org.apache.fineract.portfolio.meeting.exception.MeetingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link MeetingRepository} that is responsible for checking if
 * {@link Meeting} is returned when using <code>findOne</code> repository method
 * and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link MeetingRepository} is required.
 * </p>
 */
@Service
public class MeetingRepositoryWrapper {

    private final MeetingRepository repository;

    @Autowired
    public MeetingRepositoryWrapper(final MeetingRepository repository) {
        this.repository = repository;
    }

    public Meeting findOneWithNotFoundDetection(final Long meetingId) {
        final Meeting meeting = this.repository.findOne(meetingId);
        if (meeting == null) { throw new MeetingNotFoundException(meetingId); }
        return meeting;
    }

    public void save(final Meeting meeting) {
        this.repository.save(meeting);
    }

    public void delete(final Meeting meeting) {
        this.repository.delete(meeting);
    }

    public void saveAndFlush(final Meeting meeting) {
        this.repository.saveAndFlush(meeting);
    }
}