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
package org.apache.fineract.portfolio.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionDeleteRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdateRequest;
import org.apache.fineract.portfolio.account.data.response.StandingInstructionUpdateResponse;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.exception.StandingInstructionNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the change tracking and the post-update dependency validation of
 * {@link StandingInstructionWriteServiceImpl#update(StandingInstructionUpdateRequest)}, which used to live in the
 * entity's {@code update(JsonCommand)}, and the parts of create and delete that the legacy assembler and service did.
 */
class StandingInstructionWriteServiceImplTest {

    private static final long INSTRUCTION_ID = 7L;

    private static final long FROM_OFFICE_ID = 1L;
    private static final long TO_OFFICE_ID = 2L;
    private static final long FROM_CLIENT_ID = 10L;
    private static final long TO_CLIENT_ID = 20L;
    private static final long SAVINGS_ACCOUNT_ID = 100L;
    private static final long LOAN_ID = 200L;

    private final StandingInstructionRepository standingInstructionRepository = mock(StandingInstructionRepository.class);
    private final SavingsAccountAssembler savingsAccountAssembler = mock(SavingsAccountAssembler.class);
    private final LoanAssembler loanAssembler = mock(LoanAssembler.class);
    private final OfficeRepositoryWrapper officeRepositoryWrapper = mock(OfficeRepositoryWrapper.class);
    private final ClientRepositoryWrapper clientRepositoryWrapper = mock(ClientRepositoryWrapper.class);
    private StandingInstructionWriteServiceImpl service;
    private AccountTransferStandingInstruction instruction;

    @BeforeEach
    void setUp() {
        service = new StandingInstructionWriteServiceImpl(mock(AccountTransferDetailRepository.class), standingInstructionRepository,
                savingsAccountAssembler, loanAssembler, officeRepositoryWrapper, clientRepositoryWrapper);
        instruction = savingsTransferInstruction(false);
        when(standingInstructionRepository.findById(INSTRUCTION_ID)).thenReturn(Optional.of(instruction));
    }

    @Test
    void allowPartialTransferCanBeTurnedOnThroughUpdate() {
        final StandingInstructionUpdateRequest request = request();
        request.setAllowPartialTransfer(true);

        assertThat(service.update(request).getChanges()).containsEntry("allowPartialTransfer", true);
        assertThat(instruction.isAllowPartialTransfer()).isTrue();
        // the new value is stored: sending it again is no longer a change
        assertThat(service.update(request).getChanges()).doesNotContainKey("allowPartialTransfer");
    }

    @Test
    void updateWithoutTheFlagLeavesItUnchanged() {
        final StandingInstructionUpdateRequest request = request();
        request.setRecurrenceInterval(2);

        final StandingInstructionUpdateResponse response = service.update(request);

        assertThat(response.getResourceId()).isEqualTo(INSTRUCTION_ID);
        assertThat(response.getChanges()).containsOnlyKeys("recurrenceInterval");
        assertThat(instruction.isAllowPartialTransfer()).isFalse();
    }

    @Test
    void onlyChangedValuesAreReported() {
        final StandingInstructionUpdateRequest request = request();
        request.setAmount(new BigDecimal("100.000")); // same value, different scale
        request.setPriority(StandingInstructionPriority.MEDIUM.getValue()); // unchanged
        request.setStatus(StandingInstructionStatus.DISABLED.getValue());
        request.setValidTill("30 November 2026");

        assertThat(service.update(request).getChanges()).containsOnlyKeys("status", "validTill").containsEntry("validTill",
                LocalDate.of(2026, 11, 30));
        assertThat(instruction.getStatus()).isEqualTo(StandingInstructionStatus.DISABLED.getValue());
        assertThat(instruction.getValidTill()).isEqualTo(LocalDate.of(2026, 11, 30));
    }

    @Test
    void recurrenceOnMonthDayChangeIsReportedAsEntered() {
        final StandingInstructionUpdateRequest request = request();
        request.setRecurrenceOnMonthDay("20 March");

        assertThat(service.update(request).getChanges()).containsEntry("recurrenceOnMonthDay", "20 March");
        assertThat(instruction.getRecurrenceOnDay()).isEqualTo(20);
        assertThat(instruction.getRecurrenceOnMonth()).isEqualTo(3);
    }

    @Test
    void mergedStateIsValidatedAfterTheUpdate() {
        final StandingInstructionUpdateRequest request = request();
        request.setValidTill("31 December 2025"); // before the stored validFrom of 01 January 2026

        assertThatThrownBy(() -> service.update(request)).isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void deletedInstructionCannotBeModified() {
        service.delete(StandingInstructionDeleteRequest.builder().id(INSTRUCTION_ID).build());
        assertThat(instruction.getStatus()).isEqualTo(StandingInstructionStatus.DELETED.getValue());

        final StandingInstructionUpdateRequest request = request();
        request.setRecurrenceInterval(3);

        assertThatThrownBy(() -> service.update(request)).isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void deleteReportsTheDeletedStatus() {
        assertThat(service.delete(StandingInstructionDeleteRequest.builder().id(INSTRUCTION_ID).build()).getChanges())
                .isEqualTo(Map.of("status", StandingInstructionStatus.DELETED.getValue()));
    }

    @Test
    void createLooksUpTheOfficesAndClientsNamedInTheRequest() {
        when(savingsAccountAssembler.assembleFrom(SAVINGS_ACCOUNT_ID, false)).thenReturn(mock(SavingsAccount.class));
        when(loanAssembler.assembleFrom(LOAN_ID)).thenReturn(mock(Loan.class));
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(FROM_OFFICE_ID)).thenReturn(mock(Office.class));
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(TO_OFFICE_ID)).thenReturn(mock(Office.class));
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(FROM_CLIENT_ID)).thenReturn(mock(Client.class));
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(TO_CLIENT_ID)).thenReturn(mock(Client.class));

        assertThat(service.create(loanRepaymentRequest()).getClientId()).isEqualTo(FROM_CLIENT_ID);

        verify(officeRepositoryWrapper).findOneWithNotFoundDetection(FROM_OFFICE_ID);
        verify(officeRepositoryWrapper).findOneWithNotFoundDetection(TO_OFFICE_ID);
        verify(clientRepositoryWrapper).findOneWithNotFoundDetection(FROM_CLIENT_ID);
        verify(clientRepositoryWrapper).findOneWithNotFoundDetection(TO_CLIENT_ID);
    }

    @Test
    void createWithAnUnknownClientIsReportedAsNotFound() {
        when(savingsAccountAssembler.assembleFrom(SAVINGS_ACCOUNT_ID, false)).thenReturn(mock(SavingsAccount.class));
        when(loanAssembler.assembleFrom(LOAN_ID)).thenReturn(mock(Loan.class));
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(FROM_OFFICE_ID)).thenReturn(mock(Office.class));
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(FROM_CLIENT_ID)).thenThrow(new ClientNotFoundException(FROM_CLIENT_ID));

        assertThatThrownBy(() -> service.create(loanRepaymentRequest())).isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void unknownInstructionIsReportedAsNotFound() {
        final StandingInstructionUpdateRequest request = request();
        request.setId(INSTRUCTION_ID + 1);

        assertThatThrownBy(() -> service.update(request)).isInstanceOf(StandingInstructionNotFoundException.class);
    }

    /** A savings to loan repayment "as per dues": no amount and no periodic recurrence fields are needed. */
    private static StandingInstructionCreationRequest loanRepaymentRequest() {
        final StandingInstructionCreationRequest request = new StandingInstructionCreationRequest();
        request.setFromOfficeId(FROM_OFFICE_ID);
        request.setFromClientId(FROM_CLIENT_ID);
        request.setFromAccountId(SAVINGS_ACCOUNT_ID);
        request.setFromAccountType(PortfolioAccountType.SAVINGS.getValue());
        request.setToOfficeId(TO_OFFICE_ID);
        request.setToClientId(TO_CLIENT_ID);
        request.setToAccountId(LOAN_ID);
        request.setToAccountType(PortfolioAccountType.LOAN.getValue());
        request.setName("loan repayment");
        request.setTransferType(AccountTransferType.LOAN_REPAYMENT.getValue());
        request.setStatus(StandingInstructionStatus.ACTIVE.getValue());
        request.setPriority(StandingInstructionPriority.MEDIUM.getValue());
        request.setInstructionType(StandingInstructionType.DUES.getValue());
        request.setRecurrenceType(AccountTransferRecurrenceType.AS_PER_DUES.getValue());
        request.setValidFrom("01 January 2026");
        request.setDateFormat("dd MMMM yyyy");
        request.setLocale("en");
        return request;
    }

    private static StandingInstructionUpdateRequest request() {
        final StandingInstructionUpdateRequest request = new StandingInstructionUpdateRequest();
        request.setId(INSTRUCTION_ID);
        request.setDateFormat("dd MMMM yyyy");
        request.setMonthDayFormat("dd MMMM");
        request.setLocale("en");
        return request;
    }

    private static AccountTransferStandingInstruction savingsTransferInstruction(final boolean allowPartialTransfer) {
        final AccountTransferDetails details = mock(AccountTransferDetails.class);
        when(details.fromSavingsAccount()).thenReturn(mock(SavingsAccount.class));
        when(details.toSavingsAccount()).thenReturn(mock(SavingsAccount.class));
        when(details.transferType()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER);
        return AccountTransferStandingInstruction.create(details, "monthly savings", StandingInstructionPriority.MEDIUM.getValue(),
                StandingInstructionType.FIXED.getValue(), StandingInstructionStatus.ACTIVE.getValue(), BigDecimal.valueOf(100),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), AccountTransferRecurrenceType.PERIODIC.getValue(),
                PeriodFrequencyType.MONTHS.getValue(), 1, MonthDay.of(1, 15), allowPartialTransfer);
    }
}
