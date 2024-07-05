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
package org.apache.fineract.cob.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionAccrualActivityPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionAccrualActivityPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccrualActivityPostingBusinessStep implements LoanCOBBusinessStep {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ExternalIdFactory externalIdFactory;

    @Override
    public Loan execute(Loan loan) {
        log.debug("start processing loan accrual activity posting on installment due date with id [{}]", loan.getId());

        // check if loan capable for posting
        if (loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            final LocalDate currentDate = DateUtils.getBusinessLocalDate();
            // check if loan has installment due on business day
            Optional<LoanRepaymentScheduleInstallment> first = loan.getRepaymentScheduleInstallments().stream()
                    .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isEqual(currentDate))
                    .findFirst();
            if (first.isPresent()) {
                final LoanRepaymentScheduleInstallment installment = first.get();
                // check if there is no not-replayed-accrual-activity related to business date
                List<LoanTransaction> loanTransactions = loan.getLoanTransactions(loanTransaction -> loanTransaction.isNotReversed()
                        && loanTransaction.isAccrualActivity() && loanTransaction.getTransactionDate().isEqual(currentDate));
                if (loanTransactions.isEmpty()) {
                    businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionAccrualActivityPreBusinessEvent(loan));

                    MonetaryCurrency currency = loan.getCurrency();
                    ExternalId externalId = externalIdFactory.create();

                    BigDecimal interestPortion = installment.getInterestCharged(currency).getAmount();
                    BigDecimal feeChargesPortion = installment.getFeeChargesCharged(currency).getAmount();
                    BigDecimal penaltyChargesPortion = installment.getPenaltyChargesCharged(currency).getAmount();
                    BigDecimal transactionAmount = interestPortion.add(feeChargesPortion).add(penaltyChargesPortion);
                    LoanTransaction newAccrualActivityTransaction = new LoanTransaction(loan, loan.getOffice(),
                            LoanTransactionType.ACCRUAL_ACTIVITY.getValue(), currentDate, transactionAmount, null, interestPortion,
                            feeChargesPortion, penaltyChargesPortion, null, false, null, externalId);

                    loanAccountDomainService.saveLoanTransactionWithDataIntegrityViolationChecks(newAccrualActivityTransaction);

                    loan.addLoanTransaction(newAccrualActivityTransaction);

                    businessEventNotifierService
                            .notifyPostBusinessEvent(new LoanTransactionAccrualActivityPostBusinessEvent(newAccrualActivityTransaction));
                }
            }
        }

        // create transaction

        log.debug("end processing loan accrual activity posting on installment due date with id [{}]", loan.getId());
        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "ACCRUAL_ACTIVITY_POSTING";
    }

    @Override
    public String getHumanReadableName() {
        return "Accrual Activity Posting on Installment Due Date";
    }

}
