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

package org.apache.fineract.portfolio.self.pockets.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;

@RequiredArgsConstructor
public class AccountEntityServiceForLoanImpl implements AccountEntityService {

    private static final String KEY = EntityAccountType.LOAN.name();

    private final PlatformSecurityContext context;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;
    private final LoanReadPlatformService loanReadPlatformService;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void validateSelfUserAccountMapping(Long accountId) {
        if (!this.appuserLoansMapperReadService.isLoanMappedToUser(accountId, this.context.authenticatedUser().getId())) {
            throw new LoanNotFoundException(accountId);
        }
    }

    @Override
    public String retrieveAccountNumberByAccountId(Long accountId) {
        return this.loanReadPlatformService.retrieveAccountNumberByAccountId(accountId);
    }
}
