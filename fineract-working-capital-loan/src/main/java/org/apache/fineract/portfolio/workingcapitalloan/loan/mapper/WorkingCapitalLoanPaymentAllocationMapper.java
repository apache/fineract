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
package org.apache.fineract.portfolio.workingcapitalloan.loan.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.workingcapitalloan.product.data.WorkingCapitalLoanPaymentAllocationData;
import org.apache.fineract.portfolio.workingcapitalloan.product.domain.WorkingCapitalLoanPaymentAllocationType;
import org.apache.fineract.portfolio.workingcapitalloan.transaction.domain.WorkingCapitalLoanPaymentAllocationRule;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface WorkingCapitalLoanPaymentAllocationMapper {

    @Named("paymentAllocationRulesToData")
    default List<WorkingCapitalLoanPaymentAllocationData> paymentAllocationRulesToData(
            final List<WorkingCapitalLoanPaymentAllocationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        final List<WorkingCapitalLoanPaymentAllocationData> result = new ArrayList<>();
        for (WorkingCapitalLoanPaymentAllocationRule rule : rules) {
            final AtomicInteger counter = new AtomicInteger(1);
            final List<WorkingCapitalLoanPaymentAllocationData.PaymentAllocationOrder> orders = new ArrayList<>();
            if (rule.getAllocationTypes() != null) {
                for (WorkingCapitalLoanPaymentAllocationType type : rule.getAllocationTypes()) {
                    orders.add(new WorkingCapitalLoanPaymentAllocationData.PaymentAllocationOrder(type.name(), counter.getAndIncrement()));
                }
            }
            result.add(new WorkingCapitalLoanPaymentAllocationData(rule.getTransactionType(), orders));
        }
        return result;
    }
}
