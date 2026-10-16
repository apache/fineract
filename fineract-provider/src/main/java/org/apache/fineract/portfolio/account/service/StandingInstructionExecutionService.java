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
 * Executes a single standing instruction.
 *
 * <p>
 * {@link #execute} joins the caller's transaction, which for the batch job is the chunk transaction: a chunk of
 * instructions commits together, and if any of them fails the chunk is rolled back and replayed one instruction per
 * transaction, so a failing instruction never leaves a sibling reverted. {@link #recordFailure} runs in its own
 * transaction instead, so the failure record survives the rollback of the transfer that produced it.
 * </p>
 */
public interface StandingInstructionExecutionService {

    /**
     * Claims the instruction for the given business date, performs the transfer, and records the {@code success}
     * history row.
     *
     * <p>
     * The claim is a conditional stamp of {@code last_run_date} taken before the transfer, so an instruction can be
     * executed at most once per business date however many times this method is called — which matters because a
     * rolled-back chunk is replayed item by item, and because a job can be triggered twice.
     * </p>
     *
     * @return {@code false} if the instruction had already run for this business date and nothing was transferred
     */
    boolean execute(AccountTransferDTO accountTransferDTO, Long standingInstructionId, LocalDate transactionDate,
            BigDecimal transferredAmount);

    /** Records a failed execution in its own committed transaction (durable even when the transfer rolled back). */
    void recordFailure(Long standingInstructionId, String errorLog);
}
