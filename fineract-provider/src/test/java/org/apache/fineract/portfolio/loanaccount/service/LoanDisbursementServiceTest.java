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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDisbursementValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

public class LoanDisbursementServiceTest {

    private static final LocalDate JAN_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate JAN_2 = LocalDate.of(2026, 1, 2);
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(500);

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    private final MonetaryCurrency currency = MonetaryCurrency.fromCurrencyData(new CurrencyData("EUR"));
    private final LoanChargeService loanChargeService = mock(LoanChargeService.class);
    private final LoanDisbursementService underTest = new LoanDisbursementService(mock(LoanChargeValidator.class),
            mock(LoanDisbursementValidator.class), loanChargeService, mock(LoanBalanceService.class), mock(LoanJournalEntryPoster.class),
            mock(LoanTransactionRepository.class), mock(ConfigurationDomainService.class), mock(BusinessEventNotifierService.class));
    private final Charge flatFee = mockDefinition(1L);
    private final Charge otherFee = mockDefinition(2L);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @Test
    public void testUndoDeactivatesTheFeesOfTheUndoneTrancheOnly() {
        // same date and principal: equal by LoanDisbursementDetails.equals, still different tranches
        final LoanDisbursementDetails firstTranche = tranche(JAN_1);
        final LoanDisbursementDetails undoneTranche = tranche(JAN_1);
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, firstTranche);
        final LoanCharge undoneFee = mockFee(20L, flatFee, undoneTranche);
        final LoanCharge undoneOtherFee = mockFee(30L, otherFee, undoneTranche);
        final Loan loan = mockLoan(firstTrancheFee, undoneFee, undoneOtherFee);

        underTest.deactivateDownPaymentFeesOfTranche(loan, undoneTranche);

