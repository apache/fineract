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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.api.UserDetailsApiResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Path("/v1/self/userdetails")
@Component
@ConditionalOnProperty("fineract.security.oauth.enabled")
@Tag(name = "Self User Details", description = "")
@RequiredArgsConstructor
public class SelfUserDetailsApiResource {

    private final UserDetailsApiResource userDetailsApiResource;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Fetch authenticated user details", description = "Checks the Authentication and returns the set roles and permissions allowed\n\n"
            + "For more info visit this link - https://fineract.apache.org/legacy-docs/apiLive.htm#selfoauth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfUserDetailsApiResourceSwagger.GetSelfUserDetailsResponse.class))) })
    public String fetchAuthenticatedUserData() {
        return this.userDetailsApiResource.fetchAuthenticatedUserData();
    }
}
