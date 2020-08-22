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
package org.apache.fineract.spm.api;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.data.SurveyData;
import org.apache.fineract.spm.domain.Survey;
import org.apache.fineract.spm.service.SpmService;
import org.apache.fineract.spm.util.SurveyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/surveys")
@Component
@Scope("singleton")

@Tag(name = "Spm-Surveys", description = "")
public class SpmApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;

    @Autowired
    public SpmApiResource(final PlatformSecurityContext securityContext, final SpmService spmService) {
        this.securityContext = securityContext;
        this.spmService = spmService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(summary = "List all Surveys", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SurveyData.class)))) })
    public List<SurveyData> fetchAllSurveys(@QueryParam("isActive") final Boolean isActive) {
        this.securityContext.authenticatedUser();
        final List<SurveyData> result = new ArrayList<>();
        List<Survey> surveys = null;
        if (isActive != null && isActive) {
            surveys = this.spmService.fetchValidSurveys();
        } else {
            surveys = this.spmService.fetchAllSurveys();
        }
        if (surveys != null) {
            for (final Survey survey : surveys) {
                result.add(SurveyMapper.map(survey));
            }
        }
        return result;
    }

    @GET
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(summary = "Retrieve a Survey", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SurveyData.class))) })
    public SurveyData findSurvey(@PathParam("id") @Parameter(description = "Enter id") final Long id) {
        this.securityContext.authenticatedUser();
        final Survey survey = this.spmService.findById(id);
        return SurveyMapper.map(survey);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(summary = "Create a Survey", description = "Adds a new survey to collect client related data.\n" + "\n"
            + "Mandatory Fields\n" + "\n" + "countryCode, key, name, questions, responses, sequenceNo, text, description")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String createSurvey(@Parameter(description = "Create survey") final SurveyData surveyData) {
        this.securityContext.authenticatedUser();
        final Survey survey = SurveyMapper.map(surveyData, new Survey());
        this.spmService.createSurvey(survey);
        return getResponse(survey.getId());

    }

    @PUT
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public String editSurvey(@PathParam("id") final Long id, final SurveyData surveyData) {
        this.securityContext.authenticatedUser();
        final Survey surveyToUpdate = this.spmService.findById(id);
        final Survey survey = SurveyMapper.map(surveyData, surveyToUpdate);
        this.spmService.updateSurvey(survey);
        return getResponse(survey.getId());
    }

    @POST
    @Path("/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(summary = "Deactivate Survey", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public void activateOrDeactivateSurvey(@PathParam("id") final Long id, @QueryParam("command") final String command) {
        this.securityContext.authenticatedUser();
        if (command != null && command.equalsIgnoreCase("activate")) {
            this.spmService.activateSurvey(id);
        } else if (command != null && command.equalsIgnoreCase("deactivate")) {
            this.spmService.deactivateSurvey(id);
        } else {
            throw new UnrecognizedQueryParamException("command", command);
        }

    }

    private String getResponse(Long id) {
        Gson gson = new Gson();
        HashMap<String, Object> response = new HashMap<>();
        response.put("resourceId", id);
        return gson.toJson(response);
    }
}
