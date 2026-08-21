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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.internal.TestData;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/working-capital-loans")
@Tag(name = "Working Capital Loan Internal COB Api")
@RequiredArgsConstructor
@Slf4j
public class InternalWorkingCapitalLoanCOBApiResource implements InitializingBean {

    private final TestData testData;

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
    @Path("internal/lastCobRun")
    @Produces({ MediaType.APPLICATION_JSON })
    public Map<String, Object> getLastCobRun() {
        return testData.getData();
    }

    @DELETE
    @Path("internal/lastCobRun")
    @Produces({ MediaType.APPLICATION_JSON })
    public Map<String, Object> deleteLastCobRun() {
        testData.getData().put(TestData.COB_JOB_AFTER_LISTENER, null);
        testData.getData().put(TestData.COB_JOB_BEFORE_LISTENER, null);
        return testData.getData();
    }
}
