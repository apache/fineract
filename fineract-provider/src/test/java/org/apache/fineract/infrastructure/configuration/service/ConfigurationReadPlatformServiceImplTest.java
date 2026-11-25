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

import static org.apache.fineract.query.QCConfiguration.cConfiguration;
import static org.apache.fineract.query.QXRegisteredTable.xRegisteredTable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationData;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfigurationReadPlatformServiceImplTest {

    private static final LocalDate DATE = LocalDate.of(2024, 3, 15);

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private GlobalConfigurationRepositoryWrapper configurationRepositoryWrapper;

    @Mock
    private SQLQueryFactory queryFactory;

    // RETURNS_SELF makes from/join/on/where/orderBy return the same mock, so only
    // fetch() needs stubbing.
    @Mock(answer = Answers.RETURNS_SELF)
    private SQLQuery<Tuple> query;

    private ConfigurationReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConfigurationReadPlatformServiceImpl(context, configurationRepositoryWrapper, queryFactory);
    }

    @Test
    void retrieveGlobalConfigurationWithoutSurveyReadsAllConfigurationsOrderedById() {
        SQLQuery<Tuple> query = givenRows(row(1L, "some-config", true, 5, Date.valueOf(DATE), "A description", "text", true),
                row(2L, "other-config", false, 10, null, null, null, false));

        GlobalConfigurationData result = service.retrieveGlobalConfiguration(false);

        assertEquals(List.of(
                new GlobalConfigurationPropertyData().setId(1L).setName("some-config").setEnabled(true).setValue(5L).setDateValue(DATE)
                        .setDescription("A description").setStringValue("text").setTrapDoor(true),
                new GlobalConfigurationPropertyData().setId(2L).setName("other-config").setEnabled(false).setValue(10L).setTrapDoor(false)),
                result.getGlobalConfiguration());
        verify(context).authenticatedUser();

        verify(query).from(cConfiguration);
        verify(query).orderBy(cConfiguration.id.asc());
        verify(query).fetch();
        verifyNoMoreInteractions(query);
    }

    @Test
    void retrieveGlobalConfigurationWithSurveyJoinsRegisteredTablesOfPpiCategory() {
        SQLQuery<Tuple> query = givenRows(row(7L, "survey-config", true, 1, null, null, null, false));

        GlobalConfigurationData result = service.retrieveGlobalConfiguration(true);

        assertEquals(1, result.getGlobalConfiguration().size());
        assertEquals("survey-config", result.getGlobalConfiguration().get(0).getName());
        verify(query).from(cConfiguration);
        verify(query).join(xRegisteredTable);
        verify(query).on(xRegisteredTable.registeredTableName.eq(cConfiguration.name));
        verify(query).where(xRegisteredTable.category.eq(DataTableApiConstant.CATEGORY_PPI));
        verify(query).orderBy(cConfiguration.id.asc());
        verify(query).fetch();
        verifyNoMoreInteractions(query);
    }

    @Test
    void retrieveGlobalConfigurationKeepsTheJdbcDefaultsForNullColumns() {
        givenRows(row(3L, "nullable-config", null, null, null, null, null, null));

        GlobalConfigurationPropertyData data = service.retrieveGlobalConfiguration(false).getGlobalConfiguration().get(0);

        assertEquals(3L, data.getId());
        assertEquals(0L, data.getValue());
        assertEquals(false, data.isEnabled());
        assertEquals(false, data.isTrapDoor());
        assertNull(data.getDateValue());
        assertNull(data.getDescription());
        assertNull(data.getStringValue());
    }

    @Test
    void retrieveGlobalConfigurationConvertsTheDateToLocalDate() {
        givenRows(row(4L, "dated-config", true, 0, Date.valueOf(DATE), null, null, false));

        GlobalConfigurationPropertyData data = service.retrieveGlobalConfiguration(false).getGlobalConfiguration().get(0);

        assertEquals(DATE, data.getDateValue());
    }

    @Test
    void retrieveGlobalConfigurationReturnsEmptyListWhenThereAreNoRows() {
        givenRows();

        GlobalConfigurationData result = service.retrieveGlobalConfiguration(true);

        assertNotNull(result.getGlobalConfiguration());
        assertTrue(result.getGlobalConfiguration().isEmpty());
    }

    @Test
    void retrieveGlobalConfigurationFillsInAllowedValuesOnlyForRestrictedConfigurations() {
        givenRows(row(5L, GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, true, null, null, null, "due-date", false),
                row(6L, "unrestricted-config", true, null, null, null, null, false));

        List<GlobalConfigurationPropertyData> result = service.retrieveGlobalConfiguration(false).getGlobalConfiguration();

        assertNotNull(result.get(0).getAllowedValues());
        assertNull(result.get(1).getAllowedValues());
    }

    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        when(queryFactory.select(cConfiguration.id, cConfiguration.name, cConfiguration.enabled, cConfiguration.value,
                cConfiguration.dateValue, cConfiguration.description, cConfiguration.stringValue, cConfiguration.isTrapDoor))
                .thenReturn(query);
        when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(Long id, String name, Boolean enabled, Integer value, Date dateValue, String description, String stringValue,
            Boolean trapDoor) {
        return Projections
                .tuple(cConfiguration.id, cConfiguration.name, cConfiguration.enabled, cConfiguration.value, cConfiguration.dateValue,
                        cConfiguration.description, cConfiguration.stringValue, cConfiguration.isTrapDoor)
                .newInstance(id, name, enabled, value, dateValue, description, stringValue, trapDoor);
    }
}