        verifyDeactivated(undoneFee);
        verifyDeactivated(undoneOtherFee);
        verifyUntouched(firstTrancheFee);
    }

    @Test
    public void testUndoOfTrancheDisbursedWithoutFeeKeepsTheFeesOfOtherTranches() {
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, tranche(JAN_1));
        final Loan loan = mockLoan(firstTrancheFee);

        underTest.deactivateDownPaymentFeesOfTranche(loan, tranche(JAN_1));

        verifyUntouched(firstTrancheFee);
    }

    @Test
    public void testSkipDeactivatesTheUnpaidFeesOfTheDisbursedTranchesOnly() {
        final LoanDisbursementDetails disbursed = tranche(JAN_2);
        final LoanCharge disbursedTrancheFee = mockFee(10L, flatFee, disbursed);
        final LoanCharge applicationFee = mockFee(20L, flatFee, null);
        final LoanCharge expectedTrancheFee = mockFee(30L, flatFee, tranche(JAN_2));
        final Loan loan = mockLoan(disbursedTrancheFee, applicationFee, expectedTrancheFee);
        when(loan.isApproved()).thenReturn(true);

        final boolean deactivated;
        try (MockedConstruction<LoanRepaymentScheduleProcessingWrapper> wrapper = mockConstruction(
                LoanRepaymentScheduleProcessingWrapper.class)) {
            deactivated = underTest.prepareDownPaymentFeesForDisbursement(loan, List.of(disbursed), JAN_2, true, null);
            assertEquals(1, wrapper.constructed().size());
        }

        assertTrue(deactivated);
        verify(disbursedTrancheFee).setActive(false);
        verify(applicationFee).setActive(false);
        verify(expectedTrancheFee, never()).setActive(false);
    }

    @Test
    public void testFirstDisbursementMovesTheUnpaidFeesOfItsTrancheAndTheApplicationFee() {
        final LoanDisbursementDetails disbursed = tranche(JAN_2);
        final LoanCharge disbursedTrancheFee = mockFee(10L, flatFee, disbursed);
        final LoanCharge applicationFee = mockFee(20L, otherFee, null);
        final LoanCharge otherTrancheFee = mockFee(30L, flatFee, tranche(JAN_1));
        final Loan loan = mockLoan(disbursedTrancheFee, applicationFee, otherTrancheFee);
        when(loan.isApproved()).thenReturn(true);

        final boolean deactivated = underTest.prepareDownPaymentFeesForDisbursement(loan, List.of(disbursed), JAN_2, false, null);

        assertFalse(deactivated);
        verify(loanChargeService).updateDownPaymentFee(eq(loan), eq(disbursedTrancheFee), eq(JAN_2));
        // like a disbursement fee, the application fee is not linked to a tranche
        verify(applicationFee, never()).setDisbursementDetail(any());
        verify(loanChargeService).updateDownPaymentFee(eq(loan), eq(applicationFee), eq(JAN_2));
        verify(loanChargeService, never()).updateDownPaymentFee(eq(loan), eq(otherTrancheFee), any());
    }

    @Test
    public void testLaterDisbursementLeavesTheUnpaidApplicationFee() {
        final LoanCharge applicationFee = mockFee(20L, flatFee, null);
        final Loan loan = mockLoan(applicationFee);

        underTest.prepareDownPaymentFeesForDisbursement(loan, List.of(tranche(JAN_2)), JAN_2, false, null);
        underTest.prepareDownPaymentFeesForDisbursement(loan, List.of(tranche(JAN_2)), JAN_2, true, null);

        verify(loanChargeService, never()).updateDownPaymentFee(eq(loan), eq(applicationFee), any());
        verify(applicationFee, never()).setActive(false);
    }

    @Test
    public void testFirstDisbursementTakesTheApplicationFeeInsteadOfCreatingOne() {
        final LoanCharge applicationFee = mockFee(20L, flatFee, null);
        when(applicationFee.getDueLocalDate()).thenReturn(JAN_1);
        final Loan loan = mockLoan(applicationFee);
        when(loan.isApproved()).thenReturn(true);

        disburse(loan, tranche(JAN_1), JAN_1);

        verify(loanChargeService, never()).create(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(applicationFee, never()).setDisbursementDetail(any());
    }

    @Test
    public void testNextTrancheCreatesItsOwnFeeAndKeepsTheUnpaidFeeOfTheDisbursedTranche() {
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, tranche(JAN_1));
        final Loan loan = mockLoan(firstTrancheFee);
        final LoanDisbursementDetails nextTranche = tranche(JAN_2);
        final LoanCharge newFee = mockFee(20L, flatFee, null);
        when(loanChargeService.create(eq(loan), eq(flatFee), any(), any(), any(), any(), eq(JAN_2), any(), any(), any(), any()))
                .thenReturn(newFee);

        disburse(loan, nextTranche, JAN_2);

        verify(newFee).setDisbursementDetail(nextTranche);
        verify(loanChargeService).addLoanCharge(loan, newFee);
        verify(loanChargeService, never()).updateDownPaymentFee(eq(loan), eq(firstTrancheFee), any());
        verify(firstTrancheFee, never()).setActive(false);
    }

    @Test
    public void testSameDayTrancheDisbursedLaterDoesNotGiveTheSkippedTrancheAFee() {
        final LoanCharge firstTrancheFee = mockFee(10L, flatFee, tranche(JAN_1));
        // the second tranche of the day was disbursed with skipDownPaymentFee and has no fee
        final Loan loan = mockLoan(firstTrancheFee);
        final LoanCharge newFee = mockFee(30L, flatFee, null);
        when(loanChargeService.create(eq(loan), eq(flatFee), any(), any(), any(), any(), eq(JAN_1), any(), any(), any(), any()))
                .thenReturn(newFee);

        disburse(loan, tranche(JAN_1), JAN_1);

        verify(loanChargeService).create(eq(loan), eq(flatFee), any(), any(), any(), any(), eq(JAN_1), any(), any(), any(), any());
    }

    private void disburse(final Loan loan, final LoanDisbursementDetails tranche, final LocalDate date) {
        try (MockedStatic<TemporaryConfigurationServiceContainer> configuration = mockStatic(
                TemporaryConfigurationServiceContainer.class)) {
            configuration.when(TemporaryConfigurationServiceContainer::isExternalIdAutoGenerationEnabled).thenReturn(false);
            underTest.prepareDownPaymentFeesForDisbursement(loan, List.of(tranche), date, false, null);
            underTest.completeDownPaymentFeesForDisbursement(loan, List.of(tranche), date, false, null);
        }
    }

    private void verifyDeactivated(final LoanCharge fee) {
        verify(fee).resetPaidAmount(currency);
        verify(fee).setActive(false);
    }

    private void verifyUntouched(final LoanCharge fee) {
        verify(fee, never()).resetPaidAmount(currency);
        verify(fee, never()).setActive(false);
    }

    private Loan mockLoan(final LoanCharge... charges) {
        final Loan loan = mock(Loan.class, RETURNS_DEEP_STUBS);
        final Set<LoanCharge> loanCharges = new LinkedHashSet<>(List.of(charges));
        when(loan.getActiveCharges()).thenReturn(loanCharges);
        when(loan.getCharges()).thenReturn(loanCharges);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getLoanRepaymentScheduleDetail().isEnableDownPayment()).thenReturn(true);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of());
        when(loan.getLoanProductRelatedDetail()).thenReturn(mock(LoanProductRelatedDetail.class));
        return loan;
    }

    private static LoanDisbursementDetails tranche(final LocalDate date) {
        return new LoanDisbursementDetails(date, date, PRINCIPAL, null, false);
    }

    private static Charge mockDefinition(final Long id) {
        final Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(id);
        return charge;
    }

    private static LoanCharge mockFee(final Long id, final Charge definition, final LoanDisbursementDetails tranche) {
        final LoanCharge fee = mock(LoanCharge.class);
        when(fee.getId()).thenReturn(id);
        when(fee.getCharge()).thenReturn(definition);
        when(fee.isActive()).thenReturn(true);
        when(fee.isDueAtDownPayment()).thenReturn(true);
        when(fee.getDisbursementDetail()).thenReturn(tranche);
        if (tranche != null) {
            when(fee.getDueLocalDate()).thenReturn(tranche.actualOrExpectedDisbursementDate());
            when(fee.isDownPaymentFeeOf(same(tranche))).thenReturn(true);
        }
        return fee;
    }
}
