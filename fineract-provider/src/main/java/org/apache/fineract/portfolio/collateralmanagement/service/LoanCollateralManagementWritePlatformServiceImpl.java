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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralDeleteRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralDeletelResponse;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class LoanCollateralManagementWritePlatformServiceImpl implements LoanCollateralManagementWritePlatformService {

    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

    @Transactional
    @Override
    public LoanCollateralDeletelResponse deleteLoanCollateral(LoanCollateralDeleteRequest request) {
        final Long id = request.getCollateralId(); // entityId
        final LoanCollateralManagement loanCollateralManagement = this.loanCollateralManagementRepository.findById(id).orElseThrow();
        ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();
        BigDecimal loanQuantity = loanCollateralManagement.getQuantity();
        BigDecimal clientQuantity = clientCollateralManagement.getQuantity();
        clientCollateralManagement.updateQuantity(clientQuantity.add(loanQuantity));

        this.clientCollateralManagementRepositoryWrapper.saveAndFlush(clientCollateralManagement);
        this.loanCollateralManagementRepository.deleteById(id);

        return LoanCollateralDeletelResponse.builder().resourceId(id).loanId(loanCollateralManagement.getId()).build();
    }
}
