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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Pins the {@code ?command=adjust-by-delta} request contract: the amount is a signed delta on the adjusted
 * transaction's amount, so the delta itself must not be zero (a 400) and the resulting amount must stay at or above
 * zero (a domain rule, 403).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorAdjustByDeltaTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 2, 10);
    private static final String EXCEEDS_AMOUNT_CODE = "validation.msg.wc.loan.transaction.adjust.delta.exceeds.amount";

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction transaction;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, null, null, null, null);

        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        lenient().when(loan.getFirstActualDisbursementDate()).thenReturn(LocalDate.of(2026, 1, 1));
        lenient().when(transaction.getTransactionAmount()).thenReturn(new BigDecimal("100.000000"));
        lenient().when(transaction.isReversed()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldAcceptAnIncrease() {
        assertDoesNotThrow(() -> validator.validateAdjustByDelta(deltaCommand("40.00"), loan, transaction));
    }

    @Test
    void shouldAcceptAPartialDecrease() {
        assertDoesNotThrow(() -> validator.validateAdjustByDelta(deltaCommand("-40.00"), loan, transaction));
    }

    @Test
    void shouldAcceptADecreaseOfExactlyTheAmount() {
        assertDoesNotThrow(() -> validator.validateAdjustByDelta(deltaCommand("-100.00"), loan, transaction));
    }

    @Test
    void shouldRefuseADecreaseBeyondTheAmountAsADomainRuleNamingTheDeltaAndTheAmount() {
        final GeneralPlatformDomainRuleException ex = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> validator.validateAdjustByDelta(deltaCommand("-100.01"), loan, transaction));

        assertThat(ex.getGlobalisationMessageCode()).isEqualTo(EXCEEDS_AMOUNT_CODE);
        assertThat(ex.getDefaultUserMessage()).isEqualTo("Delta -100.01 exceeds the amount 100 of the transaction being adjusted.");
        assertThat(ex.getDefaultUserMessageArgs()).containsExactly(new BigDecimal("-100.01"), new BigDecimal("100.000000"));
    }

    @Test
    void shouldReportRequestErrorsBeforeTheOvershoot() {
        // A malformed request is a 400 even when its delta would also overshoot: the domain rule is only checked on a
        // request that is valid in itself.
        final JsonObject json = baseJson();
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal("-100.01"));
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, BUSINESS_DATE.plusDays(1).toString());

        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(command(json), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.transactionDate.cannot.be.a.future.date");
    }

    @Test
    void shouldRejectADeltaFinerThanTheCurrency() {
        when(loan.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(deltaCommand("-40.005"), loan, transaction));
        assertThat(ex.getErrors()).anyMatch(error -> error.getParameterName().equals(WorkingCapitalLoanConstants.transactionAmountParamName)
                && error.getUserMessageGlobalisationCode().endsWith("scale.is.greater.than.2"));
    }

    @Test
    void shouldRejectAZeroDelta() {
        // A zero delta would reverse and recreate the transaction at the same amount, spending an id and an event on
        // nothing.
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(deltaCommand("0"), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.transactionAmount.zero.amount.not.supported");
    }

    @Test
    void shouldRejectAMissingDelta() {
        final JsonObject json = baseJson();
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(command(json), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.transactionAmount.cannot.be.blank");
    }

    @Test
    void shouldRejectAnAlreadyReversedTransactionLikeUndoDoes() {
        lenient().when(transaction.isReversed()).thenReturn(true);

        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(deltaCommand("-40.00"), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.transaction.transaction.already.undone");
    }

    @Test
    void shouldRejectALoanStatusUndoRejects() {
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);

        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(deltaCommand("-40.00"), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.loanStatus.adjust.by.delta.transaction.not.allowed.for.loan.status");
    }

    @Test
    void shouldRejectAReplacementDateInTheFuture() {
        final JsonObject json = baseJson();
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal("-40.00"));
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, BUSINESS_DATE.plusDays(1).toString());

        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdjustByDelta(command(json), loan, transaction));

        assertThat(ex.getErrors()).extracting(ApiParameterError::getUserMessageGlobalisationCode)
                .containsExactly("validation.msg.WORKINGCAPITALLOAN.transactionDate.cannot.be.a.future.date");
    }

    private JsonCommand deltaCommand(final String delta) {
        final JsonObject json = baseJson();
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal(delta));
        return command(json);
    }

    private JsonObject baseJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        return json;
    }

    private JsonCommand command(final JsonObject json) {
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
