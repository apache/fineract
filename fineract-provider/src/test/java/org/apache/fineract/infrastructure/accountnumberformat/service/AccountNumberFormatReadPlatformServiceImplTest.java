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
package org.apache.fineract.infrastructure.accountnumberformat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Runs the QueryDSL query of {@link AccountNumberFormatReadPlatformServiceImpl#getAllAccountNumberFormats()} through a
 * real {@link SQLQueryFactory} on top of a mocked JDBC connection, so that the generated SQL and the result mapping are
 * both verified.
 */
class AccountNumberFormatReadPlatformServiceImplTest {

    private static final String EXPECTED_ALL_SQL = """
            select c_account_number_format.id, c_account_number_format.account_type_enum, \
            c_account_number_format.prefix_type_enum, c_account_number_format.prefix_character
            from c_account_number_format c_account_number_format""";

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private String executedSql;

    @Test
    void getAllAccountNumberFormatsMapsAllRowsOnPostgreSQL() throws SQLException {
        AccountNumberFormatReadPlatformService service = service(PostgreSQLTemplates.builder().build(), new Object[] { 1L, 1, 1, null },
                new Object[] { 2L, 2, 401, "LN" });

        List<AccountNumberFormatData> formats = service.getAllAccountNumberFormats();

        assertEquals(2, formats.size());
        assertFormat(formats.get(0), 1L, EntityAccountType.CLIENT, AccountNumberPrefixType.OFFICE_NAME, null);
        assertFormat(formats.get(1), 2L, EntityAccountType.LOAN, AccountNumberPrefixType.PREFIX_SHORT_NAME, "LN");
        assertEquals(EXPECTED_ALL_SQL, executedSql);
        verifyNoParametersBound();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void getAllAccountNumberFormatsMapsAllRowsOnMySQL() throws SQLException {
        AccountNumberFormatReadPlatformService service = service(MySQLTemplates.builder().build(), new Object[] { 3L, 3, 301, null });

        List<AccountNumberFormatData> formats = service.getAllAccountNumberFormats();

        assertEquals(1, formats.size());
        assertFormat(formats.get(0), 3L, EntityAccountType.SAVINGS, AccountNumberPrefixType.SAVINGS_PRODUCT_SHORT_NAME, null);
        assertEquals(EXPECTED_ALL_SQL, executedSql);
        verifyNoParametersBound();
    }

    @Test
    void getAllAccountNumberFormatsKeepsMissingPrefixTypeNull() throws SQLException {
        AccountNumberFormatReadPlatformService service = service(PostgreSQLTemplates.builder().build(), new Object[] { 4L, 6, null, null });

        List<AccountNumberFormatData> formats = service.getAllAccountNumberFormats();

        assertEquals(1, formats.size());
        assertFormat(formats.get(0), 4L, EntityAccountType.SHARES, null, null);
    }

    @Test
    void getAllAccountNumberFormatsReturnsEmptyListWhenTableIsEmpty() throws SQLException {
        AccountNumberFormatReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        assertTrue(service.getAllAccountNumberFormats().isEmpty());
    }

    private static void assertFormat(AccountNumberFormatData data, Long id, EntityAccountType accountType,
            AccountNumberPrefixType prefixType, String prefixCharacter) {
        assertEquals(id, data.getId());
        assertEnumOption(data.getAccountType(), accountType.getValue(), accountType.getCode(), accountType.toString());
        if (prefixType == null) {
            assertNull(data.getPrefixType());
        } else {
            assertEnumOption(data.getPrefixType(), prefixType.getValue(), prefixType.getCode(), prefixType.toString());
        }
        assertEquals(prefixCharacter, ReflectionTestUtils.getField(data, "prefixCharacter"));
    }

    private static void assertEnumOption(EnumOptionData option, Integer value, String code, String description) {
        assertEquals(value.longValue(), option.getId());
        assertEquals(code, option.getCode());
        assertEquals(description, option.getValue());
    }

    private void verifyNoParametersBound() throws SQLException {
        verify(statement, never()).setString(anyInt(), anyString());
        verify(statement, never()).setObject(anyInt(), any());
    }

    private AccountNumberFormatReadPlatformService service(SQLTemplates templates, Object[]... rows) throws SQLException {
        ResultSet resultSet = resultSet(rows);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet);

        return new AccountNumberFormatReadPlatformServiceImpl(jdbcTemplate,
                new SQLQueryFactory(new Configuration(templates), () -> connection));
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
