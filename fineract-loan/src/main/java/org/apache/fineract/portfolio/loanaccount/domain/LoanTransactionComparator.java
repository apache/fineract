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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.Comparator;
import org.apache.fineract.infrastructure.core.service.DateUtils;

/**
 * Sort loan transactions by transaction date, submitted date, created date, transaction type placing, lastly compare
 * ids
 */
public class LoanTransactionComparator implements Comparator<LoanTransaction> {

    public static final LoanTransactionComparator INSTANCE = new LoanTransactionComparator();

    @Override
    public int compare(final LoanTransaction o1, final LoanTransaction o2) {
        int result = DateUtils.compareWithNullsLast(o1.getTransactionDate(), o2.getTransactionDate());
        if (result != 0) {
            return result;
        }
        // Accrual activity as last
        if (o1.isAccrualActivity() != o2.isAccrualActivity()) {
            return o1.isAccrualActivity() ? 1 : -1;
        }
        result = DateUtils.compareWithNullsLast(o1.getSubmittedOnDate(), o2.getSubmittedOnDate());
        if (result != 0) {
            return result;
        }
        result = DateUtils.compareWithNullsLast(o1.getCreatedDate(), o2.getCreatedDate());
        if (result != 0) {
            return result;
        }
        // income posting takes priority
        if (o1.isIncomePosting() != o2.isIncomePosting()) {
            return o1.isIncomePosting() ? -1 : 1;
        }
        // waive takes priority
        if (o1.isWaiver() != o2.isWaiver()) {
            return o1.isWaiver() ? -1 : 1;
        }
        result = o1.getId() != null ? (o2.getId() != null ? Long.compare(o1.getId(), o2.getId()) : -1) : (o2.getId() != null ? 1 : 0);
        if (result != 0) {
            return result;
        }
        return 0;
    }
}
