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
package org.apache.fineract.portfolio.client.api;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/client")
@RequiredArgsConstructor
@Slf4j
public class InternalClientInformationApiResource implements InitializingBean {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ToApiJsonSerializer<Map> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal client services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

    }

    @GET
    @Path("{clientId}/audit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public String getClientAuditFields(@Context final UriInfo uriInfo, @PathParam("clientId") Long clientId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching client with {}", clientId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        Map<String, Object> auditFields = new HashMap<>(
                Map.of(CREATED_BY, client.getCreatedBy().orElse(null), CREATED_DATE, client.getCreatedDateTime(), LAST_MODIFIED_BY,
                        client.getLastModifiedBy().orElse(null), LAST_MODIFIED_DATE, client.getLastModifiedDateTime()));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, auditFields);
    }
}
