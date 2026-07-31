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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.portfolio.charge.command.ChargeCreateCommand;
import org.apache.fineract.portfolio.charge.command.ChargeDeleteCommand;
import org.apache.fineract.portfolio.charge.command.ChargeUpdateCommand;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeCreateResponse;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteRequest;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteResponse;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateResponse;
import org.apache.fineract.portfolio.charge.service.ChargeReadService;
import org.springframework.stereotype.Component;

@Path("/v1/charges")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Charges", description = """
        Its typical for MFIs to add extra costs for their financial products. These are typically Fees or Penalties.

        A Charge on fineract platform is what we use to model both Fees and Penalties.

        At present we support defining charges for use with Client accounts and both loan and saving products.""")
@RequiredArgsConstructor
public class ChargesApiResource {

    private final CommandDispatcher dispatcher;
    private final ChargeReadService chargeReadService;

    @GET
    @Operation(summary = "Retrieve Charges", operationId = "retrieveAllCharges", description = """
            Returns the list of defined charges.

            Example Requests:

            charges""")
    public List<ChargeData> retrieveAllCharges() {
        return chargeReadService.retrieveAllCharges();
    }

    @GET
    @Path("{chargeId}")
    @Operation(summary = "Retrieve a Charge", operationId = "retrieveOneCharge", description = """
            Returns the details of a defined Charge.

            Example Requests:

            charges/1""")
    @AlternativeOperationId("retrieveCharge")
    public ChargeData retrieveCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @QueryParam("template") @Parameter(description = "template") final boolean template) {
        ChargeData charge = chargeReadService.retrieveCharge(chargeId);
        if (template) {
            final ChargeData templateData = chargeReadService.retrieveNewChargeDetails(charge.getChargeAppliesTo().getId(),
                    charge.getChargeTimeType().getId());
            charge = ChargeData.withTemplate(charge, templateData);
        }
        return charge;
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Charge Template", operationId = "retrieveTemplateCharge", description = """
            This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:

            Field Defaults
            Allowed description Lists
            Example Request:

            charges/template
            """)
    @AlternativeOperationId("retrieveNewChargeDetails")
    public ChargeData retrieveNewChargeDetails(@QueryParam("chargeAppliesTo") Long chargeAppliesTo,
            @QueryParam("chargeTimeType") Long chargeTimeType) {
        return chargeReadService.retrieveNewChargeDetails(chargeAppliesTo, chargeTimeType);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create/Define a Charge", operationId = "createCharge", description = "Define a new charge that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    public ChargeCreateResponse createCharge(@RequestBody(required = true) @Valid final ChargeCreateRequest chargeRequest) {
        final ChargeCreateCommand command = new ChargeCreateCommand();
        command.setPayload(chargeRequest);

        final Supplier<ChargeCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Charge", operationId = "updateCharge", description = "Updates the details of a Charge.")
    public ChargeUpdateResponse updateCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @RequestBody(required = true) @Valid final ChargeUpdateRequest chargeRequest) {
        chargeRequest.setId(chargeId);

        final ChargeUpdateCommand command = new ChargeUpdateCommand();
        command.setPayload(chargeRequest);

        final Supplier<ChargeUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @DELETE
    @Path("{chargeId}")
    @Operation(summary = "Delete a Charge", operationId = "deleteCharge", description = "Deletes a Charge.")
    public ChargeDeleteResponse deleteCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {
        final ChargeDeleteCommand command = new ChargeDeleteCommand();
        command.setPayload(new ChargeDeleteRequest(chargeId));

        final Supplier<ChargeDeleteResponse> response = dispatcher.dispatch(command);

        return response.get();
    }
}
