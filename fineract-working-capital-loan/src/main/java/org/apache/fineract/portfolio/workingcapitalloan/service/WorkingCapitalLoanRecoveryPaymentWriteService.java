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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;

/**
 * Collects money on a loan that was already written off, and reverses that collection. A recovery payment is the only
 * monetary transaction allowed while the loan is in {@code CLOSED_WRITTEN_OFF}: it recognizes recovery income without
 * touching the zeroed balance or the loan status.
 */
public interface WorkingCapitalLoanRecoveryPaymentWriteService {

    CommandProcessingResult recoveryPayment(Long loanId, JsonCommand command);

    /**
     * Reverses a recovery payment. The loan and the transaction are resolved by the caller, which dispatches the
     * generic transaction-undo command by transaction type.
     */
    CommandProcessingResult undoRecoveryPayment(WorkingCapitalLoan loan, WorkingCapitalLoanTransaction transaction, JsonCommand command);
}
