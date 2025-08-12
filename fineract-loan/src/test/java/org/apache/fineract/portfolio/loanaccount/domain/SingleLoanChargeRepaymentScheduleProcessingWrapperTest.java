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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
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
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionProcessingService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class SingleLoanChargeRepaymentScheduleProcessingWrapperTest {

    private final SingleLoanChargeRepaymentScheduleProcessingWrapper underTest = new SingleLoanChargeRepaymentScheduleProcessingWrapper();
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    private MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(new CurrencyData("USD"));

    private ArgumentCaptor<Money> feeChargesDue = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> feeChargesWaived = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> feeChargesWrittenOff = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesDue = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesWaived = ArgumentCaptor.forClass(Money.class);
    private ArgumentCaptor<Money> penaltyChargesWrittenOff = ArgumentCaptor.forClass(Money.class);

    private final LoanChargeService loanChargeService = new LoanChargeService(mock(LoanChargeValidator.class),
            mock(LoanTransactionProcessingService.class), mock(LoanLifecycleStateMachine.class), mock(LoanBalanceService.class));

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @Test
    public void testOnePeriodWithFeeCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate))));

        LoanRepaymentScheduleInstallment period = createPeriod(1, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 30));
        underTest.reprocess(currency, disbursementDate, List.of(period), createCharge(false));

        customVerify(period, "10.0", "0.0", "0.0", "0.0", "0.0", "0.0");
    }

    @Test
    public void testOnePeriodWithPenaltyCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate))));

        LoanRepaymentScheduleInstallment period = createPeriod(1, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 30));
        underTest.reprocess(currency, disbursementDate, List.of(period), createCharge(true));

        customVerify(period, "0.0", "0.0", "0.0", "10.0", "0.0", "0.0");
    }

    @Test
    public void testTwoPeriodsWithPenaltyCharge() {
        LocalDate disbursementDate = LocalDate.of(2023, 1, 1);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(new EnumMap<>(Map.of(BusinessDateType.BUSINESS_DATE, disbursementDate))));

        LoanRepaymentScheduleInstallment period1 = createPeriod(1, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31));
        LoanRepaymentScheduleInstallment period2 = createPeriod(1, LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28));

        underTest.reprocess(currency, disbursementDate, List.of(period1, period2), createCharge(true));

        customVerify(period1, "0.0", "0.0", "0.0", "10.0", "0.0", "0.0");
        customVerify(period2, "0.0", "0.0", "0.0", "0.0", "0.0", "0.0");
    }

    private void customVerify(LoanRepaymentScheduleInstallment period, String expectedFeeChargesDue, String expectedFeeChargesWaived,
            String expectedFeeChargesWrittenOff, String expectedPenaltyChargesDue, String expectedPenaltyChargesWaived,
            String expectedPenaltyChargesWrittenOff) {

        verify(period, times(1)).addToChargePortion(feeChargesDue.capture(), feeChargesWaived.capture(), feeChargesWrittenOff.capture(),
                penaltyChargesDue.capture(), penaltyChargesWaived.capture(), penaltyChargesWrittenOff.capture());

        assertEquals(new BigDecimal(expectedFeeChargesDue).setScale(1, RoundingMode.UNNECESSARY),
                feeChargesDue.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal(expectedFeeChargesWaived).setScale(1, RoundingMode.UNNECESSARY),
                feeChargesWaived.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal(expectedFeeChargesWrittenOff).setScale(1, RoundingMode.UNNECESSARY),
                feeChargesWrittenOff.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal(expectedPenaltyChargesDue).setScale(1, RoundingMode.UNNECESSARY),
                penaltyChargesDue.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal(expectedPenaltyChargesWaived).setScale(1, RoundingMode.UNNECESSARY),
                penaltyChargesWaived.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
        assertEquals(new BigDecimal(expectedPenaltyChargesWrittenOff).setScale(1, RoundingMode.UNNECESSARY),
                penaltyChargesWrittenOff.getValue().getAmount().setScale(1, RoundingMode.UNNECESSARY));
    }

    @NotNull
    private LoanCharge createCharge(boolean penalty) {
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(1L);
        when(charge.getName()).thenReturn("charge a");
        when(charge.getCurrencyCode()).thenReturn("UDS");
        when(charge.isPenalty()).thenReturn(penalty);
        Loan loan = mock(Loan.class);
        when(loan.isInterestBearing()).thenReturn(false);

        return loanChargeService.create(loan, charge, new BigDecimal(1000), new BigDecimal(10), ChargeTimeType.SPECIFIED_DUE_DATE,
                ChargeCalculationType.FLAT, LocalDate.of(2023, 1, 15), ChargePaymentMode.REGULAR, 1, null, null);
    }

    @NotNull
    private LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = mock(LoanRepaymentScheduleInstallment.class);
        MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
        when(period.getInstallmentNumber()).thenReturn(periodId);
        when(period.getFromDate()).thenReturn(start);
        when(period.getDueDate()).thenReturn(end);
        Money principal = Money.of(currency, new BigDecimal("1000.0"), mc);
        Money interest = Money.of(currency, BigDecimal.ZERO, mc);

        when(period.getPrincipal(currency)).thenReturn(principal);
        when(period.getInterestCharged(currency)).thenReturn(interest);
        return period;
    }
}
