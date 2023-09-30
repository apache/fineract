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
package org.apache.fineract.integrationtests.common.loans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;

public class AdvancedPaymentAllocation extends HashMap<String, Object> {

    public static class AdvancedPaymentAllocationBuilder {

        private PaymentAllocationTransactionType transactionType;
        private FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        private List<PaymentAllocationType> paymentAllocationOrder = new ArrayList<>();

        public AdvancedPaymentAllocationBuilder withTransactionType(PaymentAllocationTransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public AdvancedPaymentAllocationBuilder withFutureInstallmentAllocationRule(
                FutureInstallmentAllocationRule futureInstallmentAllocationRule) {
            this.futureInstallmentAllocationRule = futureInstallmentAllocationRule;
            return this;
        }

        public AdvancedPaymentAllocationBuilder withPaymentAllocationType(PaymentAllocationType... allocationTypes) {
            paymentAllocationOrder.addAll(Arrays.asList(allocationTypes));
            return this;
        }

        public AdvancedPaymentAllocation build() {
            AdvancedPaymentAllocation advancedPaymentAllocation = new AdvancedPaymentAllocation();
            advancedPaymentAllocation.put("transactionType", transactionType.name());
            advancedPaymentAllocation.put("futureInstallmentAllocationRule", futureInstallmentAllocationRule.name());
            advancedPaymentAllocation.put("paymentAllocationOrder", getPaymentAllocationOrders());
            return advancedPaymentAllocation;
        }

        public List<Map<String, Object>> getPaymentAllocationOrders() {
            List<Map<String, Object>> result = new ArrayList<>();
            for (int i = 0; i < paymentAllocationOrder.size(); i++) {
                PaymentAllocationType paymentAllocationType = paymentAllocationOrder.get(i);
                result.add(Map.of("paymentAllocationRule", paymentAllocationType.name(), "order", i + 1));
            }
            return result;
        }
    }
}
