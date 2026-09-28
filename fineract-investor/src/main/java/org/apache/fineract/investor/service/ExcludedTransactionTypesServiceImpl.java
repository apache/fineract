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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.data.attribute.ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcludedTransactionTypesServiceImpl implements ExcludedTransactionTypesService {

    private final ExternalAssetOwnerLoanProductAttributesReadService externalAssetOwnerLoanProductAttributesReadService;

    @Override
    public boolean isExcluded(final Long loanProductId, final LoanTransactionType transactionType) {
        // Guard before touching the read service: its cache key dereferences the loan product id, and an unresolvable
        // transaction type can never be configured as excluded.
        if (loanProductId == null || transactionType == null || LoanTransactionType.INVALID.equals(transactionType)) {
            return false;
        }
        Page<ExternalTransferLoanProductAttributesData> attributesDataPage = externalAssetOwnerLoanProductAttributesReadService
                .retrieveAllLoanProductAttributesByLoanProductId(loanProductId,
                        ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY);
        String attributeValue = attributesDataPage.getPageItems().stream().findFirst()
                .map(ExternalTransferLoanProductAttributesData::getAttributeValue).orElse(null);
        return ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.containsType(attributeValue, transactionType);
    }
}
