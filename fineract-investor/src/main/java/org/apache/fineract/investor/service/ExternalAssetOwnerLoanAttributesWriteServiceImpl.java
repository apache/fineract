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

import static org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY;
import static org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE;
import static org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute.DEFAULT_SETTLEMENT;
import static org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute.DELAYED_SETTLEMENT;
import static org.apache.fineract.investor.service.LoanAccountOwnerTransferServiceImpl.FUTURE_DATE_9999_12_31;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshot;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributeSnapshotRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanAttributeException;
import org.apache.fineract.investor.serialization.ExternalAssetOwnerAttributeValidator;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExternalAssetOwnerLoanAttributesWriteServiceImpl implements ExternalAssetOwnerLoanAttributesWriteService {

    // transfers whose execution depends on the settlement model they were initiated under
    private static final List<ExternalTransferStatus> SETTLEMENT_MODEL_BOUND_TRANSFER_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.PENDING_INTERMEDIATE, ExternalTransferStatus.ACTIVE_INTERMEDIATE,
            ExternalTransferStatus.BUYBACK_INTERMEDIATE);

    private final ExternalAssetOwnerLoanAttributeSnapshotRepository snapshotRepository;
    private final ExternalAssetOwnerLoanProductAttributesReadService loanProductAttributesReadService;
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerAttributeValidator attributeValidator;
    private final LoanRepository loanRepository;
    private final DelayedSettlementAttributeService delayedSettlementAttributeService;

    @Override
    public void copyLoanProductAttributesIfAbsent(final Loan loan, final ExternalAssetOwnerTransfer saleTransfer) {
        if (snapshotRepository.existsByLoanId(loan.getId())) {
            return;
        }
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = new ExternalAssetOwnerLoanAttributeSnapshot(loan.getId(),
                saleTransfer.getId());
        firstValuePerKey(loan.productId()).forEach(snapshot::putAttribute);
        alignSettlementModelWithExecutedSale(loan.getId(), snapshot, saleTransfer);
        snapshotRepository.saveAndFlush(snapshot);
        log.debug("Copied {} external asset owner loan product attributes of loan product {} to loan {} on transfer {}",
                snapshot.getAttributes().size(), loan.productId(), loan.getId(), saleTransfer.getId());
    }

    /**
     * The settlement model of a sale is decided when the sale is initiated, but the copy is taken when COB executes it.
     * If the product's SETTLEMENT_MODEL changed in between, copying the product value would freeze a settlement model
     * that contradicts the transfers of the loan, and no later sale or buyback of the loan would pass validation. The
     * copy therefore records the settlement model the executed sale was initiated under.
     */
    private void alignSettlementModelWithExecutedSale(final Long loanId, final ExternalAssetOwnerLoanAttributeSnapshot snapshot,
            final ExternalAssetOwnerTransfer saleTransfer) {
        final String settlementModelKey = DELAYED_SETTLEMENT.getAttributeKey();
        final String copiedValue = snapshot.getAttributes().stream()
                .filter(attribute -> attribute.getAttributeKey().equals(settlementModelKey)).findFirst()
                .map(ExternalAssetOwnerLoanAttributes::getAttributeValue).orElse(null);
        final boolean copiedAsDelayed = DELAYED_SETTLEMENT.getAttributeValue().equalsIgnoreCase(copiedValue);
        final boolean executedAsDelayed = delayedSettlementAttributeService.isSaleUnderDelayedSettlement(saleTransfer);
        if (copiedAsDelayed == executedAsDelayed) {
            return;
        }
        final String settlementModel = (executedAsDelayed ? DELAYED_SETTLEMENT : DEFAULT_SETTLEMENT).getAttributeValue();
        log.warn("Loan {}: the loan product SETTLEMENT_MODEL is {} but transfer {} was initiated under {}; the loan-level copy keeps {}",
                loanId, copiedValue, saleTransfer.getId(), settlementModel, settlementModel);
        snapshot.putAttribute(settlementModelKey, settlementModel);
    }

    @Override
    public CommandProcessingResult updateLoanAttribute(final JsonCommand command) {
        final Long loanId = command.getLoanId();
        final String attributeKey = command.stringValueOfParameterNamedAllowingNull(ATTRIBUTE_KEY);
        final String attributeValue = command.stringValueOfParameterNamedAllowingNull(ATTRIBUTE_VALUE);
        attributeValidator.validateRequest(command.json(), attributeKey, attributeValue);
        final String normalizedAttributeValue = attributeValidator.validateAndNormalize(attributeKey, attributeValue);
        final ExternalAssetOwnerLoanAttributeSnapshot snapshot = snapshotRepository.findByLoanIdForUpdate(loanId)
                .orElseThrow(() -> noLoanLevelCopyOrLoanNotFound(loanId));
        if (DELAYED_SETTLEMENT.getAttributeKey().equals(attributeKey) && isSettlementModelBoundTransferInProgress(loanId)) {
            throw ExternalAssetOwnerLoanAttributeException.settlementModelTransferInProgress(loanId);
        }
        snapshot.putAttribute(attributeKey, normalizedAttributeValue);
        snapshotRepository.saveAndFlush(snapshot);
        return new CommandProcessingResultBuilder().withEntityId(loanId).withLoanId(loanId).build();
    }

    @Override
    public void lockLoanLevelCopy(final Long loanId) {
        snapshotRepository.findByLoanIdForUpdate(loanId);
    }

    private RuntimeException noLoanLevelCopyOrLoanNotFound(final Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            return new LoanNotFoundException(loanId);
        }
        return ExternalAssetOwnerLoanAttributeException.noLoanLevelCopy(loanId);
    }

    private Map<String, String> firstValuePerKey(final Long loanProductId) {
        final Map<String, String> attributes = new LinkedHashMap<>();
        loanProductAttributesReadService.retrieveAllLoanProductAttributesByLoanProductId(loanProductId, null).getPageItems()
                .forEach(attribute -> attributes.putIfAbsent(attribute.getAttributeKey(), attribute.getAttributeValue()));
        return attributes;
    }

    private boolean isSettlementModelBoundTransferInProgress(final Long loanId) {
        return !externalAssetOwnerTransferRepository.findAllForUpdateByLoanIdAndStatusInAndEffectiveDateTo(loanId,
                SETTLEMENT_MODEL_BOUND_TRANSFER_STATUSES, FUTURE_DATE_9999_12_31).isEmpty();
    }
}
