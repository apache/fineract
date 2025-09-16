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
package org.apache.fineract.commands.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.commands.command.ApproveRejectMakerCheckerCommand;
import org.apache.fineract.commands.command.DeleteMakerCheckerCommand;
import org.apache.fineract.commands.data.ApproveRejectMakerCheckerRequest;
import org.apache.fineract.commands.data.ApproveRejectMakerCheckerResponse;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.data.DeleteMakerCheckerRequest;
import org.apache.fineract.commands.data.DeleteMakerCheckerResponse;
import org.apache.fineract.commands.data.request.MakerCheckerRequest;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.stereotype.Component;

@Path("/v1/makercheckers")
@Component
@Tag(name = "Maker Checker (or 4-eye) functionality")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@RequiredArgsConstructor
public class MakercheckersApiResource {

    private final AuditReadPlatformService readPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CommandPipeline commandPipeline;

    @GET
    @Operation(summary = "List Maker Checker Entries", description = "Get a list of entries that can be checked by the requestor that match the criteria supplied.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers\n" + "\n" + "makercheckers?fields=madeOnDate,maker,processingResult\n"
            + "\n" + "makercheckers?makerDateTimeFrom=2013-03-25 08:00:00&makerDateTimeTo=2013-04-04 18:00:00\n" + "\n"
            + "makercheckers?officeId=1\n" + "\n" + "makercheckers?officeId=1&includeJson=true")
    public List<AuditData> retrieveCommands(@Context final UriInfo uriInfo, @BeanParam MakerCheckerRequest makerCheckerRequest) {
        final SQLBuilder extraCriteria = getExtraCriteria(makerCheckerRequest);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return readPlatformService.retrieveAllEntriesToBeChecked(extraCriteria, settings.isIncludeJson());
    }

    @GET
    @Path("/searchtemplate")
    @Operation(summary = "Maker Checker Search Template", description = "This is a convenience resource. It can be useful when building a Checker Inbox UI. \"appUsers\" are data scoped to the office/branch the requestor is associated with. \"actionNames\" and \"entityNames\" returned are those that the requestor has Checker approval permissions for.\n"
            + "\n" + "Example Requests:\n" + "\n" + "makercheckers/searchtemplate\n" + "makercheckers/searchtemplate?fields=entityNames")
    public AuditSearchData retrieveAuditSearchTemplate() {
        return readPlatformService.retrieveSearchTemplate("makerchecker");
    }

    @POST
    @Path("{auditId}")
    @Operation(summary = "Approve Maker Checker Entry | Reject Maker Checker Entry")
    public ApproveRejectMakerCheckerResponse approveMakerCheckerEntry(
            @PathParam("auditId") @Parameter(description = "auditId") @PositiveOrZero(message = "{org.apache.fineract.commands.makerchecker.auditId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.commands.makerchecker.auditId.digits}") final Long auditId,
            @QueryParam("command") @Parameter(description = "command") @NotBlank(message = "{org.apache.fineract.commands.makerchecker.command.param.notblank}") @Pattern(regexp = "^(approve|reject)$", message = "{org.apache.fineract.commands.makerchecker.command.param.pattern}") final String commandParam) {

        final ApproveRejectMakerCheckerRequest request = ApproveRejectMakerCheckerRequest.builder().auditId(auditId)
                .commandParam(commandParam).build();

        final ApproveRejectMakerCheckerCommand command = new ApproveRejectMakerCheckerCommand();
        command.setPayload(request);
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setId(UUID.randomUUID());

        final Supplier<ApproveRejectMakerCheckerResponse> response = commandPipeline.send(command);
        return response.get();
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{auditId}")
    @Operation(summary = "Delete Maker Checker Entry")
    public DeleteMakerCheckerResponse deleteMakerCheckerEntry(
            @PathParam("auditId") @Parameter(description = "auditId") @PositiveOrZero(message = "{org.apache.fineract.commands.makerchecker.auditId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.commands.makerchecker.auditId.digits}") final Long auditId) {

        final DeleteMakerCheckerRequest request = DeleteMakerCheckerRequest.builder().auditId(auditId).build();
        final DeleteMakerCheckerCommand command = new DeleteMakerCheckerCommand();

        command.setPayload(request);
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setId(UUID.randomUUID());

        final Supplier<DeleteMakerCheckerResponse> response = commandPipeline.send(command);
        return response.get();
    }

    private SQLBuilder getExtraCriteria(MakerCheckerRequest makerCheckerRequest) {

        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", makerCheckerRequest.getActionName());
        if (makerCheckerRequest.getEntityName() != null) {
            extraCriteria.addCriteria("aud.entity_name like ", makerCheckerRequest.getEntityName() + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", makerCheckerRequest.getResourceId());
        extraCriteria.addNonNullCriteria("aud.maker_id = ", makerCheckerRequest.getMakerId());
        extraCriteria.addNonNullCriteria("aud.made_on_date >= ", makerCheckerRequest.getMakerDateTimeFrom());
        extraCriteria.addNonNullCriteria("aud.made_on_date <= ", makerCheckerRequest.getMakerDateTimeTo());
        extraCriteria.addNonNullCriteria("aud.office_id = ", makerCheckerRequest.getOfficeId());
        extraCriteria.addNonNullCriteria("aud.group_id = ", makerCheckerRequest.getGroupId());
        extraCriteria.addNonNullCriteria("aud.client_id = ", makerCheckerRequest.getClientId());
        extraCriteria.addNonNullCriteria("aud.loan_id = ", makerCheckerRequest.getLoanId());
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", makerCheckerRequest.getSavingsAccountId());

        return extraCriteria;
    }
}
