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

import java.time.MonthDay;
import org.junit.jupiter.api.Test;

class AvroMonthDayMapperTest {

    private AvroMonthDayMapper underTest = new AvroMonthDayMapper();

    @Test
    public void testMapMonthDayShouldReturnNullIfNullGiven() {
        // given
        // when
        String result = underTest.mapMonthDay(null);
        // then
        assertThat(result).isNull();
    }

    @Test
    public void testMapMonthDayShouldReturnStringInParsableFormat() {
        // given
        String expected = "--09-10";
        MonthDay source = MonthDay.of(9, 10);
        // when
        String result = underTest.mapMonthDay(source);
        // then
        assertThat(result).isEqualTo(expected);
        MonthDay parsedMonthDay = MonthDay.parse(result);
        assertThat(parsedMonthDay).isEqualTo(source);
    }

}
