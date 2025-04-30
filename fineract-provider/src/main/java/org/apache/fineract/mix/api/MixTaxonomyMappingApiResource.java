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
package org.apache.fineract.mix.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.mix.command.MixTaxonomyCommand;
import org.apache.fineract.mix.data.MixTaxonomyMappingRequest;
import org.apache.fineract.mix.data.MixTaxonomyMappingResponse;
import org.apache.fineract.mix.service.MixTaxonomyMappingReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/mixmapping")
@Component
@Tag(name = "Mix Mapping", description = "")
@RequiredArgsConstructor
public class MixTaxonomyMappingApiResource {

    private final PlatformSecurityContext context;
    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final CommandPipeline pipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public MixTaxonomyMappingResponse retrieveTaxonomyMapping() {
        this.context.authenticatedUser();
        return this.readTaxonomyMappingService.retrieveTaxonomyMapping();
    }

    // TODO support multiple configuration file loading ?
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public MixTaxonomyMappingResponse updateTaxonomyMapping(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            final MixTaxonomyMappingRequest request) {

        final Long mappingId = (long) 1;
        var command = new MixTaxonomyCommand();

        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);

        request.setMappingId(mappingId);
        command.setPayload(request);

        Supplier<MixTaxonomyMappingResponse> result = pipeline.send(command);
        return result.get();
    }

}
