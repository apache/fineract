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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Transfers the funds for each due instruction of a chunk.
 *
 * <p>
 * Writing joins the chunk transaction, so a whole chunk normally commits at once. Any failure propagates and rolls the
 * chunk back; the step is fault tolerant, so it then replays the chunk one instruction per transaction, committing the
 * instructions that succeed and skipping the one that does not. That replay is why
 * {@link StandingInstructionExecutionService#execute} claims the instruction before transferring — without the claim,
 * an instruction that had already moved money could move it again.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionItemWriter implements ItemWriter<DueStandingInstruction> {

    private final StandingInstructionExecutionService standingInstructionExecutionService;

    @Override
    public void write(final Chunk<? extends DueStandingInstruction> chunk) {
        for (final DueStandingInstruction due : chunk) {
            final StandingInstructionData data = due.instruction();
            final boolean transferred = standingInstructionExecutionService.execute(accountTransferOf(due), data.getId(),
                    due.transactionDate(), due.transactionAmount());
            if (!transferred) {
                log.info("Standing instruction {} had already run for {}, not transferred again", data.getId(), due.transactionDate());
            }
        }
    }

    private AccountTransferDTO accountTransferOf(final DueStandingInstruction due) {
        final StandingInstructionData data = due.instruction();
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        return new AccountTransferDTO(due.transactionDate(), due.transactionAmount(), data.getFromAccountTypeEnum(),
                data.getToAccountTypeEnum(), data.getFromAccount().getId(), data.getToAccount().getId(),
                data.getName() + " Standing instruction trasfer ", null, null, null, null, data.toTransferType(), null, null,
                data.getTransferTypeEnum().getValue(), null, null, ExternalId.empty(), null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
    }
}
