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
package org.apache.fineract.infrastructure.survey.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadService;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.survey.data.ClientScoresOverview;
import org.apache.fineract.infrastructure.survey.data.LikelihoodStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReadSurveyServiceImplTest {

    private static final Long USER_ID = 7L;
    private static final Long CLIENT_ID = 11L;
    private static final String SURVEY_NAME = "ppi_kenya_2026";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private AppUser appUser;

    private ReadSurveyServiceImpl underTest;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(appUser);
        when(appUser.getId()).thenReturn(USER_ID);
        underTest = new ReadSurveyServiceImpl(context, jdbcTemplate, sqlValidator, genericDataService, datatableReadService, sqlGenerator);
    }

    @Test
    void retrieveClientSurveyScoreOverviewEscapesRegisteredSurveyNameAsSqlIdentifier() {
        SqlRowSet surveyNames = Mockito.mock(SqlRowSet.class);
        when(surveyNames.next()).thenReturn(true);
        when(surveyNames.getString("name")).thenReturn(SURVEY_NAME);

        SqlRowSet scoreRows = Mockito.mock(SqlRowSet.class);
        when(scoreRows.next()).thenReturn(false);

        when(jdbcTemplate.queryForRowSet(anyString(), any(Object[].class))).thenReturn(surveyNames, scoreRows);
        when(sqlGenerator.escape(SURVEY_NAME)).thenReturn("\"" + SURVEY_NAME + "\"");

        List<ClientScoresOverview> result = underTest.retrieveClientSurveyScoreOverview(SURVEY_NAME, CLIENT_ID);

        assertThat(result).isEmpty();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, times(2)).queryForRowSet(sqlCaptor.capture(), paramsCaptor.capture());

        String scoreSql = sqlCaptor.getAllValues().get(1);
        Object[] scoreParams = paramsCaptor.getAllValues().get(1);

        assertThat(scoreSql).contains("FROM \"" + SURVEY_NAME + "\" tz");
        assertThat(scoreSql).doesNotContain("FROM ? tz");
        assertThat(scoreParams).containsExactly(SURVEY_NAME, LikelihoodStatus.ENABLED, CLIENT_ID);
        verify(sqlValidator, times(2)).validate(SURVEY_NAME);
    }

    @Test
    void retrieveClientSurveyScoreOverviewReturnsEmptyListWhenSurveyIsNotPermitted() {
        SqlRowSet surveyNames = Mockito.mock(SqlRowSet.class);
        when(surveyNames.next()).thenReturn(false);
        when(jdbcTemplate.queryForRowSet(anyString(), any(Object[].class))).thenReturn(surveyNames);

        List<ClientScoresOverview> result = underTest.retrieveClientSurveyScoreOverview(SURVEY_NAME, CLIENT_ID);

        assertThat(result).isEmpty();
        verify(jdbcTemplate, times(1)).queryForRowSet(anyString(), any(Object[].class));
    }
}
