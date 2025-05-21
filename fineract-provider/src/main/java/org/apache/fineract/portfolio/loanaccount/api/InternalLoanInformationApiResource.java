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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
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
    public AuditData getLoanAuditFields(@Context final UriInfo uriInfo, @PathParam("loanId") Long loanId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loan with {}", loanId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        return new AuditData(loan.getCreatedBy().orElse(null), loan.getCreatedDate().orElse(null), loan.getLastModifiedBy().orElse(null),
                loan.getLastModifiedDate().orElse(null));
    }

    @GET
    @Path("{loanId}/transaction/{transactionId}/audit")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public AuditData getLoanTransactionAuditFields(@Context final UriInfo uriInfo, @PathParam("loanId") Long loanId,
            @PathParam("transactionId") Long transactionId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loan transaction with loanId {}, transactionId {}", loanId, transactionId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        final LoanTransaction transaction = loanTransactionRepository.findById(transactionId).orElseThrow();
        return new AuditData(transaction.getCreatedBy().orElse(null), transaction.getCreatedDate().orElse(null),
                transaction.getLastModifiedBy().orElse(null), transaction.getLastModifiedDate().orElse(null));
    }

    @GET
    @Path("status/{statusId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public List<Long> getLoansByStatus(@Context final UriInfo uriInfo, @PathParam("statusId") Integer statusId) {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("Fetching loans by status {}", statusId);
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");

        return loanRepositoryWrapper.findLoanIdsByStatusId(statusId);
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

    private record AuditData(Long createdBy, OffsetDateTime createdDate, Long lastModifiedBy, OffsetDateTime lastModifiedDate) {
    }
}
