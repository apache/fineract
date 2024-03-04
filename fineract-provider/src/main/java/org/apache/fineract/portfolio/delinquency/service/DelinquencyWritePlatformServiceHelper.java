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
package org.apache.fineract.portfolio.delinquency.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTag;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class DelinquencyWritePlatformServiceHelper {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository;
    private final DelinquencyRangeRepository repositoryRange;
    private final LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository;

    public Map<String, Object> applyDelinquencyForLoan(final Loan loan, final DelinquencyBucket delinquencyBucket, long overdueDays) {
        Map<String, Object> changes = new HashMap<>();

        if (overdueDays <= 0) { // No Delinquency
            log.debug("Loan {} without delinquency range with {} days", loan.getId(), overdueDays);
            changes = setLoanDelinquencyTag(loan, null);

        } else {
            // Sort the ranges based on the minAgeDays
            final List<DelinquencyRange> ranges = sortDelinquencyRangesByMinAge(delinquencyBucket.getRanges());

            for (final DelinquencyRange delinquencyRange : ranges) {
                if (delinquencyRange.getMaximumAgeDays() == null) { // Last Range in the Bucket
                    if (delinquencyRange.getMinimumAgeDays() <= overdueDays) {
                        log.debug("Loan {} with delinquency range {} with {} days", loan.getId(), delinquencyRange.getClassification(),
                                overdueDays);
                        changes = setLoanDelinquencyTag(loan, delinquencyRange.getId());
                        break;
                    }
                } else {
                    if (delinquencyRange.getMinimumAgeDays() <= overdueDays && delinquencyRange.getMaximumAgeDays() >= overdueDays) {
                        log.debug("Loan {} with delinquency range {} with {} days", loan.getId(), delinquencyRange.getClassification(),
                                overdueDays);
                        changes = setLoanDelinquencyTag(loan, delinquencyRange.getId());
                        break;
                    }
                }
            }
        }
        changes.put("overdueDays", overdueDays);
        return changes;
    }

    public Map<String, Object> setLoanDelinquencyTag(Loan loan, Long delinquencyRangeId) {
        Map<String, Object> changes = new HashMap<>();
        List<LoanDelinquencyTagHistory> loanDelinquencyTagHistory = new ArrayList<>();
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        Optional<LoanDelinquencyTagHistory> optLoanDelinquencyTag = this.loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(loan, null);
        // The delinquencyRangeId in null means just goes out from Delinquency
        LoanDelinquencyTagHistory loanDelinquencyTagPrev = null;
        if (delinquencyRangeId == null) {
            // The Loan will go out from Delinquency
            if (optLoanDelinquencyTag.isPresent()) {
                loanDelinquencyTagPrev = optLoanDelinquencyTag.get();
                loanDelinquencyTagPrev.setLiftedOnDate(transactionDate);
                loanDelinquencyTagHistory.add(loanDelinquencyTagPrev);
                changes.put("previous", loanDelinquencyTagPrev.getDelinquencyRange());
                // event when loan goes out of delinquency we do not calculate at
                // installment level and remove all installment tags, so event needs to raised here.
                if (loan.isEnableInstallmentLevelDelinquency()) {
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
                }
            }
        } else {
            if (optLoanDelinquencyTag.isPresent()) {
                loanDelinquencyTagPrev = optLoanDelinquencyTag.get();
            }
            // If the Delinquency Tag has not changed
            if (loanDelinquencyTagPrev != null && loanDelinquencyTagPrev.getDelinquencyRange().getId().equals(delinquencyRangeId)) {
                changes.put("current", loanDelinquencyTagPrev.getDelinquencyRange());
            } else {
                // The previous Loan Delinquency Tag will set as Lifted
                if (loanDelinquencyTagPrev != null) {
                    loanDelinquencyTagPrev.setLiftedOnDate(transactionDate);
                    loanDelinquencyTagHistory.add(loanDelinquencyTagPrev);
                    changes.put("previous", loanDelinquencyTagPrev.getDelinquencyRange());
                }

                final DelinquencyRange delinquencyRange = repositoryRange.getReferenceById(delinquencyRangeId);
                LoanDelinquencyTagHistory loanDelinquencyTag = new LoanDelinquencyTagHistory(delinquencyRange, loan, transactionDate, null);
                loanDelinquencyTagHistory.add(loanDelinquencyTag);
                changes.put("current", loanDelinquencyTag.getDelinquencyRange());
            }
        }
        if (loanDelinquencyTagHistory.size() > 0) {
            this.loanDelinquencyTagRepository.saveAllAndFlush(loanDelinquencyTagHistory);
            // if installment level delinquency is enabled event will be raised at installment level calculation, no
            // need to raise the event here
            if (!loan.isEnableInstallmentLevelDelinquency()) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
            }
        }
        return changes;
    }

    public List<DelinquencyRange> sortDelinquencyRangesByMinAge(List<DelinquencyRange> ranges) {
        final Comparator<DelinquencyRange> orderByMinAge = new Comparator<DelinquencyRange>() {

            @Override
            public int compare(DelinquencyRange o1, DelinquencyRange o2) {
                return o1.getMinimumAgeDays().compareTo(o2.getMinimumAgeDays());
            }
        };
        Collections.sort(ranges, orderByMinAge);
        return ranges;
    }

    public void applyDelinquencyForLoanInstallments(final Loan loan, final DelinquencyBucket delinquencyBucket,
            final Map<Long, CollectionData> installmentsCollectionData) {
        boolean isDelinquencyRangeChangedForAnyOfInstallment = false;
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (installmentsCollectionData.containsKey(installment.getId())) {
                boolean isDelinquencySetForInstallment = setInstallmentDelinquencyDetails(loan, installment, delinquencyBucket,
                        installmentsCollectionData.get(installment.getId()));
                isDelinquencyRangeChangedForAnyOfInstallment = isDelinquencyRangeChangedForAnyOfInstallment
                        || isDelinquencySetForInstallment;
            }
        }
        // remove tags for non-existing installments that got deleted due to re-schedule
        removeDelinquencyTagsForNonExistingInstallments(loan.getId());
        // raise event if there is any change at installment level delinquency
        if (isDelinquencyRangeChangedForAnyOfInstallment) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
        }

    }

    private void removeDelinquencyTagsForNonExistingInstallments(Long loanId) {
        List<LoanInstallmentDelinquencyTag> currentLoanInstallmentDelinquencyTags = loanInstallmentDelinquencyTagRepository
                .findByLoanId(loanId);
        if (currentLoanInstallmentDelinquencyTags != null && currentLoanInstallmentDelinquencyTags.size() > 0) {
            List<Long> loanInstallmentTagsForDelete = currentLoanInstallmentDelinquencyTags.stream()
                    .filter(tag -> tag.getInstallment() == null).map(tag -> tag.getId()).toList();
            if (loanInstallmentTagsForDelete.size() > 0) {
                loanInstallmentDelinquencyTagRepository.deleteAllLoanInstallmentsTagsByIds(loanInstallmentTagsForDelete);
            }
        }
    }

    private boolean setInstallmentDelinquencyDetails(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            final DelinquencyBucket delinquencyBucket, final CollectionData installmentDelinquencyData) {
        DelinquencyRange delinquencyRangeForInstallment = getInstallmentDelinquencyRange(delinquencyBucket,
                installmentDelinquencyData.getDelinquentDays());
        return setDelinquencyDetailsForInstallment(loan, installment, installmentDelinquencyData, delinquencyRangeForInstallment);
    }

    private DelinquencyRange getInstallmentDelinquencyRange(final DelinquencyBucket delinquencyBucket, Long overDueDays) {
        DelinquencyRange delinquencyRangeForInstallment = null;
        if (overDueDays > 0) {
            // Sort the ranges based on the minAgeDays
            final List<DelinquencyRange> ranges = sortDelinquencyRangesByMinAge(delinquencyBucket.getRanges());
            for (final DelinquencyRange delinquencyRange : ranges) {
                if (delinquencyRange.getMaximumAgeDays() == null) { // Last Range in the Bucket
                    if (delinquencyRange.getMinimumAgeDays() <= overDueDays) {
                        delinquencyRangeForInstallment = delinquencyRange;
                        break;
                    }
                } else {
                    if (delinquencyRange.getMinimumAgeDays() <= overDueDays && delinquencyRange.getMaximumAgeDays() >= overDueDays) {
                        delinquencyRangeForInstallment = delinquencyRange;
                        break;
                    }
                }
            }

        }
        return delinquencyRangeForInstallment;
    }

    private boolean setDelinquencyDetailsForInstallment(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            CollectionData installmentDelinquencyData, final DelinquencyRange delinquencyRangeForInstallment) {
        List<LoanInstallmentDelinquencyTag> installmentDelinquencyTags = new ArrayList<>();
        LocalDate delinquencyCalculationDate = DateUtils.getBusinessLocalDate();
        boolean isDelinquencyRangeChanged = false;

        LoanInstallmentDelinquencyTag previousInstallmentDelinquencyTag = loanInstallmentDelinquencyTagRepository
                .findByLoanAndInstallment(loan, installment).orElse(null);

        if (delinquencyRangeForInstallment == null) {
            // if currentInstallmentDelinquencyTag exists and range is null, installment is out of delinquency, delete
            // delinquency details
            if (previousInstallmentDelinquencyTag != null) {
                // event installment out of delinquency
                loanInstallmentDelinquencyTagRepository.delete(previousInstallmentDelinquencyTag);
                isDelinquencyRangeChanged = true;
            }
        } else {
            LoanInstallmentDelinquencyTag installmentDelinquency = null;
            if (previousInstallmentDelinquencyTag != null) {
                if (!previousInstallmentDelinquencyTag.getDelinquencyRange().getId().equals(delinquencyRangeForInstallment.getId())) {
                    // if current delinquency range exists and there is range change, delete previous delinquency
                    // details and add new range details
                    installmentDelinquency = new LoanInstallmentDelinquencyTag(delinquencyRangeForInstallment, loan, installment,
                            delinquencyCalculationDate, null, previousInstallmentDelinquencyTag.getFirstOverdueDate(),
                            installmentDelinquencyData.getDelinquentAmount());
                    loanInstallmentDelinquencyTagRepository.delete(previousInstallmentDelinquencyTag);
                    // event installment delinquency range change
                    isDelinquencyRangeChanged = true;
                } else {
                    previousInstallmentDelinquencyTag.setOutstandingAmount(installmentDelinquencyData.getDelinquentAmount());
                    installmentDelinquency = previousInstallmentDelinquencyTag;
                }
            } else {
                // add new range, first time delinquent
                installmentDelinquency = new LoanInstallmentDelinquencyTag(delinquencyRangeForInstallment, loan, installment,
                        delinquencyCalculationDate, null, installmentDelinquencyData.getDelinquentDate(),
                        installmentDelinquencyData.getDelinquentAmount());
                // event installment delinquent
                isDelinquencyRangeChanged = true;
            }

            if (installmentDelinquency != null) {
                installmentDelinquencyTags.add(installmentDelinquency);
            }

        }

        if (installmentDelinquencyTags.size() > 0) {
            loanInstallmentDelinquencyTagRepository.saveAllAndFlush(installmentDelinquencyTags);
        }
        return isDelinquencyRangeChanged;
    }
}
