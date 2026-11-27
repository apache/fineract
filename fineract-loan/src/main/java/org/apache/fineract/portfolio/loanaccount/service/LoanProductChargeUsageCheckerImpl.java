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
package org.apache.fineract.portfolio.loanaccount.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.charge.service.LoanProductChargeUsageChecker;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanProductChargeUsageCheckerImpl implements LoanProductChargeUsageChecker {

    private final LoanProductRepository loanProductRepository;

    @Override
    public boolean isChargeAttachedToAnyLoanProduct(Long chargeId) {
        final Collection<LoanProduct> loanProducts = this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId);
        return !loanProducts.isEmpty();
    }

    @Override
    public boolean isChargeAttachedToLoanProductNotSupportingDownPaymentCharges(Long chargeId) {
        return this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId).stream()
                .anyMatch(product -> !product.supportsDownPaymentCharges());
    }

    @Override
    public boolean isChargeAttachedToLoanProductWithoutChargeAmortization(Long chargeId) {
        return this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId).stream()
                .anyMatch(product -> !product.getLoanProductRelatedDetail().isEnableChargeAmortization());
    }
}
