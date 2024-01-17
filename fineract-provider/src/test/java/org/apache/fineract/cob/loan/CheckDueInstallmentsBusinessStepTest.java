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

import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountCustomSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckDueInstallmentsBusinessStepTest {

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Captor
    private ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor;

    @InjectMocks
    private CheckDueInstallmentsBusinessStep underTest;

    /**
     * Setup context before each test.
     */
    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.parse("2024-01-16"),
                BusinessDateType.COB_DATE, LocalDate.parse("2024-01-15"))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testNoRepaymentScheduleInLoan() {
        // given
        Loan loan = Mockito.mock(Loan.class);

        // when
        underTest.execute(loan);

        // then
        Mockito.verifyNoInteractions(businessEventNotifierService);
    }

    @Test
    public void testInstallmentDueDateIsNotMatchingWithCurrentBusinessDate() {
        // given
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of(Mockito.mock(LoanRepaymentScheduleInstallment.class)));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).getDueDate()).thenReturn(LocalDate.parse("2024-01-17"));

        // when
        underTest.execute(loan);

        // then
        Mockito.verifyNoInteractions(businessEventNotifierService);
    }

    @Test
    public void testSingleInstallmentDueDateIsMatchingWithCurrentBusinessDateAndNotFullyPayed() {
        // given
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of(Mockito.mock(LoanRepaymentScheduleInstallment.class)));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).getDueDate()).thenReturn(LocalDate.parse("2024-01-16"));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).isNotFullyPaidOff()).thenReturn(true);

        // when
        underTest.execute(loan);

        // then
        Mockito.verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(businessEventArgumentCaptor.capture());
        BusinessEvent<?> rawEvent = businessEventArgumentCaptor.getValue();
        Assertions.assertInstanceOf(LoanAccountCustomSnapshotBusinessEvent.class, rawEvent);
        LoanAccountCustomSnapshotBusinessEvent event = (LoanAccountCustomSnapshotBusinessEvent) rawEvent;
        Assertions.assertEquals(loan, event.get());
    }

    @Test
    public void testSingleInstallmentDueDateIsMatchingWithCurrentBusinessDateAndFullyPayed() {
        // given
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of(Mockito.mock(LoanRepaymentScheduleInstallment.class)));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).getDueDate()).thenReturn(LocalDate.parse("2024-01-16"));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).isNotFullyPaidOff()).thenReturn(false);

        // when
        underTest.execute(loan);

        // then
        Mockito.verifyNoInteractions(businessEventNotifierService);
    }

    @Test
    public void testMultipleInstallmentDueDateIsMatchingWithCurrentBusinessDateAndNotFullyPayedButSingleEventIsGenerated() {
        // given
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getRepaymentScheduleInstallments()).thenReturn(
                List.of(Mockito.mock(LoanRepaymentScheduleInstallment.class), Mockito.mock(LoanRepaymentScheduleInstallment.class)));
        // first one is a down payment installment
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).getDueDate()).thenReturn(LocalDate.parse("2024-01-16"));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(0).isNotFullyPaidOff()).thenReturn(true);
        Mockito.lenient().when(loan.getRepaymentScheduleInstallments().get(0).isDownPayment()).thenReturn(true);
        // this one is a real installment
        Mockito.when(loan.getRepaymentScheduleInstallments().get(1).getDueDate()).thenReturn(LocalDate.parse("2024-01-16"));
        Mockito.when(loan.getRepaymentScheduleInstallments().get(1).isNotFullyPaidOff()).thenReturn(true);

        // when
        underTest.execute(loan);

        // then
        Mockito.verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(businessEventArgumentCaptor.capture());
        BusinessEvent<?> rawEvent = businessEventArgumentCaptor.getValue();
        Assertions.assertInstanceOf(LoanAccountCustomSnapshotBusinessEvent.class, rawEvent);
        LoanAccountCustomSnapshotBusinessEvent event = (LoanAccountCustomSnapshotBusinessEvent) rawEvent;
        Assertions.assertEquals(loan, event.get());
    }

}
