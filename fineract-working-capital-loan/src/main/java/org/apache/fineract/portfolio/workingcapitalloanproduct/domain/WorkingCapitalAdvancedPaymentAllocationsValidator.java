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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.springframework.stereotype.Service;

@Service
public class WorkingCapitalAdvancedPaymentAllocationsValidator {

    public void validate(final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            raiseValidationError("wc-payment-allocation-without-default", "At least one DEFAULT payment allocation must be provided");
        }
        final List<WorkingCapitalLoanProductPaymentAllocationRule> rulesToValidate = Objects.requireNonNull(rules);
        if (hasWCPaymentAllocationRule(rulesToValidate)) {
            if (!hasAtLeastOneDefaultPaymentAllocation(rulesToValidate)) {
                raiseValidationError("wc-payment-allocation-without-default", "At least one DEFAULT payment allocation must be provided");
            }

            if (hasDuplicateTransactionTypes(rulesToValidate)) {
                raiseValidationError("wc-payment-allocation-with-duplicate-transaction-type",
                        "The same transaction type must be provided only once");
            }

            for (final WorkingCapitalLoanProductPaymentAllocationRule rule : rulesToValidate) {
                validateAllocationRule(rule);
            }
        }
    }

    public void validatePairOfOrderAndPaymentAllocationType(final List<Pair<Integer, WorkingCapitalPaymentAllocationType>> rules) {
        // WCL has 3 allocation types: PENALTY, FEE, PRINCIPAL (no INTEREST)
        final int expectedCount = 3;
        if (rules.size() != expectedCount) {
            raiseValidationError("wc-payment-allocation-order.must.contain.3.entries",
                    "Each provided payment allocation must contain exactly " + expectedCount + " allocation rules, but " + rules.size()
                            + " were provided");
        }

        // Check for null values (invalid payment allocation types)
        final boolean hasNullValues = rules.stream().anyMatch(pair -> pair.getRight() == null);
        if (hasNullValues) {
            raiseValidationError("wc-payment-allocation.invalid.allocation.type",
                    "One or more payment allocation types are invalid or not recognized");
        }

        final List<WorkingCapitalPaymentAllocationType> deduced = rules.stream().map(Pair::getRight).distinct().toList();
        if (deduced.size() != expectedCount) {
            raiseValidationError("wc-payment-allocation.must.not.have.duplicate.allocation.rule",
                    "The list of provided payment allocation rules must not contain any duplicates");
        }

        if (!Arrays.equals(IntStream.rangeClosed(1, expectedCount).boxed().toArray(), rules.stream().map(Pair::getLeft).toArray())) {
            raiseValidationError("wc-payment-allocation.invalid.order", "The provided orders must be between 1 and " + expectedCount);
        }
    }

    private boolean hasDuplicateTransactionTypes(final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        return rules != null && rules.stream().map(WorkingCapitalLoanProductPaymentAllocationRule::getTransactionType).distinct().toList()
                .size() != rules.size();
    }

    private void validateAllocationRule(final WorkingCapitalLoanProductPaymentAllocationRule rule) {
        if (rule.getTransactionType() == null) {
            raiseValidationError("wc-payment-allocation.with.not.valid.transaction.type",
                    "Payment allocation was provided with a not valid transaction type");
        }
    }

    private boolean hasAtLeastOneDefaultPaymentAllocation(final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        return rules != null && !rules.stream() //
                .filter(r -> PaymentAllocationTransactionType.DEFAULT.equals(r.getTransactionType())) //
                .toList() //
                .isEmpty();
    }

    private boolean hasWCPaymentAllocationRule(final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        return rules != null && !rules.isEmpty();
    }

    public void raiseValidationError(final String globalisationMessageCode, final String msg) {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.generalError(globalisationMessageCode, msg)));
    }

}
