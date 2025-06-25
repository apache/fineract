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
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.infrastructure.event.external.service.InternalExternalEventService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
// TODO: can't we test this differently without creating boilerplate code that's only available during testing?
@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/externalevents")
@RequiredArgsConstructor
public class InternalExternalEventsApiResource {

    private final InternalExternalEventService internalExternalEventService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<ExternalEventResponse> getAllExternalEvents(@QueryParam("idempotencyKey") final String idempotencyKey,
            @QueryParam("type") final String type, @QueryParam("category") final String category,
            @QueryParam("aggregateRootId") final Long aggregateRootId) {
        // TODO: authorization constraints?
        return internalExternalEventService.getAllExternalEvents(idempotencyKey, type, category, aggregateRootId);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public void deleteAllExternalEvents() {
        // TODO: authorization constraints?
        internalExternalEventService.deleteAllExternalEvents();
    }
}
