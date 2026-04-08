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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SavingsAccountWritePlatformServiceJpaRepositoryImplTest {

    private SavingsAccountWritePlatformServiceJpaRepositoryImpl service;
    private Method validateTransactionsForTransfer;

    @BeforeEach
    void setUp() throws Exception {
        service = new SavingsAccountWritePlatformServiceJpaRepositoryImpl(mock(PlatformSecurityContext.class),
                mock(SavingsAccountDataValidator.class), mock(SavingsAccountRepositoryWrapper.class), mock(StaffRepositoryWrapper.class),
                mock(SavingsAccountTransactionRepository.class), mock(SavingsAccountAssembler.class),
                mock(SavingsAccountTransactionDataValidator.class), mock(SavingsAccountChargeDataValidator.class),
                mock(PaymentDetailWritePlatformService.class), mock(JournalEntryWritePlatformService.class),
                mock(SavingsAccountDomainService.class), mock(NoteRepository.class), mock(AccountTransfersReadPlatformService.class),
                mock(AccountAssociationsReadPlatformService.class), mock(ChargeRepositoryWrapper.class),
                mock(SavingsAccountChargeRepositoryWrapper.class), mock(HolidayRepositoryWrapper.class),
                mock(WorkingDaysRepositoryWrapper.class), mock(ConfigurationDomainService.class),
                mock(DepositAccountOnHoldTransactionRepository.class), mock(EntityDatatableChecksWritePlatformService.class),
                mock(AppUserRepositoryWrapper.class), mock(StandingInstructionRepository.class), mock(BusinessEventNotifierService.class),
                mock(GSIMRepositoy.class), mock(SavingsAccountInterestPostingService.class), mock(ErrorHandler.class));

        validateTransactionsForTransfer = SavingsAccountWritePlatformServiceJpaRepositoryImpl.class
                .getDeclaredMethod("validateTransactionsForTransfer", SavingsAccount.class, LocalDate.class);
        validateTransactionsForTransfer.setAccessible(true);
    }

    @Test
    void validateTransactionsForTransfer_nullTransferDate_doesNotThrowNullPointerException() {
        LocalDate transactionDate = LocalDate.of(2024, 1, 10);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(transactionDate);
        when(transaction.getSubmittedOnDate()).thenReturn(transactionDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, null))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class)
                .satisfies(e -> assertThat(e.getCause()).isNotInstanceOf(NullPointerException.class));
    }

    @Test
    void validateTransactionsForTransfer_transactionWithNullDate_doesNotThrow() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(null);
        when(transaction.getSubmittedOnDate()).thenReturn(null);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatNoException().isThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate));
    }

    @Test
    void validateTransactionsForTransfer_transactionDateEqualsTransferDate_throwsGeneralPlatformDomainRuleException() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(transferDate);
        when(transaction.getSubmittedOnDate()).thenReturn(transferDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class);
    }

    @Test
    void validateTransactionsForTransfer_transactionDateAfterTransferDate_throwsGeneralPlatformDomainRuleException() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);
        LocalDate futureDate = transferDate.plusDays(1);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(futureDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class);
    }

    @Test
    void validateTransactionsForTransfer_transactionDateBeforeTransferDate_doesNotThrow() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);
        LocalDate pastDate = transferDate.minusDays(1);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(pastDate);
        when(transaction.getSubmittedOnDate()).thenReturn(pastDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatNoException().isThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate));
    }
}
