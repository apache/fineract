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
package org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class AvroDateTimeMapperTest {

    private AvroDateTimeMapper underTest = new AvroDateTimeMapper();

    @Test
    public void testMapOffsetDateTimeShouldReturnNullIfNullGiven() {
        // given
        // when
        String result = underTest.mapOffsetDateTime(null);
        // then
        assertThat(result).isNull();
    }

    @Test
    public void testMapOffsetDateTimeShouldReturnStringInIsoOffsetDateTimeFormat() {
        // given
        String expected = "2022-09-01T14:10:21.000000123+02:00";
        OffsetDateTime source = OffsetDateTime.of(2022, 9, 1, 14, 10, 21, 123, ZoneOffset.of("+02:00"));
        // when
        String result = underTest.mapOffsetDateTime(source);
        // then
        assertThat(result).isEqualTo(expected);
        OffsetDateTime parsedDateTime = OffsetDateTime.parse(result, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertThat(parsedDateTime).isEqualTo(source);
    }

    @Test
    public void testMapLocalDateTimeShouldReturnNullIfNullGiven() {
        // given
        // when
        String result = underTest.mapLocalDateTime(null);
        // then
        assertThat(result).isNull();
    }

    @Test
    public void testMapLocalDateTimeShouldReturnStringInIsoLocalDateTimeFormat() {
        // given
        String expected = "2022-09-01T14:10:21.000000123";
        LocalDateTime source = LocalDateTime.of(2022, 9, 1, 14, 10, 21, 123);
        // when
        String result = underTest.mapLocalDateTime(source);
        // then
        assertThat(result).isEqualTo(expected);
        LocalDateTime parsedDateTime = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(parsedDateTime).isEqualTo(source);
    }

    @Test
    public void testMapLocalDateShouldReturnNullIfNullGiven() {
        // given
        // when
        String result = underTest.mapLocalDate(null);
        // then
        assertThat(result).isNull();
    }

    @Test
    public void testMapLocalDateShouldReturnStringInIsoLocalDateFormat() {
        // given
        String expected = "2022-09-01";
        LocalDate source = LocalDate.of(2022, 9, 1);
        // when
        String result = underTest.mapLocalDate(source);
        // then
        assertThat(result).isEqualTo(expected);
        LocalDate parsedDate = LocalDate.parse(result, DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(parsedDate).isEqualTo(source);
    }
}
