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

import java.util.List;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.data.StandingInstructionPartition;

public interface StandingInstructionReadPlatformService {

    StandingInstructionData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType, Integer transferType);

    Page<StandingInstructionData> retrieveAll(StandingInstructionDTO standingInstructionDTO);

    StandingInstructionData retrieveOne(Long instructionId);

    /**
     * Cuts the set of instructions due today into partitions of at most {@code partitionSize} distinct source accounts,
     * so that the batch job can process them in parallel without two partitions contending on the same account.
     */
    List<StandingInstructionPartition> retrieveDuePartitions(Integer status, int partitionSize);

    /**
     * Reads one keyset page of the instructions due today whose source account falls in the given inclusive range,
     * ordered by priority then id.
     *
     * <p>
     * Paging is by keyset rather than offset on purpose: executing an instruction stamps its {@code last_run_date},
     * which removes it from the due set, so an offset would step over unprocessed rows. Pass {@code null} keys to read
     * the first page, then the priority and id of the last item of the previous page.
     * </p>
     */
    List<StandingInstructionData> retrieveDuePage(Integer status, Long minAccountKey, Long maxAccountKey, Integer afterPriority,
            Long afterId, int limit);

    StandingInstructionDuesData retriveLoanDuesData(Long loanId);

}
