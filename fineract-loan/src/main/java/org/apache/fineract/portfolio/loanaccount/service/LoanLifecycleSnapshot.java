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

import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;

public record LoanLifecycleSnapshot(Long loanId, LoanStatus status, List<LoanLifecycleAction> permittedActions) {

    public static LoanLifecycleSnapshot from(LoanAccountData loan) {
        LoanStatus status = loan.getStatus() == null ? LoanStatus.INVALID : LoanStatus.fromInt(loan.getStatus().getId().intValue());
        return new LoanLifecycleSnapshot(loan.getId(), status, permittedActionsFor(status));
    }

    private static List<LoanLifecycleAction> permittedActionsFor(LoanStatus status) {
        return switch (status) {
            case SUBMITTED_AND_PENDING_APPROVAL -> List.of(LoanLifecycleAction.APPROVE, LoanLifecycleAction.REJECT,
                    LoanLifecycleAction.WITHDRAW);
            case APPROVED -> List.of(LoanLifecycleAction.DISBURSE);
            case ACTIVE, OVERPAID -> List.of(LoanLifecycleAction.REPAY, LoanLifecycleAction.CLOSE);
            default -> List.of();
        };
    }
}
