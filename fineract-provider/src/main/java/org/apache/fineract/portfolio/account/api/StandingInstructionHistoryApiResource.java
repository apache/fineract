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

import io.swagger.annotations.*;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionHistoryData;
import org.apache.fineract.portfolio.account.service.StandingInstructionHistoryReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Date;

@Path("/standinginstructionrunhistory")
@Component
@Scope("singleton")
@Api(value = "Standing Instructions History", description = "The list capability of history can support pagination and sorting.")
public class StandingInstructionHistoryApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<StandingInstructionHistoryData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final StandingInstructionHistoryReadPlatformService standingInstructionHistoryReadPlatformService;

    @Autowired
    public StandingInstructionHistoryApiResource(final PlatformSecurityContext context,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final StandingInstructionHistoryReadPlatformService standingInstructionHistoryReadPlatformService,
            final DefaultToApiJsonSerializer<StandingInstructionHistoryData> toApiJsonSerializer) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.standingInstructionHistoryReadPlatformService = standingInstructionHistoryReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Standing Instructions Logged History", httpMethod = "GET", notes = "The list capability of history can support pagination and sorting \n\n" +"Example Requests :\n" + "\n" + "standinginstructionrunhistory\n" + "\n" + "standinginstructionrunhistory?orderBy=name&sortOrder=DESC\n" + "\n" + "standinginstructionrunhistory?offset=10&limit=50")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = StandingInstructionHistoryApiResourceSwagger.GetStandingInstructionRunHistoryResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId, @QueryParam("offset") @ApiParam(value = "offset") final Integer offset,
            @QueryParam("limit") @ApiParam(value = "limit") final Integer limit, @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder, @QueryParam("transferType") @ApiParam(value = "transferType") final Integer transferType,
            @QueryParam("clientName") @ApiParam(value = "clientName") final String clientName, @QueryParam("clientId") @ApiParam(value = "clientId") final Long clientId,
            @QueryParam("fromAccountId") @ApiParam(value = "fromAccountId") final Long fromAccount, @QueryParam("fromAccountType") @ApiParam(value = "fromAccountType") final Integer fromAccountType,
            @QueryParam("locale") @ApiParam(value = "locale") final String locale, @QueryParam("dateFormat") @ApiParam(value = "dateFormat") final String dateFormat,
            @QueryParam("fromDate") @ApiParam(value = "fromDate") final DateParam fromDateParam, @QueryParam("toDate") @ApiParam(value = "toDate") final DateParam toDateParam) {

        this.context.authenticatedUser().validateHasReadPermission(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forAccountTransfer(sqlSearch, externalId, offset, limit, orderBy,
                sortOrder);
        Date startDateRange = null;
        Date endDateRange = null;
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