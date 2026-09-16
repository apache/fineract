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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

/**
 * Pins the derived status and disbursement helpers that {@code WorkingCapitalLoan} mirrors from {@code Loan}. The
 * helpers are not observable through the API - the entity never reaches Jackson, the read side serialises
 * {@code WorkingCapitalLoanData} - so this is the only place their contract can be stated; the Feign suite
 * {@code FeignWorkingCapitalLoanStatusHelpersTest} guards that the call sites using them still behave the same.
 *
 * <p>
 * The expected values are the truth table of the mirrored {@code Loan} methods and of {@code LoanStatus}
 * (fineract-core), the shared enum both entities hold, which decides which status satisfies which predicate.
 *
 * <p>
 * Three deliberate deviations from {@code Loan}:
 * <ul>
 * <li>{@code isDisbursed()} is <b>disbursement-details</b>-based, not transaction-based - {@code WorkingCapitalLoan}
 * has no transaction collection, and this is the semantics already hard-coded in five places (the COB steps and the two
 * action validators).</li>
 * <li>{@code getFirstActualDisbursementDate()} returns the <b>earliest</b> non-null actual date ({@code min}).
 * {@code disbursementDetails} is an unordered bag, so a list-order contract would not be one JPA can keep. Multiple
 * rows are reachable when activation appends an actual-disbursement row after the expected-date row; the tests below
 * pin both the null-row case and the chronological guarantee.</li>
 * <li>{@code isMatured(LocalDate)} is deliberately <b>not</b> ported - WC's {@code maturedOnDate} means "paid off", not
 * "schedule maturity", so the {@code Loan} name would carry the wrong semantics.</li>
 * </ul>
 *
 * <p>
 * All status predicates must be null-safe on {@code loanStatus}: the {@code WorkingCapitalLoanDataValidator} call sites
 * these helpers replaced guarded for a null status inline, and a bare delegation would turn those rejections into 500s.
 */
class WorkingCapitalLoanStatusHelpersTest {

    private static final LocalDate EARLIER = LocalDate.of(2026, 3, 2);
    private static final LocalDate LATER = LocalDate.of(2026, 4, 15);

    /**
     * {@code getPrincipal()} builds a {@link org.apache.fineract.organisation.monetary.domain.Money}, and
     * {@code Money.of} reads the rounding configuration off the tenant - exactly as {@code Loan.getPrincipal()} does
     * through {@code LoanProductRelatedDetail}. There is no tenant in a unit test, so the helper is stubbed.
     */
    private static MockedStatic<MoneyHelper> moneyHelper;

    @BeforeAll
    static void stubTenantRoundingConfiguration() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void releaseStub() {
        moneyHelper.close();
    }

