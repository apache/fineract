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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.data.ExternalTransferRequestParameters;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerInitiateTransferException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class ExternalAssetOwnersWriteServiceTest {

    final LocalDate actualDate = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, actualDate)));
    }

    @Test
    public void testIntermediarySaleLoanByLoanIdHappyPath() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerRepository.findByExternalId(any(ExternalId.class)))
                .thenReturn(Optional.of(testContext.externalAssetOwner));
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);

        // when
        testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command);

        // then
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);

        assertAssertOwnerTransferValues(testContext, externalAssetOwnerTransferArgumentCaptor.getValue(),
                ExternalTransferStatus.PENDING_INTERMEDIATE);
    }

    @Test
    public void testIntermediarySaleLoanByLoanIdDelayedSettlementIsNotEnabled() {
        TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(false);

        // when
        ExternalAssetOwnerInitiateTransferException thrownException = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command));

        // then
        verify(testContext.externalAssetOwnerTransferRepository, times(0)).saveAndFlush(any(ExternalAssetOwnerTransfer.class));
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "Delayed Settlement Configuration is not enabled for the loan product: "
                + testContext.loanDataForExternalTransfer.getLoanProductShortName());
    }

    @ParameterizedTest
    @MethodSource("effectiveTransferDataProviderIntermediarySaleTests")
    public void testValidateEffectiveTransferForIntermediarySale(final String testName,
            final List<ExternalAssetOwnerTransfer> externalAssetOwnerTransferList, final String expectedErrorString) {
        TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(externalAssetOwnerTransferList);

        // when
        ExternalAssetOwnerInitiateTransferException thrownException = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command));

        // then
        verify(testContext.externalAssetOwnerTransferRepository, times(0)).saveAndFlush(any(ExternalAssetOwnerTransfer.class));
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), expectedErrorString);
    }

    @ParameterizedTest
    @MethodSource("loanStatusValidationDataProviderValidActive")
    public void testValidateValidActiveLoanStatus(final String testName, final LoanStatus loanStatus) {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerRepository.findByExternalId(any(ExternalId.class)))
                .thenReturn(Optional.of(testContext.externalAssetOwner));
        when(testContext.loanDataForExternalTransfer.getLoanStatus()).thenReturn(loanStatus);
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);

        // when
        testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command);

        // then
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);

        assertAssertOwnerTransferValues(testContext, externalAssetOwnerTransferArgumentCaptor.getValue(),
                ExternalTransferStatus.PENDING_INTERMEDIATE);
    }

    @ParameterizedTest
    @MethodSource("loanStatusValidationDataProviderInvalidActive")
    public void testValidateInvalidActiveLoanStatus(final String testName, final LoanStatus loanStatus) {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanDataForExternalTransfer.getLoanStatus()).thenReturn(loanStatus);
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));

        // when
        ExternalAssetOwnerInitiateTransferException exception = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command));

        assertEquals(exception.getMessage(), String.format("Loan status %s is not valid for transfer.", loanStatus.name()));

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository, times(1)).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).exists(any(Specification.class));
        verifyNoInteractions(testContext.externalAssetOwnerRepository);
    }

    @ParameterizedTest
    @MethodSource("loanStatusValidationDataProviderValidDelayedSettlement")
    public void testValidateValidDelayedSettlementLoanStatus(final String testName, final LoanStatus loanStatus) {
        final ExternalAssetOwnerTransfer effectiveTransferForDelayedSettlement = new ExternalAssetOwnerTransfer();
        effectiveTransferForDelayedSettlement.setStatus(ExternalTransferStatus.ACTIVE_INTERMEDIATE);
        List<ExternalAssetOwnerTransfer> assetOwnerTransfers = new ArrayList<>();
        assetOwnerTransfers.add(effectiveTransferForDelayedSettlement);
        final TestContext testContext = new TestContext();
        final ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanDataForExternalTransfer.getLoanStatus()).thenReturn(loanStatus);
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(assetOwnerTransfers);

        // when
        CommandProcessingResult result = testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command);

        // then
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.externalAssetOwnerTransferRepository).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);

        ExternalAssetOwnerTransfer savedTransfer = externalAssetOwnerTransferArgumentCaptor.getValue();
        assertAssertOwnerTransferValues(testContext, savedTransfer, ExternalTransferStatus.PENDING);

        assertEquals(savedTransfer.getId(), result.getResourceId());
        assertEquals(savedTransfer.getExternalId(), result.getResourceExternalId());
        assertEquals(savedTransfer.getLoanId(), result.getSubResourceId());
        assertEquals(savedTransfer.getExternalLoanId(), result.getSubResourceExternalId());
    }

    @ParameterizedTest
    @MethodSource("loanStatusValidationDataProviderInvalidDelayedSettlement")
    public void testValidateInvalidDelayedSettlementLoanStatus(final String testName, final LoanStatus loanStatus) {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanDataForExternalTransfer.getLoanStatus()).thenReturn(loanStatus);
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));

        // when
        ExternalAssetOwnerInitiateTransferException exception = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        assertEquals(exception.getMessage(), String.format("Loan status %s is not valid for transfer.", loanStatus.name()));

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository, times(1)).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).exists(any(Specification.class));
        verifyNoInteractions(testContext.externalAssetOwnerRepository);
    }

    @ParameterizedTest
    @MethodSource("effectiveTransferDataProvider")
    public void verifyWhenLoanSaleIsInitiatedThenAssetOwnerTransferIsCreated(
            final List<ExternalAssetOwnerTransfer> externalAssetOwnerTransferList, final boolean isDelayedSettlementEnabled) {
        final TestContext testContext = new TestContext();
        final ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(isDelayedSettlementEnabled);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(externalAssetOwnerTransferList);

        // when
        CommandProcessingResult result = testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command);

        // then
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.externalAssetOwnerTransferRepository).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);

        ExternalAssetOwnerTransfer savedTransfer = externalAssetOwnerTransferArgumentCaptor.getValue();
        assertAssertOwnerTransferValues(testContext, savedTransfer, ExternalTransferStatus.PENDING);

        assertEquals(savedTransfer.getId(), result.getResourceId());
        assertEquals(savedTransfer.getExternalId(), result.getResourceExternalId());
        assertEquals(savedTransfer.getLoanId(), result.getSubResourceId());
        assertEquals(savedTransfer.getExternalLoanId(), result.getSubResourceExternalId());
    }

    @Test
    public void validateSettlementDateInThePastTest() {
        TestContext testContext = new TestContext();
        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommand);
        ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        lenient().when(
                testContext.fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, jsonCommandElement))
                .thenReturn(LocalDate.EPOCH);
        lenient().when(testContext.fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE,
                jsonCommandElement, testContext.DATE_FORMAT, Locale.GERMANY)).thenReturn(LocalDate.EPOCH);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);

        // when
        ExternalAssetOwnerInitiateTransferException thrownException = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.intermediarySaleLoanByLoanId(command));

        // then
        verify(testContext.externalAssetOwnerTransferRepository, times(0)).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository, times(0)).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "Settlement date cannot be in the past");
    }

    private static Stream<Arguments> effectiveTransferDataProvider() {
        final ExternalAssetOwnerTransfer effectiveTransferForDelayedSettlement = new ExternalAssetOwnerTransfer();
        effectiveTransferForDelayedSettlement.setStatus(ExternalTransferStatus.ACTIVE_INTERMEDIATE);

        return Stream.of(Arguments.of(Collections.emptyList(), false), Arguments.of(List.of(effectiveTransferForDelayedSettlement), true));
    }

    private static Stream<Arguments> effectiveTransferDataProviderIntermediarySaleTests() {
        final ExternalAssetOwnerTransfer activeIntermediate = new ExternalAssetOwnerTransfer();
        activeIntermediate.setStatus(ExternalTransferStatus.ACTIVE_INTERMEDIATE);
        final ExternalAssetOwnerTransfer active = new ExternalAssetOwnerTransfer();
        active.setStatus(ExternalTransferStatus.ACTIVE);
        final ExternalAssetOwnerTransfer pendingIntermediate = new ExternalAssetOwnerTransfer();
        pendingIntermediate.setStatus(ExternalTransferStatus.PENDING_INTERMEDIATE);

        return Stream.of(
                Arguments.of("Incorrect State", List.of(activeIntermediate),
                        String.format("This loan cannot be sold, because it is incorrect state! (transferId = %s)",
                                activeIntermediate.getId())),
                Arguments.of("Already In Progress", List.of(activeIntermediate, active),
                        "This loan cannot be sold, there is already an in progress transfer"),
                Arguments.of("Already Pending Intermediary", List.of(pendingIntermediate),
                        "External asset owner transfer is already in PENDING_INTERMEDIATE state for this loan"),
                Arguments.of("Already Owned by External Asset Owner", List.of(active),
                        "This loan cannot be sold, because it is owned by an external asset owner"));
    }

    private static Stream<Arguments> loanStatusValidationDataProviderValidActive() {
        return Stream.of(Arguments.of("Active Loan Status", LoanStatus.ACTIVE),
                Arguments.of("Transfer In Progress Loan Status", LoanStatus.TRANSFER_IN_PROGRESS),
                Arguments.of("Transfer On Hold Loan Status", LoanStatus.TRANSFER_ON_HOLD));
    }

    private static Stream<Arguments> loanStatusValidationDataProviderValidDelayedSettlement() {
        return Stream.of(Arguments.of("Active Loan Status", LoanStatus.ACTIVE),
                Arguments.of("Transfer On Hold Loan Status", LoanStatus.TRANSFER_ON_HOLD),
                Arguments.of("Transfer In Progress Status", LoanStatus.TRANSFER_IN_PROGRESS),
                Arguments.of("Overpaid Loan Status", LoanStatus.OVERPAID),
                Arguments.of("Closed Obligations Met Loan Status", LoanStatus.CLOSED_OBLIGATIONS_MET));
    }

    private static Stream<Arguments> loanStatusValidationDataProviderInvalidActive() {
        return Stream.of(Arguments.of("Invalid Loan Status", LoanStatus.INVALID), Arguments.of("Overpaid Loan Status", LoanStatus.OVERPAID),
                Arguments.of("Approved Loan Status", LoanStatus.APPROVED), Arguments.of("Rejected Loan Status", LoanStatus.REJECTED),
                Arguments.of("Submitted and Pending Approval Loan Status", LoanStatus.SUBMITTED_AND_PENDING_APPROVAL),
                Arguments.of("Withdrawn By Client Loan Status", LoanStatus.WITHDRAWN_BY_CLIENT),
                Arguments.of("Closed Written Off Loan Status", LoanStatus.CLOSED_WRITTEN_OFF),
                Arguments.of("Closed Reschedule Outstanding Amount Loan Status", LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT),
                Arguments.of("Closed Obligations Met Loan Status", LoanStatus.CLOSED_OBLIGATIONS_MET));
    }

    private static Stream<Arguments> loanStatusValidationDataProviderInvalidDelayedSettlement() {
        return Stream.of(Arguments.of("Invalid Loan Status", LoanStatus.INVALID), Arguments.of("Rejected Loan Status", LoanStatus.REJECTED),
                Arguments.of("Approved Loan Status", LoanStatus.APPROVED),
                Arguments.of("Submitted and Pending Approval Loan Status", LoanStatus.SUBMITTED_AND_PENDING_APPROVAL),
                Arguments.of("Withdrawn By Client Loan Status", LoanStatus.WITHDRAWN_BY_CLIENT),
                Arguments.of("Closed Written Off Loan Status", LoanStatus.CLOSED_WRITTEN_OFF),
                Arguments.of("Closed Reschedule Outstanding Amount Loan Status", LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT));
    }

    @Test
    public void verifyWhenLoanSaleIsInitiatedWithoutOwnerThenAssetOwnerTransferIsCreated() {
        final TestContext testContext = new TestContext();
        final ArgumentCaptor<ExternalAssetOwnerTransfer> externalAssetOwnerTransferArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransfer.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(false);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerRepository.findByExternalId(any(ExternalId.class))).thenReturn(Optional.empty());

        // when
        CommandProcessingResult result = testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command);

        // then
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(externalAssetOwnerTransferArgumentCaptor.capture());
        verify(testContext.externalAssetOwnerRepository).saveAndFlush(any(ExternalAssetOwner.class));
        verify(testContext.externalAssetOwnerTransferRepository).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);

        ExternalAssetOwnerTransfer savedTransfer = externalAssetOwnerTransferArgumentCaptor.getValue();
        assertAssertOwnerTransferValues(testContext, savedTransfer, ExternalTransferStatus.PENDING);

        assertEquals(savedTransfer.getId(), result.getResourceId());
        assertEquals(savedTransfer.getExternalId(), result.getResourceExternalId());
        assertEquals(savedTransfer.getLoanId(), result.getSubResourceId());
        assertEquals(savedTransfer.getExternalLoanId(), result.getSubResourceExternalId());
    }

    @Test
    public void verifyWhenLoanIsNotFoundThenExceptionIsThrown() {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId)).thenReturn(Optional.empty());

        // when
        assertThrows(LoanNotFoundException.class, () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        // then
        verify(testContext.fromApiJsonHelper, times(1)).parse(command.json());
        verifyNoMoreInteractions(testContext.loanRepository);
        verifyNoInteractions(testContext.externalAssetOwnerRepository, testContext.externalAssetOwnerTransferRepository,
                testContext.delayedSettlementAttributeService);
    }

    @ParameterizedTest
    @MethodSource("invalidFieldsDataProvider")
    public void verifyWhenFieldValueInvalidThenExceptionIsThrown(final String ownerExternalId, final String transferExternalId,
            final String purchaseRatio, final LocalDate settlementDate) {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);
        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommand);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(false);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, jsonCommandElement))
                .thenReturn(ownerExternalId);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, jsonCommandElement))
                .thenReturn(transferExternalId);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, jsonCommandElement))
                .thenReturn(purchaseRatio);
        when(testContext.fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, jsonCommandElement))
                .thenReturn(settlementDate);

        // when
        assertThrows(PlatformApiDataValidationException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verifyNoMoreInteractions(testContext.loanRepository);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verifyNoInteractions(testContext.externalAssetOwnerRepository, testContext.externalAssetOwnerTransferRepository);
    }

    private static Stream<Arguments> invalidFieldsDataProvider() {
        // ownerExternalId, transferExternalId, purchaseRatio, settlementDate
        return Stream.of(
                // settlement date cannot be null
                Arguments.of("value", "value", "value", null),
                // purchaseRatio cannot be null
                Arguments.of("value", "value", null, LocalDate.now(ZoneId.systemDefault()).plusDays(1)),
                // purchaseRatio length cannot be > 50
                Arguments.of("value", "value", RandomStringUtils.randomAlphanumeric(51), LocalDate.now(ZoneId.systemDefault()).plusDays(1)),
                // transferExternalId length cannot be > 100
                Arguments.of("value", RandomStringUtils.randomAlphanumeric(101), "value",
                        LocalDate.now(ZoneId.systemDefault()).plusDays(1)),
                // ownerExternalId cannot be null
                Arguments.of(null, "value", "value", LocalDate.now(ZoneId.systemDefault()).plusDays(1)),
                // ownerExternalId length cannot be > 100
                Arguments.of(RandomStringUtils.randomAlphanumeric(101), "value", "value",
                        LocalDate.now(ZoneId.systemDefault()).plusDays(1)));
    }

    @Test
    public void verifyWhenTransferExternalIdExistsThenExceptionIsThrown() {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(false);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.exists(any(Specification.class))).thenReturn(true);

        // when
        assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository, times(1)).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).exists(any(Specification.class));
        verifyNoInteractions(testContext.externalAssetOwnerRepository);
    }

    @ParameterizedTest
    @MethodSource("delayedSettlementFlagDataProvider")
    public void verifyWhenTooManyEffectiveTransfersThenExceptionIsThrown(final boolean isDelayedSettlementEnabled) {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(isDelayedSettlementEnabled);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(List.of(new ExternalAssetOwnerTransfer(), new ExternalAssetOwnerTransfer()));

        // when
        ExternalAssetOwnerInitiateTransferException exception = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        assertEquals(exception.getMessage(), "This loan cannot be sold, there is already an in progress transfer");

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository).exists(any(Specification.class));
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
    }

    private static Stream<Arguments> delayedSettlementFlagDataProvider() {
        return Stream.of(Arguments.of(false), Arguments.of(true));
    }

    @Test
    public void verifyWhenNoEffectiveTransfersWithDelayedSettlementThenExceptionIsThrown() {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(Collections.emptyList());

        // when
        ExternalAssetOwnerInitiateTransferException exception = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        assertEquals(exception.getMessage(), "This loan cannot be sold, no effective transfer found.");
        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository).exists(any(Specification.class));
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
    }

    @ParameterizedTest
    @MethodSource("invalidTransferStatusDataProvider")
    public void verifyWhenInvalidTransferStatusThenExceptionIsThrown(final ExternalTransferStatus externalTransferStatus,
            final boolean isDelayedSettlementEnabled, final String expectedExceptionMessage) {
        final TestContext testContext = new TestContext();

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);
        final ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        externalAssetOwnerTransfer.setStatus(externalTransferStatus);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(isDelayedSettlementEnabled);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class))).thenReturn(List.of(externalAssetOwnerTransfer));

        // when
        ExternalAssetOwnerInitiateTransferException exception = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.saleLoanByLoanId(command));

        assertEquals(exception.getMessage(), expectedExceptionMessage);

        // then
        verify(testContext.fromApiJsonHelper, times(2)).parse(command.json());
        verify(testContext.loanRepository).findLoanDataForExternalTransferByLoanId(testContext.loanId);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
        verify(testContext.externalAssetOwnerTransferRepository).exists(any(Specification.class));
        verify(testContext.externalAssetOwnerTransferRepository, times(1)).findEffectiveTransfersOrderByIdDesc(eq(testContext.loanId),
                any(LocalDate.class));
        verify(testContext.externalAssetOwnerRepository).findByExternalId(any(ExternalId.class));
    }

    private static Stream<Arguments> invalidTransferStatusDataProvider() {
        return Stream.of(
                Arguments.of(ExternalTransferStatus.PENDING, false,
                        "External asset owner transfer is already in PENDING state for this loan"),
                Arguments.of(ExternalTransferStatus.ACTIVE, false,
                        "This loan cannot be sold, because it is owned by an external asset owner"),
                Arguments.of(ExternalTransferStatus.PENDING_INTERMEDIATE, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."),
                Arguments.of(ExternalTransferStatus.ACTIVE, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."),
                Arguments.of(ExternalTransferStatus.DECLINED, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."),
                Arguments.of(ExternalTransferStatus.PENDING, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."),
                Arguments.of(ExternalTransferStatus.BUYBACK, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."),
                Arguments.of(ExternalTransferStatus.CANCELLED, true,
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state."));
    }

    @ParameterizedTest
    @MethodSource("buybackValidationWithDelaySettlementSuccessfulDataProvider")
    void buybackLoanByLoanIdWhenDelaySettlementEnabledSuccess(final List<ExternalTransferStatus> transferStatuses,
            final ExternalTransferStatus expectedStatus) {
        // given
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));

        List<ExternalAssetOwnerTransfer> transfers = transferStatuses.stream()
                .map(transferStatus -> createExternalAssetOwnerTransfer(testContext, transferStatus)).toList();
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(testContext.loanId, actualDate))
                .thenReturn(transfers);

        // when
        CommandProcessingResult result = testContext.externalAssetOwnersWriteServiceImpl.buybackLoanByLoanId(command);

        // then
        ArgumentCaptor<ExternalAssetOwnerTransfer> savedTransferCaptor = ArgumentCaptor.forClass(ExternalAssetOwnerTransfer.class);
        verify(testContext.externalAssetOwnerTransferRepository).saveAndFlush(savedTransferCaptor.capture());

        ExternalAssetOwnerTransfer savedTransfer = savedTransferCaptor.getValue();
        assertNotNull(savedTransfer);
        assertFalse(transfers.isEmpty());
        ExternalAssetOwnerTransfer expectedEffectiveTransfer = transfers.get(0);
        assertEquals(testContext.transferExternalId, savedTransfer.getExternalId().getValue());
        assertEquals(expectedEffectiveTransfer.getOwner(), savedTransfer.getOwner());
        assertEquals(expectedStatus, savedTransfer.getStatus());
        assertEquals(expectedEffectiveTransfer.getLoanId(), savedTransfer.getLoanId());
        assertEquals(expectedEffectiveTransfer.getExternalLoanId(), savedTransfer.getExternalLoanId());
        assertEquals(testContext.settlementDate, savedTransfer.getSettlementDate());
        assertEquals(actualDate, savedTransfer.getEffectiveDateFrom());
        assertEquals(FUTURE_DATE_9999_12_31, savedTransfer.getEffectiveDateTo());
        assertEquals(expectedEffectiveTransfer.getPurchasePriceRatio(), savedTransfer.getPurchasePriceRatio());

        assertEquals(savedTransfer.getId(), result.getResourceId());
        assertEquals(savedTransfer.getExternalId(), result.getResourceExternalId());
        assertEquals(savedTransfer.getLoanId(), result.getSubResourceId());
        assertEquals(savedTransfer.getExternalLoanId(), result.getSubResourceExternalId());

        verifyNoInteractions(testContext.externalAssetOwnerRepository);
    }

    private static Stream<Arguments> buybackValidationWithDelaySettlementSuccessfulDataProvider() {
        return Stream.of(Arguments.of(List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE), ExternalTransferStatus.BUYBACK_INTERMEDIATE),
                Arguments.of(List.of(ExternalTransferStatus.ACTIVE), ExternalTransferStatus.BUYBACK));
    }

    @ParameterizedTest
    @MethodSource("buybackValidationWithDelaySettlementFailureDataProvider")
    void buybackLoanByLoanIdWhenDelaySettlementEnabledFailure(final List<ExternalTransferStatus> transferStatuses,
            final String expectedExceptionMessage) {
        // given
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(testContext.jsonCommand, testContext.loanId);

        when(testContext.delayedSettlementAttributeService.isEnabled(testContext.loanProductId)).thenReturn(true);
        when(testContext.loanRepository.findLoanDataForExternalTransferByLoanId(testContext.loanId))
                .thenReturn(Optional.of(testContext.loanDataForExternalTransfer));

        List<ExternalAssetOwnerTransfer> transfers = transferStatuses.stream()
                .map(transferStatus -> createExternalAssetOwnerTransfer(testContext, transferStatus)).toList();
        when(testContext.externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(testContext.loanId, actualDate))
                .thenReturn(transfers);

        // when
        ExternalAssetOwnerInitiateTransferException actualException = assertThrows(ExternalAssetOwnerInitiateTransferException.class,
                () -> testContext.externalAssetOwnersWriteServiceImpl.buybackLoanByLoanId(command));

        // then
        assertEquals(expectedExceptionMessage, actualException.getMessage());
        verify(testContext.externalAssetOwnerTransferRepository, never()).saveAndFlush(any());
        verifyNoInteractions(testContext.externalAssetOwnerRepository);
        verify(testContext.delayedSettlementAttributeService).isEnabled(testContext.loanProductId);
    }

    private static Stream<Arguments> buybackValidationWithDelaySettlementFailureDataProvider() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), "This loan cannot be bought back, it is not owned by an external asset owner"),
                Arguments.of(List.of(ExternalTransferStatus.PENDING_INTERMEDIATE),
                        "This loan cannot be bought back, effective transfer is not in right state: PENDING_INTERMEDIATE"),
                Arguments.of(List.of(ExternalTransferStatus.PENDING),
                        "This loan cannot be bought back, effective transfer is not in right state: PENDING"),
                Arguments.of(List.of(ExternalTransferStatus.DECLINED),
                        "This loan cannot be bought back, effective transfer is not in right state: DECLINED"),
                Arguments.of(List.of(ExternalTransferStatus.BUYBACK),
                        "This loan cannot be bought back, effective transfer is not in right state: BUYBACK"),
                Arguments.of(List.of(ExternalTransferStatus.BUYBACK_INTERMEDIATE),
                        "This loan cannot be bought back, effective transfer is not in right state: BUYBACK_INTERMEDIATE"),
                Arguments.of(List.of(ExternalTransferStatus.CANCELLED),
                        "This loan cannot be bought back, effective transfer is not in right state: CANCELLED"),
                Arguments.of(List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.PENDING),
                        "This loan cannot be bought back, external asset owner sale is pending"),
                Arguments.of(List.of(ExternalTransferStatus.PENDING, ExternalTransferStatus.ACTIVE_INTERMEDIATE),
                        "This loan cannot be bought back, external asset owner sale is pending"),
                Arguments.of(List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.BUYBACK_INTERMEDIATE),
                        "This loan cannot be bought back, external asset owner buyback transfer is already in progress"),
                Arguments.of(List.of(ExternalTransferStatus.BUYBACK_INTERMEDIATE, ExternalTransferStatus.ACTIVE_INTERMEDIATE),
                        "This loan cannot be bought back, external asset owner buyback transfer is already in progress"),
                Arguments.of(List.of(ExternalTransferStatus.ACTIVE, ExternalTransferStatus.BUYBACK),
                        "This loan cannot be bought back, external asset owner buyback transfer is already in progress"),
                Arguments.of(List.of(ExternalTransferStatus.BUYBACK, ExternalTransferStatus.ACTIVE),
                        "This loan cannot be bought back, external asset owner buyback transfer is already in progress"));
    }

    /**
     * Helper method to create {@link JsonCommand} object from json command string.
     *
     * @param jsonCommand
     *            the json command string
     * @param loanId
     *            the loan id
     * @return the {@link JsonCommand} object.
     */
    private JsonCommand createJsonCommand(final String jsonCommand, final Long loanId) {
        return new JsonCommand(null, jsonCommand, null, null, null, null, null, null, null, loanId, null, null, null, null, null, null,
                null, null);
    }

    /**
     * Helper method to create {@link ExternalAssetOwnerTransfer} object.
     *
     * @param status
     *            the {@link ExternalTransferStatus}
     * @return the {@link ExternalAssetOwnerTransfer} object.
     */
    private ExternalAssetOwnerTransfer createExternalAssetOwnerTransfer(final TestContext testContext,
            final ExternalTransferStatus status) {
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setExternalId(new ExternalId(RandomStringUtils.randomAlphanumeric(10)));
        transfer.setOwner(new ExternalAssetOwner());
        transfer.setStatus(status);
        transfer.setLoanId(testContext.loanId);
        transfer.setExternalLoanId(new ExternalId(testContext.externalLoanId));
        transfer.setExternalGroupId(new ExternalId(testContext.externalLoanId));
        transfer.setPurchasePriceRatio(TestContext.PURCHASE_RATIO.toString());

        return transfer;
    }

    /**
     * Asserts on the {@link ExternalAssetOwnerTransfer} object values against test values.
     *
     * @param testContext
     *            the test context with expected values.
     * @param externalAssetOwnerTransfer
     *            the {@link ExternalAssetOwnerTransfer} object.
     * @param expectedTransferStatus
     *            the expected transfer status.
     */
    private void assertAssertOwnerTransferValues(final TestContext testContext, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer,
            final ExternalTransferStatus expectedTransferStatus) {
        assertEquals(testContext.loanId, externalAssetOwnerTransfer.getLoanId());
        assertEquals(testContext.externalLoanId, externalAssetOwnerTransfer.getExternalLoanId().getValue());
        assertEquals(testContext.ownerExternalId, externalAssetOwnerTransfer.getOwner().getExternalId().getValue());
        assertEquals(testContext.transferExternalId, externalAssetOwnerTransfer.getExternalId().getValue());
        assertEquals(testContext.transferExternalGroupId, externalAssetOwnerTransfer.getExternalGroupId().getValue());
        assertEquals(expectedTransferStatus, externalAssetOwnerTransfer.getStatus());
        assertEquals(TestContext.PURCHASE_RATIO.toString(), externalAssetOwnerTransfer.getPurchasePriceRatio());
        assertEquals(testContext.settlementDate, externalAssetOwnerTransfer.getSettlementDate());
        assertEquals(actualDate, externalAssetOwnerTransfer.getEffectiveDateFrom());
        assertEquals(FUTURE_DATE_9999_12_31, externalAssetOwnerTransfer.getEffectiveDateTo());
    }

    @SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
    static class TestContext {

        @Mock
        private ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

        @Mock
        private ExternalAssetOwnerRepository externalAssetOwnerRepository;

        @Mock
        private FromJsonHelper fromApiJsonHelper;

        @Mock
        private LoanRepository loanRepository;

        @Mock
        private DelayedSettlementAttributeService delayedSettlementAttributeService;

        @Mock
        private LoanDataForExternalTransfer loanDataForExternalTransfer;

        @Mock
        private ConfigurationDomainService configurationDomainService;

        @Mock
        private ExternalAssetOwnersReadService externalAssetOwnersReadService;

        @InjectMocks
        private ExternalAssetOwnersWriteServiceImpl externalAssetOwnersWriteServiceImpl;

        private static final BigDecimal PURCHASE_RATIO = BigDecimal.valueOf(Float.parseFloat(RandomStringUtils.randomNumeric(1, 3)) / 100)
                .setScale(2, RoundingMode.HALF_UP);
        private static final String DATE_FORMAT = "yyyy-MM-dd";
        private static final String LOCALE = "de_DE";

        private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
        private final ExternalAssetOwner externalAssetOwner = new ExternalAssetOwner();
        private final Long loanId = Long.valueOf(RandomStringUtils.randomNumeric(2));
        private final String externalLoanId = RandomStringUtils.randomAlphanumeric(10);
        private final Long loanProductId = Long.valueOf(RandomStringUtils.randomNumeric(2));
        private final String loanProductShortName = RandomStringUtils.randomAlphanumeric(10);
        private final String ownerExternalId = RandomStringUtils.randomAlphanumeric(10);
        private final String transferExternalId = RandomStringUtils.randomAlphanumeric(10);
        private final String transferExternalGroupId = RandomStringUtils.randomAlphanumeric(10);
        private final LocalDate settlementDate = LocalDate.parse("9999-08-22");
        private final String jsonCommand = String.format("""
                {
                    "settlementDate": "%s",
                    "ownerExternalId": "%s",
                    "transferExternalId": "%s",
                    "transferExternalGroupId": "%s",
                    "purchasePriceRatio": "%s",
                    "dateFormat": "%s",
                    "locale": "%s"
                }
                """, settlementDate, ownerExternalId, transferExternalId, transferExternalGroupId, PURCHASE_RATIO, DATE_FORMAT, LOCALE);

        @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
        TestContext() {
            MockitoAnnotations.openMocks(this);
            final JsonElement jsonCommandElement = fromJsonHelper.parse(jsonCommand);
            externalAssetOwner.setExternalId(new ExternalId(ownerExternalId));

            lenient().when(fromApiJsonHelper.parse(anyString())).thenReturn(jsonCommandElement);
            lenient().when(fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, jsonCommandElement))
                    .thenReturn(ownerExternalId);
            lenient().when(fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, jsonCommandElement))
                    .thenReturn(transferExternalId);
            lenient().when(
                    fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID, jsonCommandElement))
                    .thenReturn(transferExternalGroupId);
            lenient().when(fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, jsonCommandElement))
                    .thenReturn(PURCHASE_RATIO.toString());
            lenient().when(fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.DATEFORMAT, jsonCommandElement))
                    .thenReturn(DATE_FORMAT);
            lenient().when(fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.LOCALE, jsonCommandElement))
                    .thenReturn(LOCALE);
            lenient().when(fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, jsonCommandElement))
                    .thenReturn(settlementDate);
            lenient().when(fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, jsonCommandElement,
                    DATE_FORMAT, Locale.GERMANY)).thenReturn(settlementDate);

            lenient().when(externalAssetOwnerRepository.findByExternalId(any(ExternalId.class)))
                    .thenReturn(Optional.of(externalAssetOwner));
            lenient().when(externalAssetOwnerTransferRepository.findEffectiveTransfersOrderByIdDesc(eq(loanId), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            lenient().when(externalAssetOwnerRepository.saveAndFlush(any(ExternalAssetOwner.class))).thenReturn(externalAssetOwner);

            lenient().when(loanDataForExternalTransfer.getId()).thenReturn(loanId);
            lenient().when(loanDataForExternalTransfer.getExternalId()).thenReturn(new ExternalId(externalLoanId));
            lenient().when(loanDataForExternalTransfer.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
            lenient().when(loanDataForExternalTransfer.getLoanProductId()).thenReturn(loanProductId);
            lenient().when(loanDataForExternalTransfer.getLoanProductShortName()).thenReturn(loanProductShortName);
            lenient().when(configurationDomainService.getAllowedLoanStatusesForExternalAssetTransfer())
                    .thenReturn(List.of("ACTIVE", "TRANSFER_IN_PROGRESS", "TRANSFER_ON_HOLD"));
            lenient().when(configurationDomainService.getAllowedLoanStatusesOfDelayedSettlementForExternalAssetTransfer())
                    .thenReturn(List.of("ACTIVE", "TRANSFER_IN_PROGRESS", "TRANSFER_ON_HOLD", "OVERPAID", "CLOSED_OBLIGATIONS_MET"));
            lenient().when(externalAssetOwnersReadService.retrieveActiveTransferData(any(Long.class), any(), any())).thenReturn(null);

        }
    }
}
