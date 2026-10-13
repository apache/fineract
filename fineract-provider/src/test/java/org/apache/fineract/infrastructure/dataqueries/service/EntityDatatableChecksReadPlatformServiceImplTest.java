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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityDataTableChecksTemplateData;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class EntityDatatableChecksReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private EntityDatatableChecksRepository entityDatatableChecksRepository;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private SavingsProductReadPlatformService savingsProductReadPlatformService;
    @Mock
    private PaginationHelper paginationHelper;

    private EntityDatatableChecksReadPlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new EntityDatatableChecksReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, entityDatatableChecksRepository,
                datatableReadService, loanProductReadPlatformService, savingsProductReadPlatformService, paginationHelper);
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sqlGenerator.alias(anyString(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sqlGenerator.formatValue(any(), anyString())).thenAnswer(invocation -> "'" + invocation.getArgument(1) + "'");
    }

    @Test
    void templateListsLoanProductDatatables() {
        underTest.retrieveTemplate();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("'m_product_loan'"), sql);
    }

    @Test
    void templateExposesLoanProductCheckStatuses() {
        EntityDataTableChecksTemplateData template = underTest.retrieveTemplate();

        JsonObject json = new Gson().toJsonTree(template).getAsJsonObject();
        JsonArray statusLoanProduct = json.getAsJsonArray("statusLoanProduct");
        assertEquals(1, statusLoanProduct.size());
        assertEquals(StatusEnum.CREATE.name(), statusLoanProduct.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(StatusEnum.CREATE.getValue(), statusLoanProduct.get(0).getAsJsonObject().get("code").getAsInt());
    }
}
