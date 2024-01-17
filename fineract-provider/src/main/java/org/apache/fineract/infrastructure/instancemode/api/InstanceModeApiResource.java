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
package org.apache.fineract.infrastructure.instancemode.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/instance-mode")
@Tag(name = "Instance Mode", description = "Instance mode changing API")
@RequiredArgsConstructor
@Slf4j
public class InstanceModeApiResource implements InitializingBean {

    private final FineractProperties fineractProperties;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() throws Exception {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Instance type changing feature is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Changes the Fineract instance mode", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InstanceModeApiResourceSwagger.ChangeInstanceModeRequest.class)))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public Response changeMode(InstanceModeApiResourceSwagger.ChangeInstanceModeRequest request) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Changing instance mode according to the request parameters {}", request);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");
        fineractProperties.getMode().setReadEnabled(request.isReadEnabled());
        fineractProperties.getMode().setWriteEnabled(request.isWriteEnabled());
        fineractProperties.getMode().setBatchWorkerEnabled(request.isBatchWorkerEnabled());
        fineractProperties.getMode().setBatchManagerEnabled(request.isBatchManagerEnabled());
        return Response.ok().build();
    }
}
