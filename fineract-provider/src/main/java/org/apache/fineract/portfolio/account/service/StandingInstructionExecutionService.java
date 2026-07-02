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
package org.apache.fineract.portfolio.account.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;

/**
 * Executes a single standing instruction with per-instruction transactional isolation.
 *
 * <p>
 * Each method runs in its own {@code REQUIRES_NEW} transaction so that a failing instruction can never mark a sibling's
 * transaction rollback-only. A successful execution (transfer + {@code last_run_date} stamp + success history) is one
 * atomic boundary; a failure is recorded in a separate committed boundary, so a rolled-back transfer still leaves a
 * durable {@code failed} history record.
 * </p>
 */
public interface StandingInstructionExecutionService {

    /**
     * Performs the transfer, stamps {@code last_run_date}, and records the {@code success} history row — all atomically
     * in one transaction. Throws on failure, in which case none of the three are persisted and the instruction is
     * re-attempted on the next run.
     */
    void execute(AccountTransferDTO accountTransferDTO, Long standingInstructionId, LocalDate transactionDate,
            BigDecimal transferredAmount);

    /** Records a failed execution in its own committed transaction (durable even when the transfer rolled back). */
    void recordFailure(Long standingInstructionId, String errorLog);
}
