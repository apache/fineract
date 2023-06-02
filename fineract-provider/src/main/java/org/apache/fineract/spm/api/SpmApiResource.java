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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.data.SurveyData;
import org.apache.fineract.spm.domain.Survey;
import org.apache.fineract.spm.service.SpmService;
import org.apache.fineract.spm.util.SurveyMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/v1/surveys")
@Component
@Tag(name = "Spm-Surveys", description = "")
@RequiredArgsConstructor
public class SpmApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;

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
