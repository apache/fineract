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
package org.apache.fineract.cob.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.ConfiguredJobNamesDTO;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.cob.data.JobBusinessStepDetail;
import org.apache.fineract.cob.service.ConfigJobParameterService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.stereotype.Component;

@Path("/v1/jobs")
@Component
@Tag(name = "Business Step Configuration", description = "")
@RequiredArgsConstructor
public class ConfigureBusinessStepApiResource {

    private static final Set<String> BUSINESS_STEP_CONFIG_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("jobName", "businessSteps"));
    private static final Set<String> BUSINESS_STEP_DETAIL_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("jobName", "availableBusinessSteps"));

    private final DefaultToApiJsonSerializer<JobBusinessStepConfigData> businessStepConfigSerializeService;
    private final DefaultToApiJsonSerializer<JobBusinessStepDetail> businessStepDetailSerializeService;
    private final DefaultToApiJsonSerializer<ConfiguredJobNamesDTO> configuredJobNamesSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ConfigJobParameterService configJobParameterService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @GET
    @Path("/names")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Business Jobs", description = "Returns the configured Business Jobs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ConfigureBusinessStepResourceSwagger.GetBusinessJobConfigResponse.class))) })
    public String retrieveAllConfiguredBusinessJobs(@Context final UriInfo uriInfo) {

        List<String> businessJobNames = configJobParameterService.getAllConfiguredJobNames();
        ConfiguredJobNamesDTO result = new ConfiguredJobNamesDTO(businessJobNames);

        return configuredJobNamesSerializer.serialize(result);
    }

    @GET
    @Path("{jobName}/steps")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Business Step Configurations for a Job", description = "Returns the configured Business Steps for a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ConfigureBusinessStepResourceSwagger.GetBusinessStepConfigResponse.class))) })
    public String retrieveAllConfiguredBusinessStep(@Context final UriInfo uriInfo,
            @PathParam("jobName") @Parameter(description = "jobName") final String jobName) {

        JobBusinessStepConfigData jobBusinessStepConfigData = configJobParameterService.getBusinessStepConfigByJobName(jobName);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return businessStepConfigSerializeService.serialize(settings, jobBusinessStepConfigData,
                BUSINESS_STEP_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{jobName}/steps")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Business Step Configurations for a Job", description = "Updates the Business steps execution order for a job")
    @RequestBody(content = @Content(schema = @Schema(implementation = ConfigureBusinessStepResourceSwagger.UpdateBusinessStepConfigRequest.class)))
    @ApiResponses({ @ApiResponse(responseCode = "204", description = "NO_CONTENT") })
    public Response updateJobBusinessStepConfig(@PathParam("jobName") @Parameter(description = "jobName") final String jobName,
            @Parameter(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateBusinessStepConfig(jobName).withJson(jsonRequestBody)
                .build();
        commandWritePlatformService.logCommandSource(commandRequest);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("{jobName}/available-steps")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Business Step Configurations for a Job", description = "Returns the available Business Steps for a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ConfigureBusinessStepResourceSwagger.GetBusinessStepConfigResponse.class))) })
    public String retrieveAllAvailableBusinessStep(@Context final UriInfo uriInfo,
            @PathParam("jobName") @Parameter(description = "jobName") final String jobName) {

        JobBusinessStepDetail availableBusinessStepsByJobName = configJobParameterService.getAvailableBusinessStepsByJobName(jobName);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return businessStepDetailSerializeService.serialize(settings, availableBusinessStepsByJobName,
                BUSINESS_STEP_DETAIL_RESPONSE_DATA_PARAMETERS);
    }
}
