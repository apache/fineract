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
package org.apache.fineract.investor.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributesData;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributesData.ExternalAssetOwnerLoanAttributeData;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshot;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshotRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalAssetOwnerLoanAttributesReadServiceImpl implements ExternalAssetOwnerLoanAttributesReadService {

    private final ExternalAssetOwnerLoanAttributeSnapshotRepository snapshotRepository;
    private final ExternalAssetOwnerLoanProductAttributesReadService loanProductAttributesReadService;
    private final LoanRepository loanRepository;

    @Override
    public Optional<String> resolveAttributeValue(final Long loanId, final Long loanProductId, final String attributeKey) {
        return Optional.ofNullable(loanId).flatMap(id -> snapshotRepository.findLoanLevelAttributeValue(id, attributeKey))
                .map(loanLevelAttribute -> Optional.ofNullable(loanLevelAttribute.getAttributeValue()))
                .orElseGet(() -> resolveLoanProductAttributeValue(loanProductId, attributeKey));
    }

    @Override
    public ExternalAssetOwnerLoanAttributesData retrieveLoanAttributes(final Long loanId) {
        final LoanDataForExternalTransfer loan = loanRepository.findLoanDataForExternalTransferByLoanId(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        final String loanExternalId = Optional.ofNullable(loan.getExternalId()).map(ExternalId::getValue).orElse(null);
        return snapshotRepository.findByLoanId(loanId).map(snapshot -> toData(loanId, loanExternalId, snapshot))
                .orElseGet(() -> new ExternalAssetOwnerLoanAttributesData(loanId, loanExternalId, false, null, List.of()));
    }

    private Optional<String> resolveLoanProductAttributeValue(final Long loanProductId, final String attributeKey) {
        if (loanProductId == null) {
            return Optional.empty();
        }
        return loanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(loanProductId, attributeKey).getPageItems()
                .stream().findFirst().map(ExternalTransferLoanProductAttributesData::getAttributeValue);
    }

    private static ExternalAssetOwnerLoanAttributesData toData(final Long loanId, final String loanExternalId,
            final ExternalAssetOwnerLoanAttributeSnapshot snapshot) {
        final List<ExternalAssetOwnerLoanAttributeData> attributes = snapshot.getAttributes().stream()
                .map(attribute -> new ExternalAssetOwnerLoanAttributeData(attribute.getAttributeKey(), attribute.getAttributeValue()))
                .toList();
        return new ExternalAssetOwnerLoanAttributesData(loanId, loanExternalId, true, snapshot.getExternalAssetOwnerTransferId(),
                attributes);
    }
}
