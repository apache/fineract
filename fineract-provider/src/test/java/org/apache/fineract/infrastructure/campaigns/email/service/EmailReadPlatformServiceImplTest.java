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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageEnumerations;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageStatusType;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailNotFoundException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.query.QScheduledEmailMessagesOutbound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EmailReadPlatformServiceImplTest {

    private static final QScheduledEmailMessagesOutbound EMO = new QScheduledEmailMessagesOutbound("emo");
    private static final NumberPath<Long> EXTERNAL_ID = Expressions.numberPath(Long.class, EMO, "external_id");

    @Mock
    private SQLQueryFactory queryFactory;

    private EmailReadPlatformServiceImpl emailReadPlatformService;

    @BeforeEach
    void setUp() {
        emailReadPlatformService = new EmailReadPlatformServiceImpl(queryFactory);
    }

    @Test
    void testRetrieveAllPendingWithLimit() {
        SearchParameters searchParameters = SearchParameters.builder().limit(10).build();
        Date submittedDate = Date.valueOf(LocalDate.of(2026, 11, 26));
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(
                List.of(row(1L, 10L, 20L, 30L, "Pending Campaign", 100, "user@example.com", submittedDate, "Subject", "Message", null)));

        Collection<EmailData> result = emailReadPlatformService.retrieveAllPending(searchParameters);

        assertNotNull(result);
        assertEquals(1, result.size());
        EmailData data = result.iterator().next();
        assertEquals(1L, data.getId());
        assertEquals(100L, data.getStatus().getId());
        assertEquals("user@example.com", data.getEmailAddress());

        verify(queryFactory).select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.PENDING.getValue()));
        verify(query).limit(10L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveAllSentWithLimit() {
        SearchParameters searchParameters = SearchParameters.builder().limit(5).build();
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllSent(searchParameters);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        verify(query).limit(5L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveAllPendingWithoutLimit() {
        SearchParameters searchParameters = SearchParameters.builder().limit(0).build();
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllPending(searchParameters);

        assertNotNull(result);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.PENDING.getValue()));
        verify(query, never()).limit(any(Long.class));
        verify(query).fetch();
    }

    @Test
    void testRetrieveWithLimitOne() {
        SearchParameters searchParameters = SearchParameters.builder().limit(1).build();
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllPending(searchParameters);

        assertNotNull(result);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.PENDING.getValue()));
        verify(query).limit(1L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveWithNegativeLimit() {
        SearchParameters searchParameters = SearchParameters.builder().limit(-5).build();
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllPending(searchParameters);

        assertNotNull(result);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.PENDING.getValue()));
        verify(query, never()).limit(any(Long.class));
        verify(query).fetch();
    }

    @Test
    void testRetrieveAllDeliveredWithLimit() {
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllDelivered(15);

        assertNotNull(result);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.DELIVERED.getValue()));
        verify(query).limit(15L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveAllFailed() {
        SearchParameters searchParameters = SearchParameters.builder().limit(25).build();
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        Collection<EmailData> result = emailReadPlatformService.retrieveAllFailed(searchParameters);

        assertNotNull(result);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.FAILED.getValue()));
        verify(query).limit(25L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveAllReturnsExpectedData() {
        Date submittedDate = Date.valueOf(LocalDate.of(2026, 11, 25));
        givenRows(row(1L, 10L, 20L, 30L, "Welcome Campaign", 100, "user@example.com", submittedDate, "Welcome", "Hello World", "No Error"));

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        EmailData data = result.iterator().next();
        assertEquals(1L, data.getId());
        assertEquals(10L, data.getGroupId());
        assertEquals(20L, data.getClientId());
        assertEquals(30L, data.getStaffId());
        assertEquals("Welcome Campaign", data.getCampaignName());
        assertNotNull(data.getStatus());
        assertEquals(100L, data.getStatus().getId());
        assertEquals("Pending", data.getStatus().getValue());
        assertEquals("user@example.com", data.getEmailAddress());
        assertEquals(LocalDate.of(2026, 11, 25), data.getSentDate());
        assertEquals("Welcome", data.getEmailSubject());
        assertEquals("Hello World", data.getEmailMessage());
        assertEquals("No Error", data.getErrorMessage());
    }

    @Test
    void testRetrieveAllBuildsExpectedQuery() {
        SQLQuery<Tuple> query = givenRows();

        emailReadPlatformService.retrieveAll();

        verify(queryFactory).select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage);
        verify(query).from(EMO);
    }

    @Test
    void testRetrieveAllReturnsEmptyWhenNoRows() {
        givenRows();

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRetrieveAllHandlesNullFieldsSafely() {
        givenRows(row(null, null, null, null, null, null, null, null, null, null, null));

        Collection<EmailData> result = emailReadPlatformService.retrieveAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        EmailData data = result.iterator().next();
        assertNull(data.getId());
        assertNull(data.getGroupId());
        assertNull(data.getClientId());
        assertNull(data.getStaffId());
        assertNull(data.getCampaignName());
        assertNull(data.getStatus());
        assertNull(data.getEmailAddress());
        assertNull(data.getSentDate());
        assertNull(data.getEmailSubject());
        assertNull(data.getEmailMessage());
        assertNull(data.getErrorMessage());
    }

    @Test
    void testRetrieveExternalIdsOfAllSentWithLimit() {
        SQLQuery<Long> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EXTERNAL_ID)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(101L, 102L, 103L));

        List<Long> result = emailReadPlatformService.retrieveExternalIdsOfAllSent(5);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(List.of(101L, 102L, 103L), result);

        verify(queryFactory).select(EXTERNAL_ID);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        verify(query).limit(5L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveExternalIdsOfAllSentWithoutLimit() {
        SQLQuery<Long> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EXTERNAL_ID)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(101L));

        List<Long> result = emailReadPlatformService.retrieveExternalIdsOfAllSent(0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0));

        verify(queryFactory).select(EXTERNAL_ID);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        verify(query, never()).limit(any(Long.class));
        verify(query).fetch();
    }

    @Test
    void testRetrieveExternalIdsOfAllSentHandlesNullLimit() {
        SQLQuery<Long> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EXTERNAL_ID)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(101L));

        List<Long> result = emailReadPlatformService.retrieveExternalIdsOfAllSent(null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(queryFactory).select(EXTERNAL_ID);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        verify(query, never()).limit(any(Long.class));
        verify(query).fetch();
    }

    @Test
    void testRetrieveExternalIdsOfAllSentEmpty() {
        SQLQuery<Long> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EXTERNAL_ID)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of());

        List<Long> result = emailReadPlatformService.retrieveExternalIdsOfAllSent(10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(queryFactory).select(EXTERNAL_ID);
        verify(query).from(EMO);
        verify(query).where(EMO.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        verify(query).limit(10L);
        verify(query).fetch();
    }

    @Test
    void testRetrieveEmailByStatusWithAllFilters() {
        Date submittedDate = Date.valueOf(LocalDate.of(2026, 11, 26));
        SQLQuery<Long> countQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id.count())).thenReturn(countQuery);
        when(countQuery.fetchOne()).thenReturn(1L);

        SQLQuery<Tuple> selectQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(selectQuery);
        when(selectQuery.fetch()).thenReturn(List
                .of(row(1L, 10L, 20L, 30L, "Welcome Campaign", 100, "user@example.com", submittedDate, "Welcome", "Hello World", null)));

        Page<EmailData> page = emailReadPlatformService.retrieveEmailByStatus(10, 100, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30));

        assertNotNull(page);
        assertEquals(1, page.getTotalFilteredRecords());
        assertEquals(1, page.getPageItems().size());

        EmailData data = page.getPageItems().get(0);
        assertEquals(1L, data.getId());
        assertEquals("user@example.com", data.getEmailAddress());

        verify(queryFactory).select(EMO.id.count());
        verify(countQuery).from(EMO);
        verify(countQuery).where(any(com.querydsl.core.types.Predicate.class));
        verify(countQuery).fetchOne();

        verify(queryFactory).select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage);
        verify(selectQuery).from(EMO);
        verify(selectQuery).where(any(com.querydsl.core.types.Predicate.class));
        verify(selectQuery).limit(10L);
        verify(selectQuery).fetch();
    }

    @Test
    void testRetrieveEmailByStatusWithoutFilters() {
        SQLQuery<Long> countQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id.count())).thenReturn(countQuery);
        when(countQuery.fetchOne()).thenReturn(0L);

        SQLQuery<Tuple> selectQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(selectQuery);
        when(selectQuery.fetch()).thenReturn(List.of());

        Page<EmailData> page = emailReadPlatformService.retrieveEmailByStatus(null, null, null, null);

        assertNotNull(page);
        assertEquals(0, page.getTotalFilteredRecords());
        assertTrue(page.getPageItems().isEmpty());

        verify(selectQuery, never()).limit(any(Long.class));
    }

    @Test
    void testRetrieveEmailByStatusWithOnlyStatusFilter() {
        SQLQuery<Long> countQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id.count())).thenReturn(countQuery);
        when(countQuery.fetchOne()).thenReturn(2L);

        Date submittedDate = Date.valueOf(LocalDate.of(2026, 11, 26));
        SQLQuery<Tuple> selectQuery = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(selectQuery);
        when(selectQuery.fetch()).thenReturn(
                List.of(row(1L, 10L, 20L, 30L, "Campaign 1", 200, "user1@example.com", submittedDate, "Subject 1", "Body 1", null),
                        row(2L, 10L, 20L, 30L, "Campaign 2", 200, "user2@example.com", submittedDate, "Subject 2", "Body 2", null)));

        Page<EmailData> page = emailReadPlatformService.retrieveEmailByStatus(20, 200, null, null);

        assertNotNull(page);
        assertEquals(2, page.getTotalFilteredRecords());
        assertEquals(2, page.getPageItems().size());
        verify(selectQuery).limit(20L);
    }

    @SuppressWarnings("unchecked")
    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(Long id, Long groupId, Long clientId, Long staffId, String campaignName, Integer statusEnum,
            String emailAddress, Date submittedonDate, String emailSubject, String message, String errorMessage) {
        return Projections.tuple(EMO.id, EMO.groupId, EMO.clientId, EMO.staffId, EMO.campaignName, EMO.statusEnum, EMO.emailAddress,
                EMO.submittedonDate, EMO.emailSubject, EMO.message, EMO.errorMessage).newInstance(id, groupId, clientId, staffId,
                        campaignName, statusEnum, emailAddress, submittedonDate, emailSubject, message, errorMessage);
    }

    private static final String EXPECTED_RETRIEVE_ONE_SQL = """
            select emo.id, emo.group_id, \
            emo.client_id, emo.staff_id, \
            emo.campaign_name, emo.status_enum, \
            emo.email_address, emo.submittedon_date, \
            emo.email_subject, emo.message, \
            emo.error_message
            from scheduled_email_messages_outbound emo
            where emo.id = ?
            limit ?""";

    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private String executedSql;

    @Test
    void retrieveOneMapsAllFieldsOnPostgreSQL() throws SQLException {
        EmailReadPlatformService service = retrieveOneService(PostgreSQLTemplates.builder().build(), fullRow());

        assertEquals(expectedFullRow(), service.retrieveOne(11L));
        assertEquals(EXPECTED_RETRIEVE_ONE_SQL, executedSql);
        verify(statement).setLong(1, 11L);
    }

    @Test
    void retrieveOneMapsAllFieldsOnMySQL() throws SQLException {
        EmailReadPlatformService service = retrieveOneService(MySQLTemplates.builder().build(), fullRow());

        assertEquals(expectedFullRow(), service.retrieveOne(11L));
        assertEquals(EXPECTED_RETRIEVE_ONE_SQL, executedSql);
        verify(statement).setLong(1, 11L);
    }

    @Test
    void retrieveOneKeepsNullableColumnsNull() throws SQLException {
        EmailReadPlatformService service = retrieveOneService(PostgreSQLTemplates.builder().build(), new Object[] { 12L, null, null, null,
                null, EmailMessageStatusType.PENDING.getValue(), "a@example.org", null, null, null, null });

        EmailData email = service.retrieveOne(12L);

        assertEquals(EmailData.instance(12L, null, null, null, EmailMessageEnumerations.status(EmailMessageStatusType.PENDING.getValue()),
                "a@example.org", null, null, null, null, null, null, null, null, null, null), email);
    }

    @Test
    void retrieveOneThrowsEmailNotFoundWhenIdDoesNotExist() throws SQLException {
        EmailReadPlatformService service = retrieveOneService(PostgreSQLTemplates.builder().build());

        EmailNotFoundException exception = assertThrows(EmailNotFoundException.class, () -> service.retrieveOne(404L));

        assertEquals("error.msg.email.identifier.not.found", exception.getGlobalisationMessageCode());
        assertEquals("Email with identifier `404` does not exist", exception.getDefaultUserMessage());
        verify(statement).setLong(1, 404L);
    }

    private static Object[] fullRow() {
        return new Object[] { 11L, 3L, 5L, 7L, "Welcome campaign", EmailMessageStatusType.SENT.getValue(), "client@example.org",
                Date.valueOf(LocalDate.of(2026, 11, 20)), "Welcome", "Hello there", "none" };
    }

    private static EmailData expectedFullRow() {
        return EmailData.instance(11L, 3L, 5L, 7L, EmailMessageEnumerations.status(EmailMessageStatusType.SENT.getValue()),
                "client@example.org", "Welcome", "Hello there", null, null, null, null, null, "Welcome campaign",
                LocalDate.of(2026, 11, 20), "none");
    }

    private EmailReadPlatformService retrieveOneService(SQLTemplates templates, Object[]... rows) throws SQLException {
        ResultSet resultSet = resultSet(rows);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet);

        return new EmailReadPlatformServiceImpl(new SQLQueryFactory(new Configuration(templates), () -> connection));
    }

    /**
     * A {@link ResultSet} that serves the given rows by column index, including {@link ResultSet#wasNull()}.
     */
    private static ResultSet resultSet(Object[]... rows) {
        AtomicInteger current = new AtomicInteger(-1);
        AtomicInteger lastColumn = new AtomicInteger();

        Answer<Object> answer = invocation -> {
            String method = invocation.getMethod().getName();
            if ("next".equals(method)) {
                return current.incrementAndGet() < rows.length;
            }
            if ("wasNull".equals(method)) {
                return rows[current.get()][lastColumn.get() - 1] == null;
            }
            if (method.startsWith("get") && invocation.getArguments().length > 0 && invocation.getArgument(0) instanceof Integer column) {
                lastColumn.set(column);
                Object value = rows[current.get()][column - 1];
                return value == null ? Mockito.RETURNS_DEFAULTS.answer(invocation) : value;
            }
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        };

        return mock(ResultSet.class, answer);
    }
}
