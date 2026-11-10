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

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@RequiredArgsConstructor
public class LoanTransferabilityServiceImpl implements LoanTransferabilityService {

    private final DelayedSettlementAttributeService delayedSettlementAttributeService;

    @Override
    public boolean isTransferable(final Loan loan, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (shouldValidateTransferable(externalAssetOwnerTransfer)) {
            return MathUtil.nullToDefault(loan.getSummary().getTotalOutstanding(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
        }

        return true;
    }

    @Override
    public ExternalTransferSubStatus getDeclinedSubStatus(final Loan loan) {
        if (MathUtil.nullToDefault(loan.getTotalOverpaid(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            return ExternalTransferSubStatus.BALANCE_NEGATIVE;
        }

        return ExternalTransferSubStatus.BALANCE_ZERO;
    }

    private boolean shouldValidateTransferable(final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        // A sale from the intermediary to the investor is not validated; every other sale is. Decided by the transfers
        // rather than by the SETTLEMENT_MODEL attribute, which may have changed since the sale was initiated.
        return ExternalTransferStatus.PENDING_INTERMEDIATE == externalAssetOwnerTransfer.getStatus()
                || !delayedSettlementAttributeService.isSaleUnderDelayedSettlement(externalAssetOwnerTransfer);
    }
}
