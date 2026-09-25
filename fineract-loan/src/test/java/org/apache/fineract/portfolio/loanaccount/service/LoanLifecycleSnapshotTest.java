/*
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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.junit.jupiter.api.Test;

class LoanLifecycleSnapshotTest {

    @Test
    void submittedLoanAllowsApplicationDecisions() {
        LoanAccountData loan = new LoanAccountData().setId(11L)
                .setStatus(LoanEnumerations.status(100));

        LoanLifecycleSnapshot snapshot = LoanLifecycleSnapshot.from(loan);

        assertEquals(List.of(LoanLifecycleAction.APPROVE, LoanLifecycleAction.REJECT, LoanLifecycleAction.WITHDRAW),
                snapshot.permittedActions());
    }

    @Test
    void activeLoanAllowsRepaymentAndClosure() {
        LoanAccountData loan = new LoanAccountData().setId(12L)
                .setStatus(LoanEnumerations.status(300));

        LoanLifecycleSnapshot snapshot = LoanLifecycleSnapshot.from(loan);

        assertEquals(List.of(LoanLifecycleAction.REPAY, LoanLifecycleAction.CLOSE), snapshot.permittedActions());
    }
}
