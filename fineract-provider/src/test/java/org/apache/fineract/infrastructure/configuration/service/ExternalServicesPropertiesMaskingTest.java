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
package org.apache.fineract.infrastructure.configuration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class ExternalServicesPropertiesMaskingTest {

    @ParameterizedTest
    @ValueSource(strings = { "password", "server_key", "s3_secret_key", "s3_access_key", "tenant_app_key", "PASSWORD", "S3_Secret_Key",
            "client_secret", "api_token", "refresh_Token", "some_new_key" })
    void treatsSecretNamesAsSecret(final String name) {
        assertTrue(ExternalServicesSecretProperties.isSecretProperty(name), name);
    }

    @ParameterizedTest
    @ValueSource(strings = { "host", "port", "username", "s3_bucket_name", "useTLS", "fromEmail", "endpoint", "keyboard" })
    void doesNotTreatOtherNamesAsSecret(final String name) {
        assertFalse(ExternalServicesSecretProperties.isSecretProperty(name), name);
    }

    @Test
    void retrieveOneMasksSecretValuesOnly() throws SQLException {
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        final ExternalServicesPropertiesReadPlatformServiceImpl service = new ExternalServicesPropertiesReadPlatformServiceImpl(
                jdbcTemplate, mock(SQLQueryFactory.class));

        service.retrieveOne("S3");

        final ArgumentCaptor<RowMapper<ExternalServicesPropertiesData>> captor = ArgumentCaptor.captor();
        verify(jdbcTemplate).query(anyString(), captor.capture());
        final RowMapper<ExternalServicesPropertiesData> mapper = captor.getValue();

        final String masked = map(mapper, "s3_secret_key", "abcdefghijklmnopqrstuvwxyz123456").getValue();
        assertEquals("a***************************3456", masked);
        assertEquals("****", map(mapper, "s3_access_key", "AKIA").getValue());
        assertTrue(map(mapper, "tenant_app_key", "tenant-app-key-value").getValue().contains("*"));
        assertTrue(map(mapper, "password", "hunter2-password").getValue().contains("*"));
        assertEquals("my-bucket", map(mapper, "s3_bucket_name", "my-bucket").getValue());
        assertNull(map(mapper, "s3_secret_key", null).getValue());
    }

    private static ExternalServicesPropertiesData map(final RowMapper<ExternalServicesPropertiesData> mapper, final String name,
            final String value) throws SQLException {
        final ResultSet rs = mock(ResultSet.class);
        when(rs.getString("name")).thenReturn(name);
        when(rs.getString("value")).thenReturn(value);
        return mapper.mapRow(rs, 0);
    }
}
