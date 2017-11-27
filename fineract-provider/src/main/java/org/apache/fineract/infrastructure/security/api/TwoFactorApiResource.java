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
package org.apache.fineract.infrastructure.security.api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.data.AccessTokenData;
import org.apache.fineract.infrastructure.security.data.OTPDeliveryMethod;
import org.apache.fineract.infrastructure.security.data.OTPMetadata;
import org.apache.fineract.infrastructure.security.data.OTPRequest;
import org.apache.fineract.infrastructure.security.domain.TFAccessToken;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TwoFactorService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/twofactor")
@Component
@Profile("twofactor")
@Scope("singleton")
public class TwoFactorApiResource {


    private final ToApiJsonSerializer<OTPMetadata> otpRequestSerializer;
    private final ToApiJsonSerializer<OTPDeliveryMethod> otpDeliveryMethodSerializer;
    private final ToApiJsonSerializer<AccessTokenData> accessTokenSerializer;
    private final DefaultToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final TwoFactorService twoFactorService;



    @Autowired
    public TwoFactorApiResource(ToApiJsonSerializer<OTPMetadata> otpRequestSerializer,
                                ToApiJsonSerializer<OTPDeliveryMethod> otpDeliveryMethodSerializer,
                                ToApiJsonSerializer<AccessTokenData> accessTokenSerializer,
                                DefaultToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer,
                                PlatformSecurityContext context,
                                PortfolioCommandSourceWritePlatformService
                                            commandsSourceWritePlatformService,
                                TwoFactorService twoFactorService) {
        this.otpRequestSerializer = otpRequestSerializer;
        this.otpDeliveryMethodSerializer = otpDeliveryMethodSerializer;
        this.accessTokenSerializer = accessTokenSerializer;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.twoFactorService = twoFactorService;
    }


    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String getOTPDeliveryMethods(@Context final UriInfo uriInfo) {
        AppUser user = context.authenticatedUser();

        List<OTPDeliveryMethod> otpDeliveryMethods = twoFactorService.getDeliveryMethodsForUser(user);
        return this.otpDeliveryMethodSerializer.serialize(otpDeliveryMethods);
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String requestToken(@QueryParam("deliveryMethod") final String deliveryMethod,
                               @QueryParam("extendedToken") @DefaultValue("false") boolean extendedAccessToken,
                               @Context final UriInfo uriInfo) {
        final AppUser user = context.authenticatedUser();

        final OTPRequest request = twoFactorService.createNewOTPToken(user, deliveryMethod, extendedAccessToken);
        return this.otpRequestSerializer.serialize(request.getMetadata());
    }

    @Path("validate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String validate(@QueryParam("token") final String token) {
        final AppUser user = context.authenticatedUser();

        TFAccessToken accessToken = twoFactorService.createAccessTokenFromOTP(user, token);

        return accessTokenSerializer.serialize(accessToken.toTokenData());
    }

    @Path("invalidate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfiguration(final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .invalidateTwoFactorAccessToken().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.
                logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
