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
 * A text amount is read with the request's {@code locale}, as everywhere else in Fineract. The recovery payment request
 * carries the amount: 100 is written off, so "40,5" passes only when it is read as 40.5, not as 405.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorAmountLocaleTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoanTransactionFinder transactionFinder;
    @Mock
    private WorkingCapitalLoan loan;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, transactionFinder, null, null, null);

        final WorkingCapitalLoanBalance balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipalWrittenOff(new BigDecimal("100"));
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);
        lenient().when(loan.isClosedWrittenOff()).thenReturn(true);
        lenient().when(loan.getBalance()).thenReturn(balance);
        lenient().when(transactionFinder.getLastUserTransactionDate(loan)).thenReturn(Optional.of(BUSINESS_DATE.minusDays(10)));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldReadADecimalCommaWithACommaLocale() {
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(command("pl", "40,5"), loan));
    }

    @Test
    void shouldRejectADecimalPointWithACommaLocale() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRecoveryPayment(command("pl", "40.5"), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().equals("validation.msg.invalid.decimal.format"));
    }

    @Test
    void shouldReadADecimalPointWithAnEnglishLocale() {
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(command("en", "40.5"), loan));
    }

    @Test
    void shouldReadAJsonNumberWhateverTheLocale() {
        final JsonObject json = json("pl");
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, new BigDecimal("40.5"));
        assertDoesNotThrow(() -> validator.validateRecoveryPayment(command(json), loan));
    }

    private JsonCommand command(final String locale, final String amount) {
        final JsonObject json = json(locale);
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, amount);
        return command(json);
    }

    private JsonObject json(final String locale) {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", locale);
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, BUSINESS_DATE.toString());
        return json;
    }

    private JsonCommand command(final JsonObject json) {
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
