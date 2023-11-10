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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Service;

@Service
public class AdvancedPaymentAllocationsValidator {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    public void validate(List<LoanProductPaymentAllocationRule> rules, String code) {
        if (isAdvancedPaymentStrategy(code)) {
            if (!hasLoanProductPaymentAllocationRule(rules) || !hasAtLeastOneDefaultPaymentAllocation(rules)) {
                raiseValidationError("advanced-payment-strategy-without-default-payment-allocation",
                        "Advanced-payment-allocation-strategy was selected but no DEFAULT payment allocation was provided");
            }

            if (hasDuplicateTransactionTypes(rules)) {
                raiseValidationError("advanced-payment-strategy-with-duplicate-payment-allocation",
                        "The same transaction type must be provided only once");
            }

            for (LoanProductPaymentAllocationRule rule : rules) {
                validateAllocationRule(rule);
            }

        } else {
            if (hasLoanProductPaymentAllocationRule(rules)) {
                raiseValidationError("payment_allocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                        "In case '" + code + "' payment strategy, payment_allocation must not be provided");
            }
        }
    }

    public void validatePairOfOrderAndPaymentAllocationType(List<Pair<Integer, PaymentAllocationType>> rules) {
        if (rules.size() != 12) {
            raiseValidationError("advanced-payment-strategy.each_payment_allocation_order.must.contain.12.entries",
                    "Each provided payment allocation must contain exactly 12 allocation rules, but " + rules.size() + " were provided");
        }

        List<PaymentAllocationType> deduped = rules.stream().map(Pair::getRight).distinct().toList();
        if (deduped.size() != 12) {
            raiseValidationError("advanced-payment-strategy.must.not.have.duplicate.payment.allocation.rule",
                    "The list of provided payment allocation rules must not contain any duplicates");
        }

        if (!Arrays.equals(IntStream.rangeClosed(1, 12).boxed().toArray(), rules.stream().map(Pair::getLeft).toArray())) {
            raiseValidationError("advanced-payment-strategy.invalid.order", "The provided orders must be between 1 and 12");
        }
    }

    public void checkGroupingOfAllocationRules(List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules) {
        loanProductPaymentAllocationRules.forEach(paymentAllocationRule -> {
            AtomicInteger pastDueRuleInteger = new AtomicInteger();
            AtomicInteger dueRuleInteger = new AtomicInteger();
            AtomicInteger inAdvanceRuleInteger = new AtomicInteger();
            paymentAllocationRule.getAllocationTypes().forEach(paymentAllocationType -> {
                validateAllocationType(paymentAllocationType, pastDueRuleInteger, dueRuleInteger, inAdvanceRuleInteger);
            });
        });
    }

    private void validateAllocationType(PaymentAllocationType paymentAllocationType, AtomicInteger pastDueRuleInteger,
            AtomicInteger dueRuleInteger, AtomicInteger inAdvanceRuleInteger) {
        switch (paymentAllocationType.getDueType()) {
            case PAST_DUE -> validateDueType(pastDueRuleInteger, dueRuleInteger, inAdvanceRuleInteger);
            case DUE -> validateDueType(dueRuleInteger, pastDueRuleInteger, inAdvanceRuleInteger);
            case IN_ADVANCE -> validateDueType(inAdvanceRuleInteger, pastDueRuleInteger, dueRuleInteger);
        }
    }

    private void validateDueType(AtomicInteger currentRuleInteger, AtomicInteger otherRule1Integer, AtomicInteger otherRule2Integer) {
        currentRuleInteger.incrementAndGet();
        if ((otherRule1Integer.get() > 0 && otherRule1Integer.get() < 4) || (otherRule2Integer.get() > 0 && otherRule2Integer.get() < 4)) {
            raiseValidationError("mixed.due.type.allocation.rules.are.not.supported.with.horizontal.installment.processing",
                    "Horizontal repayment schedule processing is not supporting mixed due type allocation rules!");
        }
    }

    private boolean hasDuplicateTransactionTypes(List<LoanProductPaymentAllocationRule> rules) {
        return rules != null
                && rules.stream().map(LoanProductPaymentAllocationRule::getTransactionType).distinct().toList().size() != rules.size();
    }

    private void validateAllocationRule(LoanProductPaymentAllocationRule rule) {
        if (rule.getTransactionType() == null) {
            raiseValidationError("advanced-payment-strategy.with.not.valid.transaction.type",
                    "Payment allocation was provided with a not valid transaction type");
        }
        if (rule.getFutureInstallmentAllocationRule() == null) {
            raiseValidationError("advanced-payment-strategy.with.not.valid.future.installment.allocation.rule",
                    "Payment allocation was provided without a valid future installment allocation rule");
        }
    }

    private boolean isAdvancedPaymentStrategy(String code) {
        return ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(code);
    }

    private boolean hasAtLeastOneDefaultPaymentAllocation(List<LoanProductPaymentAllocationRule> rules) {
        return rules != null && rules.stream() //
                .filter(r -> PaymentAllocationTransactionType.DEFAULT.equals(r.getTransactionType())) //
                .toList() //
                .size() > 0;
    }

    private boolean hasLoanProductPaymentAllocationRule(List<LoanProductPaymentAllocationRule> rules) {
        return rules != null && rules.size() > 0;
    }

    private void raiseValidationError(String globalisationMessageCode, String msg) {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.generalError(globalisationMessageCode, msg)));
    }

}
