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
package org.apache.fineract.infrastructure.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JdbcSupportTest {

    private static final String COLUMN_NAME = "transactionTime";

    @Test
    void shouldReadOffsetTime() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        final OffsetTime time = OffsetTime.of(9, 30, 0, 0, ZoneOffset.ofHours(2));
        when(resultSet.getObject(COLUMN_NAME, OffsetTime.class)).thenReturn(time);

        assertThat(JdbcSupport.getOffsetTime(resultSet, COLUMN_NAME)).isEqualTo(time);
    }

    @Test
    void shouldReturnNull() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);

        assertThat(JdbcSupport.getOffsetTime(resultSet, COLUMN_NAME)).isNull();
    }

    @Test
    void shouldReadMariaDbTimeAsUtcWhenOffsetTimeIsUnsupported() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(COLUMN_NAME, OffsetTime.class))
                .thenThrow(new SQLException("Type class java.time.OffsetTime not supported type for TIME type"));
        when(resultSet.getObject(COLUMN_NAME, LocalTime.class)).thenReturn(LocalTime.of(20, 30));

        assertThat(JdbcSupport.getOffsetTime(resultSet, COLUMN_NAME)).isEqualTo(OffsetTime.parse("20:30:00Z"));
    }

    @Test
    void shouldNotHideOtherSqlExceptions() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        final SQLException exception = new SQLException("Connection closed");
        when(resultSet.getObject(COLUMN_NAME, OffsetTime.class)).thenThrow(exception);

        assertSame(exception, assertThrows(SQLException.class, () -> JdbcSupport.getOffsetTime(resultSet, COLUMN_NAME)));
    }
}
