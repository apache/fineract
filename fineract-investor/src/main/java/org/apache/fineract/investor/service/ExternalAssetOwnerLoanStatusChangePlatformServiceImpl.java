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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalAssetOwnerLoanStatusChangePlatformServiceImpl implements ExternalAssetOwnerLoanStatusChangePlatformService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final LoanAccountOwnerTransferService loanAccountOwnerTransferService;

    private static final String ASSET_EXTERNALIZATION_OF_NON_ACTIVE_LOANS = "asset-externalization-of-non-active-loans";

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanStatusChangedBusinessEvent.class, event -> {
            final Loan loan = event.get();
            if (configurationReadPlatformService.retrieveGlobalConfiguration(ASSET_EXTERNALIZATION_OF_NON_ACTIVE_LOANS).isEnabled()
                    && (event.getOldStatus().isActive() && (loan.isClosed() || loan.getStatus().isOverpaid()))) {
                loanAccountOwnerTransferService.handleLoanClosedOrOverpaid(loan);
            }
        });
    }
}
