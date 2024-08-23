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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountDelinquencyPauseChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTag;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainService;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    @Mock
    private LoanDelinquencyDomainService loanDelinquencyDomainService;
    @Mock
    private LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository;
    @Mock
    private DelinquencyReadPlatformService delinquencyReadPlatformService;
    @Mock
    private DelinquencyActionParseAndValidator delinquencyActionParseAndValidator;
    @Mock
    private LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    @Mock
    private DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

    private DelinquencyWritePlatformServiceHelper delinquencyWritePlatformServiceHelper;

    private DelinquencyWritePlatformServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        delinquencyWritePlatformServiceHelper = Mockito.spy(new DelinquencyWritePlatformServiceHelper(businessEventNotifierService,
                loanDelinquencyTagRepository, repositoryRange, loanInstallmentDelinquencyTagRepository));
        underTest = new DelinquencyWritePlatformServiceImpl(dataValidatorBucket, dataValidatorRange, repositoryRange, repositoryBucket,
                repositoryBucketMappings, loanDelinquencyTagRepository, loanRepository, loanProductRepository, loanDelinquencyDomainService,
                loanInstallmentDelinquencyTagRepository, delinquencyReadPlatformService, loanDelinquencyActionRepository,
                delinquencyActionParseAndValidator, delinquencyEffectivePauseHelper, businessEventNotifierService,
                delinquencyWritePlatformServiceHelper);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void givenLoanAccountWithDelinquencyBucketWhenRangeChangeThenEventIsRaised() {
        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDeliquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
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
        final BigDecimal zero = BigDecimal.ZERO;
        CollectionData collectionData = new CollectionData(zero, 2L, null, 2L, overDueSinceDate, zero, null, null, null, null, null, null,
                zero, zero, zero, zero);

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(false);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(1)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDeliquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDeliquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);
    }

    @Test
    public void test_ApplyDelinquencyTagToLoan_ExecutesDelinquencyApplication_InTheRightOrder() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        final Long daysDiff = 2L;
        final LocalDate fromDate = DateUtils.getBusinessLocalDate().minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = DateUtils.getBusinessLocalDate().minusDays(daysDiff);
        final BigDecimal installmentPrincipalAmount = BigDecimal.valueOf(100);
        final BigDecimal zeroAmount = BigDecimal.ZERO;

        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1, fromDate, dueDate,
                installmentPrincipalAmount, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment.setId(1L);

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate().minusDays(2);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 1L,
                loanForProcessing);
        CollectionData collectionData = new CollectionData(zeroAmount, 2L, null, 2L, overDueSinceDate, zeroAmount, null, null, null, null,
                null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        CollectionData installmentCollectionData = new CollectionData(zeroAmount, 2L, null, 2L, overDueSinceDate,
                installmentPrincipalAmount, null, null, null, null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();
        installmentsCollection.put(1L, installmentCollectionData);

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);
        when(loanInstallmentDelinquencyTagRepository.findByLoanAndInstallment(loanForProcessing, repaymentScheduleInstallments.get(0)))
                .thenReturn(Optional.empty());

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        InOrder inOrder = inOrder(delinquencyWritePlatformServiceHelper);
        inOrder.verify(delinquencyWritePlatformServiceHelper).applyDelinquencyForLoan(eq(loanForProcessing), eq(delinquencyBucket),
                anyLong());
        inOrder.verify(delinquencyWritePlatformServiceHelper).applyDelinquencyForLoanInstallments(eq(loanForProcessing),
                eq(delinquencyBucket), eq(installmentsCollection));
    }

    @Test
    public void givenLoanAccountWithDelinquencyBucketWhenNoRangeChangeThenNoEventIsRaised() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);

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

        CollectionData collectionData = CollectionData.template();

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(0)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
    }

    @Test
    public void givenLoanAccountWithNoDelinquencyBucketThenNoEventIsRaised() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate();
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 2L,
                loanForProcessing);

        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(false);

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(0)).saveAllAndFlush(anyIterable());
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());

    }

    @Test
    public void givenLoanAccountWithOverdueInstallmentAndEnableInstallmentThenDelinquencyRangeIsSetForInstallmentTest() {
        ArgumentCaptor<List<LoanInstallmentDelinquencyTag>> loanInstallmentDelinquencyTagsArgumentCaptor = ArgumentCaptor
                .forClass(List.class);
        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDelinquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        final Long daysDiff = 2L;
        final LocalDate fromDate = DateUtils.getBusinessLocalDate().minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = DateUtils.getBusinessLocalDate().minusDays(daysDiff);
        final BigDecimal installmentPrincipalAmount = BigDecimal.valueOf(100);
        final BigDecimal zeroAmount = BigDecimal.ZERO;

        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1, fromDate, dueDate,
                installmentPrincipalAmount, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment.setId(1L);

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate().minusDays(2);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 1L,
                loanForProcessing);

        CollectionData collectionData = new CollectionData(zeroAmount, 2L, null, 2L, overDueSinceDate, zeroAmount, null, null, null, null,
                null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        CollectionData installmentCollectionData = new CollectionData(zeroAmount, 2L, null, 2L, overDueSinceDate,
                installmentPrincipalAmount, null, null, null, null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();
        installmentsCollection.put(1L, installmentCollectionData);

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);
        when(loanInstallmentDelinquencyTagRepository.findByLoanAndInstallment(loanForProcessing, repaymentScheduleInstallments.get(0)))
                .thenReturn(Optional.empty());

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(1)).saveAllAndFlush(anyIterable());
        verify(loanInstallmentDelinquencyTagRepository, times(1)).saveAllAndFlush(loanInstallmentDelinquencyTagsArgumentCaptor.capture());

        List<LoanInstallmentDelinquencyTag> installmentDelinquencyTags = loanInstallmentDelinquencyTagsArgumentCaptor.getValue();
        assertEquals(1, installmentDelinquencyTags.size());
        assertEquals(1, installmentDelinquencyTags.get(0).getInstallment().getInstallmentNumber());
        assertEquals(1, installmentDelinquencyTags.get(0).getDelinquencyRange().getId());
        assertEquals(installmentPrincipalAmount, installmentDelinquencyTags.get(0).getOutstandingAmount());

        // verify range change event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDelinquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);
    }

    @Test
    public void givenLoanAccountWithOverdueInstallmentAndEnableInstallmentThenDelinquencyRangeChangesForInstallmentTest() {
        ArgumentCaptor<List<LoanInstallmentDelinquencyTag>> loanInstallmentDelinquencyTagsArgumentCaptor = ArgumentCaptor
                .forClass(List.class);
        ArgumentCaptor<LoanInstallmentDelinquencyTag> loanInstallmentDelinquencyTagArgumentCaptorForDelete = ArgumentCaptor
                .forClass(LoanInstallmentDelinquencyTag.class);

        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDelinquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        final Long daysDiff = 2L;
        final LocalDate fromDate = DateUtils.getBusinessLocalDate().minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = DateUtils.getBusinessLocalDate().minusDays(daysDiff);
        final BigDecimal installmentPrincipalAmount = BigDecimal.valueOf(100);
        final BigDecimal zeroAmount = BigDecimal.ZERO;

        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1, fromDate, dueDate,
                installmentPrincipalAmount, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment.setId(1L);

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate().minusDays(29);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 1L,
                loanForProcessing);
        CollectionData collectionData = new CollectionData(BigDecimal.ZERO, 29L, null, 29L, overDueSinceDate, zeroAmount, null, null, null,
                null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        CollectionData installmentCollectionData = new CollectionData(zeroAmount, 29L, null, 29L, overDueSinceDate,
                installmentPrincipalAmount, null, null, null, null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();
        installmentsCollection.put(1L, installmentCollectionData);

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        LoanInstallmentDelinquencyTag previousInstallmentTag = new LoanInstallmentDelinquencyTag();
        previousInstallmentTag.setDelinquencyRange(range1);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);
        when(loanInstallmentDelinquencyTagRepository.findByLoanAndInstallment(loanForProcessing, repaymentScheduleInstallments.get(0)))
                .thenReturn(Optional.of(previousInstallmentTag));

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(1)).saveAllAndFlush(anyIterable());
        verify(loanInstallmentDelinquencyTagRepository, times(1)).saveAllAndFlush(loanInstallmentDelinquencyTagsArgumentCaptor.capture());
        verify(loanInstallmentDelinquencyTagRepository, times(1)).delete(loanInstallmentDelinquencyTagArgumentCaptorForDelete.capture());

        List<LoanInstallmentDelinquencyTag> installmentDelinquencyTags = loanInstallmentDelinquencyTagsArgumentCaptor.getValue();
        assertEquals(1, installmentDelinquencyTags.size());
        assertEquals(1, installmentDelinquencyTags.get(0).getInstallment().getInstallmentNumber());
        assertEquals(2, installmentDelinquencyTags.get(0).getDelinquencyRange().getId());
        assertEquals(installmentPrincipalAmount, installmentDelinquencyTags.get(0).getOutstandingAmount());

        LoanInstallmentDelinquencyTag deletedInstallmentDelinquencyTag = loanInstallmentDelinquencyTagArgumentCaptorForDelete.getValue();
        assertNotNull(deletedInstallmentDelinquencyTag);
        assertEquals(previousInstallmentTag, deletedInstallmentDelinquencyTag);

        // verify range change event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDelinquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);

    }

    @Test
    public void givenLoanAccountWithOverdueInstallmentsAndEnableInstallmentThenDelinquencyRangeChangesEventWhenOneOfInstallmentIsOutOfDelinquencyTest() {
        ArgumentCaptor<LoanInstallmentDelinquencyTag> loanInstallmentDelinquencyTagArgumentCaptorForDelete = ArgumentCaptor
                .forClass(LoanInstallmentDelinquencyTag.class);

        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDelinquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        final Long daysDiff = 2L;
        final LocalDate fromDate = DateUtils.getBusinessLocalDate().minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = DateUtils.getBusinessLocalDate().minusDays(daysDiff);
        final BigDecimal installmentPrincipalAmount = BigDecimal.valueOf(100);
        final BigDecimal zeroAmount = BigDecimal.ZERO;

        LoanRepaymentScheduleInstallment installment_1 = new LoanRepaymentScheduleInstallment(loanForProcessing, 1, fromDate, dueDate,
                installmentPrincipalAmount, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment_1.setId(1L);

        LoanRepaymentScheduleInstallment installment_2 = new LoanRepaymentScheduleInstallment(loanForProcessing, 2, fromDate, dueDate,
                installmentPrincipalAmount, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment_2.setId(2L);

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment_1, installment_2);

        LocalDate overDueSinceDate = DateUtils.getBusinessLocalDate().minusDays(29);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(1L, overDueSinceDate, 1L,
                loanForProcessing);
        CollectionData collectionData = new CollectionData(zeroAmount, 29L, null, 29L, overDueSinceDate, zeroAmount, null, null, null, null,
                null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        CollectionData installmentCollectionData_1 = new CollectionData(zeroAmount, 29L, null, 29L, overDueSinceDate,
                installmentPrincipalAmount, null, null, null, null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        CollectionData installmentCollectionData_2 = new CollectionData(zeroAmount, 0L, null, 0L, null, installmentPrincipalAmount, null,
                null, null, null, null, null, zeroAmount, zeroAmount, zeroAmount, zeroAmount);

        Map<Long, CollectionData> installmentsCollection = new HashMap<>();
        installmentsCollection.put(1L, installmentCollectionData_1);
        installmentsCollection.put(2L, installmentCollectionData_2);

        LoanDelinquencyData loanDelinquencyData = new LoanDelinquencyData(collectionData, installmentsCollection);

        LoanInstallmentDelinquencyTag previousInstallmentTag_1 = new LoanInstallmentDelinquencyTag();
        previousInstallmentTag_1.setDelinquencyRange(range2);

        LoanInstallmentDelinquencyTag previousInstallmentTag = new LoanInstallmentDelinquencyTag();
        previousInstallmentTag.setDelinquencyRange(range1);

        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDelinquencyBucket()).thenReturn(delinquencyBucket);
        when(loanForProcessing.hasDelinquencyBucket()).thenReturn(true);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());
        when(loanDelinquencyDomainService.getLoanDelinquencyData(loanForProcessing, effectiveDelinquencyList))
                .thenReturn(loanDelinquencyData);
        when(loanInstallmentDelinquencyTagRepository.findByLoanAndInstallment(loanForProcessing, repaymentScheduleInstallments.get(0)))
                .thenReturn(Optional.of(previousInstallmentTag_1));
        when(loanInstallmentDelinquencyTagRepository.findByLoanAndInstallment(loanForProcessing, repaymentScheduleInstallments.get(1)))
                .thenReturn(Optional.of(previousInstallmentTag));

        // when
        underTest.applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);

        // then
        verify(loanDelinquencyTagRepository, times(1)).saveAllAndFlush(anyIterable());
        verify(loanInstallmentDelinquencyTagRepository, times(1)).delete(loanInstallmentDelinquencyTagArgumentCaptorForDelete.capture());

        LoanInstallmentDelinquencyTag deletedInstallmentDelinquencyTag = loanInstallmentDelinquencyTagArgumentCaptorForDelete.getValue();
        assertNotNull(deletedInstallmentDelinquencyTag);
        assertEquals(previousInstallmentTag, deletedInstallmentDelinquencyTag);

        // verify range change event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDelinquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);

    }

    @Test
    public void givenLoanAccountEnableInstallmentLevelDelinquencyWhenLoanIsOutOfDelinquencyEventIsRaisedTest() {
        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDelinquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        DelinquencyRange range1 = DelinquencyRange.instance("Range1", 1, 2);
        range1.setId(1L);
        DelinquencyRange range2 = DelinquencyRange.instance("Range30", 3, 30);
        range2.setId(2L);
        List<DelinquencyRange> listDelinquencyRanges = Arrays.asList(range1, range2);
        DelinquencyBucket delinquencyBucket = new DelinquencyBucket("test Bucket");
        delinquencyBucket.setRanges(listDelinquencyRanges);

        LoanDelinquencyTagHistory prevTagForLoan = new LoanDelinquencyTagHistory();
        prevTagForLoan.setDelinquencyRange(range1);

        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.of(prevTagForLoan));

        // when
        underTest.removeDelinquencyTagToLoan(loanForProcessing);

        verify(loanInstallmentDelinquencyTagRepository, times(1)).deleteAllLoanInstallmentsTags(anyLong());
        // verify range change event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyRangeChangeEvent.capture());
        Loan loanPayloadForEvent = loanDelinquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);

    }

    @Test
    public void givenLoanAccountWhenBackdatedPauseActionThenLoanDelinquencyPauseChangeBusinessEventIsRaisedTest() {
        ArgumentCaptor<LoanAccountDelinquencyPauseChangedBusinessEvent> loanDelinquencyPauseChangeEvent = ArgumentCaptor
                .forClass(LoanAccountDelinquencyPauseChangedBusinessEvent.class);
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);

        JsonCommand command = Mockito.mock(JsonCommand.class);

        // Pause period
        LocalDate startDate = DateUtils.getBusinessLocalDate().minusDays(8);
        LocalDate endDate = DateUtils.getBusinessLocalDate().minusDays(1);

        List<LoanDelinquencyAction> delinquencyActions = new ArrayList<>();
        List<LoanDelinquencyActionData> effectiveDelinquency = new ArrayList<>();
        CollectionData loanCollectionData = CollectionData.template();

        when(loanRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(loanForProcessing);

        when(delinquencyReadPlatformService.retrieveLoanDelinquencyActions(anyLong())).thenReturn(delinquencyActions);
        LoanDelinquencyAction backdatedPauseAction = Mockito.mock(LoanDelinquencyAction.class);
        backdatedPauseAction.setId(1L);

        when(delinquencyActionParseAndValidator.validateAndParseUpdate(command, loanForProcessing, delinquencyActions,
                DateUtils.getBusinessLocalDate())).thenReturn(backdatedPauseAction);
        when(backdatedPauseAction.getStartDate()).thenReturn(startDate);
        when(backdatedPauseAction.getEndDate()).thenReturn(endDate);
        when(backdatedPauseAction.getAction()).thenReturn(DelinquencyAction.PAUSE);

        when(loanDelinquencyActionRepository.saveAndFlush(backdatedPauseAction)).thenReturn(backdatedPauseAction);

        when(delinquencyEffectivePauseHelper.calculateEffectiveDelinquencyList(delinquencyActions)).thenReturn(effectiveDelinquency);
        when(loanDelinquencyDomainService.getOverdueCollectionData(any(), anyList())).thenReturn(loanCollectionData);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());

        // when
        underTest.createDelinquencyAction(loanForProcessing.getId(), command);

        // then
        // verify event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyPauseChangeEvent.capture());

        Loan loanPayloadForEvent = loanDelinquencyPauseChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);

        // verify no range change event for pause flag change as both start and end date are backdated
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any(LoanDelinquencyRangeChangeBusinessEvent.class));

    }

    @Test
    public void givenLoanAccountWhenBackdatedPauseActionThenLoanDelinquencyRangeChangeBusinessEventIsRaisedIfPauseFlagChangeTest() {
        ArgumentCaptor<LoanDelinquencyRangeChangeBusinessEvent> loanDelinquencyRangeChangeEvent = ArgumentCaptor
                .forClass(LoanDelinquencyRangeChangeBusinessEvent.class);
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);

        JsonCommand command = Mockito.mock(JsonCommand.class);

        // Pause period
        LocalDate startDate = DateUtils.getBusinessLocalDate().minusDays(2);
        LocalDate endDate = DateUtils.getBusinessLocalDate().plusDays(10);

        List<LoanDelinquencyAction> delinquencyActions = new ArrayList<>();
        List<LoanDelinquencyActionData> effectiveDelinquency = new ArrayList<>();
        CollectionData loanCollectionData = CollectionData.template();

        when(loanRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(loanForProcessing);

        when(delinquencyReadPlatformService.retrieveLoanDelinquencyActions(anyLong())).thenReturn(delinquencyActions);
        LoanDelinquencyAction backdatedPauseAction = Mockito.mock(LoanDelinquencyAction.class);
        backdatedPauseAction.setId(1L);

        when(delinquencyActionParseAndValidator.validateAndParseUpdate(command, loanForProcessing, delinquencyActions,
                DateUtils.getBusinessLocalDate())).thenReturn(backdatedPauseAction);
        when(backdatedPauseAction.getStartDate()).thenReturn(startDate);
        when(backdatedPauseAction.getEndDate()).thenReturn(endDate);
        when(backdatedPauseAction.getAction()).thenReturn(DelinquencyAction.PAUSE);

        when(loanDelinquencyActionRepository.saveAndFlush(backdatedPauseAction)).thenReturn(backdatedPauseAction);

        when(delinquencyEffectivePauseHelper.calculateEffectiveDelinquencyList(delinquencyActions)).thenReturn(effectiveDelinquency);
        when(loanDelinquencyDomainService.getOverdueCollectionData(any(), anyList())).thenReturn(loanCollectionData);
        when(loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(any(), any())).thenReturn(Optional.empty());

        // when
        underTest.createDelinquencyAction(loanForProcessing.getId(), command);

        // then
        // verify event is raised
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanDelinquencyRangeChangeEvent.capture());

        Loan loanPayloadForEvent = loanDelinquencyRangeChangeEvent.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);
    }
}
