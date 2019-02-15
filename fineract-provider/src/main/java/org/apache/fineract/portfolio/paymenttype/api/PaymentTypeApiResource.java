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

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/paymenttypes")
@Component
@Api(value = "Payment Type", description = "This defines the payment type")
public class PaymentTypeApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<PaymentTypeData> jsonSerializer;
    private final PaymentTypeReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    // private final String resourceNameForPermissions = "PAYMENT_TYPE";
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;

    // private final Set<String> RESPONSE_DATA_PARAMETERS = new
    // HashSet<>(Arrays.asList("id", "value", "description", "isCashPayment"));

    @Autowired
    public PaymentTypeApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<PaymentTypeData> jsonSerializer,
            PaymentTypeReadPlatformService readPlatformService, PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper,
            ApiRequestParameterHelper apiRequestParameterHelper, PortfolioCommandSourceWritePlatformService commandWritePlatformService) {
        super();
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
        this.paymentTypeRepositoryWrapper = paymentTypeRepositoryWrapper;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandWritePlatformService = commandWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve all Payment Types", httpMethod = "GET", notes = "Retrieve list of payment types")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = PaymentTypeApiResourceSwagger.GetPaymentTypesResponse.class)})
    public String getAllPaymentTypes(@Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.resourceNameForPermissions);
        final Collection<PaymentTypeData> paymentTypes = this.readPlatformService.retrieveAllPaymentTypes();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypes, PaymentTypeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve a Payment Type", httpMethod = "GET", notes = "Retrieves a payment type")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = PaymentTypeApiResourceSwagger.GetPaymentTypesPaymentTypeIdResponse.class)})
    public String retrieveOnePaymentType(@PathParam("paymentTypeId") @ApiParam(value = "paymentTypeId") final Long paymentTypeId, @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.resourceNameForPermissions);
        this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final PaymentTypeData paymentTypes = this.readPlatformService.retrieveOne(paymentTypeId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypes, PaymentTypeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Payment Type", httpMethod = "POST", notes = "Creates a new Payment type\n\n" + "Mandatory Fields: name\n\n" + "Optional Fields: Description, isCashPayment,Position")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = PaymentTypeApiResourceSwagger.PostPaymentTypesRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = PaymentTypeApiResourceSwagger.PostPaymentTypesResponse.class)})
    public String createPaymentType(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createPaymentType().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Payment Type", httpMethod = "PUT", notes = "Updates a Payment Type")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = PaymentTypeApiResourceSwagger.PutPaymentTypesPaymentTypeIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = PaymentTypeApiResourceSwagger.PutPaymentTypesPaymentTypeIdResponse.class)})
    public String updatePaymentType(@PathParam("paymentTypeId") @ApiParam(value = "paymentTypeId") final Long paymentTypeId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaymentType(paymentTypeId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Payment Type", httpMethod = "DELETE", notes = "Deletes payment type")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = PaymentTypeApiResourceSwagger.DeletePaymentTypesPaymentTypeIdResponse.class)})
    public String deleteCode(@PathParam("paymentTypeId") @ApiParam(value = "paymentTypeId") final Long paymentTypeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePaymentType(paymentTypeId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

}
