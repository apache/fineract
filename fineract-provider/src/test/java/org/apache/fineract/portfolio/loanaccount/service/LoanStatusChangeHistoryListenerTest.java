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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusChangeHistory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusChangeHistoryRepository;
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
public class LoanStatusChangeHistoryListenerTest {

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private LoanStatusChangeHistoryRepository loanStatusChangeHistoryRepository;
    @Mock
    private FineractProperties fineractProperties;

    @Captor
    private ArgumentCaptor<Class<LoanStatusChangedBusinessEvent>> classArgumentCaptor;

    @Captor
    private ArgumentCaptor<BusinessEventListener<LoanStatusChangedBusinessEvent>> listenerCaptor;

    @Captor
    private ArgumentCaptor<LoanStatusChangeHistory> loanStatusChangeHistoryArgumentCaptor;

    @InjectMocks
    private LoanStatusChangeHistoryListener underTest;

    private final LocalDate actualDate = LocalDate.now(ZoneId.systemDefault());

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, actualDate)));
    }

    @Test
    public void testGetLoanStatusesParseOK() {
        Assertions.assertEquals(Set.of(), underTest.getLoanStatuses("NONE"));
        Assertions.assertEquals(Arrays.stream(LoanStatus.values()).collect(Collectors.toSet()), underTest.getLoanStatuses("ALL"));
        Assertions.assertEquals(Set.of(LoanStatus.OVERPAID, LoanStatus.REJECTED, LoanStatus.CLOSED_OBLIGATIONS_MET),
                underTest.getLoanStatuses("OVERPAID,REJECTED,CLOSED_OBLIGATIONS_MET"));
        Assertions.assertEquals(Set.of(LoanStatus.OVERPAID, LoanStatus.REJECTED, LoanStatus.CLOSED_OBLIGATIONS_MET),
                underTest.getLoanStatuses(" OVERPAID,  REJECTED ,CLOSED_OBLIGATIONS_MET   "));
    }

    @Test
    public void testGetLoanStatusesParseNOK() {
        RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> underTest.getLoanStatuses("MISSING"));
        Assertions.assertEquals("Invalid loan status: MISSING", runtimeException.getMessage());

        runtimeException = Assertions.assertThrows(RuntimeException.class, () -> underTest.getLoanStatuses("OVERPAID,MISSING"));
        Assertions.assertEquals("Invalid loan status: MISSING", runtimeException.getMessage());

        runtimeException = Assertions.assertThrows(RuntimeException.class, () -> underTest.getLoanStatuses("ACTIVE,ALL"));
        Assertions.assertEquals("Invalid loan status: ALL", runtimeException.getMessage());

        runtimeException = Assertions.assertThrows(RuntimeException.class, () -> underTest.getLoanStatuses("APPROVED,NONE"));
        Assertions.assertEquals("Invalid loan status: NONE", runtimeException.getMessage());
    }

    @Test
    public void testEventListenerShouldNotBeRegisteredWhenNONE() {
        // given
        FineractProperties.FineractLoanProperties loanProperties = Mockito.mock(FineractProperties.FineractLoanProperties.class);
        Mockito.when(fineractProperties.getLoan()).thenReturn(loanProperties);
        Mockito.when(loanProperties.getStatusChangeHistoryStatuses()).thenReturn("NONE");

        // when
        underTest.addListeners();

        // then
        Mockito.verifyNoInteractions(businessEventNotifierService);
    }

    @Test
    public void testEventListenerShouldBeRegisteredWhenAll() {
        // given
        FineractProperties.FineractLoanProperties loanProperties = Mockito.mock(FineractProperties.FineractLoanProperties.class);
        Mockito.when(fineractProperties.getLoan()).thenReturn(loanProperties);
        Mockito.when(loanProperties.getStatusChangeHistoryStatuses()).thenReturn("ALL");

        // when
        underTest.addListeners();

        // then
        Mockito.verify(businessEventNotifierService, Mockito.times(1)).addPostBusinessEventListener(classArgumentCaptor.capture(),
                listenerCaptor.capture());
        Mockito.verifyNoMoreInteractions(businessEventNotifierService);
        Assertions.assertNotNull(listenerCaptor.getValue());
    }

    @Test
    public void testEventListenerShouldBeRegisteredWhenValidStatusesAreProvided() {
        // given
        FineractProperties.FineractLoanProperties loanProperties = Mockito.mock(FineractProperties.FineractLoanProperties.class);
        Mockito.when(fineractProperties.getLoan()).thenReturn(loanProperties);
        Mockito.when(loanProperties.getStatusChangeHistoryStatuses()).thenReturn("ACTIVE, REJECTED");

        // when
        underTest.addListeners();

        // then
        Mockito.verify(businessEventNotifierService, Mockito.times(1)).addPostBusinessEventListener(classArgumentCaptor.capture(),
                listenerCaptor.capture());
        Mockito.verifyNoMoreInteractions(businessEventNotifierService);
        Assertions.assertNotNull(listenerCaptor.getValue());
    }

    @Test
    public void testHistoryIsSavedWhenLoansStateIsConfigured() {
        // given
        FineractProperties.FineractLoanProperties loanProperties = Mockito.mock(FineractProperties.FineractLoanProperties.class);
        Mockito.when(fineractProperties.getLoan()).thenReturn(loanProperties);
        Mockito.when(loanProperties.getStatusChangeHistoryStatuses()).thenReturn("ACTIVE, REJECTED");
        underTest.addListeners();
        Mockito.verify(businessEventNotifierService, Mockito.times(1)).addPostBusinessEventListener(classArgumentCaptor.capture(),
                listenerCaptor.capture());
        Mockito.verifyNoMoreInteractions(businessEventNotifierService);
        BusinessEventListener<LoanStatusChangedBusinessEvent> listener = listenerCaptor.getValue();

        LoanStatusChangedBusinessEvent mockEvent = Mockito.mock(LoanStatusChangedBusinessEvent.class);
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getId()).thenReturn(123L);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        Mockito.when(mockEvent.get()).thenReturn(loan);

        // when
        listener.onBusinessEvent(mockEvent);

        // then
        Mockito.verify(loanStatusChangeHistoryRepository, Mockito.times(1)).saveAndFlush(loanStatusChangeHistoryArgumentCaptor.capture());
        Mockito.verifyNoMoreInteractions(loanStatusChangeHistoryRepository);
        LoanStatusChangeHistory loanStatusChangeHistory = loanStatusChangeHistoryArgumentCaptor.getValue();
        Assertions.assertEquals(loan, loanStatusChangeHistory.getLoan());
        Assertions.assertEquals(LoanStatus.ACTIVE, loanStatusChangeHistory.getStatus());
        Assertions.assertEquals(actualDate, loanStatusChangeHistory.getBusinessDate());
    }

    @Test
    public void testHistoryIsNotSavedWhenLoansStateIsNotConfigured() {
        // given
        FineractProperties.FineractLoanProperties loanProperties = Mockito.mock(FineractProperties.FineractLoanProperties.class);
        Mockito.when(fineractProperties.getLoan()).thenReturn(loanProperties);
        Mockito.when(loanProperties.getStatusChangeHistoryStatuses()).thenReturn("ACTIVE, REJECTED");
        underTest.addListeners();
        Mockito.verify(businessEventNotifierService, Mockito.times(1)).addPostBusinessEventListener(classArgumentCaptor.capture(),
                listenerCaptor.capture());
        Mockito.verifyNoMoreInteractions(businessEventNotifierService);
        BusinessEventListener<LoanStatusChangedBusinessEvent> listener = listenerCaptor.getValue();

        LoanStatusChangedBusinessEvent mockEvent = Mockito.mock(LoanStatusChangedBusinessEvent.class);
        Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getId()).thenReturn(123L);
        Mockito.when(loan.getStatus()).thenReturn(LoanStatus.OVERPAID);
        Mockito.when(mockEvent.get()).thenReturn(loan);

        // when
        listener.onBusinessEvent(mockEvent);

        // then
        Mockito.verifyNoInteractions(loanStatusChangeHistoryRepository);
    }

}
