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
package org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.AbstractBusinessEvent;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;

/**
 * Carries data snapshots rather than the transaction entity: external events are serialized in {@code beforeCommit}, by
 * which time reprocessing has already mutated the allocation in place.
 */
public class WorkingCapitalLoanAdjustTransactionBusinessEvent
        extends AbstractBusinessEvent<WorkingCapitalLoanAdjustTransactionBusinessEvent.Data> {

    public static final String TYPE = "WorkingCapitalLoanAdjustTransactionBusinessEvent";
    private static final String CATEGORY = "WorkingCapitalLoan";

    private final Long aggregateRootId;

    public WorkingCapitalLoanAdjustTransactionBusinessEvent(final Data value, final Long aggregateRootId) {
        super(value);
        this.aggregateRootId = aggregateRootId;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Long getAggregateRootId() {
        return aggregateRootId;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Data {

        private final WorkingCapitalLoanTransactionData transactionToAdjust;
        private final WorkingCapitalLoanTransactionData newTransactionDetail;

        public static Data reversal(final WorkingCapitalLoanTransactionData reversedTransaction) {
            return new Data(reversedTransaction, null);
        }
    }
}
