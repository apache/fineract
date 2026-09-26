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
package org.apache.fineract.infrastructure.core.config.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Time;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.eclipse.persistence.platform.database.MySQLPlatform;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.eclipse.persistence.sessions.Session;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

class OffsetTimePersistenceCustomizerTest {

    private final OffsetTimePersistenceCustomizer.OffsetTimeConverter converter = new OffsetTimePersistenceCustomizer.OffsetTimeConverter();

    @Test
    void preservesOffsetForPostgreSql() {
        Session session = sessionWithPlatform(new PostgreSQLPlatform());
        OffsetTime time = OffsetTime.of(2, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30));

        PGobject value = (PGobject) converter.convertObjectValueToDataValue(time, session);

        assertEquals("timetz", value.getType());
        assertEquals("02:00+05:30", value.getValue());
        assertEquals(time, converter.convertDataValueToObjectValue(time, session));
    }

    @Test
    void bindsNullWithThePostgreSqlTimeWithTimeZoneType() {
        Session session = sessionWithPlatform(new PostgreSQLPlatform());

        PGobject value = assertInstanceOf(PGobject.class, converter.convertObjectValueToDataValue(null, session));

        assertEquals("timetz", value.getType());
        assertNull(value.getValue());
    }

    @Test
    void usesOffsetlessTimeForDatabasesWithoutTimeZoneSupport() {
        Session session = sessionWithPlatform(new MySQLPlatform());
        OffsetTime time = OffsetTime.of(2, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30));

        assertEquals(Time.valueOf("20:30:00"), converter.convertObjectValueToDataValue(time, session));
        assertEquals(OffsetTime.parse("02:00:00Z"), converter.convertDataValueToObjectValue(Time.valueOf("02:00:00"), session));
    }

    private Session sessionWithPlatform(org.eclipse.persistence.platform.database.DatabasePlatform platform) {
        Session session = mock(Session.class);
        when(session.getPlatform()).thenReturn(platform);
        return session;
    }
}
