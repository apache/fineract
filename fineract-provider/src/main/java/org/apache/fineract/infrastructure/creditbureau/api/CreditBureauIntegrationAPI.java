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

package org.apache.fineract.infrastructure.creditbureau.api;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.creditbureau.data.CreditReportData;
import org.apache.fineract.infrastructure.creditbureau.service.CreditReportReadPlatformService;
import org.apache.fineract.infrastructure.creditbureau.service.CreditReportWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Path("/creditBureauIntegration")
@Component
@Scope("singleton")
public class CreditBureauIntegrationAPI {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "creditBureauId", "nrc", "creditReport"));

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<CreditReportData> toCreditReportApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CreditReportWritePlatformService creditReportWritePlatformService;
    private final CreditReportReadPlatformService creditReportReadPlatformService;
    private final DefaultToApiJsonSerializer<CreditReportData> toApiJsonSerializer;
    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauIntegrationAPI.class);

    @Autowired
    public CreditBureauIntegrationAPI(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<CreditReportData> toCreditReportApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final CreditReportWritePlatformService creditReportWritePlatformService,
            final CreditReportReadPlatformService creditReportReadPlatformService,
            final DefaultToApiJsonSerializer<CreditReportData> toApiJsonSerializer) {
        this.context = context;
        this.toCreditReportApiJsonSerializer = toCreditReportApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.creditReportWritePlatformService = creditReportWritePlatformService;
        this.creditReportReadPlatformService = creditReportReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;

    }

    @POST
    @Path("creditReport")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String fetchCreditReport(@Context final UriInfo uriInfo, @RequestParam("params") final Map<String, Object> params) {

        Gson gson = new Gson();
        final String json = gson.toJson(params);
        final CommandWrapper commandRequest = new CommandWrapperBuilder().getCreditReport().withJson(json).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toCreditReportApiJsonSerializer.serialize(result);

    }

    // submit loan file of clients to Credit Bureau
    @POST
    @Path("addCreditReport")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload credit report", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String addCreditReport(@FormDataParam("file") final File creditReport, @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") final UriInfo uriInfo, @FormDataParam("file") FormDataContentDisposition fileDetail,
            @QueryParam("creditBureauId") @Parameter(description = "creditBureauId") final Long creditBureauId) {

        final String responseMessage = this.creditReportWritePlatformService.addCreditReport(creditBureauId, creditReport, fileDetail);
        return this.toCreditReportApiJsonSerializer.serialize(responseMessage);
    }

    // saves fetched-creditreport into database
    @POST
    @Path("saveCreditReport")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String saveCreditReport(@Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("creditBureauId") @Parameter(description = "creditBureauId") final Long creditBureauId,
            @QueryParam("nationalId") @Parameter(description = "nationalId") final String nationalId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .saveCreditReport(creditBureauId, nationalId).withJson(apiRequestBodyAsJson) // apiRequestBodyAsJson is
                                                                                             // a creditReport
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toCreditReportApiJsonSerializer.serialize(result);

    }

    // fetch saved creditReports(NRC) from DB by creditBureauId, to select for downloading and deleting the reports
    @GET
    @Path("creditReport/{creditBureauId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getSavedCreditReport(@PathParam("creditBureauId") @Parameter(description = "creditBureauId") final Long creditBureauId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser();

        final Collection<CreditReportData> creditReport = this.creditReportReadPlatformService.retrieveCreditReportDetails(creditBureauId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, creditReport, RESPONSE_DATA_PARAMETERS);

    }

    // deletes saved creditReports from database
    @DELETE
    @Path("deleteCreditReport/{creditBureauId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCreditReport(@PathParam("creditBureauId") @Parameter(description = "creditBureauId") final Long creditBureauId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCreditReport(creditBureauId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toCreditReportApiJsonSerializer.serialize(result);

    }
}
