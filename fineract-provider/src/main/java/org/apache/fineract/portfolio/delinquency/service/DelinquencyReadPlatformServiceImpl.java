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

import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelinquencyReadPlatformServiceImpl implements DelinquencyReadPlatformService {

    private final DelinquencyRangeRepository repositoryRange;
    private final DelinquencyBucketRepository repositoryBucket;
    private final LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    private final DelinquencyRangeMapper mapperRange;
    private final DelinquencyBucketMapper mapperBucket;
    private final LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;
    private final LoanRepository loanRepository;
    private final LoanDelinquencyDomainService loanDelinquencyDomainService;
    private final LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag;
    private final LoanDelinquencyActionRepository loanDelinquencyActionRepository;

    @Override
    public Collection<DelinquencyRangeData> retrieveAllDelinquencyRanges() {
        final List<DelinquencyRange> delinquencyRangeList = repositoryRange.findAll();
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public DelinquencyRangeData retrieveDelinquencyRange(Long delinquencyRangeId) {
        DelinquencyRange delinquencyRangeList = repositoryRange.getReferenceById(delinquencyRangeId);
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public Collection<DelinquencyBucketData> retrieveAllDelinquencyBuckets() {
        final List<DelinquencyBucket> delinquencyRangeList = repositoryBucket.findAll();
        return mapperBucket.map(delinquencyRangeList);
    }

    @Override
    public DelinquencyBucketData retrieveDelinquencyBucket(Long delinquencyBucketId) {
        final DelinquencyBucket delinquencyBucket = repositoryBucket.getReferenceById(delinquencyBucketId);
        final DelinquencyBucketData delinquencyBucketData = mapperBucket.map(delinquencyBucket);
        delinquencyBucketData.setRanges(mapperRange.map(delinquencyBucket.getRanges()));
        return delinquencyBucketData;
    }

    @Override
    public DelinquencyRangeData retrieveCurrentDelinquencyTag(Long loanId) {
        final Loan loan = this.loanRepository.getReferenceById(loanId);
        Optional<LoanDelinquencyTagHistory> optLoanDelinquencyTag = this.repositoryLoanDelinquencyTagHistory.findByLoanAndLiftedOnDate(loan,
                null);
        if (optLoanDelinquencyTag.isPresent()) {
            return mapperRange.map(optLoanDelinquencyTag.get().getDelinquencyRange());
        }
        return null;
    }

    @Override
    public Collection<LoanDelinquencyTagHistoryData> retrieveDelinquencyRangeHistory(Long loanId) {
        final Loan loan = this.loanRepository.getReferenceById(loanId);
        final List<LoanDelinquencyTagHistory> loanDelinquencyTagData = this.repositoryLoanDelinquencyTagHistory
                .findByLoanOrderByAddedOnDateDesc(loan);
        return mapperLoanDelinquencyTagHistory.map(loanDelinquencyTagData);
    }

    @Override
    public CollectionData calculateLoanCollectionData(final Long loanId) {
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);

        CollectionData collectionData = CollectionData.template();
        if (optLoan.isPresent()) {
            final Loan loan = optLoan.get();

            collectionData = loanDelinquencyDomainService.getOverdueCollectionData(loan);
            collectionData.setAvailableDisbursementAmount(loan.getApprovedPrincipal().subtract(loan.getDisbursedAmount()));
            collectionData.setNextPaymentDueDate(loan.possibleNextRepaymentDate());

            final LoanTransaction lastPayment = loan.getLastPaymentTransaction();
            if (lastPayment != null) {
                collectionData.setLastPaymentDate(lastPayment.getTransactionDate());
                collectionData.setLastPaymentAmount(lastPayment.getAmount());
            }

            final LoanTransaction lastRepaymentTransaction = loan.getLastRepaymentTransaction();
            if (lastRepaymentTransaction != null) {
                collectionData.setLastRepaymentDate(lastRepaymentTransaction.getTransactionDate());
                collectionData.setLastRepaymentAmount(lastRepaymentTransaction.getAmount());
            }

            enrichWithDelinquencyPausePeriodInfo(collectionData, retrieveLoanDelinquencyActions(loanId),
                    ThreadLocalContextUtil.getBusinessDate());
        }

        return collectionData;
    }

    void enrichWithDelinquencyPausePeriodInfo(CollectionData collectionData, Collection<LoanDelinquencyAction> delinquencyActions,
            LocalDate businessDate) {
        // partition them based on type
        Map<DelinquencyAction, List<LoanDelinquencyAction>> partitioned = delinquencyActions.stream()
                .collect(Collectors.groupingBy(LoanDelinquencyAction::getAction));

        // add the possible resumes to it to create the effective pause periods
        if (partitioned.containsKey(DelinquencyAction.PAUSE)) {
            List<LoanDelinquencyActionData> effective = new ArrayList<>();
            List<LoanDelinquencyAction> pauses = partitioned.get(DelinquencyAction.PAUSE);
            for (LoanDelinquencyAction loanDelinquencyAction : pauses) {
                Optional<LoanDelinquencyAction> resume = findMatchingResume(loanDelinquencyAction, partitioned.get(RESUME));
                LoanDelinquencyActionData loanDelinquencyActionData = new LoanDelinquencyActionData(loanDelinquencyAction);
                resume.ifPresent(r -> loanDelinquencyActionData.setEndDate(r.getStartDate()));
                effective.add(loanDelinquencyActionData);
            }

            // order them by start date, filter out the future items
            Optional<LoanDelinquencyActionData> last = effective.stream()
                    .sorted(Comparator.comparing(LoanDelinquencyActionData::getStartDate))
                    .filter(e -> !DateUtils.isAfter(e.getStartDate(), businessDate)) // keep only the present and the
                                                                                     // past ones
                    .reduce((first, second) -> second);

            // enrich collectionData
            last.ifPresent(action -> {
                collectionData
                        .setDelinquencyCalculationPaused(!action.startDate.isAfter(businessDate) && !businessDate.isAfter(action.endDate));
                collectionData.setDelinquencyPausePeriodStartDate(action.getStartDate());
                collectionData.setDelinquencyPausePeriodEndDate(action.getEndDate());
            });
        }
    }

    private Optional<LoanDelinquencyAction> findMatchingResume(LoanDelinquencyAction pause, List<LoanDelinquencyAction> resumes) {
        if (resumes != null && resumes.size() > 0) {
            for (LoanDelinquencyAction resume : resumes) {
                if (!pause.getStartDate().isAfter(resume.getStartDate()) && !resume.getStartDate().isAfter(pause.getEndDate())) {
                    return Optional.of(resume);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<LoanInstallmentDelinquencyTagData> retrieveLoanInstallmentsCurrentDelinquencyTag(Long loanId) {
        return repositoryLoanInstallmentDelinquencyTag.findInstallmentDelinquencyTags(loanId);
    }

    @Override
    public Collection<LoanDelinquencyAction> retrieveLoanDelinquencyActions(Long loanId) {
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);
        if (optLoan.isPresent()) {
            return loanDelinquencyActionRepository.findByLoanOrderById(optLoan.get());
        }
        return List.of();
    }

}
