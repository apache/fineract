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
package org.apache.fineract.organisation.workingdays.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.workingdays.command.WorkingDaysUpdateCommand;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysData;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateRequest;
import org.apache.fineract.organisation.workingdays.data.WorkingDaysUpdateResponse;
import org.apache.fineract.organisation.workingdays.service.WorkingDaysReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/workingdays")
@Component
@Tag(name = "Working days", description = "The days of the week that are workdays.\n" + "\n"
        + "Rescheduling of repayments when it falls on a non-working is turned on /off by enable/disable reschedule-future-repayments parameter in Global configurations\n"
        + "\n"
        + "Allow transactions on non-working days is configurable by enabling/disbaling the allow-transactions-on-non-workingday parameter in Global configurations.")
@RequiredArgsConstructor
public class WorkingDaysApiResource {

    private final WorkingDaysReadPlatformService workingDaysReadPlatformService;
    private final PlatformSecurityContext context;
    private final CommandPipeline pipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Working days", description = "Example Requests:\n" + "\n" + "workingdays")
    public WorkingDaysData retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);
        return this.workingDaysReadPlatformService.retrieve();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Working Day", description = "Mandatory Fields\n"
            + "recurrence,repaymentRescheduleType,extendTermForDailyRepayments,locale")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingDaysUpdateRequest.class)))
    public WorkingDaysUpdateResponse update(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @Valid final WorkingDaysUpdateRequest request) {

        WorkingDaysUpdateCommand command = new WorkingDaysUpdateCommand();
        initCommand(idempotencyKey, command);
        command.setPayload(request);

        Supplier<WorkingDaysUpdateResponse> result = pipeline.send(command);
        return result.get();
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Working Days Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for working days.\n"
            + "\n" + "Example Request:\n" + "\n" + "workingdays/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingDaysApiResourceSwagger.GetWorkingDaysTemplateResponse.class))) })
    public WorkingDaysData template() {
        this.context.authenticatedUser().validateHasReadPermission(WorkingDaysApiConstants.WORKING_DAYS_RESOURCE_NAME);

        return this.workingDaysReadPlatformService.repaymentRescheduleType();
    }

    private void initCommand(String idempotencyKey, Command command) {
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
    }
}
