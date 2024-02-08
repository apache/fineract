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

import static java.time.Month.JANUARY;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.RESUME;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelinquencyReadPlatformServiceImplTest {

    @Mock
    private DelinquencyRangeRepository repositoryRange;

    @Mock
    private DelinquencyBucketRepository repositoryBucket;
    @Mock
    private LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    @Mock
    private DelinquencyRangeMapper mapperRange;
    @Mock
    private DelinquencyBucketMapper mapperBucket;

    @Mock
    private LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanDelinquencyDomainService loanDelinquencyDomainService;

    @Mock
    private LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag;

    @Mock
    private LoanDelinquencyActionRepository loanDelinquencyActionRepository;

    @InjectMocks
    private DelinquencyReadPlatformServiceImpl underTest;

    @Test
    public void testNoEnrichmentWhenThereIsNoDelinquencyAction() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of();

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        Assertions.assertFalse(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertNull(collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertNull(collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseFirstDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        Assertions.assertTrue(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 12), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 13), collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseLastDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        // then
        Assertions.assertTrue(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 12), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 13), collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentBusinessDateBetweenStartAndEndDate() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 14)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        // then
        Assertions.assertTrue(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 12), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 14), collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testMultiplePausesWithoutResumeCurrentBusinessDateIsNotOverlappingWithAnyOfThePauses() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        Assertions.assertFalse(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 10), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 11), collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testResumeIsAppliedToOneOfThePauseNotActive() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 20)),
                new LoanDelinquencyAction(null, RESUME, LocalDate.of(2023, JANUARY, 11), null),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        Assertions.assertFalse(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 10), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 11), collectionData.getDelinquencyPausePeriodEndDate());
    }

    @Test
    public void testResumeIsAppliedToOneOfThePauseActive() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyAction> delinquencyActions = List.of(
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 20)),
                new LoanDelinquencyAction(null, RESUME, LocalDate.of(2023, JANUARY, 11), null),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14)),
                new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20)));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 11));

        // then
        Assertions.assertTrue(collectionData.isDelinquencyCalculationPaused());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 10), collectionData.getDelinquencyPausePeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2023, JANUARY, 11), collectionData.getDelinquencyPausePeriodEndDate());
    }

}
