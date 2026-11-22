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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating a calendar (meeting) for a client, group, center or loan. The single-field limits are checked
 * here; the rules that depend on more than one field (dates that must parse with {@code dateFormat} and {@code locale},
 * the frequency and day a repeating calendar needs) are checked by the write service.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // set from the path parameters, never part of the request body
    @Hidden
    private String entityType;
    @Hidden
    private Long entityId;

    @NotBlank(message = "{org.apache.fineract.portfolio.calendar.title.not-blank}")
    @Size(max = 50, message = "{org.apache.fineract.portfolio.calendar.title.size}")
    private String title;
    @Size(max = 100, message = "{org.apache.fineract.portfolio.calendar.description.size}")
    private String description;
    @Size(max = 50, message = "{org.apache.fineract.portfolio.calendar.location.size}")
    private String location;

    @NotBlank(message = "{org.apache.fineract.portfolio.calendar.start-date.not-blank}")
    private String startDate;
    private String endDate;
    private Integer duration;

    @NotNull(message = "{org.apache.fineract.portfolio.calendar.type-id.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.calendar.type-id.range}")
    @Max(value = 4, message = "{org.apache.fineract.portfolio.calendar.type-id.range}")
    private Integer typeId;

    private Boolean repeating;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.calendar.frequency.range}")
    @Max(value = 4, message = "{org.apache.fineract.portfolio.calendar.frequency.range}")
    private Integer frequency;
    @Positive(message = "{org.apache.fineract.portfolio.calendar.interval.positive}")
    private Integer interval;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.calendar.repeats-on-day.range}")
    @Max(value = 7, message = "{org.apache.fineract.portfolio.calendar.repeats-on-day.range}")
    private Integer repeatsOnDay;
    private Integer repeatsOnNthDayOfMonth;
    private Integer repeatsOnLastWeekdayOfMonth;
    private Integer repeatsOnDayOfMonth;

    @Min(value = 1, message = "{org.apache.fineract.portfolio.calendar.remind-by-id.range}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.calendar.remind-by-id.range}")
    private Integer remindById;
    @Positive(message = "{org.apache.fineract.portfolio.calendar.first-reminder.positive}")
    private Integer firstReminder;
    @Positive(message = "{org.apache.fineract.portfolio.calendar.second-reminder.positive}")
    private Integer secondReminder;
    private String meetingtime;
    private String timeFormat;

    private String locale;
    private String dateFormat;
}
