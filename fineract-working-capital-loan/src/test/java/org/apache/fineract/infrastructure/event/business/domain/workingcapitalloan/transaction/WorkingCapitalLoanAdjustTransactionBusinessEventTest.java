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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanAdjustTransactionBusinessEventTest {

    private static final Long LOAN_ID = 7L;

    @Test
    void isGovernedByTheEventOfTheAdjustedTransactionType() {
        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                WorkingCapitalLoanAdjustTransactionBusinessEvent.Data.reversal(snapshotOf(LoanTransactionType.DISCOUNT_FEE_AMORTIZATION)),
                LOAN_ID);

        assertEquals(Optional.of("WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent"),
                event.getGoverningExternalEventType());
    }

    @Test
    void standsAloneWhenTheAdjustedTransactionTypeHasNoEventOfItsOwn() {
        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                WorkingCapitalLoanAdjustTransactionBusinessEvent.Data.reversal(snapshotOf(LoanTransactionType.CONTRA)), LOAN_ID);

        assertTrue(event.getGoverningExternalEventType().isEmpty());
    }

    @Test
    void standsAloneWhenTheSnapshotCarriesNoTransactionType() {
        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                WorkingCapitalLoanAdjustTransactionBusinessEvent.Data.reversal(WorkingCapitalLoanTransactionData.builder().id(1L).build()),
                LOAN_ID);

        assertTrue(event.getGoverningExternalEventType().isEmpty());
    }

    private static WorkingCapitalLoanTransactionData snapshotOf(final LoanTransactionType type) {
        return WorkingCapitalLoanTransactionData.builder().id(1L)
                .type(new LoanTransactionEnumData(type.getValue().longValue(), type.getCode(), type.name())).build();
    }
}
