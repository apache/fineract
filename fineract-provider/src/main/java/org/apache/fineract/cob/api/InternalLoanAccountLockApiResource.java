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
package org.apache.fineract.cob.api;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/loans")
@RequiredArgsConstructor
@Slf4j
public class InternalLoanAccountLockApiResource implements InitializingBean {

    private final LoanAccountLockRepository loanAccountLockRepository;

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

    @POST
    @Path("{loanId}/place-lock/{lockOwner}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public Response placeLockOnLoanAccount(@Context final UriInfo uriInfo, @PathParam("loanId") Long loanId,
            @PathParam("lockOwner") String lockOwner, @RequestBody(required = false) String error) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Placing lock on loan: {}", loanId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        LoanAccountLock loanAccountLock = new LoanAccountLock(loanId, LockOwner.valueOf(lockOwner),
                ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE));

        if (StringUtils.isNotBlank(error)) {
            loanAccountLock.setError(error, error);
        }
        loanAccountLockRepository.save(loanAccountLock);
        return Response.status(Response.Status.ACCEPTED).build();
    }

}
