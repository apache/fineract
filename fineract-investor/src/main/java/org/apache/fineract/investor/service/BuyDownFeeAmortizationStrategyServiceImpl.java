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

import static org.apache.fineract.investor.data.attribute.BuyDownFeeAmortizationStrategyExternalAssetOwnerLoanProductAttribute.IMMEDIATE;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.BuyDownFeeAmortizationStrategyService;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service("buyDownFeeAmortizationStrategyServiceImpl")
@Conditional(InvestorModuleIsEnabledCondition.class)
@RequiredArgsConstructor
public class BuyDownFeeAmortizationStrategyServiceImpl implements BuyDownFeeAmortizationStrategyService {

    private final ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;

    @Override
    public boolean shouldRecognizeImmediately(@NonNull final Loan loan) {
        if (externalAssetOwnerTransferRepository.findActiveByLoanId(loan.getId()).isEmpty()) {
            return false;
        }
        return externalAssetOwnerLoanProductAttributesReadService.getAttributeValue(loan.productId(), IMMEDIATE.getAttributeKey())
                .map(value -> IMMEDIATE.getAttributeValue().equalsIgnoreCase(value)).orElse(false);
    }
}
