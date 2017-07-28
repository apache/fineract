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

/**
 * Sort loan transactions by transaction date, created date and transaction type
 * placing
 */
public class LoanTransactionComparator implements Comparator<LoanTransaction> {

    @Override
    public int compare(final LoanTransaction o1, final LoanTransaction o2) {
        int compareResult = 0;
        final int comparsion = o1.getTransactionDate().compareTo(o2.getTransactionDate());
        /**
         * For transactions bearing the same transaction date, we sort
         * transactions based on created date (when available) after which
         * sorting for waivers takes place
         **/
        if (comparsion == 0) {
            int comparisonBasedOnCreatedDate = 0;
            if (o1.isIncomePosting() && o2.isNotIncomePosting()) {
                compareResult = -1;
            } else if (o1.isNotIncomePosting() && o2.isIncomePosting()) {
                compareResult = 1;
            } else {
                compareResult = 0;
            }
            if (o1.getCreatedDateTime() != null && o2.getCreatedDateTime() != null) {
                comparisonBasedOnCreatedDate = o1.getCreatedDateTime().compareTo(o2.getCreatedDateTime());
            }
            // equal transaction dates
            if (comparisonBasedOnCreatedDate == 0) {
                if (o1.isWaiver() && o2.isNotWaiver()) {
                    compareResult = -1;
                } else if (o1.isNotWaiver() && o2.isWaiver()) {
                    compareResult = 1;
                } else {
                    compareResult = 0;
                }
            }
        } else {
            compareResult = comparsion;
        }

        return compareResult;
    }

}
