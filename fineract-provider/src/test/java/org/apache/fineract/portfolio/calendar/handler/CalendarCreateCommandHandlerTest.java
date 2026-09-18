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

import org.apache.fineract.portfolio.calendar.command.CalendarCreateCommand;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateRequest;
import org.apache.fineract.portfolio.calendar.data.CalendarCreateResponse;
import org.apache.fineract.portfolio.calendar.service.CalendarWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarCreateCommandHandlerTest {

    @Mock
    private CalendarWritePlatformService writeService;

    @InjectMocks
    private CalendarCreateCommandHandler handler;

    @Test
    void dispatchesToService() {
        CalendarCreateRequest req = CalendarCreateRequest.builder().title("t").startDate("01 January 2024").typeId(1).build();
        CalendarCreateCommand cmd = new CalendarCreateCommand();
        cmd.setPayload(req);
        CalendarCreateResponse expected = CalendarCreateResponse.builder().resourceId(42L).build();
        when(writeService.createCalendar(any())).thenReturn(expected);

        CalendarCreateResponse result = handler.handle(cmd);

        assertEquals(42L, result.getResourceId());
        verify(writeService).createCalendar(req);
    }
}
