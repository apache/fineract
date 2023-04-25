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
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentOverdueBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckLoanRepaymentOverdueBusinessStepTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    private CheckLoanRepaymentOverdueBusinessStep underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new CheckLoanRepaymentOverdueBusinessStep(configurationDomainService, businessEventNotifierService);
    }

    @Test
    public void givenLoanWithInstallmentOverdueAfterConfiguredDaysWhenStepExecutionThenBusinessEventIsRaised() {
        ArgumentCaptor<LoanRepaymentOverdueBusinessEvent> loanRepaymentDueBusinessEventArgumentCaptor = ArgumentCaptor
                .forClass(LoanRepaymentOverdueBusinessEvent.class);
        // given
        when(configurationDomainService.retrieveRepaymentOverdueDays()).thenReturn(1L);
        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().minusDays(1);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanRepaymentScheduleInstallment repaymentInstallment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1,
                LocalDate.now(ZoneId.systemDefault()), loanInstallmentRepaymentDueDate, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays.asList(repaymentInstallment);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getOverDueDaysForRepaymentEvent()).thenReturn(null);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanRepaymentDueBusinessEventArgumentCaptor.capture());
        LoanRepaymentScheduleInstallment loanPayloadForEvent = loanRepaymentDueBusinessEventArgumentCaptor.getValue().get();
        assertEquals(repaymentInstallment, loanPayloadForEvent);
        assertEquals(processedLoan, loanForProcessing);
    }

    @Test
    public void givenLoanWithNoInstallmentOverdueAfterConfiguredDaysWhenStepExecutionThenNoBusinessEventIsRaised() {
        // given
        when(configurationDomainService.retrieveRepaymentOverdueDays()).thenReturn(1L);
        LocalDate loanInstallmentRepaymentDueDateBefore5Days = DateUtils.getBusinessLocalDate().minusDays(5);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays
                .asList(new LoanRepaymentScheduleInstallment(loanForProcessing, 1, LocalDate.now(ZoneId.systemDefault()),
                        loanInstallmentRepaymentDueDateBefore5Days, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                        BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0)));
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getOverDueDaysForRepaymentEvent()).thenReturn(null);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);
        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(processedLoan, loanForProcessing);

    }

    @Test
    public void givenLoanWithInstallmentOverdueAfterConfiguredDaysButInstallmentPaidOffWhenStepExecutionThenNoBusinessEvent() {
        // given
        when(configurationDomainService.retrieveRepaymentOverdueDays()).thenReturn(1L);
        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().minusDays(1);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanRepaymentScheduleInstallment repaymentInstallmentPaidOff = new LoanRepaymentScheduleInstallment(loanForProcessing, 1,
                LocalDate.now(ZoneId.systemDefault()), loanInstallmentRepaymentDueDate, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));

        repaymentInstallmentPaidOff.updateObligationMet(true);

        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays.asList(repaymentInstallmentPaidOff);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getOverDueDaysForRepaymentEvent()).thenReturn(null);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(processedLoan, loanForProcessing);
    }

    @Test
    public void givenLoanWithInstallmentOverdueAfterConfiguredDaysInLoanProductWhenStepExecutionThenBusinessEventIsRaised() {
        ArgumentCaptor<LoanRepaymentOverdueBusinessEvent> loanRepaymentDueBusinessEventArgumentCaptor = ArgumentCaptor
                .forClass(LoanRepaymentOverdueBusinessEvent.class);
        // given
        // global configuration
        when(configurationDomainService.retrieveRepaymentOverdueDays()).thenReturn(2L);
        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().minusDays(1);
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanRepaymentScheduleInstallment repaymentInstallment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1,
                LocalDate.now(ZoneId.systemDefault()), loanInstallmentRepaymentDueDate, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));
        List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = Arrays.asList(repaymentInstallment);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        // product configuration overrides global configuration
        when(loanProduct.getOverDueDaysForRepaymentEvent()).thenReturn(1);
        when(loanForProcessing.getRepaymentScheduleInstallments()).thenReturn(loanRepaymentScheduleInstallments);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanRepaymentDueBusinessEventArgumentCaptor.capture());
        LoanRepaymentScheduleInstallment loanPayloadForEvent = loanRepaymentDueBusinessEventArgumentCaptor.getValue().get();
        assertEquals(repaymentInstallment, loanPayloadForEvent);
        assertEquals(processedLoan, loanForProcessing);
    }

}
