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
package org.apache.fineract.investor.service;

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.BUYBACK;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class LoanAccountOwnerTransferServiceTest {

    @Mock
    private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    @Mock
    private ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    @Mock
    private AccountingService accountingService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    private LoanAccountOwnerTransferService underTest;
    private final LocalDate actualDate = LocalDate.now(ZoneId.systemDefault());

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, actualDate)));
        underTest = new LoanAccountOwnerTransferServiceImpl(externalAssetOwnerTransferRepository,
                externalAssetOwnerTransferLoanMappingRepository, accountingService, businessEventNotifierService);
    }

    @Test
    public void verifyWhenCancelPendingSaleAndBuybackTransferThenBusinessEventsAreSent() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);

        ExternalAssetOwnerTransfer pendingSaleTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer pendingBuybackTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer cancelledSaleTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer cancelledBuybackTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);

        List<ExternalAssetOwnerTransfer> response = List.of(pendingSaleTransfer, pendingBuybackTransfer);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(ASC, "id")))).thenReturn(response);
        when(externalAssetOwnerTransferRepository.save(any(ExternalAssetOwnerTransfer.class))).thenReturn(pendingSaleTransfer)
                .thenReturn(cancelledSaleTransfer).thenReturn(pendingBuybackTransfer).thenReturn(cancelledBuybackTransfer);

        // when
        underTest.handleLoanClosedOrOverpaid(loanForProcessing);

        // then
        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, cancelledSaleTransfer);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing, cancelledBuybackTransfer);
    }

    @Test
    public void verifyWhenDeclinePendingSaleTransferThenBusinessEventIsSent() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);

        ExternalAssetOwnerTransfer pendingSaleTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(pendingSaleTransfer.getStatus()).thenReturn(PENDING);
        ExternalAssetOwnerTransfer declineTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingSaleTransfer);

        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(ASC, "id")))).thenReturn(response);
        when(externalAssetOwnerTransferRepository.save(any(ExternalAssetOwnerTransfer.class))).thenReturn(declineTransfer)
                .thenReturn(pendingSaleTransfer);

        // when
        underTest.handleLoanClosedOrOverpaid(loanForProcessing);

        // then
        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(1);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, declineTransfer);
    }

    @Test
    public void verifyWhenExecutePendingBuybackTransferThenBusinessEventIsSent() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);

        ExternalAssetOwnerTransfer pendingBuybackTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(pendingBuybackTransfer.getStatus()).thenReturn(BUYBACK);
        ExternalAssetOwnerTransfer activeTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingBuybackTransfer);

        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(ASC, "id")))).thenReturn(response);
        when(externalAssetOwnerTransferRepository.findOne(any(Specification.class))).thenReturn(Optional.of(activeTransfer));
        when(externalAssetOwnerTransferRepository.save(any(ExternalAssetOwnerTransfer.class))).thenReturn(activeTransfer)
                .thenReturn(pendingBuybackTransfer);

        // when
        underTest.handleLoanClosedOrOverpaid(loanForProcessing);

        // then
        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, pendingBuybackTransfer);
        verifyLoanAccountSnapshotBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing);
    }

    @NotNull
    private ArgumentCaptor<BusinessEvent<?>> verifyBusinessEvents(int expectedBusinessEvents) {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(expectedBusinessEvents)).notifyPostBusinessEvent(businessEventArgumentCaptor.capture());
        return businessEventArgumentCaptor;
    }

    private void verifyLoanTransferBusinessEvent(ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor, int index, Loan expectedLoan,
            ExternalAssetOwnerTransfer expectedAssetOwnerTransfer) {
        assertTrue(businessEventArgumentCaptor.getAllValues().get(index) instanceof LoanOwnershipTransferBusinessEvent);
        assertEquals(expectedLoan, ((LoanOwnershipTransferBusinessEvent) businessEventArgumentCaptor.getAllValues().get(index)).getLoan());
        assertEquals(expectedAssetOwnerTransfer,
                ((LoanOwnershipTransferBusinessEvent) businessEventArgumentCaptor.getAllValues().get(index)).get());
    }

    private void verifyLoanAccountSnapshotBusinessEvent(ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor, int index,
            Loan expectedLoan) {
        assertTrue(businessEventArgumentCaptor.getAllValues().get(index) instanceof LoanAccountSnapshotBusinessEvent);
        assertEquals(expectedLoan, ((LoanAccountSnapshotBusinessEvent) businessEventArgumentCaptor.getAllValues().get(index)).get());
    }

}