    @Nested
    @DisplayName("status predicates - exactly one status satisfies each")
    class StatusPredicates {

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isSubmittedAndPendingApproval is true only for SUBMITTED_AND_PENDING_APPROVAL")
        void isSubmittedAndPendingApproval(final LoanStatus status) {
            assertThat(loanWithStatus(status).isSubmittedAndPendingApproval())
                    .isEqualTo(status == LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        }

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isNotSubmittedAndPendingApproval is the exact negation")
        void isNotSubmittedAndPendingApproval(final LoanStatus status) {
            final WorkingCapitalLoan loan = loanWithStatus(status);
            assertThat(loan.isNotSubmittedAndPendingApproval()).isEqualTo(!loan.isSubmittedAndPendingApproval());
            assertThat(loan.isNotSubmittedAndPendingApproval()).isEqualTo(status != LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        }

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isApproved is true only for APPROVED")
        void isApproved(final LoanStatus status) {
            assertThat(loanWithStatus(status).isApproved()).isEqualTo(status == LoanStatus.APPROVED);
        }

        /**
         * {@code Loan.isOpen()} is {@code status.isActive()} - the name is Loan's, the meaning is "active". This is the
         * single name used across the module, replacing the inline {@code isActive()} / {@code != ACTIVE} spellings.
         */
        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isOpen is true only for ACTIVE")
        void isOpen(final LoanStatus status) {
            assertThat(loanWithStatus(status).isOpen()).isEqualTo(status == LoanStatus.ACTIVE);
        }

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isClosedObligationsMet is true only for CLOSED_OBLIGATIONS_MET")
        void isClosedObligationsMet(final LoanStatus status) {
            assertThat(loanWithStatus(status).isClosedObligationsMet()).isEqualTo(status == LoanStatus.CLOSED_OBLIGATIONS_MET);
        }

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isClosedWrittenOff is true only for CLOSED_WRITTEN_OFF")
        void isClosedWrittenOff(final LoanStatus status) {
            assertThat(loanWithStatus(status).isClosedWrittenOff()).isEqualTo(status == LoanStatus.CLOSED_WRITTEN_OFF);
        }

        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isOverpaid is true only for OVERPAID")
        void isOverpaid(final LoanStatus status) {
            assertThat(loanWithStatus(status).isOverpaid()).isEqualTo(status == LoanStatus.OVERPAID);
        }

        /**
         * {@code Loan.isCancelled()} is {@code isRejected() || isWithdrawn()}. WC's state machine has no transition to
         * {@code WITHDRAWN_BY_CLIENT} today, so only the rejected half is reachable - the two-term definition is kept
         * so the predicate stays correct if withdrawal is ever added.
         */
        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isCancelled is true for REJECTED and WITHDRAWN_BY_CLIENT")
        void isCancelled(final LoanStatus status) {
            final Set<LoanStatus> cancelled = EnumSet.of(LoanStatus.REJECTED, LoanStatus.WITHDRAWN_BY_CLIENT);
            assertThat(loanWithStatus(status).isCancelled()).isEqualTo(cancelled.contains(status));
        }

        /**
         * {@code Loan.isClosed()} is {@code status.isClosed() || isCancelled()}, and {@code LoanStatus.isClosed()}
         * covers the three closed states - so the cancelled pair is included too. This is the widest of the status
         * predicates and the easiest to get wrong by porting only {@code status.isClosed()}.
         */
        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("isClosed covers the three closed states plus the two cancelled states")
        void isClosed(final LoanStatus status) {
            final Set<LoanStatus> closed = EnumSet.of(LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.CLOSED_WRITTEN_OFF,
                    LoanStatus.CLOSED_RESCHEDULE_OUTSTANDING_AMOUNT, LoanStatus.REJECTED, LoanStatus.WITHDRAWN_BY_CLIENT);
            assertThat(loanWithStatus(status).isClosed()).isEqualTo(closed.contains(status));
        }
    }

    /**
     * A null {@code loanStatus} must answer false everywhere rather than throwing. The column is
     * {@code nullable = false}, but the {@code WorkingCapitalLoanDataValidator} call sites these helpers replaced
     * treated null as a real case, as does {@code WorkingCapitalLoanLifecycleStateMachine}, and preserving that
     * behaviour is what keeps the call-site migration a no-op.
     */
    @Test
    @DisplayName("every status predicate is null-safe on loanStatus")
    void statusPredicatesAreNullSafe() {
        final WorkingCapitalLoan loan = loanWithStatus(null);

        assertThat(loan.isSubmittedAndPendingApproval()).isFalse();
        assertThat(loan.isNotSubmittedAndPendingApproval()).isTrue();
        assertThat(loan.isApproved()).isFalse();
        assertThat(loan.isOpen()).isFalse();
        assertThat(loan.isClosed()).isFalse();
        assertThat(loan.isClosedObligationsMet()).isFalse();
        assertThat(loan.isClosedWrittenOff()).isFalse();
        assertThat(loan.isOverpaid()).isFalse();
        assertThat(loan.isCancelled()).isFalse();
    }

    @Nested
    @DisplayName("disbursement predicates - driven by the detail rows, not by the status")
    class DisbursementPredicates {

        @Test
        @DisplayName("no detail rows at all: not disbursed, no actual disbursement date")
        void noDetails() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);

            assertThat(loan.isDisbursed()).isFalse();
            assertThat(loan.isNotDisbursed()).isTrue();
            assertThat(loan.getFirstActualDisbursementDate()).isNull();
        }

        @Test
        @DisplayName("a detail row whose actual disbursement date is still null: not disbursed")
        void detailWithoutActualDate() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.APPROVED);
            loan.getDisbursementDetails().add(disbursementDetail(null));

