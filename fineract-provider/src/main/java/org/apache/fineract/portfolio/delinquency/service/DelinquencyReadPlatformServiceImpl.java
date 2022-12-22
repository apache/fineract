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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
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
    private final LoanDelinquencyDomainService loanDelinquencyDomainService;

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

            final LoanTransaction lastRepaymenTransaction = loan.getLastRepaymentTransaction();
            if (lastRepaymenTransaction != null) {
                collectionData.setLastPaymentDate(lastRepaymenTransaction.getTransactionDate());
                collectionData.setLastPaymentAmount(lastRepaymenTransaction.getAmount());
            }
        }

        return collectionData;
    }

}
