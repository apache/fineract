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
package org.apache.fineract.portfolio.charge.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.charge.command.CreateChargeCommand;
import org.apache.fineract.portfolio.charge.command.DeleteChargeCommand;
import org.apache.fineract.portfolio.charge.command.UpdateChargeCommand;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.data.CreateChargeRequest;
import org.apache.fineract.portfolio.charge.data.CreateChargeResponse;
import org.apache.fineract.portfolio.charge.data.DeleteChargeRequest;
import org.apache.fineract.portfolio.charge.data.DeleteChargeResponse;
import org.apache.fineract.portfolio.charge.data.UpdateChargeRequest;
import org.apache.fineract.portfolio.charge.data.UpdateChargeResponse;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/charges")
@Component
@Tag(name = "Charges", description = "Its typical for MFIs to add extra costs for their financial products. These are typically Fees or Penalties.\n"
        + "\n" + "A Charge on fineract platform is what we use to model both Fees and Penalties.\n" + "\n"
        + "At present we support defining charges for use with Client accounts and both loan and saving products.")
@RequiredArgsConstructor
public class ChargesApiResource {

    private final ChargeReadPlatformService readPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CommandPipeline commandPipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Charges", description = "Returns the list of defined charges.\n" + "\n" + "Example Requests:\n" + "\n"
            + "charges")
    public List<ChargeData> retrieveAllCharges() {
        return readPlatformService.retrieveAllCharges();
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Charge", description = "Returns the details of a defined Charge.\n" + "\n" + "Example Requests:\n"
            + "\n" + "charges/1")
    public ChargeData retrieveCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Context final UriInfo uriInfo) {
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final ChargeData charge = readPlatformService.retrieveCharge(chargeId);
        return settings.isTemplate() ? ChargeData.withTemplate(charge, readPlatformService.retrieveNewChargeDetails()) : charge;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Charge Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "charges/template\n")
    public ChargeData retrieveNewChargeDetails() {
        return readPlatformService.retrieveNewChargeDetails();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create/Define a Charge", description = "Define a new charge that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    public CreateChargeResponse createCharge(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @Valid CreateChargeRequest chargeRequest) {
        CreateChargeCommand createChargeCommand = new CreateChargeCommand();
        createChargeCommand.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        createChargeCommand.setId(UUID.randomUUID());
        createChargeCommand.setPayload(chargeRequest);

        Supplier<CreateChargeResponse> result = commandPipeline.send(createChargeCommand);
        return result.get();
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Charge", description = "Updates the details of a Charge.")
    public UpdateChargeResponse updateCharge(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Valid UpdateChargeRequest updateChargeRequest) {

        UpdateChargeCommand updateCommand = new UpdateChargeCommand();
        updateCommand.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        updateCommand.setId(UUID.randomUUID());
        updateChargeRequest.setId(chargeId);
        updateCommand.setPayload(updateChargeRequest);

        Supplier<UpdateChargeResponse> result = commandPipeline.send(updateCommand);
        return result.get();
    }

    @DELETE
    @Path("{chargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Charge", description = "Deletes a Charge.")
    public DeleteChargeResponse deleteCharge(@HeaderParam("Idempotency-Key") String idempotencyKey,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {
        DeleteChargeCommand deleteChargeCommand = new DeleteChargeCommand();
        deleteChargeCommand.setId(UUID.randomUUID());
        deleteChargeCommand.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        deleteChargeCommand.setPayload(new DeleteChargeRequest(chargeId));

        Supplier<DeleteChargeResponse> result = commandPipeline.send(deleteChargeCommand);
        return result.get();
    }
}
