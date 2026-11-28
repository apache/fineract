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
package org.apache.fineract.portfolio.loanaccount.service.recalculation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rebuilds a progressive loan from its terms and its transactions, for loans whose repayment schedule, transaction
 * allocations or stored interest schedule model went wrong because of a defect that has since been fixed.
 *
 * <p>
 * Unlike the model rebuild that runs before loan requests and in close of business, which replays the transactions over
 * the installments as they are stored, this regenerates the repayment schedule from the loan terms first. Installments
 * that an earlier, defective run left changed (for example by a re-age) therefore do not leak into the result. The
 * transactions and term variations are then replayed over the fresh schedule, which writes a new interest schedule
 * model, and the accruals, loan status and delinquency are brought in line with the new state.
 *
 * <p>
 * The request bypasses the model check and the inline close of business that other loan requests go through, because
 * both would rebuild or use the defective state this operation is meant to replace. It still refuses a loan that close
 * of business holds a lock on, unless that lock is one a failed close of business left behind. Everything happens in
 * the command's transaction, so a failure leaves the loan as it was.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanRecalculationService {

    private static final List<LoanStatus> ALLOWED_LOAN_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.OVERPAID,
            LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.CLOSED_WRITTEN_OFF);

    private final LoanAssembler loanAssembler;
    private final LoanAccountLockService loanAccountLockService;
    private final LoanScheduleService loanScheduleService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Transactional
    public CommandProcessingResult recalculate(final JsonCommand command) {
        final Long loanId = command.getLoanId();
        rejectIfLockedByCob(loanId);

        final Loan loan = loanAssembler.assembleFromInUserScope(loanId);
        validate(loan);

        final LoanStatus statusBefore = loan.getStatus();
        log.info("Recalculating loan {} from its terms and transactions", loanId);

        loanScheduleService.regenerateScheduleWithReprocessingTransactions(loan);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        loanRepaymentScheduleInstallmentRepository.saveAll(loan.getRepaymentScheduleInstallments());
        final Loan savedLoan = loanRepositoryWrapper.saveAndFlush(loan);

        loanLifecycleStateMachine.determineAndTransition(savedLoan, savedLoan.getLastUserTransactionDate());
        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(savedLoan,
                savedLoan.isInterestBearingAndInterestRecalculationEnabled(), true);
        loanAccountDomainService.setLoanDelinquencyTag(savedLoan, DateUtils.getBusinessLocalDate());

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(savedLoan));

        final Map<String, Object> changes = new LinkedHashMap<>();
        if (statusBefore != savedLoan.getStatus()) {
            changes.put("status", savedLoan.getStatus().getCode());
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savedLoan.getId()) //
                .withEntityExternalId(savedLoan.getExternalId()) //
                .withOfficeId(savedLoan.getOfficeId()) //
                .withClientId(savedLoan.getClientId()) //
                .withGroupId(savedLoan.getGroupId()) //
                .withLoanId(savedLoan.getId()) //
                .with(changes) //
                .build();
    }

    /**
     * The same rule the close of business API filter applies to the other loan requests: a lock that close of business
     * holds blocks the request, but one that a failed close of business left behind, with its error recorded, does not.
     * A loan that this operation is meant to repair is likely to carry such a lock.
     */
    private void rejectIfLockedByCob(final Long loanId) {
        final List<Long> loanIds = List.of(loanId);
        if (loanAccountLockService.isAnyLoanHardLocked(loanIds) && !loanAccountLockService.isAnyLockOverrulable(loanIds)) {
            throw new LoanIdsHardLockedException(loanId);
        }
    }

    private void validate(final Loan loan) {
        if (!loan.isProgressiveSchedule()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.recalculation.not.progressive",
                    "Loan recalculation is only supported for loans with progressive repayment schedule", loan.getId());
        }
        if (!ALLOWED_LOAN_STATUSES.contains(loan.getStatus())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.recalculation.invalid.status",
                    "Loan recalculation is not allowed in loan status " + loan.getStatus().getCode(), loan.getId(),
                    loan.getStatus().getCode());
        }
    }
}
