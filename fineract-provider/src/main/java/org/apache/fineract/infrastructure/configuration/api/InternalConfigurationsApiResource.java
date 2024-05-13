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
package org.apache.fineract.infrastructure.configuration.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/configurations")
@RequiredArgsConstructor
@Slf4j
public class InternalConfigurationsApiResource implements InitializingBean {

    private final GlobalConfigurationRepositoryWrapper repository;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() throws Exception {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal Config services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

    }

    @POST
    @Path("{configId}/value/{configValue}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public Response updateGlobalConfiguration(@Context final UriInfo uriInfo, @PathParam("configId") Long configId,
            @PathParam("configValue") Long configValue, @RequestBody(required = false) String error) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Update trap-door config: {}", configId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final GlobalConfigurationProperty config = repository.findOneWithNotFoundDetection(configId);
        log.warn("Config to be updated {} original value {}", config.getName(), config.getValue());
        config.setValue(configValue);
        repository.save(config);
        log.warn("Config to be updated to {}", config.getValue());
        repository.removeFromCache(config.getName());
        MoneyHelper.fetchRoundingModeFromGlobalConfig();

        return Response.status(Response.Status.OK).build();
    }

}
