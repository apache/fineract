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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Pins the recovery payment request contract, which mirrors the rules term loan applies to
 * {@code ?command=recoverypayment} with one deliberate divergence.
 * <p>
 * Term loan caps each recovery against {@code LoanSummary#getTotalWrittenOff()}, the GROSS amount written off, which is
 * never reduced by the recoveries collected against it. Two recoveries of the full written-off amount therefore both
 * pass on their own and together collect twice the loss. Working Capital caps against what is still recoverable
 * instead, so the recoveries cannot add up past the loss that was booked.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorRecoveryPaymentTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final LocalDate WRITE_OFF_DATE = LocalDate.of(2026, 2, 20);

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private WorkingCapitalLoan loan;

    private WorkingCapitalLoanBalance balance;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, transactionFinder, null, null, null);

        // A loan written off for 100, with nothing recovered yet.
        balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipalWrittenOff(new BigDecimal("100"));

        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);
        lenient().when(loan.isClosedWrittenOff()).thenReturn(true);
        lenient().when(loan.getBalance()).thenReturn(balance);
        lenient().when(transactionFinder.getLastUserTransactionDate(loan)).thenReturn(Optional.of(WRITE_OFF_DATE));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldAcceptARecoveryOnTheBusinessDateWithinTheWrittenOffAmount() {
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "40"), loan));
    }

    @Test
    void shouldAcceptARecoveryForExactlyTheRemainingAmount() {
        balance.setTotalRecovered(new BigDecimal("60"));
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "40"), loan));
    }

    @Test
    void shouldRejectASecondRecoveryThatWouldExceedWhatWasWrittenOff() {
        // The divergence from term loan: 100 was written off, 60 already recovered, so only 40 is left. Term loan
        // would still compare 50 against the gross 100 and let it through.
        balance.setTotalRecovered(new BigDecimal("60"));
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "50"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.greater.than.remaining.written.off.amount"));
    }

    @Test
    void shouldRejectARecoveryLargerThanTheWrittenOffAmount() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "101"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.greater.than.remaining.written.off.amount"));
    }

    @Test
    void shouldRejectARecoveryOnALoanThatIsNotWrittenOff() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.isClosedWrittenOff()).thenReturn(false);
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "40"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("error.msg.wc.loan.is.not.written.off"));
    }

    @Test
    void shouldRejectARecoveryInTheFuture() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE.plusDays(1), "40"), loan));
        assertThat(ex.getErrors()).anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.a.future.date"));
    }

    @Test
    void shouldRejectARecoveryDatedBeforeTheWriteOff() {
        // The write-off is a user transaction, so the "not before the last transaction" rule keeps a recovery from
        // predating the write-off that made it possible.
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(WRITE_OFF_DATE.minusDays(1), "40"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.before.last.transaction.date"));
    }

    @Test
    void shouldAcceptARecoveryOnTheWriteOffDateItself() {
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(recoveryCommand(WRITE_OFF_DATE, "40"), loan));
    }

    @Test
    void shouldRejectAZeroOrNegativeAmount() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(recoveryCommand(BUSINESS_DATE, "0"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getParameterName().equals(WorkingCapitalLoanConstants.transactionAmountParamName));
    }

    @Test
    void shouldRejectAMissingAmount() {
        final JsonObject json = recoveryJson(BUSINESS_DATE);
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(command(json), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getParameterName().equals(WorkingCapitalLoanConstants.transactionAmountParamName));
    }

    @Test
    void shouldRejectTheRepaymentClassificationParameter() {
        // A recovery is not a repayment: it carries no allocation, so a repayment classification has nothing to
        // classify and must not be silently ignored.
        final JsonObject json = recoveryJson(BUSINESS_DATE);
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, 40);
        json.addProperty(WorkingCapitalLoanConstants.classificationIdParamName, 3L);
        final UnsupportedParameterException ex = assertThrows(UnsupportedParameterException.class,
                () -> validator.validateRecoveryPayment(command(json), loan));
        assertThat(ex.getUnsupportedParameters()).contains(WorkingCapitalLoanConstants.classificationIdParamName);
    }

    @Test
    void shouldRejectUndoWriteOffOnceARecoveryHasBeenCollected() {
        // Undoing the write-off would restore the full outstanding while the recovered cash stays booked as income,
        // so the same money would be counted twice. The recoveries have to be reversed first.
        balance.setTotalRecovered(new BigDecimal("40"));
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUndoWriteOff(command(json), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.undo.write.off.with.recovery.payments"));
    }

    @Test
    void shouldAllowUndoWriteOffWhenNothingHasBeenRecovered() {
        assertDoesNotThrow(() -> validator.validateUndoWriteOff(command(new JsonObject()), loan));
    }

    private JsonCommand recoveryCommand(final LocalDate transactionDate, final String amount) {
        final JsonObject json = recoveryJson(transactionDate);
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal(amount));
        return command(json);
    }

    private JsonObject recoveryJson(final LocalDate transactionDate) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate.toString());
        return json;
    }

    private JsonCommand command(final JsonObject json) {
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
