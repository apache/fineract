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
package org.apache.fineract.portfolio.savings.jobs.generaterdschedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceService;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.DepositAccountUtils;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressFBWarnings({ "ODR_OPEN_DATABASE_RESOURCE" })
class GenerateRdScheduleTaskletTest {

    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private RoutingDataSourceServiceFactory dataSourceServiceFactory;
    @Mock
    private RoutingDataSourceService dataSourceService;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private DatabaseMetaData databaseMetaData;
    @Mock
    private DepositAccountReadPlatformService depositAccountReadPlatformService;
    @Mock
    private PlatformSecurityContext securityContext;
    @Mock
    private AppUser appUser;

    private GenerateRdScheduleTasklet underTest;

    @BeforeEach
    void setUp() throws SQLException {
        given(dataSourceServiceFactory.determineDataSourceService()).willReturn(dataSourceService);
        given(dataSourceService.retrieveDataSource()).willReturn(dataSource);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(connection.getMetaData()).willReturn(databaseMetaData);
        given(databaseMetaData.supportsBatchUpdates()).willReturn(true);
        given(preparedStatement.getConnection()).willReturn(connection);
        given(preparedStatement.executeBatch()).willReturn(new int[] { 1 });
        given(securityContext.authenticatedUser()).willReturn(appUser);
        given(appUser.getId()).willReturn(1L);

        underTest = new GenerateRdScheduleTasklet(dataSourceServiceFactory, depositAccountReadPlatformService, securityContext);
    }

    private static Map<String, Object> scheduleDetails(long savingsId, long futureInstallments) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("savingsId", savingsId);
        details.put("amount", BigDecimal.TEN);
        details.put("recurrence", "FREQ=MONTHLY;INTERVAL=1");
        // real JDBC drivers (Postgres/MySQL/MariaDB) return java.sql.Date for a DATE column via queryForList, not
        // java.time.LocalDate, so the mock must match that to catch a regression of the ClassCastException it used to
        // throw.
        details.put("dueDate", Date.valueOf(LocalDate.of(2026, 1, 1)));
        // installment is a SMALLINT column; the MariaDB driver returns java.lang.Short for it (unlike Postgres, which
        // widens to Integer), so the mock must match that to catch a regression of the ClassCastException it used to
        // throw.
        details.put("installment", (short) 3);
        details.put("futureInstallments", futureInstallments);
        return details;
    }

    @Test
    void insertSqlBindsEveryColumn() throws Exception {
        given(depositAccountReadPlatformService.retriveDataForRDScheduleCreation()).willReturn(List.of(scheduleDetails(1L, 0L)));

        underTest.execute(stepContribution, chunkContext);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();

        int columnCount = sql.substring(sql.indexOf('(') + 1, sql.indexOf(')')).split(",").length;
        int placeholderCount = sql.length() - sql.replace("?", "").length();

        assertThat(placeholderCount).as("number of '?' placeholders must match the number of columns in the INSERT statement")
                .isEqualTo(columnCount);
    }

    @Test
    void insertSqlColumnOrderMatchesBoundParameters() throws Exception {
        given(depositAccountReadPlatformService.retriveDataForRDScheduleCreation()).willReturn(List.of(scheduleDetails(7L, 4L)));

        underTest.execute(stepContribution, chunkContext);

        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(preparedStatement, times(9)).setObject(indexCaptor.capture(), valueCaptor.capture());

        assertThat(indexCaptor.getAllValues()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Object> values = valueCaptor.getAllValues();
        assertThat(values.get(0)).isEqualTo(7L);
        assertThat(values.get(1)).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(values.get(2)).isEqualTo(4);
        assertThat(values.get(3)).isEqualTo(BigDecimal.TEN);
        assertThat(values.get(4)).isEqualTo(false);
        assertThat(values.get(6)).isEqualTo(1L);
        assertThat(values.get(8)).isEqualTo(1L);
    }

    @Test
    void generatesMinimumNumberOfFutureInstallments() throws Exception {
        given(depositAccountReadPlatformService.retriveDataForRDScheduleCreation()).willReturn(List.of(scheduleDetails(1L, 0L)));

        RepeatStatus result = underTest.execute(stepContribution, chunkContext);

        verify(preparedStatement, times(DepositAccountUtils.GENERATE_MINIMUM_NUMBER_OF_FUTURE_INSTALMENTS)).addBatch();
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    void noAccountsProducesNoInsert() throws Exception {
        given(depositAccountReadPlatformService.retriveDataForRDScheduleCreation()).willReturn(List.of());

        RepeatStatus result = underTest.execute(stepContribution, chunkContext);

        verifyNoInteractions(dataSource);
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    void flushesBatchWhenIterationsExceedThreshold() throws Exception {
        List<Map<String, Object>> accounts = new ArrayList<>();
        for (long savingsId = 1; savingsId <= 42; savingsId++) {
            accounts.add(scheduleDetails(savingsId, 0L));
        }
        given(depositAccountReadPlatformService.retriveDataForRDScheduleCreation()).willReturn(accounts);

        underTest.execute(stepContribution, chunkContext);

        // 41 accounts (205 installments) trigger a mid-loop flush, the 42nd account's leftover installments flush at
        // the end
        verify(connection, times(2)).prepareStatement(anyString());
        verify(preparedStatement, never()).executeUpdate();
    }
}
