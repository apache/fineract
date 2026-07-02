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
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistory;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StandingInstructionExecutionServiceImpl implements StandingInstructionExecutionService {

    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final StandingInstructionHistoryRepository standingInstructionHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(final AccountTransferDTO accountTransferDTO, final Long standingInstructionId, final LocalDate transactionDate,
            final BigDecimal transferredAmount) {
        this.accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        // Transfer, last_run_date stamp and success history are one atomic boundary: if any step fails the transfer
        // rolls
        // back too, so the instruction is re-attempted next run (no money moved, no misleading history, last_run_date
        // untouched) rather than leaving a committed transfer with no success record.
        this.jdbcTemplate.update("UPDATE m_account_transfer_standing_instructions SET last_run_date = ? WHERE id = ?", transactionDate,
                standingInstructionId);
        this.standingInstructionHistoryRepository
                .saveAndFlush(StandingInstructionHistory.success(standingInstructionId, transferredAmount));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(final Long standingInstructionId, final String errorLog) {
        this.standingInstructionHistoryRepository.saveAndFlush(StandingInstructionHistory.failed(standingInstructionId, errorLog));
    }
}
