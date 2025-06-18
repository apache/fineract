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
package org.apache.fineract.portfolio.loanaccount.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Profile(FineractProfiles.TEST)
@Component
@Path("v1/internal/loan/progressive")
@Tag(name = "Progressive Loan", description = "internal loan testing API. This API should be disabled in production!!!")
public class InternalProgressiveLoanApiResource implements InitializingBean {

    private final LoanRepositoryWrapper loanRepository;
    private final AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor;
    private final InterestScheduleModelRepositoryWrapper writePlatformService;
    private final LoanTransactionRepository loanTransactionRepository;

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
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("{loanId}/model")
    @Operation(summary = "Fetch ProgressiveLoanInterestScheduleModel", description = "DO NOT USE THIS IN PRODUCTION!")
    public ProgressiveLoanInterestScheduleModel fetchModel(@PathParam("loanId") @Parameter(description = "loanId") long loanId) {
        Loan loan = loanRepository.findOneWithNotFoundDetection(loanId);
        if (!loan.isProgressiveSchedule()) {
            throw new IllegalArgumentException("The loan is not progressive.");
        }

        return writePlatformService.readProgressiveLoanInterestScheduleModel(loanId, loan.getLoanRepaymentScheduleDetail(),
                loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf()).orElse(null);
    }

    private ProgressiveLoanInterestScheduleModel reprocessTransactionsAndGetModel(final Loan loan) {
        final List<LoanTransaction> transactionsToReprocess = loanTransactionRepository
                .findNonReversedTransactionsForReprocessingByLoan(loan);
        final LocalDate businessDate = ThreadLocalContextUtil.getBusinessDate();
        final Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> changedTransactionDetailProgressiveLoanInterestScheduleModelPair = advancedPaymentScheduleTransactionProcessor
                .reprocessProgressiveLoanTransactionsTransactional(loan.getDisbursementDate(), businessDate, transactionsToReprocess,
                        loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
        final ProgressiveLoanInterestScheduleModel model = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getRight();
        final List<Long> replayedTransactions = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getLeft()
                .getTransactionChanges().stream().filter(change -> change.getOldTransaction() != null)
                .map(change -> change.getNewTransaction().getId()).filter(Objects::nonNull).toList();

        if (!replayedTransactions.isEmpty()) {
            log.warn("Reprocessed transactions show differences: There are unsaved changes of the following transactions: {}",
                    replayedTransactions);
        }
        return model;
    }

    @POST
    @Path("{loanId}/model")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update and Save ProgressiveLoanInterestScheduleModel", description = "DO NOT USE THIS IN PRODUCTION!")
    public ProgressiveLoanInterestScheduleModel updateModel(@PathParam("loanId") @Parameter(description = "loanId") long loanId) {
        Loan loan = loanRepository.findOneWithNotFoundDetection(loanId);
        if (!loan.isProgressiveSchedule()) {
            throw new IllegalArgumentException("The loan is not progressive.");
        }
        ProgressiveLoanInterestScheduleModel model = reprocessTransactionsAndGetModel(loan);

        return writePlatformService.writeInterestScheduleModel(loan, model);
    }
}
