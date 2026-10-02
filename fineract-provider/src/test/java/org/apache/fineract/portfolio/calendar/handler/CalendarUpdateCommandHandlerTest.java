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
package org.apache.fineract.portfolio.calendar.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.fineract.portfolio.calendar.command.CalendarUpdateCommand;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarUpdateResponse;
import org.apache.fineract.portfolio.calendar.service.CalendarWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarUpdateCommandHandlerTest {

    @Mock
    private CalendarWritePlatformService writeService;

    @InjectMocks
    private CalendarUpdateCommandHandler handler;

    @Test
    void dispatchesToService() {
        CalendarUpdateRequest req = CalendarUpdateRequest.builder().calendarId(1L).title("Updated Title").build();
        CalendarUpdateCommand cmd = new CalendarUpdateCommand();
        cmd.setPayload(req);
        CalendarUpdateResponse expected = CalendarUpdateResponse.builder().resourceId(1L).changes(Map.of("title", "Updated Title")).build();
        when(writeService.updateCalendar(any())).thenReturn(expected);

        CalendarUpdateResponse result = handler.handle(cmd);

        assertEquals(1L, result.getResourceId());
        verify(writeService).updateCalendar(req);
    }
}