            assertThat(loan.isDisbursed()).isFalse();
            assertThat(loan.isNotDisbursed()).isTrue();
            assertThat(loan.getFirstActualDisbursementDate()).isNull();
        }

        @Test
        @DisplayName("a detail row with an actual disbursement date: disbursed, and that date is returned")
        void detailWithActualDate() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            loan.getDisbursementDetails().add(disbursementDetail(EARLIER));

            assertThat(loan.isDisbursed()).isTrue();
            assertThat(loan.isNotDisbursed()).isFalse();
            assertThat(loan.getFirstActualDisbursementDate()).isEqualTo(EARLIER);
        }

        /**
         * The status must not enter into it: a written-off loan is {@code CLOSED_WRITTEN_OFF} yet certainly disbursed,
         * and the COB steps have to keep treating it as disbursed. A status-based port of {@code isDisbursed()} would
         * fail exactly here.
         */
        @Test
        @DisplayName("a written-off loan with a disbursed detail row is still disbursed")
        void writtenOffLoanIsStillDisbursed() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.CLOSED_WRITTEN_OFF);
            loan.getDisbursementDetails().add(disbursementDetail(EARLIER));

            assertThat(loan.isDisbursed()).isTrue();
        }

        @Test
        @DisplayName("undoing a disbursal clears the date on the row, so the loan is not disbursed again")
        void clearedActualDateMakesLoanUndisbursed() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            final WorkingCapitalLoanDisbursementDetails detail = disbursementDetail(EARLIER);
            loan.getDisbursementDetails().add(detail);

            detail.setActualDisbursementDate(null);

            assertThat(loan.isDisbursed()).isFalse();
            assertThat(loan.getFirstActualDisbursementDate()).isNull();
        }

        @Test
        @DisplayName("rows carrying no actual date are skipped")
        void skipsRowsWithoutAnActualDate() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            loan.getDisbursementDetails().add(disbursementDetail(null));
            loan.getDisbursementDetails().add(disbursementDetail(LATER));

            assertThat(loan.isDisbursed()).isTrue();
            assertThat(loan.getFirstActualDisbursementDate()).isEqualTo(LATER);
        }

        /**
         * {@code disbursementDetails} is a plain {@code @OneToMany} with neither {@code @OrderBy} nor
         * {@code @OrderColumn}, so JPA guarantees no iteration order. The accessor therefore promises the earliest date
         * rather than the first one encountered: with the later date added first, chronology still decides.
         */
        @Test
        @DisplayName("chronology decides, not list order: the earliest non-null date wins whatever the row order")
        void earliestWins() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            loan.getDisbursementDetails().add(disbursementDetail(LATER));
            loan.getDisbursementDetails().add(disbursementDetail(EARLIER));

            assertThat(loan.getFirstActualDisbursementDate()).isEqualTo(EARLIER);
        }

        @Test
        @DisplayName("first disbursement amount follows the earliest actual disbursement and ignores undisbursed rows")
        void firstDisbursementAmountUsesEarliestActualDetail() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            final WorkingCapitalLoanDisbursementDetails later = disbursementDetail(LATER);
            later.setActualAmount(new BigDecimal("275.00"));
            final WorkingCapitalLoanDisbursementDetails earlier = disbursementDetail(EARLIER);
            earlier.setActualAmount(new BigDecimal("125.00"));
            final WorkingCapitalLoanDisbursementDetails expectedOnly = disbursementDetail(null);
            expectedOnly.setActualAmount(new BigDecimal("999.00"));
            loan.getDisbursementDetails().add(later);
            loan.getDisbursementDetails().add(expectedOnly);
            loan.getDisbursementDetails().add(earlier);

            assertThat(loan.getFirstActualDisbursementAmount()).isEqualByComparingTo("125.00");
        }

        @Test
        @DisplayName("no actual disbursement amount returns zero")
        void firstDisbursementAmountDefaultsToZero() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.APPROVED);
            loan.getDisbursementDetails().add(disbursementDetail(null));

            assertThat(loan.getFirstActualDisbursementAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("same-date disbursements use the detail ID as a deterministic tie-breaker")
        void sameDateDisbursementsUseDetailIdAsTieBreaker() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            final WorkingCapitalLoanDisbursementDetails higherId = disbursementDetail(EARLIER);
            higherId.setId(20L);
            higherId.setActualAmount(new BigDecimal("275.00"));
            final WorkingCapitalLoanDisbursementDetails lowerId = disbursementDetail(EARLIER);
            lowerId.setId(10L);
            lowerId.setActualAmount(new BigDecimal("125.00"));
            loan.getDisbursementDetails().add(higherId);
            loan.getDisbursementDetails().add(lowerId);

            assertThat(loan.getFirstActualDisbursement()).isSameAs(lowerId);
            assertThat(loan.getFirstActualDisbursementAmount()).isEqualByComparingTo("125.00");
        }
    }

    @Nested
    @DisplayName("delegating accessors - the Loan names for state WC keeps somewhere else")
    class DelegatingAccessors {

        /**
         * {@code Loan} exposes the status as {@code getStatus()}; the WC field is {@code loanStatus}, so Lombok
         * generates {@code getLoanStatus()}. Both must answer the same value, or the {@code Loan}-shaped call sites
         * this parity work targets would read a different field.
         */
        @ParameterizedTest
        @EnumSource(LoanStatus.class)
        @DisplayName("getStatus is the Lombok-generated getLoanStatus under the Loan name")
        void getStatusMatchesGetLoanStatus(final LoanStatus status) {
            final WorkingCapitalLoan loan = loanWithStatus(status);

            assertThat(loan.getStatus()).isEqualTo(status).isEqualTo(loan.getLoanStatus());
        }

        @Test
        @DisplayName("currency, currency code and principal come from loanProductRelatedDetails")
        void moneyAccessorsReadTheProductRelatedDetails() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            loan.getLoanProductRelatedDetails().setPrincipal(new BigDecimal("1500.00"));

            assertThat(loan.getCurrency()).isSameAs(loan.getLoanProductRelatedDetails().getCurrency());
            assertThat(loan.getCurrencyCode()).isEqualTo("USD");
            assertThat(loan.getPrincipal().getAmount()).isEqualByComparingTo("1500.00");
            assertThat(loan.getPrincipal().getCurrencyCode()).isEqualTo("USD");
        }

        /**
         * {@code loanProductRelatedDetails} is only populated once the application is assembled, and the mappers read
         * these accessors off half-built loans in tests, so a missing detail block must answer null rather than throw.
         */
        @Test
        @DisplayName("the money accessors are null-safe on loanProductRelatedDetails")
        void moneyAccessorsAreNullSafe() {
            final WorkingCapitalLoan loan = new WorkingCapitalLoan();

            assertThat(loan.getCurrency()).isNull();
            assertThat(loan.getCurrencyCode()).isNull();
            assertThat(loan.getPrincipal()).isNull();
        }

        /**
         * A null principal is the state between submission and the first {@code setPrincipal}; {@code Money} maps it to
         * zero rather than throwing, and the accessor must not add a null of its own on top.
         */
        @Test
        @DisplayName("a null principal reads as zero money, not as null")
        void nullPrincipalIsZeroMoney() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

            assertThat(loan.getPrincipal().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        /**
         * {@code Loan.getOffice()} falls back to the group when there is no client. WC is client-only - the module has
         * no {@code Group} reference at all - so the accessor answers null instead of dereferencing a group that cannot
         * exist.
         */
        @Test
        @DisplayName("productId and getOffice unwrap the product and the client office, null-safely")
        void productAndOfficeAccessors() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);

            assertThat(loan.productId()).isNull();
            assertThat(loan.getOffice()).isNull();
            assertThat(loan.getOfficeId()).isNull();

            final Office office = mock(Office.class);
            final Client client = mock(Client.class);
            when(client.getOffice()).thenReturn(office);
            loan.setClient(client);

            assertThat(loan.getOffice()).isSameAs(office);
        }
    }

    @Nested
    @DisplayName("getDisbursedLoanDisbursementDetails - only the rows that were actually disbursed")
    class DisbursedDetailRows {

        @Test
        @DisplayName("no rows at all: empty, never null")
        void noRows() {
            assertThat(loanWithStatus(LoanStatus.APPROVED).getDisbursedLoanDisbursementDetails()).isEmpty();
        }

        @Test
        @DisplayName("rows without an actual disbursement date are filtered out")
        void filtersUndisbursedRows() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            final WorkingCapitalLoanDisbursementDetails disbursed = disbursementDetail(EARLIER);
            loan.getDisbursementDetails().add(disbursementDetail(null));
            loan.getDisbursementDetails().add(disbursed);

            assertThat(loan.getDisbursedLoanDisbursementDetails()).containsExactly(disbursed);
        }

        /**
         * The predicate and the row list must agree: anything else would let a caller branch on {@code isDisbursed()}
         * and then find nothing to work with.
         */
        @Test
        @DisplayName("it is non-empty exactly when isDisbursed is true")
        void agreesWithIsDisbursed() {
            final WorkingCapitalLoan loan = loanWithStatus(LoanStatus.ACTIVE);
            assertThat(loan.getDisbursedLoanDisbursementDetails().isEmpty()).isEqualTo(loan.isNotDisbursed());

            loan.getDisbursementDetails().add(disbursementDetail(LATER));
            assertThat(loan.getDisbursedLoanDisbursementDetails().isEmpty()).isEqualTo(loan.isNotDisbursed());
        }
    }

    // --- fixtures --------------------------------------------------------------------------------------------------

    private static WorkingCapitalLoan loanWithStatus(final LoanStatus status) {
        final WorkingCapitalLoan loan = new WorkingCapitalLoan();
        loan.setLoanStatus(status);
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setCurrency(new MonetaryCurrency("USD", 2, 1));
        loan.setLoanProductRelatedDetails(details);
        return loan;
    }

    private static WorkingCapitalLoanDisbursementDetails disbursementDetail(final LocalDate actualDisbursementDate) {
        final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
        detail.setActualDisbursementDate(actualDisbursementDate);
        return detail;
    }
}
