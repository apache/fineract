/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.attendance.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.meeting.attendance.AttendanceType;
import org.springframework.stereotype.Service;

@Service
public class AttendanceDropdownReadPlatformServiceImpl implements AttendanceDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveAttendanceTypeOptions() {
        return AttendanceEnumerations.attendanceType(AttendanceType.values());
    }

}
