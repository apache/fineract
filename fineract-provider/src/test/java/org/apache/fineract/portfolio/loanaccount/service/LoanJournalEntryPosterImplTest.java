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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.journalentry.data.AccountingBridgeDataDTO;
import org.apache.fineract.accounting.journalentry.data.ChargeTaxDetailDTO;
import org.apache.fineract.accounting.journalentry.data.LoanChargePaidByDTO;
import org.apache.fineract.accounting.journalentry.service.ExternalOwnerJournalEntryWriteService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeTaxDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

// lenient: the poster reads many loan and transaction fields this test does not care about, through deep stubs
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanJournalEntryPosterImplTest {

    private static final Long TRANSACTION_ID = 10L;
    private static final Long TAX_ACCOUNT_ID = 77L;
    private static final Long OTHER_TAX_ACCOUNT_ID = 78L;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    @Mock
    private ExternalOwnerJournalEntryWriteService externalOwnerJournalEntryWriteService;
    @Mock
    private LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    private MockedStatic<MoneyHelper> moneyHelper;
    private LoanJournalEntryPosterImpl underTest;
    private LoanTransaction loanTransaction;

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        underTest = new LoanJournalEntryPosterImpl(journalEntryWritePlatformService, externalOwnerJournalEntryWriteService,
                loanAmortizationAllocationMappingRepository, loanTransactionRepository);
        loanTransaction = mock(LoanTransaction.class, RETURNS_DEEP_STUBS);
        when(loanTransaction.getId()).thenReturn(TRANSACTION_ID);
        when(loanTransaction.getLoanTransactionRelations()).thenReturn(Set.of());
        when(loanTransaction.getLoanTransactionToRepaymentScheduleMappings()).thenReturn(Set.of());
        Loan loan = loanTransaction.getLoan();
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.isClosedWrittenOff()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void partialPaymentsTakeTheTaxCumulativelyRoundedToTheCurrencyScale() {
        // fee 2952.00 carrying 552.00 tax: paid 0.01, then 1000.00, then 1951.99 -> 0.00 (no line), 186.99, 365.01
        LoanCharge fee = taxedCharge("2952.00", "552.000000");
        paidBefore(fee, "0");
        assertEquals(List.of(List.of()), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "0.01")));
        paidBefore(fee, "0.01");
        assertEquals(List.of(List.of("186.99")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "1000.00")));
        paidBefore(fee, "1000.01");
        assertEquals(List.of(List.of("365.01")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "1951.99")));
    }

    @Test
    void taxThatDoesNotDivideEvenlySumsToTheRoundedTax() {
        // fee 1000.00 carrying 186.991860: 400.00 then 600.00 -> 74.80 + 112.19 = 186.99
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("74.80")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "400.00")));
        paidBefore(fee, "400.00");
        assertEquals(List.of(List.of("112.19")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "600.00")));
    }

    @Test
    void theConfiguredRoundingModeIsUsed() {
        // 10.00 tax on 100.00, 0.05 paid: 0.005 exactly
        LoanCharge fee = taxedCharge("100.00", "10.000000");
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_DOWN);
        paidBefore(fee, "0");
        assertEquals(List.of(List.of()), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "0.05")));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_UP);
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("0.01")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "0.05")));
    }

    @Test
    void theCurrencyScaleIsUsed() {
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("75")), post(LoanTransactionType.REPAYMENT, 0, row(fee, null, "400.00")));
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("74.797")), post(LoanTransactionType.REPAYMENT, 3, row(fee, null, "400.00")));
    }

    @Test
    void theBaseIsReadForThePaymentStream() {
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "400.00");
        post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "600.00"));
        verify(loanTransactionRepository).findChargeAmountPaidByEarlierTransactions(eq(fee), eq(TRANSACTION_ID),
                argThat(types -> types.contains(LoanTransactionType.REPAYMENT) && types.contains(LoanTransactionType.CHARGE_PAYMENT)
                        && !types.contains(LoanTransactionType.ACCRUAL) && !types.contains(LoanTransactionType.WAIVE_CHARGES)));
    }

    @Test
    void anAccrualAdjustmentTakesBackTheTaxOfTheAmountItReverses() {
        // 1000.00 accrued (186.99 of tax), 400.00 adjusted: 186.99 - round(112.195116) = 186.99 - 112.20 = 74.79
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "1000.00");
        assertEquals(List.of(List.of("74.79")), post(LoanTransactionType.ACCRUAL_ADJUSTMENT, 2, row(fee, null, "400.00")));
        verify(loanTransactionRepository).findChargeAmountPaidByEarlierTransactions(eq(fee), eq(TRANSACTION_ID),
                eq(Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT)));
    }

    @Test
    void rowsOfOneChargeInOneTransactionAccumulateInInstalmentOrder() {
        // instalment fee 2 x 500.00 = 1000.00 carrying 186.991860, both instalments paid at once: 93.50 then 93.49
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("93.49"), List.of("93.50")),
                post(LoanTransactionType.REPAYMENT, 2, row(fee, 2, "500.00"), row(fee, 1, "500.00")));
    }

    @Test
    void eachTaxedChargeHasItsOwnBase() {
        LoanCharge first = taxedCharge("1000.00", "186.991860");
        LoanCharge second = taxedCharge("100.00", "18.699186");
        paidBefore(first, "400.00");
        paidBefore(second, "0");
        assertEquals(List.of(List.of("112.19"), List.of("9.35")),
                post(LoanTransactionType.REPAYMENT, 2, row(first, null, "600.00"), row(second, null, "50.00")));
        verify(loanTransactionRepository).findChargeAmountPaidByEarlierTransactions(eq(first), eq(TRANSACTION_ID), any());
        verify(loanTransactionRepository).findChargeAmountPaidByEarlierTransactions(eq(second), eq(TRANSACTION_ID), any());
    }

    @Test
    void eachComponentIsRoundedOnItsOwnAndOneWithoutAnAccountGetsNoLine() {
        LoanCharge fee = charge("1000.00", component("100.000000", TAX_ACCOUNT_ID), component("86.991870", OTHER_TAX_ACCOUNT_ID),
                component("5.000000", null));
        // 400.00 of 1000.00: 40.00 and round(34.796748) = 34.80
        paidBefore(fee, "0");
        assertEquals(List.of(List.of("40.00", "34.80")), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "400.00")));
    }

    @Test
    void anUntaxedChargeReadsNoBaseAndPostsNoTax() {
        LoanCharge untaxed = charge("1000.00");
        assertEquals(List.of(List.of()), post(LoanTransactionType.REPAYMENT, 2, row(untaxed, null, "400.00")));
        verify(loanTransactionRepository, never()).findChargeAmountPaidByEarlierTransactions(any(), anyLong(), any());
    }

    @Test
    void anUntaxedChargeBesideATaxedOneIsLeftAlone() {
        LoanCharge untaxed = charge("1000.00");
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        paidBefore(fee, "0");
        assertEquals(List.of(List.of(), List.of("74.80")),
                post(LoanTransactionType.REPAYMENT, 2, row(untaxed, null, "300.00"), row(fee, null, "400.00")));
        verify(loanTransactionRepository, never()).findChargeAmountPaidByEarlierTransactions(eq(untaxed), anyLong(), any());
    }

    @Test
    void aTransactionNoProcessorPostsTaxForReadsNoBaseAndCarriesNoTax() {
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        assertEquals(List.of(List.of()), post(LoanTransactionType.WAIVE_CHARGES, 2, row(fee, null, "400.00")));
        verify(loanTransactionRepository, never()).findChargeAmountPaidByEarlierTransactions(any(), anyLong(), any());
    }

    @Test
    void aReversedTransactionReadsNoBaseAndCarriesNoTax() {
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        when(loanTransaction.isReversed()).thenReturn(true);
        assertEquals(List.of(List.of()), post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "400.00")));
        verify(loanTransactionRepository, never()).findChargeAmountPaidByEarlierTransactions(any(), anyLong(), any());
    }

    @Test
    void anUnsavedTransactionIsRefusedRatherThanRoundedAgainstNothing() {
        LoanCharge fee = taxedCharge("1000.00", "186.991860");
        when(loanTransaction.getId()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> post(LoanTransactionType.REPAYMENT, 2, row(fee, null, "400.00")));
    }

    @Test
    void thePaymentStreamIsWhatTheAccountingProcessorsPostAsRepayments() {
        // the condition both accounting processors dispatch to createJournalEntriesForRepayments on: a type added there
        // must be added to the payment stream too, or its charge tax is silently not posted
        for (LoanTransactionType type : LoanTransactionType.values()) {
            LoanTransactionEnumData data = LoanEnumerations.transactionType(type);
            boolean postedAsRepayment = (data.isRepaymentType() && !data.isChargeAdjustment()) || data.isRepaymentAtDisbursement()
                    || data.isChargePayment();
            assertEquals(postedAsRepayment, LoanJournalEntryPosterImpl.PAYMENT_TAX_STREAM.contains(type), type.name());
        }
    }

    /**
     * Posts the transaction paying {@code rows} and returns, per row in the order given, the amounts of its tax lines.
     */
    private List<List<String>> post(LoanTransactionType type, int digits, LoanChargePaidBy... rows) {
        when(loanTransaction.getTypeOf()).thenReturn(type);
        when(loanTransaction.getLoan().getCurrency().getDigitsAfterDecimal()).thenReturn(digits);
        when(loanTransaction.getLoanChargesPaid()).thenReturn(new LinkedHashSet<>(List.of(rows)));

        underTest.postJournalEntriesForLoanTransaction(loanTransaction, false, false);

        ArgumentCaptor<AccountingBridgeDataDTO> captor = ArgumentCaptor.forClass(AccountingBridgeDataDTO.class);
        verify(journalEntryWritePlatformService, atLeastOnce()).createJournalEntriesForLoanTransaction(captor.capture(),
                anyBoolean());
        List<List<String>> taxes = new ArrayList<>();
        for (LoanChargePaidByDTO paid : captor.getValue().getNewLoanTransactions().get(0).getLoanChargesPaid()) {
            taxes.add(paid.getTaxDetails().stream().map(ChargeTaxDetailDTO::getAmount).map(BigDecimal::toPlainString).toList());
        }
        return taxes;
    }

    private void paidBefore(LoanCharge loanCharge, String amount) {
        when(loanTransactionRepository.findChargeAmountPaidByEarlierTransactions(eq(loanCharge), anyLong(), any()))
                .thenReturn(new BigDecimal(amount));
    }

    private LoanChargePaidBy row(LoanCharge loanCharge, Integer installmentNumber, String amount) {
        return new LoanChargePaidBy(loanTransaction, loanCharge, new BigDecimal(amount), installmentNumber);
    }

    private static LoanCharge taxedCharge(String amount, String tax) {
        return charge(amount, component(tax, TAX_ACCOUNT_ID));
    }

    private static LoanCharge charge(String amount, LoanChargeTaxDetails... taxDetails) {
        LoanCharge loanCharge = mock(LoanCharge.class, RETURNS_DEEP_STUBS);
        when(loanCharge.getAmount()).thenReturn(new BigDecimal(amount));
        when(loanCharge.getTaxDetails()).thenReturn(List.of(taxDetails));
        return loanCharge;
    }

    private static LoanChargeTaxDetails component(String tax, Long creditAccountId) {
        LoanChargeTaxDetails taxDetail = mock(LoanChargeTaxDetails.class, RETURNS_DEEP_STUBS);
        when(taxDetail.getAmount()).thenReturn(new BigDecimal(tax));
        if (creditAccountId == null) {
            when(taxDetail.getTaxComponent().getCreditAccount()).thenReturn(null);
        } else {
            when(taxDetail.getTaxComponent().getCreditAccount().getId()).thenReturn(creditAccountId);
        }
        return taxDetail;
    }
}
