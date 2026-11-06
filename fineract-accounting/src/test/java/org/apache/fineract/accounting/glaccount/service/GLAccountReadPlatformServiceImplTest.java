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
import static org.mockito.Mockito.verify;

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
    void shouldBindWildcardsAroundSearchParam() {
        glAccountReadPlatformService.retrieveAllGLAccounts(null, "cash", null, null, null, new JournalEntryAssociationParametersData());

        verify(jdbcTemplate).query(sqlCaptor.capture(), rowMapperCaptor.capture(), paramsCaptor.capture());

        final String sql = sqlCaptor.getValue();
        assertFalse(sql.contains("%?%"), "JDBC placeholder must not be wrapped with literal '%' characters in the SQL text: " + sql);
        assertArrayEquals(new Object[] { "%cash%", "%cash%" }, paramsCaptor.getValue());
    }
}
