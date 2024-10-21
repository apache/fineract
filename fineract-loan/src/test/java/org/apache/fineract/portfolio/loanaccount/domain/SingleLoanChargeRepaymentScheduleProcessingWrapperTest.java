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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SingleLoanChargeRepaymentScheduleProcessingWrapperTest {

    private final SingleLoanChargeRepaymentScheduleProcessingWrapper underTest = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);

    private MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(new CurrencyData("USD"));

    private ArgumentCaptor<Money> feeChargesDue = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> feeChargesWaived = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> feeChargesWrittenOff = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesDue = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesWaived = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesWrittenOff = ArgumentCaptor.forClass(Money.class);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testOnePeriodWithFeeCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 01, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate)));

        LoanRepaymentScheduleInstallment period = createPeriod(1, LocalDate.of(2023, 01, 1), LocalDate.of(2023, 01, 30));
        LoanCharge loanCharge = createCharge(false);

        underTest.reprocess(currency, disbursementDate, List.of(period), loanCharge);

        verify(period, "10.0", "0.0", "0.0", "0.0", "0.0", "0.0");
    }

    @Test
    public void testOnePeriodWithPenaltyCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 01, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate)));

        LoanRepaymentScheduleInstallment period = createPeriod(1, LocalDate.of(2023, 01, 1), LocalDate.of(2023, 01, 30));
        LoanCharge loanCharge = createCharge(true);

        underTest.reprocess(currency, disbursementDate, List.of(period), loanCharge);

        verify(period, "0.0", "0.0", "0.0", "10.0", "0.0", "0.0");
    }

    @Test
    public void testTwoPeriodsWithPenaltyCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 01, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate)));

        LoanRepaymentScheduleInstallment period1 = createPeriod(1, LocalDate.of(2023, 01, 1), LocalDate.of(2023, 01, 31));
        LoanRepaymentScheduleInstallment period2 = createPeriod(1, LocalDate.of(2023, 02, 1), LocalDate.of(2023, 02, 28));

        LoanCharge loanCharge = createCharge(true);

        underTest.reprocess(currency, disbursementDate, List.of(period1, period2), loanCharge);

        verify(period1, "0.0", "0.0", "0.0", "10.0", "0.0", "0.0");
        verify(period2, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
    }

    private void verify(LoanRepaymentScheduleInstallment period, String expectedFeeChargesDue, String expectedFeeChargesWaived,
            String expectedFeeChargesWrittenOff, String expectedPenaltyChargesDue, String expectedPenaltyChargesWaived,
            String expectedPenaltyChargesWrittenOff) {

        Mockito.verify(period, times(1)).addToChargePortion(feeChargesDue.capture(), feeChargesWaived.capture(),
                feeChargesWrittenOff.capture(), penaltyChargesDue.capture(), penaltyChargesWaived.capture(),
                penaltyChargesWrittenOff.capture());

        Assertions.assertTrue(new BigDecimal(expectedFeeChargesDue).compareTo(feeChargesDue.getValue().getAmount()) == 0);
        Assertions.assertTrue(new BigDecimal(expectedFeeChargesWaived).compareTo(feeChargesWaived.getValue().getAmount()) == 0);
        Assertions.assertTrue(new BigDecimal(expectedFeeChargesWrittenOff).compareTo(feeChargesWrittenOff.getValue().getAmount()) == 0);
        Assertions.assertTrue(new BigDecimal(expectedPenaltyChargesDue).compareTo(penaltyChargesDue.getValue().getAmount()) == 0);
        Assertions.assertTrue(new BigDecimal(expectedPenaltyChargesWaived).compareTo(penaltyChargesWaived.getValue().getAmount()) == 0);
        Assertions.assertTrue(
                new BigDecimal(expectedPenaltyChargesWrittenOff).compareTo(penaltyChargesWrittenOff.getValue().getAmount()) == 0);
    }

    @NotNull
    private static LoanCharge createCharge(boolean penalty) {
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(1L);
        when(charge.getName()).thenReturn("charge a");
        when(charge.getCurrencyCode()).thenReturn("UDS");
        when(charge.isPenalty()).thenReturn(penalty);
        Loan loan = mock(Loan.class);
        when(loan.isInterestBearing()).thenReturn(false);
        LoanCharge loanCharge = new LoanCharge(loan, charge, new BigDecimal(1000), new BigDecimal(10), ChargeTimeType.SPECIFIED_DUE_DATE,
                ChargeCalculationType.FLAT, LocalDate.of(2023, 01, 15), ChargePaymentMode.REGULAR, 1, null, null);
        return loanCharge;
    }

    @NotNull
    private LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
        Mockito.when(period.getInstallmentNumber()).thenReturn(periodId);
        Mockito.when(period.getFromDate()).thenReturn(start);
        Mockito.when(period.getDueDate()).thenReturn(end);
        Money principal = Money.of(currency, new BigDecimal("1000.0"), mc);
        Money interest = Money.of(currency, BigDecimal.ZERO, mc);

        Mockito.when(period.getPrincipal(eq(currency))).thenReturn(principal);
        Mockito.when(period.getInterestCharged(eq(currency))).thenReturn(interest);
        return period;
    }
}
