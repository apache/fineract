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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanRangeScheduleDelinquencyData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapperImpl;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkingCapitalLoanDelinquencyReadPlatformServiceImplTest {

    private static final Long LOAN_ID = 7L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 20);

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;

    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository delinquencyActionRepository;

    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;

    private WorkingCapitalLoanDelinquencyReadPlatformServiceImpl service;

    private final DelinquencyRange r1 = range(1L, "R1", 1, 30);
    private final DelinquencyRange r2 = range(2L, "R2", 31, 60);
    private final List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> tags = new ArrayList<>();

    private FineractPlatformTenant originalTenant;

    @BeforeEach
    public void setUp() {
        service = new WorkingCapitalLoanDelinquencyReadPlatformServiceImpl(
                new WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapperImpl(Mockito.mock(DelinquencyRangeMapper.class)),
                delinquencyRangeScheduleTagHistoryRepository, delinquencyRangeScheduleRepository, delinquencyActionRepository,
                transactionFinder);
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        when(delinquencyRangeScheduleTagHistoryRepository.findByLoanIdOrderByAddedOnDateDesc(anyLong())).thenAnswer(invocation -> tags
                .stream().sorted(Comparator.comparing(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory::getAddedOnDate).reversed())
                .toList());
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
        MoneyHelper.clearCacheForTenant("default");
    }

    @Test
    public void lastPaymentAndLastRepayment_areReportedOnTheirOwnFields() {
        when(transactionFinder.findLastPayment(LOAN_ID))
                .thenReturn(Optional.of(new TransactionDateAndAmountHolder(LocalDate.of(2026, 1, 20), new BigDecimal("10.00"))));
        when(transactionFinder.findLastRepayment(LOAN_ID))
                .thenReturn(Optional.of(new TransactionDateAndAmountHolder(LocalDate.of(2026, 1, 15), new BigDecimal("20.00"))));

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, BUSINESS_DATE);

        assertThat(result.getLastPaymentDate()).isEqualTo(LocalDate.of(2026, 1, 20));
        assertThat(result.getLastPaymentAmount()).isEqualByComparingTo("10.00");
        assertThat(result.getLastRepaymentDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(result.getLastRepaymentAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    public void aPaymentThatIsNotARepayment_movesOnlyTheLastPayment() {
        final LocalDate goodwillCreditDate = LocalDate.of(2026, 1, 20);
        final LocalDate repaymentDate = LocalDate.of(2026, 1, 15);
        when(transactionFinder.findLastPayment(LOAN_ID))
                .thenReturn(Optional.of(new TransactionDateAndAmountHolder(goodwillCreditDate, new BigDecimal("10.00"))));
        when(transactionFinder.findLastRepayment(LOAN_ID))
                .thenReturn(Optional.of(new TransactionDateAndAmountHolder(repaymentDate, new BigDecimal("20.00"))));

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, BUSINESS_DATE);

        assertThat(result.getLastPaymentDate()).isEqualTo(goodwillCreditDate);
        assertThat(result.getLastRepaymentDate()).isEqualTo(repaymentDate);
    }

    @Test
    public void noPayments_leavesAllFourFieldsNull() {
        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, BUSINESS_DATE);

        assertThat(result.getLastPaymentDate()).isNull();
        assertThat(result.getLastPaymentAmount()).isNull();
        assertThat(result.getLastRepaymentDate()).isNull();
        assertThat(result.getLastRepaymentAmount()).isNull();
    }

    /** Loan state at 2026-02-16: P1 escalated R1 -> R2 on 02-15, P2 and P3 in R1. */
    private void givenEscalatedPeriodAndTwoPeriodsInFirstRange() {
        final WorkingCapitalLoanDelinquencyRangeSchedule p1 = period(1L, 1, "270");
        final WorkingCapitalLoanDelinquencyRangeSchedule p2 = period(2L, 2, "270");
        final WorkingCapitalLoanDelinquencyRangeSchedule p3 = period(3L, 3, "270");
        tags.add(tag(p1, r1, "2026-01-16", "2026-02-15", "270"));
        tags.add(tag(p1, r2, "2026-02-15", null, "270"));
        tags.add(tag(p2, r1, "2026-01-31", null, "270"));
        tags.add(tag(p3, r1, "2026-02-15", null, "270"));
        when(delinquencyRangeScheduleRepository.getTotalDelinquentAmount(LOAN_ID)).thenReturn(new BigDecimal("810"));
    }

    @Test
    void installmentLevelDelinquency_isGroupedByRangeSummedAndSortedByMinimumAge() {
        givenEscalatedPeriodAndTwoPeriodsInFirstRange();

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, LocalDate.of(2026, 2, 16));

        assertThat(result.getInstallmentLevelDelinquency())
                .extracting(WorkingCapitalLoanRangeScheduleDelinquencyData::getRangeId,
                        WorkingCapitalLoanRangeScheduleDelinquencyData::getClassification,
                        WorkingCapitalLoanRangeScheduleDelinquencyData::getMinimumAgeDays,
                        WorkingCapitalLoanRangeScheduleDelinquencyData::getMaximumAgeDays)
                .containsExactly(tuple(1L, "R1", 1, 30), tuple(2L, "R2", 31, 60));
        assertThat(result.getInstallmentLevelDelinquency().get(0).getDelinquentAmount()).isEqualByComparingTo("540");
        assertThat(result.getInstallmentLevelDelinquency().get(1).getDelinquentAmount()).isEqualByComparingTo("270");
    }

    @Test
    void escalationChain_keepsLoanLevelDelinquencyStartAtFirstTag() {
        givenEscalatedPeriodAndTwoPeriodsInFirstRange();

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, LocalDate.of(2026, 2, 16));

        assertThat(result.getDelinquentDate()).isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(result.getDelinquentDays()).isEqualTo(32L);
        assertThat(result.getDelinquentAmount()).isEqualByComparingTo("810");
    }

    @Test
    void resetGap_breaksTheChain() {
        final WorkingCapitalLoanDelinquencyRangeSchedule p1 = period(1L, 1, "270");
        tags.add(tag(p1, r1, "2026-01-16", "2026-01-20", "270"));
        tags.add(tag(p1, r1, "2026-01-25", null, "270"));
        when(delinquencyRangeScheduleRepository.getTotalDelinquentAmount(LOAN_ID)).thenReturn(new BigDecimal("270"));

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, LocalDate.of(2026, 2, 1));

        assertThat(result.getDelinquentDate()).isEqualTo(LocalDate.of(2026, 1, 25));
        assertThat(result.getDelinquentDays()).isEqualTo(8L);
    }

    @Test
    void selfHealedLegacyRows_stillChainToTheFirstTag() {
        final WorkingCapitalLoanDelinquencyRangeSchedule p1 = period(1L, 1, "270");
        tags.add(tag(p1, r1, "2026-01-16", "2026-02-20", null));
        tags.add(tag(p1, r2, "2026-02-15", null, "270"));
        when(delinquencyRangeScheduleRepository.getTotalDelinquentAmount(LOAN_ID)).thenReturn(new BigDecimal("270"));

        final WorkingCapitalLoanCollectionData result = service.getCollectionData(LOAN_ID, LocalDate.of(2026, 2, 20));

        assertThat(result.getDelinquentDate()).isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(result.getDelinquentDays()).isEqualTo(36L);
    }

    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistory tag(final WorkingCapitalLoanDelinquencyRangeSchedule period,
            final DelinquencyRange range, final String addedOnDate, final String liftedOnDate, final String outstandingAmount) {
        final WorkingCapitalLoanDelinquencyRangeScheduleTagHistory tag = new WorkingCapitalLoanDelinquencyRangeScheduleTagHistory();
        tag.setId((long) (tags.size() + 100));
        tag.setRangeSchedule(period);
        tag.setDelinquencyRange(range);
        tag.setAddedOnDate(LocalDate.parse(addedOnDate));
        tag.setLiftedOnDate(liftedOnDate == null ? null : LocalDate.parse(liftedOnDate));
        tag.setOutstandingAmount(outstandingAmount == null ? null : new BigDecimal(outstandingAmount));
        return tag;
    }

    private static WorkingCapitalLoanDelinquencyRangeSchedule period(final Long id, final int periodNumber, final String delinquentAmount) {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setId(id);
        period.setPeriodNumber(periodNumber);
        period.setDelinquentAmount(new BigDecimal(delinquentAmount));
        period.setOutstandingAmount(new BigDecimal(delinquentAmount));
        return period;
    }

    private static DelinquencyRange range(final Long id, final String classification, final int min, final Integer max) {
        final DelinquencyRange range = DelinquencyRange.instance(classification, min, max);
        range.setId(id);
        return range;
    }
}
