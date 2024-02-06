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
package org.apache.fineract.infrastructure.event.external.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.event.external.service.InternalExternalEventService;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/externalevents")
@RequiredArgsConstructor
@Slf4j
public class InternalExternalEventsApiResource implements InitializingBean {

    private final InternalExternalEventService internalExternalEventService;
    private final DefaultToApiJsonSerializer<List<ExternalEventDTO>> jsonSerializer;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() throws Exception {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal client services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAllExternalEvents(@QueryParam("idempotencyKey") final String idempotencyKey, @QueryParam("type") final String type,
            @QueryParam("category") final String category, @QueryParam("aggregateRootId") final Long aggregateRootId) {
        log.debug("getAllExternalEvents called with params idempotencyKey:{}, type:{}, category:{}, aggregateRootId:{}  ", idempotencyKey,
                type, category, aggregateRootId);
        List<ExternalEventDTO> allExternalEvents = internalExternalEventService.getAllExternalEvents(idempotencyKey, type, category,
                aggregateRootId);
        return jsonSerializer.serialize(allExternalEvents);
    }

    @DELETE
    public void deleteAllExternalEvents() {
        log.debug("deleteAllExternalEvents called");
        internalExternalEventService.deleteAllExternalEvents();
    }

}
