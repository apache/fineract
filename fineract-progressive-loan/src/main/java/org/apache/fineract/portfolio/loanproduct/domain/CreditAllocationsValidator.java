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
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Service;

@Service
public class CreditAllocationsValidator {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    public void validate(List<CreditAllocationRequest> requests, String code) {
        if (isAdvancedPaymentStrategy(code)) {
            if (hasDuplicateTransactionTypes(requests)) {
                raiseValidationError("advanced-payment-strategy-with-duplicate-credit-allocation",
                        "The same transaction type must be provided only once");
            }

            if (requests != null) {
                for (CreditAllocationRequest request : requests) {
                    validateAllocationRule(request);
                }
            }

        } else {
            if (hasCreditAllocationRequest(requests)) {
                raiseValidationError("credit_allocation.must.not.be.provided.when.allocation.strategy.is.not.advanced-payment-strategy",
                        "In case '" + code + "' payment strategy, creditAllocation must not be provided");
            }
        }
    }

    public void validatePairOfOrderAndCreditAllocationType(List<Pair<Integer, AllocationType>> rules) {
        if (rules.size() != 4) {
            raiseValidationError("advanced-payment-strategy.each_credit_allocation_order.must.contain.4.entries",
                    "Each provided credit allocation must contain exactly 4 allocation rules, but " + rules.size() + " were provided");
        }

        List<AllocationType> deduped = rules.stream().map(Pair::getRight).distinct().toList();
        if (deduped.size() != 4) {
            raiseValidationError("advanced-payment-strategy.must.not.have.duplicate.credit.allocation.rule",
                    "The list of provided credit allocation rules must not contain any duplicates");
        }

        if (!Arrays.equals(IntStream.rangeClosed(1, 4).boxed().toArray(), rules.stream().map(Pair::getLeft).toArray())) {
            raiseValidationError("advanced-payment-strategy.invalid.order", "The provided orders must be between 1 and 4");
        }
    }

    private boolean hasDuplicateTransactionTypes(List<CreditAllocationRequest> requests) {
        return requests != null
                && requests.stream().map(CreditAllocationRequest::transactionType).distinct().toList().size() != requests.size();
    }

    private void validateAllocationRule(CreditAllocationRequest request) {
        if (request.transactionType() == null) {
            raiseValidationError("advanced-payment-strategy.with.not.valid.transaction.type",
                    "Credit allocation was provided with a not valid transaction type");
        }
        validateAllocationStrategy(request);
    }

    private void validateAllocationStrategy(CreditAllocationRequest request) {
        CreditAllocationStrategy strategy = request.allocationStrategy();
        switch (strategy) {
            case RULE_BASED -> {
                if (!request.creditAllocationOrderProvided()) {
                    raiseValidationError("advanced-payment-strategy.rule_based_credit_allocation.must.contain.credit_allocation_order",
                            "Credit allocation with '" + strategy + "' strategy must contain creditAllocationOrder");
                }
            }
            case PRINCIPAL_ONLY -> {
                if (request.creditAllocationOrderProvided()) {
                    raiseValidationError("advanced-payment-strategy.credit_allocation_order.must.not.be.provided.for.strategy",
                            "Credit allocation with '" + strategy + "' strategy must not contain creditAllocationOrder");
                }
            }
            case null -> raiseValidationError("advanced-payment-strategy.with.not.valid.credit.allocation.strategy",
                    "Credit allocation was provided with a not valid credit allocation strategy");
        }
    }

    private boolean isAdvancedPaymentStrategy(String code) {
        return ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(code);
    }

    private boolean hasCreditAllocationRequest(List<CreditAllocationRequest> requests) {
        return requests != null && !requests.isEmpty();
    }

    private void raiseValidationError(String globalisationMessageCode, String msg) {
        throw new PlatformApiDataValidationException(List.of(ApiParameterError.generalError(globalisationMessageCode, msg)));
    }

}
