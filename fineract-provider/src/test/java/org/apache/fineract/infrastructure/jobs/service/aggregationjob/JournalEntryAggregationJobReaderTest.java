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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.JobSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
public class JournalEntryAggregationJobReaderTest {

    @Mock
    private FineractPlatformTenantConnection fineractPlatformTenantConnection;
    @Mock
    private TenantDataSourceFactory tenantDataSourceFactory;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private ExecutionContext executionContext;
    @Mock
    private HikariDataSource dataSource;
    @Mock
    private ResultSet resultSet;

    private JournalEntryAggregationJobReader reader;
    private FineractPlatformTenant tenant;

    @BeforeEach
    public void setUp() {
        tenant = new FineractPlatformTenant(1L, "default", "Default Tenant", "default", fineractPlatformTenantConnection);
        ThreadLocalContextUtil.setTenant(tenant);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
        if (JobSynchronizationManager.getContext() != null) {
            JobSynchronizationManager.close();
        }
    }

    @Test
    public void testRowMapping() throws Exception {
        // Arrange
        setupResultSetMocks();

        // Act
        reader = new JournalEntryAggregationJobReader(tenantDataSourceFactory);
        JournalEntryAggregationSummaryData result = invokeMapRowMethod(resultSet, 1);

        // Assert
        assertNotNull(result, "Mapped result should not be null");
        assertEquals(Long.valueOf(1001L), result.getGlAccountId(), "GL Account ID should match");
        assertEquals(100L, result.getProductId(), "Product ID should match");
        assertEquals(Long.valueOf(1L), result.getOffice(), "Office should be 1L");
        assertEquals(1L, result.getEntityTypeEnum(), "Entity type should be 1");
        assertEquals("USD", result.getCurrencyCode(), "Currency code should match");
        assertEquals(LocalDate.of(2023, 6, 15), result.getAggregatedOnDate(), "Aggregated date should match");
        assertEquals(Long.valueOf(500L), result.getExternalOwnerId(), "Asset owner should match");
        assertEquals(new BigDecimal("1000.00"), result.getDebitAmount(), "Debit amount should match");
        assertEquals(Boolean.FALSE, result.getManualEntry(), "Manual entry should be false");
        assertEquals(ThreadLocalContextUtil.getBusinessDate(), result.getSubmittedOnDate(), "Submitted on date should match business date");
    }

    @Test
    public void testRowMappingWithNullAssetOwner() throws Exception {
        // Arrange
        setupResultSetMocksWithNullOwner();

        // Act
        reader = new JournalEntryAggregationJobReader(tenantDataSourceFactory);
        JournalEntryAggregationSummaryData result = invokeMapRowMethod(resultSet, 1);

        // Assert
        assertEquals(Long.valueOf(0L), result.getExternalOwnerId(), "Asset owner should be 0 when null in resultset");
    }

    private void setupResultSetMocks() throws SQLException {
        when(resultSet.getLong("glAccountId")).thenReturn(1001L);
        when(resultSet.getLong("productId")).thenReturn(100L);
        when(resultSet.getString("currencyCode")).thenReturn("USD");
        when(resultSet.getDate("aggregatedOnDate")).thenReturn(Date.valueOf(LocalDate.of(2023,6,15)));
        when(resultSet.findColumn("externalOwner")).thenReturn(5);
        when(resultSet.getLong(5)).thenReturn(500L);
        when(resultSet.getLong("officeId")).thenReturn(1L);
        when(resultSet.getLong("entityTypeEnum")).thenReturn(1L);
        when(resultSet.getBigDecimal("debitAmount")).thenReturn(new BigDecimal("1000.00"));
        when(resultSet.getBigDecimal("creditAmount")).thenReturn(new BigDecimal("800.00"));
    }

    private void setupResultSetMocksWithNullOwner() throws SQLException {
        when(resultSet.getLong("glAccountId")).thenReturn(1001L);
        when(resultSet.getLong("productId")).thenReturn(100L);
        when(resultSet.getString("currencyCode")).thenReturn("USD");
        when(resultSet.getDate("aggregatedOnDate")).thenReturn(Date.valueOf(LocalDate.of(2023,6,15)));
        when(resultSet.getLong("officeId")).thenReturn(1L);
        when(resultSet.getLong("entityTypeEnum")).thenReturn(1L);
        when(resultSet.getBigDecimal("debitAmount")).thenReturn(new BigDecimal("1000.00"));
        when(resultSet.getBigDecimal("creditAmount")).thenReturn(new BigDecimal("800.00"));
    }

    private JournalEntryAggregationSummaryData invokeMapRowMethod(ResultSet rs, int rowNum) throws Exception {
        Method mapRowMethod = JournalEntryAggregationJobReader.class.getDeclaredMethod("mapRow", ResultSet.class, int.class);
        mapRowMethod.setAccessible(true);
        return (JournalEntryAggregationSummaryData) mapRowMethod.invoke(reader, rs, rowNum);
    }
}
