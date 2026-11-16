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
package org.apache.fineract.organisation.monetary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the QueryDSL query of {@link CurrencyReadPlatformServiceImpl#retrieveCurrency(String)} through a real
 * {@link SQLQueryFactory} on top of a mocked JDBC connection, so that the generated SQL, the bound parameters and the
 * result mapping are all verified.
 */
class CurrencyReadPlatformServiceImplTest {

    private static final String EXPECTED_SQL = """
            select m_currency.code, m_currency.name, m_currency.decimal_places, m_currency.currency_multiplesof, \
            m_currency.display_symbol, m_currency.internationalized_name_code
            from m_currency m_currency
            where m_currency.code = ?
            order by m_currency.name asc
            limit ?""";

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private String executedSql;

    @Test
    void retrieveCurrencyMapsAllFieldsOnPostgreSQL() throws SQLException {
        CurrencyReadPlatformService service = service(PostgreSQLTemplates.builder().build(),
                new Object[] { "USD", "US Dollar", 2, 5, "$", "currency.USD" });

        CurrencyData currency = service.retrieveCurrency("USD");

        assertEquals(new CurrencyData("USD", "US Dollar", 2, 5, "$", "currency.USD"), currency);

        assertEquals(EXPECTED_SQL, executedSql);
        verify(statement).setString(1, "USD");
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void retrieveCurrencyMapsAllFieldsOnMySQL() throws SQLException {
        CurrencyReadPlatformService service = service(MySQLTemplates.builder().build(),
                new Object[] { "EUR", "Euro", 2, 0, "€", "currency.EUR" });

        CurrencyData currency = service.retrieveCurrency("EUR");

        assertEquals(new CurrencyData("EUR", "Euro", 2, 0, "€", "currency.EUR"), currency);
        assertEquals(EXPECTED_SQL, executedSql);
        verify(statement).setString(1, "EUR");
    }

    @Test
    void retrieveCurrencyKeepsNullableColumnsNull() throws SQLException {
        CurrencyReadPlatformService service = service(PostgreSQLTemplates.builder().build(),
                new Object[] { "JPY", "Japanese Yen", 0, null, null, null });

        CurrencyData currency = service.retrieveCurrency("JPY");

        assertEquals(new CurrencyData("JPY", "Japanese Yen", 0, null, null, null), currency);
    }

    @Test
    void retrieveCurrencyThrowsEmptyResultWhenCodeDoesNotExist() throws SQLException {
        CurrencyReadPlatformService service = service(PostgreSQLTemplates.builder().build());

        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class,
                () -> service.retrieveCurrency("XXX"));

        assertEquals(1, exception.getExpectedSize());
        assertEquals(0, exception.getActualSize());
        verify(statement).setString(1, "XXX");
    }

    private CurrencyReadPlatformService service(SQLTemplates templates, Object[]... rows) throws SQLException {
        ResultSet resultSet = resultSet(rows);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            executedSql = invocation.getArgument(0);
            return statement;
        });
        when(statement.executeQuery()).thenReturn(resultSet);

        return new CurrencyReadPlatformServiceImpl(jdbcTemplate, new SQLQueryFactory(new Configuration(templates), () -> connection));
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
