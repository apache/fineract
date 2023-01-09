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
import static org.mockito.Mockito.mock;
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
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CurrencyDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanChargeDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanDelinquencyRangeDataMapperImpl;
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
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class LoanAccountDelinquencyRangeEventSerializerTest {

    @Mock
    private LoanReadPlatformService loanReadPlatformService;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @Test
    public void testLoanRepaymentEventPayloadSerialization() throws IOException {
        // given
        LoanDelinquencyRangeChangeBusinessEventSerializer serializer = new LoanDelinquencyRangeChangeBusinessEventSerializer(
                loanReadPlatformService, new LoanDelinquencyRangeDataMapperImpl(), new LoanChargeDataMapperImpl(),
                new CurrencyDataMapperImpl());

        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanAccountData loanAccountData = mock(LoanAccountData.class);
        MonetaryCurrency loanCurrency = new MonetaryCurrency("CODE", 1, 1);
        MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanAccountData.getId()).thenReturn(1L);
        when(loanAccountData.getAccountNo()).thenReturn("0001");
        when(loanAccountData.getExternalId()).thenReturn(ExternalIdFactory.produce("externalId"));
        when(loanAccountData.getDelinquencyRange()).thenReturn(new DelinquencyRangeData(1L, "classification", 1, 10));
        when(loanAccountData.getCurrency()).thenAnswer(a -> new CurrencyData(loanCurrency.getCode(), loanCurrency.getDigitsAfterDecimal(),
                loanCurrency.getCurrencyInMultiplesOf()));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);

        when(loanReadPlatformService.retrieveOne(any(Long.class))).thenReturn(loanAccountData);

        LoanDelinquencyRangeChangeBusinessEvent event = new LoanDelinquencyRangeChangeBusinessEvent(loanForProcessing);
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<>();

        repaymentScheduleInstallments.add(buildInstallment(loanForProcessing, loanCurrency, BigDecimal.valueOf(100), BigDecimal.valueOf(5),
                BigDecimal.valueOf(30), BigDecimal.valueOf(50), BigDecimal.valueOf(185), new BigDecimal("100.5"), new BigDecimal("200.3")));
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);

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

        // assertEquals(data, expectedSerializedData);
        moneyHelper.close();
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
        return new LoanCharge(loan, charge, amount, amount, ChargeTimeType.TRANCHE_DISBURSEMENT, ChargeCalculationType.FLAT,
                LocalDate.of(2022, 6, 27), ChargePaymentMode.REGULAR, 1, new BigDecimal(100), ExternalId.generate());
    }
}
