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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DelinquencyReadPlatformServiceImpl implements DelinquencyReadPlatformService {

    private final DelinquencyRangeRepository repositoryRange;
    private final DelinquencyBucketRepository repositoryBucket;
    private final LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    private final DelinquencyRangeMapper mapperRange;
    private final DelinquencyBucketMapper mapperBucket;
    private final LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;
    private final LoanRepository loanRepository;

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
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);

        CollectionData collectionData = CollectionData.template();
        if (optLoan.isPresent()) {
            final Loan loan = optLoan.get();

            collectionData.setAvailableDisbursementAmount(loan.getApprovedPrincipal().subtract(loan.getDisbursedAmount()));
            collectionData.setNextPaymentDueDate(loan.possibleNextRepaymentDate());

            final MonetaryCurrency currency = loan.getCurrency();
            BigDecimal delinquentAmount = BigDecimal.ZERO;
            // Overdue Days calculation
            Long overdueDays = 0L;
            LocalDate overdueSinceDate = null;
            final List<LoanTransaction> chargebackTransactions = loan.retrieveListOfTransactionsByType(LoanTransactionType.CHARGEBACK);
            for (LoanTransaction loanTransaction : chargebackTransactions) {
                Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = loanTransaction
                        .getLoanTransactionToRepaymentScheduleMappings();
                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = loanTransactionToRepaymentScheduleMappings
                        .iterator().next();
                if (!loanTransactionToRepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().isPrincipalCompleted(currency)) {
                    overdueSinceDate = loanTransaction.getTransactionDate();
                    break;
                }
            }

            final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            if (overdueSinceDate == null) {
                for (LoanRepaymentScheduleInstallment installment : installments) {
                    if (installment.isNotFullyPaidOff()) {
                        overdueSinceDate = installment.getDueDate();
                        break;
                    }
                }
            }

            Integer graceDays = 0;
            if (overdueSinceDate != null) {
                if (loan.loanProduct().getLoanProductConfigurableAttributes().getGraceOnArrearsAgingBoolean()) {
                    graceDays = loan.loanProduct().getLoanProductRelatedDetail().getGraceOnArrearsAgeing();
                    if (graceDays == null) {
                        graceDays = 0;
                    }
                    overdueSinceDate = overdueSinceDate.plusDays(graceDays);
                }
                overdueDays = DateUtils.getDifferenceInDays(overdueSinceDate, businessDate);
                if (overdueDays < 0) {
                    overdueDays = 0L;
                }
                collectionData.setDelinquentDate(overdueSinceDate);
            }

            collectionData.setPastDueDays(overdueDays);
            if (overdueDays > 0) {
                collectionData.setDelinquentDays(overdueDays - graceDays);
            }

            final LoanTransaction lastRepaymenTransaction = loan.getLastRepaymentTransaction();
            if (lastRepaymenTransaction != null) {
                collectionData.setLastPaymentDate(lastRepaymenTransaction.getTransactionDate());
                collectionData.setLastPaymentAmount(lastRepaymenTransaction.getAmount());
            }

            // Calculate Delinquency Amount
            for (LoanRepaymentScheduleInstallment installment : installments) {
                if (installment.getDueDate().isBefore(businessDate)) {
                    delinquentAmount = delinquentAmount.add(installment.getTotalOutstanding(currency).getAmount());
                }
            }
            for (LoanTransaction loanTransaction : chargebackTransactions) {
                Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = loanTransaction
                        .getLoanTransactionToRepaymentScheduleMappings();
                for (LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping : loanTransactionToRepaymentScheduleMappings) {
                    LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanTransactionToRepaymentScheduleMapping
                            .getLoanRepaymentScheduleInstallment();
                    if (!loanRepaymentScheduleInstallment.isPrincipalCompleted(currency)) {
                        delinquentAmount = delinquentAmount.add(loanTransaction.getAmount());
                    }
                }
            }

            collectionData.setDelinquentAmount(delinquentAmount);
        }

        return collectionData;
    }

}
