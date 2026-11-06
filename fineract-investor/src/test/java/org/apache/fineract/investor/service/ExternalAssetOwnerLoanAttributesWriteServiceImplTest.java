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

import static org.apache.fineract.investor.service.LoanAccountOwnerTransferServiceImpl.FUTURE_DATE_9999_12_31;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshot;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshotRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.serialization.ExternalAssetOwnerAttributeValidator;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalAssetOwnerLoanAttributesWriteServiceImplTest {

    private static final Long LOAN_ID = 7L;
    private static final Long LOAN_PRODUCT_ID = 3L;
    private static final Long SALE_TRANSFER_ID = 21L;
    private static final String SETTLEMENT_MODEL = "SETTLEMENT_MODEL";
    private static final String EXCLUDED_TRANSACTION_TYPES = "EXCLUDED_TRANSACTION_TYPES";
    private static final String BUY_DOWN_FEE_AMORTIZATION_STRATEGY = "BUY_DOWN_FEE_AMORTIZATION_STRATEGY";
    private static final List<ExternalTransferStatus> SETTLEMENT_MODEL_BOUND_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.PENDING_INTERMEDIATE, ExternalTransferStatus.ACTIVE_INTERMEDIATE,
            ExternalTransferStatus.BUYBACK_INTERMEDIATE);

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Mock
    private ExternalAssetOwnerLoanAttributeSnapshotRepository snapshotRepository;

    @Mock
    private ExternalAssetOwnerLoanProductAttributesReadService loanProductAttributesReadService;

    @Mock
    private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

    @Mock
    private ExternalAssetOwnerAttributeValidator attributeValidator;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private ExternalAssetOwnerLoanAttributesWriteServiceImpl underTest;

    @Test
    void copyIsNotRetakenWhenTheLoanAlreadyHasOne() {
        final Loan loan = loan();
        when(snapshotRepository.existsByLoanId(LOAN_ID)).thenReturn(true);

        underTest.copyLoanProductAttributesIfAbsent(loan, mock(ExternalAssetOwnerTransfer.class));

        verify(snapshotRepository, never()).saveAndFlush(any());
        verifyNoInteractions(loanProductAttributesReadService);
    }

    @Test
    void copyTakesTheFirstProductValuePerKeyAndRecordsTheSaleTransfer() {
        final Loan loan = loan();
        when(loan.productId()).thenReturn(LOAN_PRODUCT_ID);
        when(snapshotRepository.existsByLoanId(LOAN_ID)).thenReturn(false);
        when(loanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(LOAN_PRODUCT_ID, null))
                .thenReturn(new Page<>(List.of(productAttribute(EXCLUDED_TRANSACTION_TYPES, "BUY_DOWN_FEE"),
                        productAttribute(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "IMMEDIATE"),
                        productAttribute(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT")), 3));
        final ArgumentCaptor<ExternalAssetOwnerLoanAttributeSnapshot> snapshotCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanAttributeSnapshot.class);

        underTest.copyLoanProductAttributesIfAbsent(loan, saleTransfer());

        verify(snapshotRepository).saveAndFlush(snapshotCaptor.capture());
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = snapshotCaptor.getValue();
        assertEquals(LOAN_ID, snapshot.getLoanId());
        assertEquals(SALE_TRANSFER_ID, snapshot.getExternalAssetOwnerTransferId());
        assertEquals(Map.of(EXCLUDED_TRANSACTION_TYPES, "BUY_DOWN_FEE", BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "IMMEDIATE"),
                attributesOf(snapshot));
    }

    @Test
    void correctionOfALoanWithoutCopyIsRejected() {
        stubValidation(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT", "REPAYMENT");
        when(snapshotRepository.findByLoanIdForUpdate(LOAN_ID)).thenReturn(Optional.empty());
        when(loanRepository.existsById(LOAN_ID)).thenReturn(true);

        final AbstractPlatformDomainRuleException exception = assertThrows(AbstractPlatformDomainRuleException.class,
                () -> underTest.updateLoanAttribute(command(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT")));

        assertEquals("error.msg.externalAssetOwnerLoanAttribute.noLoanLevelCopy", exception.getGlobalisationMessageCode());
        verify(snapshotRepository, never()).saveAndFlush(any());
    }

    @Test
    void correctionOfAnUnknownLoanIsLoanNotFound() {
        stubValidation(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT", "REPAYMENT");
        when(snapshotRepository.findByLoanIdForUpdate(LOAN_ID)).thenReturn(Optional.empty());
        when(loanRepository.existsById(LOAN_ID)).thenReturn(false);

        assertThrows(LoanNotFoundException.class, () -> underTest.updateLoanAttribute(command(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT")));

        verify(snapshotRepository, never()).saveAndFlush(any());
    }

    @Test
    void settlementModelCorrectionIsRejectedWhileATransferIsInProgressAndTheLockIsTakenBeforeTheCheck() {
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = snapshotWith(SETTLEMENT_MODEL, "DEFAULT_SETTLEMENT");
        stubValidation(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT", "DELAYED_SETTLEMENT");
        when(snapshotRepository.findByLoanIdForUpdate(LOAN_ID)).thenReturn(Optional.of(snapshot));
        when(externalAssetOwnerTransferRepository.findAllForUpdateByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                SETTLEMENT_MODEL_BOUND_STATUSES, FUTURE_DATE_9999_12_31)).thenReturn(List.of(mock(ExternalAssetOwnerTransfer.class)));

        final AbstractPlatformDomainRuleException exception = assertThrows(AbstractPlatformDomainRuleException.class,
                () -> underTest.updateLoanAttribute(command(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT")));

        assertEquals("error.msg.externalAssetOwnerLoanAttribute.settlementModelTransferInProgress",
                exception.getGlobalisationMessageCode());
        final InOrder inOrder = inOrder(snapshotRepository, externalAssetOwnerTransferRepository);
        inOrder.verify(snapshotRepository).findByLoanIdForUpdate(LOAN_ID);
        inOrder.verify(externalAssetOwnerTransferRepository).findAllForUpdateByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                SETTLEMENT_MODEL_BOUND_STATUSES, FUTURE_DATE_9999_12_31);
        verify(snapshotRepository, never()).saveAndFlush(any());
        assertEquals(Map.of(SETTLEMENT_MODEL, "DEFAULT_SETTLEMENT"), attributesOf(snapshot));
    }

    @Test
    void settlementModelCorrectionWithoutTransferInProgressIsStoredAfterTheLockAndTheCheck() {
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = snapshotWith(SETTLEMENT_MODEL, "DEFAULT_SETTLEMENT");
        stubValidation(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT", "DELAYED_SETTLEMENT");
        when(snapshotRepository.findByLoanIdForUpdate(LOAN_ID)).thenReturn(Optional.of(snapshot));
        when(externalAssetOwnerTransferRepository.findAllForUpdateByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                SETTLEMENT_MODEL_BOUND_STATUSES, FUTURE_DATE_9999_12_31)).thenReturn(List.of());

        final CommandProcessingResult result = underTest.updateLoanAttribute(command(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT"));

        final InOrder inOrder = inOrder(snapshotRepository, externalAssetOwnerTransferRepository);
        inOrder.verify(snapshotRepository).findByLoanIdForUpdate(LOAN_ID);
        inOrder.verify(externalAssetOwnerTransferRepository).findAllForUpdateByLoanIdAndStatusInAndEffectiveDateTo(LOAN_ID,
                SETTLEMENT_MODEL_BOUND_STATUSES, FUTURE_DATE_9999_12_31);
        inOrder.verify(snapshotRepository).saveAndFlush(snapshot);
        assertEquals(Map.of(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT"), attributesOf(snapshot));
        assertEquals(LOAN_ID, result.getResourceId());
        assertEquals(LOAN_ID, result.getLoanId());
    }

    @Test
    void otherKeyIsCorrectedWithoutCheckingTransfersAndAbsentKeyIsAdded() {
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = snapshotWith(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT");
        stubValidation(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "immediate", "IMMEDIATE");
        when(snapshotRepository.findByLoanIdForUpdate(LOAN_ID)).thenReturn(Optional.of(snapshot));

        underTest.updateLoanAttribute(command(BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "immediate"));

        verifyNoInteractions(externalAssetOwnerTransferRepository);
        verify(snapshotRepository).saveAndFlush(snapshot);
        assertEquals(Map.of(SETTLEMENT_MODEL, "DELAYED_SETTLEMENT", BUY_DOWN_FEE_AMORTIZATION_STRATEGY, "IMMEDIATE"),
                attributesOf(snapshot));
    }

    @Test
    void validationRunsBeforeTheLock() {
        final String body = body(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT");
        when(attributeValidator.validateAndNormalize(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT"))
                .thenThrow(new IllegalArgumentException("invalid"));

        assertThrows(IllegalArgumentException.class, () -> underTest.updateLoanAttribute(command(EXCLUDED_TRANSACTION_TYPES, "REPAYMENT")));

        verify(attributeValidator).validateRequest(body, EXCLUDED_TRANSACTION_TYPES, "REPAYMENT");
        verifyNoInteractions(snapshotRepository, externalAssetOwnerTransferRepository, loanRepository);
    }

    @Test
    void lockTakesThePessimisticLockOnTheLoanLevelCopy() {
        underTest.lockLoanLevelCopy(LOAN_ID);

        verify(snapshotRepository).findByLoanIdForUpdate(LOAN_ID);
    }

    private void stubValidation(final String attributeKey, final String attributeValue, final String normalizedValue) {
        when(attributeValidator.validateAndNormalize(attributeKey, attributeValue)).thenReturn(normalizedValue);
    }

    private JsonCommand command(final String attributeKey, final String attributeValue) {
        final String body = body(attributeKey, attributeValue);
        return JsonCommand.from(body, fromJsonHelper.parse(body), fromJsonHelper, null, null, null, null, null, LOAN_ID, null, null, null,
                null, null, null, null, null);
    }

    private static String body(final String attributeKey, final String attributeValue) {
        return "{\"attributeKey\":\"" + attributeKey + "\",\"attributeValue\":\"" + attributeValue + "\"}";
    }

    private static Loan loan() {
        final Loan loan = mock(Loan.class);
        when(loan.getId()).thenReturn(LOAN_ID);
        return loan;
    }

    private static ExternalAssetOwnerTransfer saleTransfer() {
        final ExternalAssetOwnerTransfer transfer = mock(ExternalAssetOwnerTransfer.class);
        when(transfer.getId()).thenReturn(SALE_TRANSFER_ID);
        return transfer;
    }

    private static ExternalTransferLoanProductAttributesData productAttribute(final String attributeKey, final String attributeValue) {
        final ExternalTransferLoanProductAttributesData data = new ExternalTransferLoanProductAttributesData();
        data.setAttributeKey(attributeKey);
        data.setAttributeValue(attributeValue);
        return data;
    }

    private static ExternalAssetOwnerLoanAttributeSnapshot snapshotWith(final String attributeKey, final String attributeValue) {
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = new ExternalAssetOwnerLoanAttributeSnapshot(LOAN_ID, SALE_TRANSFER_ID);
        snapshot.putAttribute(attributeKey, attributeValue);
        return snapshot;
    }

    private static Map<String, String> attributesOf(final ExternalAssetOwnerLoanAttributeSnapshot snapshot) {
        return snapshot.getAttributes().stream().collect(
                Collectors.toMap(ExternalAssetOwnerLoanAttributes::getAttributeKey, ExternalAssetOwnerLoanAttributes::getAttributeValue));
    }
}
