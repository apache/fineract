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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.client.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientCollateralReadServiceImpl implements ClientCollateralReadService {

    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;

    @Override
    public Set<ClientCollateralManagementData> retrieveCollateralDataForClient(final Long clientId) {
        final Collection<ClientCollateralManagement> clientCollateralManagements = this.clientCollateralManagementRepositoryWrapper
                .getCollateralsPerClient(clientId);
        final Set<ClientCollateralManagementData> clientCollateralManagementDataSet = new HashSet<>();
        for (final ClientCollateralManagement clientCollateralManagement : clientCollateralManagements) {
            final BigDecimal total = clientCollateralManagement.getTotal();
            final BigDecimal totalCollateral = clientCollateralManagement.getTotalCollateral(total);
            clientCollateralManagementDataSet.add(new ClientCollateralManagementData(clientCollateralManagement.getId(),
                    clientCollateralManagement.getCollaterals().getName(), clientCollateralManagement.getQuantity(),
                    clientCollateralManagement.getCollaterals().getPctToBase(), clientCollateralManagement.getCollaterals().getBasePrice(),
                    total, totalCollateral));
        }
        return clientCollateralManagementDataSet;
    }
}
