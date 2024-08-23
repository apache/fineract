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
package org.apache.fineract.infrastructure.sms.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.service.SmsReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/sms")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "SMS", description = "")
@RequiredArgsConstructor
public class SmsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "SMS";

    private final PlatformSecurityContext context;
    private final SmsReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<SmsData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final SqlValidator sqlValidator;

    @GET
    public String retrieveAll(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Collection<SmsData> smsMessages = readPlatformService.retrieveAll();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsMessages);
    }

    @POST
    public String create(final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSms().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{resourceId}")
    public String retrieveOne(@PathParam("resourceId") final Long resourceId, @Context final UriInfo uriInfo) {
        final SmsData smsMessage = readPlatformService.retrieveOne(resourceId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsMessage);
    }

    @GET
    @Path("{campaignId}/messageByStatus")
    public String retrieveAllSmsByStatus(@PathParam("campaignId") final Long campaignId, @Context final UriInfo uriInfo,
            @QueryParam("status") final Long status, @QueryParam("fromDate") final DateParam fromDateParam,
            @QueryParam("toDate") final DateParam toDateParam, @QueryParam("locale") final String locale,
            @QueryParam("dateFormat") final String rawDateFormat, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();

        final DateFormat dateFormat = StringUtils.isBlank(rawDateFormat) ? null : new DateFormat(rawDateFormat);

        LocalDate fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        LocalDate toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate("toDate", dateFormat, locale);
        }

        final Page<SmsData> smsMessages = readPlatformService.retrieveSmsByStatus(campaignId, searchParameters, status.intValue(), fromDate,
                toDate);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, smsMessages);
    }

    @PUT
    @Path("{resourceId}")
    public String update(@PathParam("resourceId") final Long resourceId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSms(resourceId).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{resourceId}")
    public String delete(@PathParam("resourceId") final Long resourceId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSms(resourceId).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }
}
