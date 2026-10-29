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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.domain.AccountTransferAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.exception.DifferentCurrenciesException;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountTransfersWritePlatformServiceImplTest {

    @InjectMocks
    private AccountTransfersWritePlatformServiceImpl underTest;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AccountTransferRepository accountTransferRepository;
    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private LoanAdjustmentService loanAdjustmentService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private JsonCommand command;
    @Mock
    private AccountTransfersDataValidator accountTransfersDataValidator;
    @Mock
    private AccountTransferAssembler accountTransferAssembler;
    @Mock
    private SavingsAccountAssembler savingsAccountAssembler;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private LoanAssembler loanAccountAssembler;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private AccountTransferDetailRepository accountTransferDetailRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;

    @Test
    void givenLoanToSavingsRefund_whenUndo_thenSavingsUndoneLoanAdjustedAndTransferReversed() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);
        AccountTransferDetails details = mock(AccountTransferDetails.class);
        Loan loan = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        LoanTransaction fromLoanTransaction = mock(LoanTransaction.class);
        SavingsAccountTransaction toSavingsTransaction = mock(SavingsAccountTransaction.class);
        ExternalId reversalExternalId = mock(ExternalId.class);
        Office fromOffice = mock(Office.class);
        Office toOffice = mock(Office.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getAccountTransferDetails()).thenReturn(details);
        when(details.fromLoanAccount()).thenReturn(loan);
        when(details.toSavingsAccount()).thenReturn(toSavingsAccount);
        when(loan.getOffice()).thenReturn(fromOffice);
        when(toSavingsAccount.office()).thenReturn(toOffice);
        when(fromOffice.getHierarchy()).thenReturn(".1.");
        when(toOffice.getHierarchy()).thenReturn(".1.");
        when(transaction.getFromLoanTransaction()).thenReturn(fromLoanTransaction);
        when(fromLoanTransaction.isDisbursement()).thenReturn(false);
        when(fromLoanTransaction.getLoan()).thenReturn(loan);
        when(fromLoanTransaction.getTransactionDate()).thenReturn(LocalDate.of(2026, 5, 2));
        when(transaction.getToSavingsTransaction()).thenReturn(toSavingsTransaction);
        when(toSavingsTransaction.getSavingsAccount()).thenReturn(toSavingsAccount);
        when(toSavingsAccount.getId()).thenReturn(2L);
        when(toSavingsTransaction.getId()).thenReturn(3L);
        when(externalIdFactory.create()).thenReturn(reversalExternalId);

        underTest.undo(command);

        verify(savingsAccountWritePlatformService).undoTransaction(2L, 3L, true);
        verify(loanAdjustmentService).adjustLoanTransaction(eq(loan), eq(fromLoanTransaction), any(), isNull(), any());
        verify(transaction).reverse();
    }

    @Test
    void givenLoanToSavingsDisbursement_whenUndo_thenRejectedBeforeAnythingIsReversed() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);
        AccountTransferDetails details = mock(AccountTransferDetails.class);
        Loan loan = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        LoanTransaction fromLoanTransaction = mock(LoanTransaction.class);
        Office fromOffice = mock(Office.class);
        Office toOffice = mock(Office.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(false);
        when(transaction.getAccountTransferDetails()).thenReturn(details);
        when(details.fromLoanAccount()).thenReturn(loan);
        when(details.toSavingsAccount()).thenReturn(toSavingsAccount);
        when(loan.getOffice()).thenReturn(fromOffice);
        when(toSavingsAccount.office()).thenReturn(toOffice);
        when(fromOffice.getHierarchy()).thenReturn(".1.");
        when(toOffice.getHierarchy()).thenReturn(".1.");
        when(transaction.getFromLoanTransaction()).thenReturn(fromLoanTransaction);
        when(fromLoanTransaction.isDisbursement()).thenReturn(true);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.undo(command));

        verify(savingsAccountWritePlatformService, never()).undoTransaction(anyLong(), anyLong(), anyBoolean());
        verify(loanAdjustmentService, never()).adjustLoanTransaction(any(), any(), any(), any(), any());
        verify(transaction, never()).reverse();
    }

    @Test
    void givenAlreadyReversedTransfer_whenUndo_thenThrowsGeneralPlatformDomainRuleException() {
        AccountTransferTransaction transaction = mock(AccountTransferTransaction.class);

        when(command.entityId()).thenReturn(1L);
        when(accountTransferRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transaction.isReversed()).thenReturn(true);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.undo(command));

        verify(savingsAccountWritePlatformService, never()).undoTransaction(anyLong(), anyLong(), anyBoolean());
        verify(loanAdjustmentService, never()).adjustLoanTransaction(any(), any(), any(), any(), any());
        verify(transaction, never()).reverse();
    }

    @Test
    void givenSavingsToSavingsTransfer_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(command.localDateValueOfParameterNamed("transferDate")).thenReturn(LocalDate.of(2026, 5, 2));
        when(command.bigDecimalValueOfParameterNamed("transferAmount")).thenReturn(BigDecimal.valueOf(100));
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("yyyy-MM-dd");
        when(command.integerValueSansLocaleOfParameterNamed("fromAccountType")).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.integerValueSansLocaleOfParameterNamed("toAccountType")).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.longValueOfParameterNamed("fromAccountId")).thenReturn(101L);
        when(command.longValueOfParameterNamed("toAccountId")).thenReturn(202L);

        when(savingsAccountAssembler.assembleFromInUserScope(101L, false)).thenReturn(fromSavingsAccount);
        when(savingsAccountAssembler.assembleFromInUserScope(202L, false)).thenReturn(toSavingsAccount);
        when(fromSavingsAccount.getCurrency()).thenReturn(eur);
        when(toSavingsAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.create(command));
    }

    @Test
    void givenSavingsToLoanTransfer_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        Loan toLoanAccount = mock(Loan.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(command.localDateValueOfParameterNamed("transferDate")).thenReturn(LocalDate.of(2026, 5, 2));
        when(command.bigDecimalValueOfParameterNamed("transferAmount")).thenReturn(BigDecimal.valueOf(100));
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("yyyy-MM-dd");
        when(command.integerValueSansLocaleOfParameterNamed("fromAccountType")).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.integerValueSansLocaleOfParameterNamed("toAccountType")).thenReturn(PortfolioAccountType.LOAN.getValue());
        when(command.longValueOfParameterNamed("fromAccountId")).thenReturn(101L);
        when(command.longValueOfParameterNamed("toAccountId")).thenReturn(202L);

        when(savingsAccountAssembler.assembleFromInUserScope(101L, false)).thenReturn(fromSavingsAccount);
        when(loanAccountAssembler.assembleFromInUserScope(202L)).thenReturn(toLoanAccount);
        when(fromSavingsAccount.getCurrency()).thenReturn(eur);
        when(toLoanAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.create(command));
    }

    @Test
    void givenLoanToSavingsTransfer_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        Loan fromLoanAccount = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(command.localDateValueOfParameterNamed("transferDate")).thenReturn(LocalDate.of(2026, 5, 2));
        when(command.bigDecimalValueOfParameterNamed("transferAmount")).thenReturn(BigDecimal.valueOf(100));
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("yyyy-MM-dd");
        when(command.integerValueSansLocaleOfParameterNamed("fromAccountType")).thenReturn(PortfolioAccountType.LOAN.getValue());
        when(command.integerValueSansLocaleOfParameterNamed("toAccountType")).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.longValueOfParameterNamed("fromAccountId")).thenReturn(101L);
        when(command.longValueOfParameterNamed("toAccountId")).thenReturn(202L);

        when(loanAccountAssembler.assembleFromInUserScope(101L)).thenReturn(fromLoanAccount);
        when(savingsAccountAssembler.assembleFromInUserScope(202L, false)).thenReturn(toSavingsAccount);
        when(fromLoanAccount.getCurrency()).thenReturn(eur);
        when(toSavingsAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.create(command));
    }

    @Test
    void givenStandingInstructionSavingsToLoan_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        AccountTransferDTO dto = mock(AccountTransferDTO.class);
        SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        Loan toLoanAccount = mock(Loan.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(dto.getFromAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(dto.getToAccountType()).thenReturn(PortfolioAccountType.LOAN);
        when(dto.getFromAccountId()).thenReturn(101L);
        when(dto.getToAccountId()).thenReturn(202L);
        when(savingsAccountAssembler.assembleFrom(101L, false)).thenReturn(fromSavingsAccount);
        when(loanAccountAssembler.assembleFrom(202L)).thenReturn(toLoanAccount);
        when(fromSavingsAccount.getCurrency()).thenReturn(eur);
        when(toLoanAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.transferFunds(dto));
    }

    @Test
    void givenStandingInstructionSavingsToSavings_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        AccountTransferDTO dto = mock(AccountTransferDTO.class);
        SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(dto.getFromAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(dto.getToAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(dto.getFromAccountId()).thenReturn(101L);
        when(dto.getToAccountId()).thenReturn(202L);
        when(savingsAccountAssembler.assembleFrom(101L, false)).thenReturn(fromSavingsAccount);
        when(savingsAccountAssembler.assembleFrom(202L, false)).thenReturn(toSavingsAccount);
        when(fromSavingsAccount.getCurrency()).thenReturn(eur);
        when(toSavingsAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.transferFunds(dto));
    }

    @Test
    void givenStandingInstructionLoanToSavings_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        AccountTransferDTO dto = mock(AccountTransferDTO.class);
        Loan fromLoanAccount = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(dto.getFromAccountType()).thenReturn(PortfolioAccountType.LOAN);
        when(dto.getToAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(dto.getFromAccountId()).thenReturn(101L);
        when(dto.getToAccountId()).thenReturn(202L);
        when(loanAccountAssembler.assembleFrom(101L)).thenReturn(fromLoanAccount);
        when(savingsAccountAssembler.assembleFrom(202L, false)).thenReturn(toSavingsAccount);
        when(fromLoanAccount.getCurrency()).thenReturn(eur);
        when(toSavingsAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.transferFunds(dto));
    }

    @Test
    void givenLoanTopup_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        AccountTransferDTO dto = mock(AccountTransferDTO.class);
        Loan fromLoanAccount = mock(Loan.class);
        Loan toLoanAccount = mock(Loan.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(dto.getFromAccountId()).thenReturn(101L);
        when(dto.getToAccountId()).thenReturn(202L);
        when(loanAccountAssembler.assembleFrom(101L)).thenReturn(fromLoanAccount);
        when(loanAccountAssembler.assembleFrom(202L)).thenReturn(toLoanAccount);
        when(fromLoanAccount.getCurrency()).thenReturn(eur);
        when(toLoanAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.repayLoanWithTopup(dto));
    }

    @Test
    void givenRefundByTransfer_whenCurrenciesMismatch_thenThrowsDifferentCurrenciesException() {
        Loan fromLoanAccount = mock(Loan.class);
        SavingsAccount toSavingsAccount = mock(SavingsAccount.class);
        MonetaryCurrency eur = new MonetaryCurrency("EUR", 2, 1);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);
        PaidInAdvanceData paidInAdvance = mock(PaidInAdvanceData.class);

        when(command.localDateValueOfParameterNamed("transferDate")).thenReturn(LocalDate.of(2026, 5, 2));
        when(command.bigDecimalValueOfParameterNamed("transferAmount")).thenReturn(BigDecimal.valueOf(100));
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("yyyy-MM-dd");
        when(command.longValueOfParameterNamed("fromAccountId")).thenReturn(101L);
        when(command.longValueOfParameterNamed("toAccountId")).thenReturn(202L);

        when(loanAccountAssembler.assembleFromInUserScope(101L)).thenReturn(fromLoanAccount);
        when(loanReadPlatformService.retrieveTotalPaidInAdvance(101L)).thenReturn(paidInAdvance);
        when(paidInAdvance.getPaidInAdvance()).thenReturn(BigDecimal.valueOf(200));
        when(savingsAccountAssembler.assembleFromInUserScope(202L, false)).thenReturn(toSavingsAccount);
        when(fromLoanAccount.getCurrency()).thenReturn(eur);
        when(toSavingsAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.refundByTransfer(command));
    }

    @Test
    void givenSavingsToLoanTransfer_whenCurrencyIsNull_thenThrowsDifferentCurrenciesException() {
        SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        Loan toLoanAccount = mock(Loan.class);
        MonetaryCurrency usd = new MonetaryCurrency("USD", 2, 1);

        when(command.localDateValueOfParameterNamed("transferDate")).thenReturn(LocalDate.of(2026, 5, 2));
        when(command.bigDecimalValueOfParameterNamed("transferAmount")).thenReturn(BigDecimal.valueOf(100));
        when(command.extractLocale()).thenReturn(Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("yyyy-MM-dd");
        when(command.integerValueSansLocaleOfParameterNamed("fromAccountType")).thenReturn(PortfolioAccountType.SAVINGS.getValue());
        when(command.integerValueSansLocaleOfParameterNamed("toAccountType")).thenReturn(PortfolioAccountType.LOAN.getValue());
        when(command.longValueOfParameterNamed("fromAccountId")).thenReturn(101L);
        when(command.longValueOfParameterNamed("toAccountId")).thenReturn(202L);

        when(savingsAccountAssembler.assembleFromInUserScope(101L, false)).thenReturn(fromSavingsAccount);
        when(loanAccountAssembler.assembleFromInUserScope(202L)).thenReturn(toLoanAccount);
        when(fromSavingsAccount.getCurrency()).thenReturn(new MonetaryCurrency(null, 2, 1));
        when(toLoanAccount.getCurrency()).thenReturn(usd);

        assertThrows(DifferentCurrenciesException.class, () -> underTest.create(command));
    }
}
