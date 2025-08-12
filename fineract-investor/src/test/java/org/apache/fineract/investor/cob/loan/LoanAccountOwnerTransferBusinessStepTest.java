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
package org.apache.fineract.investor.cob.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.investor.service.DelayedSettlementAttributeService;
import org.apache.fineract.investor.service.ExternalAssetOwnerTransferOutstandingInterestCalculation;
import org.apache.fineract.investor.service.LoanTransferabilityService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.service.LoanJournalEntryPoster;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class LoanAccountOwnerTransferBusinessStepTest {

    public static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private static final Long LOAN_PRODUCT_ID = 2L;
    private final LocalDate actualDate = LocalDate.now(ZoneId.systemDefault());
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    @Mock
    private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

    @Mock
    private ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;

    @Mock
    private LoanJournalEntryPoster loanJournalEntryPoster;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransferabilityService loanTransferabilityService;

    @Mock
    private DelayedSettlementAttributeService delayedSettlementAttributeService;

    @Mock
    private ExternalAssetOwnerTransferOutstandingInterestCalculation externalAssetOwnerTransferOutstandingInterestCalculation;

    private LoanAccountOwnerTransferBusinessStep underTest;

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, actualDate)));
        underTest = new LoanAccountOwnerTransferBusinessStep(externalAssetOwnerTransferRepository,
                externalAssetOwnerTransferLoanMappingRepository, loanJournalEntryPoster, businessEventNotifierService,
                loanTransferabilityService, delayedSettlementAttributeService, externalAssetOwnerTransferOutstandingInterestCalculation);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void givenLoanNoTransfer() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        Long loanId = 1L;
        when(loanForProcessing.getId()).thenReturn(loanId);
        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verifyNoInteractions(businessEventNotifierService, loanTransferabilityService, loanJournalEntryPoster);
        assertEquals(processedLoan, loanForProcessing);
    }

    @Test
    public void givenLoanTwoTransferButInvalidTransfers() {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(false);

        ExternalAssetOwnerTransfer firstResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer secondResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(firstResponseItem.getStatus()).thenReturn(ExternalTransferStatus.PENDING);
        when(secondResponseItem.getStatus()).thenReturn(ExternalTransferStatus.ACTIVE);
        List<ExternalAssetOwnerTransfer> response = List.of(firstResponseItem, secondResponseItem);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);
        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> underTest.execute(loanForProcessing));
        // then
        assertEquals("Illegal transfer found. Expected PENDING and BUYBACK, found: PENDING and ACTIVE", exception.getMessage());
        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verifyNoInteractions(businessEventNotifierService, loanTransferabilityService, loanJournalEntryPoster);
    }

    @Test
    public void givenSameDaySaleAndBuybackWithDelayedSettlement() {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(true);

        ExternalAssetOwnerTransfer firstResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer secondResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(firstResponseItem.getStatus()).thenReturn(ExternalTransferStatus.PENDING);
        when(secondResponseItem.getStatus()).thenReturn(ExternalTransferStatus.BUYBACK);
        List<ExternalAssetOwnerTransfer> response = List.of(firstResponseItem, secondResponseItem);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);
        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> underTest.execute(loanForProcessing));
        // then
        assertEquals("Delayed Settlement enabled, but found 2 transfers of statuses: PENDING and BUYBACK", exception.getMessage());
        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verifyNoInteractions(businessEventNotifierService, loanTransferabilityService, loanJournalEntryPoster);
    }

    @Test
    public void givenLoanTwoTransferSameDay() {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(false);

        ExternalAssetOwnerTransfer firstResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer secondResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);

        ExternalAssetOwnerTransfer firstSaveResult = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer secondSaveResult = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer thirdSaveResult = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer fourthSaveResult = Mockito.mock(ExternalAssetOwnerTransfer.class);

        when(externalAssetOwnerTransferRepository.save(any(ExternalAssetOwnerTransfer.class))).thenReturn(firstSaveResult)
                .thenReturn(secondSaveResult).thenReturn(thirdSaveResult).thenReturn(fourthSaveResult);

        when(firstResponseItem.getStatus()).thenReturn(ExternalTransferStatus.PENDING);
        when(secondResponseItem.getStatus()).thenReturn(ExternalTransferStatus.BUYBACK);
        List<ExternalAssetOwnerTransfer> response = List.of(firstResponseItem, secondResponseItem);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);
        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);
        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verify(firstResponseItem).setEffectiveDateTo(actualDate);
        verify(externalAssetOwnerTransferRepository, times(4)).save(externalAssetOwnerTransferArgumentCaptor.capture());

        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0).getOwner(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getOwner());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0).getExternalId(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getExternalId());
        assertEquals(ExternalTransferStatus.CANCELLED, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getStatus());
        assertEquals(ExternalTransferSubStatus.SAMEDAY_TRANSFERS,
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getSubStatus());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getSettlementDate());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0).getLoanId(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getLoanId());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0).getPurchasePriceRatio(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getPurchasePriceRatio());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getEffectiveDateFrom());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1).getEffectiveDateTo());

        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(2).getOwner(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getOwner());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(2).getExternalId(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getExternalId());
        assertEquals(ExternalTransferStatus.CANCELLED, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getStatus());
        assertEquals(ExternalTransferSubStatus.SAMEDAY_TRANSFERS,
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getSubStatus());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getSettlementDate());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(2).getLoanId(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getLoanId());
        assertEquals(externalAssetOwnerTransferArgumentCaptor.getAllValues().get(2).getPurchasePriceRatio(),
                externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getPurchasePriceRatio());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getEffectiveDateFrom());
        assertEquals(actualDate, externalAssetOwnerTransferArgumentCaptor.getAllValues().get(3).getEffectiveDateTo());

        assertEquals(processedLoan, loanForProcessing);

        verifyNoInteractions(loanTransferabilityService, loanJournalEntryPoster);

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, secondSaveResult);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing, fourthSaveResult);
    }

    private static Stream<Arguments> buybackStatusDataProvider() {
        return Stream.of(Arguments.of(ExternalTransferStatus.BUYBACK_INTERMEDIATE), Arguments.of(ExternalTransferStatus.BUYBACK));
    }

    @ParameterizedTest
    @MethodSource("buybackStatusDataProvider")
    public void givenLoanBuyback(final ExternalTransferStatus buybackStatus) {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);
        ExternalAssetOwnerTransfer firstResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        ExternalAssetOwnerTransfer secondResponseItem = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(firstResponseItem.getStatus()).thenReturn(buybackStatus);
        List<ExternalAssetOwnerTransfer> response = List.of(firstResponseItem);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);
        when(externalAssetOwnerTransferRepository.findOne(any(Specification.class))).thenReturn(Optional.of(secondResponseItem));
        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);
        when(externalAssetOwnerTransferRepository.save(firstResponseItem)).thenReturn(firstResponseItem);
        when(externalAssetOwnerTransferRepository.save(secondResponseItem)).thenReturn(secondResponseItem);
        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verifyNoInteractions(loanTransferabilityService);

        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verify(firstResponseItem).setEffectiveDateTo(actualDate);
        verify(externalAssetOwnerTransferRepository, times(2)).save(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(secondResponseItem).setEffectiveDateTo(actualDate);
        verify(externalAssetOwnerTransferLoanMappingRepository, times(1)).deleteByLoanIdAndOwnerTransfer(1L, secondResponseItem);

        assertEquals(processedLoan, loanForProcessing);

        verify(loanJournalEntryPoster).postJournalEntriesForExternalOwnerTransfer(loanForProcessing, firstResponseItem, null);
        verifyNoMoreInteractions(loanJournalEntryPoster);

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, firstResponseItem);
        verifyLoanAccountSnapshotBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing);
    }

    private static Stream<Arguments> loanSaleTransferableDataProvider() {
        return Stream.of(Arguments.of(false, ExternalTransferStatus.PENDING, ExternalTransferStatus.ACTIVE),
                Arguments.of(true, ExternalTransferStatus.PENDING_INTERMEDIATE, ExternalTransferStatus.ACTIVE_INTERMEDIATE));
    }

    @ParameterizedTest
    @MethodSource("loanSaleTransferableDataProvider")
    public void givenLoanSaleTransferable(final boolean isDelayedSettlementEnabled, final ExternalTransferStatus pendingStatus,
            final ExternalTransferStatus expectedActiveStatus) {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(isDelayedSettlementEnabled);

        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);

        ExternalAssetOwnerTransfer pendingTransfer = new ExternalAssetOwnerTransfer();
        pendingTransfer.setStatus(pendingStatus);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingTransfer);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);

        when(loanTransferabilityService.isTransferable(loanForProcessing, pendingTransfer)).thenReturn(true);

        ExternalAssetOwnerTransfer savedNewTransfer = new ExternalAssetOwnerTransfer();
        savedNewTransfer.setStatus(expectedActiveStatus);
        when(externalAssetOwnerTransferRepository.save(any())).thenReturn(pendingTransfer).thenReturn(savedNewTransfer);

        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(loanTransferabilityService).isTransferable(loanForProcessing, pendingTransfer);
        verifyNoMoreInteractions(loanTransferabilityService);
        verify(externalAssetOwnerTransferRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));

        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);
        verify(externalAssetOwnerTransferRepository, times(2)).save(externalAssetOwnerTransferArgumentCaptor.capture());
        ExternalAssetOwnerTransfer capturedPendingTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0);
        ExternalAssetOwnerTransfer capturedActiveTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1);

        assertEquals(actualDate, capturedPendingTransfer.getEffectiveDateTo());

        assertCommonFieldsOfPendingAndActiveTransfers(capturedPendingTransfer, capturedActiveTransfer);
        assertEquals(expectedActiveStatus, capturedActiveTransfer.getStatus());
        assertEquals(actualDate, capturedActiveTransfer.getSettlementDate());
        assertEquals(actualDate.plusDays(1), capturedActiveTransfer.getEffectiveDateFrom());
        assertEquals(FUTURE_DATE_9999_12_31, capturedActiveTransfer.getEffectiveDateTo());

        ArgumentCaptor<ExternalAssetOwnerTransferLoanMapping> externalAssetOwnerTransferLoanMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferLoanMapping.class);
        verify(externalAssetOwnerTransferLoanMappingRepository).save(externalAssetOwnerTransferLoanMappingArgumentCaptor.capture());
        assertEquals(1L, externalAssetOwnerTransferLoanMappingArgumentCaptor.getValue().getLoanId());
        assertEquals(savedNewTransfer, externalAssetOwnerTransferLoanMappingArgumentCaptor.getValue().getOwnerTransfer());
        assertEquals(processedLoan, loanForProcessing);

        verify(externalAssetOwnerTransferLoanMappingRepository).save(externalAssetOwnerTransferLoanMappingArgumentCaptor.capture());

        verify(loanJournalEntryPoster).postJournalEntriesForExternalOwnerTransfer(loanForProcessing, savedNewTransfer, null);
        verifyNoMoreInteractions(loanJournalEntryPoster);

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, savedNewTransfer);
        verifyLoanAccountSnapshotBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing);
    }

    private static Stream<Arguments> loanSaleNotTransferableDataProvider() {
        return Stream.of(Arguments.of(ExternalTransferStatus.PENDING, ExternalTransferSubStatus.BALANCE_ZERO),
                Arguments.of(ExternalTransferStatus.PENDING, ExternalTransferSubStatus.BALANCE_NEGATIVE),
                Arguments.of(ExternalTransferStatus.PENDING_INTERMEDIATE, ExternalTransferSubStatus.BALANCE_ZERO),
                Arguments.of(ExternalTransferStatus.PENDING_INTERMEDIATE, ExternalTransferSubStatus.BALANCE_NEGATIVE));
    }

    @ParameterizedTest
    @MethodSource("loanSaleNotTransferableDataProvider")
    public void givenLoanSaleNotTransferable(final ExternalTransferStatus pendingStatus,
            final ExternalTransferSubStatus expectedSubStatus) {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);

        ExternalAssetOwnerTransfer pendingTransfer = new ExternalAssetOwnerTransfer();
        pendingTransfer.setStatus(pendingStatus);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingTransfer);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);

        when(loanTransferabilityService.isTransferable(loanForProcessing, pendingTransfer)).thenReturn(false);
        when(loanTransferabilityService.getDeclinedSubStatus(loanForProcessing)).thenReturn(expectedSubStatus);

        ExternalAssetOwnerTransfer savedNewTransfer = Mockito.mock(ExternalAssetOwnerTransfer.class);
        when(savedNewTransfer.getStatus()).thenReturn(ExternalTransferStatus.DECLINED);
        when(externalAssetOwnerTransferRepository.save(any())).thenReturn(pendingTransfer).thenReturn(savedNewTransfer);

        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(loanTransferabilityService).isTransferable(loanForProcessing, pendingTransfer);
        verify(externalAssetOwnerTransferRepository, times(1)).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));

        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);
        verify(externalAssetOwnerTransferRepository, times(2)).save(externalAssetOwnerTransferArgumentCaptor.capture());
        ExternalAssetOwnerTransfer capturedPendingTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0);
        ExternalAssetOwnerTransfer capturedActiveTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1);

        assertEquals(actualDate, capturedPendingTransfer.getEffectiveDateTo());

        assertCommonFieldsOfPendingAndActiveTransfers(capturedPendingTransfer, capturedActiveTransfer);
        assertEquals(ExternalTransferStatus.DECLINED, capturedActiveTransfer.getStatus());
        assertEquals(expectedSubStatus, capturedActiveTransfer.getSubStatus());
        assertEquals(actualDate, capturedActiveTransfer.getSettlementDate());
        assertEquals(actualDate, capturedActiveTransfer.getEffectiveDateFrom());
        assertEquals(actualDate, capturedActiveTransfer.getEffectiveDateTo());
        assertEquals(processedLoan, loanForProcessing);

        verifyNoInteractions(loanJournalEntryPoster);

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(1);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, savedNewTransfer);
    }

    @Test
    public void testSaleLoanWithDelayedSettlementFromIntermediateToInvestor() {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(true);

        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        when(loanForProcessing.getSummary()).thenReturn(loanSummary);

        ExternalAssetOwner previousOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer activeIntermediateTransfer = new ExternalAssetOwnerTransfer();
        activeIntermediateTransfer.setOwner(previousOwner);
        activeIntermediateTransfer.setStatus(ExternalTransferStatus.ACTIVE_INTERMEDIATE);
        when(externalAssetOwnerTransferRepository.findOne(any(Specification.class))).thenReturn(Optional.of(activeIntermediateTransfer));

        ExternalAssetOwnerTransfer pendingTransfer = new ExternalAssetOwnerTransfer();
        pendingTransfer.setStatus(ExternalTransferStatus.PENDING);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingTransfer);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);

        when(loanTransferabilityService.isTransferable(loanForProcessing, pendingTransfer)).thenReturn(true);

        ExternalAssetOwnerTransfer savedNewTransfer = new ExternalAssetOwnerTransfer();
        savedNewTransfer.setStatus(ExternalTransferStatus.ACTIVE);
        when(externalAssetOwnerTransferRepository.save(any())).thenReturn(pendingTransfer).thenReturn(savedNewTransfer);

        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);

        // then
        verify(loanTransferabilityService).isTransferable(loanForProcessing, pendingTransfer);
        verifyNoMoreInteractions(loanTransferabilityService);
        verify(externalAssetOwnerTransferRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verify(externalAssetOwnerTransferRepository).findOne(any(Specification.class));

        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);
        verify(externalAssetOwnerTransferRepository, times(3)).save(externalAssetOwnerTransferArgumentCaptor.capture());
        ExternalAssetOwnerTransfer capturedActiveIntermediateTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(0);
        ExternalAssetOwnerTransfer capturedPendingTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(1);
        ExternalAssetOwnerTransfer capturedActiveTransfer = externalAssetOwnerTransferArgumentCaptor.getAllValues().get(2);

        assertEquals(actualDate, capturedActiveIntermediateTransfer.getEffectiveDateTo());
        assertEquals(actualDate, capturedPendingTransfer.getEffectiveDateTo());

        assertCommonFieldsOfPendingAndActiveTransfers(capturedPendingTransfer, capturedActiveTransfer);
        assertEquals(ExternalTransferStatus.ACTIVE, capturedActiveTransfer.getStatus());
        assertEquals(actualDate, capturedActiveTransfer.getSettlementDate());
        assertEquals(actualDate.plusDays(1), capturedActiveTransfer.getEffectiveDateFrom());
        assertEquals(FUTURE_DATE_9999_12_31, capturedActiveTransfer.getEffectiveDateTo());

        ArgumentCaptor<ExternalAssetOwnerTransferLoanMapping> externalAssetOwnerTransferLoanMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferLoanMapping.class);
        verify(externalAssetOwnerTransferLoanMappingRepository, times(1))
                .save(externalAssetOwnerTransferLoanMappingArgumentCaptor.capture());
        assertEquals(1L, externalAssetOwnerTransferLoanMappingArgumentCaptor.getValue().getLoanId());
        assertEquals(savedNewTransfer, externalAssetOwnerTransferLoanMappingArgumentCaptor.getValue().getOwnerTransfer());
        assertEquals(processedLoan, loanForProcessing);

        verify(loanJournalEntryPoster).postJournalEntriesForExternalOwnerTransfer(loanForProcessing, savedNewTransfer, previousOwner);
        verifyNoMoreInteractions(loanJournalEntryPoster);

        ArgumentCaptor<BusinessEvent<?>> businessEventArgumentCaptor = verifyBusinessEvents(2);
        verifyLoanTransferBusinessEvent(businessEventArgumentCaptor, 0, loanForProcessing, savedNewTransfer);
        verifyLoanAccountSnapshotBusinessEvent(businessEventArgumentCaptor, 1, loanForProcessing);
    }

    @Test
    public void testSaleLoanWithDelayedSettlementFromIntermediateToInvestorActiveIntermediateTransferNotFound() {
        // given
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        when(loanProduct.getId()).thenReturn(LOAN_PRODUCT_ID);

        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(delayedSettlementAttributeService.isEnabled(LOAN_PRODUCT_ID)).thenReturn(true);

        when(externalAssetOwnerTransferRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ExternalAssetOwnerTransfer pendingTransfer = new ExternalAssetOwnerTransfer();
        pendingTransfer.setStatus(ExternalTransferStatus.PENDING);
        List<ExternalAssetOwnerTransfer> response = List.of(pendingTransfer);
        when(externalAssetOwnerTransferRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(response);
        when(loanTransferabilityService.isTransferable(loanForProcessing, pendingTransfer)).thenReturn(true);

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> underTest.execute(loanForProcessing));

        // then
        assertEquals("Expected a effective transfer of ACTIVE_INTERMEDIATE status to be present.", exception.getMessage());

        verify(loanTransferabilityService).isTransferable(loanForProcessing, pendingTransfer);
        verifyNoMoreInteractions(loanTransferabilityService);
        verifyNoInteractions(loanJournalEntryPoster);
        verify(externalAssetOwnerTransferRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.ASC, "id")));
        verify(externalAssetOwnerTransferRepository).findOne(any(Specification.class));
        verify(externalAssetOwnerTransferRepository, never()).save(any(ExternalAssetOwnerTransfer.class));
        verifyNoInteractions(externalAssetOwnerTransferLoanMappingRepository);
        verifyBusinessEvents(0);
    }

    @Test
    public void testGetEnumStyledNameSuccessScenario() {
        final String actualEnumName = underTest.getEnumStyledName();
        assertNotNull(actualEnumName);
        assertEquals("EXTERNAL_ASSET_OWNER_TRANSFER", actualEnumName);
    }

    @Test
    public void testGetHumanReadableNameSuccessScenario() {
        final String actualEnumName = underTest.getHumanReadableName();
        assertNotNull(actualEnumName);
        assertEquals("Execute external asset owner transfer", actualEnumName);
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

    private void assertCommonFieldsOfPendingAndActiveTransfers(final ExternalAssetOwnerTransfer pendingTransfer,
            final ExternalAssetOwnerTransfer activeTransfer) {
        assertEquals(pendingTransfer.getOwner(), activeTransfer.getOwner());
        assertEquals(pendingTransfer.getExternalId(), activeTransfer.getExternalId());
        assertEquals(pendingTransfer.getLoanId(), activeTransfer.getLoanId());
        assertEquals(pendingTransfer.getPurchasePriceRatio(), activeTransfer.getPurchasePriceRatio());
    }
}
