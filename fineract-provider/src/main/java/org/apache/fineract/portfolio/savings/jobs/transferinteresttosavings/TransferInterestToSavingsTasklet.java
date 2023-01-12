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
package org.apache.fineract.portfolio.savings.jobs.transferinteresttosavings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.event.business.annotation.BulkEventSupport;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
@BulkEventSupport
public class TransferInterestToSavingsTasklet implements Tasklet {

    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<Throwable> errors = new ArrayList<>();
        Collection<AccountTransferDTO> accountTransferData = depositAccountReadPlatformService.retrieveDataForInterestTransfer();
        for (AccountTransferDTO accountTransferDTO : accountTransferData) {
            try {
                accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
            } catch (final PlatformApiDataValidationException e) {
                log.error("Validation exception while trasfering Interest from {} to {}", accountTransferDTO.getFromAccountId(),
                        accountTransferDTO.getToAccountId(), e);
                errors.add(e);
            } catch (final InsufficientAccountBalanceException e) {
                log.error("InsufficientAccountBalanceException while trasfering Interest from {} to {} ",
                        accountTransferDTO.getFromAccountId(), accountTransferDTO.getToAccountId(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
        return RepeatStatus.FINISHED;
    }
}
