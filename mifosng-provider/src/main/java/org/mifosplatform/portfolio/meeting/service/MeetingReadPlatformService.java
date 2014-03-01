/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.portfolio.meeting.data.MeetingData;

public interface MeetingReadPlatformService {

    MeetingData retrieveMeeting(final Long meetingId, Long entityId, Integer entityTypeId);

    Collection<MeetingData> retrieveMeetingsByEntity(final Long entityId, final Integer entityTypeId, Integer limit);

    Collection<MeetingData> retrieveMeetingsByEntityByCalendarType(final Long entityId, final Integer entityTypeId,
            final List<Integer> calendarTypeOptions);

    MeetingData retrieveLastMeeting(Long calendarInstanceId);
}
