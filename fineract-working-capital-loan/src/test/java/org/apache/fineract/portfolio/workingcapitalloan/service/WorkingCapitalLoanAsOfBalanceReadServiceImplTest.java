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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanAsOfBalanceData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the as-of-date balance: which events count toward what is owed on a given date, and which deliberately do not.
 *
 * <p>
 * The rule under test is asymmetric on purpose. The charged side is scoped to the date, so nothing that was disbursed,
 * discounted or charged afterwards is quoted. The paid side is not, so the quote stays the amount that closes the loan.
 */
@ExtendWith(MockitoExtension.class)
public class WorkingCapitalLoanAsOfBalanceReadServiceImplTest {

    private static final Long LOAN_ID = 7L;
    private static final LocalDate AS_OF = LocalDate.of(2026, 1, 15);

    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;

    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;

    @InjectMocks
    private WorkingCapitalLoanAsOfBalanceReadServiceImpl service;

    private WorkingCapitalLoanBalance balance;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        balance = WorkingCapitalLoanBalance.createFor(mock(WorkingCapitalLoan.class));
    }

    @AfterEach
    public void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    @DisplayName("A date before the disbursement is quoted at zero: there was nothing to pay off yet")
    public void quotesNothingBeforeTheLoanWasDisbursed() {
        // The stored balance says 9000 is outstanding today, but on the quoted date nothing had been paid out.
        balance.setPrincipal(amount("9000"));
        givenPrincipalTransactions(BigDecimal.ZERO, BigDecimal.ZERO);
        givenNoCharges();

        final WorkingCapitalLoanAsOfBalanceData asOf = retrieve();

        assertThat(asOf.principalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(asOf.totalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("The discount that rides on the disbursement is part of the principal, and follows the same date")
    public void includesTheDiscountFeeThatRidesOnTheDisbursement() {
        // applyDisbursement folds the discount into principal, and a discount fee transaction exists for it, so the
        // two summed together have to reproduce that same principal - no more, no less.
        balance.setPrincipal(amount("9500"));
        givenPrincipalTransactions(amount("9500"), BigDecimal.ZERO);
        givenNoCharges();

        assertThat(retrieve().principalOutstanding()).isEqualByComparingTo("9500");
    }

    @Test
    @DisplayName("A discount fee adjustment dated after the quote has not yet reduced the principal")
    public void excludesADiscountFeeAdjustmentDatedAfterTheQuote() {
        balance.setPrincipal(amount("9000"));
        givenPrincipalTransactions(amount("9500"), BigDecimal.ZERO);
        givenNoCharges();

        assertThat(retrieve().principalOutstanding()).isEqualByComparingTo("9500");
    }

    @Test
    @DisplayName("A discount fee adjustment dated on or before the quote has reduced the principal")
    public void appliesADiscountFeeAdjustmentDatedBeforeTheQuote() {
        givenPrincipalTransactions(amount("9500"), amount("500"));
        givenNoCharges();

        assertThat(retrieve().principalOutstanding()).isEqualByComparingTo("9000");
    }

    @Test
    @DisplayName("A fee submitted after the quote date is left out, and one submitted on it is included")
    public void scopesFeesToTheQuoteDate() {
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        when(chargeRepository.sumChargedKnownBy(eq(LOAN_ID), eq(false), eq(AS_OF))).thenReturn(amount("40"));
        when(chargeRepository.sumChargedKnownBy(eq(LOAN_ID), eq(true), eq(AS_OF))).thenReturn(BigDecimal.ZERO);

        final WorkingCapitalLoanAsOfBalanceData asOf = retrieve();

        // The 40 in scope is quoted; anything the repository excluded by date never reaches the total.
        assertThat(asOf.feeOutstanding()).isEqualByComparingTo("40");
        assertThat(asOf.totalOutstanding()).isEqualByComparingTo("9040");
    }

    @Test
    @DisplayName("Penalties are scoped to the quote date in their own right, not folded in with fees")
    public void scopesPenaltiesToTheQuoteDateSeparately() {
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        when(chargeRepository.sumChargedKnownBy(eq(LOAN_ID), eq(false), eq(AS_OF))).thenReturn(BigDecimal.ZERO);
        when(chargeRepository.sumChargedKnownBy(eq(LOAN_ID), eq(true), eq(AS_OF))).thenReturn(amount("25"));

        final WorkingCapitalLoanAsOfBalanceData asOf = retrieve();

        assertThat(asOf.penaltyOutstanding()).isEqualByComparingTo("25");
        assertThat(asOf.feeOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(asOf.totalOutstanding()).isEqualByComparingTo("9025");
    }

    @Test
    @DisplayName("Payments are not scoped to the date: the quote stays net of every repayment ever made")
    public void keepsTheQuoteNetOfPaymentsMadeAfterTheDate() {
        // This is what stops a prepayment backdated behind an existing payment closing the loan and then overpaying it.
        balance.setPrincipalPaid(amount("1000"));
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        givenNoCharges();

        assertThat(retrieve().principalOutstanding()).isEqualByComparingTo("8000");
    }

    @Test
    @DisplayName("A charge already paid off but not yet submitted on the quote date leaves no negative residue")
    public void floorsABucketPaidBeyondWhatWasChargedByThatDate() {
        // The payment is counted but the charge it settled is not yet in scope, so the bucket would otherwise go
        // negative and eat into the total.
        balance.setFeePaid(amount("40"));
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        givenNoCharges();

        final WorkingCapitalLoanAsOfBalanceData asOf = retrieve();

        assertThat(asOf.feeOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(asOf.totalOutstanding()).isEqualByComparingTo("9000");
    }

    @Test
    @DisplayName("Written-off and recovered amounts are carried through unchanged, as they are not date-scoped")
    public void carriesThroughTheWrittenOffSide() {
        balance.setPrincipal(amount("9000"));
        balance.setPrincipalWrittenOff(amount("9000"));
        balance.setTotalRecovered(amount("200"));
        balance.setOverpaymentAmount(amount("15"));
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        givenNoCharges();

        final WorkingCapitalLoanAsOfBalanceData asOf = retrieve();

        assertThat(asOf.principalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(asOf.writtenOffOutstanding()).isEqualByComparingTo("8800");
        assertThat(asOf.overpaymentAmount()).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("A loan with no balance row is reported as absent, not as a balance of zero")
    public void reportsAnAbsentBalanceAsEmpty() {
        assertThat(service.retrieveAsOf(LOAN_ID, null, AS_OF)).isEmpty();
    }

    @Test
    @DisplayName("Only the disbursement and discount fee transactions feed the principal")
    public void readsOnlyThePrincipalBearingTransactionTypes() {
        givenPrincipalTransactions(amount("9000"), BigDecimal.ZERO);
        givenNoCharges();

        retrieve();

        // A repayment must never be summed into the charged side - it belongs to the paid side, which is not scoped.
        final Collection<LoanTransactionType> increasing = List.of(LoanTransactionType.DISBURSEMENT, LoanTransactionType.DISCOUNT_FEE);
        final Collection<LoanTransactionType> decreasing = List.of(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT);
        org.mockito.Mockito.verify(transactionRepository).sumAmountsOfTypesUpTo(LOAN_ID, increasing, AS_OF);
        org.mockito.Mockito.verify(transactionRepository).sumAmountsOfTypesUpTo(LOAN_ID, decreasing, AS_OF);
    }

    private WorkingCapitalLoanAsOfBalanceData retrieve() {
        final Optional<WorkingCapitalLoanAsOfBalanceData> result = service.retrieveAsOf(LOAN_ID, balance, AS_OF);
        assertThat(result).isPresent();
        return result.get();
    }

    private void givenPrincipalTransactions(final BigDecimal increasing, final BigDecimal decreasing) {
        when(transactionRepository.sumAmountsOfTypesUpTo(eq(LOAN_ID),
                eq(List.of(LoanTransactionType.DISBURSEMENT, LoanTransactionType.DISCOUNT_FEE)), eq(AS_OF))).thenReturn(increasing);
        when(transactionRepository.sumAmountsOfTypesUpTo(eq(LOAN_ID), eq(List.of(LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT)), eq(AS_OF)))
                .thenReturn(decreasing);
    }

    private void givenNoCharges() {
        when(chargeRepository.sumChargedKnownBy(eq(LOAN_ID), anyBoolean(), any())).thenReturn(BigDecimal.ZERO);
    }

    private static BigDecimal amount(final String value) {
        return new BigDecimal(value);
    }
}
