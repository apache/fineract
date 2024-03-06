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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.RoundingMode;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanDownPaymentHandlerServiceImplTest {

    private final MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private LoanTransaction loanTransaction;

    @Mock
    private ScheduleGeneratorDTO scheduleGeneratorDTO;

    @Mock
    private JsonCommand command;

    private LoanDownPaymentHandlerServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService);
        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);
    }

    @AfterEach
    public void reset() {
        moneyHelper.close();
    }

    @Test
    public void testDownPaymentHandler() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanTransaction disbursement = Mockito.mock(LoanTransaction.class);
        MonetaryCurrency loanCurrency = Mockito.mock(MonetaryCurrency.class);
        doNothing().when(businessEventNotifierService).notifyPreBusinessEvent(any(LoanTransactionDownPaymentPreBusinessEvent.class));
        doNothing().when(businessEventNotifierService).notifyPostBusinessEvent(any(LoanTransactionDownPaymentPostBusinessEvent.class));
        doNothing().when(businessEventNotifierService).notifyPostBusinessEvent(any(LoanBalanceChangedBusinessEvent.class));
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenReturn(loanTransaction);
        when(loanForProcessing.handleDownPayment(eq(disbursement), eq(command), eq(scheduleGeneratorDTO))).thenReturn(loanTransaction);
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanCurrency.getCode()).thenReturn("CODE");
        when(loanCurrency.getCurrencyInMultiplesOf()).thenReturn(1);
        when(loanCurrency.getDigitsAfterDecimal()).thenReturn(1);
        // when
        LoanTransaction actual = underTest.handleDownPayment(scheduleGeneratorDTO, command, disbursement, loanForProcessing);

        // then
        assertNotNull(actual);
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPreBusinessEvent(Mockito.any(LoanTransactionDownPaymentPreBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanTransactionDownPaymentPostBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).notifyPostBusinessEvent(Mockito.any(LoanBalanceChangedBusinessEvent.class));
        verify(loanForProcessing, Mockito.times(1)).handleDownPayment(eq(disbursement), eq(command), eq(scheduleGeneratorDTO));
    }

    @Test
    public void testDownPaymentHandlerNoNewTransaction() {
        // given
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanTransaction disbursement = Mockito.mock(LoanTransaction.class);
        MonetaryCurrency loanCurrency = Mockito.mock(MonetaryCurrency.class);
        doNothing().when(businessEventNotifierService).notifyPreBusinessEvent(any(LoanTransactionDownPaymentPreBusinessEvent.class));
        when(loanForProcessing.handleDownPayment(eq(disbursement), eq(command), eq(scheduleGeneratorDTO))).thenReturn(null);
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanCurrency.getCode()).thenReturn("CODE");
        when(loanCurrency.getCurrencyInMultiplesOf()).thenReturn(1);
        when(loanCurrency.getDigitsAfterDecimal()).thenReturn(1);
        // when
        LoanTransaction actual = underTest.handleDownPayment(scheduleGeneratorDTO, command, disbursement, loanForProcessing);

        // then
        assertNull(actual);
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPreBusinessEvent(Mockito.any(LoanTransactionDownPaymentPreBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.never())
                .notifyPostBusinessEvent(Mockito.any(LoanTransactionDownPaymentPostBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.never()).notifyPostBusinessEvent(Mockito.any(LoanBalanceChangedBusinessEvent.class));
        verify(loanForProcessing, Mockito.times(1)).handleDownPayment(eq(disbursement), eq(command), eq(scheduleGeneratorDTO));
        verify(loanTransactionRepository, Mockito.never()).saveAndFlush(any(LoanTransaction.class));
    }
}
