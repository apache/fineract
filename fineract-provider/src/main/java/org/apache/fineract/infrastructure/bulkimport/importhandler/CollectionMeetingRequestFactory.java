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
package org.apache.fineract.infrastructure.bulkimport.importhandler;

import java.time.format.DateTimeFormatter;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.exception.CalendarEntityTypeNotSupportedException;

/**
 * Builds the {@link CalendarCreateRequest} for a center/group's collection meeting during bulk import. Shared by
 * {@code CenterImportHandler} and {@code GroupImportHandler}, which otherwise duplicated this builder block.
 */
public final class CollectionMeetingRequestFactory {

    private CollectionMeetingRequestFactory() {

    }

    public static CalendarCreateRequest build(final CalendarData calendarData, final String entityType, final Long entityId,
            final String title, final String dateFormat) {
        final CalendarEntityType calendarEntityType = CalendarEntityType.getEntityType(entityType);
        if (calendarEntityType == null) {
            throw new CalendarEntityTypeNotSupportedException(entityType);
        }

        final String startDate = calendarData.getStartDate() != null
                ? calendarData.getStartDate().format(DateTimeFormatter.ofPattern(dateFormat))
                : null;

        final CalendarCreateRequest.CalendarCreateRequestBuilder builder = CalendarCreateRequest.builder().entityType(calendarEntityType)
                .entityId(entityId).title(title).description(calendarData.getDescription()).startDate(startDate)
                .typeId(calendarData.getTypeId() != null ? Integer.valueOf(calendarData.getTypeId()) : null)
                .repeating(calendarData.isRepeating()).interval(calendarData.getInterval())
                .repeatsOnDay(calendarData.getRepeatsOnDay() != null ? calendarData.getRepeatsOnDay().getId().intValue() : null)
                .dateFormat(dateFormat).locale(calendarData.getLocale());

        if (calendarData.isRepeating() && calendarData.getFrequency() != null) {
            builder.frequency(calendarData.getFrequency().getId().intValue());
        }

        return builder.build();
    }
}
