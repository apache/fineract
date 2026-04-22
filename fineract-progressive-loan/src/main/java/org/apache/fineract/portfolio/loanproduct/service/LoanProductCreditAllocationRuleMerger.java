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
package org.apache.fineract.portfolio.loanproduct.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductCreditAllocationRule;

public class LoanProductCreditAllocationRuleMerger {

    public boolean updateCreditAllocationRules(LoanProduct loanProduct,
            final List<LoanProductCreditAllocationRule> newLoanProductCreditAllocationRules) {
        if (newLoanProductCreditAllocationRules == null) {
            return false;
        }
        boolean updated = false;
        Map<CreditAllocationTransactionType, LoanProductCreditAllocationRule> originalItems = loanProduct.getCreditAllocationRules()
                .stream().collect(Collectors.toMap(LoanProductCreditAllocationRule::getTransactionType, Function.identity()));
        Map<CreditAllocationTransactionType, LoanProductCreditAllocationRule> newItems = newLoanProductCreditAllocationRules.stream()
                .collect(Collectors.toMap(LoanProductCreditAllocationRule::getTransactionType, Function.identity()));

        // elements to be deleted
        Set<CreditAllocationTransactionType> existing = new HashSet<>(originalItems.keySet());
        Set<CreditAllocationTransactionType> newSet = new HashSet<>(newItems.keySet());
        existing.removeAll(newSet);
        if (existing.size() > 0) {
            updated = true;
            existing.forEach(type -> {
                loanProduct.getCreditAllocationRules().remove(originalItems.get(type));
            });
        }

        // elements to be added
        existing = new HashSet<>(originalItems.keySet());
        newSet = new HashSet<>(newItems.keySet());
        newSet.removeAll(existing);
        if (newSet.size() > 0) {
            updated = true;
            newSet.forEach(type -> {
                loanProduct.getCreditAllocationRules().add(newItems.get(type));
            });
        }

        // elements to be merged
        existing = new HashSet<>(originalItems.keySet());
        newSet = new HashSet<>(newItems.keySet());
        existing.retainAll(newSet);

        for (CreditAllocationTransactionType type : existing) {
            boolean result = mergeLoanProductCreditAllocationRule(originalItems.get(type), newItems.get(type));
            if (result) {
                updated = true;
            }
        }

        return updated;
    }

    private boolean mergeLoanProductCreditAllocationRule(LoanProductCreditAllocationRule into, LoanProductCreditAllocationRule newElement) {
        boolean changed = false;

        if (!Objects.equals(into.getAllocationTypes(), newElement.getAllocationTypes())) {
            into.setAllocationTypes(newElement.getAllocationTypes());
            changed = true;
        }

        return changed;
    }
}
