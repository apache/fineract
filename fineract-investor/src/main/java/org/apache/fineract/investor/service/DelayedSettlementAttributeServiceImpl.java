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

import static org.apache.fineract.investor.data.attribute.SettlementModelExternalAssetOwnerLoanProductAttribute.DELAYED_SETTLEMENT;
import static org.apache.fineract.investor.service.LoanAccountOwnerTransferServiceImpl.FUTURE_DATE_9999_12_31;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DelayedSettlementAttributeServiceImpl implements DelayedSettlementAttributeService {

    private final ExternalAssetOwnerLoanAttributesReadService externalAssetOwnerLoanAttributesReadService;
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

    @Override
    public boolean isEnabled(final Long loanId, final Long loanProductId) {
        // values stored before validation became case sensitive may be lower case
        return externalAssetOwnerLoanAttributesReadService
                .resolveAttributeValue(loanId, loanProductId, DELAYED_SETTLEMENT.getAttributeKey())
                .map(value -> DELAYED_SETTLEMENT.getAttributeValue().equalsIgnoreCase(value)).orElse(false);
    }

    @Override
    public boolean isSaleUnderDelayedSettlement(final ExternalAssetOwnerTransfer saleTransfer) {
        return ExternalTransferStatus.PENDING_INTERMEDIATE == saleTransfer.getStatus()
                || !externalAssetOwnerTransferRepository.findAllByLoanIdAndStatusInAndEffectiveDateTo(saleTransfer.getLoanId(),
                        List.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE), FUTURE_DATE_9999_12_31).isEmpty();
    }
}
