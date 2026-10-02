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
package org.apache.fineract.portfolio.calendar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class CalendarReadPlatformServiceImplTest {

    private JdbcTemplate jdbcTemplate;
    private CalendarReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new CalendarReadPlatformServiceImpl(jdbcTemplate, mock(ConfigurationDomainService.class));
    }

    @Test
    void retrieveCalendarMapsNonRepeatingCalendarWithoutRecurrence() throws SQLException {
        final ResultSet rs = calendarRow(false, null);
        mapRowsWith(rs);

        final CalendarData calendar = service.retrieveCalendar(1L, 1L, 1);

        assertThat(calendar.isRepeating()).isFalse();
        assertThat(calendar.getRecurrence()).isNull();
        assertThat(calendar.getFrequency()).isNull();
        assertThat(calendar.getInterval()).isZero();
        assertThat(calendar.getRepeatsOnDay()).isNull();
        assertThat(calendar.getRepeatsOnNthDayOfMonth().getCode()).isEqualTo("nthDayType.invalid");
        assertThat(calendar.getRepeatsOnDayOfMonth()).isNull();
        assertThat(calendar.getHumanReadable()).isNull();
    }

    @Test
    void retrieveCalendarStillParsesRepeatingCalendarRecurrence() throws SQLException {
        final ResultSet rs = calendarRow(true, "FREQ=WEEKLY;INTERVAL=2;BYDAY=MO");
        mapRowsWith(rs);

        final CalendarData calendar = service.retrieveCalendar(1L, 1L, 1);

        assertThat(calendar.getFrequency().getCode()).isEqualTo("calendarFrequencyType.weekly");
        assertThat(calendar.getInterval()).isEqualTo(2);
        assertThat(calendar.getRepeatsOnDay().getCode()).isEqualTo("calendarWeekDaysType.monday");
    }

    private void mapRowsWith(final ResultSet rs) {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenAnswer(invocation -> ((RowMapper<?>) invocation.getArgument(1)).mapRow(rs, 0));
    }

    private static ResultSet calendarRow(final boolean repeating, final String recurrence) throws SQLException {
        final ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("typeId")).thenReturn(4);
        when(rs.getInt("entityTypeId")).thenReturn(2);
        when(rs.getString("title")).thenReturn("Calendar");
        when(rs.getBoolean("repeating")).thenReturn(repeating);
        when(rs.getString("recurrence")).thenReturn(recurrence);
        return rs;
    }
}
