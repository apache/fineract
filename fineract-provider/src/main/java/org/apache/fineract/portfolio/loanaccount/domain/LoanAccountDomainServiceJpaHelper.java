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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionProcessingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Slf4j
public class LoanAccountDomainServiceJpaHelper {

    private final LoanAssembler loanAssembler;
    private final LoanTransactionProcessingService loanTransactionProcessingService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public LocalDate calculateRecalculateTillDate(Loan loan, LocalDate transactionDate, ScheduleGeneratorDTO scheduleGeneratorDTOForPrepay,
            Money repaymentAmount) {
        LocalDate recalculateTill = null;
        try {
            if (FineractRequestContextHolder.isBatchRequest() && BatchRequestContextHolder.isEnclosingTransaction()) {
                // In case of Batch requests with enclosing transaction, the current way of calculating the prepayment
                // amount (since it changes
                // the state of entities which would be written back to the DB) is incorrect, so we won't allow it for
                // now.
                // With enclosing transactions where the loan is created and repaid within the same batch request, due
                // to REQUIRES_NEW, this method
                // will simply not see that a loan has been created.
                // Temporarily if you wanna use the batch API for prepayment, make sure to split the requests in a way
                // that loan creation and
                // repayment doesn't occur in the same batch request.
                // Example testcase:
                // org.apache.fineract.integrationtests.BatchApiTest.shouldReturnOkStatusOnSuccessfulGetDatatableEntryWithNoQueryParam
                // TODO: this can be removed if the prepayment amount calculation below this is fixed in a way that it
                // doesn't change any entity
                // but works with DTOs
                return null;
            }
            loan = loanAssembler.assembleFrom(loan.getId());
            if (loan.isInterestBearingAndInterestRecalculationEnabled() && loan.getLoanProduct().getProductInterestRecalculationDetails()
                    .getPreCloseInterestCalculationStrategy().calculateTillPreClosureDateEnabled()) {
                Money outstanding = loanTransactionProcessingService
                        .fetchPrepaymentDetail(scheduleGeneratorDTOForPrepay, transactionDate, loan).getTotalOutstanding();
                if (repaymentAmount.isGreaterThanOrEqualTo(outstanding)) {
                    recalculateTill = transactionDate;
                }
            }
        } catch (Exception e) {
            // TODO: there's a bug where the prepayment calculation fails
            // the test-case is org.apache.fineract.integrationtests.LoanTransactionAccrualActivityPostingTest.test in
            // the integration-tests
            // seems like it occurs only on CUMULATIVE loans, not PROGRESSIVE
            log.warn("Unable to calculate prepayment amount", e);
        }
        return recalculateTill;
    }
}
