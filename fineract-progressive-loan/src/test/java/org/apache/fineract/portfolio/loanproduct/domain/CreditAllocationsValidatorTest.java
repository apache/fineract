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
package org.apache.fineract.portfolio.loanproduct.domain;

import static org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType.CHARGEBACK;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class CreditAllocationsValidatorTest {

    private CreditAllocationsValidator underTest = new CreditAllocationsValidator();

    @Test
    public void testCreditAllocationsHasNoError() {
        underTest.validatePairOfOrderAndCreditAllocationType(createCreditAllocationTypeList());
    }

    @Test
    public void testCreditAllocationsValidationThrowsErrorWhenLessElement() {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndCreditAllocationType(createCreditAllocationTypeList().subList(0, 3)));
        assertPlatformException("Each provided credit allocation must contain exactly 4 allocation rules, but 3 were provided",
                "advanced-payment-strategy.each_credit_allocation_order.must.contain.4.entries", validationException);
    }

    @Test
    public void testCreditAllocationsValidationThrowsErrorWhenWithDuplicate() {
        ArrayList<Pair<Integer, AllocationType>> pairs = new ArrayList<>(createCreditAllocationTypeList().subList(0, 3));
        pairs.add(pairs.get(2));
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndCreditAllocationType(pairs));
        assertPlatformException("The list of provided credit allocation rules must not contain any duplicates",
                "advanced-payment-strategy.must.not.have.duplicate.credit.allocation.rule", validationException);
    }

    @Test
    public void testCreditAllocationsValidationThrowsErrorWhenOrderIsNotInRange() {
        List<Pair<Integer, AllocationType>> pairs = createCreditAllocationTypeList().stream()
                .map(p -> Pair.of(p.getLeft() + 1, p.getRight())).toList();
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndCreditAllocationType(pairs));
        assertPlatformException("The provided orders must be between 1 and 4", "advanced-payment-strategy.invalid.order",
                validationException);
    }

    @Test
    public void testValidateThrowsErrorWhenPaymentAllocationProvidedWithOtherStrategy() {
        assertPlatformValidationException("In case 'some-other-strategy' payment strategy, creditAllocation must not be provided",
                "credit_allocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                () -> underTest.validate(List.of(createLoanProductCreditAllocationRule1()), "some-other-strategy"));
    }

    @Test
    public void testValidateThrowsErrorWhenTransactionTypeEmpty() {
        LoanProductCreditAllocationRule lpcar = createLoanProductCreditAllocationRule1();
        lpcar.setTransactionType(null);
        assertPlatformValidationException("Credit allocation was provided with a not valid transaction type",
                "advanced-payment-strategy.with.not.valid.transaction.type",
                () -> underTest.validate(List.of(lpcar), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidateNoError() {
        underTest.validate(List.of(createLoanProductCreditAllocationRule1()), ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
    }

    @Test
    public void testValidateCreditAllocationIsOptional() {
        underTest.validate(List.of(), ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
    }

    @Test
    public void testValidateThrowsErrorWhenDuplicate() {
        assertPlatformValidationException("The same transaction type must be provided only once",
                "advanced-payment-strategy-with-duplicate-credit-allocation",
                () -> underTest.validate(List.of(createLoanProductCreditAllocationRule1(), createLoanProductCreditAllocationRule1()),
                        ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @NotNull
    private static List<Pair<Integer, AllocationType>> createCreditAllocationTypeList() {
        AtomicInteger i = new AtomicInteger(1);
        return EnumSet.allOf(AllocationType.class).stream().map(p -> Pair.of(i.getAndIncrement(), p)).toList();
    }

    @NotNull
    private static LoanProductCreditAllocationRule createLoanProductCreditAllocationRule1() {
        LoanProductCreditAllocationRule lpcr1 = new LoanProductCreditAllocationRule();
        lpcr1.setTransactionType(CHARGEBACK);
        lpcr1.setAllocationTypes(EnumSet.allOf(AllocationType.class).stream().toList());
        return lpcr1;
    }

    private void assertPlatformValidationException(String message, String code, Executable executable) {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class, executable);
        assertPlatformException(message, code, validationException);
    }

    private void assertPlatformException(String expectedMessage, String expectedCode,
            PlatformApiDataValidationException platformApiDataValidationException) {
        Assertions.assertEquals(expectedMessage, platformApiDataValidationException.getErrors().get(0).getDefaultUserMessage());
        Assertions.assertEquals(expectedCode, platformApiDataValidationException.getErrors().get(0).getUserMessageGlobalisationCode());
    }

}
