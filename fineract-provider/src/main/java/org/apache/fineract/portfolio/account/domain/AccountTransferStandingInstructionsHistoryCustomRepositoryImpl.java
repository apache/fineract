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
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountTransferStandingInstructionsHistoryCustomRepositoryImpl
        implements AccountTransferStandingInstructionsHistoryCustomRepository {

    private final EntityManager entityManager;
    private final StandingInstructionRepository standingInstructionRepository;

    @Override
    public void createNewHistory(final long instructionId, final BigDecimal transactionAmount, final boolean transferCompleted,
            final String errorLog) {
        final AccountTransferStandingInstructionsHistory newHistory = new AccountTransferStandingInstructionsHistory();
        newHistory.setAccountTransferStandingInstruction(standingInstructionRepository.getReferenceById(instructionId));
        newHistory.setStatus(transferCompleted ? "success" : "failed");
        newHistory.setAmount(transactionAmount);
        newHistory.setExecutionTime(LocalDateTime.now(DateUtils.getDateTimeZoneOfTenant()));
        newHistory.setErrorLog(errorLog);

        entityManager.persist(newHistory);
    }
}
