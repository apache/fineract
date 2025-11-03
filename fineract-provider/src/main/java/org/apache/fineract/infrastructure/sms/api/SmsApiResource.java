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

import static org.apache.fineract.infrastructure.core.api.DateParam.FROM_DATE_PARAM;
import static org.apache.fineract.infrastructure.core.api.DateParam.TO_DATE_PARAM;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.data.request.SmsCreationRequest;
import org.apache.fineract.infrastructure.sms.data.request.SmsUpdateRequest;
import org.apache.fineract.infrastructure.sms.param.SmsRequestParam;
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
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<String> apiJsonSerializer;

    @GET
    public List<SmsData> retrieveAll() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readPlatformService.retrieveAll();
    }

    @POST
    public CommandProcessingResult create(final SmsCreationRequest smsCreationRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSms()
                .withJson(apiJsonSerializer.serialize(smsCreationRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("{resourceId}")
    public SmsData retrieveOne(@PathParam("resourceId") final Long resourceId) {
        return readPlatformService.retrieveOne(resourceId);
    }

    @GET
    @Path("{campaignId}/messageByStatus")
    public Page<SmsData> retrieveAllSmsByStatus(@PathParam("campaignId") final Long campaignId,
            @BeanParam SmsRequestParam smsRequestParam) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final SearchParameters searchParameters = SearchParameters.builder().limit(smsRequestParam.getLimit())
                .offset(smsRequestParam.getOffset()).orderBy(smsRequestParam.getOrderBy()).sortOrder(smsRequestParam.getSortOrder())
                .build();

        final DateFormat dateFormat = Optional.ofNullable(smsRequestParam.getRawDateFormat()).map(DateFormat::new).orElse(null);
        final LocalDate fromDate = Optional.ofNullable(smsRequestParam.getFromDate())
                .map(fromDateParam -> fromDateParam.getDate(FROM_DATE_PARAM, dateFormat, smsRequestParam.getLocale())).orElse(null);
        final LocalDate toDate = Optional.ofNullable(smsRequestParam.getToDate())
                .map(toDateParam -> toDateParam.getDate(TO_DATE_PARAM, dateFormat, smsRequestParam.getLocale())).orElse(null);

        return readPlatformService.retrieveSmsByStatus(campaignId, searchParameters, smsRequestParam.getStatus().intValue(), fromDate,
                toDate);
    }

    @PUT
    @Path("{resourceId}")
    public CommandProcessingResult update(@PathParam("resourceId") final Long resourceId, final SmsUpdateRequest smsUpdateRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSms(resourceId)
                .withJson(apiJsonSerializer.serialize(smsUpdateRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{resourceId}")
    public CommandProcessingResult delete(@PathParam("resourceId") final Long resourceId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSms(resourceId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
