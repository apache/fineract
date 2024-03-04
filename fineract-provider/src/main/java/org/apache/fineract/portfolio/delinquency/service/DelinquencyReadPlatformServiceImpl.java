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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.DelinquencyPausePeriod;
import org.apache.fineract.portfolio.loanaccount.data.InstallmentLevelDelinquency;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.jetbrains.annotations.NotNull;
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
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final ConfigurationDomainService configurationDomainService;

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

            // If the Loan is not Active yet, return template data
            if (loan.isSubmittedAndPendingApproval() || loan.isApproved()) {
                return CollectionData.template();
            }

            final List<LoanDelinquencyAction> savedDelinquencyList = retrieveLoanDelinquencyActions(loanId);
            List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                    .calculateEffectiveDelinquencyList(savedDelinquencyList);

            final String nextPaymentDueDateConfig = configurationDomainService.getNextPaymentDateConfigForLoan();

            collectionData = loanDelinquencyDomainService.getOverdueCollectionData(loan, effectiveDelinquencyList);
            collectionData.setAvailableDisbursementAmount(loan.getApprovedPrincipal().subtract(loan.getDisbursedAmount()));
            collectionData.setNextPaymentDueDate(loan.possibleNextRepaymentDate(nextPaymentDueDateConfig));

            final LoanTransaction lastPayment = loan.getLastPaymentTransaction();
            if (lastPayment != null) {
                collectionData.setLastPaymentDate(lastPayment.getTransactionDate());
                collectionData.setLastPaymentAmount(lastPayment.getAmount());
            }

            final LoanTransaction lastRepaymentTransaction = loan.getLastRepaymentOrDownPaymentTransaction();
            if (lastRepaymentTransaction != null) {
                collectionData.setLastRepaymentDate(lastRepaymentTransaction.getTransactionDate());
                collectionData.setLastRepaymentAmount(lastRepaymentTransaction.getAmount());
            }

            enrichWithDelinquencyPausePeriodInfo(collectionData, effectiveDelinquencyList, ThreadLocalContextUtil.getBusinessDate());

            if (optLoan.get().isEnableInstallmentLevelDelinquency()) {
                addInstallmentLevelDelinquencyData(collectionData, loanId);
            }
        }

        return collectionData;
    }

    private void addInstallmentLevelDelinquencyData(CollectionData collectionData, Long loanId) {
        Collection<LoanInstallmentDelinquencyTagData> loanInstallmentDelinquencyTagData = retrieveLoanInstallmentsCurrentDelinquencyTag(
                loanId);
        if (loanInstallmentDelinquencyTagData != null && loanInstallmentDelinquencyTagData.size() > 0) {

            // installment level delinquency grouped by rangeId, and summed up the delinquent amount
            Collection<InstallmentLevelDelinquency> installmentLevelDelinquencies = loanInstallmentDelinquencyTagData.stream()
                    .map(InstallmentLevelDelinquency::from)
                    .collect(Collectors.groupingBy(InstallmentLevelDelinquency::getRangeId, delinquentAmountSummingCollector())).values();

            // sort this based on minimum days, so ranges will be delivered in ascending order
            List<InstallmentLevelDelinquency> sorted = installmentLevelDelinquencies.stream().sorted((o1, o2) -> {
                Integer first = Optional.ofNullable(o1.getMinimumAgeDays()).orElse(0);
                Integer second = Optional.ofNullable(o2.getMinimumAgeDays()).orElse(0);
                return first.compareTo(second);
            }).toList();

            collectionData.setInstallmentLevelDelinquency(sorted);
        }
    }

    @NotNull
    private static Collector<InstallmentLevelDelinquency, ?, InstallmentLevelDelinquency> delinquentAmountSummingCollector() {
        return Collectors.reducing(new InstallmentLevelDelinquency(), (item1, item2) -> {
            final InstallmentLevelDelinquency result = new InstallmentLevelDelinquency();
            result.setRangeId(Optional.ofNullable(item1.getRangeId()).orElse(item2.getRangeId()));
            result.setClassification(Optional.ofNullable(item1.getClassification()).orElse(item2.getClassification()));
            result.setMaximumAgeDays(Optional.ofNullable(item1.getMaximumAgeDays()).orElse(item2.getMaximumAgeDays()));
            result.setMinimumAgeDays(Optional.ofNullable(item1.getMinimumAgeDays()).orElse(item2.getMinimumAgeDays()));
            result.setDelinquentAmount(MathUtil.add(item1.getDelinquentAmount(), item2.getDelinquentAmount()));
            return result;
        });
    }

    void enrichWithDelinquencyPausePeriodInfo(CollectionData collectionData, Collection<LoanDelinquencyActionData> effectiveDelinquencyList,
            LocalDate businessDate) {
        List<DelinquencyPausePeriod> result = effectiveDelinquencyList.stream() //
                .sorted(Comparator.comparing(LoanDelinquencyActionData::getStartDate)) //
                .map(lda -> toDelinquencyPausePeriod(businessDate, lda)).toList(); //
        collectionData.setDelinquencyPausePeriods(result);
    }

    @NotNull
    private static DelinquencyPausePeriod toDelinquencyPausePeriod(LocalDate businessDate, LoanDelinquencyActionData lda) {
        return new DelinquencyPausePeriod(!lda.getStartDate().isAfter(businessDate) && !businessDate.isAfter(lda.getEndDate()),
                lda.getStartDate(), lda.getEndDate());
    }

    @Override
    public Collection<LoanInstallmentDelinquencyTagData> retrieveLoanInstallmentsCurrentDelinquencyTag(Long loanId) {
        return repositoryLoanInstallmentDelinquencyTag.findInstallmentDelinquencyTags(loanId);
    }

    @Override
    public List<LoanDelinquencyAction> retrieveLoanDelinquencyActions(Long loanId) {
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);
        if (optLoan.isPresent()) {
            return loanDelinquencyActionRepository.findByLoanOrderById(optLoan.get());
        }
        return List.of();
    }

}
