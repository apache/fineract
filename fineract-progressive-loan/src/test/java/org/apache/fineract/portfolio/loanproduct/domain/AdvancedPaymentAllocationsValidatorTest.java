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
import static org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule.LAST_INSTALLMENT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.DEFAULT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.REPAYMENT;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class AdvancedPaymentAllocationsValidatorTest {

    private AdvancedPaymentAllocationsValidator underTest = new AdvancedPaymentAllocationsValidator();

    @Test
    public void testPaymentAllocationsHasNoError() {
        underTest.validatePairOfOrderAndPaymentAllocationType(createPaymentAllocationTypeList());
    }

    @Test
    public void testPaymentAllocationsValidationThrowsErrorWhenLessElement() {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndPaymentAllocationType(createPaymentAllocationTypeList().subList(0, 11)));
        assertPlatformException("Each provided payment allocation must contain exactly 12 allocation rules, but 11 were provided",
                "advanced-payment-strategy.each_payment_allocation_order.must.contain.12.entries", validationException);
    }

    @Test
    public void testPaymentAllocationsValidationThrowsErrorWhenWithDuplicate() {
        ArrayList<Pair<Integer, PaymentAllocationType>> pairs = new ArrayList<>(createPaymentAllocationTypeList().subList(0, 11));
        pairs.add(pairs.get(10));
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndPaymentAllocationType(pairs));
        assertPlatformException("The list of provided payment allocation rules must not contain any duplicates",
                "advanced-payment-strategy.must.not.have.duplicate.payment.allocation.rule", validationException);
    }

    @Test
    public void testPaymentAllocationsValidationThrowsErrorWhenOrderIsNotInRange() {
        List<Pair<Integer, PaymentAllocationType>> pairs = createPaymentAllocationTypeList().stream()
                .map(p -> Pair.of(p.getLeft() + 1, p.getRight())).toList();
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validatePairOfOrderAndPaymentAllocationType(pairs));
        assertPlatformException("The provided orders must be between 1 and 12", "advanced-payment-strategy.invalid.order",
                validationException);
    }

    @Test
    public void testValidateNoError() {
        LoanProductPaymentAllocationRule lppr1 = createLoanProductAllocationRule1();
        LoanProductPaymentAllocationRule lppr2 = createLoanProductAllocationRule2();
        underTest.validate(List.of(lppr1, lppr2), ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
    }

    @Test
    public void testValidateEmptyRuleList() {
        assertPlatformValidationException(
                "Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                "advanced-payment-strategy-without-default-payment-allocation",
                () -> underTest.validate(List.of(), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidateMissingList() {
        assertPlatformValidationException(
                "Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                "advanced-payment-strategy-without-default-payment-allocation",
                () -> underTest.validate(null, ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidatePaymentAllocationThrowsErrorWhenNoDefault() {
        LoanProductPaymentAllocationRule lppr2 = createLoanProductAllocationRule2();
        assertPlatformValidationException(
                "Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided",
                "advanced-payment-strategy-without-default-payment-allocation",
                () -> underTest.validate(List.of(lppr2), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidateThrowsErrorWhenDuplicate() {
        LoanProductPaymentAllocationRule lppr1 = createLoanProductAllocationRule1();
        LoanProductPaymentAllocationRule lppr2 = createLoanProductAllocationRule2();
        LoanProductPaymentAllocationRule lppr3 = createLoanProductAllocationRule2();
        assertPlatformValidationException("The same transaction type must be provided only once",
                "advanced-payment-strategy-with-duplicate-payment-allocation",
                () -> underTest.validate(List.of(lppr1, lppr2, lppr3), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidateThrowsErrorWhenPaymentAllocationProvidedWithOtherStrategy() {
        LoanProductPaymentAllocationRule lppr1 = createLoanProductAllocationRule1();
        LoanProductPaymentAllocationRule lppr2 = createLoanProductAllocationRule2();
        assertPlatformValidationException("In case 'some-other-strategy' payment strategy, payment_allocation must not be provided",
                "payment_allocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                () -> underTest.validate(List.of(lppr1, lppr2), "some-other-strategy"));
    }

    @Test
    public void testValidateThrowsErrorWhenFutureInstallmentIsEmpty() {
        LoanProductPaymentAllocationRule lppr1 = createLoanProductAllocationRule1();
        lppr1.setFutureInstallmentAllocationRule(null);
        assertPlatformValidationException("Payment allocation was provided without a valid future installment allocation rule",
                "advanced-payment-strategy.with.not.valid.future.installment.allocation.rule",
                () -> underTest.validate(List.of(lppr1), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void testValidateThrowsErrorWhenTransactionTypeEmpty() {
        LoanProductPaymentAllocationRule lppr1 = createLoanProductAllocationRule1();
        LoanProductPaymentAllocationRule lppr2 = createLoanProductAllocationRule1();
        lppr2.setTransactionType(null);
        assertPlatformValidationException("Payment allocation was provided with a not valid transaction type",
                "advanced-payment-strategy.with.not.valid.transaction.type",
                () -> underTest.validate(List.of(lppr1, lppr2), ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
    }

    @Test
    public void checkGroupingOfAllocationRulesGoodOrder() {
        List<LoanProductPaymentAllocationRule> allocationRules1 = List.of(createLoanProductAllocationRule1());
        Assertions.assertDoesNotThrow(() -> underTest.checkGroupingOfAllocationRules(allocationRules1));

        List<LoanProductPaymentAllocationRule> allocationRules2 = List.of(createLoanProductAllocationRule4());
        Assertions.assertDoesNotThrow(() -> underTest.checkGroupingOfAllocationRules(allocationRules2));
    }

    @Test
    public void checkGroupingOfAllocationRulesWrongOrder() {
        List<LoanProductPaymentAllocationRule> allocationRules = List.of(createLoanProductAllocationRule3());
        assertPlatformValidationException("Horizontal repayment schedule processing is not supporting mixed due type allocation rules!",
                "mixed.due.type.allocation.rules.are.not.supported.with.horizontal.installment.processing",
                () -> underTest.checkGroupingOfAllocationRules(allocationRules));
    }

    private void assertPlatformValidationException(String message, String code, Executable executable) {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class, executable);
        assertPlatformException(message, code, validationException);
    }

    @NotNull
    private static LoanProductPaymentAllocationRule createLoanProductAllocationRule1() {
        LoanProductPaymentAllocationRule lppr1 = new LoanProductPaymentAllocationRule();
        lppr1.setTransactionType(DEFAULT);
        lppr1.setFutureInstallmentAllocationRule(LAST_INSTALLMENT);
        lppr1.setAllocationTypes(EnumSet.allOf(PaymentAllocationType.class).stream().toList());
        return lppr1;
    }

    @NotNull
    private static LoanProductPaymentAllocationRule createLoanProductAllocationRule2() {
        LoanProductPaymentAllocationRule lppr2 = new LoanProductPaymentAllocationRule();
        lppr2.setTransactionType(REPAYMENT);
        lppr2.setFutureInstallmentAllocationRule(LAST_INSTALLMENT);
        ArrayList<PaymentAllocationType> allocationTypes = new ArrayList<>(EnumSet.allOf(PaymentAllocationType.class).stream().toList());
        Collections.shuffle(allocationTypes);
        lppr2.setAllocationTypes(allocationTypes);
        return lppr2;
    }

    @NotNull
    private static LoanProductPaymentAllocationRule createLoanProductAllocationRule3() {
        LoanProductPaymentAllocationRule lppr = new LoanProductPaymentAllocationRule();
        lppr.setTransactionType(REPAYMENT);
        lppr.setFutureInstallmentAllocationRule(LAST_INSTALLMENT);
        ArrayList<PaymentAllocationType> allocationTypes = new ArrayList<>(EnumSet.allOf(PaymentAllocationType.class).stream().toList());
        Collections.swap(allocationTypes, 0, 4);
        Collections.swap(allocationTypes, 3, 8);
        Collections.swap(allocationTypes, 7, 11);
        lppr.setAllocationTypes(allocationTypes);
        return lppr;
    }

    @NotNull
    private static LoanProductPaymentAllocationRule createLoanProductAllocationRule4() {
        LoanProductPaymentAllocationRule lppr = new LoanProductPaymentAllocationRule();
        lppr.setTransactionType(DEFAULT);
        lppr.setFutureInstallmentAllocationRule(LAST_INSTALLMENT);
        ArrayList<PaymentAllocationType> allocationTypes = new ArrayList<>(EnumSet.allOf(PaymentAllocationType.class).stream().toList());
        Collections.swap(allocationTypes, 0, 4);
        Collections.swap(allocationTypes, 1, 5);
        Collections.swap(allocationTypes, 2, 6);
        Collections.swap(allocationTypes, 3, 7);
        lppr.setAllocationTypes(allocationTypes);
        return lppr;
    }

    @NotNull
    private static List<Pair<Integer, PaymentAllocationType>> createPaymentAllocationTypeList() {
        AtomicInteger i = new AtomicInteger(1);
        return EnumSet.allOf(PaymentAllocationType.class).stream().map(p -> Pair.of(i.getAndIncrement(), p)).toList();
    }

    private void assertPlatformException(String expectedMessage, String expectedCode,
            PlatformApiDataValidationException platformApiDataValidationException) {
        Assertions.assertEquals(expectedMessage, platformApiDataValidationException.getErrors().get(0).getDefaultUserMessage());
        Assertions.assertEquals(expectedCode, platformApiDataValidationException.getErrors().get(0).getUserMessageGlobalisationCode());
    }

}
