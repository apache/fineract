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
package org.apache.fineract.accounting.glaccount.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class GLAccountReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GLAccountReadPlatformServiceImpl glAccountReadPlatformService;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<RowMapper<?>> rowMapperCaptor;

    @Captor
    private ArgumentCaptor<Object[]> paramsCaptor;

    @Test
    void shouldBuildValidLikeClauseForSearchParam() {
        glAccountReadPlatformService.retrieveAllGLAccounts(null, "cash", null, null, null, new JournalEntryAssociationParametersData());

        verify(jdbcTemplate).query(sqlCaptor.capture(), rowMapperCaptor.capture(), paramsCaptor.capture());

        final String sql = sqlCaptor.getValue();
        assertFalse(sql.contains("%?%"), "JDBC placeholder must not be wrapped with literal '%' characters in the SQL text: " + sql);
        assertTrue(sql.contains("concat('%', ?, '%')"), "Expected a concat('%', ?, '%') LIKE pattern, got: " + sql);
        assertArrayEquals(new Object[] { "cash", "cash" }, paramsCaptor.getValue());
    }

    @Test
    void shouldMapMissingTagAsNullCodeId() throws SQLException {
        glAccountReadPlatformService.retrieveAllGLAccounts(null, null, null, null, null, new JournalEntryAssociationParametersData());

        verify(jdbcTemplate).query(anyString(), rowMapperCaptor.capture(), any(Object[].class));

        final ResultSet resultSetWithoutTag = fakeGLAccountResultSetWithoutTag();
        final GLAccountData glAccountData = (GLAccountData) rowMapperCaptor.getValue().mapRow(resultSetWithoutTag, 1);

        assertNull(glAccountData.getTagId(), "Expected a null tag for a GL account without a tag");
    }

    private static ResultSet fakeGLAccountResultSetWithoutTag() throws SQLException {
        final Map<String, Object> values = new HashMap<>();
        values.put("id", 1L);
        values.put("name", "Cash");
        values.put("parentId", null);
        values.put("glCode", "100");
        values.put("disabled", false);
        values.put("manualEntriesAllowed", true);
        values.put("classification", 1);
        values.put("accountUsage", 1);
        values.put("description", "Cash account");
        values.put("nameDecorated", "Cash");
        values.put("codeId", null);
        values.put("codeValue", null);

        final Map<String, Integer> columnIndexes = new HashMap<>();
        final Map<Integer, String> columnNames = new HashMap<>();
        int index = 1;
        for (final String column : values.keySet()) {
            columnIndexes.put(column, index);
            columnNames.put(index, column);
            index++;
        }

        final boolean[] lastValueWasNull = { false };
        final ResultSet rs = mock(ResultSet.class);

        when(rs.findColumn(anyString())).thenAnswer(invocation -> columnIndexes.get((String) invocation.getArgument(0)));
        when(rs.getString(anyString())).thenAnswer(invocation -> {
            final Object value = values.get((String) invocation.getArgument(0));
            lastValueWasNull[0] = value == null;
            return value;
        });
        when(rs.getBoolean(anyString())).thenAnswer(invocation -> {
            final Object value = values.get((String) invocation.getArgument(0));
            lastValueWasNull[0] = value == null;
            return Boolean.TRUE.equals(value);
        });
        when(rs.getLong(anyString())).thenAnswer(invocation -> {
            final Long value = (Long) values.get((String) invocation.getArgument(0));
            lastValueWasNull[0] = value == null;
            return value == null ? 0L : value;
        });
        when(rs.getLong(anyInt())).thenAnswer(invocation -> {
            final Long value = (Long) values.get(columnNames.get((int) invocation.getArgument(0)));
            lastValueWasNull[0] = value == null;
            return value == null ? 0L : value;
        });
        when(rs.getInt(anyInt())).thenAnswer(invocation -> {
            final Integer value = (Integer) values.get(columnNames.get((int) invocation.getArgument(0)));
            lastValueWasNull[0] = value == null;
            return value == null ? 0 : value;
        });
        when(rs.wasNull()).thenAnswer(invocation -> lastValueWasNull[0]);
        return rs;
    }
}
