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
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
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
 * Pins the write-off request contract.
 * <p>
 * The transaction date rule: the write-off may be backdated, but only as far back as the last user-initiated
 * transaction, so that it always remains the latest transaction on the loan account. Future dates are rejected
 * outright.
 * </p>
 * <p>
 * The accepted parameter names, which follow the term/progressive loan shape -- notably the lower-case
 * {@code writeoffReasonId}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDataValidatorWriteOffDateTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 10);
    private static final LocalDate LAST_REPAYMENT_DATE = LocalDate.of(2026, 2, 20);

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

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, transactionFinder, null, null, null);
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);
        lenient().when(transactionFinder.getLastUserTransactionDate(loan)).thenReturn(Optional.of(LAST_REPAYMENT_DATE));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldAcceptWriteOffOnTheBusinessDate() {
        assertDoesNotThrow(() -> validator.validateWriteOff(writeOffCommand(BUSINESS_DATE), loan));
    }

    @Test
    void shouldAcceptBackdatedWriteOffAfterTheLastUserTransaction() {
        assertDoesNotThrow(() -> validator.validateWriteOff(writeOffCommand(LAST_REPAYMENT_DATE.plusDays(1)), loan));
    }

    @Test
    void shouldAcceptWriteOffOnTheSameDateAsTheLastUserTransaction() {
        // The rule is "not before", so same-day is allowed: the write-off still is the latest transaction.
        assertDoesNotThrow(() -> validator.validateWriteOff(writeOffCommand(LAST_REPAYMENT_DATE), loan));
    }

    @Test
    void shouldRejectWriteOffBackdatedBeforeTheLastUserTransaction() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateWriteOff(writeOffCommand(LAST_REPAYMENT_DATE.minusDays(1)), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.before.last.transaction.date"));
    }

    @Test
    void shouldRejectWriteOffBackdatedBeforeTheDisbursement() {
        // The disbursement is a user transaction, so it acts as the floor when it is the only transaction on the loan.
        when(transactionFinder.getLastUserTransactionDate(loan)).thenReturn(Optional.of(DISBURSEMENT_DATE));
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateWriteOff(writeOffCommand(DISBURSEMENT_DATE.minusDays(1)), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.before.last.transaction.date"));
    }

    @Test
    void shouldRejectWriteOffInTheFuture() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateWriteOff(writeOffCommand(BUSINESS_DATE.plusDays(1)), loan));
        assertThat(ex.getErrors()).anyMatch(error -> error.getUserMessageGlobalisationCode().contains("cannot.be.a.future.date"));
    }

    @Test
    void shouldRejectMissingTransactionDate() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateWriteOff(command(json), loan));
        assertThat(ex.getErrors()).anyMatch(error -> error.getParameterName().equals(WorkingCapitalLoanConstants.transactionDateParamName));
    }

    @Test
    void shouldRejectWriteOffOnANonActiveLoan() {
        when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateWriteOff(writeOffCommand(BUSINESS_DATE), loan));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("error.msg.wc.loan.is.not.active"));
    }

    @Test
    void shouldAcceptTheLowerCaseWriteOffReasonParameter() {
        final JsonObject json = writeOffJson(BUSINESS_DATE);
        json.addProperty(WorkingCapitalLoanConstants.writeoffReasonIdParamName, 7L);
        assertDoesNotThrow(() -> validator.validateWriteOff(command(json), loan));
    }

    @Test
    void shouldRejectTheCamelCaseWriteOffReasonParameter() {
        // Term and progressive loans send "writeoffReasonId"; the camel-case spelling must not be silently accepted,
        // or a caller would get a write-off with no reason recorded and no error.
        final JsonObject json = writeOffJson(BUSINESS_DATE);
        json.addProperty("writeOffReasonId", 7L);
        final UnsupportedParameterException ex = assertThrows(UnsupportedParameterException.class,
                () -> validator.validateWriteOff(command(json), loan));
        assertThat(ex.getUnsupportedParameters()).contains("writeOffReasonId");
    }

    private JsonCommand writeOffCommand(final LocalDate transactionDate) {
        return command(writeOffJson(transactionDate));
    }

    private JsonObject writeOffJson(final LocalDate transactionDate) {
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
