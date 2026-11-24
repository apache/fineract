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
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.data.S3CredentialsData;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.query.QCExternalService;
import org.apache.fineract.query.QCExternalServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ExternalServicesPropertiesReadPlatformServiceTest {

    // Equal to the service's own "esp" alias: QueryDSL paths compare by alias and column name.
    private static final QCExternalServiceProperties ESP = new QCExternalServiceProperties("esp");
    private static final QCExternalService ES = new QCExternalService("es");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SQLQueryFactory queryFactory;

    private ExternalServicesPropertiesReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExternalServicesPropertiesReadPlatformServiceImpl(jdbcTemplate, queryFactory);
    }

    @Test
    void getSMTPCredentialsMapsEveryProperty() {
        givenRows(row("username", "user"), row("password", "secret"), row("host", "smtp.example.com"), row("port", "25"),
                row("useTLS", "true"), row("fromEmail", "noreply@example.com"), row("fromName", "Fineract"));

        SMTPCredentialsData result = service.getSMTPCredentials();

        assertEquals("user", result.getUsername());
        assertEquals("secret", result.getPassword());
        assertEquals("smtp.example.com", result.getHost());
        assertEquals("25", result.getPort());
        assertTrue(result.isUseTLS());
        assertEquals("noreply@example.com", result.getFromEmail());
        assertEquals("Fineract", result.getFromName());
    }

    @Test
    void getSMTPCredentialsReadsOnlyTheSmtpServiceProperties() {
        SQLQuery<Tuple> query = givenRows();

        service.getSMTPCredentials();

        verify(query).from(ESP);
        verify(query).innerJoin(ES);
        verify(query).on(ESP.externalServiceId.eq(ES.id));
        verify(query).where(ES.name.eq(ExternalServicesConstants.SMTP_SERVICE_NAME));
    }

    @Test
    void getSMTPCredentialsUsesDefaultsWhenNoRows() {
        givenRows();

        SMTPCredentialsData result = service.getSMTPCredentials();

        assertEquals("587", result.getPort());
        assertFalse(result.isUseTLS());
        assertNull(result.getHost());
    }

    @Test
    void getSMTPCredentialsMatchesNamesIgnoringCase() {
        givenRows(row("HOST", "smtp.example.com"));

        assertEquals("smtp.example.com", service.getSMTPCredentials().getHost());
    }

    @Test
    void getS3CredentialsMapsEveryProperty() {
        givenRows(row("s3_access_key", "my-access-key"), row("s3_bucket_name", "my-bucket"), row("s3_secret_key", "my-secret-key"));

        S3CredentialsData result = service.getS3Credentials();

        assertEquals("my-access-key", result.getAccessKey());
        assertEquals("my-bucket", result.getBucketName());
        assertEquals("my-secret-key", result.getSecretKey());
    }

    @Test
    void getS3CredentialsReadsOnlyTheS3ServiceProperties() {
        SQLQuery<Tuple> query = givenRows();

        service.getS3Credentials();

        verify(query).from(ESP);
        verify(query).innerJoin(ES);
        verify(query).on(ESP.externalServiceId.eq(ES.id));
        verify(query).where(ES.name.eq(ExternalServicesConstants.S3_SERVICE_NAME));
    }

    @Test
    void getS3CredentialsUsesDefaultsWhenNoRows() {
        givenRows();

        S3CredentialsData result = service.getS3Credentials();

        assertNull(result.getAccessKey());
        assertNull(result.getBucketName());
        assertNull(result.getSecretKey());
    }

    @Test
    void getS3CredentialsMatchesNamesIgnoringCase() {
        givenRows(row("S3_ACCESS_KEY", "my-access-key"), row("S3_BUCKET_NAME", "my-bucket"), row("S3_SECRET_KEY", "my-secret-key"));

        S3CredentialsData result = service.getS3Credentials();

        assertEquals("my-access-key", result.getAccessKey());
        assertEquals("my-bucket", result.getBucketName());
        assertEquals("my-secret-key", result.getSecretKey());
    }

    @SuppressWarnings("unchecked")
    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        // RETURNS_SELF makes from/innerJoin/on/where return the same mock, so only fetch() needs stubbing.
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(ESP.name, ESP.value)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(String name, String value) {
        return Projections.tuple(ESP.name, ESP.value).newInstance(name, value);
    }
}
