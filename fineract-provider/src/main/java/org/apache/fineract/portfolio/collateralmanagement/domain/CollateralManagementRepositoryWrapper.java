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

import java.util.List;
import org.apache.fineract.portfolio.collateral.exception.CollateralNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollateralManagementRepositoryWrapper {

    private final CollateralManagementDomainRepository collateralManagementDomainRepository;

    @Autowired
    public CollateralManagementRepositoryWrapper(final CollateralManagementDomainRepository collateralManagementDomainRepository) {
        this.collateralManagementDomainRepository = collateralManagementDomainRepository;
    }

    public CollateralManagementDomain create(CollateralManagementDomain collateralData) {
        return this.collateralManagementDomainRepository.saveAndFlush(collateralData);
    }

    public CollateralManagementDomain getCollateral(Long collateralId) {
        return this.collateralManagementDomainRepository.findById(collateralId)
                .orElseThrow(() -> new CollateralNotFoundException(collateralId));
    }

    public List<CollateralManagementDomain> getAllCollaterals() {
        return this.collateralManagementDomainRepository.findAll();
    }

    public CollateralManagementDomain update(CollateralManagementDomain collateralManagementData) {
        return this.collateralManagementDomainRepository.saveAndFlush(collateralManagementData);
    }

    public void delete(final Long collateralId) {
        this.collateralManagementDomainRepository.deleteById(collateralId);
    }
}
