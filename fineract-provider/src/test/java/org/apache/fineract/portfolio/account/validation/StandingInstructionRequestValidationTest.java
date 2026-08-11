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
package org.apache.fineract.portfolio.account.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionCreationRequest;
import org.apache.fineract.portfolio.account.data.request.StandingInstructionUpdateRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Covers the standing instruction request constraints - the four valid Transfer Type x Instruction Type x Recurrence
 * Type x Amount combinations plus the amount integrity rules - now that validation is expressed as Jakarta Bean
 * Validation constraints on the request DTOs rather than as a hand-rolled validator.
 */
public class StandingInstructionRequestValidationTest {

    private static final int SAVINGS = 2;
    private static final int LOAN = 1;
    private static final int ACCOUNT_TRANSFER = 1;
    private static final int LOAN_REPAYMENT = 2;
    private static final int FIXED = 1;
    private static final int DUES = 2;
    private static final int PERIODIC = 1;
    private static final int AS_PER_DUES = 2;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    /**
     * Baseline valid request: Account Transfer (savings -> savings) / Fixed / Periodic with an amount. Individual tests
     * mutate the fields under test.
     */
    private StandingInstructionCreationRequest baseCreate() {
        final StandingInstructionCreationRequest request = new StandingInstructionCreationRequest();
        request.setName("SI-test");
        request.setStatus(1);
        request.setPriority(1);
        request.setTransferType(ACCOUNT_TRANSFER);
        request.setFromAccountType(SAVINGS);
        request.setToAccountType(SAVINGS);
        request.setInstructionType(FIXED);
        request.setRecurrenceType(PERIODIC);
        request.setRecurrenceFrequency(0); // daily -> no month/day required
        request.setRecurrenceInterval(1);
        request.setAmount(BigDecimal.valueOf(100));
        request.setValidFrom("2024-01-01");
        request.setValidTill("2024-12-31");
        return request;
    }

    private Set<String> violatedPropertiesOf(final Object request) {
        final Set<ConstraintViolation<Object>> violations = validator.validate(request);
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    // --- The four valid scenarios (should all pass) ---

    @Test
    public void scenario1AccountTransferFixedPeriodicWithAmountPasses() {
        assertThat(violatedPropertiesOf(baseCreate())).isEmpty();
    }

    @Test
    public void scenario2LoanRepaymentFixedPeriodicWithAmountPasses() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setToAccountType(LOAN);
        request.setInstructionType(FIXED);
        request.setRecurrenceType(PERIODIC);
        request.setAmount(BigDecimal.valueOf(100));
        assertThat(violatedPropertiesOf(request)).isEmpty();
    }

    @Test
    public void scenario3LoanRepaymentDuesPeriodicWithoutAmountPasses() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setToAccountType(LOAN);
        request.setInstructionType(DUES);
        request.setRecurrenceType(PERIODIC);
        request.setAmount(null);
        assertThat(violatedPropertiesOf(request)).isEmpty();
    }

    @Test
    public void scenario4LoanRepaymentDuesAsPerDuesWithoutAmountPasses() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setToAccountType(LOAN);
        request.setInstructionType(DUES);
        request.setRecurrenceType(AS_PER_DUES);
        request.setRecurrenceInterval(null); // not required for non-periodic recurrence
        request.setAmount(null);
        assertThat(violatedPropertiesOf(request)).isEmpty();
    }

    // --- Amount integrity rules ---

    @Test
    public void duesInstructionWithAmountIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setToAccountType(LOAN);
        request.setInstructionType(DUES);
        request.setRecurrenceType(PERIODIC);
        request.setAmount(BigDecimal.valueOf(100)); // amount not allowed for Dues instruction type
        assertThat(violatedPropertiesOf(request)).contains("amount");
    }

    @Test
    public void fixedInstructionWithoutAmountIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setToAccountType(LOAN);
        request.setInstructionType(FIXED);
        request.setAmount(null); // amount required for Fixed instruction type
        assertThat(violatedPropertiesOf(request)).contains("amount");
    }

    // --- Field-level checks ---

    @Test
    public void statusOutOfRangeIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setStatus(5);
        assertThat(violatedPropertiesOf(request)).contains("status");
    }

    @Test
    public void missingNameIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setName(null);
        assertThat(violatedPropertiesOf(request)).contains("name");
    }

    @Test
    public void negativeAmountIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setAmount(BigDecimal.valueOf(-1));
        assertThat(violatedPropertiesOf(request)).contains("amount");
    }

    // --- Cross-field checks ---

    @Test
    public void validTillBeforeValidFromIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setValidFrom("2024-12-31");
        request.setValidTill("2024-01-01");
        assertThat(violatedPropertiesOf(request)).contains("validTill");
    }

    @Test
    public void missingValidFromIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setValidFrom(null);
        assertThat(violatedPropertiesOf(request)).contains("validFrom");
    }

    @Test
    public void periodicRecurrenceWithoutIntervalIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setRecurrenceType(PERIODIC);
        request.setRecurrenceInterval(null);
        assertThat(violatedPropertiesOf(request)).contains("recurrenceInterval");
    }

    @Test
    public void monthlyRecurrenceWithoutMonthDayIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setRecurrenceFrequency(2); // monthly
        request.setRecurrenceOnMonthDay(null);
        assertThat(violatedPropertiesOf(request)).contains("recurrenceOnMonthDay");
    }

    @Test
    public void loanRepaymentIntoSavingsIsRejected() {
        final StandingInstructionCreationRequest request = baseCreate();
        request.setTransferType(LOAN_REPAYMENT);
        request.setFromAccountType(SAVINGS);
        request.setToAccountType(SAVINGS);
        assertThat(violatedPropertiesOf(request)).contains("transferType");
    }

    // --- Update path (partial) ---

    @Test
    public void updateWithinRangesPasses() {
        final StandingInstructionUpdateRequest request = new StandingInstructionUpdateRequest();
        request.setStatus(2);
        request.setPriority(3);
        request.setAmount(BigDecimal.valueOf(50));
        assertThat(violatedPropertiesOf(request)).isEmpty();
    }

    @Test
    public void updateStatusOutOfRangeIsRejected() {
        final StandingInstructionUpdateRequest request = new StandingInstructionUpdateRequest();
        request.setStatus(9);
        assertThat(violatedPropertiesOf(request)).contains("status");
    }

    @Test
    public void emptyUpdatePasses() {
        assertThat(violatedPropertiesOf(new StandingInstructionUpdateRequest())).isEmpty();
    }
}
