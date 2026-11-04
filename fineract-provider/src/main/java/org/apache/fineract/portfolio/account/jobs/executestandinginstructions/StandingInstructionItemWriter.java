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

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistoryStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Transfers the funds for each due instruction of a chunk.
 *
 * <p>
 * Writing joins the chunk transaction, so a whole chunk normally commits at once. Any failure propagates and rolls the
 * chunk back; the step is fault tolerant, so it then replays the chunk one instruction per transaction, committing the
 * instructions that succeed and skipping the one that does not. The rollback also undoes the claims
 * {@link StandingInstructionExecutionService#execute} took, so the replay claims each instruction again; the claim is
 * what stops a second run of the job for the same business date from transferring twice.
 * </p>
 *
 * <p>
 * An instruction that allows partial transfer from a savings account transfers what the account can cover when that is
 * less than the amount due, and is recorded as {@code partial}. The savings account is assembled here, inside the write
 * transaction, because the transfer reuses that instance.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionItemWriter implements ItemWriter<DueStandingInstruction> {

    private final StandingInstructionExecutionService standingInstructionExecutionService;
    private final SavingsAccountAssembler savingsAccountAssembler;

    @Override
    public void write(final Chunk<? extends DueStandingInstruction> chunk) {
        for (final DueStandingInstruction due : chunk) {
            final StandingInstructionData data = due.instruction();
            SavingsAccount fromSavingsAccount = null;
            BigDecimal transactionAmount = due.transactionAmount();
            StandingInstructionHistoryStatus status = StandingInstructionHistoryStatus.SUCCESS;
            if (data.isAllowPartialTransfer() && PortfolioAccountType.SAVINGS.equals(data.getFromAccountTypeEnum())) {
                fromSavingsAccount = savingsAccountAssembler.assembleFrom(data.getFromAccount().getId(), false);
                final BigDecimal coverableAmount = coverableAmount(fromSavingsAccount, transactionAmount);
                // when nothing can be covered the full amount is attempted, so the run is recorded as failed
                if (MathUtil.isGreaterThanZero(coverableAmount) && MathUtil.isLessThan(coverableAmount, transactionAmount)) {
                    transactionAmount = coverableAmount;
                    status = StandingInstructionHistoryStatus.PARTIAL;
                }
            }
            final boolean transferred = standingInstructionExecutionService.execute(
                    accountTransferOf(due, transactionAmount, fromSavingsAccount), data.getId(), due.transactionDate(), transactionAmount,
                    status);
            if (transferred && status == StandingInstructionHistoryStatus.PARTIAL) {
                log.warn("Standing instruction {} transferred only {} of the {} due from savings account {}", data.getId(),
                        transactionAmount, due.transactionAmount(), data.getFromAccount().getId());
            } else if (!transferred) {
                log.info("Standing instruction {} had already run for {}, not transferred again", data.getId(), due.transactionDate());
            }
        }
    }

    /**
     * The most the savings account can transfer of the given amount: all of it when the amount and its withdrawal fee
     * fit in the withdrawable balance, which already allows for the overdraft limit, an enforced minimum balance and
     * holds. Otherwise the withdrawable balance less the fee on it; the fee only grows with the amount, so that always
     * leaves enough for the fee on the smaller transfer.
     */
    static BigDecimal coverableAmount(final SavingsAccount account, final BigDecimal amount) {
        final BigDecimal withdrawable = account.getWithdrawableBalance();
        if (!MathUtil.isGreaterThan(amount.add(account.calculateWithdrawalFee(amount)), withdrawable)) {
            return amount;
        }
        return withdrawable.subtract(account.calculateWithdrawalFee(withdrawable)).setScale(account.getCurrency().getDigitsAfterDecimal(),
                RoundingMode.DOWN);
    }

    private AccountTransferDTO accountTransferOf(final DueStandingInstruction due, final BigDecimal transactionAmount,
            final SavingsAccount fromSavingsAccount) {
        final StandingInstructionData data = due.instruction();
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        return new AccountTransferDTO(due.transactionDate(), transactionAmount, data.getFromAccountTypeEnum(), data.getToAccountTypeEnum(),
                data.getFromAccount().getId(), data.getToAccount().getId(), data.getName() + " Standing instruction trasfer ", null, null,
                null, null, data.toTransferType(), null, null, data.getTransferTypeEnum().getValue(), null, null, ExternalId.empty(), null,
                null, fromSavingsAccount, isRegularTransaction, isExceptionForBalanceCheck);
    }
}
