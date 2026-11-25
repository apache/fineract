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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Pins the transaction date rules of a credit balance refund, which follow the term/progressive loan: the refund may be
 * backdated, but not into the future, not before the disbursement, not before the last repayment-like, refund or credit
 * balance refund transaction, and not before the charge-off date of a charged-off loan. On top of the term/progressive
 * rules, it may not be dated before the last charge accrual, so a full refund never closes the loan before income it
 * has already recognized, nor before the last charge was added, so it never closes the loan before an obligation it
 * has.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorCreditBalanceRefundDateTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 10);
    private static final LocalDate LAST_REPAYMENT_DATE = LocalDate.of(2026, 2, 20);
    private static final LocalDate CHARGE_OFF_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate LAST_ACCRUAL_DATE = LocalDate.of(2026, 3, 5);
    private static final LocalDate LAST_CHARGE_SUBMITTED_ON_DATE = LocalDate.of(2026, 3, 8);

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private WorkingCapitalLoanChargeRepository chargeRepository;
    @Mock
    private WorkingCapitalLoan loan;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, transactionFinder, null, null, null,
                chargeRepository);
        lenient().when(loan.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
        lenient().when(loan.getFirstActualDisbursementDate()).thenReturn(DISBURSEMENT_DATE);
        lenient().when(loan.isChargedOff()).thenReturn(false);
        lenient().when(loan.getId()).thenReturn(1L);
        lenient().when(transactionFinder.findLastRepaymentOrRefundDate(loan)).thenReturn(Optional.of(LAST_REPAYMENT_DATE));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldAcceptCreditBalanceRefundOnTheBusinessDate() {
        assertDoesNotThrow(() -> validate(BUSINESS_DATE));
    }

    @Test
    void shouldAcceptBackdatedCreditBalanceRefundAfterTheLastRepayment() {
        assertDoesNotThrow(() -> validate(LAST_REPAYMENT_DATE.plusDays(1)));
    }

    @Test
    void shouldAcceptCreditBalanceRefundOnTheSameDateAsTheLastRepayment() {
        // The rule is "not before", so same-day is allowed, as Loan#possibleNextRefundDate() allows it.
        assertDoesNotThrow(() -> validate(LAST_REPAYMENT_DATE));
    }

    @Test
    void shouldAcceptBackdatedCreditBalanceRefundWhenThereIsNoRepaymentOrRefund() {
        when(transactionFinder.findLastRepaymentOrRefundDate(loan)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> validate(DISBURSEMENT_DATE));
    }

    @Test
    void shouldRejectCreditBalanceRefundBeforeTheLastRepaymentOrRefund() {
        assertRejected(LAST_REPAYMENT_DATE.minusDays(1), "cannot.be.before.last.repayment.or.refund.date");
    }

    @Test
    void shouldRejectCreditBalanceRefundInTheFuture() {
        assertRejected(BUSINESS_DATE.plusDays(1), "cannot.be.a.future.date");
    }

    @Test
    void shouldRejectCreditBalanceRefundBeforeTheDisbursement() {
        when(transactionFinder.findLastRepaymentOrRefundDate(loan)).thenReturn(Optional.empty());
        assertRejected(DISBURSEMENT_DATE.minusDays(1), "cannot.be.before.disbursal.date");
    }

    @Test
    void shouldRejectCreditBalanceRefundBeforeTheChargeOffDate() {
        // A repayment-like transaction is normally what makes a charged-off loan overpaid, and it cannot predate the
        // charge-off without lifting it, so the floor is checked here on its own to pin the charge-off rule.
        when(transactionFinder.findLastRepaymentOrRefundDate(loan)).thenReturn(Optional.empty());
        when(loan.isChargedOff()).thenReturn(true);
        when(loan.getChargedOffOnDate()).thenReturn(CHARGE_OFF_DATE);
        assertRejected(CHARGE_OFF_DATE.minusDays(1), "cannot.be.before.charge.off.date");
    }

    @Test
    void shouldAcceptCreditBalanceRefundOnTheChargeOffDate() {
        when(loan.isChargedOff()).thenReturn(true);
        when(loan.getChargedOffOnDate()).thenReturn(CHARGE_OFF_DATE);
        assertDoesNotThrow(() -> validate(CHARGE_OFF_DATE));
    }

    @Test
    void shouldIgnoreTheChargeOffDateOnceTheChargeOffIsUndone() {
        when(transactionFinder.findLastRepaymentOrRefundDate(loan)).thenReturn(Optional.empty());
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.getChargedOffOnDate()).thenReturn(CHARGE_OFF_DATE);
        assertDoesNotThrow(() -> validate(CHARGE_OFF_DATE.minusDays(1)));
    }

    @Test
    void shouldRejectCreditBalanceRefundBeforeTheLastAccrual() {
        // A charge added to the overpaid loan after the last repayment is accrued on the day it is added; a full refund
        // dated before it would close the loan with that accrual left after the closing date.
        when(transactionFinder.findLastAccrualDate(loan)).thenReturn(Optional.of(LAST_ACCRUAL_DATE));
        assertRejected(LAST_ACCRUAL_DATE.minusDays(1), "cannot.be.before.last.accrual.date");
    }

    @Test
    void shouldAcceptCreditBalanceRefundOnTheLastAccrualDate() {
        when(transactionFinder.findLastAccrualDate(loan)).thenReturn(Optional.of(LAST_ACCRUAL_DATE));
        assertDoesNotThrow(() -> validate(LAST_ACCRUAL_DATE));
    }

    @Test
    void shouldRejectCreditBalanceRefundBeforeTheLastChargeWasAdded() {
        // Without accounting no accrual is posted, so the charge itself has to keep a full refund from closing the
        // loan before the charge existed.
        when(chargeRepository.findLatestSubmittedOnDateOfActiveCharges(1L)).thenReturn(Optional.of(LAST_CHARGE_SUBMITTED_ON_DATE));
        assertRejected(LAST_CHARGE_SUBMITTED_ON_DATE.minusDays(1), "cannot.be.before.last.charge.submitted.date");
    }

    @Test
    void shouldAcceptCreditBalanceRefundOnTheDateTheLastChargeWasAdded() {
        when(chargeRepository.findLatestSubmittedOnDateOfActiveCharges(1L)).thenReturn(Optional.of(LAST_CHARGE_SUBMITTED_ON_DATE));
        assertDoesNotThrow(() -> validate(LAST_CHARGE_SUBMITTED_ON_DATE));
    }

    @Test
    void shouldReportBothTheAccrualAndTheChargeFloorTogether() {
        when(transactionFinder.findLastAccrualDate(loan)).thenReturn(Optional.of(LAST_ACCRUAL_DATE));
        when(chargeRepository.findLatestSubmittedOnDateOfActiveCharges(1L)).thenReturn(Optional.of(LAST_ACCRUAL_DATE));
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validate(LAST_ACCRUAL_DATE.minusDays(1)));
        assertThat(ex.getErrors()).extracting(error -> error.getUserMessageGlobalisationCode()).anyMatch(
                code -> code.endsWith("cannot.be.before.last.accrual.date")).anyMatch(code -> code.endsWith("cannot.be.before.last.charge.submitted.date"));
    }

    private void validate(final LocalDate transactionDate) {
        validator.validateCreditBalanceRefund(json(transactionDate).toString(), loan);
    }

    private void assertRejected(final LocalDate transactionDate, final String expectedCode) {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validate(transactionDate));
        assertThat(ex.getErrors()).anyMatch(error -> error.getParameterName().equals(WorkingCapitalLoanConstants.transactionDateParamName)
                && error.getUserMessageGlobalisationCode().endsWith(expectedCode));
    }

    private static JsonObject json(final LocalDate transactionDate) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate.toString());
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal("10"));
        return json;
    }
}
