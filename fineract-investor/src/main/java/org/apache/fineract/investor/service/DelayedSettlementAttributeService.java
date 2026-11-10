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

import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;

public interface DelayedSettlementAttributeService {

    /**
     * Whether the SETTLEMENT_MODEL in force for the loan is DELAYED_SETTLEMENT: the loan-level copy if the loan has
     * one, otherwise the loan product.
     */
    boolean isEnabled(Long loanId, Long loanProductId);

    /**
     * Whether a pending sale was initiated under delayed settlement. This is decided by the transfers of the loan, not
     * by the SETTLEMENT_MODEL attribute, which may have changed between the initiation and the execution of the sale:
     * an intermediary sale, and the investor sale that follows an active intermediary sale, can only be initiated under
     * delayed settlement.
     */
    boolean isSaleUnderDelayedSettlement(ExternalAssetOwnerTransfer saleTransfer);
}
