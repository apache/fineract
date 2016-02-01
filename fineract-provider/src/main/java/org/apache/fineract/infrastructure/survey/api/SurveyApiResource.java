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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.survey.data.ClientScoresOverview;
import org.apache.fineract.infrastructure.survey.data.SurveyData;
import org.apache.fineract.infrastructure.survey.data.SurveyDataTableData;
import org.apache.fineract.infrastructure.survey.service.ReadSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by Cieyou on 2/27/14.
 */
@Path("/survey")
@Component
@Scope("singleton")
public class SurveyApiResource {

    private final DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer;
    private final PlatformSecurityContext context;
    private final ReadSurveyService readSurveyService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final GenericDataService genericDataService;

    @Autowired
    public SurveyApiResource(final DefaultToApiJsonSerializer<SurveyData> toApiJsonSerializer, final PlatformSecurityContext context,
            final ReadSurveyService readSurveyService, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ClientScoresOverview> toApiJsonClientScoreOverviewSerializer,
            final GenericDataService genericDataService) {

        this.toApiJsonSerializer = toApiJsonSerializer;
        this.context = context;
        this.readSurveyService = readSurveyService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonClientScoreOverviewSerializer = toApiJsonClientScoreOverviewSerializer;
        this.genericDataService = genericDataService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSurveys() {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<SurveyDataTableData> surveys = this.readSurveyService.retrieveAllSurveys();
        return this.toApiJsonSerializer.serialize(surveys);
    }

    @GET
    @Path("{surveyName}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSurvey(@PathParam("surveyName") final String surveyName) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        SurveyDataTableData surveys = this.readSurveyService.retrieveSurvey(surveyName);

        return this.toApiJsonSerializer.serialize(surveys);

    }

    @POST
    @Path("{surveyName}/{apptableId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDatatableEntry(@PathParam("surveyName") final String datatable, @PathParam("apptableId") final Long apptableId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .fullFilSurvey(datatable, apptableId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    /** FIXME Vishwas what does this API really do? ***/
    @GET
    @Path("{surveyName}/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getClientSurveyOverview(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        List<ClientScoresOverview> scores = this.readSurveyService.retrieveClientSurveyScoreOverview(clientId);

        return this.toApiJsonClientScoreOverviewSerializer.serialize(scores);
    }

    @GET
    @Path("{surveyName}/{clientId}/{entryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getSurveyEntry(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId,
            @PathParam("entryId") final Long entryId) {

        this.context.authenticatedUser().validateHasReadPermission(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        final GenericResultsetData results = this.readSurveyService.retrieveSurveyEntry(surveyName, clientId, entryId);

        return this.genericDataService.generateJsonFromGenericResultsetData(results);

    }

    @PUT
    @Path("register/{surveyName}/{apptable}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String register(@PathParam("surveyName") final String datatable, @PathParam("apptable") final String apptable,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().registerSurvey(datatable, apptable)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{surveyName}/{clientId}/{fulfilledId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDatatableEntries(@PathParam("surveyName") final String surveyName, @PathParam("clientId") final Long clientId,
            @PathParam("fulfilledId") final Long fulfilledId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteDatatable(surveyName, clientId, fulfilledId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
