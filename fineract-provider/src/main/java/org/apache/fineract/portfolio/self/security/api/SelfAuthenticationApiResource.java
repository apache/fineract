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
package org.apache.fineract.portfolio.self.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResourceSwagger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@Path("/v1/self/authentication")
@Tag(name = "Self Authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed")
@RequiredArgsConstructor
public class SelfAuthenticationApiResource {

    private final AuthenticationApiResource authenticationApiResource;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed.\n\n"
            + "Please visit this link for more info - https://fineract.apache.org/legacy-docs/apiLive.htm#selfbasicauth")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfAuthenticationApiResourceSwagger.PostSelfAuthenticationResponse.class))) })
    public String authenticate(final String apiRequestBodyAsJson) {
        return this.authenticationApiResource.authenticate(apiRequestBodyAsJson, true);
    }
}
