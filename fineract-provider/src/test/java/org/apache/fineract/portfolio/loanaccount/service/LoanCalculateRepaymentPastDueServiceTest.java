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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentPastDueData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanCalculateRepaymentPastDueServiceTest {

    private LoanCalculateRepaymentPastDueService underTest;
    private MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new LoanCalculateRepaymentPastDueService();
        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);
    }

    @AfterEach
    public void reset() {
        ThreadLocalContextUtil.reset();
        moneyHelper.close();
    }

    @Test
    public void testLoanRepaymentPastDueAmountCalculations() {
        // given
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        Loan loanForProcessing = Mockito.mock(Loan.class);
        MonetaryCurrency loanCurrency = Mockito.mock(MonetaryCurrency.class);
        // repayments

        // closed repayment
        LocalDate repayment1DueDate = businessDate.minusDays(5);
        LoanRepaymentScheduleInstallment repaymentInstallment_closed = new LoanRepaymentScheduleInstallment(loanForProcessing, 1,
                LocalDate.now(ZoneId.systemDefault()), repayment1DueDate, BigDecimal.valueOf(100.0), BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0), BigDecimal.valueOf(10.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));
        repaymentInstallment_closed.updateObligationMet(true);

        // past due repayments
        LocalDate repayment2DueDate = businessDate.minusDays(4);
        LoanRepaymentScheduleInstallment repaymentInstallment_1 = new LoanRepaymentScheduleInstallment(loanForProcessing, 2,
                LocalDate.now(ZoneId.systemDefault()), repayment2DueDate, BigDecimal.valueOf(100.0), BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0), BigDecimal.valueOf(10.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));

        LocalDate repayment3DueDate = DateUtils.getBusinessLocalDate().minusDays(3);
        LoanRepaymentScheduleInstallment repaymentInstallment_2 = new LoanRepaymentScheduleInstallment(loanForProcessing, 3,
                LocalDate.now(ZoneId.systemDefault()), repayment3DueDate, BigDecimal.valueOf(100.0), BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0), BigDecimal.valueOf(10.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));

        // upcoming repayment
        LocalDate repayment4DueDate = businessDate.plusDays(1);
        LoanRepaymentScheduleInstallment repaymentInstallment_upcoming = new LoanRepaymentScheduleInstallment(loanForProcessing, 4,
                LocalDate.now(ZoneId.systemDefault()), repayment4DueDate, BigDecimal.valueOf(100.0), BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(10.0), BigDecimal.valueOf(10.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));

        List<LoanRepaymentScheduleInstallment> loanRepayments = Arrays.asList(repaymentInstallment_closed, repaymentInstallment_1,
                repaymentInstallment_2, repaymentInstallment_upcoming);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepayments);
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanCurrency.getCode()).thenReturn("CODE");
        when(loanCurrency.getCurrencyInMultiplesOf()).thenReturn(1);
        when(loanCurrency.getDigitsAfterDecimal()).thenReturn(1);

        // when
        LoanRepaymentPastDueData pastDueAmount = underTest.retrieveLoanRepaymentPastDueAmountTillDate(loanForProcessing);

        // then
        assertEquals(pastDueAmount.getTotalAmount(), BigDecimal.valueOf(260.00));
        assertEquals(pastDueAmount.getPrincipalAmount(), BigDecimal.valueOf(200.00));
        assertEquals(pastDueAmount.getInterestAmount(), BigDecimal.valueOf(20.00));
        assertEquals(pastDueAmount.getFeeAmount(), BigDecimal.valueOf(20.00));
        assertEquals(pastDueAmount.getPenaltyAmount(), BigDecimal.valueOf(20.00));

    }
}
