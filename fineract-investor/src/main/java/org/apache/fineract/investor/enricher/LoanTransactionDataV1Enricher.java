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
package org.apache.fineract.investor.enricher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.core.service.DataEnricher;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.service.ExcludedTransactionTypesService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanTransactionDataV1Enricher implements DataEnricher<LoanTransactionDataV1> {

    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalIdMapper externalIdMapper;
    private final ExcludedTransactionTypesService excludedTransactionTypesService;
    private final LoanRepository loanRepository;

    @Override
    public Class<LoanTransactionDataV1> getDataType() {
        return LoanTransactionDataV1.class;
    }

    @Override
    public void enrich(LoanTransactionDataV1 data) {
        externalAssetOwnerTransferRepository.findActiveOwnerByLoanId(data.getLoanId())
                .ifPresent(owner -> setExternalOwnerIdUnlessExcluded(data, owner));
    }

    private void setExternalOwnerIdUnlessExcluded(final LoanTransactionDataV1 data, final ExternalAssetOwner owner) {
        if (isExcluded(data)) {
            log.debug("Skipping external asset owner attribution of loan transaction {}: transaction type is excluded for its loan product",
                    data.getId());
            return;
        }
        data.setExternalOwnerId(externalIdMapper.mapExternalId(owner.getExternalId()));
    }

    /**
     * Any record we cannot confidently classify falls back to the previous behaviour of attributing it to the active
     * owner. This runs while the surrounding transaction is being committed, so it must never throw.
     */
    private boolean isExcluded(final LoanTransactionDataV1 data) {
        if (data.getType() == null || data.getType().getId() == null) {
            log.warn("Loan transaction {} has no resolvable transaction type, attributing it to the active external asset owner",
                    data.getId());
            return false;
        }
        LoanTransactionType transactionType = LoanTransactionType.fromInt(data.getType().getId());
        if (LoanTransactionType.INVALID.equals(transactionType)) {
            log.warn("Loan transaction {} has unmappable transaction type id {}, attributing it to the active external asset owner",
                    data.getId(), data.getType().getId());
            return false;
        }
        Long loanProductId = loanRepository.findLoanProductIdByLoanId(data.getLoanId()).orElse(null);
        if (loanProductId == null) {
            log.warn("Could not resolve the loan product of loan {}, attributing its transaction {} to the active external asset owner",
                    data.getLoanId(), data.getId());
            return false;
        }
        return excludedTransactionTypesService.isExcluded(loanProductId, transactionType);
    }
}
