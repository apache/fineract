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

import com.google.common.base.Enums;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WorkingCapitalAdvancedPaymentAllocationsJsonParser {

    public final WorkingCapitalAdvancedPaymentAllocationsValidator validator;

    public List<WorkingCapitalLoanProductPaymentAllocationRule> assembleWCPaymentAllocationRules(final JsonCommand command) {
        final JsonArray paymentAllocations = command.arrayOfParameterNamed("paymentAllocation");
        List<WorkingCapitalLoanProductPaymentAllocationRule> productPaymentAllocationRules = null;
        if (paymentAllocations != null) {
            productPaymentAllocationRules = paymentAllocations.asList().stream().map(json -> {
                final Map<String, JsonElement> map = json.getAsJsonObject().asMap();
                final WorkingCapitalLoanProductPaymentAllocationRule rule = new WorkingCapitalLoanProductPaymentAllocationRule();
                populatePaymentAllocationRules(map, rule);
                populateTransactionType(map, rule);
                return rule;
            }).toList();
        }
        validator.validate(productPaymentAllocationRules);
        return productPaymentAllocationRules;
    }

    private void populatePaymentAllocationRules(final Map<String, JsonElement> map,
            final WorkingCapitalLoanProductPaymentAllocationRule rule) {
        final JsonArray paymentAllocationOrder = asJsonArrayOrNull(map.get("paymentAllocationOrder"));
        if (paymentAllocationOrder != null) {
            rule.setAllocationTypes(getPaymentAllocationTypes(paymentAllocationOrder));
        }
    }

    private void populateTransactionType(final Map<String, JsonElement> map, final WorkingCapitalLoanProductPaymentAllocationRule rule) {
        final String transactionType = asStringOrNull(map.get("transactionType"));
        if (transactionType != null) {
            rule.setTransactionType(Enums.getIfPresent(PaymentAllocationTransactionType.class, transactionType).orNull());
        }
    }

    @NonNull
    private List<WorkingCapitalPaymentAllocationType> getPaymentAllocationTypes(final JsonArray paymentAllocationOrder) {
        if (paymentAllocationOrder != null) {
            // Validate that paymentAllocationOrder is not empty
            if (paymentAllocationOrder.isEmpty()) {
                validator.raiseValidationError("wc-payment-allocation-order.cannot.be.empty", "Payment allocation order cannot be empty");
            }
            final List<Pair<Integer, WorkingCapitalPaymentAllocationType>> parsedListWithOrder = paymentAllocationOrder.asList().stream()
                    .map(json -> {
                        final Map<String, JsonElement> map = json.getAsJsonObject().asMap();
                        WorkingCapitalPaymentAllocationType paymentAllocationType = null;
                        final String paymentAllocationRule = asStringOrNull(map.get("paymentAllocationRule"));
                        if (paymentAllocationRule != null) {
                            paymentAllocationType = Enums.getIfPresent(WorkingCapitalPaymentAllocationType.class, paymentAllocationRule)
                                    .orNull();
                        }
                        return Pair.of(asIntegerOrNull(map.get("order")), paymentAllocationType);
                    }).toList();
            if (parsedListWithOrder.stream().anyMatch(p -> p.getLeft() == null)) {
                validator.raiseValidationError("wc-payment-allocation-order.order.required",
                        "Each paymentAllocationOrder entry must have an 'order' field.");
            }
            final List<Pair<Integer, WorkingCapitalPaymentAllocationType>> sorted = parsedListWithOrder.stream()
                    .sorted(Comparator.comparing(Pair::getLeft)).toList();
            validator.validatePairOfOrderAndPaymentAllocationType(sorted);
            return sorted.stream().map(Pair::getRight).toList();
        } else {
            return List.of();
        }
    }

    private Integer asIntegerOrNull(final JsonElement element) {
        if (element != null && !element.isJsonNull() && element.isJsonPrimitive()) {
            return element.getAsInt();
        }
        return null;
    }

    private String asStringOrNull(final JsonElement element) {
        if (element != null && !element.isJsonNull() && element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return null;
    }

    private JsonArray asJsonArrayOrNull(final JsonElement element) {
        if (element != null && !element.isJsonNull() && element.isJsonArray()) {
            return element.getAsJsonArray();
        }
        return null;
    }

}
