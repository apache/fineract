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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.springframework.stereotype.Component;

/**
 * Validates paymentAllocation JSON structure (transactionType, paymentAllocationOrder with paymentAllocationRule and
 * order). Used by both product and loan application validators.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalPaymentAllocationDataValidator {

    private static final Set<String> SUPPORTED_PAYMENT_ALLOCATION_RULE_PARAMS = new HashSet<>(
            Arrays.asList("transactionType", "paymentAllocationOrder"));
    private static final Set<String> SUPPORTED_PAYMENT_ALLOCATION_ORDER_PARAMS = new HashSet<>(
            Arrays.asList("paymentAllocationRule", "order"));

    private final FromJsonHelper fromApiJsonHelper;

    /**
     * Validates paymentAllocation when present: array of rules, each with transactionType and paymentAllocationOrder
     * (array of paymentAllocationRule + order).
     */
    public void validate(final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        if (element == null || !element.isJsonObject()) {
            return;
        }
        final JsonElement paymentAllocationElement = element.getAsJsonObject()
                .get(WorkingCapitalLoanProductConstants.paymentAllocationParamName);
        if (paymentAllocationElement == null || !paymentAllocationElement.isJsonArray()) {
            return;
        }
        final String orderParamPath = WorkingCapitalLoanProductConstants.paymentAllocationParamName + ".paymentAllocationOrder";
        for (final JsonElement ruleEl : paymentAllocationElement.getAsJsonArray()) {
            if (ruleEl == null || !ruleEl.isJsonObject()) {
                continue;
            }
            final JsonObject rule = ruleEl.getAsJsonObject();
            fromApiJsonHelper.checkForUnsupportedNestedParameters(WorkingCapitalLoanProductConstants.paymentAllocationParamName, rule,
                    SUPPORTED_PAYMENT_ALLOCATION_RULE_PARAMS);
            final String transactionType = fromApiJsonHelper.extractStringNamed("transactionType", rule);
            baseDataValidator.reset().parameter("paymentAllocation.transactionType").value(transactionType).notBlank();
            final JsonElement orderEl = rule.get("paymentAllocationOrder");
            if (orderEl == null) {
                baseDataValidator.reset().parameter("paymentAllocation.paymentAllocationOrder").value(null).notNull();
            } else if (!orderEl.isJsonArray()) {
                baseDataValidator.reset().parameter("paymentAllocation.paymentAllocationOrder").failWithCode("must.be.array",
                        "paymentAllocationOrder must be an array");
            } else {
                for (final JsonElement orderItemEl : orderEl.getAsJsonArray()) {
                    if (orderItemEl == null || !orderItemEl.isJsonObject()) {
                        continue;
                    }
                    final JsonObject orderItem = orderItemEl.getAsJsonObject();
                    fromApiJsonHelper.checkForUnsupportedNestedParameters(orderParamPath, orderItem,
                            SUPPORTED_PAYMENT_ALLOCATION_ORDER_PARAMS);
                    final String paymentAllocationRule = fromApiJsonHelper.extractStringNamed("paymentAllocationRule", orderItem);
                    baseDataValidator.reset().parameter(orderParamPath + ".paymentAllocationRule").value(paymentAllocationRule).notBlank();
                    final Integer order = fromApiJsonHelper.extractIntegerNamed("order", orderItem, Locale.getDefault());
                    baseDataValidator.reset().parameter(orderParamPath + ".order").value(order).notNull();
                }
            }
        }
    }
}
