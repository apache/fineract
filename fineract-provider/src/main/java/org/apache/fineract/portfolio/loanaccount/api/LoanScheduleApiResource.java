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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans/{loanId}/schedule")
@Component
@Tag(name = "Loan Rescheduling", description = "Loan Term Variations provides the ability to change due dates, amounts and number of instalments before loan approval.")
@RequiredArgsConstructor
public class LoanScheduleApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate loan repayment schedule based on Loan term variations | Updates loan repayment schedule based on Loan term variations | Updates loan repayment schedule by removing Loan term variations", description = "Calculate loan repayment schedule based on Loan term variations:\n\n"
            + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule based on Loan term variations:\n\n"
            + "Mandatory Fields: exceptions,locale,dateFormat\n\n" + "Updates loan repayment schedule by removing Loan term variations:\n\n"
            + "It updates the loan repayment schedule by removing Loan term variations\n\n"
            + "Showing request/response for 'Updates loan repayment schedule by removing Loan term variations'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanScheduleApiResourceSwagger.PostLoansLoanIdScheduleResponse.class))) })
    public String calculateLoanScheduleOrSubmitVariableSchedule(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        CommandWrapper commandRequest = null;
        if (is(commandParam, "calculateLoanSchedule")) {
            this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
            final LoanScheduleData loanSchedule = this.calculationPlatformService.generateLoanScheduleForVariableInstallmentRequest(loanId,
                    apiRequestBodyAsJson);

            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, loanSchedule, new HashSet<>());
        } else if (is(commandParam, "addVariations")) {
            commandRequest = new CommandWrapperBuilder().createScheduleExceptions(loanId).withJson(apiRequestBodyAsJson).build();
        } else if (is(commandParam, "deleteVariations")) {
            commandRequest = new CommandWrapperBuilder().deleteScheduleExceptions(loanId).build();
        }

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
