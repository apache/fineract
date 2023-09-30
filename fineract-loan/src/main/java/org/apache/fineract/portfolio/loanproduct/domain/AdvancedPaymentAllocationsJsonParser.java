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

import com.google.common.base.Enums;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdvancedPaymentAllocationsJsonParser {

    public final AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;

    public List<LoanProductPaymentAllocationRule> assembleLoanProductPaymentAllocationRules(final JsonCommand command,
            String loanTransactionProcessingStrategyCode) {
        JsonArray paymentAllocations = command.arrayOfParameterNamed("paymentAllocation");
        List<LoanProductPaymentAllocationRule> productPaymentAllocationRules = null;
        if (paymentAllocations != null) {
            productPaymentAllocationRules = paymentAllocations.asList().stream().map(json -> {
                Map<String, JsonElement> map = json.getAsJsonObject().asMap();
                LoanProductPaymentAllocationRule loanProductPaymentAllocationRule = new LoanProductPaymentAllocationRule();
                populatePaymentAllocationRules(map, loanProductPaymentAllocationRule);
                populateFutureInstallment(map, loanProductPaymentAllocationRule);
                populateTransactionType(map, loanProductPaymentAllocationRule);
                return loanProductPaymentAllocationRule;
            }).toList();
        }
        advancedPaymentAllocationsValidator.validate(productPaymentAllocationRules, loanTransactionProcessingStrategyCode);
        return productPaymentAllocationRules;
    }

    private void populatePaymentAllocationRules(Map<String, JsonElement> map,
            LoanProductPaymentAllocationRule loanProductPaymentAllocationRule) {
        JsonArray paymentAllocationOrder = asJsonArrayOrNull(map.get("paymentAllocationOrder"));
        if (paymentAllocationOrder != null) {
            loanProductPaymentAllocationRule.setAllocationTypes(getPaymentAllocationTypes(paymentAllocationOrder));
        }
    }

    private void populateFutureInstallment(Map<String, JsonElement> map,
            LoanProductPaymentAllocationRule loanProductPaymentAllocationRule) {
        String futureInstallmentAllocationRule = asStringOrNull(map.get("futureInstallmentAllocationRule"));
        if (futureInstallmentAllocationRule != null) {
            loanProductPaymentAllocationRule.setFutureInstallmentAllocationRule(
                    Enums.getIfPresent(FutureInstallmentAllocationRule.class, futureInstallmentAllocationRule).orNull());
        }
    }

    private void populateTransactionType(Map<String, JsonElement> map, LoanProductPaymentAllocationRule loanProductPaymentAllocationRule) {
        String transactionType = asStringOrNull(map.get("transactionType"));
        if (transactionType != null) {
            loanProductPaymentAllocationRule
                    .setTransactionType(Enums.getIfPresent(PaymentAllocationTransactionType.class, transactionType).orNull());
        }
    }

    @NotNull
    private List<PaymentAllocationType> getPaymentAllocationTypes(JsonArray paymentAllocationOrder) {
        if (paymentAllocationOrder != null) {
            List<Pair<Integer, PaymentAllocationType>> parsedListWithOrder = paymentAllocationOrder.asList().stream().map(json -> {
                Map<String, JsonElement> map = json.getAsJsonObject().asMap();
                PaymentAllocationType paymentAllocationType = null;
                String paymentAllocationRule = asStringOrNull(map.get("paymentAllocationRule"));
                if (paymentAllocationRule != null) {
                    paymentAllocationType = Enums.getIfPresent(PaymentAllocationType.class, paymentAllocationRule).orNull();
                }
                return Pair.of(asIntegerOrNull(map.get("order")), paymentAllocationType);
            }).sorted(Comparator.comparing(Pair::getLeft)).toList();
            advancedPaymentAllocationsValidator.validatePairOfOrderAndPaymentAllocationType(parsedListWithOrder);
            return parsedListWithOrder.stream().map(Pair::getRight).toList();
        } else {
            return List.of();
        }
    }

    private Integer asIntegerOrNull(JsonElement element) {
        if (!element.isJsonNull()) {
            return element.getAsInt();
        }
        return null;
    }

    private String asStringOrNull(JsonElement element) {
        if (!element.isJsonNull()) {
            return element.getAsString();
        }
        return null;
    }

    private JsonArray asJsonArrayOrNull(JsonElement element) {
        if (!element.isJsonNull()) {
            return element.getAsJsonArray();
        }
        return null;
    }

}
