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
import jakarta.validation.constraints.NotNull;
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
public class MeetingUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    // @NotNull(message = "{org.apache.fineract.portfolio.meeting.id.not-null}")
    private Long id;
    @Hidden
    private Long entityId;
    @Hidden
    private CalendarEntityType entityType;
    @NotNull(message = "{org.apache.fineract.portfolio.meeting.calendar-id.not-null}")
    private Long calendarId;
    @NotNull(message = "{org.apache.fineract.portfolio.meeting.meeting-date.not-null}")
    private String meetingDate;
    @NotNull(message = "{org.apache.fineract.portfolio.meeting.date-format.not-null}")
    private String dateFormat;
    @NotNull(message = "{org.apache.fineract.portfolio.meeting.locale.not-null}")
    private String locale;
    private List<MeetingAttendanceData> clientsAttendance;
}
