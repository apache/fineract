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
package org.apache.fineract.portfolio.account.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionHistoryData;
import org.apache.fineract.portfolio.account.service.StandingInstructionHistoryReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/standinginstructionrunhistory")
@Component
@Tag(name = "Standing Instructions History", description = "The list capability of history can support pagination and sorting.")
@RequiredArgsConstructor
public class StandingInstructionHistoryApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<StandingInstructionHistoryData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final StandingInstructionHistoryReadPlatformService standingInstructionHistoryReadPlatformService;
    private final SqlValidator sqlValidator;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Standing Instructions Logged History", description = "The list capability of history can support pagination and sorting \n\n"
            + "Example Requests :\n" + "\n" + "standinginstructionrunhistory\n" + "\n"
            + "standinginstructionrunhistory?orderBy=name&sortOrder=DESC\n" + "\n" + "standinginstructionrunhistory?offset=10&limit=50")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StandingInstructionHistoryApiResourceSwagger.GetStandingInstructionRunHistoryResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("transferType") @Parameter(description = "transferType") final Integer transferType,
            @QueryParam("clientName") @Parameter(description = "clientName") final String clientName,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("fromAccountId") @Parameter(description = "fromAccountId") final Long fromAccount,
            @QueryParam("fromAccountType") @Parameter(description = "fromAccountType") final Integer fromAccountType,
            @QueryParam("locale") @Parameter(description = "locale") final String locale,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String rawDateFormat,
            @QueryParam("fromDate") @Parameter(description = "fromDate") final DateParam fromDateParam,
            @QueryParam("toDate") @Parameter(description = "toDate") final DateParam toDateParam) {

        this.context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        final DateFormat dateFormat = StringUtils.isBlank(rawDateFormat) ? null : new DateFormat(rawDateFormat);

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();
        LocalDate startDateRange = null;
        LocalDate endDateRange = null;
        if (fromDateParam != null) {
            startDateRange = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        if (toDateParam != null) {
            endDateRange = toDateParam.getDate("toDate", dateFormat, locale);
        }

        StandingInstructionDTO standingInstructionDTO = new StandingInstructionDTO(searchParameters, transferType, clientName, clientId,
                fromAccount, fromAccountType, startDateRange, endDateRange);

        final Page<StandingInstructionHistoryData> history = this.standingInstructionHistoryReadPlatformService
                .retrieveAll(standingInstructionDTO);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, history);
    }
}
