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
package org.apache.fineract.infrastructure.campaigns.email.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailConfigurationData;
import org.apache.fineract.query.QScheduledEmailConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class EmailConfigurationReadPlatformServiceTest {

    private static final QScheduledEmailConfiguration CNF = new QScheduledEmailConfiguration("cnf");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SQLQueryFactory queryFactory;

    private EmailConfigurationReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailConfigurationReadPlatformServiceImpl(jdbcTemplate, queryFactory);
    }

    @Test
    void retrieveAllMapsRowsCorrectly() {
        givenRows(row(1, "SMTP_SERVER", "smtp.example.com"), row(2, "SMTP_PORT", "587"));

        Collection<EmailConfigurationData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        List<EmailConfigurationData> list = List.copyOf(result);
        assertEquals(1L, list.get(0).getId());
        assertEquals("SMTP_SERVER", list.get(0).getName());
        assertEquals("smtp.example.com", list.get(0).getValue());

        assertEquals(2L, list.get(1).getId());
        assertEquals("SMTP_PORT", list.get(1).getName());
        assertEquals("587", list.get(1).getValue());
    }

    @Test
    void retrieveAllBuildsExpectedQuery() {
        SQLQuery<Tuple> query = givenRows();

        service.retrieveAll();

        verify(queryFactory).select(CNF.id, CNF.name, CNF.value);
        verify(query).from(CNF);
    }

    @Test
    void retrieveAllReturnsEmptyWhenNoRows() {
        givenRows();

        Collection<EmailConfigurationData> result = service.retrieveAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void retrieveAllHandlesNullIdSafely() {
        givenRows(row(null, "NO_ID_CONFIG", "val"));

        Collection<EmailConfigurationData> result = service.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        EmailConfigurationData data = result.iterator().next();
        assertNull(data.getId());
        assertEquals("NO_ID_CONFIG", data.getName());
        assertEquals("val", data.getValue());
    }

    @SuppressWarnings("unchecked")
    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(CNF.id, CNF.name, CNF.value)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(Integer id, String name, String value) {
        return Projections.tuple(CNF.id, CNF.name, CNF.value).newInstance(id, name, value);
    }
}
