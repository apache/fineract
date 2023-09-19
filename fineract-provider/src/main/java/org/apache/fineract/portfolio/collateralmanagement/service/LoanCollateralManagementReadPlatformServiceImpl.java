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
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.exception.LoanCollateralManagementNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class LoanCollateralManagementReadPlatformServiceImpl implements LoanCollateralManagementReadPlatformService {

    private final PlatformSecurityContext context;
    private LoanCollateralManagementRepository loanCollateralManagementRepository;
    private LoanRepository loanRepository;

    public LoanCollateralManagementReadPlatformServiceImpl(final PlatformSecurityContext context,
            final LoanCollateralManagementRepository loanCollateralManagementRepository, final LoanRepository loanRepository) {
        this.context = context;
        this.loanCollateralManagementRepository = loanCollateralManagementRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public List<LoanCollateralManagement> getLoanCollaterals(Long loanId) {
        this.context.authenticatedUser();
        Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        return this.loanCollateralManagementRepository.findByLoan(loan);
    }

    @Override
    public LoanCollateralResponseData getLoanCollateralResponseData(Long collateralId) {
        this.context.authenticatedUser();
        LoanCollateralManagement loanCollateralManagement = this.loanCollateralManagementRepository.findById(collateralId)
                .orElseThrow(() -> new LoanCollateralManagementNotFoundException(collateralId));
        final CollateralManagementDomain collateralManagementDomain = loanCollateralManagement.getClientCollateralManagement()
                .getCollaterals();
        BigDecimal quantity = loanCollateralManagement.getQuantity();
        BigDecimal total = quantity.multiply(collateralManagementDomain.getBasePrice());
        BigDecimal totalCollateral = total.multiply(collateralManagementDomain.getPctToBase()).divide(BigDecimal.valueOf(100));
        return LoanCollateralResponseData.instanceOf(loanCollateralManagement, total, totalCollateral);
    }

    @Override
    public List<LoanCollateralResponseData> getLoanCollateralResponseDataList(Long loanId) {
        this.context.authenticatedUser();
        Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
        List<LoanCollateralResponseData> loanCollateralResponseDataCollection = new ArrayList<>();
        Set<LoanCollateralManagement> loanCollateralManagements = loan.getLoanCollateralManagements();
        for (LoanCollateralManagement loanCollateralManagement : loanCollateralManagements) {
            final CollateralManagementDomain collateralManagementDomain = loanCollateralManagement.getClientCollateralManagement()
                    .getCollaterals();
            BigDecimal quantity = loanCollateralManagement.getQuantity();
            BigDecimal total = quantity.multiply(collateralManagementDomain.getBasePrice());
            BigDecimal totalCollateral = total.multiply(collateralManagementDomain.getPctToBase()).divide(BigDecimal.valueOf(100));
            loanCollateralResponseDataCollection
                    .add(LoanCollateralResponseData.instanceOf(loanCollateralManagement, total, totalCollateral));
        }
        return loanCollateralResponseDataCollection;
    }

}
