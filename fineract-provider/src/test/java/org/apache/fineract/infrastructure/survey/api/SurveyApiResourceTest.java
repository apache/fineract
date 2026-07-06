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
package org.apache.fineract.infrastructure.survey.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.ClientScoresOverview;
import org.apache.fineract.infrastructure.survey.data.SurveyData;
import org.apache.fineract.infrastructure.survey.service.ReadSurveyService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SurveyApiResourceTest {

    private static final String SURVEY_NAME = "ppi_kenya_2026";
    private static final Long CLIENT_ID = 11L;

    @Mock
    private DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer;
    @Mock
    private DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ReadSurveyService readSurveyService;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private GenericDataService genericDataService;
    @Mock
    private AppUser appUser;

    private SurveyApiResource underTest;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(appUser);
        underTest = new SurveyApiResource(toApiJsonSerializer, toApiJsonClientScoreOverviewSerializer, context, readSurveyService,
                commandsSourceWritePlatformService, genericDataService);
    }

    @Test
    void getClientSurveyOverviewRetrievesScoresForRequestedSurvey() {
        List<ClientScoresOverview> scores = List.of();
        when(readSurveyService.retrieveClientSurveyScoreOverview(SURVEY_NAME, CLIENT_ID)).thenReturn(scores);
        when(toApiJsonClientScoreOverviewSerializer.serialize(scores)).thenReturn("[]");

        underTest.getClientSurveyOverview(SURVEY_NAME, CLIENT_ID);

        verify(appUser).validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);
        verify(readSurveyService).retrieveClientSurveyScoreOverview(SURVEY_NAME, CLIENT_ID);
    }
}
