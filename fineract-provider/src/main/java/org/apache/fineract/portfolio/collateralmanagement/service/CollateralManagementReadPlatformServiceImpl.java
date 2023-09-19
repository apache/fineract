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

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;

@RequiredArgsConstructor
public class CollateralManagementReadPlatformServiceImpl implements CollateralManagementReadPlatformService {

    private final PlatformSecurityContext context;
    private final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper;

    @Override
    public CollateralManagementData getCollateralProduct(Long collateralId) {
        final CollateralManagementDomain collateralManagementDomain = this.collateralManagementRepositoryWrapper
                .getCollateral(collateralId);
        return CollateralManagementData.createNew(collateralManagementDomain);
    }

    @Override
    public List<CollateralManagementData> getAllCollateralProducts() {
        final List<CollateralManagementDomain> collateralManagementDomainSet = this.collateralManagementRepositoryWrapper
                .getAllCollaterals();
        List<CollateralManagementData> collateralManagementDataList = new ArrayList<>();
        for (CollateralManagementDomain collateralManagementDomain : collateralManagementDomainSet) {
            collateralManagementDataList.add(CollateralManagementData.createNew(collateralManagementDomain));
        }
        return collateralManagementDataList;
    }

}
