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
package org.apache.fineract.portfolio.collateralmanagement.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.exception.ClientCollateralNotFoundException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCollateralManagementRepositoryWrapper {

    private final ClientCollateralManagementRepository clientCollateralManagementRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final LoanProductRepository loanProductRepository;

    @Autowired
    public ClientCollateralManagementRepositoryWrapper(final ClientCollateralManagementRepository clientCollateralManagementRepository,
            final ClientRepositoryWrapper clientRepositoryWrapper, final LoanProductRepository loanProductRepository) {
        this.clientCollateralManagementRepository = clientCollateralManagementRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.loanProductRepository = loanProductRepository;
    }

    public List<ClientCollateralManagement> getCollateralsPerClient(final Long clientId) {
        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        return this.clientCollateralManagementRepository.findByClientId(client);
    }

    public List<ClientCollateralManagementData> getClientCollateralData(final Long clientId, final Long prodId) {
        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        String currency = null;
        if (prodId != null) {
            final LoanProduct loanProduct = this.loanProductRepository.findById(prodId)
                    .orElseThrow(() -> new LoanProductNotFoundException(prodId));
            currency = loanProduct.getCurrency().getCode();
        }
        List<ClientCollateralManagement> clientCollateralManagements = this.clientCollateralManagementRepository.findByClientId(client);
        List<ClientCollateralManagementData> clientCollateralManagementDataSet = new ArrayList<>();
        for (ClientCollateralManagement clientCollateralManagement : clientCollateralManagements) {
            BigDecimal quantity = clientCollateralManagement.getQuantity();
            BigDecimal total = clientCollateralManagement.getTotal();
            BigDecimal totalCollateralValue = clientCollateralManagement.getTotalCollateral(total);
            if (prodId != null && clientCollateralManagement.getCollaterals().getCurrency().getCode().equals(currency)) {
                clientCollateralManagementDataSet
                        .add(ClientCollateralManagementData.instance(clientCollateralManagement.getCollaterals().getName(), quantity, total,
                                totalCollateralValue, clientId, null, clientCollateralManagement.getId()));
            }
        }

        return clientCollateralManagementDataSet;
    }

    public ClientCollateralManagement getCollateral(final Long collateralId) {
        return this.clientCollateralManagementRepository.findById(collateralId)
                .orElseThrow(() -> new ClientCollateralNotFoundException(collateralId));
    }

    public ClientCollateralManagement updateClientCollateralProduct(final ClientCollateralManagement clientCollateralManagement) {
        return this.clientCollateralManagementRepository.saveAndFlush(clientCollateralManagement);
    }

    public void deleteClientCollateralProduct(final Long collateralId) {
        this.clientCollateralManagementRepository.deleteById(collateralId);
    }

    public void save(ClientCollateralManagement clientCollateralManagement) {
        this.clientCollateralManagementRepository.save(clientCollateralManagement);
    }

    public void saveAndFlush(ClientCollateralManagement clientCollateralManagement) {
        this.clientCollateralManagementRepository.saveAndFlush(clientCollateralManagement);
    }
}
