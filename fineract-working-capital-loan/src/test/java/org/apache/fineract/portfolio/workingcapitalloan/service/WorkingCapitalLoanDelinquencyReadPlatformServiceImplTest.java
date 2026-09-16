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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkingCapitalLoanDelinquencyReadPlatformServiceImplTest {

    private static final Long LOAN_ID = 7L;
    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 20);

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper delinquencyRangeScheduleTagHistoryMapper;

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;

    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository delinquencyActionRepository;

    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;

    @InjectMocks
    private WorkingCapitalLoanDelinquencyReadPlatformServiceImpl service;

    @BeforeEach
    public void noDelinquency() {
        when(delinquencyRangeScheduleTagHistoryRepository.findByLoanIdOrderByAddedOnDateDesc(anyLong())).thenReturn(List.of());
        when(delinquencyRangeScheduleRepository.findTopByLoanIdAndMinPaymentCriteriaMetFalseOrderByFromDateAsc(anyLong()))
                .thenReturn(Optional.empty());
        when(delinquencyActionRepository.findByWorkingCapitalLoanIdAndActionOrderByStartDateAsc(anyLong(), any())).thenReturn(List.of());
        when(transactionFinder.findLastPayment(anyLong())).thenReturn(Optional.empty());
        when(transactionFinder.findLastRepayment(anyLong())).thenReturn(Optional.empty());
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
}
