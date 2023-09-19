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
package org.apache.fineract.portfolio.collateralmanagement.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralTemplateData;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanTransactionData;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;

@RequiredArgsConstructor
public class ClientCollateralManagementReadPlatformServiceImpl implements ClientCollateralManagementReadPlatformService {

    private final PlatformSecurityContext context;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public List<ClientCollateralManagementData> getClientCollaterals(final Long clientId, final Long prodId) {
        return this.clientCollateralManagementRepositoryWrapper.getClientCollateralData(clientId, prodId);
    }

    @Override
    public List<LoanCollateralTemplateData> getLoanCollateralTemplate(Long clientId) {
        this.context.authenticatedUser();
        Collection<ClientCollateralManagement> clientCollateralManagements = this.clientCollateralManagementRepositoryWrapper
                .getCollateralsPerClient(clientId);
        List<LoanCollateralTemplateData> loanCollateralTemplateDataList = new ArrayList<>();
        for (ClientCollateralManagement clientCollateralManagement : clientCollateralManagements) {
            loanCollateralTemplateDataList.add(LoanCollateralTemplateData.instanceOf(clientCollateralManagement));
        }
        return loanCollateralTemplateDataList;
    }

    @Override
    public ClientCollateralManagementData getClientCollateralManagementData(final Long collateralId) {
        final ClientCollateralManagement clientCollateralManagement = this.clientCollateralManagementRepositoryWrapper
                .getCollateral(collateralId);
        BigDecimal basePrice = clientCollateralManagement.getCollaterals().getBasePrice();
        BigDecimal pctToBase = clientCollateralManagement.getCollaterals().getPctToBase().divide(BigDecimal.valueOf(100));
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalCollateral = BigDecimal.ZERO;
        BigDecimal quantity = clientCollateralManagement.getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            total = basePrice.multiply(quantity);
            totalCollateral = total.multiply(pctToBase);
        }
        Set<LoanCollateralManagement> loanCollateralManagementSet = clientCollateralManagement.getLoanCollateralManagementSet();

        List<LoanTransactionData> loanTransactionDataList = new ArrayList<>();
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagementSet) {
            if (loanCollateralManagement.getLoanTransaction() != null) {
                Long transactionId = loanCollateralManagement.getLoanTransaction().getId();
                LoanTransaction loanTransaction = this.loanTransactionRepository.findById(transactionId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(transactionId));
                LoanTransactionData loanTransactionData = LoanTransactionData.instance(loanTransaction.getLoan().getId(),
                        loanTransaction.getCreatedDateTime(), loanTransaction.getOutstandingLoanBalance(),
                        loanTransaction.getPrincipalPortion());
                loanTransactionDataList.add(loanTransactionData);
            }
        }

        return ClientCollateralManagementData.instance(clientCollateralManagement.getCollaterals().getName(),
                clientCollateralManagement.getQuantity(), total, totalCollateral, clientCollateralManagement.getClient().getId(),
                loanTransactionDataList, clientCollateralManagement.getId());
    }
}
