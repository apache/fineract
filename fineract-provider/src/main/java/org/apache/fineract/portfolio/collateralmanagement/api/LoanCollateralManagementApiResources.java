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
package org.apache.fineract.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loan-collateral-management")
@Component
@Scope("singleton")
@Tag(name = "Loan Collateral Management", description = "Loan Collateral Management is for managing collateral operations")
public class LoanCollateralManagementApiResources {

    private final DefaultToApiJsonSerializer<LoanCollateralResponseData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    public LoanCollateralManagementApiResources(final DefaultToApiJsonSerializer<LoanCollateralResponseData> apiJsonSerializerService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final PlatformSecurityContext context,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.context = context;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.loanCollateralManagementReadPlatformService = loanCollateralManagementReadPlatformService;
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(description = "Delete Loan Collateral", summary = "Delete Loan Collateral")
    public String deleteLoanCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("id") @Parameter(description = "loan collateral id") final Long id) {

        final CommandWrapper commandWrapper = new CommandWrapperBuilder().deleteLoanCollateral(loanId, id).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.apiJsonSerializerService.serialize(commandProcessingResult);
    }

    @GET
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(description = "Get Loan Collateral Details", summary = "Get Loan Collateral Details")
    public String getLoanCollateral(@PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        final LoanCollateralResponseData loanCollateralResponseData = this.loanCollateralManagementReadPlatformService
                .getLoanCollateralResponseData(collateralId);
        return this.apiJsonSerializerService.serialize(loanCollateralResponseData);
    }

}
