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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans/{loanId}/disbursements")
@Component
@Tag(name = "Loan Disbursement Details", description = "")
@RequiredArgsConstructor
public class LoanDisbursementDetailApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;

    @PUT
    @Path("{disbursementId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanDisbursementDetailApiResourceSwagger.PutLoansLoanIdDisbursementsDisbursementIdRequest.class)))
    public CommandProcessingResult updateDisbursementDate(@PathParam("loanId") final Long loanId,
            @PathParam("disbursementId") final Long disbursementId, @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDisbusementDate(loanId, disbursementId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("editDisbursements")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanDisbursementDetailApiResourceSwagger.PostAddAndDeleteDisbursementDetailRequest.class)))
    public CommandProcessingResult addAndDeleteDisbursementDetail(@PathParam("loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        CommandWrapper commandRequest = new CommandWrapperBuilder().addAndDeleteDisbursementDetails(loanId).withJson(apiRequestBodyAsJson)
                .build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("{disbursementId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponse(responseCode = "default", description = "default response", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "string")))
    public DisbursementData retriveDetail(@PathParam("loanId") final Long loanId, @PathParam("disbursementId") final Long disbursementId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.loanReadPlatformService.retrieveLoanDisbursementDetail(loanId, disbursementId);
    }

}
