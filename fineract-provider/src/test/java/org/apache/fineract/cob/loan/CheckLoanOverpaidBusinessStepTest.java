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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountOverpaidBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CheckLoanOverpaidBusinessStepTest {

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    private CheckLoanOverpaidBusinessStep underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new CheckLoanOverpaidBusinessStep(businessEventNotifierService);
    }

    @Test
    public void givenLoanAccountWithOverpaidStatusAndOverpaidDateEqualToCurrentDateWhenStepExecutionThenRaiseBusinessEvent() {
        ArgumentCaptor<LoanAccountOverpaidBusinessEvent> loanAccountOverpaidBusinessEventCaptor = ArgumentCaptor
                .forClass(LoanAccountOverpaidBusinessEvent.class);
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getStatus()).thenReturn(LoanStatus.OVERPAID);
        when(loanForProcessing.getOverpaidOnDate()).thenReturn(DateUtils.getBusinessLocalDate());

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(loanAccountOverpaidBusinessEventCaptor.capture());
        Loan loanPayloadForEvent = loanAccountOverpaidBusinessEventCaptor.getValue().get();
        assertEquals(loanForProcessing, loanPayloadForEvent);
        assertEquals(processedLoan, loanPayloadForEvent);

    }

    @Test
    public void givenLoanAccountWithOverpaidStatusButOverpaidDateNotEqualToCurrentDateWhenStepExecutionThenNoBusinessEvent() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getStatus()).thenReturn(LoanStatus.OVERPAID);
        when(loanForProcessing.getOverpaidOnDate()).thenReturn(DateUtils.getBusinessLocalDate().minusDays(1));

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(loanForProcessing, processedLoan);

    }

    @Test
    public void givenLoanAccountWithNoOverpaidStatusWhenStepExecutionThenNoBusinessEvent() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getStatus()).thenReturn(LoanStatus.ACTIVE);
        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(loanForProcessing, processedLoan);

    }

    @Test
    public void givenLoanAccountWithOverpaidStatusButOverpaidDateNullWhenStepExecutionThenNoBusinessEvent() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getStatus()).thenReturn(LoanStatus.OVERPAID);
        when(loanForProcessing.getOverpaidOnDate()).thenReturn(null);

        // when
        Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(businessEventNotifierService, times(0)).notifyPostBusinessEvent(any());
        assertEquals(loanForProcessing, processedLoan);

    }

}
