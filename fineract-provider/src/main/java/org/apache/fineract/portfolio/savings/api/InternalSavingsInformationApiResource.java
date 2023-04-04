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
package org.apache.fineract.portfolio.savings.api;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
@Path("/internal/savings")
@RequiredArgsConstructor
@Slf4j
public class InternalSavingsInformationApiResource implements InitializingBean {

    private final SavingsAccountRepositoryWrapper savingsRepository;
    private final ToApiJsonSerializer<List> toApiJsonSerializerForList;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Override
    public void afterPropertiesSet() {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal savings services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

    }

    @GET
    @Path("status/{statusId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getSavingsByStatus(@Context final UriInfo uriInfo, @PathParam("statusId") Integer statusId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching savings by status {}", statusId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final List<Long> loanIds = savingsRepository.findSavingAccountIdByStatus(statusId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerForList.serialize(settings, loanIds);
    }

    @POST
    @Path("{savingsId}/status/{statusId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response changeLoanStatus(@Context final UriInfo uriInfo, @PathParam("savingsId") Long savingsId,
            @PathParam("statusId") Integer statusId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Set Savings status with savingId {}, statusId {}", savingsId, statusId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final SavingsAccount savingsAccount = savingsRepository.findOneWithNotFoundDetection(savingsId);
        savingsAccount.setStatus(statusId);
        savingsRepository.save(savingsAccount);

        return Response.status(Response.Status.OK).build();
    }
}
