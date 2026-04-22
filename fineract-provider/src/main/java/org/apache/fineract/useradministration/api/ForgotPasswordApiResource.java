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
package org.apache.fineract.useradministration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.useradministration.service.ForgotPasswordService;
import org.springframework.stereotype.Component;

@Path("/v1/password")
@Component
@Tag(name = "Password Management", description = "APIs for password management operations including forgot password functionality.")
@RequiredArgsConstructor
public class ForgotPasswordApiResource {

    private final ForgotPasswordService forgotPasswordService;

    @POST
    @Path("/forgot")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Request password reset", description = """
            Requests a password reset for the user with the given email.
            If the email exists and the user is active, a temporary password will be sent to the email address.
            The temporary password expires in 1 hour.""")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ForgotPasswordRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK")
    public Response forgotPassword(final ForgotPasswordRequest request) {
        this.forgotPasswordService.requestPasswordReset(request.email());
        return Response.ok().build();
    }

    public record ForgotPasswordRequest(String email) {
    }
}
