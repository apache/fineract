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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistory;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandingInstructionExecutionServiceImpl implements StandingInstructionExecutionService {

    private static final String CLAIM_SQL = """
            UPDATE m_account_transfer_standing_instructions
            SET last_run_date = ?
            WHERE id = ? AND (last_run_date IS NULL OR last_run_date <> ?)
            """;

    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final StandingInstructionHistoryRepository standingInstructionHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public boolean execute(final AccountTransferDTO accountTransferDTO, final Long standingInstructionId, final LocalDate transactionDate,
            final BigDecimal transferredAmount) {
        // Claim first: the conditional update both reserves the instruction for this business date and locks its row,
        // so a replay of the same instruction finds nothing to update and transfers nothing.
        final int claimed = this.jdbcTemplate.update(CLAIM_SQL, transactionDate, standingInstructionId, transactionDate);
        if (claimed == 0) {
            log.debug("Standing instruction {} already executed for {}, skipping", standingInstructionId, transactionDate);
            return false;
        }
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        this.standingInstructionHistoryRepository
                .saveAndFlush(StandingInstructionHistory.success(standingInstructionId, transferredAmount));
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(final Long standingInstructionId, final String errorLog) {
        this.standingInstructionHistoryRepository.saveAndFlush(StandingInstructionHistory.failed(standingInstructionId, errorLog));
    }
}
