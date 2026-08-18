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
package org.apache.fineract.commands.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.data.PendingMakerCheckerData;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MakerCheckerReadServiceImpl implements MakerCheckerReadService {

    private final CommandSourceRepository commandSourceRepository;

    @Override
    public List<PendingMakerCheckerData> retrievePendingByLoanId(final Long loanId) {
        return commandSourceRepository
                .findPendingByLoanId(loanId, CommandProcessingResultType.AWAITING_APPROVAL.getValue())
                .stream()
                .map(this::toData)
                .toList();
    }

    @Override
    public List<PendingMakerCheckerData> retrievePendingByClientId(final Long clientId) {
        return commandSourceRepository
                .findPendingByClientId(clientId, CommandProcessingResultType.AWAITING_APPROVAL.getValue())
                .stream()
                .map(this::toData)
                .toList();
    }

    @Override
    public List<PendingMakerCheckerData> retrievePendingBySavingsId(final Long savingsId) {
        return commandSourceRepository
                .findPendingBySavingsId(savingsId, CommandProcessingResultType.AWAITING_APPROVAL.getValue())
                .stream()
                .map(this::toData)
                .toList();
    }

    private PendingMakerCheckerData toData(final CommandSource cs) {
        final String makerUsername = cs.getMaker() != null ? cs.getMaker().getUsername() : null;
        return PendingMakerCheckerData.builder()
                .id(cs.getId())
                .actionName(cs.getActionName())
                .entityName(cs.getEntityName())
                .permissionCode(cs.getPermissionCode())
                .makerUsername(makerUsername)
                .madeOnDate(cs.getMadeOnDate())
                .build();
    }
}
