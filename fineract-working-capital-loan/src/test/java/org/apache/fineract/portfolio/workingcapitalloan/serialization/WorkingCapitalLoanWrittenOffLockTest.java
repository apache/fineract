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
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
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
 * Pins the post-write-off lock: while a loan sits in {@code CLOSED_WRITTEN_OFF}, no new transaction can be posted on it
 * and no existing transaction can be undone. Working Capital is stricter here than term and progressive loans, which
 * reopen the loan on adjustment, so the only way back is the undo write-off.
 * <p>
 * The lock is not a check of its own - it falls out of the status gates each operation already applies. That makes it
 * easy to break by accident, hence these tests.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanWrittenOffLockTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);

    private WorkingCapitalLoanDataValidator validator;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction transaction;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, null, null, null, null);
        lenient().when(loan.getDisbursementDetails()).thenReturn(List.of());
        lenient().when(loan.getLoanStatus()).thenReturn(LoanStatus.CLOSED_WRITTEN_OFF);
        lenient().when(transaction.isReversed()).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldRejectARepaymentOnAWrittenOffLoan() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateRepayment(repaymentJson(), loan, LoanTransactionType.REPAYMENT));
        assertThat(ex.getErrors()).anyMatch(error -> error.getUserMessageGlobalisationCode().contains("wc.loan.transition.not.allowed"));
    }

    @Test
    void shouldRejectAnUndoTransactionOnAWrittenOffLoan() {
        final PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUndoTransaction(undoCommand(), loan, transaction));
        assertThat(ex.getErrors())
                .anyMatch(error -> error.getUserMessageGlobalisationCode().contains("undo.transaction.not.allowed.for.loan.status"));
    }

    @Test
    void shouldAllowBothAgainOnceTheWriteOffIsUndone() {
        // The undo write-off reopens the loan, which is what lifts the lock - nothing else does.
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);

        assertDoesNotThrow(() -> validator.validateRepayment(repaymentJson(), loan, LoanTransactionType.REPAYMENT));
        assertDoesNotThrow(() -> validator.validateUndoTransaction(undoCommand(), loan, transaction));
    }

    private String repaymentJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.transactionDateParamName, BUSINESS_DATE.toString());
        json.addProperty(WorkingCapitalLoanConstants.transactionAmountParamName, 100);
        return json.toString();
    }

    private JsonCommand undoCommand() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        return command(json);
    }

    private JsonCommand command(final JsonObject json) {
        final FromJsonHelper helper = new FromJsonHelper();
        final JsonElement parsed = helper.parse(json.toString());
        return JsonCommand.from(json.toString(), parsed, helper, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null);
    }
}
