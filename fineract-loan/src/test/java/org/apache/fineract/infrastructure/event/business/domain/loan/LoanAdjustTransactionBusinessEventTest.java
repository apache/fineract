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
package org.apache.fineract.infrastructure.event.business.domain.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.Test;

class LoanAdjustTransactionBusinessEventTest {

    @Test
    void isGovernedByTheEventOfTheAdjustedTransactionType() {
        final LoanTransaction reAgeTransaction = mock(LoanTransaction.class);
        when(reAgeTransaction.getTypeOf()).thenReturn(LoanTransactionType.REAGE);

        final LoanAdjustTransactionBusinessEvent event = new LoanAdjustTransactionBusinessEvent(
                new LoanAdjustTransactionBusinessEvent.Data(reAgeTransaction));

        assertEquals(Optional.of("LoanReAgeTransactionBusinessEvent"), event.getGoverningExternalEventType());
    }

    @Test
    void standsAloneWhenTheAdjustedTransactionTypeHasNoEventOfItsOwn() {
        final LoanTransaction contraTransaction = mock(LoanTransaction.class);
        when(contraTransaction.getTypeOf()).thenReturn(LoanTransactionType.CONTRA);

        final LoanAdjustTransactionBusinessEvent event = new LoanAdjustTransactionBusinessEvent(
                new LoanAdjustTransactionBusinessEvent.Data(contraTransaction));

        assertTrue(event.getGoverningExternalEventType().isEmpty());
    }
}
