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
package org.apache.fineract.portfolio.deliquency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DelinquencyWritePlatformServiceRangeChangeEventTest {

    @Mock
    private DelinquencyBucketParseAndValidator dataValidatorBucket;
    @Mock
    private DelinquencyRangeParseAndValidator dataValidatorRange;
    @Mock
    private DelinquencyRangeRepository repositoryRange;
    @Mock
    private DelinquencyBucketRepository repositoryBucket;
    @Mock
    private DelinquencyBucketMappingsRepository repositoryBucketMappings;
    @Mock
    private LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository;
    @Mock
    private LoanRepositoryWrapper loanRepository;
    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @InjectMocks
    private DelinquencyWritePlatformServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @Test
    public void givenLoanAccountWithDelinquencyBucketWhenRangeChangeThenEventIsRaised() {
        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDeliquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate().minusDays(2);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 1L,
                loanForProcessing);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(1);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(repositoryRange.getReferenceById(anyLong())).thenReturn(range1);

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData);

        // then
        verify(loanDelinquencyTagRepository, times(1)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDeliquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDeliquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);
    }

    @Test
    public void givenLoanAccountWithDelinquencyBucketWhenNoRangeChangeThenNoEventIsRaised() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);

        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);

        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate();
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 2L,
                loanForProcessing);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(1);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData);

        // then
        verify(loanDelinquencyTagRepository, times(0)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());

    }

    @Test
    public void givenLoanAccountWithNoDelinquencyBucketThenNoEventIsRaised() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate();
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 2L,
                loanForProcessing);

        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(false);

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData);

        // then
        verify(loanDelinquencyTagRepository, times(0)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());

    }

}
