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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CurrencyDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanChargeDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanDelinquencyRangeDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainService;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanAccountDelinquencyRangeEventSerializerTest {

    @Mock
    private LoanReadPlatformService loanReadPlatformService;

    @Mock
    private LoanChargeReadPlatformService loanChargeReadPlatformService;

    @Mock
    private DelinquencyReadPlatformService delinquencyReadPlatformService;

    @Mock
    private AvroDateTimeMapper mapper;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testLoanDelinquencyRangeEventPayloadSerialization() throws IOException {
        // given
        LoanDelinquencyRangeChangeBusinessEventSerializer serializer = new LoanDelinquencyRangeChangeBusinessEventSerializer(
                loanReadPlatformService, new LoanDelinquencyRangeDataMapperImpl(), loanChargeReadPlatformService,
                delinquencyReadPlatformService, new LoanChargeDataMapperImpl(null, null, null), new CurrencyDataMapperImpl(), mapper,
                new LoanInstallmentLevelDelinquencyEventProducer(delinquencyReadPlatformService, new CurrencyDataMapperImpl()));

        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanAccountData loanAccountData = mock(LoanAccountData.class);
        CollectionData delinquentData = mock(CollectionData.class);
        MonetaryCurrency loanCurrency = new MonetaryCurrency("CODE", 1, 1);
        MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        String delinquentDateAsStr = "2022-12-01";
        LocalDate delinquentDate = LocalDate.parse(delinquentDateAsStr);

        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanAccountData.getId()).thenReturn(1L);
        when(loanAccountData.getAccountNo()).thenReturn("0001");
        when(loanAccountData.getExternalId()).thenReturn(ExternalIdFactory.produce("externalId"));
        when(loanAccountData.getDelinquencyRange()).thenReturn(new DelinquencyRangeData(1L, "classification", 1, 10));
        when(loanAccountData.getCurrency()).thenAnswer(a -> new CurrencyData(loanCurrency.getCode(), loanCurrency.getDigitsAfterDecimal(),
                loanCurrency.getCurrencyInMultiplesOf()));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(false);
        when(delinquentData.getDelinquentDate()).thenReturn(delinquentDate);
        when(loanReadPlatformService.retrieveOne(any(Long.class))).thenReturn(loanAccountData);
        when(delinquencyReadPlatformService.calculateLoanCollectionData(any(Long.class))).thenReturn(delinquentData);
        when(mapper.mapLocalDate(delinquentDate)).thenReturn(delinquentDateAsStr);

        LoanDelinquencyRangeChangeBusinessEvent event = new LoanDelinquencyRangeChangeBusinessEvent(loanForProcessing);
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();

        repaymentScheduleInstallments.add(buildInstallment(loanForProcessing, loanCurrency, BigDecimal.valueOf(100), BigDecimal.valueOf(5),
                BigDecimal.valueOf(30), BigDecimal.valueOf(50), BigDecimal.valueOf(185), new BigDecimal("100.5"), new BigDecimal("200.3")));
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanChargeReadPlatformService.retrieveLoanCharges(anyLong())).thenAnswer(a -> repaymentScheduleInstallments.get(0)
                .getInstallmentCharges().stream().map(c -> c.getLoanCharge().toData()).collect(Collectors.toList()));

        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);

        // when
        LoanAccountDelinquencyRangeDataV1 data = (LoanAccountDelinquencyRangeDataV1) serializer.toAvroDTO(event);

        // then
        assertEquals(1L, data.getLoanId());
        assertEquals("0001", data.getLoanAccountNo());
        assertEquals("externalId", data.getLoanExternalId());
        assertEquals(1L, data.getDelinquencyRange().getId());
        assertEquals("classification", data.getDelinquencyRange().getClassification());
        assertEquals(1, data.getDelinquencyRange().getMinimumAgeDays());
        assertEquals(10, data.getDelinquencyRange().getMaximumAgeDays());
        assertEquals(2, data.getCharges().size());
        assertTrue(data.getCharges().stream().anyMatch(a -> a.getAmount().compareTo(new BigDecimal("100.5")) == 0));
        assertTrue(data.getCharges().stream().anyMatch(a -> a.getAmount().compareTo(new BigDecimal("200.3")) == 0));
        assertEquals(0, data.getAmount().getTotalAmount().compareTo(new BigDecimal("185.0")));
        assertEquals(0, data.getAmount().getPrincipalAmount().compareTo(new BigDecimal("100.0")));
        assertEquals(0, data.getAmount().getInterestAmount().compareTo(new BigDecimal("30.0")));
        assertEquals(0, data.getAmount().getFeeAmount().compareTo(new BigDecimal("5.0")));
        assertEquals(0, data.getAmount().getPenaltyAmount().compareTo(new BigDecimal("50.0")));
        assertEquals(delinquentDateAsStr, data.getDelinquentDate());

        moneyHelper.close();
    }

    @Test
    public void testLoanDelinquencyRangeEventPayloadSerializationWithInstallmentDelinquencyData() throws IOException {
        // given
        LoanDelinquencyRangeChangeBusinessEventSerializer serializer = new LoanDelinquencyRangeChangeBusinessEventSerializer(
                loanReadPlatformService, new LoanDelinquencyRangeDataMapperImpl(), loanChargeReadPlatformService,
                delinquencyReadPlatformService, new LoanChargeDataMapperImpl(null, null, null), new CurrencyDataMapperImpl(), mapper,
                new LoanInstallmentLevelDelinquencyEventProducer(delinquencyReadPlatformService, new CurrencyDataMapperImpl()));

        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanAccountData loanAccountData = mock(LoanAccountData.class);
        CollectionData delinquentData = mock(CollectionData.class);
        MonetaryCurrency loanCurrency = new MonetaryCurrency("CODE", 1, 1);
        MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        String delinquentDateAsStr = "2022-12-01";
        LocalDate delinquentDate = LocalDate.parse(delinquentDateAsStr);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanAccountData.getId()).thenReturn(1L);
        when(loanAccountData.getAccountNo()).thenReturn("0001");
        when(loanAccountData.getExternalId()).thenReturn(ExternalIdFactory.produce("externalId"));
        when(loanAccountData.getDelinquencyRange()).thenReturn(new DelinquencyRangeData(1L, "classification", 1, 10));
        when(loanAccountData.getCurrency()).thenAnswer(a -> new CurrencyData(loanCurrency.getCode(), loanCurrency.getDigitsAfterDecimal(),
                loanCurrency.getCurrencyInMultiplesOf()));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanForProcessing.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(delinquentData.getDelinquentDate()).thenReturn(delinquentDate);
        when(loanReadPlatformService.retrieveOne(any(Long.class))).thenReturn(loanAccountData);
        when(delinquencyReadPlatformService.calculateLoanCollectionData(any(Long.class))).thenReturn(delinquentData);
        when(mapper.mapLocalDate(delinquentDate)).thenReturn(delinquentDateAsStr);

        LoanDelinquencyRangeChangeBusinessEvent event = new LoanDelinquencyRangeChangeBusinessEvent(loanForProcessing);

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();
        LoanRepaymentScheduleInstallment repaymentScheduleInstallment_1 = buildInstallment(loanForProcessing, loanCurrency,
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(20), BigDecimal.valueOf(150),
                new BigDecimal("10"), new BigDecimal("20"));
        when(repaymentScheduleInstallment_1.getId()).thenReturn(1L);
        when(repaymentScheduleInstallment_1.getFromDate()).thenReturn(LocalDate.of(2022, 6, 20));
        when(repaymentScheduleInstallment_1.getDueDate()).thenReturn(LocalDate.of(2022, 6, 30));
        repaymentScheduleInstallments.add(repaymentScheduleInstallment_1);

        LoanRepaymentScheduleInstallment repaymentScheduleInstallment_2 = buildInstallment(loanForProcessing, loanCurrency,
                BigDecimal.valueOf(100), BigDecimal.valueOf(0), BigDecimal.valueOf(20), BigDecimal.valueOf(0), BigDecimal.valueOf(120));
        when(repaymentScheduleInstallment_2.getId()).thenReturn(2L);
        when(repaymentScheduleInstallment_2.getFromDate()).thenReturn(LocalDate.of(2022, 7, 1));
        when(repaymentScheduleInstallment_2.getDueDate()).thenReturn(LocalDate.of(2022, 7, 10));
        repaymentScheduleInstallments.add(repaymentScheduleInstallment_2);

        LoanRepaymentScheduleInstallment repaymentScheduleInstallment_3 = buildInstallment(loanForProcessing, loanCurrency,
                BigDecimal.valueOf(100), BigDecimal.valueOf(0), BigDecimal.valueOf(20), BigDecimal.valueOf(0), BigDecimal.valueOf(120));
        when(repaymentScheduleInstallment_3.getId()).thenReturn(3L);
        when(repaymentScheduleInstallment_3.getFromDate()).thenReturn(LocalDate.of(2022, 7, 11));
        when(repaymentScheduleInstallment_3.getDueDate()).thenReturn(LocalDate.of(2022, 7, 20));
        repaymentScheduleInstallments.add(repaymentScheduleInstallment_3);

        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanChargeReadPlatformService.retrieveLoanCharges(anyLong())).thenAnswer(a -> repaymentScheduleInstallments.get(0)
                .getInstallmentCharges().stream().map(c -> c.getLoanCharge().toData()).collect(Collectors.toList()));

        List<LoanInstallmentDelinquencyTagData> installmentDelinquencyTags = new ArrayList<>();
        installmentDelinquencyTags.add(buildInstallmentDelinquencyTag(1L, 1L));
        installmentDelinquencyTags.add(buildInstallmentDelinquencyTag(2L, 1L));
        installmentDelinquencyTags.add(buildInstallmentDelinquencyTag(3L, 2L));

        when(delinquencyReadPlatformService.retrieveLoanInstallmentsCurrentDelinquencyTag(anyLong()))
                .thenReturn(installmentDelinquencyTags);

        when(loanForProcessing.getLoanCharges()).thenAnswer(a -> repaymentScheduleInstallments.get(0).getInstallmentCharges().stream()
                .map(c -> c.getLoanCharge()).collect(Collectors.toList()));

        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);

        // when
        LoanAccountDelinquencyRangeDataV1 data = (LoanAccountDelinquencyRangeDataV1) serializer.toAvroDTO(event);

        // then
        assertEquals(1L, data.getLoanId());
        assertEquals("0001", data.getLoanAccountNo());
        assertEquals("externalId", data.getLoanExternalId());
        assertEquals(1L, data.getDelinquencyRange().getId());
        assertEquals("classification", data.getDelinquencyRange().getClassification());
        assertEquals(1, data.getDelinquencyRange().getMinimumAgeDays());
        assertEquals(10, data.getDelinquencyRange().getMaximumAgeDays());
        assertEquals(2, data.getCharges().size());
        assertTrue(data.getCharges().stream().anyMatch(a -> a.getAmount().compareTo(new BigDecimal("10")) == 0));
        assertTrue(data.getCharges().stream().anyMatch(a -> a.getAmount().compareTo(new BigDecimal("20")) == 0));
        assertEquals(0, data.getAmount().getTotalAmount().compareTo(new BigDecimal("390.0")));
        assertEquals(0, data.getAmount().getPrincipalAmount().compareTo(new BigDecimal("300.0")));
        assertEquals(0, data.getAmount().getInterestAmount().compareTo(new BigDecimal("60.0")));
        assertEquals(0, data.getAmount().getFeeAmount().compareTo(new BigDecimal("10.0")));
        assertEquals(0, data.getAmount().getPenaltyAmount().compareTo(new BigDecimal("20.0")));
        assertEquals(delinquentDateAsStr, data.getDelinquentDate());

        // check installment delinquency data
        assertEquals(2, data.getInstallmentDelinquencyBuckets().size());

        // check calculations

        LoanInstallmentDelinquencyBucketDataV1 installmentDelinquencyBucketDataV1_1 = data.getInstallmentDelinquencyBuckets().get(0);
        assertEquals(1L, installmentDelinquencyBucketDataV1_1.getDelinquencyRange().getId());
        assertEquals("range_1", installmentDelinquencyBucketDataV1_1.getDelinquencyRange().getClassification());
        assertEquals(0, installmentDelinquencyBucketDataV1_1.getAmount().getTotalAmount().compareTo(new BigDecimal("270.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_1.getAmount().getPrincipalAmount().compareTo(new BigDecimal("200.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_1.getAmount().getInterestAmount().compareTo(new BigDecimal("40.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_1.getAmount().getFeeAmount().compareTo(new BigDecimal("10.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_1.getAmount().getPenaltyAmount().compareTo(new BigDecimal("20.0")));
        assertEquals(2, installmentDelinquencyBucketDataV1_1.getCharges().size());
        assertTrue(installmentDelinquencyBucketDataV1_1.getCharges().stream()
                .anyMatch(a -> a.getAmount().compareTo(new BigDecimal("10")) == 0));
        assertTrue(installmentDelinquencyBucketDataV1_1.getCharges().stream()
                .anyMatch(a -> a.getAmount().compareTo(new BigDecimal("20")) == 0));

        LoanInstallmentDelinquencyBucketDataV1 installmentDelinquencyBucketDataV1_2 = data.getInstallmentDelinquencyBuckets().get(1);
        assertEquals(2L, installmentDelinquencyBucketDataV1_2.getDelinquencyRange().getId());
        assertEquals("range_2", installmentDelinquencyBucketDataV1_2.getDelinquencyRange().getClassification());
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getAmount().getTotalAmount().compareTo(new BigDecimal("120.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getAmount().getPrincipalAmount().compareTo(new BigDecimal("100.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getAmount().getInterestAmount().compareTo(new BigDecimal("20.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getAmount().getFeeAmount().compareTo(new BigDecimal("0.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getAmount().getPenaltyAmount().compareTo(new BigDecimal("0.0")));
        assertEquals(0, installmentDelinquencyBucketDataV1_2.getCharges().size());
        moneyHelper.close();
    }

    @Test
    public void testLastRepaymentInCollectionData() {
        // given
        DelinquencyRangeRepository repositoryRange = Mockito.mock(DelinquencyRangeRepository.class);
        DelinquencyBucketRepository repositoryBucket = Mockito.mock(DelinquencyBucketRepository.class);
        LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory = Mockito.mock(LoanDelinquencyTagHistoryRepository.class);
        DelinquencyRangeMapper mapperRange = Mockito.mock(DelinquencyRangeMapper.class);
        DelinquencyBucketMapper mapperBucket = Mockito.mock(DelinquencyBucketMapper.class);
        LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory = Mockito.mock(LoanDelinquencyTagMapper.class);
        LoanRepository loanRepository = Mockito.mock(LoanRepository.class);
        LoanDelinquencyDomainService loanDelinquencyDomainService = Mockito.mock(LoanDelinquencyDomainService.class);
        LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag = Mockito
                .mock(LoanInstallmentDelinquencyTagRepository.class);
        LoanDelinquencyActionRepository loanDelinquencyActionRepository = Mockito.mock(LoanDelinquencyActionRepository.class);
        DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper = Mockito.mock(DelinquencyEffectivePauseHelper.class);
        ConfigurationDomainService configurationDomainService = Mockito.mock(ConfigurationDomainService.class);

        DelinquencyReadPlatformService delinquencyReadPlatformService = new DelinquencyReadPlatformServiceImpl(repositoryRange,
                repositoryBucket, repositoryLoanDelinquencyTagHistory, mapperRange, mapperBucket, mapperLoanDelinquencyTagHistory,
                loanRepository, loanDelinquencyDomainService, repositoryLoanInstallmentDelinquencyTag, loanDelinquencyActionRepository,
                delinquencyEffectivePauseHelper, configurationDomainService);

        Loan loan = Mockito.spy(Loan.class);
        ReflectionTestUtils.setField(loan, "loanStatus", 300);
        LoanTransaction transaction1 = Mockito.mock(LoanTransaction.class);
        LoanTransaction transaction2 = Mockito.mock(LoanTransaction.class);
        CollectionData collectionData = Mockito.mock(CollectionData.class);
        when(transaction1.isRepayment()).thenReturn(true);
        when(transaction1.isReversed()).thenReturn(false);
        LocalDate transactionDate1 = LocalDate.of(2024, 1, 1);
        when(transaction1.getTransactionDate()).thenReturn(transactionDate1);
        when(transaction1.getAmount()).thenReturn(BigDecimal.ONE);
        when(transaction2.isDownPayment()).thenReturn(true);
        when(transaction2.isReversed()).thenReturn(false);
        LocalDate transactionDate2 = LocalDate.of(2024, 1, 2);
        when(transaction2.getTransactionDate()).thenReturn(transactionDate2);
        when(transaction2.getAmount()).thenReturn(BigDecimal.TEN);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.TEN);
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.ONE);
        ReflectionTestUtils.setField(loan, "loanTransactions", List.of(transaction1, transaction2));
        when(loan.getLoanTransactions()).thenReturn(List.of(transaction1, transaction2));
        when(loanDelinquencyDomainService.getOverdueCollectionData(Mockito.any(), Mockito.anyList())).thenReturn(collectionData);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        // when
        delinquencyReadPlatformService.calculateLoanCollectionData(1L);
        // then
        verify(collectionData, times(1)).setLastRepaymentDate(LocalDate.of(2024, 1, 2));
        verify(collectionData, times(1)).setLastRepaymentAmount(BigDecimal.TEN);
    }

    private LoanInstallmentDelinquencyTagData buildInstallmentDelinquencyTag(long installmentId, long rangeId) {
        LoanInstallmentDelinquencyTagData.InstallmentDelinquencyRange delinquencyRange = mock(
                LoanInstallmentDelinquencyTagData.InstallmentDelinquencyRange.class);
        when(delinquencyRange.getId()).thenReturn(rangeId);
        when(delinquencyRange.getClassification()).thenReturn("range_" + rangeId);
        when(delinquencyRange.getMaximumAgeDays()).thenReturn(1);
        when(delinquencyRange.getMinimumAgeDays()).thenReturn(2);
        LoanInstallmentDelinquencyTagData installmentDelinquencyTagData = mock(LoanInstallmentDelinquencyTagData.class);
        when(installmentDelinquencyTagData.getId()).thenReturn(installmentId);
        when(installmentDelinquencyTagData.getDelinquencyRange()).thenReturn(delinquencyRange);
        return installmentDelinquencyTagData;
    }

    private LoanRepaymentScheduleInstallment buildInstallment(Loan loan, MonetaryCurrency currency, BigDecimal principalAmount,
            BigDecimal freeAmount, BigDecimal interestAmount, BigDecimal penaltyAmount, BigDecimal totalAmount, BigDecimal... charges) {

        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        when(installment.getPrincipalOutstanding(any())).thenAnswer(a -> Money.of(currency, principalAmount));
        when(installment.getInterestOutstanding(any())).thenAnswer(a -> Money.of(currency, interestAmount));
        when(installment.getPenaltyChargesOutstanding(any())).thenAnswer(a -> Money.of(currency, penaltyAmount));
        when(installment.getFeeChargesOutstanding(any())).thenAnswer(a -> Money.of(currency, freeAmount));
        when(installment.getTotalOutstanding(any())).thenAnswer(a -> Money.of(currency, totalAmount));
        Charge charge = mock(Charge.class);
        when(charge.getName()).thenReturn("charge");
        when(charge.toData()).thenAnswer(a -> {
            ChargeData chargeData = mock(ChargeData.class);
            when(chargeData.getCurrency()).thenAnswer(b -> new CurrencyData(currency.getCode()));
            return chargeData;
        });

        Set<LoanInstallmentCharge> installmentCharges = Arrays.stream(charges)
                .map(amount -> buildLoanInstallmentCharge(amount, charge, loan)).collect(Collectors.toSet());
        when(installment.getInstallmentCharges()).thenReturn(installmentCharges);
        return installment;
    }

    private LoanInstallmentCharge buildLoanInstallmentCharge(BigDecimal amount, Charge charge, Loan loan) {
        LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge();
        ReflectionTestUtils.setField(installmentCharge, "amount", amount);
        ReflectionTestUtils.setField(installmentCharge, "loancharge", buildLoanCharge(loan, amount, charge));
        return installmentCharge;
    }

    private LoanCharge buildLoanCharge(Loan loan, BigDecimal amount, Charge charge) {
        LoanCharge loanCharge = new LoanCharge(loan, charge, amount, amount, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.FLAT,
                LocalDate.of(2022, 6, 27), ChargePaymentMode.REGULAR, 1, new BigDecimal(100), ExternalId.generate());
        ReflectionTestUtils.setField(loanCharge, "id", 1L);
        return loanCharge;
    }
}
