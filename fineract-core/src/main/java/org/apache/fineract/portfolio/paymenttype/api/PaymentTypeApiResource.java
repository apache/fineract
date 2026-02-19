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
package org.apache.fineract.portfolio.paymenttype.api;

import io.swagger.v3.oas.annotations.Operation;
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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.paymenttype.command.PaymentTypeCreateCommand;
import org.apache.fineract.portfolio.paymenttype.command.PaymentTypeDeleteCommand;
import org.apache.fineract.portfolio.paymenttype.command.PaymentTypeUpdateCommand;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeCreateRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeCreateResponse;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDeleteRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDeleteResponse;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeUpdateRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeUpdateResponse;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.springframework.stereotype.Component;

@Path("/v1/paymenttypes")
@Component
@Tag(name = "Payment Type", description = "This defines the payment type")
@RequiredArgsConstructor
public class PaymentTypeApiResource {

    private final PaymentTypeReadService readPlatformService;
    private final CommandPipeline commandPipeline;
    private final DefaultToApiJsonSerializer<PaymentTypeData> jsonSerializer;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types", description = "Retrieve list of payment types")
    public List<PaymentTypeData> getAllPaymentTypes(@QueryParam("onlyWithCode") final boolean onlyWithCode) {
        return onlyWithCode ? readPlatformService.retrieveAllPaymentTypesWithCode() : readPlatformService.retrieveAllPaymentTypes();
    }

    @GET
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a Payment Type", description = "Retrieves a payment type")
    public PaymentTypeData retrieveOnePaymentType(@PathParam("paymentTypeId") final Long paymentTypeId) {
        return readPlatformService.retrieveOne(paymentTypeId);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Payment Type", description = "Creates a new Payment type")
    public PaymentTypeCreateResponse createPaymentType(PaymentTypeCreateRequest request) {
        final var command = new PaymentTypeCreateCommand();

        command.setPayload(request);

        final Supplier<PaymentTypeCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Payment Type", description = "Updates a Payment Type")
    public PaymentTypeUpdateResponse updatePaymentType(@PathParam("paymentTypeId") final Long paymentTypeId,
            final PaymentTypeUpdateRequest request) {

        request.setId(paymentTypeId);

        final var command = new PaymentTypeUpdateCommand();

        command.setPayload(request);

        final Supplier<PaymentTypeUpdateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @DELETE
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Payment Type", description = "Deletes payment type")
    public PaymentTypeDeleteResponse deleteCode(@PathParam("paymentTypeId") final Long paymentTypeId) {

        final var command = new PaymentTypeDeleteCommand();

        command.setPayload(PaymentTypeDeleteRequest.builder().id(paymentTypeId).build());

        final Supplier<PaymentTypeDeleteResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
