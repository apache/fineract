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
package org.apache.fineract.portfolio.loanaccount.api;

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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanproduct.data.AdvancedPaymentData;
import org.apache.fineract.portfolio.loanproduct.mapper.AdvancedPaymentDataMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@Path("/v1/internal/loan")
@RequiredArgsConstructor
@Slf4j
public class InternalLoanInformationApiResource implements InitializingBean {

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final ToApiJsonSerializer<Map> toApiJsonSerializerForMap;
    private final ToApiJsonSerializer<List> toApiJsonSerializerForList;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AdvancedPaymentDataMapper advancedPaymentDataMapper;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal loan services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

    }

    @GET
    @Path("{loanId}/audit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public String getLoanAuditFields(@Context final UriInfo uriInfo, @PathParam("loanId") Long loanId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loan with {}", loanId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        Map<String, Object> auditFields = new HashMap<>(
                Map.of(CREATED_BY, loan.getCreatedBy().orElse(null), CREATED_DATE, loan.getCreatedDateTime(), LAST_MODIFIED_BY,
                        loan.getLastModifiedBy().orElse(null), LAST_MODIFIED_DATE, loan.getLastModifiedDateTime()));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerForMap.serialize(settings, auditFields);
    }

    @GET
    @Path("{loanId}/transaction/{transactionId}/audit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public String getLoanTransactionAuditFields(@Context final UriInfo uriInfo, @PathParam("loanId") Long loanId,
            @PathParam("transactionId") Long transactionId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loan transaction with loanId {}, transactionId {}", loanId, transactionId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final LoanTransaction transaction = loanTransactionRepository.findById(transactionId).orElseThrow();
        Map<String, Object> auditFields = new HashMap<>(Map.of(CREATED_BY, transaction.getCreatedBy().orElse(null), CREATED_DATE,
                transaction.getCreatedDateTime(), LAST_MODIFIED_BY, transaction.getLastModifiedBy().orElse(null), LAST_MODIFIED_DATE,
                transaction.getLastModifiedDateTime()));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerForMap.serialize(settings, auditFields);
    }

    @GET
    @Path("status/{statusId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public String getLoansByStatus(@Context final UriInfo uriInfo, @PathParam("statusId") Integer statusId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loans by status {}", statusId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final List<Long> loanIds = loanRepositoryWrapper.findLoanIdsByStatusId(statusId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerForList.serialize(settings, loanIds);
    }

    @GET
    @Path("{loanId}/advanced-payment-allocation-rules")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public List<AdvancedPaymentData> getAdvancedPaymentAllocationRulesOfLoan(@Context final UriInfo uriInfo,
            @PathParam("loanId") Long loanId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching advanced payment allocation rules by loanId {}", loanId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        return advancedPaymentDataMapper.mapLoanPaymentAllocationRule(loan.getPaymentAllocationRules());
    }
}
