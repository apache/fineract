/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.domain;

import org.mifosplatform.portfolio.meeting.exception.MeetingNotFoundException;
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