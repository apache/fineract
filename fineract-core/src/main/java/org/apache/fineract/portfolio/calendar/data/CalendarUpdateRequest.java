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
package org.apache.fineract.portfolio.calendar.data;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private String entityType;
    @Hidden
    private Long entityId;
    @Hidden
    private Long calendarId;

    @Length(max = 100, message = "{org.apache.fineract.portfolio.calendar.title.max}")
    private String title;

    private String description;
    private String location;

    private String startDate;
    private String endDate;
    private Integer duration;

    @Positive(message = "{org.apache.fineract.portfolio.calendar.type-id.positive}")
    private Integer typeId;

    private String repeating;
    private String frequency;
    private Integer interval;
    private String repeatsOnDay;
    private Integer repeatsOnNthDayOfMonth;

    private Integer remindBy;
    private Integer firstReminder;
    private Integer secondReminder;
    private String meetingtime;

    private Boolean rescheduleBasedOnMeetingDates;
    private String presentMeetingDate;
    private String newMeetingDate;

    private String locale;
    private String dateFormat;
    private String timeFormat;
}
