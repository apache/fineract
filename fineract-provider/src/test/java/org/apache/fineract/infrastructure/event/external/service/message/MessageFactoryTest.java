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
package org.apache.fineract.infrastructure.event.external.service.message;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

public class MessageFactoryTest {

    private final LocalDateTime localDateTime1 = LocalDateTime.of(2023, 5, 4, 10, 54, 0, 0);
    private final LocalDateTime localDateTime2 = LocalDateTime.of(2023, 5, 4, 10, 54, 1, 0);
    private final LocalDateTime localDateTime3 = LocalDateTime.of(2023, 5, 4, 10, 54, 1, 1000);
    private final LocalDateTime localDateTime4 = LocalDateTime.of(2023, 5, 4, 10, 0, 0, 1000);
    private final LocalDateTime localDateTime5 = LocalDateTime.of(2023, 5, 4, 10, 0, 0, 1000000);
    private final LocalDateTime localDateTime6 = LocalDateTime.of(2023, 5, 4, 10, 0, 0, 1234567);

    /**
     * Test whether the tailing zeros are always part of the formatter date time.
     */
    @Test
    public void formatterTest() {
        DateTimeFormatter dateTimeFormatter = MessageFactory.CUSTOM_ISO_LOCAL_DATE_TIME_FORMATTER;

        assertEquals("2023-05-04T10:54:00.000000", localDateTime1.format(dateTimeFormatter));
        assertEquals("2023-05-04T10:54:01.000000", localDateTime2.format(dateTimeFormatter));
        // 1 microsecond
        assertEquals("2023-05-04T10:54:01.000001", localDateTime3.format(dateTimeFormatter));
        assertEquals("2023-05-04T10:00:00.000001", localDateTime4.format(dateTimeFormatter));
        // 1 millisecond
        assertEquals("2023-05-04T10:00:00.001000", localDateTime5.format(dateTimeFormatter));
        assertEquals("2023-05-04T10:00:00.001234567", localDateTime6.format(dateTimeFormatter));
    }

    /**
     * Test whether the formatted datetime by CUSTOM_ISO_LOCAL_DATE_TIME_FORMATTER is still parseable by the
     * (non-custom) ISO_LOCAL_DATE_TIME_FORMATTER (compatibility check)
     */
    @Test
    public void backParsingTest() {
        DateTimeFormatter customDateTimeFormatter = MessageFactory.CUSTOM_ISO_LOCAL_DATE_TIME_FORMATTER;

        assertEquals(localDateTime1,
                LocalDateTime.parse(localDateTime1.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(localDateTime2,
                LocalDateTime.parse(localDateTime2.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(localDateTime3,
                LocalDateTime.parse(localDateTime3.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(localDateTime4,
                LocalDateTime.parse(localDateTime4.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(localDateTime5,
                LocalDateTime.parse(localDateTime5.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertEquals(localDateTime6,
                LocalDateTime.parse(localDateTime6.format(customDateTimeFormatter), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

}
