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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.repository.ProjectedAmortizationLoanModelRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Brings a Working Capital loan's stored amortization model up to the version of the calculation now in force.
 *
 * <p>
 * The model is persisted as JSON, and JSON written by an older version still parses: fields the current shape no longer
 * knows are dropped without complaint. So a stale model does not fail, it silently loses whatever those fields carried
 * - and the next write persists the loss. The loan's transactions, the principal allocated to each of them, and its
 * rate changes are all kept outside the model, so replaying those is what restores the schedule - and is why a version
 * bump may change the model's shape however it needs to.
 *
 * <p>
 * This is why the model carries its version at all. Mirrors {@code ProgressiveLoanModelProcessingService}, which does
 * the same for progressive term loans.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanModelProcessingService {

    /**
     * A rebuild replays the recorded transactions, so it is meaningful only for a loan that has some. Statuses before
     * disbursement have no schedule to rebuild, and a written-off loan's schedule is frozen at write-off.
     */
    private static final List<LoanStatus> REBUILDABLE_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET,
            LoanStatus.OVERPAID);

    private final ProjectedAmortizationLoanModelRepository modelRepository;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanAmortizationScheduleWriteService scheduleWriteService;

    /** Whether this loan's stored model was written by an older version and so has to be replayed before use. */
    @Transactional(readOnly = true)
    public boolean requiresModelRecalculation(final Long loanId) {
        return modelRepository.existsByLoanIdAndJsonModelVersionNot(loanId, ProjectedAmortizationScheduleModel.getModelVersion());
    }

    /** The same question asked of a batch, in one query rather than one per loan. */
    @Transactional(readOnly = true)
    public List<Long> findLoanIdsRequiringModelRecalculation(final List<Long> loanIds) {
        if (loanIds == null || loanIds.isEmpty()) {
            return List.of();
        }
        return modelRepository.findLoanIdsRequiringModelRecalculation(Set.copyOf(loanIds), REBUILDABLE_STATUSES,
                ProjectedAmortizationScheduleModel.getModelVersion());
    }

    /**
     * Replays the loan's recorded history onto a fresh model and saves it.
     *
     * <p>
     * In its own transaction, so a loan whose history cannot be replayed - a rate that no longer solves, say - is
     * skipped rather than failing the batch that swept it up. The stale model is left in place for that loan: worse
     * than a rebuilt one, but the rebuild is a repair, and a repair that cannot be made must not take the caller down
     * with it.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculateModelAndSave(final Long loanId) {
        loanRepository.findById(loanId).ifPresent(loan -> {
            try {
                scheduleWriteService.rebuildScheduleModelFromRecordedHistory(loan);
            } catch (final RuntimeException e) {
                log.error("Could not rebuild the amortization model of working capital loan {} at version {}", loanId,
                        ProjectedAmortizationScheduleModel.getModelVersion(), e);
            }
        });
    }
}
