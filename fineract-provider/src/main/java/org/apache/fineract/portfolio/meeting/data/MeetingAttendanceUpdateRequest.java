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
package org.apache.fineract.portfolio.meeting.data;

import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingAttendanceUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @Hidden
    private Long entityId;
    @Hidden
    private CalendarEntityType entityType;
    @Hidden
    // @NotNull(message = "{org.apache.fineract.portfolio.meeting.attendance.id.not-null}")
    private List<MeetingAttendanceData> meetingAttendance;
    private Integer attendanceType;
}
