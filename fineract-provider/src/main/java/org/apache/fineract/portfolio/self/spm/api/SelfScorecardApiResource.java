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

package org.apache.fineract.portfolio.self.spm.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.spm.api.ScorecardApiResource;
import org.apache.fineract.spm.data.ScorecardData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/v1/self/surveys/scorecards")
@Component
@Tag(name = "Self Score Card", description = "")
@RequiredArgsConstructor
public class SelfScorecardApiResource {

    private final PlatformSecurityContext context;
    private final ScorecardApiResource scorecardApiResource;
    private final AppuserClientMapperReadService appuserClientMapperReadService;

    @GET
    @Path("clients/{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findByClient(@PathParam("clientId") final Long clientId) {

        validateAppuserClientsMapping(clientId);
        return this.scorecardApiResource.findByClient(clientId);
    }

    @POST
    @Path("{surveyId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public void createScorecard(@PathParam("surveyId") final Long surveyId, final ScorecardData scorecardData) {
        if (scorecardData.getClientId() != null) {
            validateAppuserClientsMapping(scorecardData.getClientId());
            this.scorecardApiResource.createScorecard(surveyId, scorecardData);
        }

    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appuserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

}
