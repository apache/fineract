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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanDeferredChargeTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDeferredChargeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.repository.LoanDeferredChargeBalanceRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.DeleteSpecification;

@ExtendWith(MockitoExtension.class)
class LoanDeferredChargeWritePlatformServiceImplTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 15);
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 10);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 1, 12);
    private static final LocalDate FUTURE_DUE_DATE = LocalDate.of(2026, 2, 1);
    private static final MonetaryCurrency CURRENCY = MonetaryCurrency.fromCurrencyData(new CurrencyData("EUR"));
    private static final BigDecimal CHARGE_AMOUNT = new BigDecimal("10.00");

    private static MockedStatic<MoneyHelper> moneyHelperMock;
    private static MockedStatic<DateUtils> dateUtilsMock;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanDeferredChargeBalanceRepository deferredChargeBalanceRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private ExternalIdFactory externalIdFactory;

    @InjectMocks
    private LoanDeferredChargeWritePlatformServiceImpl underTest;

    private Loan loan;

    @BeforeAll
    static void setUpStatics() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        dateUtilsMock = mockStatic(DateUtils.class, CALLS_REAL_METHODS);
        dateUtilsMock.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE);
    }

    @AfterAll
    static void tearDownStatics() {
        moneyHelperMock.close();
        dateUtilsMock.close();
    }

    @BeforeEach
    void setUpLoan() {
        loan = mock(Loan.class);
        final Office office = mock(Office.class);
        lenient().when(loan.getCurrency()).thenReturn(CURRENCY);
        lenient().when(loan.getOffice()).thenReturn(office);
        lenient().when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        lenient().when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(deferredChargeBalanceRepository.saveAndFlush(any(LoanDeferredChargeBalance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsDeferredChargeAndBalanceForDisbursementCharge() {
        final LoanCharge loanCharge = amortizedCharge(1L, false, null, ChargeAmortizationPeriodType.FULL_LOAN_TERM, null);

        underTest.createForEffectiveCharge(loan, loanCharge, DISBURSEMENT_DATE);

        final LoanTransaction transaction = captureSavedTransaction();
        assertThat(transaction.getTypeOf()).isEqualTo(LoanTransactionType.DEFERRED_CHARGE);
        assertThat(transaction.getAmount()).isEqualByComparingTo(CHARGE_AMOUNT);
        assertThat(transaction.getTransactionDate()).isEqualTo(DISBURSEMENT_DATE);
        verify(loan).addLoanTransaction(transaction);

        final LoanDeferredChargeBalance balance = captureSavedBalance();
        assertThat(balance.getLoan()).isSameAs(loan);
        assertThat(balance.getLoanCharge()).isSameAs(loanCharge);
        assertThat(balance.getLoanTransaction()).isSameAs(transaction);
        assertThat(balance.getDate()).isEqualTo(DISBURSEMENT_DATE);
        assertThat(balance.getAmount()).isEqualByComparingTo(CHARGE_AMOUNT);
        assertThat(balance.getUnrecognizedAmount()).isEqualByComparingTo(CHARGE_AMOUNT);
        assertThat(balance.getPeriodType()).isEqualTo(ChargeAmortizationPeriodType.FULL_LOAN_TERM);
        assertThat(balance.getWindowEndDate()).isNull();
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(LoanDeferredChargeTransactionCreatedBusinessEvent.class));
    }

    @Test
    void createsOneBalancePerTrancheCharge() {
        final LoanCharge firstTrancheCharge = amortizedCharge(11L, false, null, ChargeAmortizationPeriodType.NUMBER_OF_DAYS, 30);
        final LoanCharge secondTrancheCharge = amortizedCharge(12L, false, null, ChargeAmortizationPeriodType.NUMBER_OF_DAYS, 30);

        underTest.createForEffectiveCharge(loan, firstTrancheCharge, DISBURSEMENT_DATE);
        underTest.createForEffectiveCharge(loan, secondTrancheCharge, DISBURSEMENT_DATE.plusMonths(1));

        final ArgumentCaptor<LoanDeferredChargeBalance> balanceCaptor = ArgumentCaptor.forClass(LoanDeferredChargeBalance.class);
        verify(deferredChargeBalanceRepository, org.mockito.Mockito.times(2)).saveAndFlush(balanceCaptor.capture());
        assertThat(balanceCaptor.getAllValues()).extracting(LoanDeferredChargeBalance::getLoanCharge).containsExactly(firstTrancheCharge,
                secondTrancheCharge);
        assertThat(balanceCaptor.getAllValues()).extracting(LoanDeferredChargeBalance::getWindowEndDate)
                .containsExactly(DISBURSEMENT_DATE.plusDays(30), DISBURSEMENT_DATE.plusMonths(1).plusDays(30));
    }

    @Test
    void createsDeferredChargeForSpecifiedDueDateOnOrBeforeBusinessDate() {
        final LoanCharge loanCharge = amortizedCharge(2L, true, DUE_DATE, ChargeAmortizationPeriodType.FULL_LOAN_TERM, null);

        underTest.createForEffectiveCharge(loan, loanCharge, DUE_DATE);

        final LoanTransaction transaction = captureSavedTransaction();
        assertThat(transaction.getTypeOf()).isEqualTo(LoanTransactionType.DEFERRED_CHARGE);
        assertThat(transaction.getTransactionDate()).isEqualTo(DUE_DATE);
        verify(deferredChargeBalanceRepository).saveAndFlush(any(LoanDeferredChargeBalance.class));
    }

    @Test
    void skipsFutureSpecifiedDueDateCharge() {
        final LoanCharge loanCharge = amortizedCharge(3L, true, FUTURE_DUE_DATE, ChargeAmortizationPeriodType.FULL_LOAN_TERM, null);

        underTest.createForEffectiveCharge(loan, loanCharge, FUTURE_DUE_DATE);

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verify(deferredChargeBalanceRepository, never()).saveAndFlush(any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void skipsNonAmortizedCharge() {
        final LoanCharge loanCharge = mock(LoanCharge.class);
        when(loanCharge.isAmortized()).thenReturn(false);

        underTest.createForEffectiveCharge(loan, loanCharge, DISBURSEMENT_DATE);

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verify(deferredChargeBalanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsWaivedSpecifiedDueDateCharge() {
        final LoanCharge loanCharge = amortizedCharge(7L, true, DUE_DATE, ChargeAmortizationPeriodType.FULL_LOAN_TERM, null);
        when(loanCharge.isWaived()).thenReturn(true);

        underTest.createForEffectiveCharge(loan, loanCharge, DUE_DATE);

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verify(deferredChargeBalanceRepository, never()).saveAndFlush(any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void skipsWhenOpenBalanceAlreadyExists() {
        final LoanCharge loanCharge = amortizedCharge(4L, false, null, ChargeAmortizationPeriodType.FULL_LOAN_TERM, null);
        when(deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(4L))
                .thenReturn(Optional.of(new LoanDeferredChargeBalance()));

        underTest.createForEffectiveCharge(loan, loanCharge, DISBURSEMENT_DATE);

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verify(deferredChargeBalanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void resetBalanceDeletesAllBalancesForLoan() {
        underTest.resetBalance(99L);

        verify(deferredChargeBalanceRepository).delete(org.mockito.ArgumentMatchers.<DeleteSpecification<LoanDeferredChargeBalance>>any());
    }

    @Test
    void reverseForLoanChargeReversesTheDeferredChargeAndSoftDeletesTheBalance() {
        final LoanTransaction deferredCharge = LoanTransaction.deferredCharge(loan, Money.of(CURRENCY, CHARGE_AMOUNT), DISBURSEMENT_DATE,
                ExternalId.empty());
        final LoanDeferredChargeBalance balance = new LoanDeferredChargeBalance();
        balance.setLoanTransaction(deferredCharge);
        when(deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(5L)).thenReturn(Optional.of(balance));

        final Optional<LoanTransaction> reversed = underTest.reverseForLoanCharge(5L);

        assertThat(reversed).containsSame(deferredCharge);
        assertThat(deferredCharge.isReversed()).isTrue();
        assertThat(balance.isDeleted()).isTrue();
        verify(deferredChargeBalanceRepository).saveAndFlush(balance);
    }

    @Test
    void reverseForLoanChargeDoesNothingWithoutAnOpenBalance() {
        when(deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(6L)).thenReturn(Optional.empty());

        assertThat(underTest.reverseForLoanCharge(6L)).isEmpty();

        verify(deferredChargeBalanceRepository, never()).saveAndFlush(any());
    }

    private LoanCharge amortizedCharge(final Long id, final boolean specifiedDueDate, final LocalDate dueDate,
            final ChargeAmortizationPeriodType periodType, final Integer days) {
        final LoanCharge loanCharge = mock(LoanCharge.class);
        when(loanCharge.isAmortized()).thenReturn(true);
        lenient().when(loanCharge.getId()).thenReturn(id);
        lenient().when(loanCharge.amount()).thenReturn(CHARGE_AMOUNT);
        lenient().when(loanCharge.isSpecifiedDueDate()).thenReturn(specifiedDueDate);
        if (specifiedDueDate) {
            lenient().when(loanCharge.getDueLocalDate()).thenReturn(dueDate);
        }
        lenient().when(loanCharge.getAmortizationPeriodType()).thenReturn(periodType);
        lenient().when(loanCharge.getAmortizationCalculationType()).thenReturn(ChargeAmortizationCalculationType.FLAT);
        lenient().when(loanCharge.getAmortizationIncomeType()).thenReturn(ChargeAmortizationIncomeType.FEE);
        lenient().when(loanCharge.getAmortizationDays()).thenReturn(days);
        lenient().when(deferredChargeBalanceRepository.findByLoanChargeIdAndDeletedFalseAndClosedFalse(id)).thenReturn(Optional.empty());
        return loanCharge;
    }

    private LoanTransaction captureSavedTransaction() {
        final ArgumentCaptor<LoanTransaction> captor = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }

    private LoanDeferredChargeBalance captureSavedBalance() {
        final ArgumentCaptor<LoanDeferredChargeBalance> captor = ArgumentCaptor.forClass(LoanDeferredChargeBalance.class);
        verify(deferredChargeBalanceRepository).saveAndFlush(captor.capture());
        return captor.getValue();
    }
}
