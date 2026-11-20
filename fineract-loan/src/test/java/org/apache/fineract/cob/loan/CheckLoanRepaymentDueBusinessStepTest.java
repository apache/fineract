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
package org.apache.fineract.cob.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentDueBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckLoanRepaymentDueBusinessStepTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    private CheckLoanRepaymentDueBusinessStep underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new CheckLoanRepaymentDueBusinessStep(configurationDomainService, businessEventNotifierService);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void givenLoanWithInstallmentDueAfterConfiguredDaysWhenStepExecutionThenBusinessEventIsRaised() {
        ArgumentCaptor<LoanRepaymentDueBusinessEvent> loanRepaymentDueEvent = ArgumentCaptor.forClass(LoanRepaymentDueBusinessEvent.class);
        // given
        when(configurationDomainService.retrieveRepaymentDueDays()).thenReturn(1L);
        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().plusDays(1);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        MonetaryCurrency currency = Mockito.mock(MonetaryCurrency.class);
        Money money = Mockito.mock(Money.class);
        LoanRepaymentScheduleInstallment repaymentInstallment = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays.asList(repaymentInstallment);
        when(repaymentInstallment.getDueDate()).thenReturn(loanInstallmentRepaymentDueDate);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDueDaysForRepaymentEvent()).thenReturn(null);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);
        when(loanForProcessing.getSummary().getTotalOutstanding()).thenReturn(BigDecimal.ONE);
        when(loanForProcessing.getCurrency()).thenReturn(currency);
        when(repaymentInstallment.getTotalOutstanding(currency)).thenReturn(money);
        when(money.isGreaterThanZero()).thenReturn(true);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanRepaymentDueEvent.capture());
        LoanRepaymentScheduleInstallment loanPayloadForEvent = loanRepaymentDueEvent.getValue().get();
        assertEquals(repaymentInstallment, loanPayloadForEvent);
        assertEquals(processedLoan, loanForProcessing);

    }

    @Test
    public void givenLoanWithNoInstallmentDueAfterConfiguredDaysWhenStepExecutionThenNoBusinessEventIsRaised() {
        // given
        when(configurationDomainService.retrieveRepaymentDueDays()).thenReturn(1L);
        LocalDate loanInstallmentRepaymentDueDateAfter5Days = DateUtils.getBusinessLocalDate().plusDays(5);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays
                .asList(new LoanRepaymentScheduleInstallment(loanForProcessing, 1, LocalDate.now(ZoneId.systemDefault()),
                        loanInstallmentRepaymentDueDateAfter5Days, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                        BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0)));
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getDueDaysForRepaymentEvent()).thenReturn(null);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(processedLoan, loanForProcessing);

    }

    @Test
    public void givenLoanWithInstallmentDueAfterConfiguredDaysInLoanProductWhenStepExecutionThenBusinessEventIsRaised() {
        ArgumentCaptor<LoanRepaymentDueBusinessEvent> loanRepaymentDueEvent = ArgumentCaptor.forClass(LoanRepaymentDueBusinessEvent.class);
        // given
        // Global config settings
        when(configurationDomainService.retrieveRepaymentDueDays()).thenReturn(2L);
        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().plusDays(1);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        MonetaryCurrency currency = Mockito.mock(MonetaryCurrency.class);
        Money money = Mockito.mock(Money.class);
        LoanRepaymentScheduleInstallment repaymentInstallment = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays.asList(repaymentInstallment);
        when(repaymentInstallment.getDueDate()).thenReturn(loanInstallmentRepaymentDueDate);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        // Loan Product setting overrides global settings
        when(loanProduct.getDueDaysForRepaymentEvent()).thenReturn(1);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);
        when(loanForProcessing.getSummary().getTotalOutstanding()).thenReturn(BigDecimal.ONE);
        when(loanForProcessing.getCurrency()).thenReturn(currency);
        when(repaymentInstallment.getTotalOutstanding(currency)).thenReturn(money);
        when(money.isGreaterThanZero()).thenReturn(true);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanRepaymentDueEvent.capture());
        LoanRepaymentScheduleInstallment loanPayloadForEvent = loanRepaymentDueEvent.getValue().get();
        assertEquals(repaymentInstallment, loanPayloadForEvent);
        assertEquals(processedLoan, loanForProcessing);

    }
}
