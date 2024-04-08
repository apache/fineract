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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.LoanCOBPartition;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.cob.loan.RetrieveLoanIdService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/cob")
@RequiredArgsConstructor
@Slf4j
public class InternalCOBApiResource implements InitializingBean {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";

    private final RetrieveLoanIdService retrieveLoanIdService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<List> toApiJsonSerializerForList;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    protected DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

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
    @Path("partitions/{partitionSize}")
    public String getCobPartitions(@Context final UriInfo uriInfo, @PathParam("partitionSize") int partitionSize) {
        LocalDate businessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.BUSINESS_DATE);
        log.info("RetrieveLoanCOBPartitions is called with partitionSize {} for {}", partitionSize, businessDate);
        List<LoanCOBPartition> loanCOBPartitions = retrieveLoanIdService.retrieveLoanCOBPartitions(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND,
                businessDate, false, partitionSize);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializerForList.serialize(settings, loanCOBPartitions);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Path("fast-forward-cob-date-of-loan/{loanId}")
    public void updateLoanCobLastDate(@Context final UriInfo uriInfo, @PathParam("loanId") long loanId, String jsonBody) {
        JsonElement root = JsonParser.parseString(jsonBody);
        String lastClosedBusinessDate = root.getAsJsonObject().get("lastClosedBusinessDate").getAsString();
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        LocalDate localDate = LocalDate.parse(lastClosedBusinessDate, dateTimeFormatter);
        loan.setLastClosedBusinessDate(localDate);
        loanRepositoryWrapper.save(loan);
    }

}
