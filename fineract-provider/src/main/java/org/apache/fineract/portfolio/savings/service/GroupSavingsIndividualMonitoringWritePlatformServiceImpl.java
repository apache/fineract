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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.GroupSavingsIndividualMonitoring;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class GroupSavingsIndividualMonitoringWritePlatformServiceImpl implements GroupSavingsIndividualMonitoringWritePlatformService {

    private final PlatformSecurityContext context;

    private final GSIMRepositoy gsimAccountRepository;

    private final LoanRepository loanRepository;

    @Override
    public GroupSavingsIndividualMonitoring addGSIMAccountInfo(String accountNumber, Group group, BigDecimal parentDeposit,
            Long childAccountsCount, Boolean isAcceptingChild, Integer loanStatus, BigDecimal applicationId) {

        GroupSavingsIndividualMonitoring glimAccountInfo = GroupSavingsIndividualMonitoring.getInstance(accountNumber, group, parentDeposit,
                childAccountsCount, isAcceptingChild, loanStatus, applicationId);

        return this.gsimAccountRepository.save(glimAccountInfo);

    }

    @Override
    public void setIsAcceptingChild(GroupSavingsIndividualMonitoring glimAccount) {
        glimAccount.setIsAcceptingChild(true);
        gsimAccountRepository.save(glimAccount);

    }

    @Override
    public void resetIsAcceptingChild(GroupSavingsIndividualMonitoring glimAccount) {
        glimAccount.setIsAcceptingChild(false);
        gsimAccountRepository.save(glimAccount);

    }

    @Override
    public void incrementChildAccountCount(GroupSavingsIndividualMonitoring glimAccount) {
        long count = glimAccount.getChildAccountsCount();
        glimAccount.setChildAccountsCount(count + 1);
        gsimAccountRepository.save(glimAccount);
    }
}
